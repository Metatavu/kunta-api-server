package fi.otavanopisto.kuntaapi.server.integrations.ptv.in;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.rest.model.Address;
import fi.metatavu.kuntaapi.server.rest.model.LocalizedValue;
import fi.metatavu.kuntaapi.server.rest.model.Phone;
import fi.metatavu.ptv.client.model.V4VmOpenApiPhone;
import fi.metatavu.ptv.client.model.V5VmOpenApiAddressWithTypeIn;
import fi.metatavu.ptv.client.model.VmOpenApiLanguageItem;
import fi.metatavu.ptv.client.model.VmOpenApiLocalizedListItem;

/**
 * Translator for translating resources from Kunta API format into PTV formats
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class KuntaApiPtvTranslator {

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
   * Translates Kunta API addresses into PTV in addresses
   * 
   * @param addresses Kunta API addresses
   * @return list of PTV in addresses
   */
  public List<V5VmOpenApiAddressWithTypeIn> translateAddresses(List<Address> addresses) {
    if (addresses == null || addresses.isEmpty()) {
      return Collections.emptyList();
    }
    
    List<V5VmOpenApiAddressWithTypeIn> result = new ArrayList<>(addresses.size());
    
    for (Address address : addresses) {
      V5VmOpenApiAddressWithTypeIn ptvAddress = translateAddress(address);
      if (ptvAddress != null) {
        result.add(ptvAddress);
      }
    }
    
    return result;
  }

  private V4VmOpenApiPhone translatePhoneNumber(Phone phoneNumber) {
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
    
    VmOpenApiLocalizedListItem result = new VmOpenApiLocalizedListItem();
    result.setLanguage(localizedValue.getLanguage());
    result.setValue(localizedValue.getValue());
    result.setType(localizedValue.getType());

    return result;
  }
  
  private V5VmOpenApiAddressWithTypeIn translateAddress(Address address) {
    if (address == null) {
      return null;
    }
    
    V5VmOpenApiAddressWithTypeIn result = new V5VmOpenApiAddressWithTypeIn();
    result.setAdditionalInformations(translateLocalizedValuesIntoLanguageItems(address.getAdditionalInformations()));
    result.setCountry(address.getCountry());
    result.setLatitude(address.getLatitude());
    result.setLongitude(address.getLongitude());
    result.setMunicipality(address.getMunicipality() != null ? address.getMunicipality().getCode() : null);
    result.setPostalCode(address.getPostalCode());
    result.setPostOfficeBox(translateLocalizedValuesIntoLanguageItems(address.getPostOfficeBox()));
    result.setStreetAddress(translateLocalizedValuesIntoLanguageItems(address.getStreetAddress()));
    result.setStreetNumber(address.getStreetNumber());
    result.setType(address.getType());
    
    return result;
  }

}
