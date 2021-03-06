package fi.metatavu.kuntaapi.server.integrations.ptv.translation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.math.NumberUtils;
import org.osgeo.proj4j.CRSFactory;
import org.osgeo.proj4j.CoordinateReferenceSystem;
import org.osgeo.proj4j.CoordinateTransform;
import org.osgeo.proj4j.CoordinateTransformFactory;
import org.osgeo.proj4j.ProjCoordinate;

import fi.metatavu.kuntaapi.server.id.BaseId;
import fi.metatavu.kuntaapi.server.id.CodeId;
import fi.metatavu.kuntaapi.server.id.ElectronicServiceChannelId;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.id.PhoneServiceChannelId;
import fi.metatavu.kuntaapi.server.id.PrintableFormServiceChannelId;
import fi.metatavu.kuntaapi.server.id.ServiceId;
import fi.metatavu.kuntaapi.server.id.ServiceLocationServiceChannelId;
import fi.metatavu.kuntaapi.server.id.WebPageServiceChannelId;
import fi.metatavu.kuntaapi.server.integrations.ptv.CodeType;
import fi.metatavu.kuntaapi.server.rest.model.AccessibilityContactInfo;
import fi.metatavu.kuntaapi.server.rest.model.AccessibilitySentence;
import fi.metatavu.kuntaapi.server.rest.model.AccessibilitySentenceValue;
import fi.metatavu.kuntaapi.server.rest.model.Address;
import fi.metatavu.kuntaapi.server.rest.model.AddressEntrance;
import fi.metatavu.kuntaapi.server.rest.model.Area;
import fi.metatavu.kuntaapi.server.rest.model.Code;
import fi.metatavu.kuntaapi.server.rest.model.CodeExtra;
import fi.metatavu.kuntaapi.server.rest.model.Coordinate;
import fi.metatavu.kuntaapi.server.rest.model.Coordinates;
import fi.metatavu.kuntaapi.server.rest.model.DailyOpeningTime;
import fi.metatavu.kuntaapi.server.rest.model.ElectronicServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.Email;
import fi.metatavu.kuntaapi.server.rest.model.Law;
import fi.metatavu.kuntaapi.server.rest.model.LocalizedValue;
import fi.metatavu.kuntaapi.server.rest.model.Municipality;
import fi.metatavu.kuntaapi.server.rest.model.NameTypeByLanguage;
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
import fi.metatavu.kuntaapi.server.rest.model.ServiceVoucher;
import fi.metatavu.kuntaapi.server.rest.model.WebPage;
import fi.metatavu.kuntaapi.server.rest.model.WebPageServiceChannel;
import fi.metatavu.kuntaapi.server.utils.TimeUtils;
import fi.metatavu.ptv.client.model.V4VmOpenApiEmail;
import fi.metatavu.ptv.client.model.V4VmOpenApiFintoItem;
import fi.metatavu.ptv.client.model.V4VmOpenApiLaw;
import fi.metatavu.ptv.client.model.V4VmOpenApiPhone;
import fi.metatavu.ptv.client.model.V4VmOpenApiPhoneWithType;
import fi.metatavu.ptv.client.model.V4VmOpenApiWebPage;
import fi.metatavu.ptv.client.model.V6VmOpenApiServiceOrganization;
import fi.metatavu.ptv.client.model.V7VmOpenApiFintoItemWithDescription;
import fi.metatavu.ptv.client.model.V8VmOpenApiAddressDelivery;
import fi.metatavu.ptv.client.model.V8VmOpenApiDailyOpeningTime;
import fi.metatavu.ptv.client.model.V9VmOpenApiElectronicChannel;
import fi.metatavu.ptv.client.model.V9VmOpenApiEntrance;
import fi.metatavu.ptv.client.model.V9VmOpenApiOrganization;
import fi.metatavu.ptv.client.model.V9VmOpenApiPhoneChannel;
import fi.metatavu.ptv.client.model.V9VmOpenApiPrintableFormChannel;
import fi.metatavu.ptv.client.model.V9VmOpenApiService;
import fi.metatavu.ptv.client.model.V8VmOpenApiServiceHour;
import fi.metatavu.ptv.client.model.V9VmOpenApiAddress;
import fi.metatavu.ptv.client.model.V9VmOpenApiAddressLocation;
import fi.metatavu.ptv.client.model.V9VmOpenApiServiceLocationChannel;
import fi.metatavu.ptv.client.model.V9VmOpenApiServiceVoucher;
import fi.metatavu.ptv.client.model.V9VmOpenApiWebPage;
import fi.metatavu.ptv.client.model.V9VmOpenApiWebPageChannel;
import fi.metatavu.ptv.client.model.VmOpenApiAccessibilityContactInfo;
import fi.metatavu.ptv.client.model.VmOpenApiAccessibilitySentence;
import fi.metatavu.ptv.client.model.VmOpenApiAddressPostOfficeBox;
import fi.metatavu.ptv.client.model.VmOpenApiAddressStreet;
import fi.metatavu.ptv.client.model.VmOpenApiAddressStreetWithCoordinates;
import fi.metatavu.ptv.client.model.VmOpenApiArea;
import fi.metatavu.ptv.client.model.VmOpenApiAttachmentWithType;
import fi.metatavu.ptv.client.model.VmOpenApiCodeListItem;
import fi.metatavu.ptv.client.model.VmOpenApiCoordinates;
import fi.metatavu.ptv.client.model.VmOpenApiDialCodeListItem;
import fi.metatavu.ptv.client.model.VmOpenApiLanguageItem;
import fi.metatavu.ptv.client.model.VmOpenApiLocalizedListItem;
import fi.metatavu.ptv.client.model.VmOpenApiMunicipality;
import fi.metatavu.ptv.client.model.VmOpenApiNameTypeByLanguage;
import fi.metatavu.ptv.client.model.VmOpenApiSentenceValue;

@ApplicationScoped
public class PtvTranslator extends AbstractTranslator {
  
  private static final String UNKNOWN_ADDRESS_SUBTYPE = "Unknown %s address subtype %s. Address body %s";

  @Inject
  private Logger logger;

  private static final String ONTOLOGY_SYSTEM_FINTO = "FINTO";
  
  public ElectronicServiceChannel translateElectronicServiceChannel(ElectronicServiceChannelId kuntaApiElectronicServiceChannelId, OrganizationId kuntaApiOrganizationId, V9VmOpenApiElectronicChannel ptvElectronicServiceChannel) {
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
    result.setUrls(translateWebPagesToLocalizedItems(ptvElectronicServiceChannel.getWebPages()));
    result.setWebPages(translateWebPagesWithOrderNumber(ptvElectronicServiceChannel.getWebPages()));
    result.setAreas(translateAreas(ptvElectronicServiceChannel.getAreas()));
    result.setAreaType(ptvElectronicServiceChannel.getAreaType());

    return result;
  }

