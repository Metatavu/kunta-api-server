package fi.otavanopisto.kuntaapi.server.integrations.ptv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.id.ElectronicServiceChannelId;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PhoneChannelId;
import fi.otavanopisto.kuntaapi.server.id.PrintableFormChannelId;
import fi.otavanopisto.kuntaapi.server.id.ServiceId;
import fi.otavanopisto.kuntaapi.server.id.ServiceLocationChannelId;
import fi.otavanopisto.kuntaapi.server.id.WebPageChannelId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.rest.model.Address;
import fi.otavanopisto.kuntaapi.server.rest.model.ElectronicChannel;
import fi.otavanopisto.kuntaapi.server.rest.model.LocalizedValue;
import fi.otavanopisto.kuntaapi.server.rest.model.OntologyItem;
import fi.otavanopisto.kuntaapi.server.rest.model.Organization;
import fi.otavanopisto.kuntaapi.server.rest.model.PhoneChannel;
import fi.otavanopisto.kuntaapi.server.rest.model.PrintableFormChannel;
import fi.otavanopisto.kuntaapi.server.rest.model.Service;
import fi.otavanopisto.kuntaapi.server.rest.model.ServiceChannelAttachment;
import fi.otavanopisto.kuntaapi.server.rest.model.ServiceHour;
import fi.otavanopisto.kuntaapi.server.rest.model.ServiceLocationChannel;
import fi.otavanopisto.kuntaapi.server.rest.model.SupportContact;
import fi.otavanopisto.kuntaapi.server.rest.model.WebPage;
import fi.otavanopisto.kuntaapi.server.rest.model.WebPageChannel;
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
        localizedValue.setType(item.getType());
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
    
  protected Organization translateOrganization(fi.otavanopisto.restfulptv.client.model.Organization ptvOrganiztion) {
    if (ptvOrganiztion == null) {
      return null;
    }
    
    OrganizationId kuntaApiId = translateOrganizationId(ptvOrganiztion.getId());
    if (kuntaApiId == null) {
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
      logger.severe(String.format("Could not translate service %s into Kunta API id", ptvId.getId()));
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
    
    OntologyItem result = new OntologyItem();
    result.setCode(ptvFintoItem.getCode());
    result.setId(ptvFintoItem.getId());
    result.setName(ptvFintoItem.getName());
    result.setOntologyType(ptvFintoItem.getOntologyType());
    result.setParentId(ptvFintoItem.getParentId());
    result.setParentUri(ptvFintoItem.getParentUri());
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
  
  protected List<PhoneChannel> translatePhoneChannels(List<fi.otavanopisto.restfulptv.client.model.PhoneChannel> ptvPhoneChannels) {
    if (ptvPhoneChannels == null) {
      return Collections.emptyList();
    }

    List<PhoneChannel> result = new ArrayList<>();
    for (fi.otavanopisto.restfulptv.client.model.PhoneChannel ptvPhoneChannel : ptvPhoneChannels) {
      PhoneChannel phoneChannel = translatePhoneChannel(ptvPhoneChannel);
      if (phoneChannel != null) {
        result.add(phoneChannel);
      }
    }

    return result;
  }
  
  protected List<PrintableFormChannel> translatePrintableFormChannels(List<fi.otavanopisto.restfulptv.client.model.PrintableFormChannel> ptvPrintableFormChannels) {
    if (ptvPrintableFormChannels == null) {
      return Collections.emptyList();
    }

    List<PrintableFormChannel> result = new ArrayList<>();
    for (fi.otavanopisto.restfulptv.client.model.PrintableFormChannel ptvPrintableFormChannel : ptvPrintableFormChannels) {
      PrintableFormChannel printableFormChannel = translatePrintableFormChannel(ptvPrintableFormChannel);
      if (printableFormChannel != null) {
        result.add(printableFormChannel);
      }
    }

    return result;
  }
  
  protected List<ServiceLocationChannel> translateServiceLocationChannels(List<fi.otavanopisto.restfulptv.client.model.ServiceLocationChannel> ptvServiceLocationChannels) {
    if (ptvServiceLocationChannels == null) {
      return Collections.emptyList();
    }

    List<ServiceLocationChannel> result = new ArrayList<>();
    for (fi.otavanopisto.restfulptv.client.model.ServiceLocationChannel ptvServiceLocationChannel : ptvServiceLocationChannels) {
      ServiceLocationChannel serviceLocationChannel = translateServiceLocationChannel(ptvServiceLocationChannel);
      if (serviceLocationChannel != null) {
        result.add(serviceLocationChannel);
      }
    }

    return result;
  }
  
  protected List<WebPageChannel> translateWebPageChannels(List<fi.otavanopisto.restfulptv.client.model.WebPageChannel> ptvWebPageChannels) {
    if (ptvWebPageChannels == null) {
      return Collections.emptyList();
    }

    List<WebPageChannel> result = new ArrayList<>();
    for (fi.otavanopisto.restfulptv.client.model.WebPageChannel ptvWebPageChannel : ptvWebPageChannels) {
      WebPageChannel webPageChannel = translateWebPageChannel(ptvWebPageChannel);
      if (webPageChannel != null) {
        result.add(webPageChannel);
      }
    }

    return result;
  }

  protected ElectronicChannel translateElectronicChannel(fi.otavanopisto.restfulptv.client.model.ElectronicChannel ptvElectronicChannel) {
    if (ptvElectronicChannel == null) {
      return null;
    }
    
    OrganizationId organizationKuntaApiId = translateOrganizationId(ptvElectronicChannel.getOrganizationId());
    if (organizationKuntaApiId == null) {
      return null;
    }
    
    ElectronicServiceChannelId channelPtvId = new ElectronicServiceChannelId(PtvConsts.IDENTIFIFER_NAME, ptvElectronicChannel.getId());
    ElectronicServiceChannelId channelKuntaApiId = idController.translateElectronicServiceChannelId(channelPtvId, KuntaApiConsts.IDENTIFIER_NAME);
    if (channelKuntaApiId == null) {
      logger.severe(String.format("Could not translate electronic channel id %s into Kunta API id", channelPtvId.getId()));
      return null;
    }
    
    ElectronicChannel result = new ElectronicChannel(); 
    result.setAttachments(translateAttachments(ptvElectronicChannel.getAttachments()));
    result.setDescriptions(translateLocalizedItems(ptvElectronicChannel.getDescriptions()));
    result.setId(channelKuntaApiId.getId());
    result.setLanguages(ptvElectronicChannel.getLanguages());
    result.setNames(translateLocalizedItems(ptvElectronicChannel.getNames()));
    result.setOrganizationId(organizationKuntaApiId.getId());
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

  private OrganizationId translateOrganizationId(String ptvOrganizationId) {
    OrganizationId organizationPtvId = new OrganizationId(PtvConsts.IDENTIFIFER_NAME, ptvOrganizationId);
    OrganizationId organizationKuntaApiId = idController.translateOrganizationId(organizationPtvId, KuntaApiConsts.IDENTIFIER_NAME);
    if (organizationKuntaApiId == null) {
      logger.severe(String.format("Could not translate organization id %s into Kunta API id", organizationPtvId.getId()));
      return null;
    }
    
    return organizationKuntaApiId;
  }

  protected PhoneChannel translatePhoneChannel(fi.otavanopisto.restfulptv.client.model.PhoneChannel ptvPhoneChannel) {
    if (ptvPhoneChannel == null) {
      return null;
    }
    
    OrganizationId organizationKuntaApiId = translateOrganizationId(ptvPhoneChannel.getOrganizationId());
    if (organizationKuntaApiId == null) {
      return null;
    }
    
    PhoneChannelId channelPtvId = new PhoneChannelId(PtvConsts.IDENTIFIFER_NAME, ptvPhoneChannel.getId());
    PhoneChannelId channelKuntaApiId = idController.translatePhoneServiceChannelId(channelPtvId, KuntaApiConsts.IDENTIFIER_NAME);
    if (channelKuntaApiId == null) {
      logger.severe(String.format("Could not translate phone channel id %s into Kunta API id", channelPtvId.getId()));
      return null;
    }
    
    PhoneChannel result = new PhoneChannel(); 

    result.setId(channelKuntaApiId.getId());
    result.setType(ptvPhoneChannel.getType());
    result.setOrganizationId(organizationKuntaApiId.getId());
    result.setNames(translateLocalizedItems(ptvPhoneChannel.getNames()));
    result.setDescriptions(translateLocalizedItems(ptvPhoneChannel.getDescriptions()));
    result.setPhoneType(ptvPhoneChannel.getPhoneType());
    result.setChargeTypes(ptvPhoneChannel.getChargeTypes());
    result.setSupportContacts(translateSupportContacts(ptvPhoneChannel.getSupportContacts()));
    result.setPhoneNumbers(translateLanguageItems(ptvPhoneChannel.getPhoneNumbers()));
    result.setLanguages(ptvPhoneChannel.getLanguages());
    result.setPhoneChargeDescriptions(translateLanguageItems(ptvPhoneChannel.getPhoneChargeDescriptions()));
    result.setWebPages(translateWebPages(ptvPhoneChannel.getWebPages()));
    result.setServiceHours(translateServiceHours(ptvPhoneChannel.getServiceHours()));
    result.setPublishingStatus(ptvPhoneChannel.getPublishingStatus());

    return result;
  }

  protected PrintableFormChannel translatePrintableFormChannel(fi.otavanopisto.restfulptv.client.model.PrintableFormChannel ptvPrintableFormChannel) {
    if (ptvPrintableFormChannel == null) {
      return null;
    }
    
    OrganizationId organizationKuntaApiId = translateOrganizationId(ptvPrintableFormChannel.getOrganizationId());
    if (organizationKuntaApiId == null) {
      return null;
    }
    
    PrintableFormChannelId channelPtvId = new PrintableFormChannelId(PtvConsts.IDENTIFIFER_NAME, ptvPrintableFormChannel.getId());
    PrintableFormChannelId channelKuntaApiId = idController.translatePrintableFormServiceChannelId(channelPtvId, KuntaApiConsts.IDENTIFIER_NAME);
    if (channelKuntaApiId == null) {
      logger.severe(String.format("Could not translate printableForm channel id %s into Kunta API id", channelPtvId.getId()));
      return null;
    }
    
    PrintableFormChannel result = new PrintableFormChannel(); 
    
    result.setId(channelKuntaApiId.getId());
    result.setType(ptvPrintableFormChannel.getType());
    result.setOrganizationId(organizationKuntaApiId.getId());
    result.setNames(translateLocalizedItems(ptvPrintableFormChannel.getNames()));
    result.setDescriptions(translateLocalizedItems(ptvPrintableFormChannel.getDescriptions()));
    result.setFormIdentifier(ptvPrintableFormChannel.getFormIdentifier());
    result.setFormReceiver(ptvPrintableFormChannel.getFormReceiver());
    result.setSupportContacts(translateSupportContacts(ptvPrintableFormChannel.getSupportContacts()));
    result.setDeliveryAddress(translateAddress(ptvPrintableFormChannel.getDeliveryAddress()));
    result.setChannelUrls(translateLocalizedItems(ptvPrintableFormChannel.getChannelUrls()));
    result.setLanguages(ptvPrintableFormChannel.getLanguages());
    result.setDeliveryAddressDescriptions(translateLanguageItems(ptvPrintableFormChannel.getDeliveryAddressDescriptions()));
    result.setAttachments(translateAttachments(ptvPrintableFormChannel.getAttachments()));
    result.setWebPages(translateWebPages(ptvPrintableFormChannel.getWebPages()));
    result.setServiceHours(translateServiceHours(ptvPrintableFormChannel.getServiceHours()));
    result.setPublishingStatus(ptvPrintableFormChannel.getPublishingStatus());

    return result;
  }

  protected ServiceLocationChannel translateServiceLocationChannel(fi.otavanopisto.restfulptv.client.model.ServiceLocationChannel ptvServiceLocationChannel) {
    if (ptvServiceLocationChannel == null) {
      return null;
    }
    
    OrganizationId organizationKuntaApiId = translateOrganizationId(ptvServiceLocationChannel.getOrganizationId());
    if (organizationKuntaApiId == null) {
      return null;
    }
    
    ServiceLocationChannelId channelPtvId = new ServiceLocationChannelId(PtvConsts.IDENTIFIFER_NAME, ptvServiceLocationChannel.getId());
    ServiceLocationChannelId channelKuntaApiId = idController.translateServiceLocationChannelId(channelPtvId, KuntaApiConsts.IDENTIFIER_NAME);
    if (channelKuntaApiId == null) {
      logger.severe(String.format("Could not translate serviceLocation channel id %s into Kunta API id", channelPtvId.getId()));
      return null;
    }
    
    ServiceLocationChannel result = new ServiceLocationChannel(); 
    
    result.setId(channelKuntaApiId.getId());
    result.setType(ptvServiceLocationChannel.getType());
    result.setOrganizationId(organizationKuntaApiId.getId());
    result.setNames(translateLocalizedItems(ptvServiceLocationChannel.getNames()));
    result.setDescriptions(translateLocalizedItems(ptvServiceLocationChannel.getDescriptions()));
    result.setServiceAreaRestricted(ptvServiceLocationChannel.getServiceAreaRestricted());
    result.setSupportContacts(translateSupportContacts(ptvServiceLocationChannel.getSupportContacts()));
    result.setEmail(ptvServiceLocationChannel.getEmail());
    result.setPhone(ptvServiceLocationChannel.getPhone());
    result.setLanguages(ptvServiceLocationChannel.getLanguages());
    result.setFax(ptvServiceLocationChannel.getFax());
    result.setLatitude(ptvServiceLocationChannel.getLatitude());
    result.setLongitude(ptvServiceLocationChannel.getLongitude());
    result.setCoordinateSystem(ptvServiceLocationChannel.getCoordinateSystem());
    result.setCoordinatesSetManually(ptvServiceLocationChannel.getCoordinatesSetManually());
    result.setPhoneServiceCharge(ptvServiceLocationChannel.getPhoneServiceCharge());
    result.setWebPages(translateWebPages(ptvServiceLocationChannel.getWebPages()));
    result.setServiceAreas(ptvServiceLocationChannel.getServiceAreas());
    result.setPhoneChargeDescriptions(translateLanguageItems(ptvServiceLocationChannel.getPhoneChargeDescriptions()));
    result.setAddresses(translateAddresses(ptvServiceLocationChannel.getAddresses()));
    result.setChargeTypes(ptvServiceLocationChannel.getChargeTypes());
    result.setServiceHours(translateServiceHours(ptvServiceLocationChannel.getServiceHours()));
    result.setPublishingStatus(ptvServiceLocationChannel.getPublishingStatus());

    return result;
  }

  protected WebPageChannel translateWebPageChannel(fi.otavanopisto.restfulptv.client.model.WebPageChannel ptvWebPageChannel) {
    if (ptvWebPageChannel == null) {
      return null;
    }
    
    OrganizationId organizationKuntaApiId = translateOrganizationId(ptvWebPageChannel.getOrganizationId());
    if (organizationKuntaApiId == null) {
      return null;
    }
    
    WebPageChannelId channelPtvId = new WebPageChannelId(PtvConsts.IDENTIFIFER_NAME, ptvWebPageChannel.getId());
    WebPageChannelId channelKuntaApiId = idController.translateWebPageServiceChannelId(channelPtvId, KuntaApiConsts.IDENTIFIER_NAME);
    if (channelKuntaApiId == null) {
      logger.severe(String.format("Could not translate webPage channel id %s into Kunta API id", channelPtvId.getId()));
      return null;
    }
    
    WebPageChannel result = new WebPageChannel(); 
    
    result.setId(channelKuntaApiId.getId());
    result.setType(ptvWebPageChannel.getType());
    result.setOrganizationId(organizationKuntaApiId.getId());
    result.setNames(translateLocalizedItems(ptvWebPageChannel.getNames()));
    result.setDescriptions(translateLocalizedItems(ptvWebPageChannel.getDescriptions()));
    result.setUrls(translateLanguageItems(ptvWebPageChannel.getUrls()));
    result.setAttachments(translateAttachments(ptvWebPageChannel.getAttachments()));
    result.setSupportContacts(translateSupportContacts(ptvWebPageChannel.getSupportContacts()));
    result.setLanguages(ptvWebPageChannel.getLanguages());
    result.setWebPages(translateWebPages(ptvWebPageChannel.getWebPages()));
    result.setServiceHours(translateServiceHours(ptvWebPageChannel.getServiceHours()));
    result.setPublishingStatus(ptvWebPageChannel.getPublishingStatus());
    
    return result;
  }

  private List<Address> translateAddresses(List<fi.otavanopisto.restfulptv.client.model.Address> ptvAddresses) {
    if (ptvAddresses == null) {
      return Collections.emptyList();
    }

    List<Address> result = new ArrayList<>(ptvAddresses.size());

    for (fi.otavanopisto.restfulptv.client.model.Address ptvAddress : ptvAddresses) {
      Address address = translateAddress(ptvAddress);
      if (address != null) {
        result.add(address);
      }
    }

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

  private Address translateAddress(fi.otavanopisto.restfulptv.client.model.Address address) {
    if (address == null) {
      return null;
    }
    
    Address result = new Address();
    
    result.setType(address.getType());
    result.setPostOfficeBox(address.getPostOfficeBox());
    result.setPostalCode(address.getPostalCode());
    result.setPostOffice(address.getPostOffice());
    result.setStreetAddress(translateLanguageItems(address.getStreetAddress()));
    result.setMunicipality(address.getMunicipality());
    result.setCountry(address.getCountry());
    result.setQualifier(address.getQualifier());
    result.setAdditionalInformations(translateLanguageItems(address.getAdditionalInformations()));
    
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
