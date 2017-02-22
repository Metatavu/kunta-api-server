package fi.otavanopisto.kuntaapi.test.server.integration.restfulptv;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertNotNull;

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
      .mockServices("6c9926b9-4aa0-4635-b66a-471af07dfec3", "822d5347-8398-4866-bb9d-9cdc60b38fba", "ef66b7c2-e938-4a30-ad57-475fc40abf27")
      .startMock();
    
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
  
}
