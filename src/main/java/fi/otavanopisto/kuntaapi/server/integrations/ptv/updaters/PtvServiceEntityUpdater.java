package fi.otavanopisto.kuntaapi.server.integrations.ptv.updaters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.ejb.TimerService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.rest.model.LocalizedValue;
import fi.metatavu.kuntaapi.server.rest.model.Service;
import fi.metatavu.kuntaapi.server.rest.model.ServiceOrganization;
import fi.metatavu.ptv.client.ApiResponse;
import fi.metatavu.ptv.client.model.V4VmOpenApiService;
import fi.metatavu.ptv.client.model.V4VmOpenApiServiceOrganization;
import fi.metatavu.ptv.client.model.V4VmOpenApiServiceServiceChannel;
import fi.otavanopisto.kuntaapi.server.cache.ModificationHashCache;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierRelationController;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.id.ElectronicServiceChannelId;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PhoneServiceChannelId;
import fi.otavanopisto.kuntaapi.server.id.PrintableFormServiceChannelId;
import fi.otavanopisto.kuntaapi.server.id.ServiceId;
import fi.otavanopisto.kuntaapi.server.id.ServiceLocationServiceChannelId;
import fi.otavanopisto.kuntaapi.server.id.WebPageServiceChannelId;
import fi.otavanopisto.kuntaapi.server.index.IndexRemoveRequest;
import fi.otavanopisto.kuntaapi.server.index.IndexRemoveService;
import fi.otavanopisto.kuntaapi.server.index.IndexRequest;
import fi.otavanopisto.kuntaapi.server.index.IndexableService;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiIdFactory;
import fi.otavanopisto.kuntaapi.server.integrations.management.ManagementConsts;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.PtvConsts;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.PtvIdFactory;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.PtvServiceResourceContainer;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.PtvTranslator;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.client.PtvApi;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.tasks.ServiceIdTaskQueue;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask.Operation;
import fi.otavanopisto.kuntaapi.server.utils.LocalizationUtils;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class PtvServiceEntityUpdater extends EntityUpdater {

  @Inject
  private Logger logger;

  @Inject  
  private SystemSettingController systemSettingController;

  @Inject
  private PtvApi ptvApi;
  
  @Inject
  private PtvIdFactory ptvIdFactory;

  @Inject
  private KuntaApiIdFactory kuntaApiIdFactory;
  
  @Inject
  private PtvTranslator ptvTranslator;
  
  @Inject
  private IdController idController;
  
  @Inject
  private IdentifierController identifierController;
  
  @Inject
  private IdentifierRelationController identifierRelationController;

  @Inject
  private PtvServiceResourceContainer ptvServiceResourceContainer;

  @Inject
  private ModificationHashCache modificationHashCache;
  
  @Inject
  private ServiceIdTaskQueue serviceIdTaskQueue;

  @Inject
  private Event<IndexRequest> indexRequest;
  
  @Inject
  private Event<IndexRemoveRequest> indexRemoveRequest;

  @Resource
  private TimerService timerService;

  @Override
  public String getName() {
    return "ptv-services";
  }

  @Override
  public void timeout() {
    executeNextTask();
  }
  
  @Override
  public TimerService getTimerService() {
    return timerService;
  }
  
  private void executeNextTask() {
    IdTask<ServiceId> task = serviceIdTaskQueue.next();
    if (task != null) {
      if (task.getOperation() == Operation.UPDATE) {
        updatePtvService(task.getId(), task.getOrderIndex()); 
      } else if (task.getOperation() == Operation.REMOVE) {
        deletePtvService(task.getId());
      }
    }
  }

  private void updatePtvService(ServiceId ptvServiceId, Long orderIndex) {
    if (!systemSettingController.hasSettingValue(PtvConsts.SYSTEM_SETTING_BASEURL)) {
      logger.log(Level.INFO, "Ptv system setting not defined, skipping update."); 
      return;
    }
    
    ApiResponse<V4VmOpenApiService> response = ptvApi.getServiceApi().apiV4ServiceByIdGet(ptvServiceId.getId());
    if (response.isOk()) {
      Identifier identifier = identifierController.acquireIdentifier(orderIndex, ptvServiceId);
      
      V4VmOpenApiService ptvService = response.getResponse();
      ServiceId kuntaApiServiceId = kuntaApiIdFactory.createFromIdentifier(ServiceId.class, identifier);
      
      fi.metatavu.kuntaapi.server.rest.model.Service service = translateService(ptvService, kuntaApiServiceId);

      if (service != null) {
        ptvServiceResourceContainer.put(kuntaApiServiceId, service);
        modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(service));
        
        Set<String> kuntaApiServiceOrganizationIds = new HashSet<>(service.getOrganizations().size());
        for (ServiceOrganization serviceOrganization : service.getOrganizations()) {
          kuntaApiServiceOrganizationIds.add(serviceOrganization.getOrganizationId());
        }
        
        for (String kuntaApiServiceOrganizationId : kuntaApiServiceOrganizationIds) {
          Identifier serviceOrganizationIdentifier = identifierController.findIdentifierById(kuntaApiIdFactory.createOrganizationId(kuntaApiServiceOrganizationId));
          identifierRelationController.addChild(serviceOrganizationIdentifier, identifier);
        }
        
        index(identifier.getKuntaApiId(), service, orderIndex);
      }
    } else {
      logger.warning(String.format("Service %s processing failed on [%d] %s", ptvServiceId.getId(), response.getStatus(), response.getMessage()));
    }
  }

  private fi.metatavu.kuntaapi.server.rest.model.Service translateService(V4VmOpenApiService ptvService, ServiceId kuntaApiServiceId) {
    List<V4VmOpenApiServiceServiceChannel> serviceChannels = ptvService.getServiceChannels();
    
    List<ElectronicServiceChannelId> kuntaApiElectronicServiceChannelIds = new ArrayList<>(); 
    List<PhoneServiceChannelId> kuntaApiPhoneServiceChannelIds = new ArrayList<>();
    List<PrintableFormServiceChannelId> kuntaApiPrintableFormServiceChannelIds = new ArrayList<>();
    List<ServiceLocationServiceChannelId> kuntaApiServiceLocationServiceChannelIds = new ArrayList<>();
    List<WebPageServiceChannelId> kuntaApiWebPageServiceChannelIds = new ArrayList<>();
    
    for (V4VmOpenApiServiceServiceChannel serviceChannel : serviceChannels) {
      sortServiceChannel(kuntaApiElectronicServiceChannelIds, kuntaApiPhoneServiceChannelIds,
          kuntaApiPrintableFormServiceChannelIds, kuntaApiServiceLocationServiceChannelIds,
          kuntaApiWebPageServiceChannelIds, serviceChannel);
    }
    
    List<ServiceOrganization> serviceOrganizations = translateServiceOrganizations(ptvService.getOrganizations());
    
    return ptvTranslator.translateService(kuntaApiServiceId,
        kuntaApiElectronicServiceChannelIds,
        kuntaApiPhoneServiceChannelIds,
        kuntaApiPrintableFormServiceChannelIds,
        kuntaApiServiceLocationServiceChannelIds,
        kuntaApiWebPageServiceChannelIds,
        serviceOrganizations,
        ptvService);
  }

  private void sortServiceChannel(List<ElectronicServiceChannelId> kuntaApiElectronicServiceChannelIds,
      List<PhoneServiceChannelId> kuntaApiPhoneServiceChannelIds,
      List<PrintableFormServiceChannelId> kuntaApiPrintableFormServiceChannelIds,
      List<ServiceLocationServiceChannelId> kuntaApiServiceLocationServiceChannelIds,
      List<WebPageServiceChannelId> kuntaApiWebPageServiceChannelIds, V4VmOpenApiServiceServiceChannel serviceChannel) {
    String serviceChannelId = serviceChannel.getServiceChannelId();
    
    ElectronicServiceChannelId kuntaApiElectronicServiceChannelId = idController.translateElectronicServiceChannelId(ptvIdFactory.createElectronicServiceChannelId(serviceChannelId), KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiElectronicServiceChannelId != null) {
      kuntaApiElectronicServiceChannelIds.add(kuntaApiElectronicServiceChannelId);
      return;
    }
    
    PhoneServiceChannelId kuntaApiPhoneServiceChannelId = idController.translatePhoneServiceChannelId(ptvIdFactory.createPhoneServiceChannelId(serviceChannelId), KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiPhoneServiceChannelId != null) {
      kuntaApiPhoneServiceChannelIds.add(kuntaApiPhoneServiceChannelId);
      return;
    } 
    
    PrintableFormServiceChannelId kuntaApiPrintableFormServiceChannelId = idController.translatePrintableFormServiceChannelId(ptvIdFactory.createPrintableFormServiceChannelId(serviceChannelId), KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiPrintableFormServiceChannelId != null) {
      kuntaApiPrintableFormServiceChannelIds.add(kuntaApiPrintableFormServiceChannelId);
      return;
    } 
    
    ServiceLocationServiceChannelId kuntaApiServiceLocationServiceChannelId = idController.translateServiceLocationServiceChannelId(ptvIdFactory.createServiceLocationServiceChannelId(serviceChannelId), KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiServiceLocationServiceChannelId != null) {
      kuntaApiServiceLocationServiceChannelIds.add(kuntaApiServiceLocationServiceChannelId);
      return;
    } 
  
    WebPageServiceChannelId kuntaApiWebPageServiceChannelId = idController.translateWebPageServiceChannelId(ptvIdFactory.createWebPageServiceChannelId(serviceChannelId), KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiWebPageServiceChannelId != null) {
      kuntaApiWebPageServiceChannelIds.add(kuntaApiWebPageServiceChannelId);
      return;
    } 
    
    logger.log(Level.WARNING, () -> String.format("Failed to resolve service channel %s type", serviceChannelId));
  }
  
  private List<ServiceOrganization> translateServiceOrganizations(List<V4VmOpenApiServiceOrganization> ptvServiceOrganizations) {
    if (ptvServiceOrganizations == null) {
      return Collections.emptyList(); 
    }
    
    List<ServiceOrganization> result = new ArrayList<>(ptvServiceOrganizations.size());
    for (V4VmOpenApiServiceOrganization ptvServiceOrganization : ptvServiceOrganizations) {
      if (ptvServiceOrganization.getOrganizationId() != null) {
        OrganizationId ptvOrganizationId = ptvIdFactory.createOrganizationId(ptvServiceOrganization.getOrganizationId());
        OrganizationId kuntaApiOrganizationId = idController.translateOrganizationId(ptvOrganizationId, KuntaApiConsts.IDENTIFIER_NAME);
        if (kuntaApiOrganizationId != null) {
          result.add(ptvTranslator.translateServiceOrganization(kuntaApiOrganizationId, ptvServiceOrganization));
        } else {
          logger.log(Level.SEVERE, () -> String.format("Failed to translate organization %s into Kunta API id", ptvOrganizationId));
        }
      }
    }
    
    return result;
  }
  
  private void index(String serviceId, Service service, Long orderIndex) {
    List<LocalizedValue> descriptions = service.getDescriptions();
    List<LocalizedValue> names = service.getNames();
    
    List<String> organizationIds = new ArrayList<>(service.getOrganizations().size());
    for (ServiceOrganization serviceOrganization : service.getOrganizations()) {
      organizationIds.add(serviceOrganization.getOrganizationId());
    }
    
    for (String language : LocalizationUtils.getListsLanguages(names, descriptions)) {
      IndexableService indexableService = new IndexableService();
      indexableService.setShortDescription(LocalizationUtils.getBestMatchingValue("ShortDescription", descriptions, language, PtvConsts.DEFAULT_LANGUAGE));
      indexableService.setDescription(LocalizationUtils.getBestMatchingValue("Description", descriptions, language, PtvConsts.DEFAULT_LANGUAGE));
      indexableService.setUserInstruction(LocalizationUtils.getBestMatchingValue("ServiceUserInstruction", descriptions, language, PtvConsts.DEFAULT_LANGUAGE));
      indexableService.setKeywords(LocalizationUtils.getLocaleValues(service.getKeywords(), PtvConsts.DEFAULT_LANGUAGE));
      indexableService.setLanguage(language);
      indexableService.setName(LocalizationUtils.getBestMatchingValue("Name", names, language, PtvConsts.DEFAULT_LANGUAGE));
      indexableService.setAlternativeName(LocalizationUtils.getBestMatchingValue("AlternativeName", names, language, PtvConsts.DEFAULT_LANGUAGE));
      indexableService.setServiceId(serviceId);
      indexableService.setOrganizationIds(organizationIds);
      indexableService.setOrderIndex(orderIndex);
      
      indexRequest.fire(new IndexRequest(indexableService));
    }
    
  }
  
  private void deletePtvService(ServiceId ptvServiceId) {
    Identifier serviceIdentifier = identifierController.findIdentifierById(ptvServiceId);
    if (serviceIdentifier != null) {
      ServiceId kuntaApiServiceId = new ServiceId(KuntaApiConsts.IDENTIFIER_NAME, serviceIdentifier.getKuntaApiId());
      modificationHashCache.clear(serviceIdentifier.getKuntaApiId());
      ptvServiceResourceContainer.clear(kuntaApiServiceId);
      identifierController.deleteIdentifier(serviceIdentifier);
      
      IndexRemoveService indexRemove = new IndexRemoveService();
      indexRemove.setServiceId(kuntaApiServiceId.getId());
      indexRemove.setLanguage(ManagementConsts.DEFAULT_LOCALE);
      indexRemoveRequest.fire(new IndexRemoveRequest(indexRemove));
    }
  }


}
