package fi.metatavu.kuntaapi.server.integrations.ptv.translation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import fi.metatavu.ptv.client.model.V4VmOpenApiFintoItem;
import fi.metatavu.ptv.client.model.V4VmOpenApiPhone;
import fi.metatavu.ptv.client.model.V4VmOpenApiPhoneSimple;
import fi.metatavu.ptv.client.model.V4VmOpenApiPhoneWithType;
import fi.metatavu.ptv.client.model.V8VmOpenApiElectronicChannelInBase;
import fi.metatavu.ptv.client.model.V6VmOpenApiServiceOrganization;
import fi.metatavu.ptv.client.model.V7VmOpenApiAddressContact;
import fi.metatavu.ptv.client.model.V7VmOpenApiAddressContactIn;
import fi.metatavu.ptv.client.model.V8VmOpenApiWebPageChannelInBase;
import fi.metatavu.ptv.client.model.V8VmOpenApiAddressDelivery;
import fi.metatavu.ptv.client.model.V8VmOpenApiAddressDeliveryIn;
import fi.metatavu.ptv.client.model.V8VmOpenApiAddressWithMoving;
import fi.metatavu.ptv.client.model.V7VmOpenApiAddressWithMovingIn;
import fi.metatavu.ptv.client.model.V8VmOpenApiElectronicChannel;
import fi.metatavu.ptv.client.model.V7VmOpenApiFintoItemWithDescription;
import fi.metatavu.ptv.client.model.V8VmOpenApiPhoneChannel;
import fi.metatavu.ptv.client.model.V8VmOpenApiPhoneChannelInBase;
import fi.metatavu.ptv.client.model.V8VmOpenApiPrintableFormChannel;
import fi.metatavu.ptv.client.model.V8VmOpenApiPrintableFormChannelInBase;
import fi.metatavu.ptv.client.model.V8VmOpenApiService;
import fi.metatavu.ptv.client.model.V8VmOpenApiServiceInBase;
import fi.metatavu.ptv.client.model.V8VmOpenApiServiceLocationChannel;
import fi.metatavu.ptv.client.model.V8VmOpenApiServiceLocationChannelInBase;
import fi.metatavu.ptv.client.model.V8VmOpenApiServiceServiceChannel;
import fi.metatavu.ptv.client.model.V8VmOpenApiServiceServiceChannelInBase;
import fi.metatavu.ptv.client.model.V8VmOpenApiWebPageChannel;
import fi.metatavu.ptv.client.model.VmOpenApiAddressPostOfficeBox;
import fi.metatavu.ptv.client.model.VmOpenApiAddressPostOfficeBoxIn;
import fi.metatavu.ptv.client.model.VmOpenApiAddressStreet;
import fi.metatavu.ptv.client.model.VmOpenApiAddressStreetIn;
import fi.metatavu.ptv.client.model.VmOpenApiAddressStreetWithCoordinates;
import fi.metatavu.ptv.client.model.VmOpenApiAddressStreetWithCoordinatesIn;
import fi.metatavu.ptv.client.model.VmOpenApiAddressStreetWithOrder;
import fi.metatavu.ptv.client.model.VmOpenApiArea;
import fi.metatavu.ptv.client.model.VmOpenApiAreaIn;
import fi.metatavu.ptv.client.model.VmOpenApiAttachment;
import fi.metatavu.ptv.client.model.VmOpenApiAttachmentWithType;
import fi.metatavu.ptv.client.model.V8VmOpenApiContactDetails;
import fi.metatavu.ptv.client.model.V8VmOpenApiContactDetailsInBase;
import fi.metatavu.ptv.client.model.VmOpenApiItem;
import fi.metatavu.ptv.client.model.VmOpenApiLanguageItem;
import fi.metatavu.ptv.client.model.VmOpenApiLocalizedListItem;
import fi.metatavu.ptv.client.model.VmOpenApiMunicipality;
import fi.metatavu.ptv.client.model.VmOpenApiServiceProducerIn;
import fi.metatavu.ptv.client.model.VmOpenApiWebPageWithOrderNumber;

