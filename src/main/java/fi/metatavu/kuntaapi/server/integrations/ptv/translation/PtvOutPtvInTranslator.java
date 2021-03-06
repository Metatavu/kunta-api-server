package fi.metatavu.kuntaapi.server.integrations.ptv.translation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.ptv.client.model.V4VmOpenApiFintoItem;
import fi.metatavu.ptv.client.model.V4VmOpenApiPhone;
import fi.metatavu.ptv.client.model.V4VmOpenApiPhoneSimple;
import fi.metatavu.ptv.client.model.V4VmOpenApiPhoneWithType;
import fi.metatavu.ptv.client.model.V6VmOpenApiServiceOrganization;
import fi.metatavu.ptv.client.model.V7VmOpenApiAddressContact;
import fi.metatavu.ptv.client.model.V7VmOpenApiAddressContactIn;
import fi.metatavu.ptv.client.model.V7VmOpenApiFintoItemWithDescription;
import fi.metatavu.ptv.client.model.V8VmOpenApiAddressDelivery;
import fi.metatavu.ptv.client.model.V8VmOpenApiAddressDeliveryIn;
import fi.metatavu.ptv.client.model.V9VmOpenApiAddressLocation;
import fi.metatavu.ptv.client.model.V9VmOpenApiAddressLocationIn;
import fi.metatavu.ptv.client.model.V9VmOpenApiContactDetails;
import fi.metatavu.ptv.client.model.V9VmOpenApiContactDetailsInBase;
import fi.metatavu.ptv.client.model.V9VmOpenApiElectronicChannel;
import fi.metatavu.ptv.client.model.V9VmOpenApiElectronicChannelInBase;
import fi.metatavu.ptv.client.model.V9VmOpenApiPhoneChannel;
import fi.metatavu.ptv.client.model.V9VmOpenApiPhoneChannelInBase;
import fi.metatavu.ptv.client.model.V9VmOpenApiPrintableFormChannel;
import fi.metatavu.ptv.client.model.V9VmOpenApiPrintableFormChannelInBase;
import fi.metatavu.ptv.client.model.V9VmOpenApiService;
import fi.metatavu.ptv.client.model.V9VmOpenApiServiceInBase;
import fi.metatavu.ptv.client.model.V9VmOpenApiServiceLocationChannel;
import fi.metatavu.ptv.client.model.V9VmOpenApiServiceLocationChannelInBase;
import fi.metatavu.ptv.client.model.V9VmOpenApiServiceProducerIn;
import fi.metatavu.ptv.client.model.V9VmOpenApiServiceServiceChannel;
import fi.metatavu.ptv.client.model.V9VmOpenApiServiceServiceChannelInBase;
import fi.metatavu.ptv.client.model.V9VmOpenApiWebPage;
import fi.metatavu.ptv.client.model.V9VmOpenApiWebPageChannel;
import fi.metatavu.ptv.client.model.V9VmOpenApiWebPageChannelInBase;
import fi.metatavu.ptv.client.model.VmOpenApiAddressOther;
import fi.metatavu.ptv.client.model.VmOpenApiAddressOtherIn;
import fi.metatavu.ptv.client.model.VmOpenApiAddressPostOfficeBox;
import fi.metatavu.ptv.client.model.VmOpenApiAddressPostOfficeBoxIn;
import fi.metatavu.ptv.client.model.VmOpenApiAddressStreet;
import fi.metatavu.ptv.client.model.VmOpenApiAddressStreetIn;
import fi.metatavu.ptv.client.model.VmOpenApiAddressStreetWithCoordinates;
import fi.metatavu.ptv.client.model.VmOpenApiAddressStreetWithCoordinatesIn;
import fi.metatavu.ptv.client.model.VmOpenApiArea;
import fi.metatavu.ptv.client.model.VmOpenApiAreaIn;
import fi.metatavu.ptv.client.model.VmOpenApiAttachment;
import fi.metatavu.ptv.client.model.VmOpenApiAttachmentWithType;
import fi.metatavu.ptv.client.model.VmOpenApiItem;
import fi.metatavu.ptv.client.model.VmOpenApiLanguageItem;
import fi.metatavu.ptv.client.model.VmOpenApiLocalizedListItem;
import fi.metatavu.ptv.client.model.VmOpenApiMunicipality;

