package fi.otavanopisto.kuntaapi.server.integrations.ptv;

import java.util.ArrayList;
import java.util.List;
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

import fi.otavanopisto.kuntaapi.server.cache.ModificationHashCache;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
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
import fi.otavanopisto.kuntaapi.server.integrations.management.ManagementConsts;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.tasks.ServiceIdTaskQueue;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask.Operation;
import fi.otavanopisto.kuntaapi.server.utils.LocalizationUtils;
import fi.metatavu.restfulptv.client.ApiResponse;
import fi.metatavu.restfulptv.client.model.LocalizedListItem;
import fi.metatavu.restfulptv.client.model.Service;
import fi.metatavu.restfulptv.client.model.StatutoryDescription;

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
  private PtvTranslator ptvTranslator;
  
  @Inject
  private IdController idController;
  
  @Inject
  private IdentifierController identifierController;

  @Inject
  private PtvServiceCache ptvServiceCache;

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

  private void updatePtvService(ServiceId serviceId, Long orderIndex) {
    if (!systemSettingController.hasSettingValue(PtvConsts.SYSTEM_SETTING_BASEURL)) {
      logger.log(Level.INFO, "Ptv system setting not defined, skipping update."); 
      return;
    }
    
    ApiResponse<Service> response = ptvApi.getServicesApi().findService(serviceId.getId());
    if (response.isOk()) {
      Identifier identifier = identifierController.acquireIdentifier(orderIndex, serviceId);
      
      Service ptvService = response.getResponse();
      ServiceId kuntaApiServiceId = new ServiceId(KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
      StatutoryDescription ptvStatutoryDescription = ptvService.getStatutoryDescriptionId() != null ? getStatutoryDescription(ptvService.getStatutoryDescriptionId()) : null;
      
      fi.metatavu.kuntaapi.server.rest.model.Service service = translateService(ptvService, kuntaApiServiceId, ptvStatutoryDescription);
      if (service != null) {
        ptvServiceCache.put(kuntaApiServiceId, service);
        modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(service));
        index(identifier.getKuntaApiId(), ptvService);
      }
      
    } else {
      logger.warning(String.format("Service %s processing failed on [%d] %s", serviceId.getId(), response.getStatus(), response.getMessage()));
    }
  }

  @SuppressWarnings ("squid:MethodCyclomaticComplexity")
  private fi.metatavu.kuntaapi.server.rest.model.Service translateService(Service ptvService,
      ServiceId kuntaApiServiceId, StatutoryDescription ptvStatutoryDescription) {
    
    List<ElectronicServiceChannelId> kuntaApiElectronicServiceChannelIds = new ArrayList<>(ptvService.getElectronicServiceChannelIds().size()); 
    List<PhoneServiceChannelId> kuntaApiPhoneServiceChannelIds = new ArrayList<>(ptvService.getPhoneServiceChannelIds().size());
    List<PrintableFormServiceChannelId> kuntaApiPrintableFormServiceChannelIds = new ArrayList<>(ptvService.getPrintableFormServiceChannelIds().size());
    List<ServiceLocationServiceChannelId> kuntaApiServiceLocationServiceChannelIds = new ArrayList<>(ptvService.getServiceLocationServiceChannelIds().size());
    List<WebPageServiceChannelId> kuntaApiWebPageServiceChannelIds = new ArrayList<>(ptvService.getWebPageServiceChannelIds().size());
    
    for (String ptvElectronicServiceChannelId : ptvService.getElectronicServiceChannelIds()) {
      ElectronicServiceChannelId kuntaApiElectronicServiceChannelId = idController.translateElectronicServiceChannelId(ptvIdFactory.createElectronicServiceChannelId(ptvElectronicServiceChannelId), KuntaApiConsts.IDENTIFIER_NAME);
      if (kuntaApiElectronicServiceChannelId == null) {
        logger.log(Level.INFO, () -> String.format("Could not translate electronic channel id %s into KuntaAPI id.", ptvElectronicServiceChannelId)); 
        return null;
      } 
      
      kuntaApiElectronicServiceChannelIds.add(kuntaApiElectronicServiceChannelId);
    }

    for (String ptvPhoneServiceChannelId : ptvService.getPhoneServiceChannelIds()) {
      PhoneServiceChannelId kuntaApiPhoneServiceChannelId = idController.translatePhoneServiceChannelId(ptvIdFactory.createPhoneServiceChannelId(ptvPhoneServiceChannelId), KuntaApiConsts.IDENTIFIER_NAME);
      if (kuntaApiPhoneServiceChannelId == null) {
        logger.log(Level.INFO, () -> String.format("Could not translate phone channel id %s into KuntaAPI id.", ptvPhoneServiceChannelId)); 
        return null;
      } 
      
      kuntaApiPhoneServiceChannelIds.add(kuntaApiPhoneServiceChannelId);
    }
    
    for (String ptvServiceLocationServiceChannelId : ptvService.getServiceLocationServiceChannelIds()) {
      ServiceLocationServiceChannelId kuntaApiServiceLocationServiceChannelId = idController.translateServiceLocationServiceChannelId(ptvIdFactory.createServiceLocationServiceChannelId(ptvServiceLocationServiceChannelId), KuntaApiConsts.IDENTIFIER_NAME);
      if (kuntaApiServiceLocationServiceChannelId == null) {
        logger.log(Level.INFO, () -> String.format("Could not translate serviceLocation channel id %s into KuntaAPI id.", ptvServiceLocationServiceChannelId)); 
        return null;
      } 
      
      kuntaApiServiceLocationServiceChannelIds.add(kuntaApiServiceLocationServiceChannelId);
    }
    
    for (String ptvPrintableFormServiceChannelId : ptvService.getPrintableFormServiceChannelIds()) {
      PrintableFormServiceChannelId kuntaApiPrintableFormServiceChannelId = idController.translatePrintableFormServiceChannelId(ptvIdFactory.createPrintableFormServiceChannelId(ptvPrintableFormServiceChannelId), KuntaApiConsts.IDENTIFIER_NAME);
      if (kuntaApiPrintableFormServiceChannelId == null) {
        logger.log(Level.INFO, () -> String.format("Could not translate printable form channel id %s into KuntaAPI id.", ptvPrintableFormServiceChannelId)); 
        return null;
      } 
      
      kuntaApiPrintableFormServiceChannelIds.add(kuntaApiPrintableFormServiceChannelId);
    }
    
    for (String ptvWebPageServiceChannelId : ptvService.getWebPageServiceChannelIds()) {
      WebPageServiceChannelId kuntaApiWebPageServiceChannelId = idController.translateWebPageServiceChannelId(ptvIdFactory.createWebPageServiceChannelId(ptvWebPageServiceChannelId), KuntaApiConsts.IDENTIFIER_NAME);
      if (kuntaApiWebPageServiceChannelId == null) {
        logger.log(Level.INFO, () -> String.format("Could not translate web page channel id %s into KuntaAPI id.", ptvWebPageServiceChannelId)); 
        return null;
      } 
      
      kuntaApiWebPageServiceChannelIds.add(kuntaApiWebPageServiceChannelId);
    }
    
    return ptvTranslator.translateService(kuntaApiServiceId,
        kuntaApiElectronicServiceChannelIds,
        kuntaApiPhoneServiceChannelIds,
        kuntaApiPrintableFormServiceChannelIds,
        kuntaApiServiceLocationServiceChannelIds,
        kuntaApiWebPageServiceChannelIds,
        ptvService, 
        ptvStatutoryDescription);
  }
  
  private StatutoryDescription getStatutoryDescription(String statutoryDescriptionId) {
    ApiResponse<StatutoryDescription> response = ptvApi.getStatutoryDescriptionsApi().findStatutoryDescription(statutoryDescriptionId);
    if (response.isOk()) {
      return response.getResponse();
    } 
    
    logger.warning(String.format("StatutoryDescription %s could not be loaded [%d] %s", statutoryDescriptionId, response.getStatus(), response.getMessage()));
    return null;
  }
  
  private void index(String serviceId, Service service) {
    List<LocalizedListItem> descriptions = service.getDescriptions();
    List<LocalizedListItem> names = service.getNames();
    List<String> ptvOrganizationIds = service.getOrganizationIds();
    List<String> organizationIds = new ArrayList<>(ptvOrganizationIds.size());
    
    for (String ptvOrganizationId : ptvOrganizationIds) {
      OrganizationId kuntaApiOrganizationId = idController.translateOrganizationId(new OrganizationId(PtvConsts.IDENTIFIER_NAME, ptvOrganizationId), KuntaApiConsts.IDENTIFIER_NAME);
      if (kuntaApiOrganizationId != null) {
        organizationIds.add(kuntaApiOrganizationId.getId());
      } else {
        logger.warning(String.format("Could not translate organization %s into Kunta API id", ptvOrganizationId));
      }
    }
    
    for (String language : LocalizationUtils.getListsLanguages(names, descriptions)) {
      IndexableService indexableService = new IndexableService();
      indexableService.setShortDescription(LocalizationUtils.getBestMatchingValue("ShortDescription", descriptions, language, PtvConsts.DEFAULT_LANGUAGE));
      indexableService.setDescription(LocalizationUtils.getBestMatchingValue("Description", descriptions, language, PtvConsts.DEFAULT_LANGUAGE));
      indexableService.setUserInstruction(LocalizationUtils.getBestMatchingValue("ServiceUserInstruction", descriptions, language, PtvConsts.DEFAULT_LANGUAGE));
      indexableService.setKeywords(service.getKeywords());
      indexableService.setLanguage(language);
      indexableService.setName(LocalizationUtils.getBestMatchingValue("Name", names, language, PtvConsts.DEFAULT_LANGUAGE));
      indexableService.setAlternativeName(LocalizationUtils.getBestMatchingValue("AlternativeName", names, language, PtvConsts.DEFAULT_LANGUAGE));
      indexableService.setServiceId(serviceId);
      indexableService.setOrganizationIds(organizationIds);
      
      indexRequest.fire(new IndexRequest(indexableService));
    }
    
  }
  
  private void deletePtvService(ServiceId ptvServiceId) {
    Identifier serviceIdentifier = identifierController.findIdentifierById(ptvServiceId);
    if (serviceIdentifier != null) {
      ServiceId kuntaApiServiceId = new ServiceId(KuntaApiConsts.IDENTIFIER_NAME, serviceIdentifier.getKuntaApiId());
      modificationHashCache.clear(serviceIdentifier.getKuntaApiId());
      ptvServiceCache.clear(kuntaApiServiceId);
      identifierController.deleteIdentifier(serviceIdentifier);
      
      IndexRemoveService indexRemove = new IndexRemoveService();
      indexRemove.setServiceId(kuntaApiServiceId.getId());
      indexRemove.setLanguage(ManagementConsts.DEFAULT_LOCALE);
      indexRemoveRequest.fire(new IndexRemoveRequest(indexRemove));
    }
  }
  

}
