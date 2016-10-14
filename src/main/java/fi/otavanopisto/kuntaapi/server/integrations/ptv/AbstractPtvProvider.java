package fi.otavanopisto.kuntaapi.server.integrations.ptv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.ServiceId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.rest.model.ElectronicChannel;
import fi.otavanopisto.kuntaapi.server.rest.model.LocalizedValue;
import fi.otavanopisto.kuntaapi.server.rest.model.OntologyItem;
import fi.otavanopisto.kuntaapi.server.rest.model.Organization;
import fi.otavanopisto.kuntaapi.server.rest.model.Service;
import fi.otavanopisto.kuntaapi.server.rest.model.ServiceChannelAttachment;
import fi.otavanopisto.kuntaapi.server.rest.model.ServiceHour;
import fi.otavanopisto.kuntaapi.server.rest.model.SupportContact;
import fi.otavanopisto.kuntaapi.server.rest.model.WebPage;
import fi.otavanopisto.restfulptv.client.model.FintoItem;
import fi.otavanopisto.restfulptv.client.model.LanguageItem;
import fi.otavanopisto.restfulptv.client.model.LocalizedListItem;
import fi.otavanopisto.restfulptv.client.model.Support;

/**
 * Abstract base class for all PTV providers
 * 
 * @author Antti Leppä
 */
public abstract class AbstractPtvProvider {

  private static final String ONTOLOGY_SYSTEM_FINTO = "FINTO";

  @Inject
  private Logger logger;
  
  @Inject
  private IdController idController;

  protected List<LocalizedValue> translateLocalizedItems(List<LocalizedListItem> items) {
    if (items != null && !items.isEmpty()) {
      List<LocalizedValue> result = new ArrayList<>();
      
      for (LocalizedListItem item : items) {
        LocalizedValue localizedValue = new LocalizedValue();
        localizedValue.setLanguage(item.getLanguage());
        localizedValue.setValue(item.getValue());
        result.add(localizedValue);
      }
    
      return result;
    }
    
    return Collections.emptyList();
  }
  
  protected List<LocalizedValue> translateLanguageItems(List<LanguageItem> items) {
    if (items != null && !items.isEmpty()) {
      List<LocalizedValue> result = new ArrayList<>();
      
      for (LanguageItem item : items) {
        LocalizedValue localizedValue = new LocalizedValue();
        localizedValue.setLanguage(item.getLanguage());
        localizedValue.setValue(item.getValue());
        result.add(localizedValue);
      }
    
      return result;
    }
    
    return Collections.emptyList();
  }
    
  protected Organization transformOrganization(fi.otavanopisto.restfulptv.client.model.Organization ptvOrganiztion) {
    OrganizationId ptvId = new OrganizationId(PtvConsts.IDENTIFIFER_NAME, ptvOrganiztion.getId());
    OrganizationId kuntaApiId = idController.translateOrganizationId(ptvId, KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiId == null) {
      logger.severe(String.format("Could not translate %s into Kunta API id", ptvId.getId()));
      return null;
    }
    
    Organization organization = new Organization();
    organization.setId(kuntaApiId.getId());
    organization.setBusinessCode(ptvOrganiztion.getBusinessCode());
    organization.setBusinessName(ptvOrganiztion.getBusinessName());
    
    return organization;
  }

  
  protected List<Service> translateServices(List<fi.otavanopisto.restfulptv.client.model.Service> ptvServices) {
    if (ptvServices == null) {
      return Collections.emptyList();
    }

    List<Service> result = new ArrayList<>(ptvServices.size());
    for (fi.otavanopisto.restfulptv.client.model.Service ptvElectronicChannel : ptvServices) {
      Service service = translateService(ptvElectronicChannel);
      if (service != null) {
        result.add(service);
      }
    }

    return result;
  }

