package fi.otavanopisto.kuntaapi.test.server.integration.restfulptv;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;

import java.time.ZoneId;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.jayway.restassured.http.ContentType;

import fi.otavanopisto.kuntaapi.test.AbstractIntegrationTest;

@SuppressWarnings ("squid:S1192")
public class ServiceChannelsTestsIT extends AbstractIntegrationTest {
  
  private static final ZoneId TIMEZONE_ID = ZoneId.of("Europe/Helsinki");
  
  /**
   * Starts WireMock
   */
  @Rule
  public WireMockRule wireMockRule = new WireMockRule(getWireMockPort());
  
  @Before
  public void beforeTest() throws InterruptedException {
    getPtvOrganizationMocker()
      .mock("0de268cf-1ea1-4719-8a6e-1150933b6b9e", "0f112910-08ca-4942-8c80-476cb710ee1d", "18bb8d7c-1dc7-4188-9149-7d89fdeac75e");
    
    getPtvServiceMocker()
      .mock("6c9926b9-4aa0-4635-b66a-471af07dfec3", "822d5347-8398-4866-bb9d-9cdc60b38fba");
    
    getPtvServiceChannelMocker()
      .mock("22472ece-95a0-4fef-a429-b4da689677b2", "44187ff9-71ed-40df-89f6-916be4f3baa6", "799e0e4f-4da7-4e7d-9e0e-f1370b80fc9a")  // ElectronicServiceChannels
      .mock("108f0c61-bfba-4dd7-8f02-deb4e77c52d0", "626cdd7a-e205-42da-8ce5-82b3b7add258", "e9e86a9e-6593-469d-bc01-f1a59c28168d")  // PhoneServiceChannels
      .mock("02256ce8-2879-47e4-a6f5-339872f0f758", "1a17f994-b924-46ae-8708-c09938125119", "6fb56241-1b43-4e42-8231-43ba8d86be36")  // PrintableFormServiceChannels
      .mock("9a9f5def-92e4-4b79-a49a-ccf20a0f75b6", "c0681f51-d1b4-4a9b-bbbf-ddf9a5273cd1", "cf927001-8b45-4f08-b93b-c78fe8477928")  // ServiceLocationServiceChannels
      .mock("4b08ae17-75ae-4746-9382-1316c4ec02c5", "aedae320-a2b2-4fe6-b23b-2e1a025ba415", "e9ec256b-5ca2-4663-9da6-d8a2faff21a8"); // WebPageServiceChannels
    
    startMocks();

    waitApiListCount("/electronicServiceChannels", 3);
    waitApiListCount("/phoneServiceChannels", 3);
    waitApiListCount("/printableFormServiceChannels", 3);
    waitApiListCount("/serviceLocationServiceChannels", 3);
    waitApiListCount("/webPageServiceChannels", 3);
    
    waitApiListCount("/organizations", 3);
    waitApiListCount("/services", 2);
  }

