package fi.otavanopisto.kuntaapi.test.server.integration.ptv;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertNotNull;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.jayway.restassured.http.ContentType;

import fi.otavanopisto.kuntaapi.test.AbstractIntegrationTest;

@SuppressWarnings ("squid:S1192")
public class OrganizationsTestsIT extends AbstractIntegrationTest {
  
  /**
   * Starts WireMock
   */
  @Rule
  public WireMockRule wireMockRule = new WireMockRule(getWireMockPort());
  
  @Before
  public void beforeTest() throws InterruptedException {
    getPtvOrganizationMocker()
      .mock("9355a207-efd3-4cfb-a02b-67187f34c822", "ae2682d3-6238-4019-b34f-b078c5f9bb50", "d45ec681-4da3-4a38-af67-fb2d949b9387");
    
    getPtvServiceMocker()
      .mock("2f21448e-e461-4ad0-a87a-47bcb08e578e", "0003651e-6afe-400e-816c-c64af41521f8", "00047a04-9c01-48ea-99da-4ec332f6d0fa");
  
    getPtvServiceChannelMocker()
      .mock("22472ece-95a0-4fef-a429-b4da689677b2", "44187ff9-71ed-40df-89f6-916be4f3baa6", "799e0e4f-4da7-4e7d-9e0e-f1370b80fc9a")  // ElectronicServiceChannels
      .mock("108f0c61-bfba-4dd7-8f02-deb4e77c52d0", "626cdd7a-e205-42da-8ce5-82b3b7add258", "e9e86a9e-6593-469d-bc01-f1a59c28168d")  // PhoneServiceChannels
      .mock("02256ce8-2879-47e4-a6f5-339872f0f758", "1a17f994-b924-46ae-8708-c09938125119", "6fb56241-1b43-4e42-8231-43ba8d86be36")  // PrintableFormServiceChannels
      .mock("9a9f5def-92e4-4b79-a49a-ccf20a0f75b6", "c0681f51-d1b4-4a9b-bbbf-ddf9a5273cd1", "cf927001-8b45-4f08-b93b-c78fe8477928")  // ServiceLocationServiceChannels
      .mock("4b08ae17-75ae-4746-9382-1316c4ec02c5", "aedae320-a2b2-4fe6-b23b-2e1a025ba415", "e9ec256b-5ca2-4663-9da6-d8a2faff21a8"); // WebPageServiceChannels
    
    startMocks();

    waitApiListCount("/organizations", 3);
  }

  @Test
  public void findOrganization() {
    String id = given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations")
      .body().jsonPath().getString("id[0]");
      
    assertNotNull(id);
    
    given() 
      .baseUri(getApiBasePath())
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
      .body("webPages.size()", is(2))
      .body("webPages[0].value", is("Example"))
      .body("webPages[0].url", is("http://www.example.com"))
      .body("webPages[0].language", is("fi"))
      .body("addresses.size()", is(0))
      .body("publishingStatus", is("Published"))
      .body("parentOrganization", nullValue())
      .body("services.size()", is(0));
  }
  
