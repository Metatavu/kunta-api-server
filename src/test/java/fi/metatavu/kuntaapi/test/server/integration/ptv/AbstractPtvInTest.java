package fi.metatavu.kuntaapi.test.server.integration.ptv;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import fi.metatavu.kuntaapi.server.rest.model.Address;
import fi.metatavu.kuntaapi.server.rest.model.Area;
import fi.metatavu.kuntaapi.server.rest.model.Coordinates;
import fi.metatavu.kuntaapi.server.rest.model.DailyOpeningTime;
import fi.metatavu.kuntaapi.server.rest.model.Email;
import fi.metatavu.kuntaapi.server.rest.model.Law;
import fi.metatavu.kuntaapi.server.rest.model.LocalizedValue;
import fi.metatavu.kuntaapi.server.rest.model.Municipality;
import fi.metatavu.kuntaapi.server.rest.model.OntologyItem;
import fi.metatavu.kuntaapi.server.rest.model.Phone;
import fi.metatavu.kuntaapi.server.rest.model.ServiceChannelAttachment;
import fi.metatavu.kuntaapi.server.rest.model.ServiceHour;
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
import fi.metatavu.ptv.client.model.VmOpenApiServiceVoucher;
import fi.metatavu.ptv.client.model.VmOpenApiWebPageWithOrderNumber;
import fi.metatavu.kuntaapi.server.integrations.ptv.translation.PtvAddressSubtype;
import fi.metatavu.kuntaapi.test.AbstractIntegrationTest;

@SuppressWarnings ({"squid:S00107", "squid:S2187"})
public class AbstractPtvInTest extends AbstractIntegrationTest {

  private static final String LOCATION = "Location";

  protected List<WebPage> createWebPages(String language, String type, String url, String value, String description) {
    WebPage result = new WebPage();
    result.setDescription(description);
    result.setLanguage(language);
    result.setType(type);
    result.setUrl(url);
    result.setValue(value);
    
    return Arrays.asList(result);
  }

  protected List<V4VmOpenApiPhoneWithType> createPtvInPhonesWithTypes(String language, String type, String prefixNumber, String number, String serviceChargeType, String chargeDescription, Boolean isFinnishServiceNumber, String additionalInformation) {
    V4VmOpenApiPhoneWithType result = new V4VmOpenApiPhoneWithType();
    result.setAdditionalInformation(additionalInformation);
    result.setChargeDescription(chargeDescription);
    result.setIsFinnishServiceNumber(isFinnishServiceNumber);
    result.setLanguage(language);
    result.setNumber(number);
    result.setPrefixNumber(prefixNumber);
    result.setServiceChargeType(serviceChargeType);
    result.setType(type);
    return Arrays.asList(result);
  }

  protected List<V4VmOpenApiPhone> createPtvInPhones(String language, String prefixNumber, String number, String serviceChargeType, String chargeDescription, Boolean isFinnishServiceNumber, String additionalInformation) {
    V4VmOpenApiPhone result = new V4VmOpenApiPhone();
    result.setAdditionalInformation(additionalInformation);
    result.setChargeDescription(chargeDescription);
    result.setIsFinnishServiceNumber(isFinnishServiceNumber);
    result.setLanguage(language);
    result.setNumber(number);
    result.setPrefixNumber(prefixNumber);
    result.setServiceChargeType(serviceChargeType);
    return Arrays.asList(result);
  }

  protected List<V4VmOpenApiPhoneSimple> createPtvInFaxNumbers(String language, String prefixNumber, String number, Boolean isFinnishServiceNumber) {
    V4VmOpenApiPhoneSimple result = new V4VmOpenApiPhoneSimple();
    result.setIsFinnishServiceNumber(isFinnishServiceNumber);
    result.setLanguage(language);
    result.setNumber(number);
    result.setPrefixNumber(prefixNumber);
    return Arrays.asList(result);
  }