/**
 * Translator for translating resources from PTV out format into PTV in format
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class PtvOutPtvInTranslator extends AbstractTranslator {
  
  /**
   * Translates PTV out service location channel into PTV in service location channel
   * 
   * @param ptvResource PTV out service location channel
   * @return PTV in service location channel
   */
  public V9VmOpenApiServiceLocationChannelInBase translateServiceLocationChannel(V9VmOpenApiServiceLocationChannel ptvResource) {
    V9VmOpenApiServiceLocationChannelInBase result = new V9VmOpenApiServiceLocationChannelInBase();
    result.setAddresses(translateAddressesLocation(ptvResource.getAddresses()));
    result.setAreas(translateAreas(ptvResource.getAreas()));
    result.setAreaType(ptvResource.getAreaType());
    result.setDeleteAllEmails(true);
    result.setDeleteAllFaxNumbers(true);
    result.setDeleteAllServiceHours(true);
    result.setDeleteAllWebPages(true);
    result.setIsVisibleForAll(true);
    result.setDeleteAllPhoneNumbers(true);
    result.setDeleteOid(false);
    result.setDisplayNameType(ptvResource.getDisplayNameType());
    result.setEmails(ptvResource.getEmails());
    result.setFaxNumbers(translateFaxNumbers(ptvResource.getPhoneNumbers()));
    result.setLanguages(ptvResource.getLanguages());
    result.setOid(ptvResource.getOid());
    result.setOrganizationId(ptvResource.getOrganizationId().toString());
    result.setPhoneNumbers(translatePhoneNumbers(ptvResource.getPhoneNumbers()));
    result.setPublishingStatus(ptvResource.getPublishingStatus());
    result.setServiceChannelDescriptions(ptvResource.getServiceChannelDescriptions());
    result.setServiceChannelNames(ptvResource.getServiceChannelNames());
    result.setServiceHours(ptvResource.getServiceHours());
    result.setSourceId(ptvResource.getSourceId());
    result.setWebPages(ptvResource.getWebPages());
    
    return result;
  }

  /**
   * Translates PTV out service location channel into PTV in service location channel
   * 
   * @param ptvResource PTV out service location channel
   * @return PTV in service location channel
   */
  public V9VmOpenApiElectronicChannelInBase translateElectronicChannel(V9VmOpenApiElectronicChannel ptvResource) {
    V9VmOpenApiElectronicChannelInBase result = new V9VmOpenApiElectronicChannelInBase();
    
    result.setAccessibilityClassificationLevel(ptvResource.getAccessibilityClassificationLevel());
    result.setAccessibilityStatementWebPage(ptvResource.getAccessibilityStatementWebPage());
    result.setAreas(translateAreas(ptvResource.getAreas()));
    result.setAreaType(ptvResource.getAreaType());
    result.setAttachments(translateAttachments(ptvResource.getAttachments()));
    result.setIsVisibleForAll(true);
    result.setOrganizationId(ptvResource.getOrganizationId().toString());
    result.setPublishingStatus(ptvResource.getPublishingStatus());
    result.setRequiresAuthentication(ptvResource.getRequiresAuthentication());
    result.setRequiresSignature(ptvResource.getRequiresSignature());
    result.setServiceChannelDescriptions(ptvResource.getServiceChannelDescriptions());
    result.setServiceChannelNames(translateLocalizedListItemsToLanguageItems(ptvResource.getServiceChannelNames()));
    result.setServiceHours(ptvResource.getServiceHours());
    result.setSignatureQuantity(String.valueOf(ptvResource.getSignatureQuantity()));
    result.setSourceId(ptvResource.getSourceId());
    result.setSupportEmails(ptvResource.getSupportEmails());
    result.setSupportPhones(ptvResource.getSupportPhones());
    result.setWcagLevel(ptvResource.getWcagLevel());
    result.setWebPage(translateWebPagesToLanguageItems(ptvResource.getWebPages()));
    
    return result;
  }

  /**
   * Translates PTV out phone service channel into PTV in phone service channel
   * 
   * @param ptvResource PTV out phone service channel
   * @return PTV in phone service channel
   */
  public V9VmOpenApiPhoneChannelInBase translatePhoneChannel(V9VmOpenApiPhoneChannel ptvResource) {
    if (ptvResource == null) {
      return null;
    }
    
    V9VmOpenApiPhoneChannelInBase result = new V9VmOpenApiPhoneChannelInBase();
    result.setAreas(translateAreas(ptvResource.getAreas()));
    result.setAreaType(ptvResource.getAreaType());
    result.setDeleteAllServiceHours(true);
    result.setDeleteAllWebPages(true);
    result.setIsVisibleForAll(true);
    result.setLanguages(ptvResource.getLanguages());
    result.setOrganizationId(ptvResource.getOrganizationId().toString());
    result.setPhoneNumbers(ptvResource.getPhoneNumbers());
    result.setPublishingStatus(ptvResource.getPublishingStatus());
    result.setServiceChannelDescriptions(ptvResource.getServiceChannelDescriptions());
    result.setServiceChannelNames(translateLocalizedListItemsToLanguageItems(ptvResource.getServiceChannelNames()));
    result.setServiceHours(ptvResource.getServiceHours());
    result.setSourceId(ptvResource.getSourceId());
    result.setSupportEmails(ptvResource.getSupportEmails());
    result.setWebPage(translateWebPagesToLanguageItems(ptvResource.getWebPages()));
    
    return result;
  }
  
  /**
   * Translates PTV out printable form service channel into PTV in printable form service channel
   * 
   * @param ptvResource PTV out service channel
   * @return PTV in service channel
   */
  public V9VmOpenApiPrintableFormChannelInBase translatePrintableFormChannel(V9VmOpenApiPrintableFormChannel ptvResource) {
    if (ptvResource == null) {
      return null;
    }
    
    V9VmOpenApiPrintableFormChannelInBase result = new V9VmOpenApiPrintableFormChannelInBase();
    result.setAreas(translateAreas(ptvResource.getAreas()));
    result.setAreaType(ptvResource.getAreaType());
    result.setAttachments(translateAttachments(ptvResource.getAttachments()));
    result.setChannelUrls(ptvResource.getChannelUrls());
    result.setDeleteAllAttachments(true);
    result.setDeleteAllChannelUrls(true);
    result.setDeleteAllFormIdentifiers(true);
    result.setDeleteAllSupportEmails(true);
    result.setDeleteAllSupportPhones(true);
    result.setDeleteAllDeliveryAddresses(true);
    result.setDeliveryAddresses(translateDeliveryAddresses(ptvResource.getDeliveryAddresses()));
    result.setFormIdentifier(ptvResource.getFormIdentifier());
    result.setIsVisibleForAll(true);
    result.setOrganizationId(ptvResource.getOrganizationId().toString());
    result.setPublishingStatus(ptvResource.getPublishingStatus());
    result.setServiceChannelDescriptions(ptvResource.getServiceChannelDescriptions());
    result.setServiceChannelNames(translateLocalizedListItemsToLanguageItems(ptvResource.getServiceChannelNames()));
    result.setSourceId(ptvResource.getSourceId());
    result.setSupportEmails(ptvResource.getSupportEmails());
    result.setSupportPhones(ptvResource.getSupportPhones());
    
    return result;
  }

  /**
   * Translates PTV out service channel into PTV in service channel
   * 
   * @param ptvResource PTV out service channel
   * @return PTV in service channel
   */
  public V9VmOpenApiWebPageChannelInBase translateWebPageChannel(V9VmOpenApiWebPageChannel ptvResource) {
    V9VmOpenApiWebPageChannelInBase result = new V9VmOpenApiWebPageChannelInBase();

    result.setAccessibilityClassificationLevel(ptvResource.getAccessibilityClassificationLevel());
    result.setAccessibilityStatementWebPage(ptvResource.getAccessibilityStatementWebPage());
    result.setDeleteAllSupportEmails(true);
    result.setDeleteAllSupportPhones(true);
    result.setIsVisibleForAll(true);
    result.setLanguages(ptvResource.getLanguages());
    result.setOrganizationId(ptvResource.getOrganizationId().toString());
    result.setPublishingStatus(ptvResource.getPublishingStatus());
    result.setServiceChannelDescriptions(ptvResource.getServiceChannelDescriptions());
    result.setServiceChannelNames(translateLocalizedListItemsToLanguageItems(ptvResource.getServiceChannelNames()));
    result.setSourceId(ptvResource.getSourceId());
    result.setSupportEmails(ptvResource.getSupportEmails());
    result.setSupportPhones(ptvResource.getSupportPhones());
    result.setWcagLevel(ptvResource.getWcagLevel());  
    result.setWebPage(translateWebPagesToLanguageItems(ptvResource.getWebPages()));
    
    return result;
  }

  /**
   * Translates PTV out service into PTV in service
   * 
   * @param service PTV out service
   * @return PTV in service
   */
  public V9VmOpenApiServiceInBase translateService(V9VmOpenApiService service) {
    if (service == null) {
      return null;
    }
    
    String mainResponsibleOrganization = null;
    List<UUID> otherResponsibleOrganizations = new ArrayList<>();
    List<V9VmOpenApiServiceProducerIn> serviceProducers = new ArrayList<>();
    
    List<V6VmOpenApiServiceOrganization> organizations = service.getOrganizations();
    for (V6VmOpenApiServiceOrganization organization : organizations) {
      VmOpenApiItem organizationItem = organization.getOrganization();
      if (organizationItem != null && organizationItem.getId() != null) {
        UUID organizationUuid = organizationItem.getId();
        String organizationId = organizationUuid.toString();
        
        if ("Responsible".equals(organization.getRoleType())) {
          if (mainResponsibleOrganization == null) {
            mainResponsibleOrganization = organizationId;
          } else {
            otherResponsibleOrganizations.add(UUID.fromString(organizationId));
          }
        } else {
          V9VmOpenApiServiceProducerIn apiServiceProducer = new V9VmOpenApiServiceProducerIn();
          apiServiceProducer.setAdditionalInformation(organization.getAdditionalInformation());
          apiServiceProducer.setOrganizations(Arrays.asList(organizationUuid));
          apiServiceProducer.setProvisionType(organization.getProvisionType());
          serviceProducers.add(apiServiceProducer); 
        }
      }
    }
    
    V9VmOpenApiServiceInBase result = new V9VmOpenApiServiceInBase();
    
    result.setAreas(translateAreas(service.getAreas()));
    result.setAreaType(service.getAreaType());
    result.setFundingType(service.getFundingType());
    result.setGeneralDescriptionId(translateUuid(service.getGeneralDescriptionId()));
    result.setIndustrialClasses(translateFintoItems(service.getIndustrialClasses()));
    result.setKeywords(service.getKeywords());
    result.setLanguages(service.getLanguages());
    result.setLegislation(service.getLegislation());
    result.setLifeEvents(translateFintoItems(service.getLifeEvents()));
    result.setMainResponsibleOrganization(mainResponsibleOrganization);
    result.setOntologyTerms(translateFintoItems(service.getOntologyTerms()));
    result.setOtherResponsibleOrganizations(otherResponsibleOrganizations);
    result.setPublishingStatus(service.getPublishingStatus());
    result.setRequirements(service.getRequirements());
    result.setServiceChargeType(service.getServiceChargeType());
    result.setServiceClasses(translateFintoItemWithDescriptions(service.getServiceClasses()));
    result.setServiceDescriptions(service.getServiceDescriptions());
    result.setServiceNames(service.getServiceNames());
    result.setServiceProducers(serviceProducers);
    result.setServiceVouchers(service.getServiceVouchers());
    result.setServiceVouchersInUse(service.getServiceVouchersInUse());
    result.setSourceId(service.getSourceId());
    result.setSubType(service.getSubType());
    result.setTargetGroups(translateFintoItems(service.getTargetGroups()));
    result.setType(service.getType());
    
    return result;
  }

  /**
   * Translates service's service channel connection from PTV out to PTV in
   * 
   * @param serviceChannel service's service channel connection in PTV out format
   * @return service's service channel connection in PTV in format
   */
  public V9VmOpenApiServiceServiceChannelInBase translateServiceServiceChannel(V9VmOpenApiServiceServiceChannel serviceChannel) {
    if (serviceChannel == null || serviceChannel.getServiceChannel() == null) {
      return null;
    }
    
    V9VmOpenApiServiceServiceChannelInBase result = new V9VmOpenApiServiceServiceChannelInBase();
    result.setContactDetails(translateContractDetails(serviceChannel.getContactDetails()));
    result.setDeleteAllDescriptions(false);
    result.setDeleteAllServiceHours(false);
    result.setDeleteServiceChargeType(false);
    result.setDescription(serviceChannel.getDescription());
    result.setServiceChannelId(serviceChannel.getServiceChannel().getId().toString());
    result.setServiceChargeType(serviceChannel.getServiceChargeType());
    result.setServiceHours(serviceChannel.getServiceHours());
    
    return result;
  }

  /**
   * Translates PTV web pages into PTV language items
   * 
   * @param webPages PTV web pages
   * @return PTV language items
   */
  private List<VmOpenApiLanguageItem> translateWebPagesToLanguageItems(List<V9VmOpenApiWebPage> webPages) {
    if (webPages == null || webPages.isEmpty()) {
      return Collections.emptyList();
    }
    
    return webPages.stream()
      .map(this::translateWebPageToLanguageItem)
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
  }

  /**
   * Translates PTV web page into PTV language item
   * 
   * @param webPage PTV web page
   * @return PTV language item
   */
  private VmOpenApiLanguageItem translateWebPageToLanguageItem(V9VmOpenApiWebPage webPage) {
    if (webPage == null) {
      return null;
    }
   
    VmOpenApiLanguageItem result = new VmOpenApiLanguageItem();
    result.setLanguage(webPage.getLanguage());
    result.setValue(webPage.getUrl());
    
    return result; 
  }

  /**
   * Translates PTV out contact details into PTV in contact details
   * 
   * @param contactDetails PTV out contact details
   * @return PTV in contact details
   */
  private V9VmOpenApiContactDetailsInBase translateContractDetails(V9VmOpenApiContactDetails contactDetails) {
    if (contactDetails == null) {
      return null;
    }
    
    V9VmOpenApiContactDetailsInBase result = new V9VmOpenApiContactDetailsInBase();
    result.setAddresses(translateAddressContacts(contactDetails.getAddresses()));
    result.setDeleteAllAddresses(true);
    result.setDeleteAllEmails(true);
    result.setDeleteAllPhones(true);
    result.setDeleteAllWebPages(true);
    result.setEmails(contactDetails.getEmails());
    result.setFaxNumbers(translateFaxNumbers(contactDetails.getPhoneNumbers()));
    result.setPhoneNumbers(translatePhoneNumbers(contactDetails.getPhoneNumbers()));
    result.setWebPages(contactDetails.getWebPages());
    
    return result;
  }

  /**
   * Translates list of address contacts from PTV out to PTV in
   * 
   * @param addressContacts list of address contacts in PTV out format
   * @return list of address contacts in PTV in format
   */
  private List<V7VmOpenApiAddressContactIn> translateAddressContacts(List<V7VmOpenApiAddressContact> addressContacts) {
    if (addressContacts == null || addressContacts.isEmpty()) {
      return Collections.emptyList();
    }
    
    return addressContacts.stream()
        .map(this::translateAddressContact)
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
  }

  /**
   * Translates address contact from PTV out to PTV in
   * 
   * @param addressContact address contact in PTV out format
   * @return addressContact address contact in PTV in format
   */
  private V7VmOpenApiAddressContactIn translateAddressContact(V7VmOpenApiAddressContact addressContact) {
    if (addressContact == null) {
      return null;
    }
    
    V7VmOpenApiAddressContactIn result = new V7VmOpenApiAddressContactIn();
    result.setCountry(addressContact.getCountry());
    result.setLocationAbroad(addressContact.getLocationAbroad());
    result.setPostOfficeBoxAddress(translatePostOfficeBoxAddress(addressContact.getPostOfficeBoxAddress()));
    result.setStreetAddress(translateAddressStreetWithCoordinates(addressContact.getStreetAddress()));
    result.setSubType(addressContact.getSubType());
    result.setType(addressContact.getType());
    
    return result;
  }

  /**
   * Translates Finto items into uris
   * 
   * @param fintoItems items
   * @return finto uris
   */
  private List<String> translateFintoItems(List<V4VmOpenApiFintoItem> fintoItems) {
    if (fintoItems == null) {
      return Collections.emptyList();
    }

    return fintoItems.stream()
      .map(V4VmOpenApiFintoItem::getUri)
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
  }

  /**
   * Translates Finto items into uris
   * 
   * @param fintoItems items
   * @return finto uris
   */
  private List<String> translateFintoItemWithDescriptions(List<V7VmOpenApiFintoItemWithDescription> fintoItems) {
    if (fintoItems == null) {
      return Collections.emptyList();
    }

    return fintoItems.stream()
      .map(V7VmOpenApiFintoItemWithDescription::getUri)
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
  }

  /**
   * Translates address locations from PTV out to PTV in
   * 
   * @param addresses address locations in PTV out format
   * @return address locations in PTV in format
   */
  private List<V9VmOpenApiAddressLocationIn> translateAddressesLocation(List<V9VmOpenApiAddressLocation> addresses) {
    if (addresses == null || addresses.isEmpty()) {
      return Collections.emptyList();
    }
    
    List<V9VmOpenApiAddressLocationIn> result = new ArrayList<>();
    
    for (V9VmOpenApiAddressLocation address : addresses) {
      result.add(translateAddressLocation(address));
    }
    
    return result;
  }

  /**
   * Translates list of address delivery entities from PTV out to PTV in
   * 
   * @param deliveryAddresses list of address delivery entities in PTV out format
   * @return list of address delivery entities in PTV in format
   */
  private List<V8VmOpenApiAddressDeliveryIn> translateDeliveryAddresses(List<V8VmOpenApiAddressDelivery> deliveryAddresses) {
    if (deliveryAddresses == null || deliveryAddresses.isEmpty()) {
      return Collections.emptyList();  
    }
    
    return deliveryAddresses.stream()
      .map(this::translateDeliveryAddress)
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
  }

  /**
   * Translates address delivery from PTV out to PTV in
   * 
   * @param address address delivery in PTV out format
   * @return address delivery in PTV in format
   */
  private V8VmOpenApiAddressDeliveryIn translateDeliveryAddress(V8VmOpenApiAddressDelivery address) {
    if (address == null) {
      return null;
    }
    
    V8VmOpenApiAddressDeliveryIn result = new V8VmOpenApiAddressDeliveryIn();
    result.setDeliveryAddressInText(address.getDeliveryAddressInText());
    result.setFormReceiver(address.getReceiver());
    result.setPostOfficeBoxAddress(translatePostOfficeBoxAddress(address.getPostOfficeBoxAddress()));
    result.setStreetAddress(translateAddressStreet(address.getStreetAddress()));
    result.setSubType(address.getSubType());
    
    return result;
  }

  /**
   * Translates address location from PTV out to PTV in
   * 
   * @param address address location in PTV out format
   * @return address location in PTV in format
   */
  private V9VmOpenApiAddressLocationIn translateAddressLocation(V9VmOpenApiAddressLocation address) {
    if (address == null) {
      return null;
    }
    
    V9VmOpenApiAddressLocationIn result = new V9VmOpenApiAddressLocationIn();
    
    result.setCountry(address.getCountry());
    result.setLocationAbroad(address.getLocationAbroad());
    result.setOtherAddress(translateOtherAddress(address.getOtherAddress()));
    result.setPostOfficeBoxAddress(translatePostOfficeBoxAddress(address.getPostOfficeBoxAddress()));
    result.setStreetAddress(translateAddressStreetWithCoordinates(address.getStreetAddress()));
    result.setSubType(address.getSubType());
    result.setType(address.getType());
    
    if ("Visiting".equals(result.getType())) {
      result.setType("Location");
      result.setSubType(PtvAddressSubtype.SINGLE.getPtvValue());
    }
        
    return result;
  }

  /**
   * Translates address post office box from PTV out to PTV in
   * 
   * @param postOfficeBoxAddress address post office box in PTV out format
   * @return address post office box in PTV in format
   */
  private VmOpenApiAddressPostOfficeBoxIn translatePostOfficeBoxAddress(VmOpenApiAddressPostOfficeBox postOfficeBoxAddress) {
    if (postOfficeBoxAddress == null) {
      return null;
    }
    
    VmOpenApiAddressPostOfficeBoxIn result = new VmOpenApiAddressPostOfficeBoxIn();
    result.setAdditionalInformation(postOfficeBoxAddress.getAdditionalInformation());
    
    if (postOfficeBoxAddress.getMunicipality() != null) {
      result.setMunicipality(postOfficeBoxAddress.getMunicipality().getCode());
    }
    
    result.setPostalCode(postOfficeBoxAddress.getPostalCode());
    result.setPostOfficeBox(postOfficeBoxAddress.getPostOfficeBox());
    
    return result;
  }

  /**
   * Translates other address from PTV out to PTV in
   * 
   * @param otherAddress other address in PTV out format
   * @return other address in PTV in format
   */
  private VmOpenApiAddressOtherIn translateOtherAddress(VmOpenApiAddressOther otherAddress) {
    if (otherAddress == null) {
      return null;
    }
    
    VmOpenApiAddressOtherIn result = new VmOpenApiAddressOtherIn();
    result.setAdditionalInformation(otherAddress.getAdditionalInformation());
    result.setLatitude(otherAddress.getLatitude());
    result.setLongitude(otherAddress.getLongitude());
    result.setPostalCode(otherAddress.getPostalCode());

    return result;
  }

  /**
   * Translates street address from PTV out to PTV in
   * 
   * @param streetAddress street address in PTV out format
   * @return street address in PTV in format
   */
  private VmOpenApiAddressStreetIn translateAddressStreet(VmOpenApiAddressStreet streetAddress) {
    if (streetAddress == null) {
      return null;
    }
    
    VmOpenApiAddressStreetIn result = new VmOpenApiAddressStreetIn();
    result.setAdditionalInformation(streetAddress.getAdditionalInformation());
    result.setMunicipality(streetAddress.getMunicipality() != null ? streetAddress.getMunicipality().getCode() : null);
    result.setPostalCode(streetAddress.getPostalCode());
    result.setStreet(streetAddress.getStreet());
    result.setStreetNumber(streetAddress.getStreetNumber());
    
    return result;
  }
  
  /**
   * Translates street address from PTV out to PTV in
   * 
   * @param streetAddress street address in PTV out format
   * @return street address in PTV in format
   */
  private VmOpenApiAddressStreetWithCoordinatesIn translateAddressStreetWithCoordinates(VmOpenApiAddressStreetWithCoordinates streetAddress) {
    if (streetAddress == null) {
      return null;
    }
    
    VmOpenApiAddressStreetWithCoordinatesIn result = new VmOpenApiAddressStreetWithCoordinatesIn();
    result.setAdditionalInformation(streetAddress.getAdditionalInformation());
    result.setLatitude(streetAddress.getLatitude());
    result.setLongitude(streetAddress.getLongitude());
    result.setMunicipality(streetAddress.getMunicipality() != null ? streetAddress.getMunicipality().getCode() : null);
    result.setPostalCode(streetAddress.getPostalCode());
    result.setStreet(streetAddress.getStreet());
    result.setStreetNumber(streetAddress.getStreetNumber());
    
    return result;
  }

  /**
   * Translates areas from PTV out to PTV in
   * 
   * @param areas areas in PTV out format
   * @return areas in PTV in format
   */
  private List<VmOpenApiAreaIn> translateAreas(List<VmOpenApiArea> areas) {
    if (areas == null || areas.isEmpty()) {
      return Collections.emptyList();
    }
    
    List<VmOpenApiAreaIn> result = new ArrayList<>(areas.size());
    
    for (VmOpenApiArea area : areas) {
      VmOpenApiAreaIn areaIn = translateArea(area);
      if (areaIn != null) {
        result.add(areaIn);
      }
    }
    
    return result;
  }

  /**
   * Translates attachments from PTV out to PTV in
   * 
   * @param attachments attachments in PTV out format
   * @return attachments in PTV in format
   */
  private List<VmOpenApiAttachment> translateAttachments(List<VmOpenApiAttachmentWithType> attachments) {
    if (attachments == null) {
      return Collections.emptyList();
    }
    
    return attachments.stream()
      .map(this::translateAttachment)
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
  }
  
  /**
   * Translates attachment from PTV out to PTV in
   * 
   * @param attachment attachment in PTV out format
   * @return attachment in PTV in format
   */
  private VmOpenApiAttachment translateAttachment(VmOpenApiAttachmentWithType attachment) {
    if (attachment == null) {
      return null;
    }
    
    VmOpenApiAttachment result = new VmOpenApiAttachment();
    result.setDescription(attachment.getDescription());
    result.setLanguage(attachment.getLanguage());
    result.setName(attachment.getName());
    result.setUrl(attachment.getUrl());
    
    return result;
  }
  
  /**
   * Translates area from PTV out to PTV in
   * 
   * @param area area in PTV out format
   * @return area in PTV in format
   */
  private VmOpenApiAreaIn translateArea(VmOpenApiArea area) {
    if (area == null) {
      return null;
    }
    
    List<String> areaCodes = null;
    
    VmOpenApiAreaIn result = new VmOpenApiAreaIn();
    
    
    if ("Municipality".equals(area.getType())) {
      if (area.getMunicipalities() != null) {
        areaCodes = area.getMunicipalities()
          .stream()
          .map(VmOpenApiMunicipality::getCode)
          .filter(Objects::nonNull)
          .collect(Collectors.toList());
      } else {
        areaCodes = Collections.emptyList();
      }
    } else {
      areaCodes = Arrays.asList(area.getCode());
    }
    
    result.setAreaCodes(areaCodes);
    result.setType(area.getType());
    
    return result;
  }

  /**
   * Translates phone numbers with type from PTV out to PTV in phones
   * 
   * @param phoneNumbers phone numbers with type
   * @return PTV in phones
   */
  private List<V4VmOpenApiPhone> translatePhoneNumbers(List<V4VmOpenApiPhoneWithType> phoneNumbers) {
    if (phoneNumbers == null || phoneNumbers.isEmpty()) {
      return Collections.emptyList();
    }
    
    List<V4VmOpenApiPhone> result = new ArrayList<>(phoneNumbers.size());
    
    for (V4VmOpenApiPhoneWithType phoneNumber : phoneNumbers) {
      if (phoneNumber != null && "Phone".equals(phoneNumber.getType())) {
        result.add(translatePhone(phoneNumber));
      }
    }
    
    return result;
  }

  /**
   * Translates phone numbers with type from PTV out to PTV in fax numbers
   * 
   * @param phoneNumbers phone numbers with type
   * @return PTV in fax numbers
   */
  private List<V4VmOpenApiPhoneSimple> translateFaxNumbers(List<V4VmOpenApiPhoneWithType> phoneNumbers) {
    if (phoneNumbers == null || phoneNumbers.isEmpty()) {
      return Collections.emptyList();
    }
    
    List<V4VmOpenApiPhoneSimple> result = new ArrayList<>(phoneNumbers.size());
    
    for (V4VmOpenApiPhoneWithType phoneNumber : phoneNumbers) {
      if (phoneNumber != null && "Fax".equals(phoneNumber.getType())) {
        result.add(translatePhoneSimple(phoneNumber));
      }
    }
    
    return result;
  }

  /**
   * Translates phone number with type from PTV out to PTV in phone
   * 
   * @param phoneNumber phone numbers with type
   * @return PTV in phone
   */
  private V4VmOpenApiPhone translatePhone(V4VmOpenApiPhoneWithType phoneNumber) {
    if (phoneNumber == null) {
      return null;
    }
    
    V4VmOpenApiPhone result = new V4VmOpenApiPhone();
    result.setAdditionalInformation(phoneNumber.getAdditionalInformation());
    result.setChargeDescription(phoneNumber.getChargeDescription());
    result.setIsFinnishServiceNumber(phoneNumber.getIsFinnishServiceNumber());
    result.setLanguage(phoneNumber.getLanguage());
    result.setNumber(phoneNumber.getNumber());
    result.setPrefixNumber(phoneNumber.getPrefixNumber());
    result.setServiceChargeType(phoneNumber.getServiceChargeType());
    
    return result;
  }

  /**
   * Translates phone number with type from PTV out to PTV in phone simple
   * 
   * @param phoneNumber phone numbers with type
   * @return PTV in phone simple
   */
  private V4VmOpenApiPhoneSimple translatePhoneSimple(V4VmOpenApiPhoneWithType phoneNumber) {
    if (phoneNumber == null) {
      return null;
    }
    
    V4VmOpenApiPhoneSimple result = new V4VmOpenApiPhoneSimple();
    result.setIsFinnishServiceNumber(phoneNumber.getIsFinnishServiceNumber());
    result.setLanguage(phoneNumber.getLanguage());
    result.setNumber(phoneNumber.getNumber());
    result.setPrefixNumber(phoneNumber.getPrefixNumber());

    return result;
  }

  /**
   * Translates localized list items into language items
   * 
   * @param listItems localized list items
   * @return language items
   */
  private List<VmOpenApiLanguageItem> translateLocalizedListItemsToLanguageItems(List<VmOpenApiLocalizedListItem> listItems) {
    if (listItems == null || listItems.isEmpty()) {
      return Collections.emptyList();
    }
    
    List<VmOpenApiLanguageItem> result = new ArrayList<>(listItems.size());
    
    for (VmOpenApiLocalizedListItem listItem : listItems) {
      VmOpenApiLanguageItem languageItem = translateLocalizedListItemsToLanguageItems(listItem);
      if (languageItem != null) {
        result.add(languageItem);
      }
    }

    return result;
  }
  
  /**
   * Translates localized list item into language item
   * 
   * @param listItem localized list item
   * @return language item
   */
  private VmOpenApiLanguageItem translateLocalizedListItemsToLanguageItems(VmOpenApiLocalizedListItem listItem) {
    if (listItem == null) {
      return null;
    }
    
    VmOpenApiLanguageItem result = new VmOpenApiLanguageItem();
    result.setLanguage(listItem.getLanguage());
    result.setValue(listItem.getValue());

    return result;
  }

  /**
   * Translates UUID into string
   * 
   * @param uuid UUID
   * @return string
   */
  private String translateUuid(UUID uuid) {
    if (uuid == null) {
      return null;
    }
    
    return uuid.toString();
  }
}
