package fi.otavanopisto.kuntaapi.test.server.integration.restfulptv;

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
      .mock("0f112910-08ca-4942-8c80-476cb710ee1d", "18bb8d7c-1dc7-4188-9149-7d89fdeac75e");
    
    getPtvServiceMocker()
      .mock("6c9926b9-4aa0-4635-b66a-471af07dfec3", "822d5347-8398-4866-bb9d-9cdc60b38fba", "ef66b7c2-e938-4a30-ad57-475fc40abf27");
    
    getPtvServiceChannelMocker()
      .mock("22472ece-95a0-4fef-a429-b4da689677b2", "44187ff9-71ed-40df-89f6-916be4f3baa6", "799e0e4f-4da7-4e7d-9e0e-f1370b80fc9a")  // ElectronicServiceChannels
      .mock("108f0c61-bfba-4dd7-8f02-deb4e77c52d0", "626cdd7a-e205-42da-8ce5-82b3b7add258", "e9e86a9e-6593-469d-bc01-f1a59c28168d")  // PhoneServiceChannels
      .mock("02256ce8-2879-47e4-a6f5-339872f0f758", "1a17f994-b924-46ae-8708-c09938125119", "6fb56241-1b43-4e42-8231-43ba8d86be36")  // PrintableFormServiceChannels
      .mock("9a9f5def-92e4-4b79-a49a-ccf20a0f75b6", "c0681f51-d1b4-4a9b-bbbf-ddf9a5273cd1", "cf927001-8b45-4f08-b93b-c78fe8477928")  // ServiceLocationServiceChannels
      .mock("4b08ae17-75ae-4746-9382-1316c4ec02c5", "aedae320-a2b2-4fe6-b23b-2e1a025ba415", "e9ec256b-5ca2-4663-9da6-d8a2faff21a8"); // WebPageServiceChannels
    
    startMocks();
    
    waitApiListCount("/serviceLocationServiceChannels", 3);
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
      .body("lifeEvents[0].name.size()", is(11))
      .body("lifeEvents[0].name[0].value", is("Asevelvollisuus"))
      .body("lifeEvents[0].name[0].language", is("fi"))
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

      .body("names.size()", is(2))
      .body("names[0].language", is("fi"))
      .body("names[0].value", is("Metatavu testaa"))
      .body("names[0].type", is("Name"))

      .body("descriptions.size()", is(4))
      .body("descriptions[0].language", is("fi"))
      .body("descriptions[0].value", is("Tiivistelmä."))
      .body("descriptions[0].type", is("ShortDescription"))
      
      .body("languages.size()", is(1))
      .body("languages[0]", is("fi"))
      .body("keywords.size()", is(0))
      .body("legislation.size()", is(0))
      .body("coverageType", is("Nationwide"))
      .body("municipalities.size()", is(0))
      .body("requirements.size()", is(1))
      .body("requirements[0].value", is(""))
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
      .body("type[1]", nullValue())
      .body("statutoryDescriptionId[1]", notNullValue())
      .body("serviceClasses[1].size()", is(1))
      .body("serviceClasses[1][0].name.size()", is(3))
      .body("serviceClasses[1][0].name[0].value", is("Työnhaku ja työpaikat"))
      .body("serviceClasses[1][0].name[0].language", is("fi"))
      .body("serviceClasses[1][0].code", is("P10.1"))
      .body("serviceClasses[1][0].ontologyType", is("PTVL"))
      .body("serviceClasses[1][0].uri", is("http://urn.fi/URN:NBN:fi:au:ptvl:P10.1"))
      .body("serviceClasses[1][0].parentId", is("dbe5f86e-3c58-4208-b64f-17ca809796e2"))
      .body("serviceClasses[1][0].parentUri", is("http://urn.fi/URN:NBN:fi:au:ptvl:P10"))

      .body("ontologyTerms[1].size()", is(0))
      .body("targetGroups[1].size()", is(0))
      .body("lifeEvents[1].size()", is(0))
      .body("industrialClasses[1].size()", is(0))
      
      .body("names[1].size()", is(1))
      .body("names[1][0].language", is("fi"))
      .body("names[1][0].value", is("Nuorten työpajat"))
      .body("names[1][0].type", is("Name"))

      .body("descriptions[1].size()", is(0))
      
      .body("languages[1].size()", is(0))
      .body("keywords[1].size()", is(0))
      .body("legislation[1].size()", is(0))
      .body("coverageType[1]", nullValue())
      .body("municipalities[1].size()", is(0))
      .body("requirements[1].size()", is(0))
      .body("publishingStatus[1]", is("Published"))
      .body("chargeType[1]", nullValue())

      .body("organizations[1].size()", is(1))
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
  public void testListServicesLimits() {
    assertListLimits("/services", 3);
  }
  
  @Test
  public void testServiceUnarchive() throws InterruptedException {
    String serviceId = getServiceId(2);
    assertNotNull(serviceId);
    
    getPtvServiceMocker().unmock("ef66b7c2-e938-4a30-ad57-475fc40abf27");
    
    waitApiListCount("/services", 2);
    assertNull(getServiceId(2));
    
    getPtvServiceMocker().mock("ef66b7c2-e938-4a30-ad57-475fc40abf27");
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