  protected List<Phone> createPhones(String language, String type, String prefixNumber, String number, String serviceChargeType, String chargeDescription, Boolean isFinnishServiceNumber, String additionalInformation) {
    Phone result = createPhone(language, type, prefixNumber, number, serviceChargeType, chargeDescription,
        isFinnishServiceNumber, additionalInformation);

    return Arrays.asList(result);
  }
  
  protected Phone createPhone(String language, String type, String prefixNumber, String number,
      String serviceChargeType, String chargeDescription, Boolean isFinnishServiceNumber,
      String additionalInformation) {
    Phone result = new Phone();
    result.setAdditionalInformation(additionalInformation);
    result.setChargeDescription(chargeDescription);
    result.setIsFinnishServiceNumber(isFinnishServiceNumber);
    result.setLanguage(language);
    result.setNumber(number);
    result.setPrefixNumber(prefixNumber);
    result.setServiceChargeType(serviceChargeType);
    result.setType(type);
    return result;
  }

  protected List<Email> createEmails(String language, String value) {
    Email result = new Email();
    result.setLanguage(language);
    result.setValue(value);
    return Arrays.asList(result);
  }

  protected List<VmOpenApiLocalizedListItem> createPtvInLocalizedItems(String language, String type, String value) {
    VmOpenApiLocalizedListItem result = new VmOpenApiLocalizedListItem();
    result.setLanguage(language);
    result.setType(type);
    result.setValue(value);
    return Arrays.asList(result);
  }

  protected List<VmOpenApiLanguageItem> createPtvInLanguageItems(String language, String value) {
    VmOpenApiLanguageItem result = new VmOpenApiLanguageItem();
    result.setLanguage(language);
    result.setValue(value);
    return Arrays.asList(result);
  }
  
  protected List<LocalizedValue> createLocalizedValue(String language, String value) {
    return createLocalizedValue(language, null, value);
  }

  protected List<LocalizedValue> createLocalizedValue(String language, String type, String value) {
    LocalizedValue result = new LocalizedValue();
    result.setLanguage(language);
    result.setType(type);
    result.setValue(value);
    return Arrays.asList(result);
  }

  protected V4VmOpenApiServiceHour creaatePtvInServiceHour(Boolean isClosed, List<V2VmOpenApiDailyOpeningTime> openingHour, String serviceHourType, Boolean validForNow, OffsetDateTime validFrom, OffsetDateTime validTo, List<VmOpenApiLanguageItem> additionalInformation) {
    V4VmOpenApiServiceHour result = new V4VmOpenApiServiceHour();
    result.setAdditionalInformation(additionalInformation);
    result.setIsClosed(isClosed);
    result.setOpeningHour(openingHour);
    result.setServiceHourType(serviceHourType);
    result.setValidForNow(validForNow);
    result.setValidFrom(validFrom);
    result.setValidTo(validTo);
    return result;
  }

  protected ServiceHour createServiceHour(Boolean isClosed, List<DailyOpeningTime> openingHour, String serviceHourType, Boolean validForNow, OffsetDateTime validFrom, OffsetDateTime validTo, List<LocalizedValue> additionalInformation) {
    ServiceHour result = new ServiceHour();
    result.setAdditionalInformation(additionalInformation);
    result.setIsClosed(isClosed);
    result.setOpeningHour(openingHour);
    result.setServiceHourType(serviceHourType);
    result.setValidForNow(validForNow);
    result.setValidFrom(validFrom);
    result.setValidTo(validTo);
    return result;
  }

  protected List<VmOpenApiWebPageWithOrderNumber> createPtvInWebPages(String language, String url, String value) {
    VmOpenApiWebPageWithOrderNumber result = new VmOpenApiWebPageWithOrderNumber();
    result.setLanguage(language);
    result.setOrderNumber("0");
    result.setUrl(url);
    result.setValue(value);
    return Arrays.asList(result);
  }

