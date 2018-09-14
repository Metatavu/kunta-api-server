package fi.metatavu.kuntaapi.test.server.unit.ptv;

import java.io.IOException;
import java.io.InputStream;

import org.json.JSONException;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.metatavu.kuntaapi.server.rest.model.Organization;
import fi.metatavu.kuntaapi.server.rest.ptv7adapter.Ptv7Adapter;
import fi.metatavu.kuntaapi.test.AbstractTest;

public class Ptv7AdapterTest extends AbstractTest {
  
  @Test
  public void testOrganization() throws IOException, JSONException {
    Ptv7Adapter adapter = new Ptv7Adapter();
    
    Organization organization = loadOrganization("ptv/adapter/organizations/v8.json");
    Organization expected = loadOrganization("ptv/adapter/organizations/v7.json");
    Organization translated = adapter.translate(organization);

    JSONAssert.assertEquals(toJson(expected), toJson(translated), false);
  }
  
  private String toJson(Object entity) throws JsonProcessingException {
    return getObjectMapper().writeValueAsString(entity);
  }

  @Test
  public void testOrganizationNull() throws IOException {
    Ptv7Adapter adapter = new Ptv7Adapter();
    adapter.translate(new Organization());
  }
  
  private Organization loadOrganization(String file) throws IOException {
    try (InputStream fileStream = getClass().getClassLoader().getResourceAsStream(file)) {
      ObjectMapper objectMapper = getObjectMapper();
      return objectMapper.readValue(fileStream, Organization.class);
    }
  }

}