  protected Service translateService(fi.otavanopisto.restfulptv.client.model.Service ptvService) {
    if (ptvService == null) {
      return null;
    }
    
    ServiceId ptvId = new ServiceId(PtvConsts.IDENTIFIFER_NAME, ptvService.getId());
    ServiceId kuntaApiId = idController.translateServiceId(ptvId, KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiId == null) {
      logger.severe(String.format("Could not translate %s into Kunta API id", ptvId.getId()));
      return null;
    }
    
    Service result = new Service();
    result.setAdditionalInformations(translateLocalizedItems(ptvService.getAdditionalInformations()));
    result.setChargeType(ptvService.getChargeType());
    result.setCoverageType(ptvService.getCoverageType());
    result.setDescriptions(translateLocalizedItems(ptvService.getDescriptions()));
    result.setId(kuntaApiId.getId());
    result.setIndustrialClasses(translateFintoItems(ptvService.getIndustrialClasses()));
    result.setKeywords(ptvService.getKeywords());
    result.setLanguages(ptvService.getLanguages());
    result.setLifeEvents(translateFintoItems(ptvService.getLifeEvents()));
    result.setMunicipalities(ptvService.getMunicipalities());
    result.setNames(translateLocalizedItems(ptvService.getNames()));
    result.setOntologyTerms(translateFintoItems(ptvService.getOntologyTerms()));
    result.setPublishingStatus(ptvService.getPublishingStatus());
    result.setRequirements(translateLanguageItems(ptvService.getRequirements()));
    result.setServiceClasses(translateFintoItems(ptvService.getServiceClasses()));
    result.setStatutoryDescriptionId(ptvService.getStatutoryDescriptionId());
    result.setTargetGroups(translateFintoItems(ptvService.getTargetGroups()));
    result.setType(ptvService.getType());
    result.setWebPages(translateWebPages(ptvService.getWebPages()));
    
    return result;
  }
  
  protected OntologyItem translateFintoItem(FintoItem ptvFintoItem) {
    if (ptvFintoItem == null) {
      return null;
    }
    
    // TODO: IDs
    
    String id = null;
    String parentId = null;
    
    OntologyItem result = new OntologyItem();
    result.setCode(ptvFintoItem.getCode());
    result.setId(id);
    result.setName(ptvFintoItem.getName());
    result.setOntologyType(ptvFintoItem.getOntologyType());
    result.setParentId(parentId);
    result.setParentUri(ptvFintoItem.getParentId());
    result.setSystem(ONTOLOGY_SYSTEM_FINTO);
    result.setUri(ptvFintoItem.getUri());
    
    return result;
  }

  protected List<OntologyItem> translateFintoItems(List<FintoItem> ptvFintoItems) {
    if (ptvFintoItems == null) {
      return Collections.emptyList();
    }
    
    List<OntologyItem> result = new ArrayList<>(ptvFintoItems.size());
    
    for (FintoItem fintoItem : ptvFintoItems) {
      OntologyItem ontologyItem = translateFintoItem(fintoItem);
      if (ontologyItem != null) {
        result.add(ontologyItem);
      }
    }
    
    return result;
  }
  
  protected List<ElectronicChannel> translateElectronicChannels(List<fi.otavanopisto.restfulptv.client.model.ElectronicChannel> ptvElectronicChannels) {
    if (ptvElectronicChannels == null) {
      return Collections.emptyList();
    }

    List<ElectronicChannel> result = new ArrayList<>();
    for (fi.otavanopisto.restfulptv.client.model.ElectronicChannel ptvElectronicChannel : ptvElectronicChannels) {
      ElectronicChannel electronicChannel = translateElectronicChannel(ptvElectronicChannel);
      if (electronicChannel != null) {
        result.add(electronicChannel);
      }
    }

    return result;
  }

  private ElectronicChannel translateElectronicChannel(fi.otavanopisto.restfulptv.client.model.ElectronicChannel ptvElectronicChannel) {
    if (ptvElectronicChannel == null) {
      return null;
    }
    
    // TODO: ids
    String id = null;
    String organizationId = null;
    
    ElectronicChannel result = new ElectronicChannel(); 
    result.setAttachments(translateAttachments(ptvElectronicChannel.getAttachments()));
    result.setDescriptions(translateLocalizedItems(ptvElectronicChannel.getDescriptions()));
    result.setId(id);
    result.setLanguages(ptvElectronicChannel.getLanguages());
    result.setNames(translateLocalizedItems(ptvElectronicChannel.getNames()));
    result.setOrganizationId(organizationId);
    result.setPublishingStatus(ptvElectronicChannel.getPublishingStatus());
    result.setRequiresAuthentication(ptvElectronicChannel.getRequiresAuthentication());
    result.setRequiresSignature(ptvElectronicChannel.getRequiresSignature());
    result.setServiceHours(translateServiceHours(ptvElectronicChannel.getServiceHours()));
    result.setSignatureQuantity(ptvElectronicChannel.getSignatureQuantity());
    result.setSupportContacts(translateSupportContacts(ptvElectronicChannel.getSupportContacts()));
    result.setType(ptvElectronicChannel.getType());
    result.setUrls(translateLanguageItems(ptvElectronicChannel.getUrls()));
    result.setWebPages(translateWebPages(ptvElectronicChannel.getWebPages()));
    
    return result;
  }

