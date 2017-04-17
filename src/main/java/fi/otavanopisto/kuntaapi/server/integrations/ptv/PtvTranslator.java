package fi.otavanopisto.kuntaapi.server.integrations.ptv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import fi.metatavu.kuntaapi.server.rest.model.Address;
import fi.metatavu.kuntaapi.server.rest.model.DailyOpeningTime;
import fi.metatavu.kuntaapi.server.rest.model.ElectronicServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.Email;
import fi.metatavu.kuntaapi.server.rest.model.Law;
import fi.metatavu.kuntaapi.server.rest.model.LocalizedValue;
import fi.metatavu.kuntaapi.server.rest.model.Municipality;
import fi.metatavu.kuntaapi.server.rest.model.OntologyItem;
import fi.metatavu.kuntaapi.server.rest.model.Organization;
import fi.metatavu.kuntaapi.server.rest.model.OrganizationService;
import fi.metatavu.kuntaapi.server.rest.model.Phone;
import fi.metatavu.kuntaapi.server.rest.model.PhoneServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.PrintableFormServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.Service;
import fi.metatavu.kuntaapi.server.rest.model.ServiceChannelAttachment;
import fi.metatavu.kuntaapi.server.rest.model.ServiceHour;
import fi.metatavu.kuntaapi.server.rest.model.ServiceLocationServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.ServiceOrganization;
import fi.metatavu.kuntaapi.server.rest.model.WebPage;
import fi.metatavu.kuntaapi.server.rest.model.WebPageServiceChannel;
import fi.metatavu.ptv.client.model.V2VmOpenApiDailyOpeningTime;
import fi.metatavu.ptv.client.model.V4VmOpenApiAddressWithCoordinates;
import fi.metatavu.ptv.client.model.V4VmOpenApiAddressWithTypeAndCoordinates;
import fi.metatavu.ptv.client.model.V4VmOpenApiElectronicChannel;
import fi.metatavu.ptv.client.model.V4VmOpenApiEmail;
import fi.metatavu.ptv.client.model.V4VmOpenApiFintoItem;
import fi.metatavu.ptv.client.model.V4VmOpenApiLaw;
import fi.metatavu.ptv.client.model.V4VmOpenApiOrganization;
import fi.metatavu.ptv.client.model.V4VmOpenApiPhone;
import fi.metatavu.ptv.client.model.V4VmOpenApiPhoneChannel;
import fi.metatavu.ptv.client.model.V4VmOpenApiPhoneWithType;
import fi.metatavu.ptv.client.model.V4VmOpenApiPrintableFormChannel;
import fi.metatavu.ptv.client.model.V4VmOpenApiService;
import fi.metatavu.ptv.client.model.V4VmOpenApiServiceHour;
import fi.metatavu.ptv.client.model.V4VmOpenApiServiceLocationChannel;
import fi.metatavu.ptv.client.model.V4VmOpenApiServiceOrganization;
import fi.metatavu.ptv.client.model.V4VmOpenApiWebPage;
import fi.metatavu.ptv.client.model.V4VmOpenApiWebPageChannel;
import fi.metatavu.ptv.client.model.VmOpenApiAttachmentWithType;
import fi.metatavu.ptv.client.model.VmOpenApiLanguageItem;
import fi.metatavu.ptv.client.model.VmOpenApiLocalizedListItem;
import fi.metatavu.ptv.client.model.VmOpenApiMunicipality;
import fi.metatavu.ptv.client.model.VmOpenApiWebPageWithOrderNumber;
import fi.otavanopisto.kuntaapi.server.id.BaseId;
import fi.otavanopisto.kuntaapi.server.id.ElectronicServiceChannelId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PhoneServiceChannelId;
import fi.otavanopisto.kuntaapi.server.id.PrintableFormServiceChannelId;
import fi.otavanopisto.kuntaapi.server.id.ServiceId;
import fi.otavanopisto.kuntaapi.server.id.ServiceLocationServiceChannelId;
import fi.otavanopisto.kuntaapi.server.id.WebPageServiceChannelId;

@ApplicationScoped
public class PtvTranslator {

