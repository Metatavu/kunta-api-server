package fi.otavanopisto.kuntaapi.test.server.integration.ptv;

import java.util.Arrays;
import java.util.List;

import fi.metatavu.kuntaapi.server.rest.model.Email;
import fi.metatavu.kuntaapi.server.rest.model.LocalizedValue;
import fi.metatavu.kuntaapi.server.rest.model.Phone;
import fi.metatavu.kuntaapi.server.rest.model.WebPage;
import fi.metatavu.ptv.client.model.V4VmOpenApiPhone;
import fi.metatavu.ptv.client.model.VmOpenApiLanguageItem;
import fi.metatavu.ptv.client.model.VmOpenApiLocalizedListItem;
import fi.otavanopisto.kuntaapi.test.AbstractIntegrationTest;

public class AbstractPtvInTest extends AbstractIntegrationTest {

  protected List<WebPage> createWebPages(String language, String type, String url, String value, String description) {
    WebPage result = new WebPage();
    result.setDescription(description);
    result.setLanguage(language);
    result.setType(type);
    result.setUrl(url);
    result.setValue(value);
    
    return Arrays.asList(result);
  }

  protected List<V4VmOpenApiPhone> createPtvInPhones(String language, String type, String prefixNumber, String number, String serviceChargeType, String chargeDescription, Boolean isFinnishServiceNumber, String additionalInformation) {
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

  protected List<Phone> createPhones(String language, String type, String prefixNumber, String number, String serviceChargeType, String chargeDescription, Boolean isFinnishServiceNumber, String additionalInformation) {
    Phone result = new Phone();
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

  protected List<LocalizedValue> createLocalizedValue(String language, String type, String value) {
    LocalizedValue result = new LocalizedValue();
    result.setLanguage(language);
    result.setType(type);
    result.setValue(value);
    return Arrays.asList(result);
  }
  
}
