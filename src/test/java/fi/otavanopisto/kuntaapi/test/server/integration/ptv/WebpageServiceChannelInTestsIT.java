package fi.otavanopisto.kuntaapi.test.server.integration.ptv;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.jayway.restassured.http.ContentType;

import fi.metatavu.kuntaapi.server.rest.model.WebPageServiceChannel;
import fi.metatavu.ptv.client.model.V6VmOpenApiWebPageChannelInBase;
import fi.metatavu.ptv.client.model.V7VmOpenApiWebPageChannel;
import fi.otavanopisto.kuntaapi.server.persistence.model.clients.AccessType;
import fi.otavanopisto.kuntaapi.server.persistence.model.clients.ClientOrganizationPermission;
import fi.otavanopisto.kuntaapi.test.AbstractPtvMocker;

public class WebpageServiceChannelInTestsIT extends AbstractPtvInTest {

  /**
   * Starts WireMock
   */
  @Rule
  public WireMockRule wireMockRule = new WireMockRule(getWireMockPort());
  
  @Before
  public void beforeTest() throws InterruptedException {
    getPtvOrganizationMocker().mock(TestPtvConsts.ORGANIZATIONS);
    getPtvServiceMocker().mock(TestPtvConsts.SERVICES);    
    getPtvServiceChannelMocker().mock(TestPtvConsts.SERVICE_CHANNELS);

    startMocks();
    
    waitApiListCount("/organizations", 3);
    waitApiListCount("/webPageServiceChannels", TestPtvConsts.WEB_PAGE_SERVICE_CHANNELS.length);
    waitApiListCount("/services", TestPtvConsts.SERVICES.length);
  }
  
  @Test
  public void updateWebPageServiceChannelUnauthorized() throws JsonParseException, JsonMappingException, IOException, InterruptedException {
    WebPageServiceChannel kuntaApiResource = getWebPageChannel(0, TestPtvConsts.WEB_PAGE_SERVICE_CHANNELS.length);
    
    givenReadonly()
      .body(kuntaApiResource)
      .contentType(ContentType.JSON)
      .put("/webPageServiceChannels/{kuntaApiChannelId}", kuntaApiResource.getId())
      .then()
      .assertThat()
      .statusCode(401);
  }
  
  @Test
  public void updateWebPageServiceChannelForbidden() throws JsonParseException, JsonMappingException, IOException, InterruptedException {
    WebPageServiceChannel kuntaApiResource = getWebPageChannel(0, TestPtvConsts.WEB_PAGE_SERVICE_CHANNELS.length);
    
    givenReadWrite()
      .body(kuntaApiResource)
      .contentType(ContentType.JSON)
      .put("/webPageServiceChannels/{kuntaApiChannelId}", kuntaApiResource.getId())
      .then()
      .assertThat()
      .statusCode(403);
  }
  
  @Test
  public void updateWebPageServiceChannelUnchanged() throws JsonParseException, JsonMappingException, IOException, InterruptedException {
    String ptvId = TestPtvConsts.WEB_PAGE_SERVICE_CHANNELS[0];
    String organizationId = getOrganizationId(0);
    String kuntaApiChannelId = getWebPageChannelId(0, TestPtvConsts.WEB_PAGE_SERVICE_CHANNELS.length);
    
    grantOrganizationPermission(AccessType.READ_WRITE, organizationId, ClientOrganizationPermission.UPDATE_SERVICE_CHANNELS);

    WebPageServiceChannel kuntaApiResource = getWebPageChannel(0, TestPtvConsts.WEB_PAGE_SERVICE_CHANNELS.length);
    V6VmOpenApiWebPageChannelInBase ptvInResource = getPtvServiceChannelMocker().readEntity(AbstractPtvMocker.PTV_IN_API, ptvId, V6VmOpenApiWebPageChannelInBase.class);
    V7VmOpenApiWebPageChannel ptvOutResource =  getPtvServiceChannelMocker().readEntity(AbstractPtvMocker.PTV_OUT_API, ptvId, V7VmOpenApiWebPageChannel.class);
    
    getPtvServiceChannelMocker().mockWebpagePut(ptvId, ptvOutResource);
    
    givenReadWrite()
      .body(kuntaApiResource)
      .contentType(ContentType.JSON)
      .put("/webPageServiceChannels/{kuntaApiChannelId}",kuntaApiChannelId)
      .then()
      .assertThat()
      .statusCode(200);

    getPtvServiceChannelMocker().verifyWebPage(ptvId, ptvInResource);
  }
  
  @Test
  public void updateWebPageServiceChannelChanges() throws JsonParseException, JsonMappingException, IOException, InterruptedException {
    String ptvId = TestPtvConsts.WEB_PAGE_SERVICE_CHANNELS[0];
    String organizationId = getOrganizationId(0);
    
    String kuntaApiChannelId = getWebPageChannelId(0, TestPtvConsts.WEB_PAGE_SERVICE_CHANNELS.length);
    
    grantOrganizationPermission(AccessType.READ_WRITE, organizationId, ClientOrganizationPermission.UPDATE_SERVICE_CHANNELS);

    WebPageServiceChannel kuntaApiResource = getWebPageChannel(0, TestPtvConsts.WEB_PAGE_SERVICE_CHANNELS.length);
    kuntaApiResource.setDescriptions(createLocalizedValue("en", "Description", "Changed Description"));
    kuntaApiResource.setLanguages(Arrays.asList("en"));
    kuntaApiResource.setNames(createLocalizedValue("en", "Name", "Changed Name"));
    kuntaApiResource.setSupportEmails(createEmails("en", "fake@example.com"));
    kuntaApiResource.setSupportPhones(createPhones("en", "Phone", "+358", "12345-FAKE", "Charged", "Testing", false, "Test phone"));
    kuntaApiResource.setUrls(createLocalizedValue("en", "URL", "www.example.com"));
    
    V6VmOpenApiWebPageChannelInBase ptvInResource = getPtvServiceChannelMocker().readEntity(AbstractPtvMocker.PTV_IN_API, ptvId, V6VmOpenApiWebPageChannelInBase.class);
    ptvInResource.setServiceChannelDescriptions(createPtvInLocalizedItems("en", "Description", "Changed Description"));
    ptvInResource.setLanguages(Arrays.asList("en"));
    ptvInResource.setServiceChannelNames(createPtvInLanguageItems("en", "Changed Name"));
    ptvInResource.setSupportEmails(createPtvInLanguageItems("en", "fake@example.com"));
    ptvInResource.setSupportPhones(createPtvInPhones("en", "Phone", "+358", "12345-FAKE", "Charged", "Testing", false, "Test phone"));
    ptvInResource.setUrls(createPtvInLanguageItems("en", "www.example.com"));
    
    V7VmOpenApiWebPageChannel ptvOutResource =  getPtvServiceChannelMocker().readEntity(AbstractPtvMocker.PTV_OUT_API, ptvId, V7VmOpenApiWebPageChannel.class);
    
    getPtvServiceChannelMocker().mockWebpagePut(ptvId, ptvOutResource);
    
    givenReadWrite()
      .body(kuntaApiResource)
      .contentType(ContentType.JSON)
      .put("/webPageServiceChannels/{kuntaApiChannelId}",kuntaApiChannelId)
      .then()
      .assertThat()
      .statusCode(200);

    getPtvServiceChannelMocker().verifyWebPage(ptvId, ptvInResource);
  }
}