  public ServiceLocationServiceChannel translateServiceLocationServiceChannel(
      ServiceLocationServiceChannelId kuntaApiServiceLocationServiceChannelId, OrganizationId kuntaApiOrganizationId,
      V9VmOpenApiServiceLocationChannel ptvServiceLocationServiceChannel) {
    
    ServiceLocationServiceChannel result = new ServiceLocationServiceChannel();
    result.setAddresses(translateAddressesLocation(ptvServiceLocationServiceChannel.getAddresses()));
    result.setDescriptions(translateLocalizedValues(ptvServiceLocationServiceChannel.getServiceChannelDescriptions()));
    result.setEmails(translateEmailsLanguageItem(ptvServiceLocationServiceChannel.getEmails()));
    result.setId(kuntaApiServiceLocationServiceChannelId.getId());
    result.setLanguages(ptvServiceLocationServiceChannel.getLanguages());
    result.setNames(translateLocalizedValues(ptvServiceLocationServiceChannel.getServiceChannelNames()));
    result.setOrganizationId(kuntaApiOrganizationId.getId());
    result.setPhoneNumbers(translatePhonesWithTypes(ptvServiceLocationServiceChannel.getPhoneNumbers()));    
    result.setPhoneServiceCharge(null);
    result.setPublishingStatus(ptvServiceLocationServiceChannel.getPublishingStatus());
    result.setAreas(translateAreas(ptvServiceLocationServiceChannel.getAreas()));
    result.setAreaType(ptvServiceLocationServiceChannel.getAreaType());
    result.setServiceHours(translateServiceHours(ptvServiceLocationServiceChannel.getServiceHours()));
    result.setWebPages(translateWebPagesWithOrderNumber(ptvServiceLocationServiceChannel.getWebPages()));
    
    return result;
  }

  public PrintableFormServiceChannel translatePrintableFormServiceChannel(
      PrintableFormServiceChannelId kuntaApiPrintableFormServiceChannelId, OrganizationId kuntaApiOrganizationId,
      V9VmOpenApiPrintableFormChannel ptvPrintableFormServiceChannel) {
    
    if (ptvPrintableFormServiceChannel == null) {
      return null;
    }
    
    V8VmOpenApiAddressDelivery deliveryAddress = null;
    List<V8VmOpenApiAddressDelivery> allDeliveryAddresses = ptvPrintableFormServiceChannel.getDeliveryAddresses();

    if (allDeliveryAddresses != null) {
      List<V8VmOpenApiAddressDelivery> deliveryAddresses = allDeliveryAddresses.stream()
        .filter(Objects::nonNull)
        .filter(address -> {
          List<VmOpenApiLanguageItem> receiver = address.getReceiver();

          long receiverCount = receiver == null ? 0 : receiver.stream()
            .map(VmOpenApiLanguageItem::getValue)
            .filter(StringUtils::isNotBlank)
            .count();
          
          long inTextCount = address.getDeliveryAddressInText() == null ? 0 : address.getDeliveryAddressInText().stream()
            .map(VmOpenApiLanguageItem::getValue)
            .filter(StringUtils::isNotBlank)
            .count();
          
          return receiverCount > 0 || inTextCount > 0 || address.getPostOfficeBoxAddress() != null || address.getStreetAddress() != null;
        })
        .collect(Collectors.toList());
      
      if (deliveryAddresses != null && !deliveryAddresses.isEmpty()) {
        deliveryAddress = deliveryAddresses.get(0);
        if (deliveryAddresses.size() > 1) {
          logger.log(Level.WARNING, () -> String.format("Multiple delivery addresses are not yet supported: %d delivery addresses found", deliveryAddresses.size()));
        }
      }
    }

    PrintableFormServiceChannel result = new PrintableFormServiceChannel();
    result.setAttachments(translateAttachments(ptvPrintableFormServiceChannel.getAttachments()));
    result.setChannelUrls(translateLocalizedValues(ptvPrintableFormServiceChannel.getChannelUrls()));
    result.setDeliveryAddress(translateAddressDelivery(deliveryAddress));
    result.setDescriptions(translateLocalizedValues(ptvPrintableFormServiceChannel.getServiceChannelDescriptions()));
    result.setFormIdentifier(translateLocalizedItems(ptvPrintableFormServiceChannel.getFormIdentifier()));
    result.setFormReceiver(deliveryAddress != null ? translateLocalizedItems(deliveryAddress.getReceiver()) : null);
    result.setId(kuntaApiPrintableFormServiceChannelId.getId());
    result.setLanguages(ptvPrintableFormServiceChannel.getLanguages());
    result.setNames(translateLocalizedValues(ptvPrintableFormServiceChannel.getServiceChannelNames()));
    result.setOrganizationId(kuntaApiOrganizationId.getId());
    result.setPublishingStatus(ptvPrintableFormServiceChannel.getPublishingStatus());
    result.setServiceHours(translateServiceHours(ptvPrintableFormServiceChannel.getServiceHours()));
    result.setSupportEmails(translateEmailsLanguageItem(ptvPrintableFormServiceChannel.getSupportEmails()));
    result.setSupportPhones(translatePhones(ptvPrintableFormServiceChannel.getSupportPhones()));
    result.setWebPages(translateWebPagesWithOrderNumber(ptvPrintableFormServiceChannel.getWebPages()));
    result.setAreaType(ptvPrintableFormServiceChannel.getAreaType());
    result.setAreas(translateAreas(ptvPrintableFormServiceChannel.getAreas()));
    
    return result;
  }

  public PhoneServiceChannel translatePhoneServiceChannel(PhoneServiceChannelId kuntaApiPhoneServiceChannelId,
      OrganizationId kuntaApiOrganizationId, V9VmOpenApiPhoneChannel ptvPhoneServiceChannel) {
    
    if (ptvPhoneServiceChannel == null) {
      return null;
    }
    
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
    result.setAreaType(ptvPhoneServiceChannel.getAreaType());
    result.setAreas(translateAreas(ptvPhoneServiceChannel.getAreas()));
    
    return result;
  }

  public WebPageServiceChannel translateWebPageServiceChannel(WebPageServiceChannelId kuntaApiWebPageServiceChannelId,
      OrganizationId kuntaApiOrganizationId, V9VmOpenApiWebPageChannel ptvWebPageServiceChannel) {
    
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
    result.setUrls(translateWebPagesToLocalizedItems(ptvWebPageServiceChannel.getWebPages()));
    result.setWebPages(translateWebPagesWithOrderNumber(ptvWebPageServiceChannel.getWebPages()));
    
    return result;
  }

