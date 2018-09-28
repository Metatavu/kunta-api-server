package fi.metatavu.kuntaapi.test.server.unit.ptv;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.io.InputStream;

import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.metatavu.kuntaapi.server.rest.model.ElectronicServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.Organization;
import fi.metatavu.kuntaapi.server.rest.model.PhoneServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.PrintableFormServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.Service;
import fi.metatavu.kuntaapi.server.rest.model.ServiceLocationServiceChannel;
import fi.metatavu.kuntaapi.server.rest.model.WebPageServiceChannel;
import fi.metatavu.kuntaapi.server.rest.ptv7adapter.Ptv7Adapter;
import fi.metatavu.kuntaapi.test.AbstractTest;

public class Ptv7AdapterTest extends AbstractTest {
  
  @Test
  public void testOrganization() throws IOException, JSONException {
    Ptv7Adapter adapter = new Ptv7Adapter();
    
    Organization organization = loadEntity(Organization.class, "ptv/adapter/organizations/v8.json");
    Organization expected = loadEntity(Organization.class, "ptv/adapter/organizations/v7.json");
    Organization translated = adapter.translate(organization);

    JSONAssert.assertEquals(toJson(expected), toJson(translated), false);
  }
  
  @Test
  public void testElectronicServiceChannel() throws IOException, JSONException {
    Ptv7Adapter adapter = new Ptv7Adapter();
    
    ElectronicServiceChannel original = loadEntity(ElectronicServiceChannel.class, "ptv/adapter/servicechannels/electronic/v8.json");
    ElectronicServiceChannel expected = loadEntity(ElectronicServiceChannel.class, "ptv/adapter/servicechannels/electronic/v7.json");
    ElectronicServiceChannel translated = adapter.translate(original);

    JSONAssert.assertEquals(toJson(expected), toJson(translated), false);
  }

  @Test
  public void testPrintableFormServiceChannel() throws IOException, JSONException {
    Ptv7Adapter adapter = new Ptv7Adapter();
    
    PrintableFormServiceChannel original = loadEntity(PrintableFormServiceChannel.class, "ptv/adapter/servicechannels/printableform/v8.json");
    PrintableFormServiceChannel expected = loadEntity(PrintableFormServiceChannel.class, "ptv/adapter/servicechannels/printableform/v7.json");
    PrintableFormServiceChannel translated = adapter.translate(original);

    JSONAssert.assertEquals(toJson(expected), toJson(translated), false);
  }

  @Test
  public void testServiceLocationServiceChannel() throws IOException, JSONException {
    Ptv7Adapter adapter = new Ptv7Adapter();
    
    ServiceLocationServiceChannel original = loadEntity(ServiceLocationServiceChannel.class, "ptv/adapter/servicechannels/servicelocation/v8.json");
    ServiceLocationServiceChannel expected = loadEntity(ServiceLocationServiceChannel.class, "ptv/adapter/servicechannels/servicelocation/v7.json");
    ServiceLocationServiceChannel translated = adapter.translate(original);

    JSONAssert.assertEquals(toJson(expected), toJson(translated), false);
  }
  
  @Test
  public void testPhoneServiceChannel() throws IOException, JSONException {
    Ptv7Adapter adapter = new Ptv7Adapter();
    
    PhoneServiceChannel original = loadEntity(PhoneServiceChannel.class, "ptv/adapter/servicechannels/phone/v8.json");
    PhoneServiceChannel expected = loadEntity(PhoneServiceChannel.class, "ptv/adapter/servicechannels/phone/v7.json");
    PhoneServiceChannel translated = adapter.translate(original);

    JSONAssert.assertEquals(toJson(expected), toJson(translated), false);
  }
  
  @Test
  public void testWebPageServiceChannel() throws IOException, JSONException {
    Ptv7Adapter adapter = new Ptv7Adapter();
    
    WebPageServiceChannel original = loadEntity(WebPageServiceChannel.class, "ptv/adapter/servicechannels/webpage/v8.json");
    WebPageServiceChannel expected = loadEntity(WebPageServiceChannel.class, "ptv/adapter/servicechannels/webpage/v7.json");
    WebPageServiceChannel translated = adapter.translate(original);

    JSONAssert.assertEquals(toJson(expected), toJson(translated), false);
  }
  
  @Test
  public void testService() throws IOException, JSONException {
    Ptv7Adapter adapter = new Ptv7Adapter();
    
    Service original = loadEntity(Service.class, "ptv/adapter/services/v8.json");
    Service expected = loadEntity(Service.class, "ptv/adapter/services/v7.json");
    Service translated = adapter.translate(original);

    JSONAssert.assertEquals(toJson(expected), toJson(translated), false);
  }

  @Test
  public void testEmptys() throws IOException {
    Ptv7Adapter adapter = new Ptv7Adapter();

    assertNotNull(adapter.translate(new Organization()));
    assertNotNull(adapter.translate(new ElectronicServiceChannel()));
    assertNotNull(adapter.translate(new PrintableFormServiceChannel()));
    assertNotNull(adapter.translate(new ServiceLocationServiceChannel()));
    assertNotNull(adapter.translate(new PhoneServiceChannel()));
    assertNotNull(adapter.translate(new WebPageServiceChannel()));
    assertNotNull(adapter.translate(new Service()));
  }
  
  @Test
  public void testNulls() throws IOException {
    Ptv7Adapter adapter = new Ptv7Adapter();

    assertNull(adapter.translate((Organization) null));
    assertNull(adapter.translate((ElectronicServiceChannel) null));
    assertNull(adapter.translate((PrintableFormServiceChannel) null));
    assertNull(adapter.translate((ServiceLocationServiceChannel) null));
    assertNull(adapter.translate((PhoneServiceChannel) null));
    assertNull(adapter.translate((WebPageServiceChannel) null));
    assertNull(adapter.translate((Service) null));
  }
  
  /**
   * Translates entity into JSON
   * 
   * @param entity entity
   * @return JSON string
   * @throws JsonProcessingException thrown when JSON procesing fails
   */
  private String toJson(Object entity) throws JsonProcessingException {
    return getObjectMapper().writeValueAsString(entity);
  }
  
  /**
   * Loads test resource
   * 
   * @param resultClass result class
   * @param file resource file
   * @return loaded object
   * @throws IOException thrown when reading fails
   * @throws JsonParseException throw when JSON parsing fails
   * @throws JsonMappingException throw when JSON mapping fails
   */
  private <T> T loadEntity(Class<?extends T> resultClass, String file) throws IOException, JsonParseException, JsonMappingException {
    try (InputStream fileStream = getClass().getClassLoader().getResourceAsStream(file)) {
      ObjectMapper objectMapper = getObjectMapper();
      return objectMapper.readValue(fileStream, resultClass);
    }
  }
  
}
