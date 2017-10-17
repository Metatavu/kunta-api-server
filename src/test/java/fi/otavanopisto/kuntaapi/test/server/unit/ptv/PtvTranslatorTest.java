package fi.otavanopisto.kuntaapi.test.server.unit.ptv;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
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
import fi.otavanopisto.kuntaapi.test.AbstractTest;

@RunWith (CdiTestRunner.class)
public class PtvTranslatorTest extends AbstractTest {
  
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
  
  private void assertServiceHoursEqual(List<ServiceHour> expectedServiceHours, List<ServiceHour> actualServiceHours) throws IOException, JSONException {
    assertEquals(expectedServiceHours.size(), actualServiceHours.size());
    for (int i = 0; i < expectedServiceHours.size(); i++) {
      ServiceHour expectedServiceHour = expectedServiceHours.get(i);
      ServiceHour actualServiceHour = actualServiceHours.get(i);
      assertEquals(expectedServiceHour.getServiceHourType(), actualServiceHour.getServiceHourType());
      assertInstantsMatch(expectedServiceHour.getValidFrom(), actualServiceHour.getValidFrom());
      assertInstantsMatch(expectedServiceHour.getValidTo(), actualServiceHour.getValidTo());
      assertEquals(expectedServiceHour.getIsClosed(), actualServiceHour.getIsClosed());
      assertEquals(expectedServiceHour.getValidForNow(), actualServiceHour.getValidForNow());
      assertJSONEquals(expectedServiceHour.getAdditionalInformation(), actualServiceHour.getAdditionalInformation());
      assertJSONEquals(expectedServiceHour.getOpeningHour(), actualServiceHour.getOpeningHour());
    }
  }
  
  private void assertJSONEquals(Object expected, Object actual) throws IOException, JSONException {
    ObjectMapper objectMapper = new ObjectMapper();

    JSONAssert.assertEquals(objectMapper.writeValueAsString(expected), objectMapper.writeValueAsString(actual), false);
  }

  private void assertInstantsMatch(OffsetDateTime expected, OffsetDateTime actual) {
    if (expected == actual) {
      return;  
    }
    
    if (expected == null || actual == null) {
      fail("ServiceHours are not equal");
    }
    
    assertTrue(String.format("ServiceHours (%s, %s) are not equal", expected, actual), sameInstant(expected.toInstant()).matches(actual.toInstant()));
  }
  
  private ObjectMapper getObjectMapper() {
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    return objectMapper;
  }
  
}
