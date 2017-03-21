package fi.otavanopisto.kuntaapi.server.integrations.ptv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import fi.metatavu.kuntaapi.server.rest.model.Address;
import fi.metatavu.kuntaapi.server.rest.model.ElectronicServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.LocalizedValue;
import fi.metatavu.kuntaapi.server.rest.model.OntologyItem;
import fi.metatavu.kuntaapi.server.rest.model.Organization;
import fi.metatavu.kuntaapi.server.rest.model.OrganizationService;
import fi.metatavu.kuntaapi.server.rest.model.PhoneServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.PrintableFormServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.Service;
import fi.metatavu.kuntaapi.server.rest.model.ServiceChannelAttachment;
import fi.metatavu.kuntaapi.server.rest.model.ServiceHour;
import fi.metatavu.kuntaapi.server.rest.model.ServiceLocationServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.SupportContact;
import fi.metatavu.kuntaapi.server.rest.model.WebPage;
import fi.metatavu.kuntaapi.server.rest.model.WebPageServiceChannel;
import fi.otavanopisto.kuntaapi.server.id.ElectronicServiceChannelId;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationServiceId;
import fi.otavanopisto.kuntaapi.server.id.PhoneServiceChannelId;
import fi.otavanopisto.kuntaapi.server.id.PrintableFormServiceChannelId;
import fi.otavanopisto.kuntaapi.server.id.ServiceId;
import fi.otavanopisto.kuntaapi.server.id.ServiceLocationServiceChannelId;
import fi.otavanopisto.kuntaapi.server.id.WebPageServiceChannelId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.metatavu.restfulptv.client.model.FintoItem;
import fi.metatavu.restfulptv.client.model.LanguageItem;
import fi.metatavu.restfulptv.client.model.LocalizedListItem;
import fi.metatavu.restfulptv.client.model.StatutoryDescription;
import fi.metatavu.restfulptv.client.model.Support;

@ApplicationScoped
public class PtvTranslator {

  private static final String ONTOLOGY_SYSTEM_FINTO = "FINTO";

  @Inject
  private Logger logger;
  
  @Inject
  private IdController idController;

  public List<LocalizedValue> translateLocalizedItems(List<LocalizedListItem> items) {
    if (items != null && !items.isEmpty()) {
      List<LocalizedValue> result = new ArrayList<>();
      
      for (LocalizedListItem item : items) {
        if (item != null) {
          LocalizedValue localizedValue = new LocalizedValue();
          localizedValue.setLanguage(item.getLanguage());
          localizedValue.setValue(item.getValue());
          localizedValue.setType(item.getType());
          result.add(localizedValue);
        }
      }
    
      return result;
    }
    
    return Collections.emptyList();
  }
  
