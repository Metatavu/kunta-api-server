package fi.otavanopisto.kuntaapi.test.server.unit.ptv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.skyscreamer.jsonassert.JSONAssert;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import fi.metatavu.kuntaapi.server.rest.model.ServiceHour;
import fi.metatavu.ptv.client.model.V4VmOpenApiServiceHour;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.PtvTranslator;

@RunWith (CdiTestRunner.class)
public class PtvTranslatorTest {
  
  @Inject
  private PtvTranslator ptvTranslator;

  @Test
  public void testNull() {
    assertNotNull(ptvTranslator);
    List<ServiceHour> translated = ptvTranslator.translateServiceHours(null);
    assertNotNull(translated);
    assertEquals(0, translated.size());
  }

  @Test
  public void testEmpty() {
    assertNotNull(ptvTranslator);
    List<ServiceHour> translated = ptvTranslator.translateServiceHours(Collections.emptyList());
    assertNotNull(translated);
    assertEquals(0, translated.size());
  }

  @Test
  public void testSingle() throws IOException, JSONException {
    assertNotNull(ptvTranslator);
    
    List<V4VmOpenApiServiceHour> ptvServiceHours = loadPtvTestHours("ptv-single.json");
    List<ServiceHour> expected = loadKuntaAPITestHours("kuntaapi-single.json");
    List<ServiceHour> actual = ptvTranslator.translateServiceHours(ptvServiceHours);
    
    assertServiceHoursEqual(expected, actual);
  }

  @Test
  public void testUnmodified() throws IOException, JSONException {
    assertNotNull(ptvTranslator);
    
    List<V4VmOpenApiServiceHour> ptvServiceHours = loadPtvTestHours("ptv-unmodified.json");
    List<ServiceHour> expected = loadKuntaAPITestHours("kuntaapi-unmodified.json");
    List<ServiceHour> actual = ptvTranslator.translateServiceHours(ptvServiceHours);
    
    assertServiceHoursEqual(expected, actual);
  }

  @Test
  public void testMerged() throws IOException, JSONException {
    assertNotNull(ptvTranslator);
    
    List<V4VmOpenApiServiceHour> ptvServiceHours = loadPtvTestHours("ptv-merged.json");
    List<ServiceHour> expected = loadKuntaAPITestHours("kuntaapi-merged.json");
    List<ServiceHour> actual = ptvTranslator.translateServiceHours(ptvServiceHours);
    
    assertServiceHoursEqual(expected, actual);
  }
  
  private List<V4VmOpenApiServiceHour> loadPtvTestHours(String file) throws IOException {
    try (InputStream fileStream = getClass().getClassLoader().getResourceAsStream(String.format("ptv/servicehours/%s", file))) {
      ObjectMapper objectMapper = getObjectMapper();
      return objectMapper.readValue(fileStream, new TypeReference<List<V4VmOpenApiServiceHour>>() {});
    }
  }

  private List<ServiceHour> loadKuntaAPITestHours(String file) throws IOException {
    try (InputStream fileStream = getClass().getClassLoader().getResourceAsStream(String.format("ptv/servicehours/%s", file))) {
      ObjectMapper objectMapper = getObjectMapper();
      return objectMapper.readValue(fileStream, new TypeReference<List<ServiceHour>>() {});
    }
  }
  
  private void assertServiceHoursEqual(List<ServiceHour> expected, List<ServiceHour> actual) throws IOException, JSONException {
    String expectedString = getObjectMapper().writeValueAsString(expected);
    String actualString = getObjectMapper().writeValueAsString(actual);
    JSONAssert.assertEquals("ServiceHours are not equal", expectedString, actualString, false);
  }
  
  private ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    return objectMapper;
  }
  
}
