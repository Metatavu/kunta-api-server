package fi.metatavu.kuntaapi.server.integrations.ptv.updaters;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.rest.model.LocalizedValue;
import fi.metatavu.kuntaapi.server.rest.model.Service;
import fi.metatavu.kuntaapi.server.rest.model.ServiceOrganization;
import fi.metatavu.ptv.client.ApiResponse;
import fi.metatavu.ptv.client.model.V6VmOpenApiServiceOrganization;
import fi.metatavu.ptv.client.model.V7VmOpenApiServiceServiceChannel;
import fi.metatavu.ptv.client.model.VmOpenApiItem;
import fi.metatavu.ptv.client.model.V7VmOpenApiService;
import fi.metatavu.kuntaapi.server.cache.ModificationHashCache;
import fi.metatavu.kuntaapi.server.controllers.IdentifierController;
import fi.metatavu.kuntaapi.server.controllers.IdentifierRelationController;
import fi.metatavu.kuntaapi.server.discover.EntityDiscoverJob;
import fi.metatavu.kuntaapi.server.id.ElectronicServiceChannelId;
import fi.metatavu.kuntaapi.server.id.IdController;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.id.PageId;
import fi.metatavu.kuntaapi.server.id.PhoneServiceChannelId;
import fi.metatavu.kuntaapi.server.id.PrintableFormServiceChannelId;
import fi.metatavu.kuntaapi.server.id.ServiceId;
import fi.metatavu.kuntaapi.server.id.ServiceLocationServiceChannelId;
import fi.metatavu.kuntaapi.server.id.WebPageServiceChannelId;
import fi.metatavu.kuntaapi.server.index.IndexRemoveDeprecatedService;
import fi.metatavu.kuntaapi.server.index.IndexRemoveRequest;
import fi.metatavu.kuntaapi.server.index.IndexRemoveService;
import fi.metatavu.kuntaapi.server.index.IndexRequest;
import fi.metatavu.kuntaapi.server.index.IndexableService;
import fi.metatavu.kuntaapi.server.integrations.KuntaApiConsts;
import fi.metatavu.kuntaapi.server.integrations.KuntaApiIdFactory;
import fi.metatavu.kuntaapi.server.integrations.management.ManagementConsts;
import fi.metatavu.kuntaapi.server.integrations.management.ManagementIdFactory;
import fi.metatavu.kuntaapi.server.integrations.management.tasks.PageIdTaskQueue;
import fi.metatavu.kuntaapi.server.integrations.ptv.PtvConsts;
import fi.metatavu.kuntaapi.server.integrations.ptv.PtvIdFactory;
import fi.metatavu.kuntaapi.server.integrations.ptv.PtvServiceResourceContainer;
import fi.metatavu.kuntaapi.server.integrations.ptv.client.PtvApi;
import fi.metatavu.kuntaapi.server.integrations.ptv.tasks.ServiceChannelTasksQueue;
import fi.metatavu.kuntaapi.server.integrations.ptv.tasks.ServiceChannelUpdateTask;
import fi.metatavu.kuntaapi.server.integrations.ptv.tasks.ServiceIdTaskQueue;
import fi.metatavu.kuntaapi.server.integrations.ptv.translation.PtvTranslator;
import fi.metatavu.kuntaapi.server.persistence.model.Identifier;
import fi.metatavu.kuntaapi.server.settings.SystemSettingController;
import fi.metatavu.kuntaapi.server.tasks.IdTask;
import fi.metatavu.kuntaapi.server.tasks.IdTask.Operation;
import fi.metatavu.kuntaapi.server.utils.LocalizationUtils;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class PtvServiceEntityDiscoverJob extends EntityDiscoverJob<IdTask<ServiceId>> {

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

  @Inject
  private ServiceChannelTasksQueue serviceChannelTasksQueue;
  
  @Inject
  private PageIdTaskQueue pageIdTaskQueue;

  @Inject
  private ManagementIdFactory managementIdFactory;
   
  @Override
  public String getName() {
    return "ptv-services";
  }

  @Override
  public void timeout() {
    executeNextTask();
  }
  
  @Override
  public void execute(IdTask<ServiceId> task) {
    if (task.getOperation() == Operation.UPDATE) {
      updatePtvService(task.getId(), task.getOrderIndex()); 
    } else if (task.getOperation() == Operation.REMOVE) {
      deletePtvService(task.getId());
    }
  }
  
  private void executeNextTask() {
    IdTask<ServiceId> task = serviceIdTaskQueue.next();
    if (task != null) {
      execute(task);
    }
  }

  private void updatePtvService(ServiceId ptvServiceId, Long orderIndex) {
    if (!systemSettingController.hasSettingValue(PtvConsts.SYSTEM_SETTING_BASEURL)) {
      logger.log(Level.INFO, "Ptv system setting not defined, skipping update."); 
      return;
    }
    
    ApiResponse<V7VmOpenApiService> response = ptvApi.getServiceApi(null).apiV7ServiceByIdGet(ptvServiceId.getId());
    if (response.isOk()) {
      Identifier identifier = identifierController.acquireIdentifier(orderIndex, ptvServiceId);
      
      V7VmOpenApiService ptvService = response.getResponse();
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
      
      List<PageId> parentPageIds = identifierRelationController.listPageIdsByChildId(ptvServiceId);
      updateParentPageIds(parentPageIds);
    } else {
      logger.warning(String.format("Service %s processing failed on [%d] %s", ptvServiceId.getId(), response.getStatus(), response.getMessage()));
    }
  }

  private void updateParentPageIds(List<PageId> parentPageIds) {
    for (PageId parentPageId : parentPageIds) {
      Identifier parentPageIdentifier = identifierController.findIdentifierById(parentPageId);
      if (ManagementConsts.IDENTIFIER_NAME.equals(parentPageIdentifier.getSource())) {
        // TODO: REFACTOR THIS AWAY FROM THE PTV INTEGRATION PLUGIN
        PageId pageId = managementIdFactory.createFromIdentifier(PageId.class, parentPageIdentifier);
        pageIdTaskQueue.enqueueTask(new IdTask<PageId>(true, Operation.UPDATE, pageId));
      }
    }
  }

  private fi.metatavu.kuntaapi.server.rest.model.Service translateService(V7VmOpenApiService ptvService, ServiceId kuntaApiServiceId) {
    List<V7VmOpenApiServiceServiceChannel> serviceChannels = ptvService.getServiceChannels();
    
    List<ElectronicServiceChannelId> kuntaApiElectronicServiceChannelIds = new ArrayList<>(); 
    List<PhoneServiceChannelId> kuntaApiPhoneServiceChannelIds = new ArrayList<>();
    List<PrintableFormServiceChannelId> kuntaApiPrintableFormServiceChannelIds = new ArrayList<>();
    List<ServiceLocationServiceChannelId> kuntaApiServiceLocationServiceChannelIds = new ArrayList<>();
    List<WebPageServiceChannelId> kuntaApiWebPageServiceChannelIds = new ArrayList<>();
    
    for (V7VmOpenApiServiceServiceChannel serviceChannel : serviceChannels) {
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
      List<WebPageServiceChannelId> kuntaApiWebPageServiceChannelIds, V7VmOpenApiServiceServiceChannel serviceServiceChannel) {
    
    VmOpenApiItem serviceChannel = serviceServiceChannel.getServiceChannel();
    if (serviceChannel == null || serviceChannel.getId() == null) {
      logger.log(Level.WARNING, "Service Service Channel service is null");
      return;
    }
    
    String serviceChannelId = serviceChannel.getId().toString();
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
    serviceChannelTasksQueue.enqueueTask(new ServiceChannelUpdateTask(true, serviceChannelId, null));
  }
  
  private List<ServiceOrganization> translateServiceOrganizations(List<V6VmOpenApiServiceOrganization> ptvServiceOrganizations) {
    if (ptvServiceOrganizations == null) {
      return Collections.emptyList(); 
    }
    
    List<ServiceOrganization> result = new ArrayList<>(ptvServiceOrganizations.size());
    for (V6VmOpenApiServiceOrganization ptvServiceOrganization : ptvServiceOrganizations) {
      VmOpenApiItem organization = ptvServiceOrganization.getOrganization();
      
      if (organization != null && organization.getId() != null) {
        OrganizationId ptvOrganizationId = ptvIdFactory.createOrganizationId(organization.getId());
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
    
    // Remove deprecated version of this service before indexing new. 
    // This can be removed after few weeks of operation
    for (String language : PtvConsts.PTV_SUPPORTED_LANGUAGES) {
      IndexRemoveDeprecatedService removeDeprecatedService = new IndexRemoveDeprecatedService();
      removeDeprecatedService.setLanguage(language);
      removeDeprecatedService.setServiceId(serviceId);
      indexRemoveRequest.fire(new IndexRemoveRequest(removeDeprecatedService));
    }

    Map<String, String> shortDescriptionMap = LocalizationUtils.getLocalizedValueMap("ShortDescription", descriptions);
    Map<String, String> descriptionMap = LocalizationUtils.getLocalizedValueMap("Description", descriptions);
    Map<String, String> userInstructionMap = LocalizationUtils.getLocalizedValueMap("ServiceUserInstruction", descriptions);
    Map<String, String> nameMap = LocalizationUtils.getLocalizedValueMap("Name", names);
    Map<String, String> alternativeNameMap = LocalizationUtils.getLocalizedValueMap("AlternativeName", names);
    
    IndexableService indexableService = new IndexableService();
    indexableService.setShortDescriptionEn(shortDescriptionMap.get("en"));
    indexableService.setShortDescriptionFi(shortDescriptionMap.get("fi"));
    indexableService.setShortDescriptionSv(shortDescriptionMap.get("sv"));
    
    indexableService.setDescriptionEn(descriptionMap.get("en"));
    indexableService.setDescriptionFi(descriptionMap.get("fi"));
    indexableService.setDescriptionSv(descriptionMap.get("sv"));

    indexableService.setUserInstructionEn(userInstructionMap.get("en"));
    indexableService.setUserInstructionFi(userInstructionMap.get("fi"));
    indexableService.setUserInstructionSv(userInstructionMap.get("sv"));

    indexableService.setNameEn(nameMap.get("en"));
    indexableService.setNameFi(nameMap.get("fi"));
    indexableService.setNameSv(nameMap.get("sv"));

    indexableService.setAlternativeNameEn(alternativeNameMap.get("en"));
    indexableService.setAlternativeNameFi(alternativeNameMap.get("fi"));
    indexableService.setAlternativeNameSv(alternativeNameMap.get("sv"));

    indexableService.setKeywordsEn(LocalizationUtils.getLocaleValues(service.getKeywords(), "en"));
    indexableService.setKeywordsFi(LocalizationUtils.getLocaleValues(service.getKeywords(), "fi"));
    indexableService.setKeywordsSv(LocalizationUtils.getLocaleValues(service.getKeywords(), "sv"));
    
    indexableService.setServiceId(serviceId);
    indexableService.setOrganizationIds(organizationIds);
    indexableService.setOrderIndex(orderIndex);
    
    indexableService.setElectronicServiceChannelIds(service.getElectronicServiceChannelIds());
    indexableService.setPhoneServiceChannelIds(service.getPhoneServiceChannelIds());
    indexableService.setServiceLocationServiceChannelIds(service.getServiceLocationServiceChannelIds());
    indexableService.setPrintableFormServiceChannelIds(service.getPrintableFormServiceChannelIds());
    indexableService.setWebPageServiceChannelIds(service.getWebPageServiceChannelIds());
    
    indexRequest.fire(new IndexRequest(indexableService));  
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
      indexRemoveRequest.fire(new IndexRemoveRequest(indexRemove));
    }
  }


}
