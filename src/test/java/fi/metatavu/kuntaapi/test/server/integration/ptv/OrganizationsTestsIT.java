package fi.metatavu.kuntaapi.test.server.integration.ptv;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.jayway.restassured.http.ContentType;

import fi.metatavu.kuntaapi.test.AbstractIntegrationTest;

@SuppressWarnings ("squid:S1192")
public class OrganizationsTestsIT extends AbstractIntegrationTest {
  
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

    waitApiListCount("/services", TestPtvConsts.SERVICES.length);
    waitApiListCount("/organizations", TestPtvConsts.ORGANIZATIONS.length);
  }

  @Test
  public void findOrganization() {
    String id = givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations")
      .body().jsonPath().getString("id[2]");
      
    assertNotNull(id);
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}", id)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id", is(id))
      .body("municipality.code", is("507"))
      .body("municipality.names.size()", is(2))
      .body("municipality.names[0].language", is("sv"))
      .body("municipality.names[0].value", is("Mäntyharju"))
      .body("municipality.names[0].type", nullValue())
      .body("organizationType", is("Municipality"))
      .body("businessCode", is("0165761-0"))
      .body("businessName", is("Mäntyharju"))
      .body("names.size()", is(1))
      .body("names[0].language", is("fi"))
      .body("names[0].value", is("Mäntyharjun kunta"))
      .body("names[0].type", is("Name"))
      .body("displayNameType.size()", is(2))
      .body("displayNameType[0].type", is("Name"))
      .body("displayNameType[0].language", is("fi"))
      .body("displayNameType[1].type", is("Name"))
      .body("displayNameType[1].language", is("sv"))
      .body("descriptions.size()", is(0))
      .body("emailAddresses.size()", is(0))
      .body("phoneNumbers.size()", is(0))
      .body("webPages.size()", is(0))
      .body("addresses.size()", is(0))
      .body("publishingStatus", is("Published"))
      .body("parentOrganization", nullValue())
      .body("services.size()", is(0));
  }
  
  @Test
  public void testListOrganizations() {
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations")
      .then()
      .assertThat()
      .statusCode(200)
      .body("id[0]", notNullValue())
      .body("municipality[0].code", is("491"))
      .body("municipality[0].names.size()", is(2))
      .body("municipality[0].names[0].language", is("sv"))
      .body("municipality[0].names[0].value", is("S:t Michel"))
      .body("municipality[0].names[0].type", nullValue())
      .body("organizationType[0]", is("Municipality"))
      .body("businessName[0]", is("Mikkelin kaupunki"))
      .body("businessCode[0]", is("0165116-3"))
      .body("names[0].size()", is(2))
      .body("names[0][0].language", is("fi"))
      .body("names[0][0].value", is("Mikkelin kaupunki"))
      .body("names[0][0].type", is("Name"))
      .body("displayNameType[0].size()", is(2))
      .body("displayNameType[0][0].type", is("Name"))
      .body("displayNameType[0][0].language", is("fi"))
      .body("displayNameType[0][1].type", is("Name"))
      .body("displayNameType[0][1].language", is("sv"))
      
      .body("descriptions[0].size()", is(1))
      .body("descriptions[0][0].language", is("fi"))
      .body("descriptions[0][0].value", is("Mikkeli on 55 000 asukkaan kaupunki Etelä-Savossa Saimaan rannalla. Asiointi Mikkelin kaupungin kanssa on sujuvaa. Palvelut ovat saatavilla verkossa ympäri vuorokauden. Asiointi onnistuu vaikka kotisohvalta käsin helposti ja nopeasti.\nMikkeli panostaa uudenlaisten palvelujen tuottamiseen ja tarjoamiseen. Avoin digitaalisuus palvelee kuntalaisia ja tarjoaa mallia koko valtakunnalle. \nAvoimen datan sivusto open.mikkeli.fi kutsuu kuntalaisia mukaan ideoimaan digitaalisuuden mahdollisuuksia."))
      .body("descriptions[0][0].type", is("Description"))
      .body("emailAddresses[0].size()", is(1))
      .body("emailAddresses[0][0].language", is("fi"))
      .body("emailAddresses[0][0].value", is("kirjaamo@mikkeli.fi"))
      .body("emailAddresses[0][0].type", nullValue())
      .body("phoneNumbers[0].size()", is(1))
      .body("phoneNumbers[0][0].additionalInformation", nullValue())
      .body("phoneNumbers[0][0].serviceChargeType", is("Charged"))
      .body("phoneNumbers[0][0].chargeDescription", nullValue())
      .body("phoneNumbers[0][0].prefixNumber", nullValue())
      .body("phoneNumbers[0][0].isFinnishServiceNumber", is(Boolean.TRUE))
      .body("phoneNumbers[0][0].number", is("0151941"))
      .body("phoneNumbers[0][0].language", is("fi"))

      .body("webPages[0].size()", is(1))
      .body("webPages[0][0].description", nullValue())
      .body("webPages[0][0].url", is("http://www.mikkeli.fi/"))
      .body("webPages[0][0].language", is("fi"))
      .body("webPages[0][0].value", is("Mikkelin kaupungin kotisivut"))
      .body("webPages[0][0].type", nullValue())
  
      .body("addresses[0].size()", is(2))
      .body("addresses[0][0].type", is("Postal"))
      .body("addresses[0][0].subtype", is("PostOfficeBox"))
      .body("addresses[0][0].postOfficeBox.size()", is(2))
      .body("addresses[0][0].postOfficeBox[0].language", is("sv"))
      .body("addresses[0][0].postOfficeBox[0].value", is("PL 33"))
      .body("addresses[0][0].postOfficeBox[1].language", is("fi"))
      .body("addresses[0][0].postOfficeBox[1].value", is("PL 33"))
      .body("addresses[0][0].postalCode", is("50101"))
      .body("addresses[0][0].postOffice.size()", is(2))
      .body("addresses[0][0].postOffice[0].value", is("MIKKELI"))
      .body("addresses[0][0].postOffice[0].language", is("sv"))
      .body("addresses[0][0].streetAddress", nullValue())
      .body("addresses[0][0].streetNumber", nullValue())
      .body("addresses[0][0].municipality", nullValue())
      .body("addresses[0][0].country", is("FI"))
      .body("addresses[0][0].additionalInformations.size()", is(0))

      .body("addresses[0][1].type", is("Visiting"))
      .body("addresses[0][1].subtype", is("Street"))
      .body("addresses[0][1].streetAddress.size()", is(1))
      .body("addresses[0][1].streetAddress[0].value", is("Maaherrankatu 9-11"))
      .body("addresses[0][1].streetAddress[0].language", is("fi"))
      .body("addresses[0][1].streetNumber", nullValue())
      .body("addresses[0][1].postalCode", is("50100"))
      .body("addresses[0][1].municipality", nullValue())
      .body("addresses[0][1].additionalInformations.size()", is(1))
      .body("addresses[0][1].additionalInformations[0].value", is("Virastotalo"))
      .body("addresses[0][1].additionalInformations[0].language", is("fi"))
      .body("addresses[0][1].latitude", is("0"))
      .body("addresses[0][1].longitude", is("0"))
      .body("addresses[0][1].coordinateState", is("EmptyInputReceived"))
      .body("addresses[0][1].country", is("FI"))
      
      .body("publishingStatus[0]", is("Published"))
      .body("parentOrganization[0]", nullValue())
      .body("services[0].size()", is(10))

      .body("services[0][0].serviceId", notNullValue())
      .body("services[0][0].organizationId", notNullValue())
      .body("services[0][0].roleType", is("Responsible"))
      .body("services[0][0].provisionType", nullValue())
      .body("services[0][0].webPages.size()", is(0));
  } 

  @Test
  public void testListOrganizationsByBusinessCode() {
    if (skipElasticSearchTests()) {
      return;
    }
    
    waitForElasticIndex();
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations?businessCode=0165116-3")
      .then()
      .assertThat()
      .statusCode(200)
      .body("size()", is(1))
      .body("id[0]", notNullValue())
      .body("businessName[0]", is("Mikkelin kaupunki"))
      .body("businessCode[0]", is("0165116-3"));
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations?businessCode=0000000-0")
      .then()
      .assertThat()
      .statusCode(200)
      .body("size()", is(0));
  }

  @Test
  public void testListOrganizationsByBusinessName() {
    if (skipElasticSearchTests()) {
      return;
    }
    
    waitForElasticIndex();
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations?businessName=Mikkelin kaupunki")
      .then()
      .assertThat()
      .statusCode(200)
      .body("size()", is(1))
      .body("id[0]", notNullValue())
      .body("businessName[0]", is("Mikkelin kaupunki"))
      .body("businessCode[0]", is("0165116-3"));
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations?businessName=invalid")
      .then()
      .assertThat()
      .statusCode(200)
      .body("size()", is(0));
  }
  
  @Test
  public void testListOrganizationsSearch() {
    if (skipElasticSearchTests()) {
      return;
    }
    
    waitForElasticIndex();
    
    String search = "(M*)|(Mi*)";
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("/organizations?search=%s", search))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(2))
      .body("names[0][0].value", is("Mikkelin kaupunki"))
      .body("names[1][0].value", is("Mäntyharjun kunta"));
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("/organizations?search=%s&sortBy=SCORE&sortDir=DESC", search))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(2))
      .body("names[0][0].value", is("Mikkelin kaupunki"))
      .body("names[1][0].value", is("Mäntyharjun kunta"));
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("/organizations?search=%s&sortBy=SCORE&sortDir=ASC", search))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(2))
      .body("names[0][0].value", is("Mäntyharjun kunta"))
      .body("names[1][0].value", is("Mikkelin kaupunki"));
  }

}