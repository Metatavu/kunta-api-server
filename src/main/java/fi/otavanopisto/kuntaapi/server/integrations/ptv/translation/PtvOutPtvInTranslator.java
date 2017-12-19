package fi.otavanopisto.kuntaapi.server.integrations.ptv.translation;

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
import fi.metatavu.ptv.client.model.V6VmOpenApiElectronicChannelInBase;
import fi.metatavu.ptv.client.model.V6VmOpenApiServiceOrganization;
import fi.metatavu.ptv.client.model.V7VmOpenApiAddressWithMoving;
import fi.metatavu.ptv.client.model.V7VmOpenApiAddressWithMovingIn;
import fi.metatavu.ptv.client.model.V7VmOpenApiElectronicChannel;
import fi.metatavu.ptv.client.model.V7VmOpenApiFintoItemWithDescription;
import fi.metatavu.ptv.client.model.V7VmOpenApiService;
import fi.metatavu.ptv.client.model.V7VmOpenApiServiceInBase;
import fi.metatavu.ptv.client.model.V7VmOpenApiServiceLocationChannel;
import fi.metatavu.ptv.client.model.V7VmOpenApiServiceLocationChannelInBase;
import fi.metatavu.ptv.client.model.VmOpenApiArea;
import fi.metatavu.ptv.client.model.VmOpenApiAreaIn;
import fi.metatavu.ptv.client.model.VmOpenApiAttachment;
import fi.metatavu.ptv.client.model.VmOpenApiAttachmentWithType;
import fi.metatavu.ptv.client.model.VmOpenApiItem;
import fi.metatavu.ptv.client.model.VmOpenApiLanguageItem;
import fi.metatavu.ptv.client.model.VmOpenApiLocalizedListItem;
import fi.metatavu.ptv.client.model.VmOpenApiMunicipality;
import fi.metatavu.ptv.client.model.VmOpenApiServiceProducerIn;

/**
 * Translator for translating resources from PTV out format into PTV in format
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class PtvOutPtvInTranslator extends AbstractTranslator {
  
  @Inject
  private Logger logger;

  /**
   * Translates PTV out service location channel into PTV in service location channel
   * 
   * @param ptvResource PTV out service location channel
   * @return PTV in service location channel
   */
  public V7VmOpenApiServiceLocationChannelInBase translateServiceLocationChannel(V7VmOpenApiServiceLocationChannel ptvResource) {
    V7VmOpenApiServiceLocationChannelInBase result = new V7VmOpenApiServiceLocationChannelInBase();
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
  public V6VmOpenApiElectronicChannelInBase translateElectronicChannel(V7VmOpenApiElectronicChannel ptvResource) {
    V6VmOpenApiElectronicChannelInBase result = new V6VmOpenApiElectronicChannelInBase();
    
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
    result.setUrls(ptvResource.getUrls());
    
    return result;
  }
  

  /**
   * Translates PTV out service into PTV in service
   * 
   * @param service PTV out service
   * @return PTV in service
   */
  public V7VmOpenApiServiceInBase translateService(V7VmOpenApiService service) {
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
    
    V7VmOpenApiServiceInBase result = new V7VmOpenApiServiceInBase();
    
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
    result.setStatutoryServiceGeneralDescriptionId(translateUuid(service.getStatutoryServiceGeneralDescriptionId()));
    result.setTargetGroups(translateFintoItems(service.getTargetGroups()));
    result.setType(service.getType());
    
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

  private List<V7VmOpenApiAddressWithMovingIn> translateAddressesWithMoving(List<V7VmOpenApiAddressWithMoving> addresses) {
    if (addresses == null || addresses.isEmpty()) {
      return Collections.emptyList();
    }
    
    List<V7VmOpenApiAddressWithMovingIn> result = new ArrayList<>();
    
    for (V7VmOpenApiAddressWithMoving address : addresses) {
      result.add(translateAddressWithMoving(address));
    }
    
    return result;
  }

  private V7VmOpenApiAddressWithMovingIn translateAddressWithMoving(V7VmOpenApiAddressWithMoving address) {
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
        result.setLocationAbroad(result.getLocationAbroad());
      break;
      case MULTIPOINT:
        result.setMultipointLocation(result.getMultipointLocation());
      break;
      case POST_OFFICE_BOX:
        result.setPostOfficeBoxAddress(result.getPostOfficeBoxAddress());
      break;
      case NO_ADDRESS:
      break;
      case SINGLE:
      case STREET:
        result.setStreetAddress(result.getStreetAddress());
      break;
      default:
        logger.log(Level.SEVERE, () -> String.format("Unknown subtype %s", result.getSubType()));
      break;
    }
    
    return result;
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
