package fi.otavanopisto.kuntaapi.server.integrations.ptv.updaters;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.rest.model.ElectronicServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.LocalizedValue;
import fi.metatavu.kuntaapi.server.rest.model.PhoneServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.PrintableFormServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.ServiceLocationServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.WebPageServiceChannel;
import fi.metatavu.ptv.client.model.V6VmOpenApiWebPageChannel;
import fi.metatavu.ptv.client.model.V6VmOpenApiElectronicChannel;
import fi.metatavu.ptv.client.model.V6VmOpenApiPhoneChannel;
import fi.metatavu.ptv.client.model.V6VmOpenApiPrintableFormChannel;
import fi.metatavu.ptv.client.model.V6VmOpenApiServiceLocationChannel;
import fi.otavanopisto.kuntaapi.server.cache.ModificationHashCache;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierRelationController;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.id.ElectronicServiceChannelId;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PageId;
import fi.otavanopisto.kuntaapi.server.id.PhoneServiceChannelId;
import fi.otavanopisto.kuntaapi.server.id.PrintableFormServiceChannelId;
import fi.otavanopisto.kuntaapi.server.id.ServiceLocationServiceChannelId;
import fi.otavanopisto.kuntaapi.server.id.WebPageServiceChannelId;
import fi.otavanopisto.kuntaapi.server.index.IndexRemoveRequest;
import fi.otavanopisto.kuntaapi.server.index.IndexRemoveServiceLocationServiceChannel;
import fi.otavanopisto.kuntaapi.server.index.IndexRequest;
import fi.otavanopisto.kuntaapi.server.index.IndexableServiceLocationServiceChannel;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiIdFactory;
import fi.otavanopisto.kuntaapi.server.integrations.management.ManagementConsts;
import fi.otavanopisto.kuntaapi.server.integrations.management.ManagementIdFactory;
import fi.otavanopisto.kuntaapi.server.integrations.management.tasks.PageIdTaskQueue;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.PtvConsts;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.PtvIdFactory;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.PtvTranslator;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.resources.PtvElectronicServiceChannelResourceContainer;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.resources.PtvPhoneServiceChannelResourceContainer;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.resources.PtvPrintableFormServiceChannelResourceContainer;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.resources.PtvServiceLocationServiceChannelResourceContainer;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.resources.PtvWebPageServiceChannelResourceContainer;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.servicechannels.PtvServiceChannelResolver;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.servicechannels.ServiceChannelType;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.tasks.AbstractServiceChannelTask;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.tasks.ElectronicServiceChannelRemoveTask;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.tasks.PhoneServiceChannelRemoveTask;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.tasks.PrintableFormServiceChannelRemoveTask;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.tasks.ServiceChannelTasksQueue;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.tasks.ServiceChannelUpdateTask;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.tasks.ServiceLocationServiceChannelRemoveTask;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.tasks.WebPageServiceChannelRemoveTask;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask.Operation;
import fi.otavanopisto.kuntaapi.server.utils.LocalizationUtils;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class PtvServiceChannelEntityUpdater extends EntityUpdater {

  private static final String COULD_NOT_TRANSLATE_ORGANIZATION_INTO_KUNTA_API_ID = "Could not translate organization %s into kunta api id";

  @Inject
  private Logger logger;

  @Inject
  private SystemSettingController systemSettingController;

  @Inject
  private KuntaApiIdFactory kuntaApiIdFactory;
  
  @Inject
  private PtvTranslator ptvTranslator;

  @Inject
  private PtvIdFactory ptvIdFactory;

  @Inject
  private IdController idController;

  @Inject
  private IdentifierController identifierController;

  @Inject
  private PtvElectronicServiceChannelResourceContainer ptvElectronicServiceChannelResourceContainer;

  @Inject
  private PtvPhoneServiceChannelResourceContainer ptvPhoneServiceChannelResourceContainer;

  @Inject
  private PtvServiceLocationServiceChannelResourceContainer ptvServiceLocationServiceChannelResourceContainer;
  
  @Inject
  private PtvPrintableFormServiceChannelResourceContainer ptvPrintableFormServiceChannelResourceContainer;

  @Inject
  private PtvWebPageServiceChannelResourceContainer ptvWebPageServiceChannelResourceContainer;

  @Inject
  private Event<IndexRequest> indexRequest;
  
  @Inject
  private Event<IndexRemoveRequest> indexRemoveRequest;

  @Inject
  private ModificationHashCache modificationHashCache;

  @Inject
  private PtvServiceChannelResolver serviceChannelResolver;

  @Inject
  private ServiceChannelTasksQueue serviceChannelTasksQueue;
  
  @Inject
  private IdentifierRelationController identifierRelationController;
  
  @Inject
  private PageIdTaskQueue pageIdTaskQueue;

  @Inject
  private ManagementIdFactory managementIdFactory;
  
  @Override
  public String getName() {
    return "ptv-service-channels";
  }

  @Override
  public void timeout() {
    executeNextTask();
  }
  
  private void executeNextTask() {
    AbstractServiceChannelTask task = serviceChannelTasksQueue.next();
    if (task != null) {
      if (task.getOperation() == Operation.UPDATE) {
        updateServiceChannelChannel((ServiceChannelUpdateTask) task);
      } else if (task.getOperation() == Operation.REMOVE) {
        removeServiceChannelChannel(task);
      }
    }
  }

  private void updateServiceChannelChannel(ServiceChannelUpdateTask task) {
    if (!systemSettingController.hasSettingValue(PtvConsts.SYSTEM_SETTING_BASEURL)) {
      logger.log(Level.INFO, "Ptv system setting not defined, skipping update."); 
      return;
    }

    Map<String, Object> serviceChannelData = serviceChannelResolver.loadServiceChannelData(task.getId());
    ServiceChannelType type = serviceChannelResolver.resolveServiceChannelType(serviceChannelData);
    
    if (type == null) {
      logger.log(Level.WARNING, () -> String.format("ServiceChannel %s does not have a type", task.getId()));
    } else {
      updateServiceChannelChannel(task.getOrderIndex(), type, serviceChannelData);
    }
  }
  
  private void updateServiceChannelChannel(Long orderIndex, ServiceChannelType type, Map<String, Object> serviceChannelData) {
    byte[] channelData = serviceChannelResolver.serializeChannelData(serviceChannelData);
    
    switch (type) {
      case ELECTRONIC_CHANNEL:
        updateElectronicServiceChannel(orderIndex, serviceChannelResolver.unserializeElectronicChannel(channelData));
      break;
      case SERVICE_LOCATION:
        updateServiceLocationServiceChannel(orderIndex, serviceChannelResolver.unserializeServiceLocationChannel(channelData));
      break;
      case PRINTABLE_FORM:
        updatePrintableFormServiceChannel(orderIndex, serviceChannelResolver.unserializePrintableFormChannel(channelData));
      break;
      case PHONE:
        updatePhoneServiceChannel(orderIndex, serviceChannelResolver.unserializePhoneChannel(channelData));
      break;
      case WEB_PAGE:
        updateWebPageServiceChannel(orderIndex, serviceChannelResolver.unserializeWebPageChannel(channelData));
      break;
      default:
        logger.log(Level.SEVERE, () -> String.format("Unknown service channel type %s", type));
      break;
    }
  }
  
  private void updateElectronicServiceChannel(Long orderIndex, V6VmOpenApiElectronicChannel ptvElectronicServiceChannel) {
    if (ptvElectronicServiceChannel == null) {
      return;
    }
    
    OrganizationId ptvOrganizationId = ptvIdFactory.createOrganizationId(ptvElectronicServiceChannel.getOrganizationId());
    OrganizationId kuntaApiOrganizationId = idController.translateOrganizationId(ptvOrganizationId, KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiOrganizationId == null) {
      logger.log(Level.WARNING, () -> String.format(COULD_NOT_TRANSLATE_ORGANIZATION_INTO_KUNTA_API_ID, ptvOrganizationId));
      return;
    }
    
    ElectronicServiceChannelId ptvElectronicServiceChannelId = ptvIdFactory.createElectronicServiceChannelId(ptvElectronicServiceChannel.getId());
    
    Identifier identifier = identifierController.acquireIdentifier(orderIndex, ptvElectronicServiceChannelId);
    ElectronicServiceChannelId kuntaApiElectronicServiceChannelId = kuntaApiIdFactory.createFromIdentifier(ElectronicServiceChannelId.class, identifier);
    ElectronicServiceChannel electronicServiceChannel = ptvTranslator.translateElectronicServiceChannel(kuntaApiElectronicServiceChannelId, kuntaApiOrganizationId, ptvElectronicServiceChannel);
    if (electronicServiceChannel == null) {
      logger.log(Level.WARNING, () -> String.format("Could not translate electronicServiceChannel %s", ptvElectronicServiceChannelId));
      return;
    }
    
    ptvElectronicServiceChannelResourceContainer.put(kuntaApiElectronicServiceChannelId, electronicServiceChannel);
    modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(electronicServiceChannel));
  }

  private void updateServiceLocationServiceChannel(Long orderIndex, V6VmOpenApiServiceLocationChannel ptvServiceLocationServiceChannel) {
    if (ptvServiceLocationServiceChannel == null) {
      return;
    }
    
    OrganizationId ptvOrganizationId = ptvIdFactory.createOrganizationId(ptvServiceLocationServiceChannel.getOrganizationId());
    OrganizationId kuntaApiOrganizationId = idController.translateOrganizationId(ptvOrganizationId, KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiOrganizationId == null) {
      logger.log(Level.WARNING, () -> String.format(COULD_NOT_TRANSLATE_ORGANIZATION_INTO_KUNTA_API_ID, ptvOrganizationId));
      return;
    }
    
    ServiceLocationServiceChannelId ptvServiceLocationServiceChannelId = ptvIdFactory.createServiceLocationServiceChannelId(ptvServiceLocationServiceChannel.getId());
    
    Identifier identifier = identifierController.acquireIdentifier(orderIndex, ptvServiceLocationServiceChannelId);
    ServiceLocationServiceChannelId kuntaApiServiceLocationServiceChannelId = kuntaApiIdFactory.createFromIdentifier(ServiceLocationServiceChannelId.class, identifier);
    ServiceLocationServiceChannel serviceLocationServiceChannel = ptvTranslator.translateServiceLocationServiceChannel(kuntaApiServiceLocationServiceChannelId, kuntaApiOrganizationId, ptvServiceLocationServiceChannel);
    if (serviceLocationServiceChannel == null) {
      logger.log(Level.WARNING, () -> String.format("Could not translate serviceLocationServiceChannel %s", ptvServiceLocationServiceChannelId));
      return;
    }
    
    ptvServiceLocationServiceChannelResourceContainer.put(kuntaApiServiceLocationServiceChannelId, serviceLocationServiceChannel);
    modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(serviceLocationServiceChannel));
    indexServiceLocationChannel(orderIndex, serviceLocationServiceChannel);
    
    List<PageId> parentPageIds = identifierRelationController.listPageIdsByChildId(kuntaApiServiceLocationServiceChannelId);
    updateParentPageIds(parentPageIds);
  }

  private void updatePrintableFormServiceChannel(Long orderIndex, V6VmOpenApiPrintableFormChannel ptvPrintableFormServiceChannel) {
    if (ptvPrintableFormServiceChannel == null) {
      return;
    }
    
    OrganizationId ptvOrganizationId = ptvIdFactory.createOrganizationId(ptvPrintableFormServiceChannel.getOrganizationId());
    OrganizationId kuntaApiOrganizationId = idController.translateOrganizationId(ptvOrganizationId, KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiOrganizationId == null) {
      logger.log(Level.WARNING, () -> String.format(COULD_NOT_TRANSLATE_ORGANIZATION_INTO_KUNTA_API_ID, ptvOrganizationId));
      return;
    }
    
    PrintableFormServiceChannelId ptvPrintableFormServiceChannelId = ptvIdFactory.createPrintableFormServiceChannelId(ptvPrintableFormServiceChannel.getId());
    
    Identifier identifier = identifierController.acquireIdentifier(orderIndex, ptvPrintableFormServiceChannelId);
    PrintableFormServiceChannelId kuntaApiPrintableFormServiceChannelId = kuntaApiIdFactory.createFromIdentifier(PrintableFormServiceChannelId.class, identifier);
    PrintableFormServiceChannel printableFormServiceChannel = ptvTranslator.translatePrintableFormServiceChannel(kuntaApiPrintableFormServiceChannelId, kuntaApiOrganizationId, ptvPrintableFormServiceChannel);
    if (printableFormServiceChannel == null) {
      logger.log(Level.WARNING, () -> String.format("Could not translate printableFormServiceChannel %s", ptvPrintableFormServiceChannelId));
      return;
    }
    
    ptvPrintableFormServiceChannelResourceContainer.put(kuntaApiPrintableFormServiceChannelId, printableFormServiceChannel);
    modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(printableFormServiceChannel));
  }
  
  private void updatePhoneServiceChannel(Long orderIndex, V6VmOpenApiPhoneChannel ptvPhoneServiceChannel) {
    if (ptvPhoneServiceChannel == null) {
      return;
    }
    
    OrganizationId ptvOrganizationId = ptvIdFactory.createOrganizationId(ptvPhoneServiceChannel.getOrganizationId());
    OrganizationId kuntaApiOrganizationId = idController.translateOrganizationId(ptvOrganizationId, KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiOrganizationId == null) {
      logger.log(Level.WARNING, () -> String.format(COULD_NOT_TRANSLATE_ORGANIZATION_INTO_KUNTA_API_ID, ptvOrganizationId));
      return;
    }
    
    PhoneServiceChannelId ptvPhoneServiceChannelId = ptvIdFactory.createPhoneServiceChannelId(ptvPhoneServiceChannel.getId());
    
    Identifier identifier = identifierController.acquireIdentifier(orderIndex, ptvPhoneServiceChannelId);
    PhoneServiceChannelId kuntaApiPhoneServiceChannelId = kuntaApiIdFactory.createFromIdentifier(PhoneServiceChannelId.class, identifier);
    PhoneServiceChannel phoneServiceChannel = ptvTranslator.translatePhoneServiceChannel(kuntaApiPhoneServiceChannelId, kuntaApiOrganizationId, ptvPhoneServiceChannel);
    if (phoneServiceChannel == null) {
      logger.log(Level.WARNING, () -> String.format("Could not translate phoneServiceChannel %s", ptvPhoneServiceChannelId));
      return;
    }
    
    ptvPhoneServiceChannelResourceContainer.put(kuntaApiPhoneServiceChannelId, phoneServiceChannel);
    modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(phoneServiceChannel));
  }
  
  private void updateWebPageServiceChannel(Long orderIndex, V6VmOpenApiWebPageChannel ptvWebPageServiceChannel) {
    if (ptvWebPageServiceChannel == null) {
      return;
    }
    
    OrganizationId ptvOrganizationId = ptvIdFactory.createOrganizationId(ptvWebPageServiceChannel.getOrganizationId());
    OrganizationId kuntaApiOrganizationId = idController.translateOrganizationId(ptvOrganizationId, KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiOrganizationId == null) {
      logger.log(Level.WARNING, () -> String.format(COULD_NOT_TRANSLATE_ORGANIZATION_INTO_KUNTA_API_ID, ptvOrganizationId));
      return;
    }
    
    WebPageServiceChannelId ptvWebPageServiceChannelId = ptvIdFactory.createWebPageServiceChannelId(ptvWebPageServiceChannel.getId());
    
    Identifier identifier = identifierController.acquireIdentifier(orderIndex, ptvWebPageServiceChannelId);
    WebPageServiceChannelId kuntaApiWebPageServiceChannelId = kuntaApiIdFactory.createFromIdentifier(WebPageServiceChannelId.class, identifier);
    WebPageServiceChannel webPageServiceChannel = ptvTranslator.translateWebPageServiceChannel(kuntaApiWebPageServiceChannelId, kuntaApiOrganizationId, ptvWebPageServiceChannel);
    if (webPageServiceChannel == null) {
      logger.log(Level.WARNING, () -> String.format("Could not translate webPageServiceChannel %s", ptvWebPageServiceChannelId));
      return;
    }
    
    ptvWebPageServiceChannelResourceContainer.put(kuntaApiWebPageServiceChannelId, webPageServiceChannel);
    modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(webPageServiceChannel));
  }

  private void removeServiceChannelChannel(AbstractServiceChannelTask task) {
    if (task instanceof ElectronicServiceChannelRemoveTask) {
      removeElectronicServiceChannel(((ElectronicServiceChannelRemoveTask) task).getElectronicServiceChannelId());
    } else if (task instanceof PhoneServiceChannelRemoveTask) {
      removePhoneServiceChannel(((PhoneServiceChannelRemoveTask) task).getPhoneServiceChannelId());
    } else if (task instanceof PrintableFormServiceChannelRemoveTask) {
      removePrintableFormServiceChannel(((PrintableFormServiceChannelRemoveTask) task).getPrintableFormServiceChannelId());
    } else if (task instanceof ServiceLocationServiceChannelRemoveTask) {
      removeServiceLocationServiceChannel(((ServiceLocationServiceChannelRemoveTask) task).getServiceLocationServiceChannelId());
    } else if (task instanceof WebPageServiceChannelRemoveTask) {
      removeWebPageServiceChannel(((WebPageServiceChannelRemoveTask) task).getWebPageServiceChannelId());
    }
  }

  private void removeElectronicServiceChannel(ElectronicServiceChannelId electronicServiceChannelId) {
    Identifier electronicServiceChannelIdentifier = identifierController.findIdentifierById(electronicServiceChannelId);
    if (electronicServiceChannelIdentifier != null) {
      ElectronicServiceChannelId kuntaApiElectronicServiceChannelId = kuntaApiIdFactory.createFromIdentifier(ElectronicServiceChannelId.class, electronicServiceChannelIdentifier);
      modificationHashCache.clear(electronicServiceChannelIdentifier.getKuntaApiId());
      ptvElectronicServiceChannelResourceContainer.clear(kuntaApiElectronicServiceChannelId);
      identifierController.deleteIdentifier(electronicServiceChannelIdentifier);      
    }
  }

  private void removePhoneServiceChannel(PhoneServiceChannelId phoneServiceChannelId) {
    Identifier phoneServiceChannelIdentifier = identifierController.findIdentifierById(phoneServiceChannelId);
    if (phoneServiceChannelIdentifier != null) {
      PhoneServiceChannelId kuntaApiPhoneServiceChannelId = kuntaApiIdFactory.createFromIdentifier(PhoneServiceChannelId.class, phoneServiceChannelIdentifier);
      modificationHashCache.clear(phoneServiceChannelIdentifier.getKuntaApiId());
      ptvPhoneServiceChannelResourceContainer.clear(kuntaApiPhoneServiceChannelId);
      identifierController.deleteIdentifier(phoneServiceChannelIdentifier);      
    }
  }

  private void removePrintableFormServiceChannel(PrintableFormServiceChannelId printableFormServiceChannelId) {
    Identifier printableFormServiceChannelIdentifier = identifierController.findIdentifierById(printableFormServiceChannelId);
    if (printableFormServiceChannelIdentifier != null) {
      PrintableFormServiceChannelId kuntaApiPrintableFormServiceChannelId = kuntaApiIdFactory.createFromIdentifier(PrintableFormServiceChannelId.class, printableFormServiceChannelIdentifier);
      modificationHashCache.clear(printableFormServiceChannelIdentifier.getKuntaApiId());
      ptvPrintableFormServiceChannelResourceContainer.clear(kuntaApiPrintableFormServiceChannelId);
      identifierController.deleteIdentifier(printableFormServiceChannelIdentifier);      
    }
  }

  private void removeServiceLocationServiceChannel(ServiceLocationServiceChannelId serviceLocationServiceChannelId) {
    Identifier serviceLocationServiceChannelIdentifier = identifierController.findIdentifierById(serviceLocationServiceChannelId);
    if (serviceLocationServiceChannelIdentifier != null) {
      ServiceLocationServiceChannelId kuntaApiServiceLocationServiceChannelId = kuntaApiIdFactory.createFromIdentifier(ServiceLocationServiceChannelId.class, serviceLocationServiceChannelIdentifier);
      
      for (String language : PtvConsts.PTV_SUPPORTED_LANGUAGES) {
        IndexRemoveServiceLocationServiceChannel removeServiceLocationServiceChannel = new IndexRemoveServiceLocationServiceChannel();
        removeServiceLocationServiceChannel.setLanguage(language);
        removeServiceLocationServiceChannel.setServiceLocationServiceChannelId(kuntaApiServiceLocationServiceChannelId.getId());
        indexRemoveRequest.fire(new IndexRemoveRequest(removeServiceLocationServiceChannel));
      }

      modificationHashCache.clear(serviceLocationServiceChannelIdentifier.getKuntaApiId());
      ptvServiceLocationServiceChannelResourceContainer.clear(kuntaApiServiceLocationServiceChannelId);
      identifierController.deleteIdentifier(serviceLocationServiceChannelIdentifier);    
    }
  }

  private void removeWebPageServiceChannel(WebPageServiceChannelId webPageServiceChannelId) {
    Identifier webPageServiceChannelIdentifier = identifierController.findIdentifierById(webPageServiceChannelId);
    if (webPageServiceChannelIdentifier != null) {
      WebPageServiceChannelId kuntaApiWebPageServiceChannelId = kuntaApiIdFactory.createFromIdentifier(WebPageServiceChannelId.class, webPageServiceChannelIdentifier);
      modificationHashCache.clear(webPageServiceChannelIdentifier.getKuntaApiId());
      ptvWebPageServiceChannelResourceContainer.clear(kuntaApiWebPageServiceChannelId);
      identifierController.deleteIdentifier(webPageServiceChannelIdentifier);      
    }
  }
  
  private void indexServiceLocationChannel(Long orderIndex, ServiceLocationServiceChannel serviceLocationServiceChannel) {
    List<LocalizedValue> names = serviceLocationServiceChannel.getNames();
    List<LocalizedValue> descriptions = serviceLocationServiceChannel.getDescriptions();
    
    for (String language : LocalizationUtils.getListsLanguages(names, descriptions)) {
      IndexableServiceLocationServiceChannel indexableServiceLocationServiceChannel = new IndexableServiceLocationServiceChannel();
      
      indexableServiceLocationServiceChannel.setDescription(LocalizationUtils.getLocaleValue(descriptions, "ShortDescription", language));
      indexableServiceLocationServiceChannel.setLanguage(language);
      indexableServiceLocationServiceChannel.setName(LocalizationUtils.getLocaleValue(names, "Name", language));
      indexableServiceLocationServiceChannel.setOrderIndex(orderIndex);
      indexableServiceLocationServiceChannel.setOrganizationId(serviceLocationServiceChannel.getOrganizationId());
      indexableServiceLocationServiceChannel.setServiceLocationServiceChannelId(serviceLocationServiceChannel.getId());
      
      indexRequest.fire(new IndexRequest(indexableServiceLocationServiceChannel));
    }
  }

  private void updateParentPageIds(List<PageId> parentPageIds) {
    for (PageId parentPageId : parentPageIds) {
      Identifier parentPageIdentifier = identifierController.findIdentifierById(parentPageId);
      if (ManagementConsts.IDENTIFIER_NAME.equals(parentPageIdentifier.getSource())) {
        // TODO: REFACTOR THIS AWAY FROM THE PTV INTEGRATION PLUGIN
        PageId pageId = managementIdFactory.createFromIdentifier(PageId.class, parentPageIdentifier);
        pageIdTaskQueue.enqueueTask(true, new IdTask<PageId>(Operation.UPDATE, pageId));
      }
    }
  }

}
