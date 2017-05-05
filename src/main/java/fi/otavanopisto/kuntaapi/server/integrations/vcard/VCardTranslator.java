package fi.otavanopisto.kuntaapi.server.integrations.vcard;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import ezvcard.VCard;
import ezvcard.parameter.AddressType;
import ezvcard.parameter.TelephoneType;
import ezvcard.property.Organization;
import ezvcard.property.RawProperty;
import ezvcard.property.StructuredName;
import ezvcard.property.Telephone;
import ezvcard.property.TextProperty;
import ezvcard.util.GeoUri;
import fi.metatavu.kuntaapi.server.rest.model.Address;
import fi.metatavu.kuntaapi.server.rest.model.Contact;
import fi.metatavu.kuntaapi.server.rest.model.ContactPhone;
import fi.metatavu.kuntaapi.server.rest.model.ContactStatus;
import fi.metatavu.kuntaapi.server.rest.model.LocalizedValue;
import fi.otavanopisto.kuntaapi.server.id.ContactId;
import fi.otavanopisto.kuntaapi.server.integrations.vcard.mecm.Status;

@ApplicationScoped
@SuppressWarnings ({"squid:S3306", "squid:S1450"})
public class VCardTranslator {
  
  private static final String MECM_ADDITIONAL_STATUS_JSON = "mecm-status-json";
  
  @Inject
  private Logger logger;
  
  public Contact translateVCard(ContactId kuntaApiContactId, VCard vCard) {
    Contact result = new Contact();
    
    result.setAdditionalInformations(getAll(vCard.getNotes()));
    result.setAddresses(translateAddresses(vCard.getAddresses()));
    result.setDisplayName(getString(vCard.getFormattedName()));
    result.setEmails(getAll(vCard.getEmails()));
    result.setFirstName(getFirstName(vCard.getStructuredName()));
    result.setId(kuntaApiContactId.getId());
    result.setLastName(getLastName(vCard.getStructuredName()));
    result.setOrganization(getOrganization(vCard.getOrganization()));
    result.setOrganizationUnits(getOrganizationUnits(vCard.getOrganizations()));
    result.setPhones(translatePhones(vCard.getTelephoneNumbers()));
    result.setStatuses(translateStatuses(vCard));
    result.setTitle(getFirst(vCard.getTitles()));
    
    return result;
  }
  
  private List<ContactStatus> translateStatuses(VCard vCard) {
    // Statuses are currenctly supported only in MECM flavoured vCards
    
    List<RawProperty> properties = vCard.getExtendedProperties(MECM_ADDITIONAL_STATUS_JSON);
    if (properties != null) {
      List<ContactStatus> translateMecmStatuses = new ArrayList<>(properties.size());
      for (RawProperty property : properties) {
        translateMecmStatuses.add(translateMecmStatus(property.getValue()));
      }
      
      return translateMecmStatuses;
    }
    
    return Collections.emptyList();
  }

  private ContactStatus translateMecmStatus(String value) {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    
    try {
      Status mecmStatus = objectMapper.readValue(value, Status.class);
      
      ContactStatus contactStatus = new ContactStatus();
      contactStatus.setEnd(mecmStatus.getArrival());
      contactStatus.setStart(mecmStatus.getDeparture());
      contactStatus.setText(mecmStatus.getReason());
      
      return contactStatus;
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Failed to parse MECM statuses", e);
    }
    
    return null;
  }

  private List<ContactPhone> translatePhones(List<Telephone> vCardNumbers) {
    if (vCardNumbers != null && !vCardNumbers.isEmpty()) {
      List<ContactPhone> result = new ArrayList<>(vCardNumbers.size());
      for (Telephone vCardNumber : vCardNumbers) {
        ContactPhone contactPhone = new ContactPhone();
        if (StringUtils.isNotBlank(vCardNumber.getText())) {
          contactPhone.setNumber(vCardNumber.getText());
          contactPhone.setType(getPhoneType(vCardNumber.getTypes()));
          result.add(contactPhone);
        }
      }
    }
    
    return Collections.emptyList();
  }

  private List<Address> translateAddresses(List<ezvcard.property.Address> vCardAddresses) {
    if (vCardAddresses != null && !vCardAddresses.isEmpty()) {
      List<Address> result = new ArrayList<>(vCardAddresses.size());
      
      for (ezvcard.property.Address vCardAddress : vCardAddresses) {
        Address address = translateAddress(vCardAddress);
        if (address != null) {
          result.add(address);
        }
      }
      
      return result;
    }
    
    return Collections.emptyList();
  }
  