  protected List<WebPage> translateWebPages(List<fi.otavanopisto.restfulptv.client.model.WebPage> ptvWebPages) {
    if (ptvWebPages == null) {
      return Collections.emptyList();
    }

    List<WebPage> result = new ArrayList<>(ptvWebPages.size());

    for (fi.otavanopisto.restfulptv.client.model.WebPage ptvWebPage : ptvWebPages) {
      WebPage webPage = translateWebPage(ptvWebPage);
      if (webPage != null) {
        result.add(webPage);
      }
    }

    return result;
  }
  
  protected WebPage translateWebPage(fi.otavanopisto.restfulptv.client.model.WebPage ptvWebPage) {
    if (ptvWebPage == null) {
      return null;
    }

    WebPage webPage = new WebPage();
    webPage.setLanguage(ptvWebPage.getLanguage());
    webPage.setType(ptvWebPage.getType());
    webPage.setUrl(ptvWebPage.getUrl());
    webPage.setValue(ptvWebPage.getValue());
    webPage.setDescription(ptvWebPage.getDescription());
    
    return webPage;
  }
  
  private List<ServiceHour> translateServiceHours(List<fi.otavanopisto.restfulptv.client.model.ServiceHour> ptvServiceHours) {
    if (ptvServiceHours == null) {
      return Collections.emptyList();
    }

    List<ServiceHour> result = new ArrayList<>(ptvServiceHours.size());

    for (fi.otavanopisto.restfulptv.client.model.ServiceHour ptvServiceHour : ptvServiceHours) {
      ServiceHour serviceHour = translateServiceHour(ptvServiceHour);
      if (serviceHour != null) {
        result.add(serviceHour);
      }
    }

    return result;
  }
  
  private ServiceHour translateServiceHour(fi.otavanopisto.restfulptv.client.model.ServiceHour ptvServiceHour) {
    if (ptvServiceHour == null) {
      return null;
    }
    
    ServiceHour result = new ServiceHour();
    result.setAdditionalInformation(translateLanguageItems(ptvServiceHour.getAdditionalInformation()));
    result.setCloses(ptvServiceHour.getCloses());
    result.setDays(ptvServiceHour.getDays());
    result.setOpens(ptvServiceHour.getOpens());
    result.setStatus(ptvServiceHour.getStatus());
    result.setType(ptvServiceHour.getType());
    result.setValidFrom(ptvServiceHour.getValidFrom());
    result.setValidTo(ptvServiceHour.getValidTo());

    return result;
  }
  
  private List<SupportContact> translateSupportContacts(List<Support> ptvSupportContacts) {
    if (ptvSupportContacts == null) {
      return Collections.emptyList();
    }

    List<SupportContact> result = new ArrayList<>(ptvSupportContacts.size());

    for (Support ptvSupportContact : ptvSupportContacts) {
      SupportContact supportContact = translateSupportContact(ptvSupportContact);
      if (supportContact != null) {
        result.add(supportContact);
      }
    }

    return result;
  }
  
  private SupportContact translateSupportContact(Support ptvSupport) {
    if (ptvSupport == null) {
      return null;
    }

    SupportContact support = new SupportContact();
    support.setEmail(ptvSupport.getEmail());
    support.setLanguage(ptvSupport.getLanguage());
    support.setPhone(ptvSupport.getPhone());
    support.setPhoneChargeDescription(ptvSupport.getPhoneChargeDescription());
    support.setServiceChargeTypes(ptvSupport.getServiceChargeTypes());

    return support;
  }
  

  private List<ServiceChannelAttachment> translateAttachments(List<fi.otavanopisto.restfulptv.client.model.Attachment> ptvAttachments) {
    if (ptvAttachments == null) {
      return Collections.emptyList();
    }

    List<ServiceChannelAttachment> result = new ArrayList<>(ptvAttachments.size());
    for (fi.otavanopisto.restfulptv.client.model.Attachment ptvAttachment : ptvAttachments) {
      ServiceChannelAttachment attachment = translateAttachment(ptvAttachment);
      if (attachment != null) {
        result.add(attachment);
      }
    }

    return result;
  }
  
  private ServiceChannelAttachment translateAttachment(fi.otavanopisto.restfulptv.client.model.Attachment ptvAttachment) {
    if (ptvAttachment == null) {
      return null;
    }
    
    ServiceChannelAttachment result = new ServiceChannelAttachment();
    result.setDescription(ptvAttachment.getDescription());
    result.setLanguage(ptvAttachment.getLanguage());
    result.setName(ptvAttachment.getName());
    result.setType(ptvAttachment.getType());
    result.setUrl(ptvAttachment.getUrl());
    
    return result;
  }
  
  
}
