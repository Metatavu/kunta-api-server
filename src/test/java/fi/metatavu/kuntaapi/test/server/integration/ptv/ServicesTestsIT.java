package fi.metatavu.kuntaapi.test.server.integration.ptv;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static com.jayway.restassured.config.RedirectConfig.redirectConfig;
import static com.jayway.restassured.config.RestAssuredConfig.newConfig;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.IOException;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.jayway.restassured.http.ContentType;

import fi.metatavu.kuntaapi.test.json.JSONAssertCustomizations;

@SuppressWarnings ("squid:S1192")
public class ServicesTestsIT extends AbstractPtvTest {
  
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

    waitApiListCount("/services", TestPtvConsts.SERVICES.length);
  }

  @Test
  public void findService() throws InterruptedException, JSONException, IOException {
    int serviceIndex = 0;
    
    waitServiceOrganizations(serviceIndex, 2);
    
    String serviceId = getServiceId(serviceIndex, TestPtvConsts.SERVICES.length);
    
    String response = givenReadonly()
      .contentType(ContentType.JSON)
      .get("/services/{serviceId}", serviceId)
      .body().asString();
    
    assertJSONFileEquals(String.format("ptv/kuntaapi/services/%d.json", serviceIndex) , response, 
      JSONAssertCustomizations.notNull("id"),
      JSONAssertCustomizations.equalLength("electronicServiceChannelIds", TestPtvConsts.SERVICE_ELECTRONIC_CHANNEL_CHANNELS[serviceIndex].length),
      JSONAssertCustomizations.equalLength("phoneServiceChannelIds", TestPtvConsts.SERVICE_PHONE_CHANNELS[serviceIndex].length),
      JSONAssertCustomizations.equalLength("printableFormServiceChannelIds", TestPtvConsts.SERVICE_PRINTABLE_FORM_CHANNELS[serviceIndex].length),
      JSONAssertCustomizations.equalLength("serviceLocationServiceChannelIds", TestPtvConsts.SERVICE_SERVICE_LOCATION_CHANNELS[serviceIndex].length),
      JSONAssertCustomizations.equalLength("webPageServiceChannelIds", TestPtvConsts.SERVICE_WEB_PAGE_CHANNELS[serviceIndex].length)
    );
  }

  @Test
  public void findServiceCompability() throws InterruptedException, JSONException, IOException {
    int serviceIndex = 0;
    String serviceId = getServiceId(serviceIndex, TestPtvConsts.SERVICES.length);
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/services/{serviceId}", serviceId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("descriptions[1].type", is("Summary"));

    givenReadonlyCompabilityMode()
      .contentType(ContentType.JSON)
      .get("/services/{serviceId}", serviceId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("descriptions[1].type", is("ShortDescription"));

  }
  
  @Test
  public void testListServices() throws IOException, InterruptedException {
    int serviceIndex = 1;
    
    waitServiceChannels(serviceIndex);
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/services")
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(TestPtvConsts.SERVICES.length))
      .body(String.format("[%d]", serviceIndex), jsonEqualsFile(String.format("ptv/kuntaapi/services/%d.json", serviceIndex), getServiceCustomizations(serviceIndex)));
  }

  @Test
  public void testListServicesCompability() throws IOException, InterruptedException {
    int serviceIndex = 0;
    
    waitServiceChannels(serviceIndex);
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/services")
      .then()
      .assertThat()
      .statusCode(200)
      .body("[0].descriptions[1].type", is("Summary"));

    givenReadonlyCompabilityMode()
      .contentType(ContentType.JSON)
      .get("/services")
      .then()
      .assertThat()
      .statusCode(200)
      .body("[0].descriptions[1].type", is("ShortDescription"));
  }

  @Test
  public void testListServicesByElectronicServiceChannelId() throws InterruptedException {
    int serviceIndex = 11;
    waitServiceChannels(serviceIndex);

    String electronicChannelId1 = getElectronicChannelId(0, TestPtvConsts.ELECTRONIC_CHANNEL_SERVICE_CHANNELS.length);
    String serviceId = getServiceId(serviceIndex, TestPtvConsts.SERVICES.length);
    String invalidElectronicChannelId = "invalid";
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("/services?electronicServiceChannelId=%s", electronicChannelId1))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(1))
      .body("id[0]", is(serviceId))
      .body("electronicServiceChannelIds[0].size()", is(TestPtvConsts.SERVICE_ELECTRONIC_CHANNEL_CHANNELS[serviceIndex].length))
      .body("electronicServiceChannelIds[0][0]", is(electronicChannelId1));
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("/services?electronicServiceChannelId=%s", invalidElectronicChannelId))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(0));
  }
  
  @Test
  public void testListServicesByPhoneServiceChannelId() throws InterruptedException {
    int serviceIndex = 5;
    waitServiceChannels(serviceIndex);
  
    String phoneChannelId1 = getPhoneChannelId(0, TestPtvConsts.PHONE_SERVICE_CHANNELS.length);
    String serviceId = getServiceId(serviceIndex, TestPtvConsts.SERVICES.length);
    String invalidPhoneChannelId = "invalid";
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("/services?phoneServiceChannelId=%s", phoneChannelId1))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(2))
      .body("id[0]", is(serviceId))
      .body("phoneServiceChannelIds[0].size()", is(TestPtvConsts.SERVICE_PHONE_CHANNELS[serviceIndex].length))
      .body("phoneServiceChannelIds[0][1]", is(phoneChannelId1));
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("/services?phoneServiceChannelId=%s", invalidPhoneChannelId))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(0));
  }

  @Test
  public void testListServicesByPrintableFormServiceChannelId() throws InterruptedException {
    int serviceIndex = 22;
    waitServiceChannels(serviceIndex);
    
    String printableFormChannelId = getPrintableFormChannelId(0, TestPtvConsts.PRINTABLE_FORM_SERVICE_CHANNELS.length);
    String serviceId = getServiceId(serviceIndex, TestPtvConsts.SERVICES.length);
    String invalidPrintableFormChannelId = "invalid";
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("/services?printableFormServiceChannelId=%s", printableFormChannelId))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(1))
      .body("id[0]", is(serviceId))
      .body("printableFormServiceChannelIds[0].size()", is(TestPtvConsts.SERVICE_PRINTABLE_FORM_CHANNELS[serviceIndex].length))
      .body("printableFormServiceChannelIds[0][1]", is(printableFormChannelId));
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("/services?printableFormServiceChannelId=%s", invalidPrintableFormChannelId))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(0));
  }

  @Test
  public void testListServicesByServiceLocationServiceChannelId() throws InterruptedException {
    int serviceIndex = 7;
    waitServiceChannels(serviceIndex);
    
    String serviceLocationChannelId = getServiceLocationChannelId(0, TestPtvConsts.SERVICE_LOCATION_SERVICE_CHANNELS.length);
    String serviceId = getServiceId(serviceIndex, TestPtvConsts.SERVICES.length);
    String invalidServiceLocationChannelId = "invalid";
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("/services?serviceLocationServiceChannelId=%s", serviceLocationChannelId))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(1))
      .body("id[0]", is(serviceId))
      .body("serviceLocationServiceChannelIds[0].size()", is(TestPtvConsts.SERVICE_SERVICE_LOCATION_CHANNELS[serviceIndex].length))
      .body("serviceLocationServiceChannelIds[0][0]", is(serviceLocationChannelId));
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("/services?serviceLocationServiceChannelId=%s", invalidServiceLocationChannelId))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(0));
  }

  @Test
  public void testListServicesByWebPageServiceChannelId() throws InterruptedException {
    int serviceIndex = 6;
    waitServiceChannels(serviceIndex);
    
    String webPageChannelId1 = getWebPageChannelId(0, TestPtvConsts.WEB_PAGE_SERVICE_CHANNELS.length);
    String serviceId = getServiceId(serviceIndex, TestPtvConsts.SERVICES.length);
    String invalidWebPageChannelId = "invalid";
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("/services?webPageServiceChannelId=%s", webPageChannelId1))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(1))
      .body("id[0]", is(serviceId))
      .body("webPageServiceChannelIds[0].size()", is(TestPtvConsts.SERVICE_WEB_PAGE_CHANNELS[serviceIndex].length))
      .body("webPageServiceChannelIds[0][0]", is(webPageChannelId1));
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("/services?webPageServiceChannelId=%s", invalidWebPageChannelId))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(0));
  }

  @Test
  public void testListServicesByOrganization() throws InterruptedException {
    String organizationId1 = getOrganizationId(0);
    String organizationId2 = getOrganizationId(1); 
    String invalidOrganizationId = "invalid";
    
    waitOrganizationServices(0);
    waitOrganizationServices(1);
    
    waitForElasticIndex();
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("/services?organizationId=%s", organizationId1))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(TestPtvConsts.ORGANIZATION_SERVICES[0].length))
      .body("organizations[0].size()", is(2))
      .body("organizations[0][0].organizationId", is(organizationId1));
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("/services?organizationId=%s", organizationId2))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(TestPtvConsts.ORGANIZATION_SERVICES[1].length))
      .body("organizations[0].size()", is(2))
      .body("organizations[0][0].organizationId", is(organizationId2));
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("/services?organizationId=%s", invalidOrganizationId))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(0));
  }

  @Test
  public void testListServicesSearch() throws InterruptedException {
    if (skipElasticSearchTests()) {
      return;
    }
    
    String query = "+perusopetus vaasa";

    waitServiceChannels(0);
    waitServiceChannels(1);
    waitServiceChannels(2);
    waitForElasticIndex();
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("/services?search=%s", query))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(2))
      .body("names[0].size()", is(3))
      .body("names[0][2].value", is("Perusopetus"))
      .body("areas[0].municipalities[0].names[0].value[0]", is("Vaasa"))
      .body("names[1].size()", is(1))
      .body("names[1][0].value", is("Perusopetus"))
      .body("areas[1].municipalities[0].names[0].value[0]", is("Naantali"));
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("/services?search=%s&sortBy=SCORE&sortDir=ASC", query))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(2))
      .body("names[1].size()", is(3))
      .body("names[1][2].value", is("Perusopetus"))
      .body("areas[1].municipalities[0].names[0].value[0]", is("Vaasa"))
      .body("names[0].size()", is(1))
      .body("names[0][0].value", is("Perusopetus"))
      .body("areas[0].municipalities[0].names[0].value[0]", is("Naantali"));

    givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("/services?search=%s&sortBy=SCORE&sortDir=DESC", query))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(2))
      .body("names[1].size()", is(1))
      .body("names[1][0].value", is("Perusopetus"))
      .body("areas[1].municipalities[0].names[0].value[0]", is("Naantali"))
      .body("names[0].size()", is(3))
      .body("names[0][2].value", is("Perusopetus"))
      .body("areas[0].municipalities[0].names[0].value[0]", is("Vaasa"));
  }

  @Test
  public void testListServicesLimits() {
    assertListLimits("/services", TestPtvConsts.SERVICES.length);
  }
  
  @Test
  public void testServiceUnarchive() throws InterruptedException {
    String serviceId = getServiceId(TestPtvConsts.SERVICES.length - 1, TestPtvConsts.SERVICES.length);
    assertNotNull(serviceId);
    
    getPtvServiceMocker().unmock(TestPtvConsts.SERVICES[0]);
    
    waitApiListCount("/services", TestPtvConsts.SERVICES.length - 1);
    waitForElasticIndex();
    
    assertNull(getServiceId(TestPtvConsts.SERVICES.length - 1, TestPtvConsts.SERVICES.length - 1));
    
    getPtvServiceMocker().mock(TestPtvConsts.SERVICES[0]);
    waitApiListCount("/services", TestPtvConsts.SERVICES.length);
    waitForElasticIndex();
    
    assertEquals(serviceId, getServiceId(TestPtvConsts.SERVICES.length - 1, TestPtvConsts.SERVICES.length));
  }
  
  @Test
  public void testServiceChannelIds() throws InterruptedException  {
    int serviceIndex = 0;
    
    waitServiceChannels(serviceIndex);
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/services/{serviceId}", getServiceId(serviceIndex, TestPtvConsts.SERVICES.length))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id", notNullValue())
      .body("electronicServiceChannelIds.size()", is(TestPtvConsts.SERVICE_ELECTRONIC_CHANNEL_CHANNELS[serviceIndex].length))
      .body("phoneServiceChannelIds.size()", is(TestPtvConsts.SERVICE_PHONE_CHANNELS[serviceIndex].length))
      .body("printableFormServiceChannelIds.size()", is(TestPtvConsts.SERVICE_PRINTABLE_FORM_CHANNELS[serviceIndex].length))
      .body("serviceLocationServiceChannelIds.size()", is(TestPtvConsts.SERVICE_SERVICE_LOCATION_CHANNELS[serviceIndex].length))
      .body("webPageServiceChannelIds.size()", is(TestPtvConsts.SERVICE_WEB_PAGE_CHANNELS[serviceIndex].length));
  }

  @Test
  public void testServiceChannelRedirects() {
    testServiceChannelRedirect("electronicChannels", "electronicServiceChannels");
    testServiceChannelRedirect("phoneChannels", "phoneServiceChannels");
    testServiceChannelRedirect("printableFormChannels", "printableFormServiceChannels");
    testServiceChannelRedirect("serviceLocationChannels", "serviceLocationServiceChannels");
    testServiceChannelRedirect("webPageChannels", "webPageServiceChannels");
  }
  
  private void testServiceChannelRedirect(String from, String to) {
    String fromListPath = String.format("/services/SERVICE_ID/%s", from);
    String fromFindPath = String.format("%s/CHANNEL_ID", fromListPath);
    String toListPath = String.format("/%s", to);
    String toFindPath = String.format("%s/%s", toListPath, "CHANNEL_ID");

    givenReadonly()
      .contentType(ContentType.JSON)
      .config(newConfig().redirect(redirectConfig().followRedirects(false)))
      .get(fromListPath)
      .then()
      .assertThat()
      .statusCode(307)
      .header("Location", String.format("%s%s", getApiBasePath(), toListPath));
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .config(newConfig().redirect(redirectConfig().followRedirects(false)))
      .get(fromFindPath)
      .then()
      .assertThat()
      .statusCode(307)
      .header("Location", String.format("%s%s", getApiBasePath(), toFindPath));
  }

}