  private Address translateAddress(ezvcard.property.Address vCardAddress) {
    if (vCardAddress == null) {
      return null;
    }
    
    GeoUri geo = vCardAddress.getGeo();
    String latitude = null;
    String longitude = null;
    
    if (geo != null) {
      latitude = String.valueOf(geo.getCoordA());
      longitude = String.valueOf(geo.getCoordB());
    }
    
    Address result = new Address();
    result.setAdditionalInformations(Collections.emptyList());
    result.setCoordinateState(null);
    result.setCountry(getString(vCardAddress.getCountry(), VCardConsts.DEFAULT_COUNTRY));
    result.setLatitude(latitude);
    result.setLongitude(longitude);
    result.setMunicipality(null);
    result.setPostalCode(getString(vCardAddress.getPostalCode()));
    result.setPostOffice(Collections.emptyList());
    result.setPostOfficeBox(getLocalizedString(vCardAddress.getPoBox()));
    result.setStreetAddress(getLocalizedString(vCardAddress.getStreetAddress()));
    result.setStreetNumber(null);
    result.setType(getAddressType(vCardAddress.getTypes()));


    return result;
  }

  private String getAddressType(List<AddressType> types) {
    if (types != null && !types.isEmpty()) {
      return types.get(0).getValue();
    }

    return null;
  }

  private String getPhoneType(List<TelephoneType> types) {
    if (types != null && !types.isEmpty()) {
      return types.get(0).getValue();
    }

    return null;
  }

  private List<LocalizedValue> getLocalizedString(String value) {
    if (StringUtils.isNotBlank(value)) {
      List<LocalizedValue> result = new ArrayList<>(1);
      
      LocalizedValue localizedValue = new LocalizedValue();
      localizedValue.setLanguage(VCardConsts.DEFAULT_LANGUAGE);
      localizedValue.setValue(value);
      
      result.add(localizedValue);
      
      return result;
    }

    return Collections.emptyList();
  }

  private String getOrganization(Organization organization) {
    if (organization != null && organization.getValues() != null && !organization.getValues().isEmpty()) {
      return organization.getValues().get(0);
    }
    
    return null;
  }
  
  private List<String> getOrganizationUnits(List<Organization> organizations) {
    if (organizations != null && organizations.size() > 1) {
      List<String> result = new ArrayList<>(organizations.size() - 1);
      for (int i = 1, l = organizations.size(); i < l; i++) {
        Organization organization = organizations.get(i);
        String value = organization.getValues().isEmpty() ? null : organization.getValues().get(0);
        if (StringUtils.isNotBlank(value)) {
          result.add(value);
        }
      }
      
      return result;
    }
    
    return Collections.emptyList();
  }

  private String getFirstName(StructuredName structuredName) {
    if (structuredName != null && StringUtils.isNotBlank(structuredName.getGiven())) {
      return structuredName.getGiven();
    }
    
    return null;
  }
  
  private String getLastName(StructuredName structuredName) {
    if (structuredName != null && StringUtils.isNotBlank(structuredName.getFamily())) {
      return structuredName.getFamily();
    }
    
    return null;
  }

  private String getString(String value) {
    return getString(value, null);
  }
  
  private String getString(String value, String defaultValue) {
    if (StringUtils.isNotBlank(value)) {
      return value;
    }
    
    return defaultValue;
  }
  
  private String getString(TextProperty textProperty) {
    return getString(textProperty, null);
  }
  
  private String getString(TextProperty textProperty, String defaultValue) {
    if (textProperty != null) {
      return getString(textProperty.getValue(), defaultValue);
    }
    
    return null;
  }

  private String getFirst(List<? extends TextProperty> textProperties) {
    if (textProperties != null && !textProperties.isEmpty()) {
      return getString(textProperties.get(0));
    }
    
    return null;
  }

  private List<String> getAll(List<? extends TextProperty> textProperties) {
    if (textProperties != null && !textProperties.isEmpty()) {
      List<String> result = new ArrayList<>(textProperties.size());
      for (TextProperty textProperty : textProperties) {
        String value = textProperty.getValue();
        if (StringUtils.isNotBlank(value)) {
          result.add(value);
        }
      }
      
      return result;
    }
    
    return Collections.emptyList();
  }
  
}
