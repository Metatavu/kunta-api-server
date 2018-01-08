package fi.otavanopisto.kuntaapi.test.server.integration.ptv;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.jayway.restassured.http.ContentType;

import fi.metatavu.kuntaapi.server.rest.model.PhoneServiceChannel;
import fi.metatavu.ptv.client.model.V7VmOpenApiPhoneChannel;
import fi.metatavu.ptv.client.model.V7VmOpenApiPhoneChannelInBase;
import fi.otavanopisto.kuntaapi.server.persistence.model.clients.AccessType;
import fi.otavanopisto.kuntaapi.server.persistence.model.clients.ClientOrganizationPermission;
import fi.otavanopisto.kuntaapi.test.AbstractPtvMocker;

@SuppressWarnings ("squid:S1075")
public class PhoneServiceChannelInTestsIT extends AbstractPtvInTest {

  private static final String SERVICE_CHANNEL_FIND_PATH = "/phoneServiceChannels/{kuntaApiChannelId}";
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
    waitApiListCount("/phoneServiceChannels", TestPtvConsts.PHONE_SERVICE_CHANNELS.length);
    waitApiListCount("/services", TestPtvConsts.SERVICES.length);
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
    String organizationId = getOrganizationId(0);
    String kuntaApiChannelId = getPhoneChannelId(0, TestPtvConsts.PHONE_SERVICE_CHANNELS.length);
    
    grantOrganizationPermission(AccessType.READ_WRITE, organizationId, ClientOrganizationPermission.UPDATE_SERVICE_CHANNELS);

    PhoneServiceChannel kuntaApiResource = getPhoneServiceChannel(0, TestPtvConsts.PHONE_SERVICE_CHANNELS.length);
    V7VmOpenApiPhoneChannelInBase ptvInResource = getPtvServiceChannelMocker().readEntity(AbstractPtvMocker.PTV_IN_API, ptvId, V7VmOpenApiPhoneChannelInBase.class);
    V7VmOpenApiPhoneChannel ptvOutResource =  getPtvServiceChannelMocker().readEntity(AbstractPtvMocker.PTV_OUT_API, ptvId, V7VmOpenApiPhoneChannel.class);
    
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
    String organizationId = getOrganizationId(0);
    
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
    
    V7VmOpenApiPhoneChannelInBase ptvInResource = getPtvServiceChannelMocker().readEntity(AbstractPtvMocker.PTV_IN_API, ptvId, V7VmOpenApiPhoneChannelInBase.class);
    ptvInResource.setAreas(Arrays.asList(createArea("Municipality", "12345")));
    ptvInResource.setAreaType("AreaType");
    ptvInResource.setServiceChannelDescriptions(createPtvInLocalizedItems("en", "Description", "Changed Description"));
    ptvInResource.setLanguages(Arrays.asList("en"));
    ptvInResource.setServiceChannelNames(createPtvInLanguageItems("en", "Changed Name"));
    ptvInResource.setPhoneNumbers(createPtvInPhonesWithTypes("en", "Phone", "+358", "12345-FAKE", "Charged", "Testing", false, "Test phone"));
    ptvInResource.setSupportEmails(createPtvInLanguageItems("en", "fake@example.com"));
    
    V7VmOpenApiPhoneChannel ptvOutResource =  getPtvServiceChannelMocker().readEntity(AbstractPtvMocker.PTV_OUT_API, ptvId, V7VmOpenApiPhoneChannel.class);
    
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
