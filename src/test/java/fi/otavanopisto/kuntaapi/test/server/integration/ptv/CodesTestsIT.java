package fi.otavanopisto.kuntaapi.test.server.integration.ptv;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.jayway.restassured.http.ContentType;

import fi.otavanopisto.kuntaapi.test.AbstractIntegrationTest;

@SuppressWarnings ("squid:S1192")
public class CodesTestsIT extends AbstractIntegrationTest {
  
  /**
   * Starts WireMock
   */
  @Rule
  public WireMockRule wireMockRule = new WireMockRule(getWireMockPort());
  
  @Before
  public void beforeTest() throws InterruptedException {
    startMocks();
  }
  
  protected  boolean dropIdentifiersAfter() {
    return false;
  }
  
  @Test
  public void testListCountryCodes() throws InterruptedException {
    if (skipElasticSearchTests()) {
      return;
    }
    
    waitApiListCount("/codes?types=Country&maxResults=25", 25);
    waitForElasticIndex();
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/codes?types=Country&maxResults=3")
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(3))
      .body("id[0]", notNullValue())
      .body("type[0]", is("Country"))
      .body("code[0]", is("AD"))
      .body("names[0].size()", is(3))
      .body("names[0][0].language", is("fi"))
      .body("names[0][0].value", is("Andorra"))
      .body("names[0][0].type", nullValue())
      .body("extra[0].size()", is(1))
      .body("extra[0][0].key", is("prefixNumber"))
      .body("extra[0][0].value", is("+376"));
  } 
  
  @Test
  public void testListMunicipalityCodes() throws InterruptedException {
    if (skipElasticSearchTests()) {
      return;
    }
    
    waitApiListCount("/codes?types=Municipality&maxResults=10", 10);
    waitForElasticIndex();

    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/codes?types=Municipality&maxResults=3")
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(3))
      .body("id[0]", notNullValue())
      .body("type[0]", is("Municipality"))
      .body("code[0]", is("005"))
      .body("names[0].size()", is(2))
      .body("names[0][0].language", is("fi"))
      .body("names[0][0].value", is("Alajärvi"))
      .body("names[0][0].type", nullValue())
      .body("extra[0].size()", is(0));
      
  }
  
  @Test
  public void testListProvinceCodes() throws InterruptedException {
    if (skipElasticSearchTests()) {
      return;
    }
    
    waitApiListCount("/codes?types=Province&maxResults=19", 19);
    waitForElasticIndex();

    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/codes?types=Province&maxResults=3")
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(3))
      .body("id[0]", notNullValue())
      .body("type[0]", is("Province"))
      .body("code[0]", is("01"))
      .body("names[0].size()", is(2))
      .body("names[0][0].language", is("fi"))
      .body("names[0][0].value", is("Uusimaa"))
      .body("names[0][0].type", nullValue())
      .body("extra[0].size()", is(0));
  } 
  
  @Test
  public void testListHospitalRegionsCodes() throws InterruptedException {
    if (skipElasticSearchTests()) {
      return;
    }
    
    waitApiListCount("/codes?types=HospitalRegions&maxResults=15", 15);
    waitForElasticIndex();

    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/codes?types=HospitalRegions&maxResults=3")
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(3))
      .body("id[0]", notNullValue())
      .body("type[0]", is("HospitalRegions"))
      .body("code[0]", is("03"))
      .body("names[0].size()", is(2))
      .body("names[0][0].language", is("fi"))
      .body("names[0][0].value", is("Varsinais-Suomen sairaanhoitopiiri"))
      .body("names[0][0].type", nullValue())
      .body("extra[0].size()", is(0));
  } 

  @Test
  public void testListBusinessRegionsCodes() throws InterruptedException {
    if (skipElasticSearchTests()) {
      return;
    }
    
    waitApiListCount("/codes?types=BusinessRegions&maxResults=15", 15);
    waitForElasticIndex();

    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/codes?types=BusinessRegions&maxResults=3")
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(3))
      .body("id[0]", notNullValue())
      .body("type[0]", is("BusinessRegions"))
      .body("code[0]", is("302081"))
      .body("names[0].size()", is(3))
      .body("names[0][0].language", is("sv"))
      .body("names[0][0].value", is("Loimaaregionen"))
      .body("names[0][0].type", nullValue())
      .body("extra[0].size()", is(0));
  } 

  @Test
  public void testListLanguageCodes() throws InterruptedException {
    if (skipElasticSearchTests()) {
      return;
    }
    
    waitApiListCount("/codes?types=Language&maxResults=15", 15);
    waitForElasticIndex();

    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/codes?types=Language&maxResults=3")
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(3))
      .body("id[0]", notNullValue())
      .body("type[0]", is("Language"))
      .body("code[0]", is("am"))
      .body("names[0].size()", is(3))
      .body("names[0][0].language", is("sv"))
      .body("names[0][0].value", is("amhariska"))
      .body("names[0][0].type", nullValue())
      .body("extra[0].size()", is(0));
  } 

  @Test
  public void testListPostalCodes() throws InterruptedException {
    if (skipElasticSearchTests()) {
      return;
    }
    
    waitApiListCount("/codes?types=Postal&maxResults=10", 10);
    waitForElasticIndex();

    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/codes?types=Postal&maxResults=3")
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(3))
      .body("id[0]", notNullValue())
      .body("type[0]", is("Postal"))
      .body("code[0]", is("00002"))
      .body("names[0].size()", is(2))
      .body("names[0][0].language", is("sv"))
      .body("names[0][0].value", is("HELSINGFORS"))
      .body("names[0][0].type", nullValue())
      .body("extra[0].size()", is(0));
  }

}