  public Organization translateOrganization(OrganizationId kuntaApiOrganizationId, OrganizationId kuntaApiParentOrganizationId, List<OrganizationService> organizationServices, V9VmOpenApiOrganization ptvOrganization) {
    if (ptvOrganization == null) {
      return null;
    }

    Organization organization = new Organization();
    
    organization.setAddresses(translateAddresses(ptvOrganization.getAddresses()));
    organization.setBusinessCode(ptvOrganization.getBusinessCode());
    organization.setBusinessName(ptvOrganization.getBusinessName());
    organization.setDescriptions(translateLocalizedValues(ptvOrganization.getOrganizationDescriptions()));
    organization.setDisplayNameType(translateNameTypeByLanguage(ptvOrganization.getDisplayNameType()));
    organization.setEmailAddresses(translateEmails(ptvOrganization.getEmails()));
    organization.setId(kuntaApiOrganizationId.getId());
    organization.setMunicipality(translateMunicipality(ptvOrganization.getMunicipality()));
    organization.setNames(translateLocalizedValues(ptvOrganization.getOrganizationNames()));
    organization.setOrganizationType(ptvOrganization.getOrganizationType());
    organization.setPhoneNumbers(translatePhones(ptvOrganization.getPhoneNumbers()));
    organization.setPublishingStatus(ptvOrganization.getPublishingStatus());
    organization.setServices(organizationServices);
    organization.setWebPages(translateWebPagesWithOrderNumber(ptvOrganization.getWebPages()));
    organization.setAreas(translateAreas(ptvOrganization.getAreas()));
    organization.setAreaType(ptvOrganization.getAreaType());
    
    if (kuntaApiParentOrganizationId != null) {
      organization.setParentOrganization(kuntaApiParentOrganizationId.getId());
    }

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
      V9VmOpenApiService ptvService) {
    
    if (ptvService == null) {
      return null;
    }
    
    Service result = new Service();

    result.setChargeType(ptvService.getServiceChargeType());
    result.setDescriptions(translateLocalizedValues(ptvService.getServiceDescriptions()));
    result.setId(serviceKuntaApiId.getId());
    result.setIndustrialClasses(translateFintoItems(ptvService.getIndustrialClasses()));
    result.setKeywords(translateLocalizedItems(ptvService.getKeywords()));
    result.setLanguages(ptvService.getLanguages());
    result.setLifeEvents(translateFintoItems(ptvService.getLifeEvents()));
    result.setNames(translateLocalizedValues(ptvService.getServiceNames()));
    result.setOntologyTerms(translateFintoItems(ptvService.getOntologyTerms()));
    result.setPublishingStatus(ptvService.getPublishingStatus());
    result.setRequirements(translateLocalizedItems(ptvService.getRequirements()));
    result.setServiceClasses(translateFintoItemsWithDescriptions(ptvService.getServiceClasses()));
    result.setStatutoryDescriptionId(null);
    result.setTargetGroups(translateFintoItems(ptvService.getTargetGroups()));
    result.setType(ptvService.getType());
    result.setElectronicServiceChannelIds(extractIds(kuntaApiElectronicServiceChannelIds));
    result.setPhoneServiceChannelIds(extractIds(kuntaApiPhoneServiceChannelIds));
    result.setPrintableFormServiceChannelIds(extractIds(kuntaApiPrintableFormServiceChannelIds));
    result.setServiceLocationServiceChannelIds(extractIds(kuntaApiServiceLocationServiceChannelIds));
    result.setWebPageServiceChannelIds(extractIds(kuntaApiWebPageServiceChannelIds));
    result.setLegislation(translateLaws(ptvService.getLegislation()));
    result.setOrganizations(serviceOrganizations);
    result.setAreas(translateAreas(ptvService.getAreas()));
    result.setAreaType(ptvService.getAreaType());
    result.setVouchers(ptvService.getServiceVouchersInUse() ? translateServiceVouchers(ptvService.getServiceVouchers()) : Collections.emptyList());
    result.setFundingType(ptvService.getFundingType());

    return result;
  }
  
  private List<ServiceVoucher> translateServiceVouchers(List<V9VmOpenApiServiceVoucher> ptvServiceVouchers) {
    if (ptvServiceVouchers == null || ptvServiceVouchers.isEmpty()) {
      return Collections.emptyList();
    }
    
    return ptvServiceVouchers
      .stream()
      .map(this::translateServiceVoucher)
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
  }
  
  private ServiceVoucher translateServiceVoucher(V9VmOpenApiServiceVoucher ptvServiceVoucher) {
    if (ptvServiceVoucher == null) {
      return null;
    }
    
    ServiceVoucher serviceVoucher = new ServiceVoucher();
    serviceVoucher.setAdditionalInformation(ptvServiceVoucher.getAdditionalInformation());
    serviceVoucher.setLanguage(ptvServiceVoucher.getLanguage());
    serviceVoucher.setUrl(ptvServiceVoucher.getUrl());
    serviceVoucher.setValue(ptvServiceVoucher.getValue());
    
    return serviceVoucher;
  }

  public ServiceOrganization translateServiceOrganization(OrganizationId kuntaApiOrganizationId, V6VmOpenApiServiceOrganization ptvServiceOrganization) {
    if (ptvServiceOrganization == null) {
      return null;
    }
    
    ServiceOrganization result = new ServiceOrganization();
    result.setAdditionalInformation(translateLocalizedItems(ptvServiceOrganization.getAdditionalInformation()));
    result.setOrganizationId(kuntaApiOrganizationId.getId());
    result.setProvisionType(ptvServiceOrganization.getProvisionType());
    result.setRoleType(ptvServiceOrganization.getRoleType());
    result.setWebPages(translateWebPages(Collections.emptyList()));
    
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
      ontologyItem.setParentId(translateUUID(ptvFintoItem.getParentId()));
      ontologyItem.setParentUri(ptvFintoItem.getParentUri());
      ontologyItem.setUri(ptvFintoItem.getUri());
      result.add(ontologyItem);
    }
    
