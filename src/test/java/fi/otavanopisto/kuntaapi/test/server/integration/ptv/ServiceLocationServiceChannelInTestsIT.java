package fi.otavanopisto.kuntaapi.test.server.integration.ptv;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.jayway.restassured.http.ContentType;

import fi.metatavu.kuntaapi.server.rest.model.ServiceLocationServiceChannel;
import fi.metatavu.ptv.client.model.V7VmOpenApiServiceLocationChannel;
import fi.metatavu.ptv.client.model.V7VmOpenApiServiceLocationChannelInBase;
import fi.otavanopisto.kuntaapi.server.persistence.model.clients.AccessType;
import fi.otavanopisto.kuntaapi.server.persistence.model.clients.ClientOrganizationPermission;
import fi.otavanopisto.kuntaapi.test.AbstractPtvMocker;

public class ServiceLocationServiceChannelInTestsIT extends AbstractPtvInTest {

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
    waitApiListCount("/serviceLocationServiceChannels", TestPtvConsts.SERVICE_LOCATION_SERVICE_CHANNELS.length);
    waitApiListCount("/services", TestPtvConsts.SERVICES.length);
  }
  
  @Test
  public void updateServiceLocationServiceChannelUnauthorized() throws JsonParseException, JsonMappingException, IOException, InterruptedException {
    ServiceLocationServiceChannel kuntaApiResource = getServiceLocationServiceChannel(0, TestPtvConsts.SERVICE_LOCATION_SERVICE_CHANNELS.length);
    
    givenReadonly()
      .body(kuntaApiResource)
      .contentType(ContentType.JSON)
      .put("/serviceLocationServiceChannels/{kuntaApiChannelId}", kuntaApiResource.getId())
      .then()
      .assertThat()
      .statusCode(401);
  }
  
  @Test
  public void updateServiceLocationServiceChannelForbidden() throws JsonParseException, JsonMappingException, IOException, InterruptedException {
    ServiceLocationServiceChannel kuntaApiResource = getServiceLocationServiceChannel(0, TestPtvConsts.SERVICE_LOCATION_SERVICE_CHANNELS.length);
    
    givenReadWrite()
      .body(kuntaApiResource)
      .contentType(ContentType.JSON)
      .put("/serviceLocationServiceChannels/{kuntaApiChannelId}", kuntaApiResource.getId())
      .then()
      .assertThat()
      .statusCode(403);
  }
  
  @Test
  public void updateServiceLocationServiceChannelUnchanged() throws JsonParseException, JsonMappingException, IOException, InterruptedException {
    String ptvId = TestPtvConsts.SERVICE_LOCATION_SERVICE_CHANNELS[0];
    String organizationId = getOrganizationId(0);
    String kuntaApiChannelId = getServiceLocationChannelId(0, TestPtvConsts.SERVICE_LOCATION_SERVICE_CHANNELS.length);
    
    grantOrganizationPermission(AccessType.READ_WRITE, organizationId, ClientOrganizationPermission.UPDATE_SERVICE_CHANNELS);

    ServiceLocationServiceChannel kuntaApiResource = getServiceLocationServiceChannel(0, TestPtvConsts.SERVICE_LOCATION_SERVICE_CHANNELS.length);
    V7VmOpenApiServiceLocationChannelInBase ptvInResource = getPtvServiceChannelMocker().readEntity(AbstractPtvMocker.PTV_IN_API, ptvId, V7VmOpenApiServiceLocationChannelInBase.class);
    V7VmOpenApiServiceLocationChannel ptvOutResource =  getPtvServiceChannelMocker().readEntity(AbstractPtvMocker.PTV_OUT_API, ptvId, V7VmOpenApiServiceLocationChannel.class);
    
    getPtvServiceChannelMocker().mockServiceLocationPut(ptvId, ptvOutResource);
    
    givenReadWrite()
      .body(kuntaApiResource)
      .contentType(ContentType.JSON)
      .put("/serviceLocationServiceChannels/{kuntaApiChannelId}",kuntaApiChannelId)
      .then()
      .assertThat()
      .statusCode(200);

    getPtvServiceChannelMocker().verifyServiceLocation(ptvId, ptvInResource);
  }
  
  @Test
  public void updateServiceLocationServiceChannelChanges() throws JsonParseException, JsonMappingException, IOException, InterruptedException {
    String ptvId = TestPtvConsts.SERVICE_LOCATION_SERVICE_CHANNELS[0];
    String organizationId = getOrganizationId(0);
    
    String kuntaApiChannelId = getServiceLocationChannelId(0, TestPtvConsts.SERVICE_LOCATION_SERVICE_CHANNELS.length);
    
    grantOrganizationPermission(AccessType.READ_WRITE, organizationId, ClientOrganizationPermission.UPDATE_SERVICE_CHANNELS);

    ServiceLocationServiceChannel kuntaApiResource = getServiceLocationServiceChannel(0, TestPtvConsts.SERVICE_LOCATION_SERVICE_CHANNELS.length);
    
    kuntaApiResource.setAddresses(createAddresssesAbroad(createLocalizedValue("en", "Far away")));
    kuntaApiResource.setAreas(Arrays.asList(createArea("Municipality", "12345", createLocalizedValue("en", "Examplia"))));
    kuntaApiResource.setAreaType("AreaType");
    kuntaApiResource.setDescriptions(createLocalizedValue("en", "Description", "Changed Description"));
    kuntaApiResource.setEmails(createEmails("en", "fake@example.com"));
    kuntaApiResource.setLanguages(Arrays.asList("en"));
    kuntaApiResource.setNames(createLocalizedValue("en", "Name", "Changed Name"));
    kuntaApiResource.setPhoneNumbers(Arrays.asList(
      createPhone("en", "Phone", "+358", "12345-FAKE", "Charged", "Testing", false, "Test phone"),
      createPhone("en", "Fax", "+258", "54321-FAKE", "Free", "Testing fax", true, "Test fax")
    ));
    kuntaApiResource.setPhoneServiceCharge(false);
    kuntaApiResource.setServiceHours(Arrays.asList(createServiceHour(false, Collections.emptyList(), "Exception", false, null, null, createLocalizedValue("en", "Test"))));
    kuntaApiResource.setWebPages(createWebPages("en", "WebPage", "https://www.example.com", "Example", "Example page"));
    
    V7VmOpenApiServiceLocationChannelInBase ptvInResource = getPtvServiceChannelMocker().readEntity(AbstractPtvMocker.PTV_IN_API, ptvId, V7VmOpenApiServiceLocationChannelInBase.class);
    ptvInResource.setAddresses(createPtvInAddressAbroad(createPtvInLanguageItems("en", "Far away")));
    ptvInResource.setAreas(Arrays.asList(createArea("Municipality", "12345")));
    ptvInResource.setAreaType("AreaType");
    ptvInResource.setServiceChannelDescriptions(createPtvInLocalizedItems("en", "Description", "Changed Description"));
    ptvInResource.setLanguages(Arrays.asList("en"));
    ptvInResource.setServiceChannelNames(createPtvInLanguageItems("en", "Changed Name"));
    ptvInResource.setEmails(createPtvInLanguageItems("en", "fake@example.com"));
    ptvInResource.setPhoneNumbers(createPtvInPhones("en", "Phone", "+358", "12345-FAKE", "Charged", "Testing", false, "Test phone"));
    ptvInResource.setFaxNumbers(createPtvInFaxNumbers("en", "+258", "54321-FAKE", true));
    ptvInResource.setServiceHours(Arrays.asList(creaatePtvInServiceHour(false, Collections.emptyList(), "Exception", false, null, null, createPtvInLanguageItems("en", "Test"))));
    ptvInResource.setWebPages(createPtvInWebPages("en", "https://www.example.com", "Example"));
    
    V7VmOpenApiServiceLocationChannel ptvOutResource =  getPtvServiceChannelMocker().readEntity(AbstractPtvMocker.PTV_OUT_API, ptvId, V7VmOpenApiServiceLocationChannel.class);
    
    getPtvServiceChannelMocker().mockServiceLocationPut(ptvId, ptvOutResource);
    
    givenReadWrite()
      .body(kuntaApiResource)
      .contentType(ContentType.JSON)
      .put("/serviceLocationServiceChannels/{kuntaApiChannelId}",kuntaApiChannelId)
      .then()
      .assertThat()
      .statusCode(200);

    getPtvServiceChannelMocker().verifyServiceLocation(ptvId, ptvInResource);
  }

}