  @Test
  public void testFindElectronicChannel() throws InterruptedException {
    String channelId = given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/electronicServiceChannels")
      .body().jsonPath().getString("id[0]");

    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/electronicServiceChannels/{channelId}", channelId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id", notNullValue())
      .body("organizationId", is(getOrganizationId(2)))
      .body("names.size()", is(1))
      .body("names[0].language", is("fi"))
      .body("names[0].value", is("Wilma"))
      .body("names[0].type", is("Name"))
      .body("descriptions.size()", is(2))
      .body("descriptions[1].language", is("fi"))
      .body("descriptions[1].value", is("Mikkelissä huoltajien, opettajien ja oppilaiden viestinnän välineenä käytetään Wilmaa."))
      .body("descriptions[1].type", is("ShortDescription"))
      .body("signatureQuantity", is(0))
      .body("requiresSignature", is(false))
      .body("supportPhones.size()", is(1))
      .body("supportPhones[0].additionalInformation", nullValue())
      .body("supportPhones[0].serviceChargeType", is("Charged"))
      .body("supportPhones[0].chargeDescription", nullValue())
      .body("supportPhones[0].prefixNumber", nullValue())
      .body("supportPhones[0].isFinnishServiceNumber", is(true))
      .body("supportPhones[0].number", nullValue())
      .body("supportPhones[0].language", is("fi"))
      .body("supportPhones[0].type", nullValue())
      .body("supportEmails.size()", is(1))
      .body("supportEmails[0].language", is("fi"))
      .body("supportEmails[0].value", nullValue())
      .body("requiresAuthentication", is(true))
      .body("urls.size()", is(1))
      .body("urls[0].language", is("fi"))
      .body("urls[0].value", is("https://wilma.mikkeli.fi"))
      .body("languages.size()", is(1))
      .body("languages[0]", is("fi"))
      .body("attachments.size()", is(0))
      .body("webPages.size()", is(0))
      .body("serviceHours.size()", is(1))
      .body("serviceHours[0].serviceHourType", is("Standard"))
      .body("serviceHours[0].validFrom", sameInstant(getInstant(2017, 3, 1, 0, 0, TIMEZONE_ID)))
      .body("serviceHours[0].validTo", sameInstant(getInstant(2017, 12, 31, 0, 0, TIMEZONE_ID)))
      .body("serviceHours[0].isClosed", is(false))
      .body("serviceHours[0].validForNow", is(false))
      .body("serviceHours[0].additionalInformation.size()", is(1))
      .body("serviceHours[0].additionalInformation[0].value", is(""))
      .body("serviceHours[0].additionalInformation[0].language", is("fi"))
      .body("serviceHours[0].openingHour.size()", is(0))
      .body("publishingStatus", is("Published"));
  }
  
  @Test
  public void testListElectronicChannels() throws InterruptedException {
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/electronicServiceChannels")
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(3))
      
      .body("organizationId[1]", is(getOrganizationId(2)))
      .body("names[1].size()", is(1))
      .body("names[1][0].language", is("fi"))
      .body("names[1][0].value", is("Haku esiopetukseen"))
      .body("names[1][0].type", is("Name"))
      .body("descriptions[1].size()", is(2))
      .body("descriptions[1][1].language", is("fi"))
      .body("descriptions[1][1].value", is("Hae esiopetukseen"))
      .body("descriptions[1][1].type", is("Description"))
      .body("signatureQuantity[1]", is(1))
      .body("requiresSignature[1]", is(true))
      .body("supportPhones[1].size()", is(1))
      .body("supportPhones[1][0].additionalInformation", is("Neuvonta ja asiakaspalvelu"))
      .body("supportPhones[1][0].serviceChargeType", is("Charged"))
      .body("supportPhones[1][0].chargeDescription", is("Jonotusajalta peritään normaali puhelumaksu."))
      .body("supportPhones[1][0].prefixNumber", is("+358"))
      .body("supportPhones[1][0].isFinnishServiceNumber", is(false))
      .body("supportPhones[1][0].number", is("931086400"))
      .body("supportPhones[1][0].language", is("fi"))
      .body("supportPhones[1][0].type", nullValue())
      .body("supportEmails[1].size()", is(1))
      .body("supportEmails[1][0].language", is("fi"))
      .body("supportEmails[1][0].value", is("neuonta@edu.hel.fi"))
      .body("requiresAuthentication[1]", is(true))
      .body("urls[1].size()", is(1))
      .body("urls[1][0].language", is("fi"))
      .body("urls[1][0].value", is("http://hel.fi/edu"))
      .body("languages[1].size()", is(0))
      .body("attachments[1].size()", is(0))
      .body("webPages[1].size()", is(0))
      .body("serviceHours[1].size()", is(1))
      .body("serviceHours[1][0].serviceHourType", is("Special"))
      .body("serviceHours[1][0].validFrom", sameInstant(getInstant(2017, 1, 22, 0, 0, TIMEZONE_ID)))
      .body("serviceHours[1][0].validTo", sameInstant(getInstant(2017, 4, 19, 0, 0, TIMEZONE_ID)))
      .body("serviceHours[1][0].isClosed", is(false))
      .body("serviceHours[1][0].validForNow", is(false))
      .body("serviceHours[1][0].additionalInformation.size()", is(1))
      .body("serviceHours[1][0].additionalInformation[0].value", is("Hakuaika"))
      .body("serviceHours[1][0].additionalInformation[0].language", is("fi"))
      .body("serviceHours[1][0].openingHour.size()", is(1))
      .body("serviceHours[1][0].openingHour[0].dayFrom", is(2))
      .body("serviceHours[1][0].openingHour[0].dayTo", is(1))
      .body("serviceHours[1][0].openingHour[0].from", is("00:00:00"))
      .body("serviceHours[1][0].openingHour[0].to", is("00:00:00"))
      .body("serviceHours[1][0].openingHour[0].isExtra", is(false))
      .body("publishingStatus[1]", is("Published"));
  } 
  
  @Test
  public void testFindPhoneChannel() throws InterruptedException {
    String channelId = given() 
        .baseUri(getApiBasePath())
        .contentType(ContentType.JSON)
        .get("/phoneServiceChannels")
        .body().jsonPath().getString("id[0]");

      given() 
        .baseUri(getApiBasePath())
        .contentType(ContentType.JSON)
        .get("/phoneServiceChannels/{channelId}", channelId)
        .then()
        .assertThat()
        .statusCode(200)
        .body("id", notNullValue())
        .body("organizationId", is(getOrganizationId(2)))
        .body("names.size()", is(1))
        .body("names[0].language", is("fi"))
        .body("names[0].value", is("Forssan kaupungin puhelinvaihde"))
        .body("names[0].type", is("Name"))
        .body("descriptions.size()", is(2))
        .body("descriptions[1].language", is("fi"))
        .body("descriptions[1].value", is("Puhelinvaihde välittää puhelut kaupungin henkilökunnalle."))
        .body("descriptions[1].type", is("Description"))
        .body("phoneNumbers.size()", is(1))
        .body("phoneNumbers[0].additionalInformation", is("vaihde"))
        .body("phoneNumbers[0].serviceChargeType", is("Charged"))
        .body("phoneNumbers[0].chargeDescription", nullValue())
        .body("phoneNumbers[0].prefixNumber", is("+358"))
        .body("phoneNumbers[0].isFinnishServiceNumber", is(false))
        .body("phoneNumbers[0].number", is("341411"))
        .body("phoneNumbers[0].language", is("fi"))
        .body("phoneNumbers[0].type", is("Phone"))
        .body("supportEmails.size()", is(1))
        .body("supportEmails[0].language", is("fi"))
        .body("supportEmails[0].value", is("palvelupiste@forssa.fi"))
        .body("languages.size()", is(1))
        .body("languages[0]", is("fi"))
        .body("webPages.size()", is(1))
        .body("webPages[0].value", nullValue())
        .body("webPages[0].url", is("http://www.forssa.fi/"))
        .body("webPages[0].language", is("fi"))
        .body("serviceHours.size()", is(1))
        .body("serviceHours[0].serviceHourType", is("Standard"))
        .body("serviceHours[0].validFrom", nullValue())
        .body("serviceHours[0].validTo", nullValue())
        .body("serviceHours[0].isClosed", is(false))
        .body("serviceHours[0].validForNow", is(true))
        .body("serviceHours[0].additionalInformation.size()", is(1))
        .body("serviceHours[0].additionalInformation[0].value", nullValue())
        .body("serviceHours[0].additionalInformation[0].language", is("fi"))
        .body("serviceHours[0].openingHour.size()", is(5))
        .body("serviceHours[0].openingHour[0].dayFrom", is(1))
        .body("serviceHours[0].openingHour[0].dayTo", nullValue())
        .body("serviceHours[0].openingHour[0].from", is("08:00:00"))
        .body("serviceHours[0].openingHour[0].to", is("16:00:00"))
        .body("serviceHours[0].openingHour[0].isExtra", is(false))
        .body("publishingStatus", is("Published"));
  }
  
  @Test
  public void testListPhoneChannels() throws InterruptedException {
    String channelId = given() 
        .baseUri(getApiBasePath())
        .contentType(ContentType.JSON)
        .get("/phoneServiceChannels")
        .body().jsonPath().getString("id[0]");

      given() 
        .baseUri(getApiBasePath())
        .contentType(ContentType.JSON)
        .get("/phoneServiceChannels/{channelId}", channelId)
        .then()
        .assertThat()
        .statusCode(200)
        .body("id", notNullValue())
        .body("organizationId", is(getOrganizationId(2)))
        .body("names.size()", is(1))
        .body("names[0].language", is("fi"))
        .body("names[0].value", is("Forssan kaupungin puhelinvaihde"))
        .body("names[0].type", is("Name"))
        .body("descriptions.size()", is(2))
        .body("descriptions[1].language", is("fi"))
        .body("descriptions[1].value", is("Puhelinvaihde välittää puhelut kaupungin henkilökunnalle."))
        .body("descriptions[1].type", is("Description"))
        .body("phoneNumbers.size()", is(1))
        .body("phoneNumbers[0].additionalInformation", is("vaihde"))
        .body("phoneNumbers[0].serviceChargeType", is("Charged"))
        .body("phoneNumbers[0].chargeDescription", nullValue())
        .body("phoneNumbers[0].prefixNumber", is("+358"))
        .body("phoneNumbers[0].isFinnishServiceNumber", is(false))
        .body("phoneNumbers[0].number", is("341411"))
        .body("phoneNumbers[0].language", is("fi"))
        .body("phoneNumbers[0].type", is("Phone"))
        .body("supportEmails.size()", is(1))
        .body("supportEmails[0].language", is("fi"))
        .body("supportEmails[0].value", is("palvelupiste@forssa.fi"))
        .body("languages.size()", is(1))
        .body("languages[0]", is("fi"))
        .body("webPages.size()", is(1))
        .body("webPages[0].value", nullValue())
        .body("webPages[0].url", is("http://www.forssa.fi/"))
        .body("webPages[0].language", is("fi"))
        .body("serviceHours.size()", is(1))
        .body("serviceHours[0].serviceHourType", is("Standard"))
        .body("serviceHours[0].validFrom", nullValue())
        .body("serviceHours[0].validTo", nullValue())
        .body("serviceHours[0].isClosed", is(false))
        .body("serviceHours[0].validForNow", is(true))
        .body("serviceHours[0].additionalInformation.size()", is(1))
        .body("serviceHours[0].additionalInformation[0].value", nullValue())
        .body("serviceHours[0].additionalInformation[0].language", is("fi"))
        .body("serviceHours[0].openingHour.size()", is(5))
        .body("serviceHours[0].openingHour[0].dayFrom", is(1))
        .body("serviceHours[0].openingHour[0].dayTo", nullValue())
        .body("serviceHours[0].openingHour[0].from", is("08:00:00"))
        .body("serviceHours[0].openingHour[0].to", is("16:00:00"))
        .body("serviceHours[0].openingHour[0].isExtra", is(false))
        .body("publishingStatus", is("Published"));
  }
  
  @Test
  public void testFindPrintableFormChannel() throws InterruptedException {
    String channelId = given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/printableFormServiceChannels")
      .body().jsonPath().getString("id[0]");
 

    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/printableFormServiceChannels/{channelId}", channelId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id", notNullValue())
      .body("organizationId", is(getOrganizationId(2)))
      .body("names.size()", is(1))
      .body("names[0].language", is("fi"))
      .body("names[0].value", is("Ohjeita päivähoitosijoitusta ja maksua varten"))
      .body("names[0].type", is("Name"))
      .body("descriptions.size()", is(2))
      .body("descriptions[1].language", is("fi"))
      .body("descriptions[1].value", is("Ohjeita päivähoitosijoitusta ja maksua varten"))
      .body("descriptions[1].type", is("Description"))
      .body("formIdentifier.size()", is(2))
      .body("formIdentifier[0].language", is("fi"))
      .body("formIdentifier[0].value", is(""))
      .body("formReceiver.size()", is(2))
      .body("formReceiver[0].language", is("fi"))
      .body("formReceiver[0].value", is(""))
      .body("deliveryAddress", nullValue())
      .body("channelUrls.size()", is(1))
      .body("channelUrls[0].language", is("fi"))
      .body("channelUrls[0].value", is("www.kuusamo.fi/node/501"))
      .body("channelUrls[0].type", is("PDF"))
      .body("attachments.size()", is(0))
      .body("supportPhones.size()", is(0))
      .body("supportEmails.size()", is(0))
      .body("languages.size()", is(0))
      .body("webPages.size()", is(0))
      .body("serviceHours.size()", is(0))
      .body("publishingStatus", is("Published"));
  }

  @Test
  public void testListPrintableFormChannels() throws InterruptedException {
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/printableFormServiceChannels")
      .then()
      .assertThat()
      .statusCode(200)
      .body("id[1]", notNullValue())
      .body("organizationId[1]", is(getOrganizationId(2)))
      .body("names[1].size()", is(1))
      .body("names[1][0].language", is("fi"))
      .body("names[1][0].value", is("Vieraskuntalaishakemus"))
      .body("names[1][0].type", is("Name"))
      .body("descriptions[1].size()", is(2))
      .body("descriptions[1][1].language", is("fi"))
      .body("descriptions[1][1].value", is("TOISTETAAN TIETO, KOSKA NETTISIVUISSA EI OLE LISÄINFOA Tulevien 1. luokkalaisten lasten jotka eivät asu Kaarinassa, huoltajan tulee tehdä vieraskuntalaishakemus Kaarinan koulutoimistoon.   "))
      .body("descriptions[1][1].type", is("Description"))
      .body("formIdentifier[1].size()", is(2))
      .body("formIdentifier[1][0].language", is("fi"))
      .body("formIdentifier[1][0].value", is(""))
      .body("formReceiver[1].size()", is(2))
      .body("formReceiver[1][0].language", is("fi"))
      .body("formReceiver[1][0].value", is("Sivistyspalvelut, Koulutuspalvelut"))
      .body("deliveryAddress[1].latitude", is("0"))
      .body("deliveryAddress[1].longitude", is("0"))
      .body("deliveryAddress[1].coordinateState", is("EmptyInputReceived"))
      .body("deliveryAddress[1].postOfficeBox", is(""))
      .body("deliveryAddress[1].postalCode", is("20780"))
      .body("deliveryAddress[1].postOffice.size()", is(2))
      .body("deliveryAddress[1].postOffice[0].value", is("KAARINA"))
      .body("deliveryAddress[1].postOffice[0].language", is("sv"))
      .body("deliveryAddress[1].streetAddress.size()", is(1))
      .body("deliveryAddress[1].streetAddress[0].value", is("Lautakunnankatu 4"))
      .body("deliveryAddress[1].streetAddress[0].language", is("fi"))
      .body("deliveryAddress[1].additionalInformations.size()", is(1))
      .body("deliveryAddress[1].additionalInformations[0].value", is("Vieraskuntalaishakemus voi postittaa tai tuoda Kaarinan koulutoimistoon. "))
      .body("deliveryAddress[1].additionalInformations[0].language", is("fi"))
      .body("deliveryAddress[1].country", is("FI"))
      .body("deliveryAddress[1].municipality", nullValue())
      .body("deliveryAddress[1].streetNumber", nullValue())
      .body("channelUrls[1].size()", is(1))
      .body("channelUrls[1][0].language", is("fi"))
      .body("channelUrls[1][0].value", is("http://www.kaarina.fi/opetus_ja_koulutus/perusopetus/fi_FI/ilmoittautuminen/"))
      .body("channelUrls[1][0].type", is("DOC"))
      .body("attachments[1].size()", is(0))
      .body("supportPhones[1].size()", is(0))
      .body("supportEmails[1].size()", is(0))
      .body("languages[1].size()", is(0))
      .body("webPages[1].size()", is(0))
      .body("serviceHours[1].size()", is(0))
      .body("publishingStatus[1]", is("Published"));
  }
  
  @Test
  public void testFindServiceLocationChannel() throws InterruptedException {
    waitApiListCount("/serviceLocationServiceChannels", 3);
    
    String channelId = given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/serviceLocationServiceChannels")
      .body().jsonPath().getString("id[0]");
    
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/serviceLocationServiceChannels/{channelId}", channelId)
      .then()
      .assertThat()
      .statusCode(200)
      
      .body("id", notNullValue())
      .body("organizationId", notNullValue())
      .body("names.size()", is(1))
      .body("names[0].language", is("fi"))
      .body("names[0].value", is("Someron kaupungintalo"))
      .body("names[0].type", is("Name"))
      .body("descriptions.size()", is(2))
      .body("descriptions[1].language", is("fi"))
      .body("descriptions[1].value", is("Someron kaupungin infopiste ja puhelinvaihde palvelevat kaupungintalon aulassa. Infopisteestä saa yleistietoa kaupunginpalveluista, sinne voi toimittaa kaupungille suunnatut asiakirjat kirjattavaksi ja edelleen oikeille tahoille toimitettaviksi. Infopisteessä voi myös maksaa kaupungin laskuja, hoitaa tilojen varauksia ja ostaa koululaisten matkalippuja.  "))
      .body("descriptions[1].type", is("Description"))
      .body("serviceAreaRestricted", is(false))
      .body("phoneNumbers.size()", is(2))
      .body("phoneNumbers[0].additionalInformation", is("vaihde"))
      .body("phoneNumbers[0].serviceChargeType", is("Charged"))
      .body("phoneNumbers[0].chargeDescription", nullValue())
      .body("phoneNumbers[0].prefixNumber", is("+358"))
      .body("phoneNumbers[0].isFinnishServiceNumber", is(false))
      .body("phoneNumbers[0].number", is("277911"))
      .body("phoneNumbers[0].language", is("fi"))
      .body("phoneNumbers[0].type", is("Phone"))
      .body("emails.size()", is(1))
      .body("emails[0].language", is("fi"))
      .body("emails[0].value", is("info@somero.fi"))
      .body("languages.size()", is(1))
      .body("languages[0]", is("fi"))
      .body("phoneServiceCharge", is(false))
      .body("webPages.size()", is(1))
      .body("webPages[0].value", is("Someron kaupunki"))
      .body("webPages[0].url", is("https://www.somero.fi"))
      .body("webPages[0].language", is("fi"))
      .body("serviceAreas.size()", is(0))
      .body("addresses.size()", is(2))
      .body("addresses[0].latitude", is("6726437.712"))
      .body("addresses[0].longitude", is("309359.871"))
      .body("addresses[0].type", is("Visiting"))
      .body("addresses[0].postOfficeBox", is(""))
      .body("addresses[0].postalCode", is("31400"))
      .body("addresses[0].postOffice.size()", is(2))
      .body("addresses[0].postOffice[0].value", is("SOMERO"))
      .body("addresses[0].postOffice[0].language", is("sv"))
      .body("addresses[0].streetAddress.size()", is(1))
      .body("addresses[0].streetAddress[0].value", is("Joensuuntie"))
      .body("addresses[0].streetAddress[0].language", is("fi"))
      .body("addresses[0].streetNumber", is("20"))
      .body("addresses[0].municipality", nullValue())
      .body("addresses[0].country", is("FI"))
      .body("addresses[0].additionalInformations.size()", is(1))
      .body("addresses[0].additionalInformations[0].value", is(""))
      .body("addresses[0].additionalInformations[0].language", is("fi"))
      .body("serviceHours.size()", is(11))
      .body("publishingStatus", is("Published"));
  }

  @Test
  public void testListServiceLocationChannel() throws InterruptedException {
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/serviceLocationServiceChannels")
      .then()
      .assertThat()
      .statusCode(200)
      .body("id[1]", notNullValue())
      .body("organizationId[1]", notNullValue())
      .body("names[1].size()", is(1))
      .body("names[1][0].language", is("fi"))
      .body("names[1][0].value", is("Metatavun toimisto"))
      .body("names[1][0].type", is("Name"))
      .body("descriptions[1].size()", is(2))
      .body("descriptions[1][1].language", is("fi"))
      .body("descriptions[1][1].value", is("Metatavun toimiston aukioloajat"))
      .body("descriptions[1][1].type", is("Description"))
      .body("serviceAreaRestricted[1]", is(false))
      .body("phoneNumbers[1].size()", is(1))
      .body("phoneNumbers[1][0].additionalInformation", nullValue())
      .body("phoneNumbers[1][0].serviceChargeType", is("Charged"))
      .body("phoneNumbers[1][0].chargeDescription", nullValue())
      .body("phoneNumbers[1][0].prefixNumber", is("+358"))
      .body("phoneNumbers[1][0].isFinnishServiceNumber", is(false))
      .body("phoneNumbers[1][0].number", is("442909201"))
      .body("phoneNumbers[1][0].language", is("fi"))
      .body("phoneNumbers[1][0].type", is("Phone"))
      .body("emails[1].size()", is(1))
      .body("emails[1][0].language", is("fi"))
      .body("emails[1][0].value", is("info@metatavu.fi"))
      .body("languages[1].size()", is(1))
      .body("languages[1][0]", is("fi"))
      .body("phoneServiceCharge[1]", is(false))
      .body("webPages[1].size()", is(0))
      .body("serviceAreas[1].size()", is(0))
      .body("addresses[1].size()", is(1))
      .body("addresses[1][0].type", is("Visiting"))
      .body("addresses[1][0].postOfficeBox", is(""))
      .body("addresses[1][0].postalCode", is("50100"))
      .body("addresses[1][0].postOffice.size()", is(2))
      .body("addresses[1][0].postOffice[0].value", is("MIKKELI"))
      .body("addresses[1][0].postOffice[0].language", is("sv"))
      .body("addresses[1][0].streetAddress.size()", is(1))
      .body("addresses[1][0].streetAddress[0].value", is("Rouhialankatu"))
      .body("addresses[1][0].streetAddress[0].language", is("fi"))
      .body("addresses[1][0].streetNumber", is("4"))
      .body("addresses[1][0].municipality.code", is("491"))
      .body("addresses[1][0].municipality.names.size()", is(2))
      .body("addresses[1][0].municipality.names[0].value", is("S:t Michel"))
      .body("addresses[1][0].municipality.names[0].language", is("sv"))
      .body("addresses[1][0].country", is("FI"))
      .body("addresses[1][0].latitude", is("6840381.653"))
      .body("addresses[1][0].longitude", is("514607.162"))
      .body("addresses[1][0].qualifier", nullValue())
      .body("addresses[1][0].additionalInformations.size()", is(1))
      .body("addresses[1][0].additionalInformations[0].value", is("käynti sisäpihalta"))
      .body("addresses[1][0].additionalInformations[0].language", is("fi"))

      .body("serviceHours[1].size()", is(4))
      .body("serviceHours[1][0].serviceHourType", is("Exception"))
      .body("serviceHours[1][0].validFrom", sameInstant(getInstant(2017, 4, 26, 0, 0, TIMEZONE_ID)))
      .body("serviceHours[1][0].validTo", nullValue())
      .body("serviceHours[1][0].isClosed", is(true))
      .body("serviceHours[1][0].validForNow", is(false))
      .body("serviceHours[1][0].additionalInformation.size()", is(1))
      .body("serviceHours[1][0].additionalInformation[0].value", nullValue())
      .body("serviceHours[1][0].additionalInformation[0].language", is("fi"))
      .body("serviceHours[1][0].openingHour.size()", is(0))
      
      .body("serviceHours[1][1].serviceHourType", is("Standard"))
      .body("serviceHours[1][1].validFrom", sameInstant(getInstant(2017, 4, 3, 0, 0, TIMEZONE_ID)))
      .body("serviceHours[1][1].validTo", sameInstant(getInstant(2017, 4, 9, 0, 0, TIMEZONE_ID)))
      .body("serviceHours[1][1].isClosed", is(false))
      .body("serviceHours[1][1].validForNow", is(false))
      .body("serviceHours[1][1].additionalInformation.size()", is(1))
      .body("serviceHours[1][1].additionalInformation[0].value", is("Huhtikuun ensimmäinen viikko"))
      .body("serviceHours[1][1].additionalInformation[0].language", is("fi"))
      .body("serviceHours[1][1].openingHour.size()", is(2))
      .body("serviceHours[1][1].openingHour[0].dayFrom", is(2))
      .body("serviceHours[1][1].openingHour[0].dayTo", nullValue())
      .body("serviceHours[1][1].openingHour[0].from", is("08:00:00"))
      .body("serviceHours[1][1].openingHour[0].to", is("17:00:00"))
      .body("serviceHours[1][1].openingHour[0].isExtra", is(false))
      
      .body("serviceHours[1][3].serviceHourType", is("Exception"))
      .body("serviceHours[1][3].validFrom", sameInstant(getInstant(2017, 2, 27, 0, 0, TIMEZONE_ID)))
      .body("serviceHours[1][3].validTo", nullValue())
      .body("serviceHours[1][3].isClosed", is(false))
      .body("serviceHours[1][3].validForNow", is(false))
      .body("serviceHours[1][3].additionalInformation.size()", is(1))
      .body("serviceHours[1][3].additionalInformation[0].value", is("Aamupäivystys"))
      .body("serviceHours[1][3].additionalInformation[0].language", is("fi"))
      .body("serviceHours[1][3].openingHour.size()", is(1))
      .body("serviceHours[1][3].openingHour[0].dayFrom", nullValue())
      .body("serviceHours[1][3].openingHour[0].dayTo", nullValue())
      .body("serviceHours[1][3].openingHour[0].from", is("06:00:00"))
      .body("serviceHours[1][3].openingHour[0].to", is("9:00:00"))
      .body("serviceHours[1][3].openingHour[0].isExtra", is(false))

      .body("publishingStatus[1]", is("Published"));
  }

  @Test
  public void testFindWebPageChannel() throws InterruptedException {
    String channelId = given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/webPageServiceChannels")
      .body().jsonPath().getString("id[0]");
    
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/webPageServiceChannels/{channelId}", channelId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id", notNullValue())
      .body("organizationId", notNullValue())
      .body("names.size()", is(1))
      .body("names[0].language", is("fi"))
      .body("names[0].value", is("Koulumaitotuen lainsäädäntö"))
      .body("names[0].type", is("Name"))
      .body("descriptions.size()", is(2))
      .body("descriptions[1].language", is("fi"))
      .body("descriptions[1].value", is("Koulumaitotuen lainsäädäntö perustuu Euroopan parlamentin, neuvoston ja komission asetuksiin sekä lisäksi koulumaitotukeen sovelletaan kansallista lainsäädäntöä ja Maaseutuviraston antamaa määräystä."))
      .body("descriptions[1].type", is("Description"))
      .body("urls.size()", is(1))
      .body("urls[0].language", is("fi"))
      .body("urls[0].value", is("http://www.mavi.fi/fi/tuet-ja-palvelut/kunta-koulu-paivakoti/Sivut/Koulumaitotuen-lainsäädäntö.aspx"))
      .body("supportPhones.size()", is(0))
      .body("supportEmails.size()", is(0))
      .body("languages.size()", is(2))
      .body("languages[0]", is("sv"))
      .body("webPages.size()", is(0))
      .body("serviceHours.size()", is(0))
      .body("publishingStatus", is("Published"));
  }

  @Test
  public void testListWebPageChannel() throws InterruptedException {
    waitApiListCount("/webPageServiceChannels", 3);
    
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/webPageServiceChannels")
      .then()
      .assertThat()
      .statusCode(200)
      .body("id[1]", notNullValue())
      .body("organizationId[1]", notNullValue())
      .body("names[1].size()", is(1))
      .body("names[1][0].language", is("fi"))
      .body("names[1][0].value", is("Pukkilan kunnan verkkosivut"))
      .body("names[1][0].type", is("Name"))
      .body("descriptions[1].size()", is(2))
      .body("descriptions[1][1].language", is("fi"))
      .body("descriptions[1][1].value", is("Palvelut, tapahtumakalenteri, asiointi."))
      .body("descriptions[1][1].type", is("Description"))
      .body("urls[1].size()", is(1))
      .body("urls[1][0].language", is("fi"))
      .body("urls[1][0].value", is("http://www.pukkila.fi/"))

      .body("supportPhones[1].size()", is(0))
      .body("supportEmails[1].size()", is(0))
      .body("languages[1].size()", is(1))
      .body("languages[1][0]", is("fi"))
      .body("webPages[1].size()", is(0))
      .body("serviceHours[1].size()", is(0))
      .body("publishingStatus[1]", is("Published"));
  }

  @Test
  public void testListElectronicChannelsLimits() throws InterruptedException {
    String channelsUrl = "/electronicServiceChannels";
    waitApiListCount(channelsUrl, 3);
    assertListLimits(channelsUrl, 3);
  }
  
  @Test
  public void testListPhoneChannelsLimits() throws InterruptedException {
    String channelsUrl = "/phoneServiceChannels";
    waitApiListCount(channelsUrl, 3);
    assertListLimits(channelsUrl, 3);
  }
  
  @Test
  public void testListPrintableFormChannelsLimits() throws InterruptedException {
    String channelsUrl = "/printableFormServiceChannels";
    waitApiListCount(channelsUrl, 3);
    assertListLimits(channelsUrl, 3);
  }
  
  @Test
  public void testListServiceLocationChannelLimits() throws InterruptedException {
    String channelsUrl = "/serviceLocationServiceChannels";
    waitApiListCount(channelsUrl, 3);
    assertListLimits(channelsUrl, 3);
  }
  
  @Test
  public void testListWebPageChannelsLimits() throws InterruptedException {
    String channelsUrl = "/webPageServiceChannels";
    waitApiListCount(channelsUrl, 3);
    assertListLimits(channelsUrl, 3);
  }
  
  @Test
  public void testElectronicChannelNotFound() throws InterruptedException {
    String electronicChannelId = getElectronicChannelId(0);
    String phoneChannelId = getPhoneChannelId(0);
    String[] malformedIds = new String[] {"evil", "*", "/", "1", "-1", "~"};
    
    assertFound(String.format("/electronicServiceChannels/%s", electronicChannelId));
    
    for (String malformedId : malformedIds) {
      assertNotFound(String.format("/electronicServiceChannels/%s", malformedId));
    }
    
    assertNotFound(String.format("/electronicServiceChannels/%s", phoneChannelId));
  }
  
  @Test
  public void testPhoneChannelNotFound() throws InterruptedException {
    String electronicChannelId = getElectronicChannelId(0);
    String phoneChannelId = getPhoneChannelId(0);
    String[] malformedIds = new String[] {"evil", "*", "/", "1", "-1", "~"};
    
    assertFound(String.format("/phoneServiceChannels/%s", phoneChannelId));
    
    for (String malformedId : malformedIds) {
      assertNotFound(String.format("/phoneServiceChannels/%s", malformedId));
    }
    
    assertNotFound(String.format("/phoneServiceChannels/%s", electronicChannelId));
  }
  
  @Test
  public void testPrintableFormChannelNotFound() throws InterruptedException {
    String electronicChannelId = getElectronicChannelId(0);
    String printableFormChannelId = getPrintableFormChannelId(0);
    String[] malformedIds = new String[] {"evil", "*", "/", "1", "-1", "~"};
    
    assertFound(String.format("/printableFormServiceChannels/%s", printableFormChannelId));
    
    for (String malformedId : malformedIds) {
      assertNotFound(String.format("/printableFormServiceChannels/%s", malformedId));
    }
    
    assertNotFound(String.format("/printableFormServiceChannels/%s", electronicChannelId));
  }

  @Test
  public void testServiceLocationChannelNotFound() throws InterruptedException {
    String electronicChannelId = getElectronicChannelId(0);
    String serviceLocationChannelId = getServiceLocationChannelId(0);
    String[] malformedIds = new String[] {"evil", "*", "/", "1", "-1", "~"};
    
    assertFound(String.format("/serviceLocationServiceChannels/%s", serviceLocationChannelId));
    
    for (String malformedId : malformedIds) {
      assertNotFound(String.format("/serviceLocationServiceChannels/%s", malformedId));
    }
    
    assertNotFound(String.format("/serviceLocationServiceChannels/%s", electronicChannelId));
  }

  @Test
  public void testWebPageChannelNotFound() throws InterruptedException {
    String electronicChannelId = getElectronicChannelId(0);
    String webPageChannelId = getWebPageChannelId(0);
    String[] malformedIds = new String[] {"evil", "*", "/", "1", "-1", "~"};
    
    assertFound(String.format("/webPageServiceChannels/%s", webPageChannelId));
    
    for (String malformedId : malformedIds) {
      assertNotFound(String.format("/webPageServiceChannels/%s", malformedId));
    }
    
    assertNotFound(String.format("/webPageServiceChannels/%s", electronicChannelId));
  }
  
  // TODO : Service REMOVE tests
}