  @Test
  public void testListOrganizations() {
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations")
      .then()
      .assertThat()
      .statusCode(200)
      .body("id[1]", notNullValue())
      .body("municipality[1].code", is("491"))
      .body("municipality[1].names.size()", is(2))
      .body("municipality[1].names[0].language", is("sv"))
      .body("municipality[1].names[0].value", is("S:t Michel"))
      .body("municipality[1].names[0].type", nullValue())
      .body("organizationType[1]", is("Municipality"))
      .body("businessName[1]", is("Mikkelin kaupunki"))
      .body("businessCode[1]", is("0165116-3"))
      .body("names[1].size()", is(2))
      .body("names[1][0].language", is("fi"))
      .body("names[1][0].value", is("Mikkelin kaupunki"))
      .body("names[1][0].type", is("Name"))
      .body("displayNameType[1].size()", is(2))
      .body("displayNameType[1][0].type", is("Name"))
      .body("displayNameType[1][0].language", is("fi"))
      .body("displayNameType[1][1].type", is("Name"))
      .body("displayNameType[1][1].language", is("sv"))
      
      .body("descriptions[1].size()", is(1))
      .body("descriptions[1][0].language", is("fi"))
      .body("descriptions[1][0].value", is("Mikkeli on 55 000 asukkaan kaupunki Etelä-Savossa Saimaan rannalla. Asiointi Mikkelin kaupungin kanssa on sujuvaa. Palvelut ovat saatavilla verkossa ympäri vuorokauden. Asiointi onnistuu vaikka kotisohvalta käsin helposti ja nopeasti.\nMikkeli panostaa uudenlaisten palvelujen tuottamiseen ja tarjoamiseen. Avoin digitaalisuus palvelee kuntalaisia ja tarjoaa mallia koko valtakunnalle. \nAvoimen datan sivusto open.mikkeli.fi kutsuu kuntalaisia mukaan ideoimaan digitaalisuuden mahdollisuuksia."))
      .body("descriptions[1][0].type", is("Description"))
      .body("emailAddresses[1].size()", is(1))
      .body("emailAddresses[1][0].language", is("fi"))
      .body("emailAddresses[1][0].value", is("kirjaamo@mikkeli.fi"))
      .body("emailAddresses[1][0].type", nullValue())
      .body("phoneNumbers[1].size()", is(1))
      .body("phoneNumbers[1][0].additionalInformation", nullValue())
      .body("phoneNumbers[1][0].serviceChargeType", is("Charged"))
      .body("phoneNumbers[1][0].chargeDescription", nullValue())
      .body("phoneNumbers[1][0].prefixNumber", nullValue())
      .body("phoneNumbers[1][0].isFinnishServiceNumber", is(Boolean.TRUE))
      .body("phoneNumbers[1][0].number", is("0151941"))
      .body("phoneNumbers[1][0].language", is("fi"))

      .body("webPages[1].size()", is(1))
      .body("webPages[1][0].description", nullValue())
      .body("webPages[1][0].url", is("http://www.mikkeli.fi/"))
      .body("webPages[1][0].language", is("fi"))
      .body("webPages[1][0].value", is("Mikkelin kaupungin kotisivut"))
      .body("webPages[1][0].type", nullValue())
  
      .body("addresses[1].size()", is(2))
      .body("addresses[1][0].latitude", is("0"))
      .body("addresses[1][0].longitude", is("0"))
      .body("addresses[1][0].coordinateState", is("EmptyInputReceived"))
      .body("addresses[1][0].type", is("Postal"))
      .body("addresses[1][0].postOfficeBox.size()", is(2))
      .body("addresses[1][0].postOfficeBox[0].language", is("sv"))
      .body("addresses[1][0].postOfficeBox[0].value", is("PL 33"))
      .body("addresses[1][0].postOfficeBox[1].language", is("fi"))
      .body("addresses[1][0].postOfficeBox[1].value", is("PL 33"))
      
      .body("addresses[1][0].postalCode", is("50101"))
      
      .body("addresses[1][0].postOffice.size()", is(2))
      .body("addresses[1][0].postOffice[0].value", is("MIKKELI"))
      .body("addresses[1][0].postOffice[0].language", is("sv"))
      .body("addresses[1][0].streetAddress.size()", is(1))
      .body("addresses[1][0].streetAddress[0].value", is("Example"))
      .body("addresses[1][0].streetAddress[0].language", is("fi"))
      .body("addresses[1][0].streetNumber", nullValue())
      .body("addresses[1][0].municipality", nullValue())
      .body("addresses[1][0].country", is("FI"))
      .body("addresses[1][0].additionalInformations.size()", is(0))
      
      .body("publishingStatus[1]", is("Published"))
      .body("parentOrganization[1]", nullValue())
      .body("services[1].size()", is(5))

      .body("services[1][0].serviceId", notNullValue())
      .body("services[1][0].organizationId", notNullValue())
      .body("services[1][0].roleType", is("Responsible"))
      .body("services[1][0].provisionType", nullValue())
      .body("services[1][0].webPages.size()", is(0));
  } 

  @Test
  public void testListOrganizationsByBusinessCode() {
    if (skipElasticSearchTests()) {
      return;
    }
    
    waitForElasticIndex();
    
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations?businessCode=0165116-3")
      .then()
      .assertThat()
      .statusCode(200)
      .body("size()", is(1))
      .body("id[0]", notNullValue())
      .body("businessName[0]", is("Mikkelin kaupunki"))
      .body("businessCode[0]", is("0165116-3"));
    
    given() 
      .baseUri(getApiBasePath())
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
    
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations?businessName=Mikkelin kaupunki")
      .then()
      .assertThat()
      .statusCode(200)
      .body("size()", is(1))
      .body("id[0]", notNullValue())
      .body("businessName[0]", is("Mikkelin kaupunki"))
      .body("businessCode[0]", is("0165116-3"));
    
    given() 
      .baseUri(getApiBasePath())
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
    
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get(String.format("/organizations?search=%s", search))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(2))
      .body("names[0][0].value", is("Mäntyharjun kunta"))
      .body("names[1][0].value", is("Mikkelin kaupunki"));
    
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get(String.format("/organizations?search=%s&sortBy=SCORE&sortDir=DESC", search))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(2))
      .body("names[0][0].value", is("Mikkelin kaupunki"))
      .body("names[1][0].value", is("Mäntyharjun kunta"));
    
    given() 
      .baseUri(getApiBasePath())
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