  protected List<V7VmOpenApiAddressWithMovingIn> createPtvInAddressAbroad(List<VmOpenApiLanguageItem> locationAbroad) {
    return createPtvInAddress(LOCATION, PtvAddressSubtype.ABROAD.getPtvValue(), null, null, null, Collections.emptyList(), locationAbroad);
  }

  protected List<V7VmOpenApiAddressWithMovingIn> createPtvInAddress(String type, String subType, String country, VmOpenApiAddressStreetWithCoordinatesIn streetAddress, VmOpenApiAddressPostOfficeBoxIn postOfficeBoxAddress, List<VmOpenApiAddressStreetWithCoordinatesIn> multipointLocation, List<VmOpenApiLanguageItem> locationAbroad) {
    V7VmOpenApiAddressWithMovingIn result = new V7VmOpenApiAddressWithMovingIn();
    result.setCountry(country);
    result.setLocationAbroad(locationAbroad);
    result.setMultipointLocation(multipointLocation);
    result.setPostOfficeBoxAddress(postOfficeBoxAddress);
    result.setStreetAddress(streetAddress);
    result.setSubType(subType);
    result.setType(type);
    return Arrays.asList(result);
  }
  
  protected List<Address> createAddresssesAbroad(List<LocalizedValue> locationAbroad) {
    return Arrays.asList(createAddressAbroad(locationAbroad));
  }

  protected Address createAddressAbroad(List<LocalizedValue> locationAbroad) {
    return createAddress(LOCATION, PtvAddressSubtype.ABROAD.getPtvValue(), null, null, null, null, null, locationAbroad, null, null, null, null, null, null, null, null);
  }
  
  protected Address createAddressFreeText(List<LocalizedValue> additionalInformation) {
    return createAddress(LOCATION, PtvAddressSubtype.NO_ADDRESS.getPtvValue(), null, null, null, null, null, null, null, null, null, null, null, null, null, additionalInformation);
  }

  protected List<Address> createAddressses(String type, String subtype, String country, List<LocalizedValue> streetAddress, String streetNumber, List<LocalizedValue> postOffice, List<Address> multipointLocation, List<LocalizedValue> locationAbroad, String latitude, String longitude, Municipality municipality, String postalCode, List<LocalizedValue> postOfficeBox, String coordinateState, Coordinates coordinates, List<LocalizedValue> additionalInformations) {
    Address result = createAddress(type, subtype, country, streetAddress, streetNumber, postOffice, multipointLocation,
        locationAbroad, latitude, longitude, municipality, postalCode, postOfficeBox, coordinateState, coordinates,
        additionalInformations);
    return Arrays.asList(result);
  }

  protected Address createAddress(String type, String subtype, String country, List<LocalizedValue> streetAddress,
      String streetNumber, List<LocalizedValue> postOffice, List<Address> multipointLocation,
      List<LocalizedValue> locationAbroad, String latitude, String longitude, Municipality municipality,
      String postalCode, List<LocalizedValue> postOfficeBox, String coordinateState, Coordinates coordinates,
      List<LocalizedValue> additionalInformations) {
    Address result = new Address();
    result.setAdditionalInformations(additionalInformations);
    result.setCoordinates(coordinates);
    result.setCoordinateState(coordinateState);
    result.setCountry(country);
    result.setLatitude(latitude);
    result.setLocationAbroad(locationAbroad);
    result.setLongitude(longitude);
    result.setMultipointLocation(multipointLocation);
    result.setMunicipality(municipality);
    result.setPostalCode(postalCode);
    result.setPostOffice(postOffice);
    result.setPostOfficeBox(postOfficeBox);
    result.setStreetAddress(streetAddress);
    result.setStreetNumber(streetNumber);
    result.setSubtype(subtype);
    result.setType(type);
    return result;
  }

  protected VmOpenApiAreaIn createArea(String type, String code) {
    VmOpenApiAreaIn result = new VmOpenApiAreaIn();
    result.setAreaCodes(Arrays.asList(code));
    result.setType(type);
    return result;
  }

