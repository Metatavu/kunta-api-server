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

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.jayway.restassured.http.ContentType;

import fi.otavanopisto.kuntaapi.server.integrations.ptv.PtvConsts;
import fi.otavanopisto.kuntaapi.test.AbstractIntegrationTest;

@SuppressWarnings ("squid:S1192")
public class ServicesTestsIT extends AbstractIntegrationTest {
  
  private static final String STATUTORY_DESCRIPTION = "Lapset ja perheet voivat saada hyvinvointinsa tueksi monenlaista toimintaa avoimissa päiväkodeissa.";
  private static final String STATUTORY_SHORT_DESCRIPTION = "Lapsen hyvinvointia tuetaan kunnan varhaiskasvatuspalveluilla.";
  
  /**
   * Starts WireMock
   */
  @Rule
  public WireMockRule wireMockRule = new WireMockRule(getWireMockPort());
  
  @Before
  public void beforeTest() throws InterruptedException {
    createSettings();
    getPtvMocker()
      .mockStatutoryDescriptions("2ddfcd49-b0a8-4221-8d8f-4c4d3c5c0ab8")
      .startMock();
    
    getRestfulPtvServiceMocker()
      .mockServices("6c9926b9-4aa0-4635-b66a-471af07dfec3", "822d5347-8398-4866-bb9d-9cdc60b38fba", "ef66b7c2-e938-4a30-ad57-475fc40abf27");
    
    getRestfulPtvElectronicServiceChannelMocker()
      .mockElectronicServiceChannels("22472ece-95a0-4fef-a429-b4da689677b2", "44187ff9-71ed-40df-89f6-916be4f3baa6", "799e0e4f-4da7-4e7d-9e0e-f1370b80fc9a");
    
    getRestfulPtvPhoneServiceChannelMocker()
      .mockPhoneServiceChannels("108f0c61-bfba-4dd7-8f02-deb4e77c52d0", "626cdd7a-e205-42da-8ce5-82b3b7add258", "e9e86a9e-6593-469d-bc01-f1a59c28168d");
      
    getRestfulPtvPrintableFormServiceChannelMocker()
      .mockPrintableFormServiceChannels("02256ce8-2879-47e4-a6f5-339872f0f758", "1a17f994-b924-46ae-8708-c09938125119", "6fb56241-1b43-4e42-8231-43ba8d86be36");
      
    getRestfulPtvServiceLocationServiceChannelMocker()
      .mockServiceLocationServiceChannels("9a9f5def-92e4-4b79-a49a-ccf20a0f75b6", "c0681f51-d1b4-4a9b-bbbf-ddf9a5273cd1", "cf927001-8b45-4f08-b93b-c78fe8477928");
      
    getRestfulPtvWebPageServiceChannelMocker()
      .mockWebPageServiceChannels("4b08ae17-75ae-4746-9382-1316c4ec02c5", "aedae320-a2b2-4fe6-b23b-2e1a025ba415", "e9ec256b-5ca2-4663-9da6-d8a2faff21a8");
    
    startMocks();
     
    waitApiListCount("/services", 3);
  }

  @After
  public void afterClass() {
    getPtvMocker().endMock();
    deleteSettings();
  }
   
  private void createSettings() {
    insertSystemSetting(PtvConsts.SYSTEM_SETTING_BASEURL, String.format("%s%s", getWireMockBasePath(), BASE_URL));
  }
  
