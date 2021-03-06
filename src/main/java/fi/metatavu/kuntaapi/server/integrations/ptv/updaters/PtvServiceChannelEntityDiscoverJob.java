package fi.metatavu.kuntaapi.server.integrations.ptv.updaters;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.jboss.ejb3.annotation.Pool;

import fi.metatavu.kuntaapi.server.cache.ModificationHashCache;
import fi.metatavu.kuntaapi.server.controllers.IdentifierController;
import fi.metatavu.kuntaapi.server.controllers.IdentifierRelationController;
import fi.metatavu.kuntaapi.server.id.ElectronicServiceChannelId;
import fi.metatavu.kuntaapi.server.id.IdController;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.id.PageId;
import fi.metatavu.kuntaapi.server.id.PhoneServiceChannelId;
import fi.metatavu.kuntaapi.server.id.PrintableFormServiceChannelId;
import fi.metatavu.kuntaapi.server.id.ServiceLocationServiceChannelId;
import fi.metatavu.kuntaapi.server.id.WebPageServiceChannelId;
import fi.metatavu.kuntaapi.server.index.AbstractIndexableServiceChannel;
import fi.metatavu.kuntaapi.server.index.IndexRemoveDeprecatedServiceLocationServiceChannel;
import fi.metatavu.kuntaapi.server.index.IndexRemoveRequest;
import fi.metatavu.kuntaapi.server.index.IndexRequest;
import fi.metatavu.kuntaapi.server.index.IndexableElectronicServiceChannel;
import fi.metatavu.kuntaapi.server.index.IndexablePhoneServiceChannel;
import fi.metatavu.kuntaapi.server.index.IndexablePrintableFormServiceChannel;
import fi.metatavu.kuntaapi.server.index.IndexableServiceLocationServiceChannel;
import fi.metatavu.kuntaapi.server.index.IndexableWebPageServiceChannel;
import fi.metatavu.kuntaapi.server.integrations.KuntaApiConsts;
import fi.metatavu.kuntaapi.server.integrations.KuntaApiIdFactory;
import fi.metatavu.kuntaapi.server.integrations.management.ManagementConsts;
import fi.metatavu.kuntaapi.server.integrations.management.ManagementIdFactory;
import fi.metatavu.kuntaapi.server.integrations.management.tasks.PageIdTaskQueue;
import fi.metatavu.kuntaapi.server.integrations.ptv.PtvConsts;
import fi.metatavu.kuntaapi.server.integrations.ptv.PtvIdFactory;
import fi.metatavu.kuntaapi.server.integrations.ptv.resources.PtvElectronicServiceChannelResourceContainer;
import fi.metatavu.kuntaapi.server.integrations.ptv.resources.PtvPhoneServiceChannelResourceContainer;
import fi.metatavu.kuntaapi.server.integrations.ptv.resources.PtvPrintableFormServiceChannelResourceContainer;
import fi.metatavu.kuntaapi.server.integrations.ptv.resources.PtvServiceLocationServiceChannelResourceContainer;
import fi.metatavu.kuntaapi.server.integrations.ptv.resources.PtvWebPageServiceChannelResourceContainer;
import fi.metatavu.kuntaapi.server.integrations.ptv.servicechannels.PtvServiceChannelResolver;
import fi.metatavu.kuntaapi.server.integrations.ptv.servicechannels.ServiceChannelType;
import fi.metatavu.kuntaapi.server.integrations.ptv.tasks.AbstractServiceChannelTask;
import fi.metatavu.kuntaapi.server.integrations.ptv.tasks.ElectronicServiceChannelRemoveTask;
import fi.metatavu.kuntaapi.server.integrations.ptv.tasks.PhoneServiceChannelRemoveTask;
import fi.metatavu.kuntaapi.server.integrations.ptv.tasks.PrintableFormServiceChannelRemoveTask;
import fi.metatavu.kuntaapi.server.integrations.ptv.tasks.ServiceChannelTasksQueue;
import fi.metatavu.kuntaapi.server.integrations.ptv.tasks.ServiceChannelUpdateTask;
import fi.metatavu.kuntaapi.server.integrations.ptv.tasks.ServiceLocationServiceChannelRemoveTask;
import fi.metatavu.kuntaapi.server.integrations.ptv.tasks.WebPageServiceChannelRemoveTask;
import fi.metatavu.kuntaapi.server.integrations.ptv.translation.PtvTranslator;
import fi.metatavu.kuntaapi.server.persistence.model.Identifier;
import fi.metatavu.kuntaapi.server.rest.model.ElectronicServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.LocalizedValue;
import fi.metatavu.kuntaapi.server.rest.model.PhoneServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.PrintableFormServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.ServiceLocationServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.WebPageServiceChannel;
import fi.metatavu.kuntaapi.server.settings.SystemSettingController;
import fi.metatavu.kuntaapi.server.tasks.IdTask;
import fi.metatavu.kuntaapi.server.tasks.IdTask.Operation;
import fi.metatavu.kuntaapi.server.tasks.jms.AbstractRespondingJmsJob;
import fi.metatavu.kuntaapi.server.tasks.jms.JmsQueueProperties;
import fi.metatavu.kuntaapi.server.utils.LocalizationUtils;
import fi.metatavu.ptv.client.model.V9VmOpenApiElectronicChannel;
import fi.metatavu.ptv.client.model.V9VmOpenApiPhoneChannel;
import fi.metatavu.ptv.client.model.V9VmOpenApiPrintableFormChannel;
import fi.metatavu.ptv.client.model.V9VmOpenApiServiceLocationChannel;
import fi.metatavu.ptv.client.model.V9VmOpenApiWebPageChannel;