  protected Area createArea(String type, String code, List<LocalizedValue> name) {
    Area result = new Area();
    
    if ("Municipality".equals(type)) {
      Municipality municipality = new Municipality();
      municipality.setCode(code);
      municipality.setNames(name);
      result.setMunicipalities(Arrays.asList(municipality));
    } else {
      result.setCode(code);
      result.setName(name);
      result.setMunicipalities(Collections.emptyList());
    }

    result.setType(type);
    return result;
  }

  protected V7VmOpenApiAddressDeliveryIn createPtvInDeliveryAddress(String subType, VmOpenApiAddressStreetIn streetAddress, VmOpenApiAddressPostOfficeBoxIn postOfficeBoxAddress, List<VmOpenApiLanguageItem> deliveryAddressInText) {
    V7VmOpenApiAddressDeliveryIn result = new V7VmOpenApiAddressDeliveryIn();
    result.setDeliveryAddressInText(deliveryAddressInText);
    result.setPostOfficeBoxAddress(postOfficeBoxAddress);
    result.setStreetAddress(streetAddress);
    result.setSubType(subType);
    return result;
  }

  protected List<VmOpenApiAttachment> createPtvInAttachments(String language, String url, String name, String description) {
    VmOpenApiAttachment result = new VmOpenApiAttachment();
    result.setDescription(description);
    result.setLanguage(language);
    result.setName(name);
    result.setUrl(url);
    return Arrays.asList(result);
  }

  protected List<ServiceChannelAttachment> createAttachments(String language, String type, String url, String name, String description) {
    ServiceChannelAttachment result = new ServiceChannelAttachment();
    result.setDescription(description);
    result.setLanguage(language);
    result.setName(name);
    result.setType(type);
    result.setUrl(url);
    return Arrays.asList(result);
  }

  protected List<VmOpenApiServiceVoucher> createPtvServiceVouchers(String language, String value, String url, String additionalInformation) {
    VmOpenApiServiceVoucher result = new VmOpenApiServiceVoucher();
    result.setAdditionalInformation(additionalInformation);
    result.setLanguage(language);
    result.setOrderNumber(0);
    result.setUrl(url);
    result.setValue(value);
    return Arrays.asList(result);
  }
  
  protected List<ServiceVoucher> createServiceVouchers(String language, String value, String url, String additionalInformation) {
    ServiceVoucher result = new ServiceVoucher();
    result.setAdditionalInformation(additionalInformation);
    result.setLanguage(language);
    result.setUrl(url);
    result.setValue(value);
    return Arrays.asList(result);
  }
  
  protected List<V4VmOpenApiLaw> createPtvInLaw(List<VmOpenApiLanguageItem> names, String url, String value) {
    V4VmOpenApiLaw result = new V4VmOpenApiLaw();
    result.setNames(names);
    result.setWebPages(createPtvWebPages("en", url, value));
    return Arrays.asList(result);
  }
  
  protected List<V4VmOpenApiWebPage> createPtvWebPages(String language, String url, String value) {
    V4VmOpenApiWebPage result = new V4VmOpenApiWebPage();
    result.setLanguage(language);
    result.setUrl(url);
    result.setValue(value);
    return Arrays.asList(result);
  }
  
  protected List<Law> createLaw(List<LocalizedValue> names, String url, String value, String description) {
    Law result = new Law();
    result.setNames(names);
    result.setWebPages(createWebPages("en", "Web", url, value, description));
    return Arrays.asList(result);
  }
  
  protected List<OntologyItem> createOntologyItems(String uri) {
    OntologyItem result = new OntologyItem();
    result.setCode("Fake");
    result.setName(createLocalizedValue("en", "Fake Class"));
    result.setOntologyType("TestType");
    result.setParentId(null);
    result.setParentUri(null);
    result.setSystem("TEST");
    result.setUri(uri);
    return Arrays.asList(result);
  }
  
}