  private static final String ONTOLOGY_SYSTEM_FINTO = "FINTO";
  private static final String[] WEEKDAY_INDICES = new String[] {
    "Sunday",
    "Monday",
    "Tuesday",
    "Wednesday",
    "Thursday",
    "Friday",
    "Saturday"
  };
  
  public ElectronicServiceChannel translateElectronicServiceChannel(ElectronicServiceChannelId kuntaApiElectronicServiceChannelId, OrganizationId kuntaApiOrganizationId, V4VmOpenApiElectronicChannel ptvElectronicServiceChannel) {
    ElectronicServiceChannel result = new ElectronicServiceChannel();
    
    result.setAttachments(translateAttachments(ptvElectronicServiceChannel.getAttachments()));
    result.setDescriptions(translateLocalizedValues(ptvElectronicServiceChannel.getServiceChannelDescriptions()));
    result.setId(kuntaApiElectronicServiceChannelId.getId());
    result.setLanguages(ptvElectronicServiceChannel.getLanguages());
    result.setNames(translateLocalizedValues(ptvElectronicServiceChannel.getServiceChannelNames()));
    result.setOrganizationId(kuntaApiOrganizationId.getId());
    result.setPublishingStatus(ptvElectronicServiceChannel.getPublishingStatus());
    result.setRequiresAuthentication(ptvElectronicServiceChannel.getRequiresAuthentication());
    result.setRequiresSignature(ptvElectronicServiceChannel.getRequiresSignature());
    result.setServiceHours(translateServiceHours(ptvElectronicServiceChannel.getServiceHours()));
    result.setSignatureQuantity(ptvElectronicServiceChannel.getSignatureQuantity());
    result.setSupportEmails(translateEmailsLanguageItem(ptvElectronicServiceChannel.getSupportEmails()));
    result.setSupportPhones(translatePhones(ptvElectronicServiceChannel.getSupportPhones()));
    result.setType(ptvElectronicServiceChannel.getServiceChannelType());
    result.setUrls(translateLocalizedItems(ptvElectronicServiceChannel.getUrls()));
    result.setWebPages(translateWebPagesWithOrderNumber(ptvElectronicServiceChannel.getWebPages()));
    
    return result;
  }

  public ServiceLocationServiceChannel translateServiceLocationServiceChannel(
      ServiceLocationServiceChannelId kuntaApiServiceLocationServiceChannelId, OrganizationId kuntaApiOrganizationId,
      V4VmOpenApiServiceLocationChannel ptvServiceLocationServiceChannel) {
    
    ServiceLocationServiceChannel result = new ServiceLocationServiceChannel();
    result.setAddresses(translateAddresses(ptvServiceLocationServiceChannel.getAddresses()));
    result.setDescriptions(translateLocalizedValues(ptvServiceLocationServiceChannel.getServiceChannelDescriptions()));
    result.setEmails(translateEmailsLanguageItem(ptvServiceLocationServiceChannel.getEmails()));
    result.setId(kuntaApiServiceLocationServiceChannelId.getId());
    result.setLanguages(ptvServiceLocationServiceChannel.getLanguages());
    result.setNames(translateLocalizedValues(ptvServiceLocationServiceChannel.getServiceChannelNames()));
    result.setOrganizationId(kuntaApiOrganizationId.getId());
    result.setPhoneNumbers(translatePhonesWithTypes(ptvServiceLocationServiceChannel.getPhoneNumbers()));    
    result.setPhoneServiceCharge(ptvServiceLocationServiceChannel.getPhoneServiceCharge());
    result.setPublishingStatus(ptvServiceLocationServiceChannel.getPublishingStatus());
    result.setServiceAreas(translateMunicipalities(ptvServiceLocationServiceChannel.getServiceAreas()));
    result.setServiceAreaRestricted(ptvServiceLocationServiceChannel.getServiceAreaRestricted());
    result.setServiceHours(translateServiceHours(ptvServiceLocationServiceChannel.getServiceHours()));
    result.setWebPages(translateWebPagesWithOrderNumber(ptvServiceLocationServiceChannel.getWebPages()));
    
    
    return result;
  }