@ApplicationScoped
@SuppressWarnings ("squid:S3306")
@MessageDriven (
  activationConfig = {
    @ActivationConfigProperty (propertyName = JmsQueueProperties.DESTINATION_LOOKUP, propertyValue = ServiceChannelTasksQueue.JMS_QUEUE),
    @ActivationConfigProperty(propertyName = JmsQueueProperties.MESSAGE_SELECTOR, propertyValue = JmsQueueProperties.TASK_MESSAGE_SELECTOR)
  }
)
@Pool(JmsQueueProperties.NO_CONCURRENCY_POOL)
public class PtvServiceChannelEntityDiscoverJob extends AbstractRespondingJmsJob<AbstractServiceChannelTask, Serializable> {

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
  private IdentifierRelationController identifierRelationController;
  
  @Inject
  private PageIdTaskQueue pageIdTaskQueue;

  @Inject
  private ManagementIdFactory managementIdFactory;
  
  @Override
  public Serializable executeWithResponse(AbstractServiceChannelTask task) {
    if (task.getOperation() == Operation.UPDATE) {
      return updateServiceChannelChannel((ServiceChannelUpdateTask) task);
    } else if (task.getOperation() == Operation.REMOVE) {
      removeServiceChannelChannel(task);
    }
    
    return null;
  }
  
  private Serializable updateServiceChannelChannel(ServiceChannelUpdateTask task) {
    if (!systemSettingController.hasSettingValue(PtvConsts.SYSTEM_SETTING_BASEURL)) {
      logger.log(Level.INFO, "Ptv system setting not defined, skipping update."); 
      return null;
    }
    
    Map<String, Object> serviceChannelData = serviceChannelResolver.loadServiceChannelData(task.getId());
    ServiceChannelType type = serviceChannelResolver.resolveServiceChannelType(serviceChannelData);
    
    if (type == null) {
      logger.log(Level.WARNING, () -> String.format("ServiceChannel %s does not have a type", task.getId()));
      return null;
    } else {
      return updateServiceChannelChannel(task.getOrderIndex(), type, serviceChannelData);
    }
  }
  
