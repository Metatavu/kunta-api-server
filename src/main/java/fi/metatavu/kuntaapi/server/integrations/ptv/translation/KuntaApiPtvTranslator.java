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

import fi.metatavu.kuntaapi.server.rest.model.Address;
import fi.metatavu.kuntaapi.server.rest.model.Area;
import fi.metatavu.kuntaapi.server.rest.model.DailyOpeningTime;
import fi.metatavu.kuntaapi.server.rest.model.Email;
import fi.metatavu.kuntaapi.server.rest.model.Law;
import fi.metatavu.kuntaapi.server.rest.model.LocalizedValue;
import fi.metatavu.kuntaapi.server.rest.model.Municipality;
import fi.metatavu.kuntaapi.server.rest.model.OntologyItem;
import fi.metatavu.kuntaapi.server.rest.model.Phone;
import fi.metatavu.kuntaapi.server.rest.model.ServiceChannelAttachment;
import fi.metatavu.kuntaapi.server.rest.model.ServiceHour;
import fi.metatavu.kuntaapi.server.rest.model.ServiceOrganization;
import fi.metatavu.kuntaapi.server.rest.model.ServiceVoucher;
import fi.metatavu.kuntaapi.server.rest.model.WebPage;
import fi.metatavu.ptv.client.model.V8VmOpenApiDailyOpeningTime;
import fi.metatavu.ptv.client.model.V4VmOpenApiLaw;
import fi.metatavu.ptv.client.model.V4VmOpenApiPhone;
import fi.metatavu.ptv.client.model.V4VmOpenApiPhoneSimple;
import fi.metatavu.ptv.client.model.V4VmOpenApiPhoneWithType;
import fi.metatavu.ptv.client.model.V8VmOpenApiServiceHour;
import fi.metatavu.ptv.client.model.V9VmOpenApiAddressLocationIn;
import fi.metatavu.ptv.client.model.V9VmOpenApiServiceProducerIn;
import fi.metatavu.ptv.client.model.V9VmOpenApiServiceVoucher;
import fi.metatavu.ptv.client.model.V9VmOpenApiWebPage;
import fi.metatavu.ptv.client.model.V4VmOpenApiWebPage;
import fi.metatavu.ptv.client.model.V8VmOpenApiAddressDeliveryIn;
import fi.metatavu.ptv.client.model.VmOpenApiAddressPostOfficeBoxIn;
import fi.metatavu.ptv.client.model.VmOpenApiAddressStreetIn;
import fi.metatavu.ptv.client.model.VmOpenApiAddressStreetWithCoordinatesIn;
import fi.metatavu.ptv.client.model.VmOpenApiAreaIn;
import fi.metatavu.ptv.client.model.VmOpenApiAttachment;
import fi.metatavu.ptv.client.model.VmOpenApiLanguageItem;
import fi.metatavu.ptv.client.model.VmOpenApiLocalizedListItem;
import fi.metatavu.kuntaapi.server.id.IdController;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.integrations.KuntaApiIdFactory;
import fi.metatavu.kuntaapi.server.integrations.ptv.PtvConsts;

