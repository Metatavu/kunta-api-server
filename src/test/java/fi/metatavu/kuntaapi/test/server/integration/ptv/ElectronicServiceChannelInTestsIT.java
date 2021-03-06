package fi.metatavu.kuntaapi.test.server.integration.ptv;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.jayway.restassured.http.ContentType;

import fi.metatavu.kuntaapi.server.rest.model.ElectronicServiceChannel;
import fi.metatavu.ptv.client.model.V9VmOpenApiElectronicChannelInBase;
import fi.metatavu.ptv.client.model.V9VmOpenApiElectronicChannel;
import fi.metatavu.kuntaapi.server.persistence.model.clients.AccessType;
import fi.metatavu.kuntaapi.server.persistence.model.clients.ClientOrganizationPermission;
import fi.metatavu.kuntaapi.test.AbstractPtvMocker;

@SuppressWarnings ("squid:S1075")
public class ElectronicServiceChannelInTestsIT extends AbstractPtvInTest {

  private static final String WWW_EXAMPLE_COM = "www.example.com";
  private static final String SERVICE_CHANNEL_FIND_PATH = "/electronicServiceChannels/{kuntaApiChannelId}";

  /**
   * Starts WireMock
   */
  @Rule
  public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(getWireMockPort()), false);
  
  @Before
  public void beforeTest() throws InterruptedException {
    getPtvOrganizationMocker().mock(TestPtvConsts.ORGANIZATIONS);
    getPtvServiceMocker().mock(TestPtvConsts.SERVICES);    
    getPtvServiceChannelMocker().mock(TestPtvConsts.SERVICE_CHANNELS);

    startMocks();
    
    waitApiListCount("/organizations", TestPtvConsts.ORGANIZATIONS.length);
  }

  @Test
  public void updateElectronicServiceChannelUnauthorized() throws IOException, InterruptedException {
    ElectronicServiceChannel kuntaApiResource = getElectronicServiceChannel(0, TestPtvConsts.ELECTRONIC_CHANNEL_SERVICE_CHANNELS.length);
    
    givenReadonly()
      .body(kuntaApiResource)
      .contentType(ContentType.JSON)
      .put(SERVICE_CHANNEL_FIND_PATH, kuntaApiResource.getId())
      .then()
      .assertThat()
      .statusCode(401);
  }

  @Test
  public void updateElectronicServiceChannelForbidden() throws IOException, InterruptedException {
    ElectronicServiceChannel kuntaApiResource = getElectronicServiceChannel(0, TestPtvConsts.ELECTRONIC_CHANNEL_SERVICE_CHANNELS.length);
    
    givenReadWrite()
      .body(kuntaApiResource)
      .contentType(ContentType.JSON)
      .put(SERVICE_CHANNEL_FIND_PATH, kuntaApiResource.getId())
      .then()
      .assertThat()
      .statusCode(403);
  }  

  @Test
  public void updateElectronicServiceChannelUnchanged() throws IOException, InterruptedException {
    String ptvId = TestPtvConsts.ELECTRONIC_CHANNEL_SERVICE_CHANNELS[0];
    String organizationId = getOrganizationId(2);
    String kuntaApiChannelId = getElectronicChannelId(0, TestPtvConsts.ELECTRONIC_CHANNEL_SERVICE_CHANNELS.length);
    
    grantOrganizationPermission(AccessType.READ_WRITE, organizationId, ClientOrganizationPermission.UPDATE_SERVICE_CHANNELS);

    ElectronicServiceChannel kuntaApiResource = getElectronicServiceChannel(0, TestPtvConsts.ELECTRONIC_CHANNEL_SERVICE_CHANNELS.length);
    V9VmOpenApiElectronicChannelInBase ptvInResource = getPtvServiceChannelMocker().readEntity(AbstractPtvMocker.PTV_IN_API, ptvId, V9VmOpenApiElectronicChannelInBase.class);
    V9VmOpenApiElectronicChannel ptvOutResource = getPtvServiceChannelMocker().readEntity(AbstractPtvMocker.PTV_OUT_API, ptvId, V9VmOpenApiElectronicChannel.class);
    
    getPtvServiceChannelMocker().mockElectronicPut(ptvId, ptvOutResource);
    
    givenReadWrite()
      .body(kuntaApiResource)
      .contentType(ContentType.JSON)
      .put(SERVICE_CHANNEL_FIND_PATH, kuntaApiChannelId)
      .then()
      .assertThat()
      .statusCode(200);

    getPtvServiceChannelMocker().verifyElectronic(ptvId, ptvInResource);
  }

  @Test
  public void updateElectronicServiceChannelChanges() throws IOException, InterruptedException {
    String ptvId = TestPtvConsts.ELECTRONIC_CHANNEL_SERVICE_CHANNELS[0];
    String organizationId = getOrganizationId(2);
    String kuntaApiChannelId = getElectronicChannelId(0, TestPtvConsts.ELECTRONIC_CHANNEL_SERVICE_CHANNELS.length);
    
    grantOrganizationPermission(AccessType.READ_WRITE, organizationId, ClientOrganizationPermission.UPDATE_SERVICE_CHANNELS);

    ElectronicServiceChannel kuntaApiResource = getElectronicServiceChannel(0, TestPtvConsts.ELECTRONIC_CHANNEL_SERVICE_CHANNELS.length);
    
    kuntaApiResource.setAreaType("AreaType");
    kuntaApiResource.setAreas(Arrays.asList(createArea("Municipality", "12345", createLocalizedValue("en", "Examplia"))));
    kuntaApiResource.setAttachments(createAttachments("en", "PDF", "https://www.example.com", "Example PDF", "PDF file for testing"));
    kuntaApiResource.setDescriptions(createLocalizedValue("en", "Description", "Changed Description"));
    kuntaApiResource.setNames(createLocalizedValue("en", "Name", "Changed Name"));
    kuntaApiResource.setRequiresAuthentication(true);
    kuntaApiResource.setRequiresSignature(true);
    kuntaApiResource.setServiceHours(Arrays.asList(createServiceHour(false, Collections.emptyList(), "Exception", false, null, null, createLocalizedValue("en", "Test"))));
    kuntaApiResource.setSignatureQuantity(22);
    kuntaApiResource.setSupportEmails(createEmails("en", "fake@example.com"));
    kuntaApiResource.setSupportPhones(Arrays.asList(
      createPhone("en", "Phone", "+358", "12345-FAKE", "Charged", "Testing", false, "Test phone"),
      createPhone("en", "Fax", "+258", "54321-FAKE", "Free", "Testing fax", true, "Test fax")
    ));
    kuntaApiResource.setWebPages(createWebPages("en", "WebPage", WWW_EXAMPLE_COM, WWW_EXAMPLE_COM, null));
    
    V9VmOpenApiElectronicChannelInBase ptvInResource = getPtvServiceChannelMocker().readEntity(AbstractPtvMocker.PTV_IN_API, ptvId, V9VmOpenApiElectronicChannelInBase.class);
    ptvInResource.setAreas(Arrays.asList(createArea("Municipality", "12345")));
    ptvInResource.setAreaType("AreaType");
    ptvInResource.setAttachments(createPtvInAttachments("en", "https://www.example.com", "Example PDF", "PDF file for testing"));
    ptvInResource.setServiceChannelDescriptions(createPtvInLocalizedItems("en", "Description", "Changed Description"));
    ptvInResource.setServiceChannelNames(createPtvInLanguageItems("en", "Changed Name"));
    ptvInResource.setRequiresAuthentication(true);
    ptvInResource.setRequiresSignature(true);
    ptvInResource.setServiceHours(Arrays.asList(creaatePtvInServiceHour(false, Collections.emptyList(), "Exception", false, null, null, createPtvInLanguageItems("en", "Test"))));
    ptvInResource.setSignatureQuantity("22");
    ptvInResource.setSupportEmails(createPtvInLanguageItems("en", "fake@example.com"));
    ptvInResource.setSupportPhones(createPtvInPhones("en", "+358", "12345-FAKE", "Charged", "Testing", false, "Test phone"));
    ptvInResource.setWebPage(createPtvInLanguageItems("en", WWW_EXAMPLE_COM));
    
    V9VmOpenApiElectronicChannel ptvOutResource =  getPtvServiceChannelMocker().readEntity(AbstractPtvMocker.PTV_OUT_API, ptvId, V9VmOpenApiElectronicChannel.class);
    
    getPtvServiceChannelMocker().mockElectronicPut(ptvId, ptvOutResource);
    
    givenReadWrite()
      .body(kuntaApiResource)
      .contentType(ContentType.JSON)
      .put(SERVICE_CHANNEL_FIND_PATH,kuntaApiChannelId)
      .then()
      .assertThat()
      .statusCode(200);

    getPtvServiceChannelMocker().verifyElectronic(ptvId, ptvInResource);
  }

}