  private Serializable updateServiceChannelChannel(Long orderIndex, ServiceChannelType type, Map<String, Object> serviceChannelData) {
    byte[] channelData = serviceChannelResolver.serializeChannelData(serviceChannelData);
    
    switch (type) {
      case ELECTRONIC_CHANNEL:
        return updateElectronicServiceChannel(orderIndex, serviceChannelResolver.unserializeElectronicChannel(channelData));
      case SERVICE_LOCATION:
        return updateServiceLocationServiceChannel(orderIndex, serviceChannelResolver.unserializeServiceLocationChannel(channelData));
      case PRINTABLE_FORM:
        return updatePrintableFormServiceChannel(orderIndex, serviceChannelResolver.unserializePrintableFormChannel(channelData));
      case PHONE:
        return updatePhoneServiceChannel(orderIndex, serviceChannelResolver.unserializePhoneChannel(channelData));
      case WEB_PAGE:
        return updateWebPageServiceChannel(orderIndex, serviceChannelResolver.unserializeWebPageChannel(channelData));
      default:
        logger.log(Level.SEVERE, () -> String.format("Unknown service channel type %s", type));
      break;
    }
    
    return null;
  }
  
  private ElectronicServiceChannel updateElectronicServiceChannel(Long orderIndex, V9VmOpenApiElectronicChannel ptvElectronicServiceChannel) {
    if (ptvElectronicServiceChannel == null) {
      return null;
    }
    
    OrganizationId ptvOrganizationId = ptvIdFactory.createOrganizationId(ptvElectronicServiceChannel.getOrganizationId());
    OrganizationId kuntaApiOrganizationId = idController.translateOrganizationId(ptvOrganizationId, KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiOrganizationId == null) {
      logger.log(Level.INFO, () -> String.format(COULD_NOT_TRANSLATE_ORGANIZATION_INTO_KUNTA_API_ID, ptvOrganizationId));
      return null;
    }
    
    ElectronicServiceChannelId ptvElectronicServiceChannelId = ptvIdFactory.createElectronicServiceChannelId(ptvElectronicServiceChannel.getId());
    
    Identifier identifier = identifierController.acquireIdentifier(orderIndex, ptvElectronicServiceChannelId);
    ElectronicServiceChannelId kuntaApiElectronicServiceChannelId = kuntaApiIdFactory.createFromIdentifier(ElectronicServiceChannelId.class, identifier);
    ElectronicServiceChannel electronicServiceChannel = ptvTranslator.translateElectronicServiceChannel(kuntaApiElectronicServiceChannelId, kuntaApiOrganizationId, ptvElectronicServiceChannel);
    if (electronicServiceChannel == null) {
      logger.log(Level.INFO, () -> String.format("Could not translate electronicServiceChannel %s", ptvElectronicServiceChannelId));
      return null;
    }
    
    ptvElectronicServiceChannelResourceContainer.put(kuntaApiElectronicServiceChannelId, electronicServiceChannel);
    modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(electronicServiceChannel));
    
    indexElectronicServiceChannel(orderIndex, electronicServiceChannel);
    