  public PrintableFormServiceChannel translatePrintableFormServiceChannel(
      PrintableFormServiceChannelId kuntaApiPrintableFormServiceChannelId, OrganizationId kuntaApiOrganizationId,
      V4VmOpenApiPrintableFormChannel ptvPrintableFormServiceChannel) {
    
    PrintableFormServiceChannel result = new PrintableFormServiceChannel();
    result.setAttachments(translateAttachments(ptvPrintableFormServiceChannel.getAttachments()));
    result.setChannelUrls(translateLocalizedValues(ptvPrintableFormServiceChannel.getChannelUrls()));
    result.setDeliveryAddress(translateAddress(ptvPrintableFormServiceChannel.getDeliveryAddress()));
    result.setDescriptions(translateLocalizedValues(ptvPrintableFormServiceChannel.getServiceChannelDescriptions()));
    result.setFormIdentifier(translateLocalizedItems(ptvPrintableFormServiceChannel.getFormIdentifier()));
    result.setFormReceiver(translateLocalizedItems(ptvPrintableFormServiceChannel.getFormReceiver()));
    result.setId(kuntaApiPrintableFormServiceChannelId.getId());
    result.setLanguages(ptvPrintableFormServiceChannel.getLanguages());
    result.setNames(translateLocalizedValues(ptvPrintableFormServiceChannel.getServiceChannelNames()));
    result.setOrganizationId(kuntaApiOrganizationId.getId());
    result.setPublishingStatus(ptvPrintableFormServiceChannel.getPublishingStatus());
    result.setServiceHours(translateServiceHours(ptvPrintableFormServiceChannel.getServiceHours()));
    result.setSupportEmails(translateEmailsLanguageItem(ptvPrintableFormServiceChannel.getSupportEmails()));
    result.setSupportPhones(translatePhones(ptvPrintableFormServiceChannel.getSupportPhones()));
    result.setWebPages(translateWebPagesWithOrderNumber(ptvPrintableFormServiceChannel.getWebPages()));
    
    return result;
  }

  public PhoneServiceChannel translatePhoneServiceChannel(PhoneServiceChannelId kuntaApiPhoneServiceChannelId,
      OrganizationId kuntaApiOrganizationId, V4VmOpenApiPhoneChannel ptvPhoneServiceChannel) {
    
    PhoneServiceChannel result = new PhoneServiceChannel();
    result.setDescriptions(translateLocalizedValues(ptvPhoneServiceChannel.getServiceChannelDescriptions()));
    result.setId(kuntaApiPhoneServiceChannelId.getId());
    result.setLanguages(ptvPhoneServiceChannel.getLanguages());
    result.setNames(translateLocalizedValues(ptvPhoneServiceChannel.getServiceChannelNames()));
    result.setOrganizationId(kuntaApiOrganizationId.getId());
    result.setPhoneNumbers(translatePhonesWithTypes(ptvPhoneServiceChannel.getPhoneNumbers()));
    result.setPublishingStatus(ptvPhoneServiceChannel.getPublishingStatus());
    result.setServiceHours(translateServiceHours(ptvPhoneServiceChannel.getServiceHours()));
    result.setSupportEmails(translateEmailsLanguageItem(ptvPhoneServiceChannel.getSupportEmails()));
    result.setWebPages(translateWebPagesWithOrderNumber(ptvPhoneServiceChannel.getWebPages()));
    
    return result;
  }

  public WebPageServiceChannel translateWebPageServiceChannel(WebPageServiceChannelId kuntaApiWebPageServiceChannelId,
      OrganizationId kuntaApiOrganizationId, V4VmOpenApiWebPageChannel ptvWebPageServiceChannel) {
    
    WebPageServiceChannel result = new WebPageServiceChannel();
    result.setDescriptions(translateLocalizedValues(ptvWebPageServiceChannel.getServiceChannelDescriptions()));
    result.setId(kuntaApiWebPageServiceChannelId.getId());
    result.setLanguages(ptvWebPageServiceChannel.getLanguages());
    result.setNames(translateLocalizedValues(ptvWebPageServiceChannel.getServiceChannelNames()));
    result.setOrganizationId(kuntaApiOrganizationId.getId());
    result.setPublishingStatus(ptvWebPageServiceChannel.getPublishingStatus());
    result.setServiceHours(translateServiceHours(ptvWebPageServiceChannel.getServiceHours()));
    result.setSupportEmails(translateEmailsLanguageItem(ptvWebPageServiceChannel.getSupportEmails()));
    result.setSupportPhones(translatePhones(ptvWebPageServiceChannel.getSupportPhones()));
    result.setUrls(translateLocalizedItems(ptvWebPageServiceChannel.getUrls()));
    result.setWebPages(translateWebPagesWithOrderNumber(ptvWebPageServiceChannel.getWebPages()));
    
    return result;
  }

