package fi.metatavu.kuntaapi.test.server.integration.ptv;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.jayway.restassured.http.ContentType;

import fi.metatavu.kuntaapi.server.rest.model.Service;
import fi.metatavu.ptv.client.model.V9VmOpenApiService;
import fi.metatavu.ptv.client.model.V9VmOpenApiServiceAndChannelRelationInBase;
import fi.metatavu.ptv.client.model.V9VmOpenApiServiceInBase;
import fi.metatavu.ptv.client.model.V9VmOpenApiServiceServiceChannelInBase;
import fi.metatavu.kuntaapi.server.persistence.model.clients.AccessType;
import fi.metatavu.kuntaapi.server.persistence.model.clients.ClientOrganizationPermission;
import fi.metatavu.kuntaapi.test.AbstractPtvMocker;

@SuppressWarnings ("squid:S1075")
public class ServiceInTestsIT extends AbstractPtvInTest {

  private static final String SERVICE_FIND_PATH = "/services/{kuntaApiId}";
  
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
    waitApiListCount("/electronicServiceChannels", TestPtvConsts.ELECTRONIC_CHANNEL_SERVICE_CHANNELS.length);
    waitApiListCount("/phoneServiceChannels", TestPtvConsts.PHONE_SERVICE_CHANNELS.length);
    waitApiListCount("/printableFormServiceChannels", TestPtvConsts.PRINTABLE_FORM_SERVICE_CHANNELS.length);
    waitApiListCount("/serviceLocationServiceChannels", TestPtvConsts.SERVICE_LOCATION_SERVICE_CHANNELS.length);
    waitApiListCount("/webPageServiceChannels", TestPtvConsts.WEB_PAGE_SERVICE_CHANNELS.length);
    waitApiListCount("/services", TestPtvConsts.SERVICES.length);
  }
  
  @Test
  public void updateServiceUnauthorized() throws IOException, InterruptedException {
    Service kuntaApiResource = getService(0, TestPtvConsts.SERVICES.length);
    givenReadonly()
      .body(kuntaApiResource)
      .contentType(ContentType.JSON)
      .put(SERVICE_FIND_PATH, kuntaApiResource.getId())
      .then()
      .assertThat()
      .statusCode(401);
  }
  
  @Test
  public void updateServiceForbidden() throws IOException, InterruptedException {
    Service kuntaApiResource = getService(0, TestPtvConsts.SERVICES.length);
    givenReadWrite()
      .body(kuntaApiResource)
      .contentType(ContentType.JSON)
      .put(SERVICE_FIND_PATH, kuntaApiResource.getId())
      .then()
      .assertThat()
      .statusCode(403);
  }
  
  @Test
  public void updateServiceUnchanged() throws IOException, InterruptedException {
    String ptvId = TestPtvConsts.SERVICES[0];
    String organizationId = getOrganizationId(0);
    
    grantOrganizationPermission(AccessType.READ_WRITE, organizationId, ClientOrganizationPermission.UPDATE_SERVICES);

    Service kuntaApiResource = getService(0, TestPtvConsts.SERVICES.length);
    V9VmOpenApiServiceInBase ptvInResource = getPtvServiceMocker().readEntity(AbstractPtvMocker.PTV_IN_API, ptvId, V9VmOpenApiServiceInBase.class);
    V9VmOpenApiService ptvOutResource =  getPtvServiceMocker().readEntity(AbstractPtvMocker.PTV_OUT_API, ptvId, V9VmOpenApiService.class);
    
    getPtvServiceMocker().mockServicePut(ptvId, ptvOutResource);
    
    givenReadWrite()
      .body(kuntaApiResource)
      .contentType(ContentType.JSON)
      .put(SERVICE_FIND_PATH, kuntaApiResource.getId())
      .then()
      .assertThat()
      .statusCode(200);
    
    getPtvServiceMocker().verifyService(ptvId, ptvInResource);
  }
  
  @Test
  public void updateServiceChannels() throws IOException, InterruptedException {
    String ptvServiceId = TestPtvConsts.SERVICES[0];
    String organizationId = getOrganizationId(0);
    
    grantOrganizationPermission(AccessType.READ_WRITE, organizationId, ClientOrganizationPermission.UPDATE_SERVICES);

    Service kuntaApiResource = getService(0, TestPtvConsts.SERVICES.length);
    
    kuntaApiResource.setElectronicServiceChannelIds(Arrays.asList(getElectronicChannelId(0, TestPtvConsts.ELECTRONIC_CHANNEL_SERVICE_CHANNELS.length)));
    kuntaApiResource.setPhoneServiceChannelIds(Arrays.asList(getPhoneChannelId(0, TestPtvConsts.PHONE_SERVICE_CHANNELS.length)));
    kuntaApiResource.setPrintableFormServiceChannelIds(Arrays.asList(getPrintableFormChannelId(0, TestPtvConsts.PRINTABLE_FORM_SERVICE_CHANNELS.length)));
    kuntaApiResource.setServiceLocationServiceChannelIds(Arrays.asList(getServiceLocationChannelId(0, TestPtvConsts.SERVICE_LOCATION_SERVICE_CHANNELS.length)));
    kuntaApiResource.setWebPageServiceChannelIds(Arrays.asList(getWebPageChannelId(0, TestPtvConsts.WEB_PAGE_SERVICE_CHANNELS.length)));

    V9VmOpenApiService ptvOutResource =  getPtvServiceMocker().readEntity(AbstractPtvMocker.PTV_OUT_API, ptvServiceId, V9VmOpenApiService.class);
    
    getPtvServiceMocker().mockServicePut(ptvServiceId, ptvOutResource);
    
    givenReadWrite()
      .body(kuntaApiResource)
      .contentType(ContentType.JSON)
      .put(SERVICE_FIND_PATH, kuntaApiResource.getId())
      .then()
      .assertThat()
      .statusCode(200);
    
    verifyChannelRequest(ptvServiceId, 
      TestPtvConsts.ELECTRONIC_CHANNEL_SERVICE_CHANNELS[0],
      TestPtvConsts.PHONE_SERVICE_CHANNELS[0],
      TestPtvConsts.PRINTABLE_FORM_SERVICE_CHANNELS[0],
      TestPtvConsts.SERVICE_LOCATION_SERVICE_CHANNELS[0],
      TestPtvConsts.WEB_PAGE_SERVICE_CHANNELS[0]
    );
  }
  
  private void verifyChannelRequest(String ptvServiceId, String... ptvServiceChannelIds) {
    V9VmOpenApiServiceAndChannelRelationInBase ptvChannelRequest = new V9VmOpenApiServiceAndChannelRelationInBase();
    
    for (String ptvServiceChannelId : ptvServiceChannelIds) {
      V9VmOpenApiServiceServiceChannelInBase channelRelation = new V9VmOpenApiServiceServiceChannelInBase();
      channelRelation.setServiceChannelId(ptvServiceChannelId);
      channelRelation.setDeleteAllDescriptions(false);
      channelRelation.setDeleteAllServiceHours(false);
      channelRelation.setDeleteServiceChargeType(false);
      ptvChannelRequest.addChannelRelationsItem(channelRelation);
    }
    
    ptvChannelRequest.setDeleteAllChannelRelations(true);
    getPtvServiceMocker().verifyServiceConnection(ptvServiceId, ptvChannelRequest);
  }
  
  @Test
  public void updateServiceChanges() throws IOException, InterruptedException {
    String ptvId = TestPtvConsts.SERVICES[0];
    String organizationId = getOrganizationId(0);
    
    grantOrganizationPermission(AccessType.READ_WRITE, organizationId, ClientOrganizationPermission.UPDATE_SERVICES);

    Service kuntaApiResource = getService(0, TestPtvConsts.SERVICES.length);
    kuntaApiResource.setAreas(Arrays.asList(createArea("Municipality", "12345", createLocalizedValue("en", "Examplia"))));
    kuntaApiResource.setAreaType("AreaType");
    kuntaApiResource.setChargeType("Charged");
    kuntaApiResource.setDescriptions(createLocalizedValue("en", "Description", "Changed Description"));
    kuntaApiResource.setFundingType("PrivateFunded");
    kuntaApiResource.setIndustrialClasses(createOntologyItems("https://example.com/Industrial"));
    kuntaApiResource.setKeywords(createLocalizedValue("en", "keyword"));
    kuntaApiResource.setLanguages(Arrays.asList("en"));
    kuntaApiResource.setLegislation(createLaw(createLocalizedValue("en", "Test Law"), "https://www.exmapl.com/law", "Example", "Example law"));
    kuntaApiResource.setLifeEvents(createOntologyItems("https://example.com/Life"));
    kuntaApiResource.setNames(createLocalizedValue("en", "Name", "Changed Name"));
    kuntaApiResource.setOntologyTerms(createOntologyItems("https://example.com/Ontology"));
    kuntaApiResource.setRequirements(createLocalizedValue("en", "Requirement"));
    kuntaApiResource.setServiceClasses(createOntologyItems("https://example.com/Service"));
    kuntaApiResource.setTargetGroups(createOntologyItems("https://example.com/Target"));
    kuntaApiResource.setType("PermissionAndObligation");
    kuntaApiResource.setVouchers(createServiceVouchers("en", "Voucher", "https://www.example.com/voucher", "Test voucher"));
    
    V9VmOpenApiServiceInBase ptvInResource = getPtvServiceMocker().readEntity(AbstractPtvMocker.PTV_IN_API, ptvId, V9VmOpenApiServiceInBase.class);
    ptvInResource.setAreas(Arrays.asList(createArea("Municipality", "12345")));
    ptvInResource.setAreaType("AreaType");
    ptvInResource.setServiceChargeType("Charged");
    ptvInResource.setFundingType("PrivateFunded");
    ptvInResource.setServiceDescriptions(createPtvInLocalizedItems("en", "Description", "Changed Description"));
    ptvInResource.setIndustrialClasses(Arrays.asList( "https://example.com/Industrial"));
    ptvInResource.setKeywords(createPtvInLanguageItems("en", "keyword"));
    ptvInResource.setLanguages(Arrays.asList("en"));
    ptvInResource.setLegislation(createPtvInLaw(createPtvInLanguageItems("en", "Test Law"), "https://www.exmapl.com/law", "Example"));
    ptvInResource.setLifeEvents(Arrays.asList( "https://example.com/Life"));
    ptvInResource.setServiceNames(createPtvInLocalizedItems("en", "Name", "Changed Name"));
    ptvInResource.setOntologyTerms(Arrays.asList( "https://example.com/Ontology"));
    ptvInResource.setRequirements(createPtvInLanguageItems("en", "Requirement"));
    ptvInResource.setServiceClasses(Arrays.asList( "https://example.com/Service"));
    ptvInResource.setTargetGroups(Arrays.asList( "https://example.com/Target"));
    ptvInResource.setType("PermissionAndObligation");
    ptvInResource.setServiceVouchers(createPtvServiceVouchers("en", "Voucher", "https://www.example.com/voucher", "Test voucher"));
    ptvInResource.setServiceVouchersInUse(true);
    
    V9VmOpenApiService ptvOutResource =  getPtvServiceMocker().readEntity(AbstractPtvMocker.PTV_OUT_API, ptvId, V9VmOpenApiService.class);
    
    getPtvServiceMocker().mockServicePut(ptvId, ptvOutResource);
    
    givenReadWrite()
      .body(kuntaApiResource)
      .contentType(ContentType.JSON)
      .put(SERVICE_FIND_PATH, kuntaApiResource.getId())
      .then()
      .assertThat()
      .statusCode(200);

    getPtvServiceMocker().verifyService(ptvId, ptvInResource);
  }

}