    return electronicServiceChannel;
  }

  private ServiceLocationServiceChannel updateServiceLocationServiceChannel(Long orderIndex, V9VmOpenApiServiceLocationChannel ptvServiceLocationServiceChannel) {
    if (ptvServiceLocationServiceChannel == null) {
      return null;
    }
    
    OrganizationId ptvOrganizationId = ptvIdFactory.createOrganizationId(ptvServiceLocationServiceChannel.getOrganizationId());
    OrganizationId kuntaApiOrganizationId = idController.translateOrganizationId(ptvOrganizationId, KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiOrganizationId == null) {
      logger.log(Level.INFO, () -> String.format(COULD_NOT_TRANSLATE_ORGANIZATION_INTO_KUNTA_API_ID, ptvOrganizationId));
      return null;
    }
    
    ServiceLocationServiceChannelId ptvServiceLocationServiceChannelId = ptvIdFactory.createServiceLocationServiceChannelId(ptvServiceLocationServiceChannel.getId());
    
    Identifier identifier = identifierController.acquireIdentifier(orderIndex, ptvServiceLocationServiceChannelId);
    ServiceLocationServiceChannelId kuntaApiServiceLocationServiceChannelId = kuntaApiIdFactory.createFromIdentifier(ServiceLocationServiceChannelId.class, identifier);
    ServiceLocationServiceChannel serviceLocationServiceChannel = ptvTranslator.translateServiceLocationServiceChannel(kuntaApiServiceLocationServiceChannelId, kuntaApiOrganizationId, ptvServiceLocationServiceChannel);
    if (serviceLocationServiceChannel == null) {
      logger.log(Level.INFO, () -> String.format("Could not translate serviceLocationServiceChannel %s", ptvServiceLocationServiceChannelId));
      return null;
    }
    
    ptvServiceLocationServiceChannelResourceContainer.put(kuntaApiServiceLocationServiceChannelId, serviceLocationServiceChannel);
    modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(serviceLocationServiceChannel));
    indexServiceLocationChannel(orderIndex, serviceLocationServiceChannel);
    
    List<PageId> parentPageIds = identifierRelationController.listPageIdsByChildId(kuntaApiServiceLocationServiceChannelId);
    updateParentPageIds(parentPageIds);
    
    return serviceLocationServiceChannel;
  }

  private PrintableFormServiceChannel updatePrintableFormServiceChannel(Long orderIndex, V9VmOpenApiPrintableFormChannel ptvPrintableFormServiceChannel) {
    if (ptvPrintableFormServiceChannel == null) {
      return null;
    }
    
    OrganizationId ptvOrganizationId = ptvIdFactory.createOrganizationId(ptvPrintableFormServiceChannel.getOrganizationId());
    OrganizationId kuntaApiOrganizationId = idController.translateOrganizationId(ptvOrganizationId, KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiOrganizationId == null) {
      logger.log(Level.INFO, () -> String.format(COULD_NOT_TRANSLATE_ORGANIZATION_INTO_KUNTA_API_ID, ptvOrganizationId));
      return null;
    }
    
    PrintableFormServiceChannelId ptvPrintableFormServiceChannelId = ptvIdFactory.createPrintableFormServiceChannelId(ptvPrintableFormServiceChannel.getId());
    
    Identifier identifier = identifierController.acquireIdentifier(orderIndex, ptvPrintableFormServiceChannelId);
    PrintableFormServiceChannelId kuntaApiPrintableFormServiceChannelId = kuntaApiIdFactory.createFromIdentifier(PrintableFormServiceChannelId.class, identifier);
    PrintableFormServiceChannel printableFormServiceChannel = ptvTranslator.translatePrintableFormServiceChannel(kuntaApiPrintableFormServiceChannelId, kuntaApiOrganizationId, ptvPrintableFormServiceChannel);
    if (printableFormServiceChannel == null) {
      logger.log(Level.INFO, () -> String.format("Could not translate printableFormServiceChannel %s", ptvPrintableFormServiceChannelId));
      return null;
    }
    
    ptvPrintableFormServiceChannelResourceContainer.put(kuntaApiPrintableFormServiceChannelId, printableFormServiceChannel);
    modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(printableFormServiceChannel));
    indexPrintableFormServiceChannel(orderIndex, printableFormServiceChannel);
    
    return printableFormServiceChannel;
  }
  
  private PhoneServiceChannel updatePhoneServiceChannel(Long orderIndex, V9VmOpenApiPhoneChannel ptvPhoneServiceChannel) {
    if (ptvPhoneServiceChannel == null) {
      return null;
    }
    
    OrganizationId ptvOrganizationId = ptvIdFactory.createOrganizationId(ptvPhoneServiceChannel.getOrganizationId());
    OrganizationId kuntaApiOrganizationId = idController.translateOrganizationId(ptvOrganizationId, KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiOrganizationId == null) {
      logger.log(Level.INFO, () -> String.format(COULD_NOT_TRANSLATE_ORGANIZATION_INTO_KUNTA_API_ID, ptvOrganizationId));
      return null;
    }
    
    PhoneServiceChannelId ptvPhoneServiceChannelId = ptvIdFactory.createPhoneServiceChannelId(ptvPhoneServiceChannel.getId());
    
    Identifier identifier = identifierController.acquireIdentifier(orderIndex, ptvPhoneServiceChannelId);
    PhoneServiceChannelId kuntaApiPhoneServiceChannelId = kuntaApiIdFactory.createFromIdentifier(PhoneServiceChannelId.class, identifier);
    PhoneServiceChannel phoneServiceChannel = ptvTranslator.translatePhoneServiceChannel(kuntaApiPhoneServiceChannelId, kuntaApiOrganizationId, ptvPhoneServiceChannel);
    if (phoneServiceChannel == null) {
      logger.log(Level.INFO, () -> String.format("Could not translate phoneServiceChannel %s", ptvPhoneServiceChannelId));
      return null;
    }
    
    ptvPhoneServiceChannelResourceContainer.put(kuntaApiPhoneServiceChannelId, phoneServiceChannel);
    modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(phoneServiceChannel));
    indexPhoneServiceChannel(orderIndex, phoneServiceChannel);
    
    return phoneServiceChannel;
  }
  
  private WebPageServiceChannel updateWebPageServiceChannel(Long orderIndex, V9VmOpenApiWebPageChannel ptvWebPageServiceChannel) {
    if (ptvWebPageServiceChannel == null) {
      return null;
    }
    
    OrganizationId ptvOrganizationId = ptvIdFactory.createOrganizationId(ptvWebPageServiceChannel.getOrganizationId());
    OrganizationId kuntaApiOrganizationId = idController.translateOrganizationId(ptvOrganizationId, KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiOrganizationId == null) {
      logger.log(Level.INFO, () -> String.format(COULD_NOT_TRANSLATE_ORGANIZATION_INTO_KUNTA_API_ID, ptvOrganizationId));
      return null;
    }
    
    WebPageServiceChannelId ptvWebPageServiceChannelId = ptvIdFactory.createWebPageServiceChannelId(ptvWebPageServiceChannel.getId());
    
    Identifier identifier = identifierController.acquireIdentifier(orderIndex, ptvWebPageServiceChannelId);
    WebPageServiceChannelId kuntaApiWebPageServiceChannelId = kuntaApiIdFactory.createFromIdentifier(WebPageServiceChannelId.class, identifier);
    WebPageServiceChannel webPageServiceChannel = ptvTranslator.translateWebPageServiceChannel(kuntaApiWebPageServiceChannelId, kuntaApiOrganizationId, ptvWebPageServiceChannel);
    if (webPageServiceChannel == null) {
      logger.log(Level.INFO, () -> String.format("Could not translate webPageServiceChannel %s", ptvWebPageServiceChannelId));
      return null;
    }
    
    ptvWebPageServiceChannelResourceContainer.put(kuntaApiWebPageServiceChannelId, webPageServiceChannel);
    modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(webPageServiceChannel));
    indexWebPageServiceChannel(orderIndex, webPageServiceChannel);
    
    return webPageServiceChannel;
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
      indexRemoveRequest.fire(new IndexRemoveRequest(new IndexableElectronicServiceChannel(kuntaApiElectronicServiceChannelId.getId())));
      modificationHashCache.clear(electronicServiceChannelIdentifier.getKuntaApiId());
      ptvElectronicServiceChannelResourceContainer.clear(kuntaApiElectronicServiceChannelId);
      identifierController.deleteIdentifier(electronicServiceChannelIdentifier);
    }
  }

  private void removePhoneServiceChannel(PhoneServiceChannelId phoneServiceChannelId) {
    Identifier phoneServiceChannelIdentifier = identifierController.findIdentifierById(phoneServiceChannelId);
    if (phoneServiceChannelIdentifier != null) {
      PhoneServiceChannelId kuntaApiPhoneServiceChannelId = kuntaApiIdFactory.createFromIdentifier(PhoneServiceChannelId.class, phoneServiceChannelIdentifier);
      indexRemoveRequest.fire(new IndexRemoveRequest(new IndexablePhoneServiceChannel(kuntaApiPhoneServiceChannelId.getId())));
      modificationHashCache.clear(phoneServiceChannelIdentifier.getKuntaApiId());
      ptvPhoneServiceChannelResourceContainer.clear(kuntaApiPhoneServiceChannelId);
      identifierController.deleteIdentifier(phoneServiceChannelIdentifier);      
    }
  }

  private void removePrintableFormServiceChannel(PrintableFormServiceChannelId printableFormServiceChannelId) {
    Identifier printableFormServiceChannelIdentifier = identifierController.findIdentifierById(printableFormServiceChannelId);
    if (printableFormServiceChannelIdentifier != null) {
      PrintableFormServiceChannelId kuntaApiPrintableFormServiceChannelId = kuntaApiIdFactory.createFromIdentifier(PrintableFormServiceChannelId.class, printableFormServiceChannelIdentifier);
      indexRemoveRequest.fire(new IndexRemoveRequest(new IndexablePrintableFormServiceChannel(kuntaApiPrintableFormServiceChannelId.getId())));
      modificationHashCache.clear(printableFormServiceChannelIdentifier.getKuntaApiId());
      ptvPrintableFormServiceChannelResourceContainer.clear(kuntaApiPrintableFormServiceChannelId);
      identifierController.deleteIdentifier(printableFormServiceChannelIdentifier);      
    }
  }

  private void removeServiceLocationServiceChannel(ServiceLocationServiceChannelId serviceLocationServiceChannelId) {
    Identifier serviceLocationServiceChannelIdentifier = identifierController.findIdentifierById(serviceLocationServiceChannelId);
    if (serviceLocationServiceChannelIdentifier != null) {
      ServiceLocationServiceChannelId kuntaApiServiceLocationServiceChannelId = kuntaApiIdFactory.createFromIdentifier(ServiceLocationServiceChannelId.class, serviceLocationServiceChannelIdentifier);
      indexRemoveRequest.fire(new IndexRemoveRequest(new IndexableServiceLocationServiceChannel(kuntaApiServiceLocationServiceChannelId.getId())));
      modificationHashCache.clear(serviceLocationServiceChannelIdentifier.getKuntaApiId());
      ptvServiceLocationServiceChannelResourceContainer.clear(kuntaApiServiceLocationServiceChannelId);
      identifierController.deleteIdentifier(serviceLocationServiceChannelIdentifier);    
    }
  }

  private void removeWebPageServiceChannel(WebPageServiceChannelId webPageServiceChannelId) {
    Identifier webPageServiceChannelIdentifier = identifierController.findIdentifierById(webPageServiceChannelId);
    if (webPageServiceChannelIdentifier != null) {
      WebPageServiceChannelId kuntaApiWebPageServiceChannelId = kuntaApiIdFactory.createFromIdentifier(WebPageServiceChannelId.class, webPageServiceChannelIdentifier);
      indexRemoveRequest.fire(new IndexRemoveRequest(new IndexableWebPageServiceChannel(kuntaApiWebPageServiceChannelId.getId())));
      modificationHashCache.clear(webPageServiceChannelIdentifier.getKuntaApiId());
      ptvWebPageServiceChannelResourceContainer.clear(kuntaApiWebPageServiceChannelId);
      identifierController.deleteIdentifier(webPageServiceChannelIdentifier);      
    }
  }
  
  private void indexElectronicServiceChannel(Long orderIndex, ElectronicServiceChannel serviceChannel) {
    indexServiceChannel(IndexableElectronicServiceChannel.class, orderIndex, serviceChannel.getId(), serviceChannel.getOrganizationId(), serviceChannel.getNames(), serviceChannel.getDescriptions());
  }

  private void indexPhoneServiceChannel(Long orderIndex, PhoneServiceChannel serviceChannel) {
    indexServiceChannel(IndexablePhoneServiceChannel.class, orderIndex, serviceChannel.getId(), serviceChannel.getOrganizationId(), serviceChannel.getNames(), serviceChannel.getDescriptions());
  }
  
  private void indexPrintableFormServiceChannel(Long orderIndex, PrintableFormServiceChannel serviceChannel) {
    indexServiceChannel(IndexablePrintableFormServiceChannel.class, orderIndex, serviceChannel.getId(), serviceChannel.getOrganizationId(), serviceChannel.getNames(), serviceChannel.getDescriptions());
  }
  
  private void indexWebPageServiceChannel(Long orderIndex, WebPageServiceChannel serviceChannel) {
    indexServiceChannel(IndexableWebPageServiceChannel.class, orderIndex, serviceChannel.getId(), serviceChannel.getOrganizationId(), serviceChannel.getNames(), serviceChannel.getDescriptions());
  }
  
  private void indexServiceLocationChannel(Long orderIndex, ServiceLocationServiceChannel serviceChannel) {
    // Remove deprecated version of this service before indexing new. 
    // This can be removed after few weeks of operation
    for (String language : PtvConsts.PTV_SUPPORTED_LANGUAGES) {
      IndexRemoveDeprecatedServiceLocationServiceChannel removeChannel = new IndexRemoveDeprecatedServiceLocationServiceChannel();
      removeChannel.setLanguage(language);
      removeChannel.setServiceLocationServiceChannelId(serviceChannel.getId());
      indexRemoveRequest.fire(new IndexRemoveRequest(removeChannel));
    }
    
    indexServiceChannel(IndexableServiceLocationServiceChannel.class, orderIndex, serviceChannel.getId(), serviceChannel.getOrganizationId(), serviceChannel.getNames(), serviceChannel.getDescriptions());
  }
  
  private void indexServiceChannel(Class<? extends AbstractIndexableServiceChannel> indexableClass, Long orderIndex, String serviceChannelId, String organizationId, List<LocalizedValue> names, List<LocalizedValue> descriptions) {
    Map<String, String> shortDescriptionMap = LocalizationUtils.getLocalizedValueMap("ShortDescription", descriptions);
    Map<String, String> descriptionMap = LocalizationUtils.getLocalizedValueMap("Description", descriptions);
    Map<String, String> nameMap = LocalizationUtils.getLocalizedValueMap("Name", names);
    
    indexServiceChannel(indexableClass, orderIndex, serviceChannelId, organizationId, shortDescriptionMap, descriptionMap, nameMap);
  }

  private void indexServiceChannel(Class<? extends AbstractIndexableServiceChannel> indexableClass, Long orderIndex, String serviceChannelId, String organizationId, Map<String, String> shortDescriptionMap, Map<String, String> descriptionMap, Map<String, String> nameMap) {
    try {
      AbstractIndexableServiceChannel indexable = indexableClass.newInstance();
      
      indexable.setShortDescriptionEn(shortDescriptionMap.get("en"));
      indexable.setShortDescriptionFi(shortDescriptionMap.get("fi"));
      indexable.setShortDescriptionSv(shortDescriptionMap.get("sv"));
      
      indexable.setDescriptionEn(descriptionMap.get("en"));
      indexable.setDescriptionFi(descriptionMap.get("fi"));
      indexable.setDescriptionSv(descriptionMap.get("sv"));

      indexable.setNameEn(nameMap.get("en"));
      indexable.setNameFi(nameMap.get("fi"));
      indexable.setNameSv(nameMap.get("sv"));
      
      indexable.setServiceChannelId(serviceChannelId);
      indexable.setOrganizationId(organizationId);
      indexable.setOrderIndex(orderIndex);
      
      indexRequest.fire(new IndexRequest(indexable));
    } catch (InstantiationException | IllegalAccessException e) {
      logger.log(Level.SEVERE, "Failed to index service channe", e);
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

}
