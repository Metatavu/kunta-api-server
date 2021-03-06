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

import fi.metatavu.kuntaapi.server.rest.model.PhoneServiceChannel;
import fi.metatavu.ptv.client.model.V9VmOpenApiPhoneChannel;
import fi.metatavu.ptv.client.model.V9VmOpenApiPhoneChannelInBase;
import fi.metatavu.kuntaapi.server.integrations.ptv.PtvConsts;
import fi.metatavu.kuntaapi.server.persistence.model.clients.AccessType;
import fi.metatavu.kuntaapi.server.persistence.model.clients.ClientOrganizationPermission;
import fi.metatavu.kuntaapi.test.AbstractPtvMocker;

@SuppressWarnings ("squid:S1075")
public class PhoneServiceChannelInTestsIT extends AbstractPtvInTest {

  private static final String SERVICE_CHANNEL_FIND_PATH = "/phoneServiceChannels/{kuntaApiChannelId}";
  
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
    
    waitApiListCount("/organizations", 3);
  }
  
  @Test
  public void updatePhoneServiceChannelUnauthorized() throws IOException, InterruptedException {
    PhoneServiceChannel kuntaApiResource = getPhoneServiceChannel(0, TestPtvConsts.PHONE_SERVICE_CHANNELS.length);
    
    givenReadonly()
      .body(kuntaApiResource)
      .contentType(ContentType.JSON)
      .put(SERVICE_CHANNEL_FIND_PATH, kuntaApiResource.getId())
      .then()
      .assertThat()
      .statusCode(401);
  }
  
  @Test
  public void updatePhoneServiceChannelForbidden() throws IOException, InterruptedException {
    PhoneServiceChannel kuntaApiResource = getPhoneServiceChannel(0, TestPtvConsts.PHONE_SERVICE_CHANNELS.length);
    
    givenReadWrite()
      .body(kuntaApiResource)
      .contentType(ContentType.JSON)
      .put(SERVICE_CHANNEL_FIND_PATH, kuntaApiResource.getId())
      .then()
      .assertThat()
      .statusCode(403);
  }
  
  @Test
  public void updatePhoneServiceChannelUnchanged() throws IOException, InterruptedException {
    String ptvId = TestPtvConsts.PHONE_SERVICE_CHANNELS[0];
    String organizationId = getOrganizationId(1);
    String kuntaApiChannelId = getPhoneChannelId(0, TestPtvConsts.PHONE_SERVICE_CHANNELS.length);
    
    grantOrganizationPermission(AccessType.READ_WRITE, organizationId, ClientOrganizationPermission.UPDATE_SERVICE_CHANNELS);

    PhoneServiceChannel kuntaApiResource = getPhoneServiceChannel(0, TestPtvConsts.PHONE_SERVICE_CHANNELS.length);
    V9VmOpenApiPhoneChannelInBase ptvInResource = getPtvServiceChannelMocker().readEntity(AbstractPtvMocker.PTV_IN_API, ptvId, V9VmOpenApiPhoneChannelInBase.class);
    V9VmOpenApiPhoneChannel ptvOutResource = getPtvServiceChannelMocker().readEntity(AbstractPtvMocker.PTV_OUT_API, ptvId, V9VmOpenApiPhoneChannel.class);
    
    getPtvServiceChannelMocker().mockPhonePut(ptvId, ptvOutResource);
    
    givenReadWrite()
      .body(kuntaApiResource)
      .contentType(ContentType.JSON)
      .put(SERVICE_CHANNEL_FIND_PATH,kuntaApiChannelId)
      .then()
      .assertThat()
      .statusCode(200);

    getPtvServiceChannelMocker().verifyPhone(ptvId, ptvInResource);
  }
  
  @Test
  public void updatePhoneServiceChannelChanges() throws IOException, InterruptedException {
    String ptvId = TestPtvConsts.PHONE_SERVICE_CHANNELS[0];
    String organizationId = getOrganizationId(1);
    
    String kuntaApiChannelId = getPhoneChannelId(0, TestPtvConsts.PHONE_SERVICE_CHANNELS.length);
    
    grantOrganizationPermission(AccessType.READ_WRITE, organizationId, ClientOrganizationPermission.UPDATE_SERVICE_CHANNELS);

    PhoneServiceChannel kuntaApiResource = getPhoneServiceChannel(0, TestPtvConsts.PHONE_SERVICE_CHANNELS.length);
    
    
    kuntaApiResource.setAreas(Arrays.asList(createArea("Municipality", "12345", createLocalizedValue("en", "Examplia"))));
    kuntaApiResource.setAreaType("AreaType");
    kuntaApiResource.setDescriptions(createLocalizedValue("en", "Description", "Changed Description"));
    kuntaApiResource.setLanguages(Arrays.asList("en"));
    kuntaApiResource.setNames(createLocalizedValue("en", "Name", "Changed Name"));
    kuntaApiResource.setPhoneNumbers(Arrays.asList(createPhone("en", "Phone", "+358", "12345-FAKE", "Charged", "Testing", false, "Test phone")));
    kuntaApiResource.setServiceHours(Arrays.asList(createServiceHour(false, Collections.emptyList(), "Exception", false, null, null, createLocalizedValue("en", "Test"))));
    kuntaApiResource.setSupportEmails(createEmails("en", "fake@example.com"));
    
    V9VmOpenApiPhoneChannelInBase ptvInResource = getPtvServiceChannelMocker().readEntity(AbstractPtvMocker.PTV_IN_API, ptvId, V9VmOpenApiPhoneChannelInBase.class);
    ptvInResource.setAreas(Arrays.asList(createArea("Municipality", "12345")));
    ptvInResource.setAreaType("AreaType");
    ptvInResource.setServiceChannelDescriptions(createPtvInLocalizedItems("en", "Description", "Changed Description"));
    ptvInResource.setLanguages(Arrays.asList("en"));
    ptvInResource.setServiceChannelNames(createPtvInLanguageItems("en", "Changed Name"));
    ptvInResource.setPhoneNumbers(createPtvInPhonesWithTypes("en", "Phone", "+358", "12345-FAKE", "Charged", "Testing", false, "Test phone"));
    ptvInResource.setSupportEmails(createPtvInLanguageItems("en", "fake@example.com"));
    ptvInResource.setDeleteAllServiceHours(true);
    ptvInResource.setDeleteAllWebPages(true);
    ptvInResource.setOrganizationId(TestPtvConsts.ORGANIZATIONS[1]);
    ptvInResource.setPublishingStatus(PtvConsts.PUBLISHED_STATUS);
    ptvInResource.setWebPage(Collections.emptyList());
    ptvInResource.setServiceHours(Arrays.asList(creaatePtvInServiceHour(false, Collections.emptyList(), "Exception", false, null, null, createPtvInLanguageItems("en", "Test"))));
    
    V9VmOpenApiPhoneChannel ptvOutResource =  getPtvServiceChannelMocker().readEntity(AbstractPtvMocker.PTV_OUT_API, ptvId, V9VmOpenApiPhoneChannel.class);
    
    getPtvServiceChannelMocker().mockPhonePut(ptvId, ptvOutResource);
    
    givenReadWrite()
      .body(kuntaApiResource)
      .contentType(ContentType.JSON)
      .put(SERVICE_CHANNEL_FIND_PATH,kuntaApiChannelId)
      .then()
      .assertThat()
      .statusCode(200);

    getPtvServiceChannelMocker().verifyPhone(ptvId, ptvInResource);
  }

}