  public Organization translateOrganization(OrganizationId kuntaApiOrganizationId, List<OrganizationService> organizationServices, V4VmOpenApiOrganization ptvOrganization) {
    Organization organization = new Organization();
    
    organization.setAddresses(translateAddresses(ptvOrganization.getAddresses()));
    organization.setBusinessCode(ptvOrganization.getBusinessCode());
    organization.setBusinessName(ptvOrganization.getBusinessName());
    organization.setDescriptions(translateLocalizedValues(ptvOrganization.getOrganizationDescriptions()));
    organization.setDisplayNameType(ptvOrganization.getDisplayNameType());
    organization.setEmailAddresses(translateEmails(ptvOrganization.getEmailAddresses()));
    organization.setId(kuntaApiOrganizationId.getId());
    organization.setMunicipality(translateMunicipality(ptvOrganization.getMunicipality()));
    organization.setNames(translateLocalizedValues(ptvOrganization.getOrganizationNames()));
    organization.setOrganizationType(ptvOrganization.getOrganizationType());
    organization.setPhoneNumbers(translatePhones(ptvOrganization.getPhoneNumbers()));
    organization.setPublishingStatus(ptvOrganization.getPublishingStatus());
    organization.setServices(organizationServices);
    organization.setWebPages(translateWebPagesWithOrderNumber(ptvOrganization.getWebPages()));
    
    return organization;
  }

  @SuppressWarnings ("squid:S00107")
  public Service translateService(ServiceId serviceKuntaApiId, 
      List<ElectronicServiceChannelId> kuntaApiElectronicServiceChannelIds, 
      List<PhoneServiceChannelId> kuntaApiPhoneServiceChannelIds, 
      List<PrintableFormServiceChannelId> kuntaApiPrintableFormServiceChannelIds, 
      List<ServiceLocationServiceChannelId> kuntaApiServiceLocationServiceChannelIds, 
      List<WebPageServiceChannelId> kuntaApiWebPageServiceChannelIds, 
      List<ServiceOrganization> serviceOrganizations,
      V4VmOpenApiService ptvService) {
    
    if (ptvService == null) {
      return null;
    }
    
    Service result = new Service();

    result.setChargeType(ptvService.getServiceChargeType());
    result.setCoverageType(ptvService.getServiceCoverageType());
    result.setDescriptions(translateLocalizedValues(ptvService.getServiceDescriptions()));
    result.setId(serviceKuntaApiId.getId());
    result.setIndustrialClasses(translateFintoItems(ptvService.getIndustrialClasses()));
    result.setKeywords(translateLocalizedItems(ptvService.getKeywords()));
    result.setLanguages(ptvService.getLanguages());
    result.setLifeEvents(translateFintoItems(ptvService.getLifeEvents()));
    result.setMunicipalities(translateMunicipalities(ptvService.getMunicipalities()));
    result.setNames(translateLocalizedValues(ptvService.getServiceNames()));
    result.setOntologyTerms(translateFintoItems(ptvService.getOntologyTerms()));
    result.setPublishingStatus(ptvService.getPublishingStatus());
    result.setRequirements(translateLocalizedItems(ptvService.getRequirements()));
    result.setServiceClasses(translateFintoItems(ptvService.getServiceClasses()));
    result.setStatutoryDescriptionId(ptvService.getStatutoryServiceGeneralDescriptionId());
    result.setTargetGroups(translateFintoItems(ptvService.getTargetGroups()));
    result.setType(ptvService.getType());
    result.setElectronicServiceChannelIds(extractIds(kuntaApiElectronicServiceChannelIds));
    result.setPhoneServiceChannelIds(extractIds(kuntaApiPhoneServiceChannelIds));
    result.setPrintableFormServiceChannelIds(extractIds(kuntaApiPrintableFormServiceChannelIds));
    result.setServiceLocationServiceChannelIds(extractIds(kuntaApiServiceLocationServiceChannelIds));
    result.setWebPageServiceChannelIds(extractIds(kuntaApiWebPageServiceChannelIds));
    result.setLegislation(translateLaws(ptvService.getLegislation()));
    result.setOrganizations(serviceOrganizations);
    
    ptvService.getOrganizations();
    
    return result;
  }
  