/**
 * Translator for translating resources from PTV out format into PTV in format
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class PtvOutPtvInTranslator extends AbstractTranslator {
  
  private static final String UNKNOWN_SUBTYPE = "Unknown subtype %s";

  @Inject
  private Logger logger;

  /**
   * Translates PTV out service location channel into PTV in service location channel
   * 
   * @param ptvResource PTV out service location channel
   * @return PTV in service location channel
   */
  public V8VmOpenApiServiceLocationChannelInBase translateServiceLocationChannel(V8VmOpenApiServiceLocationChannel ptvResource) {
    V8VmOpenApiServiceLocationChannelInBase result = new V8VmOpenApiServiceLocationChannelInBase();
    result.setAddresses(translateAddressesWithMoving(ptvResource.getAddresses()));
    result.setAreas(translateAreas(ptvResource.getAreas()));
    result.setAreaType(ptvResource.getAreaType());
    result.setDeleteAllEmails(true);
    result.setDeleteAllFaxNumbers(true);
    result.setDeleteAllServiceHours(true);
    result.setDeleteAllWebPages(true);
    result.setIsVisibleForAll(true);
    result.setDeleteAllPhoneNumbers(true);
    result.setEmails(ptvResource.getEmails());
    result.setFaxNumbers(translateFaxNumbers(ptvResource.getPhoneNumbers()));
    result.setLanguages(ptvResource.getLanguages());
    result.setOrganizationId(ptvResource.getOrganizationId().toString());
    result.setPhoneNumbers(translatePhoneNumbers(ptvResource.getPhoneNumbers()));
    result.setPublishingStatus(ptvResource.getPublishingStatus());
    result.setServiceChannelDescriptions(ptvResource.getServiceChannelDescriptions());
    result.setServiceChannelNames(translateLocalizedListItemsToLanguageItems(ptvResource.getServiceChannelNames()));
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
  public V8VmOpenApiElectronicChannelInBase translateElectronicChannel(V8VmOpenApiElectronicChannel ptvResource) {
    V8VmOpenApiElectronicChannelInBase result = new V8VmOpenApiElectronicChannelInBase();
    
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
    result.setWebPage(translateWebPagesToLanguageItems(ptvResource.getWebPages()));
    
    return result;
  }

  /**
   * Translates PTV out phone service channel into PTV in phone service channel
   * 
   * @param ptvResource PTV out phone service channel
   * @return PTV in phone service channel
   */
  public V8VmOpenApiPhoneChannelInBase translatePhoneChannel(V8VmOpenApiPhoneChannel ptvResource) {
    if (ptvResource == null) {
      return null;
    }
    
    V8VmOpenApiPhoneChannelInBase result = new V8VmOpenApiPhoneChannelInBase();
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
    
    return result;
  }
  
  /**
   * Translates PTV out printable form service channel into PTV in printable form service channel
   * 
   * @param ptvResource PTV out service channel
   * @return PTV in service channel
   */
  public V8VmOpenApiPrintableFormChannelInBase translatePrintableFormChannel(V8VmOpenApiPrintableFormChannel ptvResource) {
    if (ptvResource == null) {
      return null;
    }
    
    V8VmOpenApiPrintableFormChannelInBase result = new V8VmOpenApiPrintableFormChannelInBase();
    result.setAreas(translateAreas(ptvResource.getAreas()));
    result.setAreaType(ptvResource.getAreaType());
    result.setAttachments(translateAttachments(ptvResource.getAttachments()));
    result.setChannelUrls(ptvResource.getChannelUrls());
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
    result.setDeleteAllAttachments(true);
    result.setDeleteAllChannelUrls(true);
    result.setDeleteAllFormIdentifiers(true);
    result.setDeleteAllSupportEmails(true);
    result.setDeleteAllSupportPhones(true);
    result.setDeleteAllDeliveryAddresses(true);

    return result;
  }

  public V8VmOpenApiWebPageChannelInBase translateWebPageChannel(V8VmOpenApiWebPageChannel ptvResource) {
    V8VmOpenApiWebPageChannelInBase result = new V8VmOpenApiWebPageChannelInBase();
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
    result.setWebPage(translateWebPagesToLanguageItems(ptvResource.getWebPages()));
    
    return result;
  }

  /**
   * Translates PTV out service into PTV in service
   * 
   * @param service PTV out service
   * @return PTV in service
   */
  public V8VmOpenApiServiceInBase translateService(V8VmOpenApiService service) {
    if (service == null) {
      return null;
    }
    
    String mainResponsibleOrganization = null;
    List<UUID> otherResponsibleOrganizations = new ArrayList<>();
    List<VmOpenApiServiceProducerIn> serviceProducers = new ArrayList<>();
    int serviceProducerOrderNumber = 0;
    
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
          VmOpenApiServiceProducerIn apiServiceProducer = new VmOpenApiServiceProducerIn();
          apiServiceProducer.setAdditionalInformation(organization.getAdditionalInformation());
          apiServiceProducer.setOrderNumber(serviceProducerOrderNumber);
          apiServiceProducer.setOrganizations(Arrays.asList(organizationUuid));
          apiServiceProducer.setProvisionType(organization.getProvisionType());
          serviceProducers.add(apiServiceProducer); 
          serviceProducerOrderNumber++;
        }
      }
    }
    
    V8VmOpenApiServiceInBase result = new V8VmOpenApiServiceInBase();
    
    result.setAreas(translateAreas(service.getAreas()));
    result.setAreaType(service.getAreaType());
    result.setFundingType(service.getFundingType());
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
    result.setGeneralDescriptionId(translateUuid(service.getGeneralDescriptionId()));
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
  public V8VmOpenApiServiceServiceChannelInBase translateServiceServiceChannel(V8VmOpenApiServiceServiceChannel serviceChannel) {
    if (serviceChannel == null || serviceChannel.getServiceChannel() == null) {
      return null;
    }
    
    V8VmOpenApiServiceServiceChannelInBase result = new V8VmOpenApiServiceServiceChannelInBase();
    result.setContactDetails(translateContractDetails(serviceChannel.getContactDetails()));
    result.setDescription(serviceChannel.getDescription());
    result.setServiceChannelId(serviceChannel.getServiceChannel().getId().toString());
    result.setServiceChargeType(serviceChannel.getServiceChargeType());
    result.setServiceHours(serviceChannel.getServiceHours());
    result.setDeleteAllDescriptions(false);
    result.setDeleteAllServiceHours(false);
    result.setDeleteServiceChargeType(false);
    
    return result;
  }

  /**
   * Translates PTV web pages into PTV language items
   * 
   * @param webPages PTV web pages
   * @return PTV language items
   */
  private List<VmOpenApiLanguageItem> translateWebPagesToLanguageItems(List<VmOpenApiWebPageWithOrderNumber> webPages) {
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
  private VmOpenApiLanguageItem translateWebPageToLanguageItem(VmOpenApiWebPageWithOrderNumber webPage) {
    if (webPage == null) {
      return null;
    }
   
    VmOpenApiLanguageItem result = new VmOpenApiLanguageItem();
    result.setLanguage(webPage.getLanguage());
    result.setValue(webPage.getValue());
    
    return result; 
  }

  private V8VmOpenApiContactDetailsInBase translateContractDetails(V8VmOpenApiContactDetails contactDetails) {
    if (contactDetails == null) {
      return null;
    }
    
    V8VmOpenApiContactDetailsInBase result = new V8VmOpenApiContactDetailsInBase();
    result.setAddresses(translateAddressContacts(contactDetails.getAddresses()));
    result.setEmails(contactDetails.getEmails());
    result.setPhoneNumbers(contactDetails.getPhoneNumbers());
    result.setWebPages(contactDetails.getWebPages());
    result.setDeleteAllAddresses(true);
    result.setDeleteAllEmails(true);
    result.setDeleteAllPhones(true);
    result.setDeleteAllWebPages(true);
    
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
    result.setPostOfficeBoxAddress(translateAddressPostOfficeBox(addressContact.getPostOfficeBoxAddress()));
    result.setStreetAddress(translateAddressStreetWithCoordinates(addressContact.getStreetAddress()));
    result.setSubType(addressContact.getSubType());
    result.setType(addressContact.getType());
    
    return result;
  }

  private String translateUuid(UUID uuid) {
    if (uuid == null) {
      return null;
    }
    
    return uuid.toString();
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

  private List<V7VmOpenApiAddressWithMovingIn> translateAddressesWithMoving(List<V8VmOpenApiAddressWithMoving> addresses) {
    if (addresses == null || addresses.isEmpty()) {
      return Collections.emptyList();
    }
    
    List<V7VmOpenApiAddressWithMovingIn> result = new ArrayList<>();
    
    for (V8VmOpenApiAddressWithMoving address : addresses) {
      result.add(translateAddressWithMoving(address));
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
    result.setSubType(address.getSubType());
    result.setPostOfficeBoxAddress(null);
    result.setStreetAddress(null);
    result.setFormReceiver(address.getReceiver());
    
    PtvAddressSubtype subtype = getAddressSubtype(result.getSubType());
    switch (subtype) {
      case POST_OFFICE_BOX:
        result.setPostOfficeBoxAddress(translateAddressPostOfficeBox(address.getPostOfficeBoxAddress()));
      break;
      case NO_ADDRESS:
      break;
      case SINGLE:
      case STREET:
        result.setStreetAddress(translateAddressStreet(address.getStreetAddress()));
      break;
    
      default:
        logger.log(Level.SEVERE, () -> String.format(UNKNOWN_SUBTYPE, result.getSubType()));
      break;
    }
    
    return result;
  }

  private V7VmOpenApiAddressWithMovingIn translateAddressWithMoving(V8VmOpenApiAddressWithMoving address) {
    if (address == null) {
      return null;
    }
    
    V7VmOpenApiAddressWithMovingIn result = new V7VmOpenApiAddressWithMovingIn();
    
    result.setCountry(address.getCountry());
    result.setType(address.getType());
    result.setSubType(address.getSubType());
    
    if ("Visiting".equals(result.getType())) {
      result.setType("Location");
      result.setSubType(PtvAddressSubtype.SINGLE.getPtvValue());
    }
    
    result.setLocationAbroad(null);
    result.setMultipointLocation(null);
    result.setPostOfficeBoxAddress(null);
    result.setStreetAddress(null);
    
    PtvAddressSubtype subtype = getAddressSubtype(result.getSubType());
    switch (subtype) {
      case ABROAD:
        result.setLocationAbroad(address.getLocationAbroad());
      break;
      case MULTIPOINT:
        result.setMultipointLocation(translateAddressesStreetWithCoordinates(address.getMultipointLocation()));
      break;
      case POST_OFFICE_BOX:
        result.setPostOfficeBoxAddress(translateAddressPostOfficeBox(address.getPostOfficeBoxAddress()));
      break;
      case NO_ADDRESS:
      break;
      case SINGLE:
      case STREET:
        result.setStreetAddress(translateAddressStreetWithCoordinates(address.getStreetAddress()));
      break;
      default:
        logger.log(Level.SEVERE, () -> String.format(UNKNOWN_SUBTYPE, result.getSubType()));
      break;
    }
    
    return result;
  }

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
  
  private VmOpenApiAddressStreetWithCoordinatesIn translateAddressStreetWithCoordinates(VmOpenApiAddressStreetWithOrder streetAddress) {
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

  private VmOpenApiAddressPostOfficeBoxIn translateAddressPostOfficeBox(VmOpenApiAddressPostOfficeBox postOfficeBoxAddress) {
    if (postOfficeBoxAddress == null) {
      return null;
    }
    
    VmOpenApiAddressPostOfficeBoxIn result = new VmOpenApiAddressPostOfficeBoxIn();
    result.setAdditionalInformation(postOfficeBoxAddress.getAdditionalInformation());
    result.setMunicipality(postOfficeBoxAddress.getMunicipality() != null ? postOfficeBoxAddress.getMunicipality().getCode() : null);
    result.setPostalCode(postOfficeBoxAddress.getPostalCode());
    result.setPostOfficeBox(postOfficeBoxAddress.getPostOfficeBox());
    
    return result;
  }

  private List<VmOpenApiAddressStreetWithCoordinatesIn> translateAddressesStreetWithCoordinates(List<VmOpenApiAddressStreetWithOrder> addresses) {
    if (addresses == null) {
      return Collections.emptyList();
    }
    
    return addresses.stream()
      .map(this::translateAddressStreetWithCoordinates)
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
  }


  @SuppressWarnings("unused")
  private List<VmOpenApiLanguageItem> cleanLanguageItems(List<VmOpenApiLanguageItem> items) {
    if (items == null || items.isEmpty()) {
      return Collections.emptyList();
    }

    List<VmOpenApiLanguageItem> result = new ArrayList<>(items.size());
    
    for (VmOpenApiLanguageItem item : items) {
      if (StringUtils.isNotEmpty(item.getValue())) {
        result.add(item);
      }
    }
    
    return result;
  }

  @SuppressWarnings("unused")
  private String translateMunicipality(VmOpenApiMunicipality municipality) {
    if (municipality == null) {
      return null;
    }
    
    return municipality.getCode();
  }

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
  
  private List<VmOpenApiAttachment> translateAttachments(List<VmOpenApiAttachmentWithType> attachments) {
    if (attachments == null) {
      return Collections.emptyList();
    }
    
    return attachments.stream()
      .map(this::translateAttachment)
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
  }
  
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

  private VmOpenApiLanguageItem translateLocalizedListItemsToLanguageItems(VmOpenApiLocalizedListItem listItem) {
    if (listItem == null) {
      return null;
    }
    
    VmOpenApiLanguageItem result = new VmOpenApiLanguageItem();
    result.setLanguage(listItem.getLanguage());
    result.setValue(listItem.getValue());

    return result;
  }
  
}
