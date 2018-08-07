package fi.metatavu.kuntaapi.server.integrations.ptv.updaters;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.cache.ModificationHashCache;
import fi.metatavu.kuntaapi.server.controllers.IdentifierController;
import fi.metatavu.kuntaapi.server.discover.EntityDiscoverJob;
import fi.metatavu.kuntaapi.server.id.CodeId;
import fi.metatavu.kuntaapi.server.index.IndexRequest;
import fi.metatavu.kuntaapi.server.index.IndexableCode;
import fi.metatavu.kuntaapi.server.integrations.KuntaApiIdFactory;
import fi.metatavu.kuntaapi.server.integrations.ptv.CodeType;
import fi.metatavu.kuntaapi.server.integrations.ptv.PtvIdFactory;
import fi.metatavu.kuntaapi.server.integrations.ptv.client.PtvApi;
import fi.metatavu.kuntaapi.server.integrations.ptv.resources.PtvCodeListTask;
import fi.metatavu.kuntaapi.server.integrations.ptv.resources.PtvCodeListTaskQueue;
import fi.metatavu.kuntaapi.server.integrations.ptv.resources.PtvCodeResourceContainer;
import fi.metatavu.kuntaapi.server.integrations.ptv.translation.PtvTranslator;
import fi.metatavu.kuntaapi.server.persistence.model.Identifier;
import fi.metatavu.kuntaapi.server.rest.model.Code;
import fi.metatavu.kuntaapi.server.rest.model.LocalizedValue;
import fi.metatavu.kuntaapi.server.settings.SystemSettingController;
import fi.metatavu.ptv.client.ApiResponse;
import fi.metatavu.ptv.client.CodeListApi;
import fi.metatavu.ptv.client.model.VmOpenApiCodeListItem;
import fi.metatavu.ptv.client.model.VmOpenApiCodeListPage;
import fi.metatavu.ptv.client.model.VmOpenApiDialCodeListItem;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class PtvCodeListEntityDiscoverJob extends EntityDiscoverJob<PtvCodeListTask> {

  @Inject
  private Logger logger;

  @Inject
  private SystemSettingController systemSettingController;
  
  @Inject
  private PtvCodeResourceContainer ptvCodeResourceContainer;

  @Inject
  private PtvApi ptvApi;
  
  @Inject
  private PtvTranslator ptvTranslator;
  
  @Inject
  private KuntaApiIdFactory kuntaApiIdFactory;

  @Inject
  private PtvIdFactory ptvIdFactory;

  @Inject
  private IdentifierController identifierController;

  @Inject
  private Event<IndexRequest> indexRequest;
  
  @Inject
  private PtvCodeListTaskQueue codeListTaskQueue;

  @Inject
  private ModificationHashCache modificationHashCache;

  @Override
  public String getName() {
    return "ptv-codes";
  }

  @Override
  public void timeout() {
    executeNextTask();
  }
  
  @Override
  public void execute(PtvCodeListTask task) {
    executeTask(task);
  }
  
  @Override
  public long getTimerInterval() {
    if (systemSettingController.inTestMode()) {
      return 5000l;
    }
    
    return super.getTimerInterval();
  }
  
  private void executeNextTask() {
    PtvCodeListTask task = codeListTaskQueue.next();
    if (task != null) {
      execute(task);
    } else if (codeListTaskQueue.isEmptyAndLocalNodeResponsible()) {
      fillQueue();
    }
  }
  
  private void executeTask(PtvCodeListTask task) {
    CodeListApi codeListApi = ptvApi.getCodeListApi();
    
    switch (task.getType()) {
      case BUSINESS_REGIONS:
        executeAreaCodeListTask(codeListApi, "BusinessRegions", task.getType());
      break;
      case COUNTRY:
        handleCountryCodeListResponse(codeListApi.apiV8CodeListGetCountryCodesGet(), task.getType());
      break;
      case HOSPITAL_REGIONS:
        executeAreaCodeListTask(codeListApi, "HospitalRegions", task.getType());
      break;
      case LANGUAGE:
        handleCodeListResponse(codeListApi.apiV8CodeListGetLanguageCodesGet(), task.getType());
      break;
      case MUNICIPALITY:
        handleCodeListResponse(codeListApi.apiV8CodeListGetMunicipalityCodesGet(), task.getType());
      break;
      case POSTAL:
        handleCodeListPagedResponse(codeListApi.apiV8CodeListGetPostalCodesGet(task.getPage()), task.getType());
      break;
      case PROVINCE:
        executeAreaCodeListTask(codeListApi, "Province", task.getType());
      break;
    }
    
  }

  private void executeAreaCodeListTask(CodeListApi codeListApi, String areaCodeType, CodeType codeType) {
    handleCodeListResponse(codeListApi.apiV8CodeListGetAreaCodesTypeByTypeGet(areaCodeType), codeType);
  }

  private void handleCodeListResponse(ApiResponse<List<VmOpenApiCodeListItem>> response, CodeType codeType) {
    Long orderIndex = codeType.ordinal() * 100000l;
    
    if (response.isOk()) {
      for (VmOpenApiCodeListItem item : response.getResponse()) {
        handleItem(orderIndex, item, codeType);
        orderIndex++;
      }
    } else {
      logger.log(Level.SEVERE, () -> String.format("Failed to update code list [%d]: %s", response.getStatus(), response.getMessage()));
    }
  }


  private void handleCodeListPagedResponse(ApiResponse<VmOpenApiCodeListPage> response, CodeType codeType) {
    Long orderIndex = codeType.ordinal() * 100000l;
    
    if (response.isOk()) {
      if (response.getResponse() != null) {
        for (VmOpenApiCodeListItem item : response.getResponse().getItemList()) {
          handleItem(orderIndex, item, codeType);
          orderIndex++;
        }
      }
    } else {
      logger.log(Level.SEVERE, () -> String.format("Failed to update paged code list [%d]: %s", response.getStatus(), response.getMessage()));
    }
  }

  private void handleCountryCodeListResponse(ApiResponse<List<VmOpenApiDialCodeListItem>> response, CodeType codeType) {
    Long orderIndex = codeType.ordinal() * 100000l;
    if (response.isOk()) {
      for (VmOpenApiDialCodeListItem item : response.getResponse()) {
        handleDialCodeItem(orderIndex, item, codeType);
        orderIndex++;
      }
    } else {
      logger.log(Level.SEVERE, () -> String.format("Failed to update country code list [%d]: %s", response.getStatus(), response.getMessage()));
    }
  }
  
  private void handleDialCodeItem(Long orderIndex, VmOpenApiDialCodeListItem ptvItem, CodeType codeType) {
    CodeId ptvCodeId = ptvIdFactory.createCodeId(String.format("%s-%s", codeType, ptvItem.getCode()));
    Identifier identifier = identifierController.acquireIdentifier(orderIndex, ptvCodeId);
    CodeId kuntaApiCodeId = kuntaApiIdFactory.createFromIdentifier(CodeId.class, identifier);
    updateItem(orderIndex, kuntaApiCodeId, ptvTranslator.translateCode(kuntaApiCodeId, ptvItem, codeType));
  }

  private void handleItem(Long orderIndex, VmOpenApiCodeListItem ptvItem, CodeType codeType) {
    CodeId ptvCodeId = ptvIdFactory.createCodeId(String.format("%s-%s", codeType, ptvItem.getCode()));
    Identifier identifier = identifierController.acquireIdentifier(orderIndex, ptvCodeId);
    CodeId kuntaApiCodeId = kuntaApiIdFactory.createFromIdentifier(CodeId.class, identifier);
    updateItem(orderIndex, kuntaApiCodeId, ptvTranslator.translateCode(kuntaApiCodeId, ptvItem, codeType));
  }
  
  private void updateItem(Long orderIndex, CodeId kuntaApiCodeId, Code code) {
    if (code != null) {
      modificationHashCache.put(kuntaApiCodeId.getId(), createPojoHash(code));
      ptvCodeResourceContainer.put(kuntaApiCodeId, code);
      index(kuntaApiCodeId.getId(), code, orderIndex);
    } else {
      logger.log(Level.SEVERE, () -> String.format("Failed to translate code %s", kuntaApiCodeId));
    }
  }
  
  private void index(String kuntaApiId, Code code, Long orderIndex) {
    String nameFi = null;
    String nameSv = null;
    String nameEn = null;
    
    for (LocalizedValue localizedValue : code.getNames()) {
      switch (localizedValue.getLanguage()) {
        case "fi":
          nameFi = localizedValue.getValue();
        break;
        case "sv":
          nameSv = localizedValue.getValue();
        break;
        case "en":
          nameEn = localizedValue.getValue();
        break;
        default:
          logger.log(Level.SEVERE, () -> String.format("Unrecognized locale %s", localizedValue.getLanguage()));
      }
    }
    
    IndexableCode indexableCode = new IndexableCode(orderIndex, kuntaApiId, code.getType(), code.getCode(), nameFi, nameSv, nameEn);
    indexRequest.fire(new IndexRequest(indexableCode));  
  }

  private void fillQueue() {
    CodeListApi codeListApi = ptvApi.getCodeListApi();
    
    for (CodeType type : CodeType.values()) {
      if (type == CodeType.POSTAL) {
        fillQueuePostal(codeListApi);
      } else {
        codeListTaskQueue.enqueueTask(new PtvCodeListTask(false, type, 0));
      }
    }
  }

  private void fillQueuePostal(CodeListApi codeListApi) {
    ApiResponse<VmOpenApiCodeListPage> postalCodesResponse = codeListApi.apiV8CodeListGetPostalCodesGet(0);
    if (postalCodesResponse.isOk()) {
      if (postalCodesResponse.getResponse() == null) {
        logger.log(Level.SEVERE, "Failed to read PTV code list size");
      } else {
        for (int page = 0; page < postalCodesResponse.getResponse().getPageCount(); page++) {
          codeListTaskQueue.enqueueTask(new PtvCodeListTask(false, CodeType.POSTAL, page));
        }
      }
    } else {
      logger.log(Level.SEVERE, () -> String.format("Failed to read PTV code list size [%d]: %s", postalCodesResponse.getStatus(), postalCodesResponse.getMessage()));
    }
  }
  
}