  public ServiceOrganization translateServiceOrganization(OrganizationId kuntaApiOrganizationId, V4VmOpenApiServiceOrganization ptvServiceOrganization) {
    if (ptvServiceOrganization == null) {
      return null;
    }
    
    ServiceOrganization result = new ServiceOrganization();
    result.setAdditionalInformation(translateLocalizedItems(ptvServiceOrganization.getAdditionalInformation()));
    result.setOrganizationId(kuntaApiOrganizationId.getId());
    result.setProvisionType(ptvServiceOrganization.getProvisionType());
    result.setRoleType(ptvServiceOrganization.getRoleType());
    result.setWebPages(translateWebPages(ptvServiceOrganization.getWebPages()));
    
    return result;
  }
  
  private List<Law> translateLaws(List<V4VmOpenApiLaw> ptvLaws) {
    if (ptvLaws == null) {
      return Collections.emptyList();
    }
    
    List<Law> result = new ArrayList<>(ptvLaws.size());
    for (V4VmOpenApiLaw ptvLaw : ptvLaws) {
      Law law = new Law();
      law.setNames(translateLocalizedItems(ptvLaw.getNames()));
      law.setWebPages(translateWebPages(ptvLaw.getWebPages()));
      result.add(law);
    }
    
    return result;
  }

  private List<OntologyItem> translateFintoItems(List<V4VmOpenApiFintoItem> ptvFintoItems) {
    if (ptvFintoItems == null) {
      return Collections.emptyList();
    }
    
    List<OntologyItem> result = new ArrayList<>(ptvFintoItems.size());
    for (V4VmOpenApiFintoItem ptvFintoItem : ptvFintoItems) {
      OntologyItem ontologyItem = new OntologyItem();
      ontologyItem.setSystem(ONTOLOGY_SYSTEM_FINTO);
      ontologyItem.setCode(ptvFintoItem.getCode());
      ontologyItem.setName(translateLocalizedItems(ptvFintoItem.getName()));
      ontologyItem.setOntologyType(ptvFintoItem.getOntologyType());
      ontologyItem.setParentId(ptvFintoItem.getParentId());
      ontologyItem.setParentUri(ptvFintoItem.getParentUri());
      ontologyItem.setUri(ptvFintoItem.getUri());
      result.add(ontologyItem);
    }
    
    return result;
  }

  private List<Address> translateAddresses(List<V4VmOpenApiAddressWithTypeAndCoordinates> ptvAddresses) {
    if (ptvAddresses == null) {
      return Collections.emptyList();
    }

    List<Address> result = new ArrayList<>(ptvAddresses.size());

    for (V4VmOpenApiAddressWithTypeAndCoordinates ptvAddress : ptvAddresses) {
      Address address = translateAddress(ptvAddress);
      if (address != null) {
        result.add(address);
      }
    }

    return result;
  }
  
  private Address translateAddress(V4VmOpenApiAddressWithCoordinates ptvAddress) {
    if (ptvAddress == null) {
      return null;
    }
    
    Address result = new Address();
    result.setAdditionalInformations(translateLocalizedItems(ptvAddress.getAdditionalInformations()));
    result.setCoordinateState(ptvAddress.getCoordinateState());
    result.setCountry(ptvAddress.getCountry());
    result.setLatitude(ptvAddress.getLatitude());
    result.setLongitude(ptvAddress.getLongitude());
    result.setMunicipality(translateMunicipality(ptvAddress.getMunicipality()));
    result.setPostalCode(ptvAddress.getPostalCode());
    result.setPostOffice(translateLocalizedItems(ptvAddress.getPostOffice()));
    result.setPostOfficeBox(ptvAddress.getPostOfficeBox());
    result.setStreetAddress(translateLocalizedItems(ptvAddress.getStreetAddress()));
    result.setStreetNumber(ptvAddress.getStreetNumber());
    result.setType(null);

    return result;
  }
  
