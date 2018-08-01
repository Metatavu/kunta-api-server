package fi.metatavu.kuntaapi.test.server.integration.ptv;

import static com.jayway.restassured.config.RedirectConfig.redirectConfig;
import static com.jayway.restassured.config.RestAssuredConfig.newConfig;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.jayway.restassured.http.ContentType;

import fi.metatavu.kuntaapi.test.AbstractIntegrationTest;

@SuppressWarnings ("squid:S1192")
public class ServicesTestsIT extends AbstractIntegrationTest {
  
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
    
    waitApiListCount("/electronicServiceChannels", TestPtvConsts.ELECTRONIC_CHANNEL_SERVICE_CHANNELS.length);
    waitApiListCount("/phoneServiceChannels", TestPtvConsts.PHONE_SERVICE_CHANNELS.length);
    waitApiListCount("/printableFormServiceChannels", TestPtvConsts.PRINTABLE_FORM_SERVICE_CHANNELS.length);
    waitApiListCount("/serviceLocationServiceChannels", TestPtvConsts.SERVICE_LOCATION_SERVICE_CHANNELS.length);
    waitApiListCount("/webPageServiceChannels", TestPtvConsts.WEB_PAGE_SERVICE_CHANNELS.length);
    
    waitApiListCount("/services", TestPtvConsts.SERVICES.length);
  }
  
  @Test
  public void findService() {
    String id = givenReadonly()
        .contentType(ContentType.JSON)
        .get("/services")
        .body().jsonPath().getString("id[0]");
        
    assertNotNull(id);
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/services/{serviceId}", id)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id", notNullValue())
      .body("type", is("Service"))
      .body("statutoryDescriptionId", nullValue())
      .body("serviceClasses.size()", is(1))
      .body("serviceClasses[0].name.size()", is(3))
      .body("serviceClasses[0].name[0].value", is("Eläkkeet"))
      .body("serviceClasses[0].name[0].language", is("fi"))
      .body("serviceClasses[0].code", is("P13"))
      .body("serviceClasses[0].ontologyType", is("PTVL"))
      .body("serviceClasses[0].uri", is("http://urn.fi/URN:NBN:fi:au:ptvl:v1120"))
      .body("serviceClasses[0].parentId", nullValue())
      .body("serviceClasses[0].parentUri", is(""))

      .body("ontologyTerms.size()", is(1))
      .body("ontologyTerms[0].name.size()", is(3))
      .body("ontologyTerms[0].name[0].value", is("DNA-testit"))
      .body("ontologyTerms[0].name[0].language", is("fi"))
      .body("ontologyTerms[0].name[1].value", is("dna-test"))
      .body("ontologyTerms[0].name[1].language", is("sv"))
      .body("ontologyTerms[0].code", is(""))
      .body("ontologyTerms[0].ontologyType", is("YSO"))
      .body("ontologyTerms[0].uri", is("http://www.yso.fi/onto/koko/p64557"))
      .body("ontologyTerms[0].parentId", nullValue())
      .body("ontologyTerms[0].parentUri", is("http://www.yso.fi/onto/koko/p10657"))

      .body("targetGroups.size()", is(1))
      .body("targetGroups[0].name.size()", is(3))
      .body("targetGroups[0].name[0].value", is("Businesses and non-government organizations"))
      .body("targetGroups[0].name[0].language", is("en"))
      .body("targetGroups[0].code", is("KR2"))
      .body("targetGroups[0].ontologyType", is("TARGETGROUP"))
      .body("targetGroups[0].uri", is("http://urn.fi/URN:NBN:fi:au:ptvl:v2008"))
      .body("targetGroups[0].parentId", nullValue())
      .body("targetGroups[0].parentUri", is(""))
      
      .body("lifeEvents.size()", is(1))
      .body("lifeEvents[0].name.size()", is(3))
      .body("lifeEvents[0].name[1].value", is("Värnplikt"))
      .body("lifeEvents[0].name[1].language", is("sv"))
      .body("lifeEvents[0].code", is("KE2"))
      .body("lifeEvents[0].ontologyType", is("LIFESITUATION"))
      .body("lifeEvents[0].uri", is("http://urn.fi/URN:NBN:fi:au:ptvl:v3008"))
      .body("lifeEvents[0].parentId", nullValue())
      .body("lifeEvents[0].parentUri", is(""))
      
      .body("industrialClasses.size()", is(1))
      .body("industrialClasses[0].name.size()", is(3))
      .body("industrialClasses[0].name[0].value", is("Aikakauslehtien kustantaminen"))
      .body("industrialClasses[0].name[0].language", is("fi"))
      .body("industrialClasses[0].code", is("5"))
      .body("industrialClasses[0].ontologyType", nullValue())
      .body("industrialClasses[0].uri", is("http://www.stat.fi/meta/luokitukset/toimiala/001-2008/58142"))
      .body("industrialClasses[0].parentId", is("f82cd02c-a5e4-4624-bebe-a9afe66a776b"))
      .body("industrialClasses[0].parentUri", is("http://www.stat.fi/meta/luokitukset/toimiala/001-2008/5814"))

      .body("names.size()", is(1))
      .body("names[0].language", is("fi"))
      .body("names[0].value", is("Metatavu testaa"))
      .body("names[0].type", is("Name"))

      .body("descriptions.size()", is(2))
      .body("descriptions[0].language", is("fi"))
      .body("descriptions[0].value", is("xxKuvaus"))
      .body("descriptions[0].type", is("Description"))
      
      .body("languages.size()", is(1))
      .body("languages[0]", is("fi"))
      .body("keywords.size()", is(0))
      .body("legislation.size()", is(1))
      .body("legislation[0].names.size()", is(1))
      .body("legislation[0].names[0].value", is("Korkein oikeus"))
      .body("legislation[0].names[0].language", is("fi"))
      .body("legislation[0].webPages.size()", is(0))
      .body("areas.size()", is(0))
      .body("areaType", is("WholeCountry"))
      .body("requirements.size()", is(0))
      .body("publishingStatus", is("Published"))
      .body("chargeType", is("Free"))
      .body("organizations.size()", is(2))
      .body("organizations[0].additionalInformation.size()", is(0))
      .body("organizations[0].organizationId", notNullValue())
      .body("organizations[0].roleType", is("Responsible"))
      .body("organizations[0].provisionType", nullValue())
      .body("electronicServiceChannelIds.size()", is(0))
      .body("phoneServiceChannelIds.size()", is(1))
      .body("printableFormServiceChannelIds.size()", is(0))
      .body("serviceLocationServiceChannelIds.size()", is(1))
      .body("webPageServiceChannelIds.size()", is(0));
  }
  
  @Test
  public void testListServices() {
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/services")
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(TestPtvConsts.SERVICES.length))
      .body("type[1]", is("Service"))
      .body("statutoryDescriptionId[1]", nullValue())
      .body("serviceClasses[1].size()", is(1))
      .body("serviceClasses[1][0].name.size()", is(3))
      .body("serviceClasses[1][0].name[0].value", is("Asuminen"))
      .body("serviceClasses[1][0].name[0].language", is("fi"))
      .body("serviceClasses[1][0].code", is("P1"))
      .body("serviceClasses[1][0].ontologyType", is("PTVL"))
      .body("serviceClasses[1][0].uri", is("http://urn.fi/URN:NBN:fi:au:ptvl:v1001"))
      .body("serviceClasses[1][0].parentId", nullValue())
      .body("serviceClasses[1][0].parentUri", is(""))

      .body("ontologyTerms[1].size()", is(1))
      .body("targetGroups[1].size()", is(1))
      .body("lifeEvents[1].size()", is(1))
      .body("industrialClasses[1].size()", is(0))
      
      .body("names[1].size()", is(1))
      .body("names[1][0].language", is("fi"))
      .body("names[1][0].value", is("Uusi testipalvelu"))
      .body("names[1][0].type", is("Name"))

      .body("descriptions[1].size()", is(2))

      .body("languages[1].size()", is(1))
      .body("languages[1][0]", is("fi"))
      .body("keywords[1].size()", is(0))
      .body("legislation[1].size()", is(0))
      .body("areas[1].size()", is(0))
      .body("areaType[1]", is("WholeCountry"))
      .body("requirements[1].size()", is(0))
      .body("publishingStatus[1]", is("Published"))
      .body("chargeType[1]", nullValue())

      .body("organizations[1].size()", is(2))
      .body("organizations[1][0].additionalInformation.size()", is(0))
      .body("organizations[1][0].organizationId", notNullValue())
      .body("organizations[1][0].roleType", is("Responsible"))
      .body("organizations[1][0].provisionType", nullValue())
      .body("organizations[1][0].webPages.size()", is(0))
      
      .body("electronicServiceChannelIds[1].size()", is(0))
      .body("phoneServiceChannelIds[1].size()", is(0))
      .body("printableFormServiceChannelIds[1].size()", is(0))
      .body("serviceLocationServiceChannelIds[1].size()", is(1))
      .body("webPageServiceChannelIds[1].size()", is(0));
  }

  @Test
  public void testListServicesByElectronicServiceChannelId() throws InterruptedException {
    String electronicChannelId1 = getElectronicChannelId(0, TestPtvConsts.ELECTRONIC_CHANNEL_SERVICE_CHANNELS.length);
    String electronicChannelId2 = getElectronicChannelId(1, TestPtvConsts.ELECTRONIC_CHANNEL_SERVICE_CHANNELS.length);
    String serviceId = getServiceId(6, TestPtvConsts.SERVICES.length);
    String invalidElectronicChannelId = "invalid";
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("/services?electronicServiceChannelId=%s", electronicChannelId1))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(1))
      .body("id[0]", is(serviceId))
      .body("electronicServiceChannelIds[0].size()", is(2))
      .body("electronicServiceChannelIds[0][0]", is(electronicChannelId2))
      .body("electronicServiceChannelIds[0][1]", is(electronicChannelId1));
    
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
    String phoneChannelId1 = getPhoneChannelId(0, TestPtvConsts.PHONE_SERVICE_CHANNELS.length);
    String serviceId = getServiceId(6, TestPtvConsts.SERVICES.length);
    String invalidPhoneChannelId = "invalid";
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("/services?phoneServiceChannelId=%s", phoneChannelId1))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(1))
      .body("id[0]", is(serviceId))
      .body("phoneServiceChannelIds[0].size()", is(1))
      .body("phoneServiceChannelIds[0][0]", is(phoneChannelId1));
    
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
    String printableFormChannelId1 = getPrintableFormChannelId(0, TestPtvConsts.PRINTABLE_FORM_SERVICE_CHANNELS.length);
    String printableFormChannelId2 = getPrintableFormChannelId(1, TestPtvConsts.PRINTABLE_FORM_SERVICE_CHANNELS.length);
    String serviceId = getServiceId(6, TestPtvConsts.SERVICES.length);
    String invalidPrintableFormChannelId = "invalid";
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("/services?printableFormServiceChannelId=%s", printableFormChannelId1))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(1))
      .body("id[0]", is(serviceId))
      .body("printableFormServiceChannelIds[0].size()", is(2))
      .body("printableFormServiceChannelIds[0][0]", is(printableFormChannelId1))
      .body("printableFormServiceChannelIds[0][1]", is(printableFormChannelId2));
    
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
    String serviceLocationChannelId1 = getServiceLocationChannelId(0, TestPtvConsts.SERVICE_LOCATION_SERVICE_CHANNELS.length);
    String serviceLocationChannelId8 = getServiceLocationChannelId(8, TestPtvConsts.SERVICE_LOCATION_SERVICE_CHANNELS.length);
    String serviceLocationChannelId9 = getServiceLocationChannelId(9, TestPtvConsts.SERVICE_LOCATION_SERVICE_CHANNELS.length);
    String serviceId = getServiceId(5, TestPtvConsts.SERVICES.length);
    String invalidServiceLocationChannelId = "invalid";
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("/services?serviceLocationServiceChannelId=%s", serviceLocationChannelId1))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(1))
      .body("id[0]", is(serviceId))
      .body("serviceLocationServiceChannelIds[0].size()", is(3))
      .body("serviceLocationServiceChannelIds[0][0]", is(serviceLocationChannelId1))
      .body("serviceLocationServiceChannelIds[0][1]", is(serviceLocationChannelId9))
      .body("serviceLocationServiceChannelIds[0][2]", is(serviceLocationChannelId8));
    
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
    String webPageChannelId1 = getWebPageChannelId(0, TestPtvConsts.WEB_PAGE_SERVICE_CHANNELS.length);
    String serviceId = getServiceId(6, TestPtvConsts.SERVICES.length);
    String invalidWebPageChannelId = "invalid";
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("/services?webPageServiceChannelId=%s", webPageChannelId1))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(1))
      .body("id[0]", is(serviceId))
      .body("webPageServiceChannelIds[0].size()", is(1))
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
  public void testListServicesByOrganization() {
    String organizationId1 = getOrganizationId(0);
    String organizationId2 = getOrganizationId(1); 
    String invalidOrganizationId = "invalid";
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("/services?organizationId=%s", organizationId1))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(5))
      .body("organizations[0].size()", is(2))
      .body("organizations[0][0].organizationId", is(organizationId1))
      .body("organizations[1].size()", is(2))
      .body("organizations[1][0].organizationId", is(organizationId1));
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("/services?organizationId=%s", organizationId2))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(4))
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
  public void testListServicesSearch() {
    if (skipElasticSearchTests()) {
      return;
    }
    
    String query = "((test*) OR (Metatavu*)) AND (-Sosiaalipäivystys)";
    
    waitForElasticIndex();
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("/services?search=%s", query))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(2))
      .body("names[0][0].value", is("Metatavu testaa"))
      .body("names[1][0].value", is("Uusi testipalvelu"));
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("/services?search=%s&sortBy=SCORE&sortDir=DESC", query))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(2))
      .body("names[0][0].value", is("Metatavu testaa"))
      .body("names[1][0].value", is("Uusi testipalvelu"));
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("/services?search=%s&sortBy=SCORE&sortDir=ASC", query))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(2))
      .body("names[0][0].value", is("Uusi testipalvelu"))
      .body("names[1][0].value", is("Metatavu testaa"));
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
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/services/{serviceId}", getServiceId(0, TestPtvConsts.SERVICES.length))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id", notNullValue())
      .body("electronicServiceChannelIds.size()", is(0))
      .body("phoneServiceChannelIds.size()", is(1))
      .body("printableFormServiceChannelIds.size()", is(0))
      .body("serviceLocationServiceChannelIds.size()", is(1))
      .body("webPageServiceChannelIds.size()", is(0));

    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/services/{serviceId}", getServiceId(1, TestPtvConsts.SERVICES.length))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id", notNullValue())
      .body("electronicServiceChannelIds.size()", is(0))
      .body("phoneServiceChannelIds.size()", is(0))
      .body("printableFormServiceChannelIds.size()", is(0))
      .body("serviceLocationServiceChannelIds.size()", is(1))
      .body("webPageServiceChannelIds.size()", is(0));
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