    return result;
  }
  
  private List<OntologyItem> translateFintoItemsWithDescriptions(List<V7VmOpenApiFintoItemWithDescription> ptvFintoItems) {
    if (ptvFintoItems == null) {
      return Collections.emptyList();
    }
    
    List<OntologyItem> result = new ArrayList<>(ptvFintoItems.size());
    for (V7VmOpenApiFintoItemWithDescription ptvFintoItem : ptvFintoItems) {
      OntologyItem ontologyItem = new OntologyItem();
      ontologyItem.setSystem(ONTOLOGY_SYSTEM_FINTO);
      ontologyItem.setCode(ptvFintoItem.getCode());
      ontologyItem.setName(translateLocalizedItems(ptvFintoItem.getName()));
      ontologyItem.setOntologyType(ptvFintoItem.getOntologyType());
      ontologyItem.setParentId(translateUUID(ptvFintoItem.getParentId()));
      ontologyItem.setParentUri(ptvFintoItem.getParentUri());
      ontologyItem.setUri(ptvFintoItem.getUri());
      result.add(ontologyItem);
    }
    
    return result;
  }

  private List<Address> translateAddresses(List<V9VmOpenApiAddress> ptvAddresses) {
    if (ptvAddresses == null) {
      return Collections.emptyList();
    }

    List<Address> result = new ArrayList<>(ptvAddresses.size());

    for (V9VmOpenApiAddress ptvAddress : ptvAddresses) {
      Address address = translateAddress(ptvAddress);
      if (address != null) {
        result.add(address);
      }
    }

    return result;
  }

  private List<Address> translateAddressesLocation(List<V9VmOpenApiAddressLocation> ptvAddresses) {
    if (ptvAddresses == null) {
      return Collections.emptyList();
    }

    List<Address> result = new ArrayList<>(ptvAddresses.size());

    for (V9VmOpenApiAddressLocation ptvAddress : ptvAddresses) {
      Address address = translateAddressLocation(ptvAddress);
      if (address != null) {
        result.add(address);
      }
    }

    return result;
  }
  
  private Address translateAddressLocation(V9VmOpenApiAddressLocation ptvAddress) {
    if ((ptvAddress == null)|| (ptvAddress.getPostOfficeBoxAddress() == null && ptvAddress.getStreetAddress() == null && ptvAddress.getLocationAbroad() == null)) {
      return null;
    }
    
    switch (getAddressSubtype(ptvAddress.getSubType())) {
      case SINGLE:
      case STREET:
        return translateStreetAddressWithCoordinates(ptvAddress.getStreetAddress(), ptvAddress.getType(), ptvAddress.getSubType(), ptvAddress.getCountry(), ptvAddress.getEntrances());
      case ABROAD:
        return translateAddressAbroad(ptvAddress.getLocationAbroad(), ptvAddress.getType(), ptvAddress.getSubType(), ptvAddress.getCountry(), ptvAddress.getEntrances());
      case POST_OFFICE_BOX:
        return translatePostOfficeBoxAddress(ptvAddress.getPostOfficeBoxAddress(), ptvAddress.getType(), ptvAddress.getSubType(), ptvAddress.getCountry(), ptvAddress.getEntrances());
      default:
        logger.severe(() -> String.format(UNKNOWN_ADDRESS_SUBTYPE, "with moving", ptvAddress.getSubType(), ptvAddress.toString()));
    }
    
    return translateNoAddress(ptvAddress.getLocationAbroad(), ptvAddress.getType(), ptvAddress.getSubType(), ptvAddress.getEntrances());
  }

  private Address translateAddress(V9VmOpenApiAddress ptvAddress) {
    if (ptvAddress == null) {
      return null;
    }
    
    switch (getAddressSubtype(ptvAddress.getSubType())) {
      case SINGLE:
      case STREET:
        return translateStreetAddressWithCoordinates(ptvAddress.getStreetAddress(), ptvAddress.getType(), ptvAddress.getSubType(), ptvAddress.getCountry(), Collections.emptyList());
      case POST_OFFICE_BOX:
        return translatePostOfficeBoxAddress(ptvAddress.getPostOfficeBoxAddress(), ptvAddress.getType(), ptvAddress.getSubType(), ptvAddress.getCountry(), Collections.emptyList());
      case FOREIGN:
        return translateForeignAddress(ptvAddress.getForeignAddress(), ptvAddress.getType(), ptvAddress.getSubType());
      default:
        logger.severe(() -> String.format(UNKNOWN_ADDRESS_SUBTYPE, ptvAddress.getType(), ptvAddress.getSubType(), ptvAddress.toString()));
    }
    
    return translateNoAddress(ptvAddress.getForeignAddress(), ptvAddress.getType(), ptvAddress.getSubType(), Collections.emptyList());
  }

  private Address translateForeignAddress(List<VmOpenApiLanguageItem> foreignAddress, String type, String subtype) {
    if (foreignAddress == null || foreignAddress.isEmpty()) {
      return null;
    }
    
    Address result = new Address();
    result.setAdditionalInformations(translateLocalizedItems(foreignAddress));
    result.setCoordinateState(null);
    result.setCountry(null);
    result.setLatitude(null);
    result.setLongitude(null);
    result.setCoordinates(null);
    result.setMunicipality(null);
    result.setPostalCode(null);
    result.setPostOffice(null);
    result.setPostOfficeBox(null);
    result.setStreetAddress(null);
    result.setStreetNumber(null);
    result.setType(type);
    result.setSubtype(subtype);
    result.setEntrances(Collections.emptyList());
    
    return result;
  }
  
  private Address translateAddressDelivery(V8VmOpenApiAddressDelivery ptvAddress) {
    if (ptvAddress == null) {
      return null;
    }
    
    switch (getAddressSubtype(ptvAddress.getSubType())) {
      case SINGLE:
      case STREET:
        return translateStreetAddress(ptvAddress.getStreetAddress(), null, ptvAddress.getSubType(), null);
      case POST_OFFICE_BOX:
        return translatePostOfficeBoxAddress(ptvAddress.getPostOfficeBoxAddress(), null, ptvAddress.getSubType(), null, Collections.emptyList());
      case NO_ADDRESS:
        return translateNoAddress(ptvAddress.getDeliveryAddressInText(), null, ptvAddress.getSubType(), Collections.emptyList());
      default:
        logger.severe(() -> String.format(UNKNOWN_ADDRESS_SUBTYPE, "delivery", ptvAddress.getSubType(), ptvAddress.toString()));
    }
    
    return translateNoAddress(ptvAddress.getDeliveryAddressInText(), null, ptvAddress.getSubType(), Collections.emptyList());
  }
  
  private Address translateNoAddress(List<VmOpenApiLanguageItem> deliveryAddressInText, String type, String subtype, List<V9VmOpenApiEntrance> ptvEntrances) {
    if (deliveryAddressInText == null || deliveryAddressInText.isEmpty()) {
      return null;
    }
    
    Address result = new Address();
    result.setAdditionalInformations(translateLocalizedItems(deliveryAddressInText));
    result.setCoordinateState(null);
    result.setCountry(null);
    result.setLatitude(null);
    result.setLongitude(null);
    result.setCoordinates(null);
    result.setMunicipality(null);
    result.setPostalCode(null);
    result.setPostOffice(null);
    result.setPostOfficeBox(null);
    result.setStreetAddress(null);
    result.setStreetNumber(null);
    result.setType(type);
    result.setSubtype(subtype);
    result.setEntrances(translateEntrances(ptvEntrances));
    
    return result;
  }

  private Address translatePostOfficeBoxAddress(VmOpenApiAddressPostOfficeBox ptvAddress, String type, String subtype, String country, List<V9VmOpenApiEntrance> ptvEntrances) {
    if (ptvAddress == null) {
      return null;
    }
    
    Address result = new Address();
    result.setAdditionalInformations(translateLocalizedItems(ptvAddress.getAdditionalInformation()));
    result.setCoordinateState(null);
    result.setCountry(country);
    result.setLatitude(null);
    result.setLongitude(null);
    result.setCoordinates(null);
    result.setMunicipality(translateMunicipality(ptvAddress.getMunicipality()));
    result.setPostalCode(ptvAddress.getPostalCode());
    result.setPostOffice(translateLocalizedItems(ptvAddress.getPostOffice()));
    result.setPostOfficeBox(translateLocalizedItems(ptvAddress.getPostOfficeBox()));
    result.setStreetAddress(null);
    result.setStreetNumber(null);
    result.setType(type);
    result.setSubtype(subtype);
    result.setEntrances(translateEntrances(ptvEntrances));
    
    return result;
  }

  private Address translateAddressAbroad(List<VmOpenApiLanguageItem> ptvLocationAbroad, String type, String subtype, String country, List<V9VmOpenApiEntrance> ptvEntrances) {
    if (ptvLocationAbroad == null) {
      return null;
    }
    
    Address result = new Address();
    result.setAdditionalInformations(null);
    result.setCoordinateState(null);
    result.setCountry(country);
    result.setLatitude(null);
    result.setLongitude(null);
    result.setCoordinates(null);
    result.setMunicipality(null);
    result.setPostalCode(null);
    result.setPostOffice(null);
    result.setPostOfficeBox(null);
    result.setStreetAddress(null);
    result.setStreetNumber(null);
    result.setType(type);
    result.setSubtype(subtype);
    result.setLocationAbroad(translateLocalizedItems(ptvLocationAbroad));
    result.setEntrances(translateEntrances(ptvEntrances));

    return result;
  }

  private Address translateStreetAddressWithCoordinates(VmOpenApiAddressStreetWithCoordinates ptvAddress, String type, String subtype, String country, List<V9VmOpenApiEntrance> ptvEntrances) {
    if (ptvAddress == null) {
      return null;
    }

    Address result = new Address();
    result.setAdditionalInformations(translateLocalizedItems(ptvAddress.getAdditionalInformation()));
    result.setCoordinateState(ptvAddress.getCoordinateState());
    result.setCountry(country);
    result.setLatitude(ptvAddress.getLatitude());
    result.setLongitude(ptvAddress.getLongitude());
    result.setCoordinates(translateCoordinates(ptvAddress.getLatitude(), ptvAddress.getLongitude()));
    result.setMunicipality(translateMunicipality(ptvAddress.getMunicipality()));
    result.setPostalCode(ptvAddress.getPostalCode());
    result.setPostOffice(translateLocalizedItems(ptvAddress.getPostOffice()));
    result.setPostOfficeBox(null);
    result.setStreetAddress(translateLocalizedItems(ptvAddress.getStreet()));
    result.setStreetNumber(ptvAddress.getStreetNumber());
    result.setType(type);
    result.setSubtype(subtype);
    result.setEntrances(translateEntrances(ptvEntrances));
    
    return result;
  }

  /**
   * Translates PTV entrances into Kunta API entrances
   * 
   * @param ptvEntrances PTV entrances
   * @return Kunta API entrances
   */
  private List<AddressEntrance> translateEntrances(List<V9VmOpenApiEntrance> ptvEntrances) {
    if (ptvEntrances == null || ptvEntrances.isEmpty()) {
      return Collections.emptyList();
    }
    
    return ptvEntrances.stream().map(this::translateEntrance).filter(Objects::nonNull).collect(Collectors.toList());
  }
  
  /**
   * Translates PTV entrance into Kunta API entrance
   * 
   * @param ptvEntrance PTV entrance
   * @return Kunta API entrance
   */
  private AddressEntrance translateEntrance(V9VmOpenApiEntrance ptvEntrance) {
    if (ptvEntrance == null) {
      return null;
    }
    
    AddressEntrance result = new AddressEntrance();
    result.setAccessibilitySentences(translateAccessibilitySentences(ptvEntrance.getAccessibilitySentences()));
    result.setContactInfo(translateContactInfo(ptvEntrance.getContactInfo()));
    result.setCoordinates(translateCoordinates(ptvEntrance.getCoordinates()));
    result.setIsMainEntrance(ptvEntrance.getIsMainEntrance());
    result.setName(translateLocalizedItems(ptvEntrance.getName()));
    
    return result;
  }
  
  /**
   * Translates PTV contact info into Kunta API contact info
   * 
   * @param contactInfo PTV contact info 
   * @return Kunta API contact info
   */
  private AccessibilityContactInfo translateContactInfo(VmOpenApiAccessibilityContactInfo contactInfo) {
    if (contactInfo == null) {
      return null;
    }
    
    AccessibilityContactInfo result = new AccessibilityContactInfo();
    result.setEmail(contactInfo.getEmail());
    result.setPhone(contactInfo.getPhone());
    result.setUrl(contactInfo.getUrl());
    
    return result;
  }
  
  /**
   * Translates PTV accessibility sentences into Kunta API accessibility sentences
   * 
   * @param accessibilitySentences PTV accessibility sentences
   * @return Kunta API accessibility sentences
   */
  private List<AccessibilitySentence> translateAccessibilitySentences(List<VmOpenApiAccessibilitySentence> accessibilitySentences) {
    if (accessibilitySentences == null || accessibilitySentences.isEmpty()) {
      return Collections.emptyList();
    }
    
    return accessibilitySentences.stream().map(this::translateAccessibilitySentence).filter(Objects::nonNull).collect(Collectors.toList());
  }

  /**
   * Translates PTV accessibility sentence into Kunta API accessibility sentence
   * 
   * @param accessibilitySentence PTV accessibility sentence
   * @return Kunta API accessibility sentence
   */
  private AccessibilitySentence translateAccessibilitySentence(VmOpenApiAccessibilitySentence accessibilitySentence) {
    if (accessibilitySentence == null) {
      return null;
    }
    
    AccessibilitySentence result = new AccessibilitySentence();
    result.setSentenceGroup(translateLocalizedItems(accessibilitySentence.getSentenceGroup()));
    result.setSentences(translateSentences(accessibilitySentence.getSentences()));
    return result;
  }
  
  /**
   * Translates PTV accessibility sentences into Kunta API accessibility sentences
   * 
   * @param sentence PTV accessibility sentences
   * @return Kunta API accessibility sentences
   */
  private List<AccessibilitySentenceValue> translateSentences(List<VmOpenApiSentenceValue> sentences) {
    if (sentences == null || sentences.isEmpty()) {
      return Collections.emptyList();
    }
    
    return sentences.stream().map(this::translateSentence).filter(Objects::nonNull).collect(Collectors.toList());
  }
  
  /**
   * Translates PTV accessibility sentence into Kunta API accessibility sentence 
   * 
   * @param sentence PTV accessibility sentence
   * @return Kunta API accessibility sentence
   */
  private AccessibilitySentenceValue translateSentence(VmOpenApiSentenceValue sentence) {
    if (sentence == null) {
      return null;
    }
    
    AccessibilitySentenceValue result = new AccessibilitySentenceValue();
    result.setSentence(translateLocalizedItems(sentence.getSentence()));
    
    return result;
  }

  private Address translateStreetAddress(VmOpenApiAddressStreet ptvAddress, String type, String subtype, String country) {
    if (ptvAddress == null) {
      return null;
    }
    
    Address result = new Address();
    result.setAdditionalInformations(translateLocalizedItems(ptvAddress.getAdditionalInformation()));
    result.setCoordinateState(null);
    result.setCountry(country);
    result.setLatitude(null);
    result.setLongitude(null);
    result.setCoordinates(null);
    result.setMunicipality(translateMunicipality(ptvAddress.getMunicipality()));
    result.setPostalCode(ptvAddress.getPostalCode());
    result.setPostOffice(translateLocalizedItems(ptvAddress.getPostOffice()));
    result.setPostOfficeBox(null);
    result.setStreetAddress(translateLocalizedItems(ptvAddress.getStreet()));
    result.setStreetNumber(ptvAddress.getStreetNumber());
    result.setType(type);
    result.setSubtype(subtype);
    result.setEntrances(Collections.emptyList());
    
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

  private Area translateArea(VmOpenApiArea ptvArea) {
    if (ptvArea == null) {
      return null;
    }
    
    Area area = new Area();
    area.setCode(ptvArea.getCode());
    area.setName(translateLocalizedItems(ptvArea.getName()));
    area.setMunicipalities(translateMunicipalities(ptvArea.getMunicipalities()));
    area.setType(ptvArea.getType());
    
    return area;
  }

  private List<Area> translateAreas(List<VmOpenApiArea> ptvAreas) {
    if (ptvAreas == null) {
      return Collections.emptyList();
    }
    
    List<Area> result = new ArrayList<>(ptvAreas.size());
    for (VmOpenApiArea ptvArea : ptvAreas) {
      Area municipality = translateArea(ptvArea);
      if (municipality != null) {
        result.add(municipality);
      }
    }
    
    return result;
  }

  private List<Phone> translatePhonesWithTypes(List<V4VmOpenApiPhoneWithType> ptvPhones) {
    if (ptvPhones == null) {
      return Collections.emptyList();
    }
    
    List<Phone> result = new ArrayList<>(ptvPhones.size());
    for (V4VmOpenApiPhoneWithType ptvPhone : ptvPhones) {
      if (StringUtils.isNotBlank(ptvPhone.getNumber())) {
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
    }
    
    return result;
  }

  private List<Phone> translatePhones(List<V4VmOpenApiPhone> ptvPhones) {
    if (ptvPhones == null) {
      return Collections.emptyList();
    }
    
    List<Phone> result = new ArrayList<>(ptvPhones.size());
    for (V4VmOpenApiPhone ptvPhone : ptvPhones) {
      if (StringUtils.isNotBlank(ptvPhone.getNumber())) {
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
    }
    
    return result;
  }
  
  private List<Email> translateEmails(List<V4VmOpenApiEmail> ptvEmails) {
    if (ptvEmails == null) {
      return Collections.emptyList();
    }
    
    List<Email> result = new ArrayList<>(ptvEmails.size());    
    for (V4VmOpenApiEmail ptvEmail : ptvEmails) {
      if (StringUtils.isNotBlank(ptvEmail.getValue())) {
        Email email = new Email();
        email.setDescription(ptvEmail.getDescription());
        email.setLanguage(ptvEmail.getLanguage());
        email.setValue(ptvEmail.getValue());
        result.add(email);
      }
    }
    
    return result;
  }


  private List<Email> translateEmailsLanguageItem(List<VmOpenApiLanguageItem> ptvEmails) {
    if (ptvEmails == null) {
      return Collections.emptyList();
    }
    
    List<Email> result = new ArrayList<>(ptvEmails.size());    
    for (VmOpenApiLanguageItem ptvEmail : ptvEmails) {
      if (StringUtils.isNotBlank(ptvEmail.getValue())) {
        Email email = new Email();
        email.setDescription(null);
        email.setLanguage(ptvEmail.getLanguage());
        email.setValue(ptvEmail.getValue());
        result.add(email);
      }
    }
    
    return result;
  }

  public List<ServiceHour> translateServiceHours(List<V8VmOpenApiServiceHour> ptvServiceHours) {
    if (ptvServiceHours == null || (ptvServiceHours.isEmpty())) {
      return Collections.emptyList();
    }
    
    List<ServiceHour> result = new ArrayList<>(ptvServiceHours.size());
    for (V8VmOpenApiServiceHour ptvServiceHour : mergePtvServiceHours(ptvServiceHours)) {
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
    
    Collections.sort(result, new ServiceHourComparator());
    
    return result;
  }
  
  private List<V8VmOpenApiServiceHour> mergePtvServiceHours(List<V8VmOpenApiServiceHour> ptvServiceHours) {
    Map<Integer, List<V8VmOpenApiServiceHour>> mapped = mapMergeablePtvServiceHours(ptvServiceHours);
    
    List<V8VmOpenApiServiceHour> result = new ArrayList<>(ptvServiceHours.size());
    
    for (List<V8VmOpenApiServiceHour> serviceHours : mapped.values()) {
      if (!serviceHours.isEmpty()) {
        result.add(mergePtvServiceHour(serviceHours));
      }
    }
    
    return result;    
  }

  private V8VmOpenApiServiceHour mergePtvServiceHour(List<V8VmOpenApiServiceHour> serviceHours) {
    V8VmOpenApiServiceHour ptvServiceHour = serviceHours.get(0);
    
    if (serviceHours.size() > 1) {
      List<V8VmOpenApiDailyOpeningTime> mergedOpeningOurs = new ArrayList<>();
      if (ptvServiceHour.getOpeningHour() != null) {
        mergedOpeningOurs.addAll(ptvServiceHour.getOpeningHour());
      }
      
      for (int i = 1; i < serviceHours.size(); i++) {
        if (serviceHours.get(i).getOpeningHour() != null) {
          mergedOpeningOurs.addAll(serviceHours.get(i).getOpeningHour());
        }
      }
      
      ptvServiceHour.setOpeningHour(mergedOpeningOurs); 
    }
    
    return ptvServiceHour;
  }

  private Map<Integer, List<V8VmOpenApiServiceHour>> mapMergeablePtvServiceHours(List<V8VmOpenApiServiceHour> ptvServiceHours) {
    Map<Integer, List<V8VmOpenApiServiceHour>> result = new HashMap<>(ptvServiceHours.size());
    
    for (V8VmOpenApiServiceHour ptvServiceHour : ptvServiceHours) {
      int serviceHourHash = calculateMergeablePtvServiceHourHash(ptvServiceHour);
      List<V8VmOpenApiServiceHour> serviceHourList = result.get(serviceHourHash);
      if (serviceHourList == null) {
        serviceHourList = new ArrayList<>();
      }
      
      serviceHourList.add(ptvServiceHour);
      
      result.put(serviceHourHash, serviceHourList);
    }
    
    return result;
  }

  /**
   * Calculates hash for mergable service hour. 
   * 
   * Method uses all fields except the opening hours to calulate the hash
   * 
   * @param ptvServiceHour ptv service hour object
   * @return hash for mergable service hour
   */
  private int calculateMergeablePtvServiceHourHash(V8VmOpenApiServiceHour ptvServiceHour) {
    HashCodeBuilder hashCodeBuilder = new HashCodeBuilder(6621, 5511);
    
    if (ptvServiceHour.getAdditionalInformation() != null) {
      for (VmOpenApiLanguageItem additionalInformation : ptvServiceHour.getAdditionalInformation()) {
        hashCodeBuilder.append(additionalInformation.getLanguage());
        hashCodeBuilder.append(additionalInformation.getValue());
      }
    }

    hashCodeBuilder.append(ptvServiceHour.getIsClosed());
    hashCodeBuilder.append(ptvServiceHour.getServiceHourType());
    hashCodeBuilder.append(ptvServiceHour.getValidForNow());
    hashCodeBuilder.append(ptvServiceHour.getValidFrom());
    hashCodeBuilder.append(ptvServiceHour.getValidTo());
    
    return hashCodeBuilder.toHashCode();
  }
  
  private List<DailyOpeningTime> translateOpeningHours(List<V8VmOpenApiDailyOpeningTime> ptvOpeningHours) {
    if (ptvOpeningHours == null) {
      return Collections.emptyList();
    }
    
    List<DailyOpeningTime> result = new ArrayList<>(ptvOpeningHours.size());
    for (V8VmOpenApiDailyOpeningTime ptvOpeningHour : ptvOpeningHours) {
      DailyOpeningTime dailyOpeningTime = new DailyOpeningTime();
      dailyOpeningTime.setDayFrom(translateDay(ptvOpeningHour.getDayFrom()));
      dailyOpeningTime.setDayTo(translateDay(ptvOpeningHour.getDayTo()));
      dailyOpeningTime.setFrom(ptvOpeningHour.getFrom());
      dailyOpeningTime.setIsExtra(false);
      dailyOpeningTime.setTo(ptvOpeningHour.getTo());
      result.add(dailyOpeningTime);
    }
    
    Collections.sort(result, new DailyOpeningTimeComparator());
    
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

  public List<LocalizedValue> translateWebPagesToLocalizedItems(List<V9VmOpenApiWebPage> ptvWebPages) {
    if (ptvWebPages != null && !ptvWebPages.isEmpty()) {
      List<LocalizedValue> result = new ArrayList<>();
      
      for (V9VmOpenApiWebPage ptvWebPage : ptvWebPages) {
        if ((ptvWebPage != null) && StringUtils.isNotBlank(ptvWebPage.getUrl())) {
          LocalizedValue localizedValue = new LocalizedValue();
          localizedValue.setLanguage(ptvWebPage.getLanguage());
          localizedValue.setValue(ptvWebPage.getUrl());
          localizedValue.setType(null);
          result.add(localizedValue);
        }
      }
    
      return result;
    }
    
    return Collections.emptyList();
  }
  
  public List<LocalizedValue> translateLocalizedItems(List<VmOpenApiLanguageItem> items) {
    if (items != null && !items.isEmpty()) {
      List<LocalizedValue> result = new ArrayList<>();
      
      for (VmOpenApiLanguageItem item : items) {
        if ((item != null) && StringUtils.isNotBlank(item.getValue())) {
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
  
  /**
   * Translates PTV code list item into Kunta API code
   * 
   * @param ptvCodeListItem PTV code list item
   * @param codeType code type
   * @return Kunta API code
   */
  public Code translateCode(CodeId kuntaApiId, VmOpenApiDialCodeListItem ptvCodeListItem, CodeType codeType) {
    if (ptvCodeListItem == null) {
      return null;
    }

    List<String> prefixNumbers = ptvCodeListItem.getPrefixNumbers();
    List<CodeExtra> extra = new ArrayList<>(prefixNumbers != null ? prefixNumbers.size() : 0);
    
    for (String prefixNumber : prefixNumbers) {
      CodeExtra codeExtra = new CodeExtra();
      codeExtra.setKey("prefixNumber");
      codeExtra.setValue(prefixNumber);
      extra.add(codeExtra);
    }
    
    Code result = new Code();
    result.setId(kuntaApiId.getId());
    result.setCode(ptvCodeListItem.getCode());
    result.setExtra(extra);
    result.setNames(translateLocalizedItems(ptvCodeListItem.getNames()));
    result.setType(codeType.getType());
    
    return result;
  }

  /**
   * Translates PTV code list item into Kunta API code
   * 
   * @param ptvCodeListItem PTV code list item
   * @param codeType code type
   * @return Kunta API code
   */
  public Code translateCode(CodeId kuntaApiId, VmOpenApiCodeListItem ptvCodeListItem, CodeType codeType) {
    if (ptvCodeListItem == null) {
      return null;
    }

    Code result = new Code();
    result.setId(kuntaApiId.getId());
    result.setCode(ptvCodeListItem.getCode());
    result.setExtra(Collections.emptyList());
    result.setNames(translateLocalizedItems(ptvCodeListItem.getNames()));
    result.setType(codeType.getType());
    
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
  
  private List<WebPage> translateWebPagesWithOrderNumber(List<V9VmOpenApiWebPage> ptvWebPages) {
    if (ptvWebPages == null) {
      return Collections.emptyList();
    }

    List<WebPage> result = new ArrayList<>(ptvWebPages.size());

    for (V9VmOpenApiWebPage ptvWebPage : ptvWebPages) {
      WebPage webPage = translateWebPage(ptvWebPage);
      if (webPage != null) {
        result.add(webPage);
      }
    }

    return result;
  }
  
  private WebPage translateWebPage(V9VmOpenApiWebPage ptvWebPage) {
    if (ptvWebPage == null) {
      return null;
    }
    
    if (StringUtils.isBlank(ptvWebPage.getUrl())) {
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

    if (StringUtils.isBlank(ptvWebPage.getUrl())) {
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
    
    if (StringUtils.isBlank(ptvAttachment.getUrl())) {
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

  private List<NameTypeByLanguage> translateNameTypeByLanguage(List<VmOpenApiNameTypeByLanguage> ptvNameTypesByLanguage) {
    if (ptvNameTypesByLanguage == null) {
      return Collections.emptyList();
    }
    
    List<NameTypeByLanguage> result = new ArrayList<>(ptvNameTypesByLanguage.size());
    
    for (VmOpenApiNameTypeByLanguage ptvNameTypeByLanguage : ptvNameTypesByLanguage) {
      NameTypeByLanguage nameTypeByLanguage = new NameTypeByLanguage();
      nameTypeByLanguage.setLanguage(ptvNameTypeByLanguage.getLanguage());
      nameTypeByLanguage.setType(ptvNameTypeByLanguage.getType());
      result.add(nameTypeByLanguage);
    }
    
    return result;
  }
  
  private List<String> extractIds(List<? extends BaseId> kuntaApiIds) {
    List<String> result = new ArrayList<>(kuntaApiIds.size());
    
    for (BaseId kuntaApiId : kuntaApiIds) {
      result.add(kuntaApiId.getId());
    }
    
    return result;
  }

  /**
   * Translates PTV coordinates object into Kunta API coordinates
   * 
   * @param coordinates PTV coordinates
   * @return Kunta API coordinates
   */
  private Coordinates translateCoordinates(VmOpenApiCoordinates coordinates) {
    if (coordinates == null) {
      return null;
    }
    
    return translateCoordinates(coordinates.getLatitude(), coordinates.getLongitude());
  }

  /**
   * Translates PTV coordinates into Kunta API coordinates
   * 
   * @param ptvLatitude PTV latitude
   * @param ptvLongitude PTV longitude
   * @return Kunta API coordinates
   */
  private Coordinates translateCoordinates(String ptvLatitude, String ptvLongitude) {
    if (StringUtils.isBlank(ptvLatitude) || StringUtils.isBlank(ptvLongitude)) {
      return null;
    }

    if (!NumberUtils.isParsable(ptvLongitude) || !NumberUtils.isParsable(ptvLatitude)){
      logger.warning("coordinates not parsable");
      return null;
    }
    
    Double srcLongitude = NumberUtils.createDouble(ptvLongitude);
    Double srcLatitude = NumberUtils.createDouble(ptvLatitude);
    
    ProjCoordinate srcProjCoordinates = new ProjCoordinate(srcLongitude, srcLatitude);

    if (!srcProjCoordinates.hasValidXandYOrdinates()) {
      logger.warning("coordinates are not valid");
      return null;
    }
    
    ProjCoordinate targetProjCoordinates = new ProjCoordinate();
    
    CRSFactory crsFactory = new CRSFactory();
    CoordinateReferenceSystem srcCRS = crsFactory.createFromName("EPSG:3067");
    CoordinateReferenceSystem targetCRS = crsFactory.createFromName("EPSG:4326");
    
    CoordinateTransformFactory coordinateTransformFactory = new CoordinateTransformFactory();
    CoordinateTransform coordinateTransform = coordinateTransformFactory.createTransform(srcCRS, targetCRS);
    coordinateTransform.transform(srcProjCoordinates, targetProjCoordinates);

    Coordinate epsg3067Coordinate = new Coordinate();
    epsg3067Coordinate.setLatitude(srcLatitude.toString());
    epsg3067Coordinate.setLongitude(srcLongitude.toString());
    
    Coordinate epsg4326Coordinate = new Coordinate();
    epsg4326Coordinate.setLatitude(String.valueOf(targetProjCoordinates.y));
    epsg4326Coordinate.setLongitude(String.valueOf(targetProjCoordinates.x));

    Coordinates coordinates = new Coordinates();
    coordinates.setEpsg3067(epsg3067Coordinate);
    coordinates.setEpsg4326(epsg4326Coordinate);
    
    return coordinates;
  }
  
  @SuppressWarnings ("squid:S1698")
  private final class ServiceHourComparator implements Comparator<ServiceHour> {
    
    @Override
    public int compare(ServiceHour o1, ServiceHour o2) {
      Integer typeIndex1 = getTypeIndex(o1.getServiceHourType());
      Integer typeIndex2 = getTypeIndex(o2.getServiceHourType());
      int result = typeIndex1.compareTo(typeIndex2);
      
      if (result != 0) {
        return result;
      }
      
      return TimeUtils.compareOffsetDateTimes(o1.getValidFrom(), o2.getValidFrom());
    }
    
    private Integer getTypeIndex(String type) {
      if (StringUtils.equals("Standard", type)) {
        return 0;
      }

      if (StringUtils.equals("Special", type)) {
        return 1;
      }

      if (StringUtils.equals("Exception", type)) {
        return 2;
      }
      
      return 3;
    }
    
  }
  
  @SuppressWarnings ("squid:S1698")
  private final class DailyOpeningTimeComparator implements Comparator<DailyOpeningTime> {
    
    @Override
    public int compare(DailyOpeningTime o1, DailyOpeningTime o2) {
      Integer dayFrom1 = o1.getDayFrom();
      Integer dayFrom2 = o2.getDayFrom();

      if (dayFrom1 == dayFrom2) {
        return 0;
      }
      
      if (dayFrom1 == null) {
        return -1;
      }
      
      if (dayFrom2 == null) {
        return 1;
      }
      
      return toMondayFirst(dayFrom1).compareTo(toMondayFirst(dayFrom2));
    }
    
    private Integer toMondayFirst(Integer index) {
      return (index + 6) % 7;
    }
    
  }

  /**
   * Translate UUID into string
   * 
   * @param uuid UUID
   * @return string
   */
  private String translateUUID(UUID uuid) {
    if (uuid == null) {
      return null;
    }
    
    return uuid.toString();
  }
  
}