  private Address translateAddress(V4VmOpenApiAddressWithTypeAndCoordinates ptvAddress) {
    if (ptvAddress == null) {
      return null;
    }
    
    Address result = new Address();
    result.setAdditionalInformations(translateLocalizedItems(ptvAddress.getAdditionalInformations()));
    result.setCoordinateState(ptvAddress.getCoordinateState());
    result.setCountry(ptvAddress.getCountry());
    result.setLatitude(ptvAddress.getLatitude());
    result.setLongitude(ptvAddress.getLongitude());
    result.setMunicipality(translateMunicipality(ptvAddress.getMunicipality()));
    result.setPostalCode(ptvAddress.getPostalCode());
    result.setPostOffice(translateLocalizedItems(ptvAddress.getPostOffice()));
    result.setPostOfficeBox(ptvAddress.getPostOfficeBox());
    result.setStreetAddress(translateLocalizedItems(ptvAddress.getStreetAddress()));
    result.setStreetNumber(ptvAddress.getStreetNumber());
    result.setType(ptvAddress.getType());

    return result;
  }

  private List<Municipality> translateMunicipalities(List<VmOpenApiMunicipality> ptvMunicipalities) {
    if (ptvMunicipalities == null) {
      return Collections.emptyList();
    }
    
    List<Municipality> result = new ArrayList<>(ptvMunicipalities.size());
    for (VmOpenApiMunicipality ptvMunicipality : ptvMunicipalities) {
      Municipality municipality = translateMunicipality(ptvMunicipality);
      if (municipality != null) {
        result.add(municipality);
      }
    }
    
    return result;
  }

  private Municipality translateMunicipality(VmOpenApiMunicipality ptvMunicipality) {
    if (ptvMunicipality == null) {
      return null;
    }
    
    Municipality municipality = new Municipality();
    municipality.setCode(ptvMunicipality.getCode());
    municipality.setNames(translateLocalizedItems(ptvMunicipality.getName()));
    return municipality;
  }

  private List<Phone> translatePhonesWithTypes(List<V4VmOpenApiPhoneWithType> ptvPhones) {
    if (ptvPhones == null) {
      return Collections.emptyList();
    }
    
    List<Phone> result = new ArrayList<>(ptvPhones.size());
    for (V4VmOpenApiPhoneWithType ptvPhone : ptvPhones) {
      Phone phone = new Phone();
      phone.setAdditionalInformation(ptvPhone.getAdditionalInformation());
      phone.setChargeDescription(ptvPhone.getChargeDescription());
      phone.setIsFinnishServiceNumber(ptvPhone.getIsFinnishServiceNumber());
      phone.setLanguage(ptvPhone.getLanguage());
      phone.setNumber(ptvPhone.getNumber());
      phone.setPrefixNumber(ptvPhone.getPrefixNumber());
      phone.setServiceChargeType(ptvPhone.getServiceChargeType());
      phone.setType(ptvPhone.getType());
      result.add(phone);
    }
    
    return result;
  }

  private List<Phone> translatePhones(List<V4VmOpenApiPhone> ptvPhones) {
    if (ptvPhones == null) {
      return Collections.emptyList();
    }
    
    List<Phone> result = new ArrayList<>(ptvPhones.size());
    for (V4VmOpenApiPhone ptvPhone : ptvPhones) {
      Phone phone = new Phone();
      phone.setAdditionalInformation(ptvPhone.getAdditionalInformation());
      phone.setChargeDescription(ptvPhone.getChargeDescription());
      phone.setIsFinnishServiceNumber(ptvPhone.getIsFinnishServiceNumber());
      phone.setLanguage(ptvPhone.getLanguage());
      phone.setNumber(ptvPhone.getNumber());
      phone.setPrefixNumber(ptvPhone.getPrefixNumber());
      phone.setServiceChargeType(ptvPhone.getServiceChargeType());
      phone.setType(null);
      result.add(phone);
    }
    
    return result;
  }
  
