package fi.otavanopisto.kuntaapi.test.server.integration.ptv;

import static com.jayway.restassured.RestAssured.given;
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

import fi.otavanopisto.kuntaapi.test.AbstractIntegrationTest;

@SuppressWarnings ("squid:S1192")
public class ServicesTestsIT extends AbstractIntegrationTest {
  
  /**
   * Starts WireMock
   */
  @Rule
  public WireMockRule wireMockRule = new WireMockRule(getWireMockPort());
  
  @Before
  public void beforeTest() throws InterruptedException {
    getPtvOrganizationMocker()
      .mock("ae2682d3-6238-4019-b34f-b078c5f9bb50", "d45ec681-4da3-4a38-af67-fb2d949b9387");
    
    getPtvServiceMocker()
      .mock("2f21448e-e461-4ad0-a87a-47bcb08e578e", "0003651e-6afe-400e-816c-c64af41521f8", "00047a04-9c01-48ea-99da-4ec332f6d0fa");
    
    getPtvServiceChannelMocker()
      .mock("22472ece-95a0-4fef-a429-b4da689677b2", "44187ff9-71ed-40df-89f6-916be4f3baa6", "799e0e4f-4da7-4e7d-9e0e-f1370b80fc9a")  // ElectronicServiceChannels
      .mock("108f0c61-bfba-4dd7-8f02-deb4e77c52d0", "626cdd7a-e205-42da-8ce5-82b3b7add258", "e9e86a9e-6593-469d-bc01-f1a59c28168d")  // PhoneServiceChannels
      .mock("02256ce8-2879-47e4-a6f5-339872f0f758", "1a17f994-b924-46ae-8708-c09938125119", "6fb56241-1b43-4e42-8231-43ba8d86be36")  // PrintableFormServiceChannels
      .mock("9a9f5def-92e4-4b79-a49a-ccf20a0f75b6", "c0681f51-d1b4-4a9b-bbbf-ddf9a5273cd1", "cf927001-8b45-4f08-b93b-c78fe8477928")  // ServiceLocationServiceChannels
      .mock("4b08ae17-75ae-4746-9382-1316c4ec02c5", "aedae320-a2b2-4fe6-b23b-2e1a025ba415", "e9ec256b-5ca2-4663-9da6-d8a2faff21a8"); // WebPageServiceChannels
    
    startMocks();
    
    waitApiListCount("/organizations", 2);
    
    waitApiListCount("/electronicServiceChannels", 3);
    waitApiListCount("/phoneServiceChannels", 3);
    waitApiListCount("/printableFormServiceChannels", 3);
    waitApiListCount("/serviceLocationServiceChannels", 3);
    waitApiListCount("/webPageServiceChannels", 3);
    
    waitApiListCount("/services", 3);
  }
  