  private void deleteSettings() {
    deleteSystemSetting(PtvConsts.SYSTEM_SETTING_BASEURL);
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
      .body("statutoryDescriptionId", notNullValue())
      .body("serviceClasses.size()", is(0))
      .body("ontologyTerms.size()", is(1))
      .body("ontologyTerms[0].id", is("1290f79b-b486-4d0d-bca2-20b980297400"))
      .body("ontologyTerms[0].name", is("lasten päivähoito"))
      .body("ontologyTerms[0].code", is(""))
      .body("ontologyTerms[0].system", is("FINTO"))
      .body("ontologyTerms[0].ontologyType", is("All"))
      .body("ontologyTerms[0].uri", is("http://www.yso.fi/onto/koko/p58117"))
      .body("ontologyTerms[0].parentId", nullValue())
      .body("ontologyTerms[0].parentUri", nullValue())
      .body("targetGroups.size()", is(0))
      .body("lifeEvents.size()", is(0))
      .body("industrialClasses.size()", is(0))
      .body("names.size()", is(2))
      .body("names[0].language", is("fi"))
      .body("names[0].value", is("Varhaiskasvatus"))
      .body("names[0].type", is("AlternateName"))
      .body("names[1].language", is("fi"))
      .body("names[1].value", is("Avoin varhaiskasvatuspalvelu"))
      .body("names[1].type", is("Name"))
      .body("descriptions.size()", is(3))
      .body("descriptions[0].language", is("fi"))
      .body("descriptions[0].value", is(String.format("%s%n%s", STATUTORY_SHORT_DESCRIPTION, "Varhaiskasvatus tukee lapsen elinikäistä oppimista, jossa leikillä on tärkeä merkitys. ")))
      .body("descriptions[0].type", is("ShortDescription"))
      .body("descriptions[1].language", is("fi"))
      .body("descriptions[1].value", is(String.format("%s%n%s", STATUTORY_DESCRIPTION, "Lapsiperheille tarjotaan vaihtoehtoisia palveluja erilaisiin elämäntilan-teisiin.")))
      .body("descriptions[1].type", is("Description"))
      .body("languages.size()", is(1))
      .body("languages[0]", is("fi"))
      .body("keywords.size()", is(0))
      .body("chargeType", nullValue())
      .body("municipalities.size()", is(1))
      .body("municipalities[0]", is("Kaarina"))
      .body("webPages.size()", is(0))
      .body("requirements.size()", is(1))
      .body("requirements[0].value", is(""))
      .body("requirements[0].language", is("fi"))
      .body("publishingStatus", is("Published"))
      .body("coverageType", is("Local"))
      .body("additionalInformations.size()", is(0));
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
      .body("id[1]", notNullValue())
      .body("statutoryDescriptionId[1]", nullValue())
      .body("serviceClasses[1].size()", is(1))
      .body("serviceClasses[1][0].id", is("afc6b0aa-c63c-45e5-9169-138a1c4b593b"))
      .body("serviceClasses[1][0].name", is("Aamu- ja iltapäiväkerhotoiminta"))
      .body("serviceClasses[1][0].code", is("P6.3"))
      .body("serviceClasses[1][0].system", is("FINTO"))
      .body("serviceClasses[1][0].ontologyType", nullValue())
      .body("serviceClasses[1][0].uri", is("http://urn.fi/URN:NBN:fi:au:ptvl:P6.3"))
      .body("serviceClasses[1][0].parentId", is("08f4f600-6f02-4aac-8f1d-06696469f499"))
      .body("serviceClasses[1][0].parentUri", nullValue())
      .body("ontologyTerms[1].size()", is(0))
      .body("targetGroups[1].size()", is(0))
      .body("lifeEvents[1].size()", is(0))
      .body("industrialClasses[1].size()", is(0))
      .body("names[1].size()", is(2))
      .body("names[1][0].language", is("fi"))
      .body("names[1][0].value", is(""))
      .body("names[1][0].type", is("AlternateName"))
      .body("names[1][1].language", is("fi"))
      .body("names[1][1].value", is("Perusopetuksen aamu-ja iltapäivätoiminta"))
      .body("names[1][1].type", is("Name"))
      .body("descriptions[1].size()", is(3))
      .body("descriptions[1][0].language", is("fi"))
      .body("descriptions[1][0].value", is("Tarjoamme lapselle tutun ja turvallisen paikan viettää aikaa koulun jälkeen.  "))
      .body("descriptions[1][0].type", is("ShortDescription"))
      .body("languages[1].size()", is(2))
      .body("languages[1][0]", is("sv"))
      .body("languages[1][1]", is("fi"))
      .body("keywords[1].size()", is(0))
      .body("municipalities[1].size()", is(0))
      .body("webPages[1].size()", is(0))
      .body("requirements[0].size()", is(1))
      .body("requirements[0][0].value", is(""))
      .body("requirements[0][0].language", is("fi"))
      .body("publishingStatus[1]", is("Published"))
      .body("chargeType[1]", nullValue())
      .body("coverageType[1]", nullValue())
      .body("type[1]", is("Service"))
      .body("additionalInformations[1].size()", is(5))
      .body("additionalInformations[1][0].value", is(""))
      .body("additionalInformations[1][0].type", is("ChargeType"))
      .body("additionalInformations[1][0].language", is("fi"));
  } 
  
  @Test
  public void testListServicesLimits() {
    assertListLimits("/services", 3);
  }
  
  @Test
  public void testServiceUnarchive() throws InterruptedException {
    String serviceId = getServiceId(2);
    assertNotNull(serviceId);
    
    getRestfulPtvServiceMocker().unmockServices("ef66b7c2-e938-4a30-ad57-475fc40abf27");
    
    waitApiListCount("/services", 2);
    assertNull(getServiceId(2));
    
    getRestfulPtvServiceMocker().mockServices("ef66b7c2-e938-4a30-ad57-475fc40abf27");
    waitApiListCount("/services", 3);
    
    assertEquals(serviceId, getServiceId(2));
  }
  
  @Test
  public void testServiceChannelIds()  {
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
      .body("electronicServiceChannelIds.size()", is(1))
      .body("phoneServiceChannelIds.size()", is(2))
      .body("printableFormServiceChannelIds.size()", is(1))
      .body("serviceLocationServiceChannelIds.size()", is(2))
      .body("webPageServiceChannelIds.size()", is(1));
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