/**
 * Translator for translating resources from Kunta API format into PTV formats
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class KuntaApiPtvTranslator extends AbstractTranslator {

  private static final String UNKNOWN_SUBTYPE = "Unknown subtype %s";

  @Inject
  private Logger logger;

  @Inject
  private KuntaApiIdFactory kuntaApiIdFactory;
  
  @Inject
  private IdController idController;

  /**
   * Translates list of localized values into list of VmOpenApiLanguageItems
   * 
   * @param localizedValues list of LocalizedValue
   * @return list of VmOpenApiLanguageItems
   */
  public List<VmOpenApiLanguageItem> translateLocalizedValuesIntoLanguageItems(List<LocalizedValue> localizedValues) {
    if (localizedValues == null || localizedValues.isEmpty()) {
      return Collections.emptyList();
    }
    
    List<VmOpenApiLanguageItem> result = new ArrayList<>(localizedValues.size());
    
    for (LocalizedValue localizedValue : localizedValues) {
      VmOpenApiLanguageItem languageItem = translateLocalizedValuesIntoLanguageItem(localizedValue);
      if (languageItem != null) {
        result.add(languageItem);
      }
    }

    return result;
  }

  /**
   * Translates list of localized values into list of VmOpenApiLanguageItems
   * 
   * @param localizedValues list of LocalizedValue
   * @return list of VmOpenApiLanguageItems
   */
  public List<VmOpenApiLocalizedListItem> translateLocalizedValuesIntoLocalizedListItems(List<LocalizedValue> localizedValues) {
    if (localizedValues == null || localizedValues.isEmpty()) {
      return Collections.emptyList();
    }
    
    List<VmOpenApiLocalizedListItem> result = new ArrayList<>(localizedValues.size());
    
    for (LocalizedValue localizedValue : localizedValues) {
      VmOpenApiLocalizedListItem localizedListItem = translateLocalizedValuesIntoLocalizedListItem(localizedValue);
      if (localizedListItem != null) {
        result.add(localizedListItem);
      }
    }

    return result;
  }

  /**
   * Translates Kunta API phone numbers into PTV phone numbers
   * 
   * @param phoneNumbers Kunta API phone numbers
   * @return PTV phone numbers
   */
  public List<V4VmOpenApiPhone> translatePhoneNumbers(List<Phone> phoneNumbers) {
    if (phoneNumbers == null || phoneNumbers.isEmpty()) {
      return Collections.emptyList();
    }
    
    List<V4VmOpenApiPhone> result = new ArrayList<>(phoneNumbers.size());
    
    for (Phone phoneNumber : phoneNumbers) {
      V4VmOpenApiPhone ptvPhoneNumber = translatePhoneNumber(phoneNumber);
      if (ptvPhoneNumber != null) {
        result.add(ptvPhoneNumber);
      }
    }
    
    return result;
  }
  
  /**
   * Translates Kunta API phone numbers into PTV phone numbers
   * 
   * @param phoneNumbers Kunta API phone numbers
   * @return PTV phone numbers
   */
  public List<V4VmOpenApiPhoneWithType> translatePhoneNumbersWithTypes(List<Phone> phoneNumbers) {
    if (phoneNumbers == null || phoneNumbers.isEmpty()) {
      return Collections.emptyList();
    }
    
    return phoneNumbers.stream()
      .map(this::translatePhoneNumberWithType)
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
  }


  /**
   * Translates Kunta API fax numbers into PTV fax numbers
   * 
   * @param phoneNumbers Kunta API fax numbers
   * @return PTV fax numbers
   */
  public List<V4VmOpenApiPhoneSimple> translateFaxNumbers(List<Phone> phoneNumbers) {
    if (phoneNumbers == null || phoneNumbers.isEmpty()) {
      return Collections.emptyList();
    }
    
    return phoneNumbers
      .stream()
      .map(this::translateFaxNumber)
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
  }
  
  /**
   * Translates Kunta API addresses into PTV in moving in addresses
   * 
   * @param addresses Kunta API addresses
   * @return list of PTV moving in addresses
   */
  public List<V9VmOpenApiAddressLocationIn> translateAddressesLocationIn(List<Address> addresses) {
    if (addresses == null || addresses.isEmpty()) {
      return Collections.emptyList();
    }
    
    List<V9VmOpenApiAddressLocationIn> result = new ArrayList<>(addresses.size());
    
    for (Address address : addresses) {
      V9VmOpenApiAddressLocationIn ptvAddress = translateAddressLocationIn(address);
      if (ptvAddress != null) {
        result.add(ptvAddress);
      }
    }
    
    return result;
  }

  /**
   * Translates Kunta API addresses into PTV address streets with coordinates in
   * 
   * @param addresses Kunta API addresses
   * @return list of PTV address streets with coordinates in
   */
  public List<VmOpenApiAddressStreetWithCoordinatesIn> translateAddressesWithCoordinatesIn(List<Address> addresses) {
    if (addresses == null || addresses.isEmpty()) {
      return Collections.emptyList();
    }
    
    List<VmOpenApiAddressStreetWithCoordinatesIn> result = new ArrayList<>(addresses.size());
    
    for (Address address : addresses) {
      VmOpenApiAddressStreetWithCoordinatesIn ptvAddress = translateAddressWithCoordinatesIn(address);
      if (ptvAddress != null) {
        result.add(ptvAddress);
      }
    }
    
    return result;
  }

  /**
   * Translates Kunta API Service hours into PTV Service Hours
   * 
   * @param serviceHours Kunta API Service hours
   * @return PTV Service Hours
   */
  public List<V8VmOpenApiServiceHour> translateServiceHours(List<ServiceHour> serviceHours) {
    if (serviceHours == null || serviceHours.isEmpty()) {
      return Collections.emptyList();
    }
    
    List<V8VmOpenApiServiceHour> result = new ArrayList<>(serviceHours.size());
    
    for (ServiceHour serviceHour : serviceHours) {
      V8VmOpenApiServiceHour ptvServiceHour = translateServiceHour(serviceHour);
      if (ptvServiceHour != null) {
        result.add(ptvServiceHour);
      }
    }
    
    return result;
  }

  /**
   * Translates list of Kunta API emails into list of PTV language items
   * 
   * @param emails list of Kunta API emails
   * @return list of PTV language items
   */
  public List<VmOpenApiLanguageItem> translateEmailsIntoLanguageItems(List<Email> emails) {
    if (emails == null || emails.isEmpty()) {
      return Collections.emptyList();
    }
    
    List<VmOpenApiLanguageItem> result = new ArrayList<>(emails.size());
    
    for (Email email : emails) {
      VmOpenApiLanguageItem ptvLanguageItem = translateEmailIntoLanguageItem(email);
      if (ptvLanguageItem != null) {
        result.add(ptvLanguageItem);
      }
    }
    
    return result;
  }

  /**
   * Translates list of Kunta API web pages into list of PTV web pages
   * 
   * @param webPages list of Kunta API web pages 
   * @return list of PTV web pages
   */
  public List<V4VmOpenApiWebPage> translateWebPages4(List<WebPage> webPages) {
    if (webPages == null || webPages.isEmpty()) {
      return Collections.emptyList();
    }
    
    List<V4VmOpenApiWebPage> result = new ArrayList<>(webPages.size());
    
    for (WebPage webPage : webPages) {
      V4VmOpenApiWebPage ptvWebPage = translateWebPage4(webPage);
      if (ptvWebPage != null) {
        result.add(ptvWebPage);
      }
    }
    
    return result;
  }

  /**
   * Translates list of Kunta API web pages into list of PTV web pages
   * 
   * @param webPages list of Kunta API web pages 
   * @return list of PTV web pages
   */
  public List<V9VmOpenApiWebPage> translateWebPages(List<WebPage> webPages) {
    if (webPages == null || webPages.isEmpty()) {
      return Collections.emptyList();
    }
    
    List<V9VmOpenApiWebPage> result = new ArrayList<>(webPages.size());
    
    for (WebPage webPage : webPages) {
      V9VmOpenApiWebPage ptvWebPage = translateWebPage(webPage);
      if (ptvWebPage != null) {
        result.add(ptvWebPage);
      }
    }
    
    return result;
  }

  /**
   * Translates KuntaAPI areas into PTV in Areas
   * 
   * @param List of Kunta API areas
   * @return List of PTV in areas
   */
  public List<VmOpenApiAreaIn> translateAreas(List<Area> areas) {
    if (areas == null || areas.isEmpty()) {
      return Collections.emptyList();
    }

    return areas
      .stream()
      .map(this::translateArea)
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
  }

  /**
   * Translates Kunta API WebPages into PTV language items
   * 
   * @param webPages Kunta API WebPages
   * @return PTV language items
   */
  public List<VmOpenApiLanguageItem> translateWebPagesIntoLanguageItems(List<WebPage> webPages) {
    if (webPages == null) {
      return Collections.emptyList();
    }
    
    return webPages.stream()
      .filter(webPage -> webPage != null && StringUtils.isNotBlank(webPage.getUrl()))
      .map(webPage -> {
        VmOpenApiLanguageItem result = new VmOpenApiLanguageItem();
        result.setLanguage(webPage.getLanguage());
        result.setValue(webPage.getUrl());
        return result;
      })
      .collect(Collectors.toList());
  }

  /**
   * Translates ontolygy items into uris
   * 
   * @param items items
   * @return uris
   */
  public List<String> translateOntologyItems(List<OntologyItem> fintoItems) {
    if (fintoItems == null) {
      return Collections.emptyList();
    }

    return fintoItems.stream()
      .map(OntologyItem::getUri)
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
  }

  /**
   * Translates Kunta API laws into PTV laws
   * 
   * @param laws laws
   * @return
   */
  public List<V4VmOpenApiLaw> translateLaws(List<Law> laws) {
    if (laws == null) {
      return Collections.emptyList();
    }

    return laws.stream()
      .map(this::translateLaw)
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
  }

  /**
   * Translates Kunta API vouchers into PTV vouchers
   * 
   * @param vouchers Kunta API vouchers
   * @return PTV vouchers
   */
  public List<V9VmOpenApiServiceVoucher> translateVouchers(List<ServiceVoucher> vouchers) {
    if (vouchers == null) {
      return Collections.emptyList();
    }
    
    List<V9VmOpenApiServiceVoucher> result = new ArrayList<>(vouchers.size());
    
    for (int i = 0; i < vouchers.size(); i++) {
      V9VmOpenApiServiceVoucher voucher = translateVoucher(vouchers.get(i));
      if (voucher != null) {
        result.add(voucher);
      }
    }
    
    return result;
  }
  
  /**
   * Translates Kunta API service organizations into PTV Service Producers
   * 
   * @param organizations Kunta API service organizations
   * @return PTV Service Producers
   */
  public List<V9VmOpenApiServiceProducerIn> translateServiceProducers(List<ServiceOrganization> organizations) {
    if (organizations == null) {
      return Collections.emptyList();
    }
    
    List<ServiceOrganization> serviceProducers = organizations.stream()
      .filter(serviceOrganization -> "Producer".equals(serviceOrganization.getRoleType()))
      .collect(Collectors.toList());
    
    List<V9VmOpenApiServiceProducerIn> result = new ArrayList<>(serviceProducers.size());
    
    for (int i = 0; i < serviceProducers.size(); i++) {
      V9VmOpenApiServiceProducerIn producer = translateServiceProducer(serviceProducers.get(i));
      if (producer != null) {
        result.add(producer);
      }
    }
    
    return result;
  }

  /**
   * Translates list of Kunta API service channel attachments into Ptv In attachments
   * 
   * @param attachments Kunta API service channel attachments
   * @return Ptv In attachments
   */
  public List<VmOpenApiAttachment> translateAttachments(List<ServiceChannelAttachment> attachments) {
    if (attachments == null) {
      return Collections.emptyList();
    }
    
    return attachments.stream()
      .map(this::translateAttachment)
      .filter(Objects::nonNull)
      .collect(Collectors.toList());
  }

  /**
   * Translates Kunta API delivery address into PTV in addresses
   * 
   * @param formReceiver 
   * @param deliveryAddress Kunta API delivery address
   * @return PTV in addresses
   */
  public List<V8VmOpenApiAddressDeliveryIn> translateDeliveryAddresses(List<LocalizedValue> formReceiver, Address deliveryAddress) {
    if (deliveryAddress == null && (formReceiver == null || formReceiver.isEmpty())) {
      return Collections.emptyList();
    }
    
    V8VmOpenApiAddressDeliveryIn result = new V8VmOpenApiAddressDeliveryIn(); 
    
    if (deliveryAddress != null) {
      PtvAddressSubtype subtype = getAddressSubtype(deliveryAddress.getSubtype());
      switch (subtype) {
        case POST_OFFICE_BOX:
          result.setPostOfficeBoxAddress(translatePostOfficeBoxAddress(deliveryAddress));
        break;
        case NO_ADDRESS:
          result.setDeliveryAddressInText(translateLocalizedValuesIntoLanguageItems(deliveryAddress.getAdditionalInformations()));
        break;
        case SINGLE:
        case STREET:
          result.setStreetAddress(translateStreetAddress(deliveryAddress));
        break;
        default:
          logger.log(Level.SEVERE, () -> String.format(UNKNOWN_SUBTYPE, deliveryAddress.getSubtype()));
        break;
      }

      result.setSubType(deliveryAddress.getSubtype());
    }

    result.setFormReceiver(translateLocalizedValuesIntoLanguageItems(formReceiver));
    
    return Arrays.asList(result);
  }
  
  private VmOpenApiAttachment translateAttachment(ServiceChannelAttachment attachment) {
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

  private V9VmOpenApiServiceProducerIn translateServiceProducer(ServiceOrganization serviceOrganization) {
    if (serviceOrganization == null || !"Producer".equals(serviceOrganization.getRoleType())) {
      return null;
    }
    
    OrganizationId kuntaApiOrganizationId = kuntaApiIdFactory.createOrganizationId(serviceOrganization.getOrganizationId());
    OrganizationId ptvOrganizationId = idController.translateOrganizationId(kuntaApiOrganizationId, PtvConsts.IDENTIFIER_NAME);
    if (ptvOrganizationId == null) {
      logger.log(Level.SEVERE, () -> String.format("Failed to translate organization id %s into Ptv Id", kuntaApiOrganizationId));
      return null;
    }
    
    V9VmOpenApiServiceProducerIn result = new V9VmOpenApiServiceProducerIn();
    result.setAdditionalInformation(translateLocalizedValuesIntoLanguageItems(serviceOrganization.getAdditionalInformation()));
    result.setOrganizations(Arrays.asList(UUID.fromString(ptvOrganizationId.getId())));
    result.setProvisionType(serviceOrganization.getProvisionType());
    
    return result;
  }
  
  private V4VmOpenApiLaw translateLaw(Law law) {
    if (law == null) {
      return null;
    }
    
    V4VmOpenApiLaw result = new V4VmOpenApiLaw();
    result.setNames(translateLocalizedValuesIntoLanguageItems(law.getNames()));
    result.setWebPages(translateWebPages4(law.getWebPages()));
    
    return result;
  }
  
  private V9VmOpenApiServiceVoucher translateVoucher(ServiceVoucher voucher) {
    if (voucher == null) {
      return null;
    }
    
    V9VmOpenApiServiceVoucher result = new V9VmOpenApiServiceVoucher();
    result.setAdditionalInformation(voucher.getAdditionalInformation());
    result.setLanguage(voucher.getLanguage());
    result.setUrl(voucher.getUrl());
    result.setValue(voucher.getValue());
    
    return result;
  }
  
  private VmOpenApiAreaIn translateArea(Area area) {
    if (area == null) {
      return null;
    }
    
    List<String> areaCodes = null;
    
    VmOpenApiAreaIn result = new VmOpenApiAreaIn();
    if ("Municipality".equals(area.getType())) {
      if (area.getMunicipalities() != null) {
        areaCodes = area.getMunicipalities()
          .stream()
          .map(Municipality::getCode)
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

  private V4VmOpenApiWebPage translateWebPage4(WebPage webPage) {
    if (webPage == null || (StringUtils.isBlank(webPage.getUrl()) && StringUtils.isBlank(webPage.getValue()))) {
      return null;
    }

    
    V4VmOpenApiWebPage result = new V4VmOpenApiWebPage();
    result.setLanguage(webPage.getLanguage());
    result.setUrl(webPage.getUrl());
    result.setValue(webPage.getValue());
    
    return result;
  }

  private V9VmOpenApiWebPage translateWebPage(WebPage webPage) {
    if (webPage == null || (StringUtils.isBlank(webPage.getUrl()) && StringUtils.isBlank(webPage.getValue()))) {
      return null;
    }

    
    V9VmOpenApiWebPage result = new V9VmOpenApiWebPage();
    result.setLanguage(webPage.getLanguage());
    result.setUrl(webPage.getUrl());
    result.setValue(webPage.getValue());
    
    return result;
  }
  
  private V4VmOpenApiPhoneSimple translateFaxNumber(Phone phoneNumber) {
    if (phoneNumber == null || !"Fax".equals(phoneNumber.getType())) {
      return null;
    }
    
    V4VmOpenApiPhoneSimple result = new V4VmOpenApiPhoneSimple();
    result.setIsFinnishServiceNumber(phoneNumber.getIsFinnishServiceNumber());
    result.setLanguage(phoneNumber.getLanguage());
    result.setNumber(phoneNumber.getNumber());
    result.setPrefixNumber(phoneNumber.getPrefixNumber());
    
    return result;
  }
  

  private VmOpenApiLanguageItem translateEmailIntoLanguageItem(Email email) {
    if (email == null) {
      return null;
    }
    
    VmOpenApiLanguageItem result = new VmOpenApiLanguageItem();
    result.setLanguage(email.getLanguage());
    result.setValue(email.getValue());
    
    return result;
  }

  private V4VmOpenApiPhone translatePhoneNumber(Phone phoneNumber) {
    if (phoneNumber == null) {
      return null;
    }
    
    String type = phoneNumber.getType();
    if (type != null && !"Phone".equals(type)) {
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

  private V4VmOpenApiPhoneWithType translatePhoneNumberWithType(Phone phoneNumber) {
    if (phoneNumber == null) {
      return null;
    }
    
    V4VmOpenApiPhoneWithType result = new V4VmOpenApiPhoneWithType();
    result.setAdditionalInformation(phoneNumber.getAdditionalInformation());
    result.setChargeDescription(phoneNumber.getChargeDescription());
    result.setIsFinnishServiceNumber(phoneNumber.getIsFinnishServiceNumber());
    result.setLanguage(phoneNumber.getLanguage());
    result.setNumber(phoneNumber.getNumber());
    result.setPrefixNumber(phoneNumber.getPrefixNumber());
    result.setServiceChargeType(phoneNumber.getServiceChargeType());
    result.setType(phoneNumber.getType());
    
    return result;
  }

  private VmOpenApiLanguageItem translateLocalizedValuesIntoLanguageItem(LocalizedValue localizedValue) {
    if (localizedValue == null) {
      return null;
    }
    
    VmOpenApiLanguageItem result = new VmOpenApiLanguageItem();
    result.setLanguage(localizedValue.getLanguage());
    result.setValue(localizedValue.getValue());
    
    return result;
  }

  private VmOpenApiLocalizedListItem translateLocalizedValuesIntoLocalizedListItem(LocalizedValue localizedValue) {
    if (localizedValue == null) {
      return null;
    }
    
    if (StringUtils.isBlank(localizedValue.getValue())) {
      return null;
    }
    
    VmOpenApiLocalizedListItem result = new VmOpenApiLocalizedListItem();
    result.setLanguage(localizedValue.getLanguage());
    result.setValue(localizedValue.getValue());
    result.setType(localizedValue.getType());

    return result;
  }
  
  private V9VmOpenApiAddressLocationIn translateAddressLocationIn(Address address) {
    if (address == null) {
      return null;
    }
    
    V9VmOpenApiAddressLocationIn result = new V9VmOpenApiAddressLocationIn();
    result.setCountry(address.getCountry());
    result.setLocationAbroad(translateLocalizedValuesIntoLanguageItems(address.getLocationAbroad()));
    result.setPostOfficeBoxAddress(translatePostOfficeBoxAddress(address));
    result.setStreetAddress(translateAddressWithCoordinatesIn(address));
    result.setSubType(address.getSubtype());
    result.setType(address.getType());
    
    return result;
  }

  private VmOpenApiAddressStreetWithCoordinatesIn translateAddressWithCoordinatesIn(Address address) {
    PtvAddressSubtype addressSubtype = getAddressSubtype(address);
    
    if (addressSubtype == PtvAddressSubtype.SINGLE || addressSubtype == PtvAddressSubtype.STREET) {
      VmOpenApiAddressStreetWithCoordinatesIn result = new VmOpenApiAddressStreetWithCoordinatesIn();
      result.setAdditionalInformation(translateLocalizedValuesIntoLanguageItems(address.getAdditionalInformations()));
      result.setLatitude(address.getLatitude());
      result.setLongitude(address.getLongitude());
      result.setMunicipality(address.getMunicipality() != null ? address.getMunicipality().getCode() : null);
      result.setPostalCode(address.getPostalCode());
      result.setStreet(translateLocalizedValuesIntoLanguageItems(address.getStreetAddress()));
      result.setStreetNumber(address.getStreetNumber());
      return result;
    }
    
    return null;
  }

  private V8VmOpenApiServiceHour translateServiceHour(ServiceHour serviceHour) {
    if (serviceHour == null) {
      return null;
    }
    
    V8VmOpenApiServiceHour result = new V8VmOpenApiServiceHour();
    result.setAdditionalInformation(translateLocalizedValuesIntoLanguageItems(serviceHour.getAdditionalInformation()));
    result.setIsClosed(serviceHour.getIsClosed());
    result.setOpeningHour(translateDailyOpeningTimes(serviceHour.getOpeningHour()));
    result.setServiceHourType(serviceHour.getServiceHourType());
    result.setValidForNow(serviceHour.getValidForNow());
    result.setValidFrom(serviceHour.getValidFrom());
    result.setValidTo(serviceHour.getValidTo());
    
    return result;
  }

  private List<V8VmOpenApiDailyOpeningTime> translateDailyOpeningTimes(List<DailyOpeningTime> dailyOpeningTimes) {
    if (dailyOpeningTimes == null || dailyOpeningTimes.isEmpty()) {
      return Collections.emptyList();
    }
    
    List<V8VmOpenApiDailyOpeningTime> result = new ArrayList<>(dailyOpeningTimes.size());
    
    for (DailyOpeningTime dailyOpeningTime : dailyOpeningTimes) {
      V8VmOpenApiDailyOpeningTime ptvServiceHour = translateDailyOpeningTime(dailyOpeningTime);
      if (ptvServiceHour != null) {
        result.add(ptvServiceHour);
      }
    }
    
    return result;
  }

  private V8VmOpenApiDailyOpeningTime translateDailyOpeningTime(DailyOpeningTime dailyOpeningTime) {
    if (dailyOpeningTime == null) {
      return null;
    }
    
    V8VmOpenApiDailyOpeningTime result = new V8VmOpenApiDailyOpeningTime();
    result.setDayFrom(translateOpeningTimeDay(dailyOpeningTime.getDayFrom()));
    result.setDayTo(translateOpeningTimeDay(dailyOpeningTime.getDayTo()));
    result.setFrom(dailyOpeningTime.getFrom());
    result.setTo(dailyOpeningTime.getTo());
    
    return result;
  }

  private String translateOpeningTimeDay(Integer dayFrom) {
    if (dayFrom == null || dayFrom < 0 || dayFrom > WEEKDAY_INDICES.length - 1) {
      return null;
    }
    
    return WEEKDAY_INDICES[dayFrom];
  }

  private VmOpenApiAddressPostOfficeBoxIn translatePostOfficeBoxAddress(Address address) {
    if (getAddressSubtype(address) == PtvAddressSubtype.POST_OFFICE_BOX) {
      VmOpenApiAddressPostOfficeBoxIn result = new VmOpenApiAddressPostOfficeBoxIn();
      result.setAdditionalInformation(translateLocalizedValuesIntoLanguageItems(address.getAdditionalInformations()));
      result.setMunicipality(address.getMunicipality() != null ? address.getMunicipality().getCode() : null);
      result.setPostalCode(address.getPostalCode());
      result.setPostOfficeBox(translateLocalizedValuesIntoLanguageItems(address.getPostOfficeBox()));
      return result;
    }
    
    return null;
  }

  private VmOpenApiAddressStreetIn translateStreetAddress(Address address) {
    if (address == null) {
      return null;
    }
    
    VmOpenApiAddressStreetIn result = new VmOpenApiAddressStreetIn();
    result.setAdditionalInformation(translateLocalizedValuesIntoLanguageItems(address.getAdditionalInformations()));
    result.setMunicipality(address.getMunicipality() != null ? address.getMunicipality().getCode() : null);
    result.setPostalCode(address.getPostalCode());
    result.setStreet(translateLocalizedValuesIntoLanguageItems(address.getStreetAddress()));
    result.setStreetNumber(address.getStreetNumber());
    
    return result;
  }

}