  private List<Email> translateEmails(List<V4VmOpenApiEmail> ptvEmails) {
    if (ptvEmails == null) {
      return Collections.emptyList();
    }
    
    List<Email> result = new ArrayList<>(ptvEmails.size());    
    for (V4VmOpenApiEmail ptvEmail : ptvEmails) {
      Email email = new Email();
      email.setDescription(ptvEmail.getDescription());
      email.setLanguage(ptvEmail.getLanguage());
      email.setValue(ptvEmail.getValue());
      result.add(email);
    }
    
    return result;
  }


  private List<Email> translateEmailsLanguageItem(List<VmOpenApiLanguageItem> ptvEmails) {
    if (ptvEmails == null) {
      return Collections.emptyList();
    }
    
    List<Email> result = new ArrayList<>(ptvEmails.size());    
    for (VmOpenApiLanguageItem ptvEmail : ptvEmails) {
      Email email = new Email();
      email.setDescription(null);
      email.setLanguage(ptvEmail.getLanguage());
      email.setValue(ptvEmail.getValue());
      result.add(email);
    }
    
    return result;
  }

  private List<ServiceHour> translateServiceHours(List<V4VmOpenApiServiceHour> ptvServiceHours) {
    if (ptvServiceHours == null) {
      return Collections.emptyList();
    }
    
    List<ServiceHour> result = new ArrayList<>(ptvServiceHours.size());

    for (V4VmOpenApiServiceHour ptvServiceHour : ptvServiceHours) {
      ServiceHour serviceHour = new ServiceHour();
      serviceHour.setAdditionalInformation(translateLocalizedItems(ptvServiceHour.getAdditionalInformation()));
      serviceHour.setIsClosed(ptvServiceHour.getIsClosed());
      serviceHour.setOpeningHour(translateOpeningHours(ptvServiceHour.getOpeningHour()));
      serviceHour.setServiceHourType(ptvServiceHour.getServiceHourType());
      serviceHour.setValidForNow(ptvServiceHour.getValidForNow());
      serviceHour.setValidFrom(ptvServiceHour.getValidFrom());
      serviceHour.setValidTo(ptvServiceHour.getValidTo());
      result.add(serviceHour);
    }
    
    return result;
  }

  private List<DailyOpeningTime> translateOpeningHours(List<V2VmOpenApiDailyOpeningTime> ptvOpeningHours) {
    if (ptvOpeningHours == null) {
      return Collections.emptyList();
    }
    
    List<DailyOpeningTime> result = new ArrayList<>(ptvOpeningHours.size());
    for (V2VmOpenApiDailyOpeningTime ptvOpeningHour : ptvOpeningHours) {
      DailyOpeningTime dailyOpeningTime = new DailyOpeningTime();
      dailyOpeningTime.setDayFrom(translateDay(ptvOpeningHour.getDayFrom()));
      dailyOpeningTime.setDayTo(translateDay(ptvOpeningHour.getDayTo()));
      dailyOpeningTime.setFrom(ptvOpeningHour.getFrom());
      dailyOpeningTime.setIsExtra(ptvOpeningHour.getIsExtra());
      dailyOpeningTime.setTo(ptvOpeningHour.getTo());
      dailyOpeningTime.isExtra(ptvOpeningHour.getIsExtra());
      result.add(dailyOpeningTime);
    }
    
    return result;
  }

  private Integer translateDay(String day) {
    if (StringUtils.isBlank(day)) {
      return null;
    }
    
    int result = ArrayUtils.indexOf(WEEKDAY_INDICES, day);
    if (result > -1) {
      return result;
    }
    
    return null;
  }
  
  public List<LocalizedValue> translateLocalizedItems(List<VmOpenApiLanguageItem> items) {
    if (items != null && !items.isEmpty()) {
      List<LocalizedValue> result = new ArrayList<>();
      
      for (VmOpenApiLanguageItem item : items) {
        if (item != null) {
          LocalizedValue localizedValue = new LocalizedValue();
          localizedValue.setLanguage(item.getLanguage());
          localizedValue.setValue(item.getValue());
          localizedValue.setType(null);
          result.add(localizedValue);
        }
      }
    
      return result;
    }
    
    return Collections.emptyList();
  }

