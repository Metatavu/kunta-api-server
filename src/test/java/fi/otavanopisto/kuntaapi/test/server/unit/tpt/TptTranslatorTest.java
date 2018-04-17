package fi.otavanopisto.kuntaapi.test.server.unit.tpt;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneId;
import java.util.UUID;

import javax.inject.Inject;

import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.metatavu.kuntaapi.server.rest.model.Job;
import fi.otavanopisto.kuntaapi.server.id.JobId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.integrations.tpt.TptConsts;
import fi.otavanopisto.kuntaapi.server.integrations.tpt.TptTranslator;
import fi.otavanopisto.kuntaapi.server.integrations.tpt.client.model.ApiResponse;
import fi.otavanopisto.kuntaapi.server.integrations.tpt.client.model.DocsEntry;
import fi.otavanopisto.kuntaapi.test.AbstractTest;

@RunWith (CdiTestRunner.class)
public class TptTranslatorTest extends AbstractTest {
  
  private static final String EXAMPLE_BASE_URL = "https://api.example.com";
  
  @Inject
  private TptTranslator tptTranslator;

  @Test
  public void testNull() {
    assertNotNull(tptTranslator);
    tptTranslator.translateJob(getJobId(), EXAMPLE_BASE_URL, null);
  }

  @Test
  public void testHakuPaattyNull() throws IOException {
    assertNotNull(tptTranslator);
    ApiResponse areaSearch = loadAreaSearch();
    
    DocsEntry docsEntry = areaSearch.getResponse().getDocs().get(0);
    assertNotNull(docsEntry);
    assertNull(docsEntry.getHakuPaattyy());
    
    Job kuntaApiJob = tptTranslator.translateJob(getJobId(), EXAMPLE_BASE_URL, docsEntry);
    assertNotNull(kuntaApiJob);
    assertNull(kuntaApiJob.getPublicationEnd());
  }
  
  @Test
  public void testHakuPaattyDate() throws IOException {
    assertNotNull(tptTranslator);
    ApiResponse areaSearch = loadAreaSearch();
    
    DocsEntry docsEntry = areaSearch.getResponse().getDocs().get(1);
    assertNotNull(docsEntry);
    assertEquals("14.04.2018", docsEntry.getHakuPaattyy());

    Job kuntaApiJob = tptTranslator.translateJob(getJobId(), EXAMPLE_BASE_URL, docsEntry);
    assertNotNull(kuntaApiJob);
    assertNotNull(kuntaApiJob.getPublicationEnd());
    assertEquals(kuntaApiJob.getPublicationEnd().toInstant(), getInstant(2018, 4, 14, 0, 0, ZoneId.of(TptConsts.TIMEZONE)));    
  }
  
  @Test
  public void testHakuPaattyDateTime() throws IOException {
    assertNotNull(tptTranslator);
    ApiResponse areaSearch = loadAreaSearch();
    
    DocsEntry docsEntry = areaSearch.getResponse().getDocs().get(2);
    assertNotNull(docsEntry);
    assertEquals("15.05.2018 klo 14:00", docsEntry.getHakuPaattyy());

    Job kuntaApiJob = tptTranslator.translateJob(getJobId(), EXAMPLE_BASE_URL, docsEntry);
    assertNotNull(kuntaApiJob);
    assertNotNull(kuntaApiJob.getPublicationEnd());
    assertEquals(kuntaApiJob.getPublicationEnd().toInstant(), getInstant(2018, 5, 15, 14, 0, ZoneId.of(TptConsts.TIMEZONE)));    
  }

  private JobId getJobId() {
    return new JobId(getOrganizationId(), KuntaApiConsts.IDENTIFIER_NAME, UUID.randomUUID().toString());
  }
  
  private OrganizationId getOrganizationId() {
    return new OrganizationId(KuntaApiConsts.IDENTIFIER_NAME, UUID.randomUUID().toString());
  }
  
  private ApiResponse loadAreaSearch() throws IOException {
    try (InputStream fileStream = getClass().getClassLoader().getResourceAsStream("tpt/area-search.json")) {
      ObjectMapper objectMapper = getObjectMapper();
      return objectMapper.readValue(fileStream, new TypeReference<ApiResponse>() {});
    }
  }

}