  public List<LocalizedValue> translateLanguageItems(List<LanguageItem> items) {
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
    
  public Organization translateOrganization(fi.metatavu.restfulptv.client.model.Organization ptvOrganiztion) {
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
  
  public OrganizationService translateOrganizationService(OrganizationServiceId kuntaApiOrganizationServiceId, OrganizationId kuntaApiOrganizationId, ServiceId kuntaApiServiceId, fi.metatavu.restfulptv.client.model.OrganizationService ptvOrganizationService) {
    if (ptvOrganizationService == null) {
      return null;
    }
    
    OrganizationService result = new OrganizationService();
    result.setAdditionalInformation(translateLanguageItems(ptvOrganizationService.getAdditionalInformation()));
    result.setId(kuntaApiOrganizationServiceId.getId());
    result.setOrganizationId(kuntaApiOrganizationId.getId());
    result.setProvisionType(ptvOrganizationService.getProvisionType());
    result.setRoleType(ptvOrganizationService.getRoleType());
    result.setServiceId(kuntaApiServiceId.getId());
    result.setWebPages(translateWebPages(ptvOrganizationService.getWebPages()));
    
    return result;
  }

  public Service translateService(ServiceId serviceKuntaApiId, fi.metatavu.restfulptv.client.model.Service ptvService, StatutoryDescription ptvStatutoryDescription) {
    if (ptvService == null) {
      return null;
    }
    
    List<LocalizedValue> statutoryDescription = ptvStatutoryDescription != null ? translateLocalizedItems(ptvStatutoryDescription.getDescriptions()) : null;
    List<LocalizedValue> descriptions = translateLocalizedItems(ptvService.getDescriptions());
    
    Service result = new Service();
    result.setAdditionalInformations(translateLocalizedItems(ptvService.getAdditionalInformations()));
    result.setChargeType(ptvService.getChargeType());
    result.setCoverageType(ptvService.getCoverageType());
    result.setDescriptions(mergeDescriptions(statutoryDescription, descriptions));
    result.setId(serviceKuntaApiId.getId());
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

  public OntologyItem translateFintoItem(FintoItem ptvFintoItem) {
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

  public List<OntologyItem> translateFintoItems(List<FintoItem> ptvFintoItems) {
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
  
  public List<ElectronicServiceChannel> translateElectronicServiceChannels(List<fi.metatavu.restfulptv.client.model.ElectronicServiceChannel> ptvElectronicServiceChannels) {
    if (ptvElectronicServiceChannels == null) {
      return Collections.emptyList();
    }

    List<ElectronicServiceChannel> result = new ArrayList<>();
    for (fi.metatavu.restfulptv.client.model.ElectronicServiceChannel ptvElectronicServiceChannel : ptvElectronicServiceChannels) {
      ElectronicServiceChannel electronicChannel = translateElectronicServiceChannel(ptvElectronicServiceChannel);
      if (electronicChannel != null) {
        result.add(electronicChannel);
      }
    }

    return result;
  }
  
  public List<PhoneServiceChannel> translatePhoneServiceChannels(List<fi.metatavu.restfulptv.client.model.PhoneServiceChannel> ptvPhoneServiceChannels) {
    if (ptvPhoneServiceChannels == null) {
      return Collections.emptyList();
    }

    List<PhoneServiceChannel> result = new ArrayList<>();
    for (fi.metatavu.restfulptv.client.model.PhoneServiceChannel ptvPhoneServiceChannel : ptvPhoneServiceChannels) {
      PhoneServiceChannel phoneChannel = translatePhoneServiceChannel(ptvPhoneServiceChannel);
      if (phoneChannel != null) {
        result.add(phoneChannel);
      }
    }

    return result;
  }
  
  public List<PrintableFormServiceChannel> translatePrintableFormServiceChannels(List<fi.metatavu.restfulptv.client.model.PrintableFormServiceChannel> ptvPrintableFormServiceChannels) {
    if (ptvPrintableFormServiceChannels == null) {
      return Collections.emptyList();
    }

    List<PrintableFormServiceChannel> result = new ArrayList<>();
    for (fi.metatavu.restfulptv.client.model.PrintableFormServiceChannel ptvPrintableFormServiceChannel : ptvPrintableFormServiceChannels) {
      PrintableFormServiceChannel printableFormChannel = translatePrintableFormServiceChannel(ptvPrintableFormServiceChannel);
      if (printableFormChannel != null) {
        result.add(printableFormChannel);
      }
    }

    return result;
  }
  
  public List<ServiceLocationServiceChannel> translateServiceLocationServiceChannels(List<fi.metatavu.restfulptv.client.model.ServiceLocationServiceChannel> ptvServiceLocationServiceChannels) {
    if (ptvServiceLocationServiceChannels == null) {
      return Collections.emptyList();
    }

    List<ServiceLocationServiceChannel> result = new ArrayList<>();
    for (fi.metatavu.restfulptv.client.model.ServiceLocationServiceChannel ptvServiceLocationServiceChannel : ptvServiceLocationServiceChannels) {
      ServiceLocationServiceChannel serviceLocationChannel = translateServiceLocationServiceChannel(ptvServiceLocationServiceChannel);
      if (serviceLocationChannel != null) {
        result.add(serviceLocationChannel);
      }
    }

    return result;
  }
  
  public List<WebPageServiceChannel> translateWebPageServiceChannels(List<fi.metatavu.restfulptv.client.model.WebPageServiceChannel> ptvWebPageServiceChannels) {
    if (ptvWebPageServiceChannels == null) {
      return Collections.emptyList();
    }

    List<WebPageServiceChannel> result = new ArrayList<>();
    for (fi.metatavu.restfulptv.client.model.WebPageServiceChannel ptvWebPageServiceChannel : ptvWebPageServiceChannels) {
      WebPageServiceChannel webPageChannel = translateWebPageServiceChannel(ptvWebPageServiceChannel);
      if (webPageChannel != null) {
        result.add(webPageChannel);
      }
    }

    return result;
  }

  public ElectronicServiceChannel translateElectronicServiceChannel(fi.metatavu.restfulptv.client.model.ElectronicServiceChannel ptvElectronicServiceChannel) {
    if (ptvElectronicServiceChannel == null) {
      return null;
    }
    
    OrganizationId organizationKuntaApiId = translateOrganizationId(ptvElectronicServiceChannel.getOrganizationId());
    if (organizationKuntaApiId == null) {
      return null;
    }
    
    ElectronicServiceChannelId channelPtvId = new ElectronicServiceChannelId(PtvConsts.IDENTIFIER_NAME, ptvElectronicServiceChannel.getId());
    ElectronicServiceChannelId channelKuntaApiId = idController.translateElectronicServiceChannelId(channelPtvId, KuntaApiConsts.IDENTIFIER_NAME);
    if (channelKuntaApiId == null) {
      logger.severe(String.format("Could not translate electronic channel id %s into Kunta API id", channelPtvId.getId()));
      return null;
    }
    
    ElectronicServiceChannel result = new ElectronicServiceChannel(); 
    result.setAttachments(translateAttachments(ptvElectronicServiceChannel.getAttachments()));
    result.setDescriptions(translateLocalizedItems(ptvElectronicServiceChannel.getDescriptions()));
    result.setId(channelKuntaApiId.getId());
    result.setLanguages(ptvElectronicServiceChannel.getLanguages());
    result.setNames(translateLocalizedItems(ptvElectronicServiceChannel.getNames()));
    result.setOrganizationId(organizationKuntaApiId.getId());
    result.setPublishingStatus(ptvElectronicServiceChannel.getPublishingStatus());
    result.setRequiresAuthentication(ptvElectronicServiceChannel.getRequiresAuthentication());
    result.setRequiresSignature(ptvElectronicServiceChannel.getRequiresSignature());
    result.setServiceHours(translateServiceHours(ptvElectronicServiceChannel.getServiceHours()));
    result.setSignatureQuantity(ptvElectronicServiceChannel.getSignatureQuantity());
    result.setSupportContacts(translateSupportContacts(ptvElectronicServiceChannel.getSupportContacts()));
    result.setType(ptvElectronicServiceChannel.getType());
    result.setUrls(translateLanguageItems(ptvElectronicServiceChannel.getUrls()));
    result.setWebPages(translateWebPages(ptvElectronicServiceChannel.getWebPages()));
    
    return result;
  }

  public PhoneServiceChannel translatePhoneServiceChannel(fi.metatavu.restfulptv.client.model.PhoneServiceChannel ptvPhoneServiceChannel) {
    if (ptvPhoneServiceChannel == null) {
      return null;
    }
    
    OrganizationId organizationKuntaApiId = translateOrganizationId(ptvPhoneServiceChannel.getOrganizationId());
    if (organizationKuntaApiId == null) {
      return null;
    }
    
    PhoneServiceChannelId channelPtvId = new PhoneServiceChannelId(PtvConsts.IDENTIFIER_NAME, ptvPhoneServiceChannel.getId());
    PhoneServiceChannelId channelKuntaApiId = idController.translatePhoneServiceChannelId(channelPtvId, KuntaApiConsts.IDENTIFIER_NAME);
    if (channelKuntaApiId == null) {
      logger.severe(String.format("Could not translate phone channel id %s into Kunta API id", channelPtvId.getId()));
      return null;
    }
    
    PhoneServiceChannel result = new PhoneServiceChannel(); 

    result.setId(channelKuntaApiId.getId());
    result.setType(ptvPhoneServiceChannel.getType());
    result.setOrganizationId(organizationKuntaApiId.getId());
    result.setNames(translateLocalizedItems(ptvPhoneServiceChannel.getNames()));
    result.setDescriptions(translateLocalizedItems(ptvPhoneServiceChannel.getDescriptions()));
    result.setPhoneType(ptvPhoneServiceChannel.getPhoneType());
    result.setChargeTypes(ptvPhoneServiceChannel.getChargeTypes());
    result.setSupportContacts(translateSupportContacts(ptvPhoneServiceChannel.getSupportContacts()));
    result.setPhoneNumbers(translateLanguageItems(ptvPhoneServiceChannel.getPhoneNumbers()));
    result.setLanguages(ptvPhoneServiceChannel.getLanguages());
    result.setPhoneChargeDescriptions(translateLanguageItems(ptvPhoneServiceChannel.getPhoneChargeDescriptions()));
    result.setWebPages(translateWebPages(ptvPhoneServiceChannel.getWebPages()));
    result.setServiceHours(translateServiceHours(ptvPhoneServiceChannel.getServiceHours()));
    result.setPublishingStatus(ptvPhoneServiceChannel.getPublishingStatus());

    return result;
  }

  public PrintableFormServiceChannel translatePrintableFormServiceChannel(fi.metatavu.restfulptv.client.model.PrintableFormServiceChannel ptvPrintableFormServiceChannel) {
    if (ptvPrintableFormServiceChannel == null) {
      return null;
    }
    
    OrganizationId organizationKuntaApiId = translateOrganizationId(ptvPrintableFormServiceChannel.getOrganizationId());
    if (organizationKuntaApiId == null) {
      return null;
    }
    
    PrintableFormServiceChannelId channelPtvId = new PrintableFormServiceChannelId(PtvConsts.IDENTIFIER_NAME, ptvPrintableFormServiceChannel.getId());
    PrintableFormServiceChannelId channelKuntaApiId = idController.translatePrintableFormServiceChannelId(channelPtvId, KuntaApiConsts.IDENTIFIER_NAME);
    if (channelKuntaApiId == null) {
      logger.severe(String.format("Could not translate printableForm channel id %s into Kunta API id", channelPtvId.getId()));
      return null;
    }
    
    PrintableFormServiceChannel result = new PrintableFormServiceChannel(); 
    
    result.setId(channelKuntaApiId.getId());
    result.setType(ptvPrintableFormServiceChannel.getType());
    result.setOrganizationId(organizationKuntaApiId.getId());
    result.setNames(translateLocalizedItems(ptvPrintableFormServiceChannel.getNames()));
    result.setDescriptions(translateLocalizedItems(ptvPrintableFormServiceChannel.getDescriptions()));
    result.setFormIdentifier(ptvPrintableFormServiceChannel.getFormIdentifier());
    result.setFormReceiver(ptvPrintableFormServiceChannel.getFormReceiver());
    result.setSupportContacts(translateSupportContacts(ptvPrintableFormServiceChannel.getSupportContacts()));
    result.setDeliveryAddress(translateAddress(ptvPrintableFormServiceChannel.getDeliveryAddress()));
    result.setChannelUrls(translateLocalizedItems(ptvPrintableFormServiceChannel.getChannelUrls()));
    result.setLanguages(ptvPrintableFormServiceChannel.getLanguages());
    result.setDeliveryAddressDescriptions(translateLanguageItems(ptvPrintableFormServiceChannel.getDeliveryAddressDescriptions()));
    result.setAttachments(translateAttachments(ptvPrintableFormServiceChannel.getAttachments()));
    result.setWebPages(translateWebPages(ptvPrintableFormServiceChannel.getWebPages()));
    result.setServiceHours(translateServiceHours(ptvPrintableFormServiceChannel.getServiceHours()));
    result.setPublishingStatus(ptvPrintableFormServiceChannel.getPublishingStatus());

    return result;
  }

  public ServiceLocationServiceChannel translateServiceLocationServiceChannel(fi.metatavu.restfulptv.client.model.ServiceLocationServiceChannel ptvServiceLocationServiceChannel) {
    if (ptvServiceLocationServiceChannel == null) {
      return null;
    }
    
    OrganizationId organizationKuntaApiId = translateOrganizationId(ptvServiceLocationServiceChannel.getOrganizationId());
    if (organizationKuntaApiId == null) {
      return null;
    }
    
    ServiceLocationServiceChannelId channelPtvId = new ServiceLocationServiceChannelId(PtvConsts.IDENTIFIER_NAME, ptvServiceLocationServiceChannel.getId());
    ServiceLocationServiceChannelId channelKuntaApiId = idController.translateServiceLocationServiceChannelId(channelPtvId, KuntaApiConsts.IDENTIFIER_NAME);
    if (channelKuntaApiId == null) {
      logger.severe(String.format("Could not translate serviceLocation channel id %s into Kunta API id", channelPtvId.getId()));
      return null;
    }
    
    ServiceLocationServiceChannel result = new ServiceLocationServiceChannel(); 
    
    result.setId(channelKuntaApiId.getId());
    result.setType(ptvServiceLocationServiceChannel.getType());
    result.setOrganizationId(organizationKuntaApiId.getId());
    result.setNames(translateLocalizedItems(ptvServiceLocationServiceChannel.getNames()));
    result.setDescriptions(translateLocalizedItems(ptvServiceLocationServiceChannel.getDescriptions()));
    result.setServiceAreaRestricted(ptvServiceLocationServiceChannel.getServiceAreaRestricted());
    result.setSupportContacts(translateSupportContacts(ptvServiceLocationServiceChannel.getSupportContacts()));
    result.setEmail(ptvServiceLocationServiceChannel.getEmail());
    result.setPhone(ptvServiceLocationServiceChannel.getPhone());
    result.setLanguages(ptvServiceLocationServiceChannel.getLanguages());
    result.setFax(ptvServiceLocationServiceChannel.getFax());
    result.setLatitude(ptvServiceLocationServiceChannel.getLatitude());
    result.setLongitude(ptvServiceLocationServiceChannel.getLongitude());
    result.setCoordinateSystem(ptvServiceLocationServiceChannel.getCoordinateSystem());
    result.setCoordinatesSetManually(ptvServiceLocationServiceChannel.getCoordinatesSetManually());
    result.setPhoneServiceCharge(ptvServiceLocationServiceChannel.getPhoneServiceCharge());
    result.setWebPages(translateWebPages(ptvServiceLocationServiceChannel.getWebPages()));
    result.setServiceAreas(ptvServiceLocationServiceChannel.getServiceAreas());
    result.setPhoneChargeDescriptions(translateLanguageItems(ptvServiceLocationServiceChannel.getPhoneChargeDescriptions()));
    result.setAddresses(translateAddresses(ptvServiceLocationServiceChannel.getAddresses()));
    result.setChargeTypes(ptvServiceLocationServiceChannel.getChargeTypes());
    result.setServiceHours(translateServiceHours(ptvServiceLocationServiceChannel.getServiceHours()));
    result.setPublishingStatus(ptvServiceLocationServiceChannel.getPublishingStatus());

    return result;
  }

  public WebPageServiceChannel translateWebPageServiceChannel(fi.metatavu.restfulptv.client.model.WebPageServiceChannel ptvWebPageServiceChannel) {
    if (ptvWebPageServiceChannel == null) {
      return null;
    }
    
    OrganizationId organizationKuntaApiId = translateOrganizationId(ptvWebPageServiceChannel.getOrganizationId());
    if (organizationKuntaApiId == null) {
      return null;
    }
    
    WebPageServiceChannelId channelPtvId = new WebPageServiceChannelId(PtvConsts.IDENTIFIER_NAME, ptvWebPageServiceChannel.getId());
    WebPageServiceChannelId channelKuntaApiId = idController.translateWebPageServiceChannelId(channelPtvId, KuntaApiConsts.IDENTIFIER_NAME);
    if (channelKuntaApiId == null) {
      logger.severe(String.format("Could not translate webPage channel id %s into Kunta API id", channelPtvId.getId()));
      return null;
    }
    
    WebPageServiceChannel result = new WebPageServiceChannel(); 
    
    result.setId(channelKuntaApiId.getId());
    result.setType(ptvWebPageServiceChannel.getType());
    result.setOrganizationId(organizationKuntaApiId.getId());
    result.setNames(translateLocalizedItems(ptvWebPageServiceChannel.getNames()));
    result.setDescriptions(translateLocalizedItems(ptvWebPageServiceChannel.getDescriptions()));
    result.setUrls(translateLanguageItems(ptvWebPageServiceChannel.getUrls()));
    result.setAttachments(translateAttachments(ptvWebPageServiceChannel.getAttachments()));
    result.setSupportContacts(translateSupportContacts(ptvWebPageServiceChannel.getSupportContacts()));
    result.setLanguages(ptvWebPageServiceChannel.getLanguages());
    result.setWebPages(translateWebPages(ptvWebPageServiceChannel.getWebPages()));
    result.setServiceHours(translateServiceHours(ptvWebPageServiceChannel.getServiceHours()));
    result.setPublishingStatus(ptvWebPageServiceChannel.getPublishingStatus());
    
    return result;
  }

  public List<WebPage> translateWebPages(List<fi.metatavu.restfulptv.client.model.WebPage> ptvWebPages) {
    if (ptvWebPages == null) {
      return Collections.emptyList();
    }

    List<WebPage> result = new ArrayList<>(ptvWebPages.size());

    for (fi.metatavu.restfulptv.client.model.WebPage ptvWebPage : ptvWebPages) {
      WebPage webPage = translateWebPage(ptvWebPage);
      if (webPage != null) {
        result.add(webPage);
      }
    }

    return result;
  }
  
  public WebPage translateWebPage(fi.metatavu.restfulptv.client.model.WebPage ptvWebPage) {
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

  private OrganizationId translateOrganizationId(String ptvOrganizationId) {
    OrganizationId organizationPtvId = new OrganizationId(PtvConsts.IDENTIFIER_NAME, ptvOrganizationId);
    OrganizationId organizationKuntaApiId = idController.translateOrganizationId(organizationPtvId, KuntaApiConsts.IDENTIFIER_NAME);
    if (organizationKuntaApiId == null) {
      logger.severe(String.format("Could not translate organization id %s into Kunta API id", organizationPtvId.getId()));
      return null;
    }
    
    return organizationKuntaApiId;
  }
  
  private List<Address> translateAddresses(List<fi.metatavu.restfulptv.client.model.Address> ptvAddresses) {
    if (ptvAddresses == null) {
      return Collections.emptyList();
    }

    List<Address> result = new ArrayList<>(ptvAddresses.size());

    for (fi.metatavu.restfulptv.client.model.Address ptvAddress : ptvAddresses) {
      Address address = translateAddress(ptvAddress);
      if (address != null) {
        result.add(address);
      }
    }

    return result;
  }
  
  private List<ServiceHour> translateServiceHours(List<fi.metatavu.restfulptv.client.model.ServiceHour> ptvServiceHours) {
    if (ptvServiceHours == null) {
      return Collections.emptyList();
    }

    List<ServiceHour> result = new ArrayList<>(ptvServiceHours.size());

    for (fi.metatavu.restfulptv.client.model.ServiceHour ptvServiceHour : ptvServiceHours) {
      ServiceHour serviceHour = translateServiceHour(ptvServiceHour);
      if (serviceHour != null) {
        result.add(serviceHour);
      }
    }

    return result;
  }
  
  private ServiceHour translateServiceHour(fi.metatavu.restfulptv.client.model.ServiceHour ptvServiceHour) {
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
  

  private List<ServiceChannelAttachment> translateAttachments(List<fi.metatavu.restfulptv.client.model.Attachment> ptvAttachments) {
    if (ptvAttachments == null) {
      return Collections.emptyList();
    }

    List<ServiceChannelAttachment> result = new ArrayList<>(ptvAttachments.size());
    for (fi.metatavu.restfulptv.client.model.Attachment ptvAttachment : ptvAttachments) {
      ServiceChannelAttachment attachment = translateAttachment(ptvAttachment);
      if (attachment != null) {
        result.add(attachment);
      }
    }

    return result;
  }

  private Address translateAddress(fi.metatavu.restfulptv.client.model.Address address) {
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
  
  private ServiceChannelAttachment translateAttachment(fi.metatavu.restfulptv.client.model.Attachment ptvAttachment) {
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
  
  private List<LocalizedValue> mergeDescriptions(List<LocalizedValue> statutoryDescriptions, List<LocalizedValue> descriptions) {
    if (statutoryDescriptions == null && descriptions == null) {
      return Collections.emptyList();
    }
    
    if (statutoryDescriptions == null) {
      return descriptions;
    }
    
    if (descriptions == null) {
      return statutoryDescriptions;
    }
    
    List<LocalizedValue> result = new ArrayList<>(descriptions);
    
    for (LocalizedValue statutoryDescription : statutoryDescriptions) {
      if (StringUtils.isNotBlank(statutoryDescription.getValue())) {
        int valueIndex = findLocalizedValueIndex(result, statutoryDescription.getType(), statutoryDescription.getLanguage());
        if (valueIndex == -1) {
          result.add(statutoryDescription);
        } else {
          LocalizedValue localizedValue = result.get(valueIndex);
          localizedValue.setValue(String.format("%s%n%s", statutoryDescription.getValue(), localizedValue.getValue()));
        }
      }
    }

    return result;
  }
  
  private int findLocalizedValueIndex(List<LocalizedValue> localizedValues, String type, String language) {
    for (int i = 0; i < localizedValues.size(); i++) {
      if (StringUtils.equals(type, localizedValues.get(i).getType()) && StringUtils.equals(language, localizedValues.get(i).getLanguage())) {
        return i;
      }
    }
    
    return -1;
  }
  
}