  @Test
  public void findService() {
    String id = given() 
        .baseUri(getApiBasePath())
        .contentType(ContentType.JSON)
        .get("/services")
        .body().jsonPath().getString("id[0]");
        
    assertNotNull(id);
    
    given() 
      .baseUri(getApiBasePath())
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
      .body("serviceClasses[0].uri", is("http://urn.fi/URN:NBN:fi:au:ptvl:P13"))
      .body("serviceClasses[0].parentId", nullValue())
      .body("serviceClasses[0].parentUri", is(""))

      .body("ontologyTerms.size()", is(1))
      .body("ontologyTerms[0].name.size()", is(3))
      .body("ontologyTerms[0].name[0].value", is("dna-test"))
      .body("ontologyTerms[0].name[0].language", is("sv"))
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
      .body("targetGroups[0].uri", is("http://urn.fi/URN:NBN:fi:au:ptvl:KR2"))
      .body("targetGroups[0].parentId", nullValue())
      .body("targetGroups[0].parentUri", is(""))
      
      .body("lifeEvents.size()", is(1))
      .body("lifeEvents[0].name.size()", is(3))
      .body("lifeEvents[0].name[0].value", is("Värnplikt"))
      .body("lifeEvents[0].name[0].language", is("sv"))
      .body("lifeEvents[0].code", is("KE2"))
      .body("lifeEvents[0].ontologyType", is("LIFESITUATION"))
      .body("lifeEvents[0].uri", is("http://urn.fi/URN:NBN:fi:au:ptvl:KE2"))
      .body("lifeEvents[0].parentId", nullValue())
      .body("lifeEvents[0].parentUri", is(""))
      
      .body("industrialClasses.size()", is(1))
      .body("industrialClasses[0].name.size()", is(4))
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
      .body("descriptions[0].value", is("Kuvaus"))
      .body("descriptions[0].type", is("Description"))
      
      .body("languages.size()", is(1))
      .body("languages[0]", is("fi"))
      .body("keywords.size()", is(0))
      .body("legislation.size()", is(1))
      .body("legislation[0].names.size()", is(1))
      .body("legislation[0].names[0].value", is("Korkein oikeus"))
      .body("legislation[0].names[0].language", is("fi"))
      .body("legislation[0].webPages.size()", is(1))
      .body("legislation[0].webPages[0].value", is("Korkein oikeus"))
      .body("legislation[0].webPages[0].language", is("fi"))
      .body("legislation[0].webPages[0].url", is("http://www.finlex.fi/fi/oikeus/kko/"))
      .body("areas.size()", is(0))
      .body("areaType", is("WholeCountry"))
      .body("requirements.size()", is(1))
      .body("requirements[0].value", is("Testi vaatimus"))
      .body("requirements[0].language", is("fi"))
      .body("publishingStatus", is("Published"))
      .body("chargeType", is("Free"))
      .body("organizations.size()", is(1))
      .body("organizations[0].additionalInformation.size()", is(0))
      .body("organizations[0].organizationId", notNullValue())
      .body("organizations[0].roleType", is("Responsible"))
      .body("organizations[0].provisionType", nullValue())
      .body("organizations[0].webPages.size()", is(0))
      .body("electronicServiceChannelIds.size()", is(0))
      .body("phoneServiceChannelIds.size()", is(0))
      .body("printableFormServiceChannelIds.size()", is(0))
      .body("serviceLocationServiceChannelIds.size()", is(1))
      .body("webPageServiceChannelIds.size()", is(0));
  }
  
  @Test
  public void testListServices() {
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/services")
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(3))
      .body("type[1]", is("Service"))
      .body("statutoryDescriptionId[1]", nullValue())
      .body("serviceClasses[1].size()", is(1))
      .body("serviceClasses[1][0].name.size()", is(3))
      .body("serviceClasses[1][0].name[0].value", is("Työnhaku ja työpaikat"))
      .body("serviceClasses[1][0].name[0].language", is("fi"))
      .body("serviceClasses[1][0].code", is("P10.1"))
      .body("serviceClasses[1][0].ontologyType", is("PTVL"))
      .body("serviceClasses[1][0].uri", is("http://urn.fi/URN:NBN:fi:au:ptvl:P10.1"))
      .body("serviceClasses[1][0].parentId", is("dbe5f86e-3c58-4208-b64f-17ca809796e2"))
      .body("serviceClasses[1][0].parentUri", is("http://urn.fi/URN:NBN:fi:au:ptvl:P10"))

      .body("ontologyTerms[1].size()", is(2))
      .body("targetGroups[1].size()", is(2))
      .body("lifeEvents[1].size()", is(0))
      .body("industrialClasses[1].size()", is(0))
      
      .body("names[1].size()", is(1))
      .body("names[1][0].language", is("fi"))
      .body("names[1][0].value", is("Nuorten työpajat"))
      .body("names[1][0].type", is("Name"))

      .body("descriptions[1].size()", is(2))

