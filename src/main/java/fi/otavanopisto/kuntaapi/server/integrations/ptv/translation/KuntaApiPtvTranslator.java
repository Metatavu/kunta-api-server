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
import fi.metatavu.ptv.client.model.V2VmOpenApiDailyOpeningTime;
import fi.metatavu.ptv.client.model.V4VmOpenApiLaw;
import fi.metatavu.ptv.client.model.V4VmOpenApiPhone;
import fi.metatavu.ptv.client.model.V4VmOpenApiPhoneSimple;
import fi.metatavu.ptv.client.model.V4VmOpenApiPhoneWithType;
import fi.metatavu.ptv.client.model.V4VmOpenApiServiceHour;
import fi.metatavu.ptv.client.model.V4VmOpenApiWebPage;
import fi.metatavu.ptv.client.model.V7VmOpenApiAddressDeliveryIn;
import fi.metatavu.ptv.client.model.V7VmOpenApiAddressWithMovingIn;
import fi.metatavu.ptv.client.model.VmOpenApiAddressPostOfficeBoxIn;
import fi.metatavu.ptv.client.model.VmOpenApiAddressStreetIn;
import fi.metatavu.ptv.client.model.VmOpenApiAddressStreetWithCoordinatesIn;
import fi.metatavu.ptv.client.model.VmOpenApiAreaIn;
import fi.metatavu.ptv.client.model.VmOpenApiAttachment;
import fi.metatavu.ptv.client.model.VmOpenApiLanguageItem;
import fi.metatavu.ptv.client.model.VmOpenApiLocalizedListItem;
import fi.metatavu.ptv.client.model.VmOpenApiServiceProducerIn;
import fi.metatavu.ptv.client.model.VmOpenApiServiceVoucher;
import fi.metatavu.ptv.client.model.VmOpenApiWebPageWithOrderNumber;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiIdFactory;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.PtvConsts;

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
  public List<V7VmOpenApiAddressWithMovingIn> translateAddressesMovingIn(List<Address> addresses) {
    if (addresses == null || addresses.isEmpty()) {
      return Collections.emptyList();
    }
    
    List<V7VmOpenApiAddressWithMovingIn> result = new ArrayList<>(addresses.size());
    
    for (Address address : addresses) {
      V7VmOpenApiAddressWithMovingIn ptvAddress = translateAddressMovingIn(address);
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
  public List<V4VmOpenApiServiceHour> translateServiceHours(List<ServiceHour> serviceHours) {
    if (serviceHours == null || serviceHours.isEmpty()) {
      return Collections.emptyList();
    }
    
    List<V4VmOpenApiServiceHour> result = new ArrayList<>(serviceHours.size());
    
    for (ServiceHour serviceHour : serviceHours) {
      V4VmOpenApiServiceHour ptvServiceHour = translateServiceHour(serviceHour);
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
  public List<VmOpenApiWebPageWithOrderNumber> translateWebPagesWithOrder(List<WebPage> webPages) {
    if (webPages == null || webPages.isEmpty()) {
      return Collections.emptyList();
    }
    
    List<VmOpenApiWebPageWithOrderNumber> result = new ArrayList<>(webPages.size());
    int orderNumber = 0;
    
    for (WebPage webPage : webPages) {
      VmOpenApiWebPageWithOrderNumber ptvWebPage = translateWebPageWithOrder(webPage, String.valueOf(orderNumber));
      if (ptvWebPage != null) {
        result.add(ptvWebPage);
        orderNumber++;
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
  public List<V4VmOpenApiWebPage> translateWebPages(List<WebPage> webPages) {
    if (webPages == null || webPages.isEmpty()) {
      return Collections.emptyList();
    }
    
    List<V4VmOpenApiWebPage> result = new ArrayList<>(webPages.size());
    
    for (WebPage webPage : webPages) {
      V4VmOpenApiWebPage ptvWebPage = translateWebPage(webPage);
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
  public List<VmOpenApiServiceVoucher> translateVouchers(List<ServiceVoucher> vouchers) {
    if (vouchers == null) {
      return Collections.emptyList();
    }
    
    List<VmOpenApiServiceVoucher> result = new ArrayList<>(vouchers.size());
    
    for (int i = 0; i < vouchers.size(); i++) {
      VmOpenApiServiceVoucher voucher = translateVoucher(vouchers.get(i), i);
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
  public List<VmOpenApiServiceProducerIn> translateServiceProducers(List<ServiceOrganization> organizations) {
    if (organizations == null) {
      return Collections.emptyList();
    }
    
    List<ServiceOrganization> serviceProducers = organizations.stream()
      .filter(serviceOrganization -> "Producer".equals(serviceOrganization.getRoleType()))
      .collect(Collectors.toList());
    
    List<VmOpenApiServiceProducerIn> result = new ArrayList<>(serviceProducers.size());
    
    for (int i = 0; i < serviceProducers.size(); i++) {
      VmOpenApiServiceProducerIn producer = translateServiceProducer(serviceProducers.get(i), i);
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
   * @param deliveryAddress Kunta API delivery address
   * @return PTV in addresses
   */
  public V7VmOpenApiAddressDeliveryIn translateDeliveryAddresses(Address deliveryAddress) {
    if (deliveryAddress == null) {
      return null;
    }
    
    V7VmOpenApiAddressDeliveryIn result = new V7VmOpenApiAddressDeliveryIn(); 
    
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
    
    return result;
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

  private VmOpenApiServiceProducerIn translateServiceProducer(ServiceOrganization serviceOrganization, Integer orderNumber) {
    if (serviceOrganization == null || !"Producer".equals(serviceOrganization.getRoleType())) {
      return null;
    }
    
    OrganizationId kuntaApiOrganizationId = kuntaApiIdFactory.createOrganizationId(serviceOrganization.getOrganizationId());
    OrganizationId ptvOrganizationId = idController.translateOrganizationId(kuntaApiOrganizationId, PtvConsts.IDENTIFIER_NAME);
    if (ptvOrganizationId == null) {
      logger.log(Level.SEVERE, () -> String.format("Failed to translate organization id %s into Ptv Id", kuntaApiOrganizationId));
      return null;
    }
    
    VmOpenApiServiceProducerIn result = new VmOpenApiServiceProducerIn();
    result.setAdditionalInformation(translateLocalizedValuesIntoLanguageItems(serviceOrganization.getAdditionalInformation()));
    result.setOrderNumber(orderNumber);
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
    result.setWebPages(translateWebPages(law.getWebPages()));
    
    return result;
  }
  
  private VmOpenApiServiceVoucher translateVoucher(ServiceVoucher voucher, Integer orderNumber) {
    if (voucher == null) {
      return null;
    }
    
    VmOpenApiServiceVoucher result = new VmOpenApiServiceVoucher();
    result.setAdditionalInformation(voucher.getAdditionalInformation());
    result.setLanguage(voucher.getLanguage());
    result.setOrderNumber(orderNumber);
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

  private V4VmOpenApiWebPage translateWebPage(WebPage webPage) {
    if (webPage == null || (StringUtils.isBlank(webPage.getUrl()) && StringUtils.isBlank(webPage.getValue()))) {
      return null;
    }

    
    V4VmOpenApiWebPage result = new V4VmOpenApiWebPage();
    result.setLanguage(webPage.getLanguage());
    result.setUrl(webPage.getUrl());
    result.setValue(webPage.getValue());
    
    return result;
  }
  
  private VmOpenApiWebPageWithOrderNumber translateWebPageWithOrder(WebPage webPage, String orderNumber) {
    if (webPage == null) {
      return null;
    }
    
    VmOpenApiWebPageWithOrderNumber result = new VmOpenApiWebPageWithOrderNumber();
    result.setLanguage(webPage.getLanguage());
    result.setOrderNumber(orderNumber);
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
    if (phoneNumber == null || !"Phone".equals(phoneNumber.getType())) {
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
  
  private V7VmOpenApiAddressWithMovingIn translateAddressMovingIn(Address address) {
    if (address == null) {
      return null;
    }
    
    V7VmOpenApiAddressWithMovingIn result = new V7VmOpenApiAddressWithMovingIn();
    result.setCountry(address.getCountry());
    result.setLocationAbroad(translateLocalizedValuesIntoLanguageItems(address.getLocationAbroad()));
    result.setMultipointLocation(translateAddressesWithCoordinatesIn(address.getMultipointLocation()));
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

  private V4VmOpenApiServiceHour translateServiceHour(ServiceHour serviceHour) {
    if (serviceHour == null) {
      return null;
    }
    
    V4VmOpenApiServiceHour result = new V4VmOpenApiServiceHour();
    result.setAdditionalInformation(translateLocalizedValuesIntoLanguageItems(serviceHour.getAdditionalInformation()));
    result.setIsClosed(serviceHour.getIsClosed());
    result.setOpeningHour(translateDailyOpeningTimes(serviceHour.getOpeningHour()));
    result.setServiceHourType(serviceHour.getServiceHourType());
    result.setValidForNow(serviceHour.getValidForNow());
    result.setValidFrom(serviceHour.getValidFrom());
    result.setValidTo(serviceHour.getValidTo());
    
    return result;
  }

  private List<V2VmOpenApiDailyOpeningTime> translateDailyOpeningTimes(List<DailyOpeningTime> dailyOpeningTimes) {
    if (dailyOpeningTimes == null || dailyOpeningTimes.isEmpty()) {
      return Collections.emptyList();
    }
    
    List<V2VmOpenApiDailyOpeningTime> result = new ArrayList<>(dailyOpeningTimes.size());
    
    for (DailyOpeningTime dailyOpeningTime : dailyOpeningTimes) {
      V2VmOpenApiDailyOpeningTime ptvServiceHour = translateDailyOpeningTime(dailyOpeningTime);
      if (ptvServiceHour != null) {
        result.add(ptvServiceHour);
      }
    }
    
    return result;
  }

  private V2VmOpenApiDailyOpeningTime translateDailyOpeningTime(DailyOpeningTime dailyOpeningTime) {
    if (dailyOpeningTime == null) {
      return null;
    }
    
    V2VmOpenApiDailyOpeningTime result = new V2VmOpenApiDailyOpeningTime();
    result.setDayFrom(translateOpeningTimeDay(dailyOpeningTime.getDayFrom()));
    result.setDayTo(translateOpeningTimeDay(dailyOpeningTime.getDayTo()));
    result.setFrom(dailyOpeningTime.getFrom());
    result.setIsExtra(dailyOpeningTime.getIsExtra());
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