  public List<WebPage> translateWebPages(List<V4VmOpenApiWebPage> ptvWebPages) {
    if (ptvWebPages == null) {
      return Collections.emptyList();
    }

    List<WebPage> result = new ArrayList<>(ptvWebPages.size());

    for (V4VmOpenApiWebPage ptvWebPage : ptvWebPages) {
      WebPage webPage = translateWebPage(ptvWebPage);
      if (webPage != null) {
        result.add(webPage);
      }
    }

    return result;
  }
  
  private List<LocalizedValue> translateLocalizedValues(List<VmOpenApiLocalizedListItem> items) {
    if (items != null && !items.isEmpty()) {
      List<LocalizedValue> result = new ArrayList<>();
      
      for (VmOpenApiLocalizedListItem item : items) {
        if ((item != null) && StringUtils.isNotBlank(item.getValue())) {
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

  private List<ServiceChannelAttachment> translateAttachments(List<VmOpenApiAttachmentWithType> ptvAttachments) {
    if (ptvAttachments == null) {
      return Collections.emptyList();
    }

    List<ServiceChannelAttachment> result = new ArrayList<>(ptvAttachments.size());
    for (VmOpenApiAttachmentWithType ptvAttachment : ptvAttachments) {
      ServiceChannelAttachment attachment = translateAttachment(ptvAttachment);
      if (attachment != null) {
        result.add(attachment);
      }
    }

    return result;
  }
  
  private List<WebPage> translateWebPagesWithOrderNumber(List<VmOpenApiWebPageWithOrderNumber> ptvWebPages) {
    if (ptvWebPages == null) {
      return Collections.emptyList();
    }

    Collections.sort(ptvWebPages, new WebPageWithOrderNumberComparator());

    List<WebPage> result = new ArrayList<>(ptvWebPages.size());

    for (VmOpenApiWebPageWithOrderNumber ptvWebPage : ptvWebPages) {
      WebPage webPage = translateWebPage(ptvWebPage);
      if (webPage != null) {
        result.add(webPage);
      }
    }

    return result;
  }
  
  private WebPage translateWebPage(VmOpenApiWebPageWithOrderNumber ptvWebPage) {
    if (ptvWebPage == null) {
      return null;
    }

    WebPage webPage = new WebPage();
    webPage.setLanguage(ptvWebPage.getLanguage());
    webPage.setType(null);
    webPage.setUrl(ptvWebPage.getUrl());
    webPage.setValue(ptvWebPage.getValue());
    webPage.setDescription(null);
    
    return webPage;
  }
  
  private WebPage translateWebPage(V4VmOpenApiWebPage ptvWebPage) {
    if (ptvWebPage == null) {
      return null;
    }

    WebPage webPage = new WebPage();
    webPage.setLanguage(ptvWebPage.getLanguage());
    webPage.setType(null);
    webPage.setUrl(ptvWebPage.getUrl());
    webPage.setValue(ptvWebPage.getValue());
    webPage.setDescription(null);
    
    return webPage;
  }

  private ServiceChannelAttachment translateAttachment(VmOpenApiAttachmentWithType ptvAttachment) {
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

  private List<String> extractIds(List<? extends BaseId> kuntaApiIds) {
    List<String> result = new ArrayList<>(kuntaApiIds.size());
    
    for (BaseId kuntaApiId : kuntaApiIds) {
      result.add(kuntaApiId.getId());
    }
    
    return result;
  }

  private final class WebPageWithOrderNumberComparator implements Comparator<VmOpenApiWebPageWithOrderNumber> {
    @Override
    public int compare(VmOpenApiWebPageWithOrderNumber o1, VmOpenApiWebPageWithOrderNumber o2) {
      Double order1 = NumberUtils.isParsable(o1.getOrderNumber()) ? NumberUtils.createDouble(o1.getOrderNumber()) : null;
      Double order2 = NumberUtils.isParsable(o2.getOrderNumber()) ? NumberUtils.createDouble(o2.getOrderNumber()) : null;
      
      if (order1 == order2) {
        return 0;
      }
      
      if (order1 == null) {
        return -1;
      }
      
      if (order2 == null) {
        return 1;
      }
      
      return order1.compareTo(order2);
    }
  }

}