      .body("languages[1].size()", is(1))
      .body("languages[1][0]", is("sv"))
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
      .body("phoneServiceChannelIds[1].size()", is(1))
      .body("printableFormServiceChannelIds[1].size()", is(2))
      .body("serviceLocationServiceChannelIds[1].size()", is(0))
      .body("webPageServiceChannelIds[1].size()", is(0));
  } 
  
  @Test
  public void testListServicesByOrganization() {
    String serviceId1 = getServiceId(0);
    String serviceId2 = getServiceId(1);
    String serviceId3 = getServiceId(2);
    String organizationId1 = getOrganizationId(0);
    String organizationId2 = getOrganizationId(1); 
    String invalidOrganizationId = "invalid";
    
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get(String.format("/services?organizationId=%s", organizationId1))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(2))
      .body("id[0]", is(serviceId1))
      .body("id[1]", is(serviceId2))
      .body("organizations[0].size()", is(1))
      .body("organizations[0][0].organizationId", is(organizationId1))
      .body("organizations[1].size()", is(2))
      .body("organizations[1][0].organizationId", is(organizationId1));
    
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get(String.format("/services?organizationId=%s", organizationId2))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(1))
      .body("id[0]", is(serviceId3))
      .body("organizations[0].size()", is(1))
      .body("organizations[0][0].organizationId", is(organizationId2));
    
    given() 
      .baseUri(getApiBasePath())
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
    
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/services?search=(tes*)|(Nuorten*)")
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(2))
      .body("names[0][0].value", is("Metatavu testaa"))
      .body("names[1][0].value", is("Nuorten työpajat"));
    
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/services?search=(tes*)|(Nuorten*)&sortBy=SCORE&sortDir=DESC")
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(2))
      .body("names[0][0].value", is("Nuorten työpajat"))
      .body("names[1][0].value", is("Metatavu testaa"));
    
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/services?search=(tes*)|(Nuorten*)&sortBy=SCORE&sortDir=ASC")
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(2))
      .body("names[0][0].value", is("Metatavu testaa"))
      .body("names[1][0].value", is("Nuorten työpajat"));
  }

  @Test
  public void testListServicesLimits() {
    assertListLimits("/services", 3);
  }
  
  @Test
  public void testServiceUnarchive() throws InterruptedException {
    String serviceId = getServiceId(2);
    assertNotNull(serviceId);
    
    getPtvServiceMocker().unmock("00047a04-9c01-48ea-99da-4ec332f6d0fa");
    
    waitApiListCount("/services", 2);
    assertNull(getServiceId(2));
    
    getPtvServiceMocker().mock("00047a04-9c01-48ea-99da-4ec332f6d0fa");
    waitApiListCount("/services", 3);
    
    assertEquals(serviceId, getServiceId(2));
  }
  
  @Test
  public void testServiceChannelIds()  {
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/services/{serviceId}", getServiceId(0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id", notNullValue())
      .body("electronicServiceChannelIds.size()", is(0))
      .body("phoneServiceChannelIds.size()", is(0))
      .body("printableFormServiceChannelIds.size()", is(0))
      .body("serviceLocationServiceChannelIds.size()", is(1))
      .body("webPageServiceChannelIds.size()", is(0));

    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/services/{serviceId}", getServiceId(1))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id", notNullValue())
      .body("electronicServiceChannelIds.size()", is(0))
      .body("phoneServiceChannelIds.size()", is(1))
      .body("printableFormServiceChannelIds.size()", is(2))
      .body("serviceLocationServiceChannelIds.size()", is(0))
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

    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .config(newConfig().redirect(redirectConfig().followRedirects(false)))
      .get(fromListPath)
      .then()
      .assertThat()
      .statusCode(307)
      .header("Location", String.format("%s%s", getApiBasePath(), toListPath));
    
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .config(newConfig().redirect(redirectConfig().followRedirects(false)))
      .get(fromFindPath)
      .then()
      .assertThat()
      .statusCode(307)
      .header("Location", String.format("%s%s", getApiBasePath(), toFindPath));
  }
  
}
