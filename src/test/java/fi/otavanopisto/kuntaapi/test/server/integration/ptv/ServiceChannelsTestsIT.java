package fi.otavanopisto.kuntaapi.test.server.integration.ptv;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.hamcrest.core.StringStartsWith.startsWith;

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
    getPtvOrganizationMocker().mock(TestPtvConsts.ORGANIZATIONS);
    getPtvServiceMocker().mock(TestPtvConsts.SERVICES);    
    getPtvServiceChannelMocker().mock(TestPtvConsts.SERVICE_CHANNELS);

    startMocks();

    waitApiListCount("/electronicServiceChannels", TestPtvConsts.ELECTRONIC_CHANNEL_SERVICE_CHANNELS.length);
    waitApiListCount("/phoneServiceChannels", TestPtvConsts.PHONE_SERVICE_CHANNELS.length);
    waitApiListCount("/printableFormServiceChannels", TestPtvConsts.PRINTABLE_FORM_SERVICE_CHANNELS.length);
    waitApiListCount("/serviceLocationServiceChannels", TestPtvConsts.SERVICE_LOCATION_SERVICE_CHANNELS.length);
    waitApiListCount("/webPageServiceChannels", TestPtvConsts.WEB_PAGE_SERVICE_CHANNELS.length);
    
    waitApiListCount("/organizations", TestPtvConsts.ORGANIZATIONS.length);
    waitApiListCount("/services", TestPtvConsts.SERVICES.length);
  }

  @Test
  public void testFindElectronicChannel() throws InterruptedException {
    String channelId = givenReadonly()
      .contentType(ContentType.JSON)
      .get("/electronicServiceChannels")
      .body().jsonPath().getString("id[0]");

    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/electronicServiceChannels/{channelId}", channelId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id", notNullValue())
      .body("organizationId", is(getOrganizationId(0)))
      .body("names.size()", is(1))
      .body("names[0].language", is("fi"))
      .body("names[0].value", is("Wilma"))
      .body("names[0].type", is("Name"))
      .body("descriptions.size()", is(2))
      .body("descriptions[1].language", is("fi"))
      .body("descriptions[1].value", startsWith("Wilma on peruskoulujen ja lukioiden hallinto-ohjelman www-liittymä."))
      .body("descriptions[1].type", is("Description"))
      .body("signatureQuantity", is(0))
      .body("requiresSignature", is(false))
      .body("supportPhones.size()", is(0))
      .body("supportEmails.size()", is(0))
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
      .body("serviceHours[0].additionalInformation.size()", is(0))
      .body("serviceHours[0].openingHour.size()", is(0))
      .body("publishingStatus", is("Published"));
  }
  
  @Test
  public void testListElectronicChannels() throws InterruptedException {
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/electronicServiceChannels")
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(2))
      
      .body("organizationId[1]", is(getOrganizationId(0)))
      .body("names[1].size()", is(1))
      .body("names[1][0].language", is("fi"))
      .body("names[1][0].value", is("Testiverkkoasiointikanava"))
      .body("names[1][0].type", is("Name"))
      .body("descriptions[1].size()", is(2))
      .body("descriptions[1][1].language", is("fi"))
      .body("descriptions[1][1].value", is("Testausta varten tehty verkkoasiointikanava"))
      .body("descriptions[1][1].type", is("Description"))
      .body("signatureQuantity[1]", is(0))
      .body("requiresSignature[1]", is(false))
      .body("supportPhones[1].size()", is(0))
      .body("supportEmails[1].size()", is(0))
      .body("requiresAuthentication[1]", is(true))
      .body("urls[1].size()", is(1))
      .body("urls[1][0].language", is("fi"))
      .body("urls[1][0].value", is("https://www.google.com"))
      .body("languages[1].size()", is(0))
      .body("attachments[1].size()", is(0))
      .body("webPages[1].size()", is(0))
      .body("serviceHours[1].size()", is(0))
      .body("publishingStatus[1]", is("Published"));
  }
  
  @Test
  public void testFindPhoneChannel() throws InterruptedException {
    String channelId = givenReadonly()
      .contentType(ContentType.JSON)
      .get("/phoneServiceChannels")
      .body().jsonPath().getString("id[0]");

    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/phoneServiceChannels/{channelId}", channelId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id", notNullValue())
      .body("organizationId", is(getOrganizationId(0)))
      .body("names.size()", is(1))
      .body("names[0].language", is("fi"))
      .body("names[0].value", is("Testi puhelinasiointikanava"))
      .body("names[0].type", is("Name"))
      .body("descriptions.size()", is(2))
      .body("descriptions[1].language", is("fi"))
      .body("descriptions[1].value", is("Puhelinasiointikanava testausta varten"))
      .body("descriptions[1].type", is("Description"))
      .body("phoneNumbers.size()", is(1))
      .body("phoneNumbers[0].serviceChargeType", is("Charged"))
      .body("phoneNumbers[0].chargeDescription", is("Testiajalta peritään lisämaksu"))
      .body("phoneNumbers[0].prefixNumber", is("+358"))
      .body("phoneNumbers[0].isFinnishServiceNumber", is(false))
      .body("phoneNumbers[0].number", is("9876543"))
      .body("phoneNumbers[0].language", is("fi"))
      .body("phoneNumbers[0].type", is("Phone"))
      .body("supportEmails.size()", is(0))
      .body("languages.size()", is(1))
      .body("languages[0]", is("fi"))
      .body("webPages.size()", is(0))
      .body("serviceHours.size()", is(0))
      .body("publishingStatus", is("Published"));
  }
  
  @Test
  public void testListPhoneChannels() throws InterruptedException {
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/phoneServiceChannels")
      .then()
      .assertThat()
      .statusCode(200)
      .body("id[1]", notNullValue())
      .body("organizationId[1]", is(getOrganizationId(0)))
      .body("names[1].size()", is(1))
      .body("names[1][0].language", is("fi"))
      .body("names[1][0].value", is("Metatavun auttava puhelin"))
      .body("names[1][0].type", is("Name"))
      .body("descriptions[1].size()", is(2))
      .body("descriptions[1][1].language", is("fi"))
      .body("descriptions[1][1].value", is("Testataan puhelinkanavien aukioloaikoja."))
      .body("descriptions[1][1].type", is("Description"))
      .body("phoneNumbers[1].size()", is(1))
      .body("phoneNumbers[1][0].serviceChargeType", is("Charged"))
      .body("phoneNumbers[1][0].chargeDescription", nullValue())
      .body("phoneNumbers[1][0].prefixNumber", is("+358"))
      .body("phoneNumbers[1][0].isFinnishServiceNumber", is(false))
      .body("phoneNumbers[1][0].number", is("503263840"))
      .body("phoneNumbers[1][0].language", is("fi"))
      .body("phoneNumbers[1][0].type", is("Phone"))
      .body("supportEmails[1].size()", is(0))
      .body("languages[1].size()", is(1))
      .body("languages[1][0]", is("fi"))
      .body("webPages[1].size()", is(0))
      .body("serviceHours[1].size()", is(5))
      .body("serviceHours[1][0].serviceHourType", is("Standard"))
      .body("serviceHours[1][0].validFrom", nullValue())
      .body("serviceHours[1][0].validTo", nullValue())
      .body("serviceHours[1][0].isClosed", is(false))
      .body("serviceHours[1][0].validForNow", is(true))
      .body("serviceHours[1][0].additionalInformation.size()", is(0))
      .body("serviceHours[1][0].openingHour.size()", is(5))
      .body("serviceHours[1][0].openingHour[0].dayFrom", is(1))
      .body("serviceHours[1][0].openingHour[0].dayTo", nullValue())
      .body("serviceHours[1][0].openingHour[0].from", is("08:00:00"))
      .body("serviceHours[1][0].openingHour[0].to", is("16:00:00"))
      .body("serviceHours[1][0].openingHour[0].isExtra", is(false))
      
      .body("serviceHours[1][1].serviceHourType", is("Special"))
      .body("serviceHours[1][1].validFrom", sameInstant(getInstant(2017, 4, 6, 0, 0, 0, TIMEZONE_ID)))
      .body("serviceHours[1][1].validTo", sameInstant(getInstant(2017, 4, 20, 0, 0, 0, TIMEZONE_ID)))
      .body("serviceHours[1][1].isClosed", is(false))
      .body("serviceHours[1][1].validForNow", is(false))
      .body("serviceHours[1][1].additionalInformation.size()", is(0))
      .body("serviceHours[1][1].openingHour.size()", is(1))
      .body("serviceHours[1][1].openingHour[0].dayFrom", is(1))
      .body("serviceHours[1][1].openingHour[0].dayTo", is(5))
      .body("serviceHours[1][1].openingHour[0].from", is("02:15:00"))
      .body("serviceHours[1][1].openingHour[0].to", is("0:45:00"))
      .body("serviceHours[1][1].openingHour[0].isExtra", is(false))
  
      .body("serviceHours[1][2].serviceHourType", is("Exception"))
      .body("serviceHours[1][2].validFrom", sameInstant(getInstant(2017, 4, 5, 0, 0, 0, TIMEZONE_ID)))
      .body("serviceHours[1][2].validTo", nullValue())
      .body("serviceHours[1][2].isClosed", is(false))
      .body("serviceHours[1][2].validForNow", is(false))
      .body("serviceHours[1][2].additionalInformation.size()", is(0))
      .body("serviceHours[1][2].openingHour.size()", is(1))
      .body("serviceHours[1][2].openingHour[0].dayFrom", nullValue())
      .body("serviceHours[1][2].openingHour[0].dayTo", nullValue())
      .body("serviceHours[1][2].openingHour[0].from", is("01:00:00"))
      .body("serviceHours[1][2].openingHour[0].to", is("3:00:00"))
      .body("serviceHours[1][2].openingHour[0].isExtra", is(false))
  
      .body("serviceHours[1][4].serviceHourType", is("Exception"))
      .body("serviceHours[1][4].validFrom", sameInstant(getInstant(2017, 6, 8, 0, 0, 0, TIMEZONE_ID)))
      .body("serviceHours[1][4].validTo", sameInstant(getInstant(2017, 6, 22, 0, 0, 0, TIMEZONE_ID)))
      .body("serviceHours[1][4].isClosed", is(true))
      .body("serviceHours[1][4].validForNow", is(false))
      .body("serviceHours[1][4].additionalInformation.size()", is(0))
      .body("serviceHours[1][4].openingHour.size()", is(0))
  
      .body("publishingStatus[1]", is("Published"));
  }
  
  @Test
  public void testFindPrintableFormChannel() throws InterruptedException {
    String channelId = givenReadonly()
      .contentType(ContentType.JSON)
      .get("/printableFormServiceChannels")
      .body().jsonPath().getString("id[0]");
 
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/printableFormServiceChannels/{channelId}", channelId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id", notNullValue())
      .body("organizationId", is(getOrganizationId(0)))
      .body("names.size()", is(1))
      .body("names[0].language", is("fi"))
      .body("names[0].value", is("Hakemus vieraan oppilasalueen kouluun"))
      .body("names[0].type", is("Name"))
      .body("descriptions.size()", is(2))
      .body("descriptions[0].language", is("fi"))
      .body("descriptions[0].value", is("Hakemus koulunkäyntiin muussa kuin omassa lähikoulussa."))
      .body("descriptions[0].type", is("ShortDescription"))
      .body("formIdentifier.size()", is(0))
      .body("formReceiver.size()", is(0))
      .body("deliveryAddress", nullValue())
      .body("channelUrls.size()", is(1))
      .body("channelUrls[0].language", is("fi"))
      .body("channelUrls[0].value", is("http://www.mikkeli.fi/sites/mikkeli.fi/files/atoms/files/hakemus_vieraan_oppilasalueen_kouluun_2014.pdf"))
      .body("channelUrls[0].type", is("PDF"))
      .body("attachments.size()", is(0))
      .body("supportPhones.size()", is(0))
      .body("supportEmails.size()", is(0))
      .body("languages.size()", is(1))
      .body("languages[0]", is("fi"))
      .body("webPages.size()", is(0))
      .body("serviceHours.size()", is(0))
      .body("publishingStatus", is("Published"));
  }

  @Test
  public void testListPrintableFormChannels() throws InterruptedException {
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/printableFormServiceChannels")
      .then()
      .assertThat()
      .statusCode(200)
      .body("id[1]", notNullValue())
      .body("organizationId[1]", is(getOrganizationId(0)))
      .body("names[1].size()", is(1))
      .body("names[1][0].language", is("fi"))
      .body("names[1][0].value", is("Hakemus muun kuin lähikoulun 7. luokalle"))
      .body("names[1][0].type", is("Name"))
      .body("descriptions[1].size()", is(2))
      .body("descriptions[1][0].language", is("fi"))
      .body("descriptions[1][0].value", is("Hakemus siirtymisestä 7. luokalle muuhun kuin osoitteenmukaiseen lähikouluun."))
      .body("descriptions[1][0].type", is("ShortDescription"))
      .body("formIdentifier[1].size()", is(0))
      .body("formReceiver[1].size()", is(0))
      .body("deliveryAddress[1]", nullValue())
      .body("channelUrls[1].size()", is(1))
      .body("channelUrls[1][0].language", is("fi"))
      .body("channelUrls[1][0].value", is("http://www.mikkeli.fi/sites/mikkeli.fi/files/atoms/files/hakemus_muun_kuin_lahikoulun_7.luokalle_2014.pdf"))
      .body("channelUrls[1][0].type", is("PDF"))
      .body("attachments[1].size()", is(0))
      .body("supportPhones[1].size()", is(0))
      .body("supportEmails[1].size()", is(0))
      .body("languages[1].size()", is(1))
      .body("languages[1][0]", is("fi"))
      .body("webPages[1].size()", is(0))
      .body("serviceHours[1].size()", is(0))
      .body("publishingStatus[1]", is("Published"));
  }
  
  @Test
  public void testFindServiceLocationChannel() throws InterruptedException {
    waitApiListCount("/serviceLocationServiceChannels", TestPtvConsts.SERVICE_LOCATION_SERVICE_CHANNELS.length);
    
    String channelId = givenReadonly()
      .contentType(ContentType.JSON)
      .get("/serviceLocationServiceChannels")
      .body().jsonPath().getString("id[0]");
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/serviceLocationServiceChannels/{channelId}", channelId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id", notNullValue())
      .body("organizationId", notNullValue())
      .body("names.size()", is(1))
      .body("names[0].language", is("fi"))
      .body("names[0].value", is("Saksalan päiväkoti"))
      .body("names[0].type", is("Name"))
      .body("descriptions.size()", is(2))
      .body("descriptions[0].language", is("fi"))
      .body("descriptions[0].value", is("Saksalan päiväkoti järjestää vuoropäivähoitoa ja esiopetusta kolmessa lapsiryhmässä."))
      .body("descriptions[0].type", is("ShortDescription"))
      .body("phoneNumbers.size()", is(2))
      .body("phoneNumbers[0].serviceChargeType", is("Other"))
      .body("phoneNumbers[0].chargeDescription", nullValue())
      .body("phoneNumbers[0].prefixNumber", nullValue())
      .body("phoneNumbers[0].isFinnishServiceNumber", is(true))
      .body("phoneNumbers[0].number", is("000"))
      .body("phoneNumbers[0].language", is("fi"))
      .body("phoneNumbers[0].type", is("Fax"))
      .body("emails.size()", is(0))
      .body("languages.size()", is(1))
      .body("languages[0]", is("fi"))
      .body("webPages.size()", is(2))
      .body("webPages[0].value", is("Saksalan päiväkodin blogi"))
      .body("webPages[0].url", is("http://saksalapk.blogspot.fi/"))
      .body("webPages[0].language", is("fi"))
      .body("areas.size()", is(1))
      .body("areas[0].code", nullValue())
      .body("areas[0].name.size()", is(0))
      .body("areas[0].municipalities.size()", is(1))
      .body("areas[0].municipalities[0].code", is("491"))
      .body("areas[0].municipalities[0].names.size()", is(2))
      .body("areas[0].municipalities[0].names[0].value", is("S:t Michel"))
      .body("areas[0].municipalities[0].names[0].language", is("sv"))
      .body("addresses.size()", is(1))
      .body("addresses[0].latitude", is("6839060.668"))
      .body("addresses[0].longitude", is("514295.022"))
      .body("addresses[0].coordinateState", is("Ok"))
      .body("addresses[0].coordinates.epsg3067.latitude", is("6839060.668"))
      .body("addresses[0].coordinates.epsg3067.longitude", is("514295.022"))
      .body("addresses[0].coordinates.epsg4326.latitude", is("61.68447357132788"))
      .body("addresses[0].coordinates.epsg4326.longitude", is("27.270135123294395"))
      .body("addresses[0].type", is("Location"))
      .body("addresses[0].subtype", is("Single"))
      .body("addresses[0].postOfficeBox", nullValue())
      .body("addresses[0].streetAddress.size()", is(1))
      .body("addresses[0].streetAddress[0].value", is("Maaherrankatu "))
      .body("addresses[0].streetAddress[0].language", is("fi"))
      .body("addresses[0].streetNumber", is("1"))
      .body("addresses[0].postalCode", is("50170"))
      .body("addresses[0].postOffice.size()", is(2))
      .body("addresses[0].postOffice[0].value", is("MIKKELI"))
      .body("addresses[0].postOffice[0].language", is("sv"))
      .body("addresses[0].municipality.code", is("491"))
      .body("addresses[0].municipality.names.size()", is(2))
      .body("addresses[0].municipality.names[0].value", is("S:t Michel"))
      .body("addresses[0].municipality.names[0].language", is("sv"))
      .body("addresses[0].country", is("FI"))
      .body("addresses[0].additionalInformations.size()", is(0))
      .body("addresses[0].locationAbroad.size()", is(0))
      .body("addresses[0].multipointLocation.size()", is(0))
      .body("serviceHours.size()", is(1))
      .body("serviceHours[0].serviceHourType", is("Standard"))
      .body("serviceHours[0].validFrom", nullValue())
      .body("serviceHours[0].validTo", nullValue())
      .body("serviceHours[0].isClosed", is(false))
      .body("serviceHours[0].validForNow", is(true))
      .body("serviceHours[0].additionalInformation.size()", is(0))
      .body("serviceHours[0].openingHour.size()", is(5))
      .body("serviceHours[0].openingHour[0].dayFrom", is(1))
      .body("serviceHours[0].openingHour[0].dayTo", is(1))
      .body("serviceHours[0].openingHour[0].from", is("05:00:00"))
      .body("serviceHours[0].openingHour[0].to", is("23:00:00"))
      .body("serviceHours[0].openingHour[0].isExtra", is(false))
      .body("publishingStatus", is("Published"));
  }

  @Test
  public void testListServiceLocationChannel() throws InterruptedException {
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/serviceLocationServiceChannels")
      .then()
      .assertThat()
      .statusCode(200)
      .body("id[4]", notNullValue())
      .body("organizationId[4]", notNullValue())
      .body("names[4].size()", is(1))
      .body("names[4][0].language", is("fi"))
      .body("names[4][0].value", is("Metatavun toimisto"))
      .body("names[4][0].type", is("Name"))
      .body("descriptions[4].size()", is(2))
      .body("descriptions[4][1].language", is("fi"))
      .body("descriptions[4][1].value", is("Metatavun toimiston aukioloajat x"))
      .body("descriptions[4][1].type", is("Description"))
      .body("phoneNumbers[4].size()", is(1))
      .body("phoneNumbers[4][0].serviceChargeType", is("Charged"))
      .body("phoneNumbers[4][0].chargeDescription", nullValue())
      .body("phoneNumbers[4][0].prefixNumber", is("+358"))
      .body("phoneNumbers[4][0].isFinnishServiceNumber", is(false))
      .body("phoneNumbers[4][0].number", is("442909201"))
      .body("phoneNumbers[4][0].language", is("fi"))
      .body("phoneNumbers[4][0].type", is("Phone"))
      .body("emails[4].size()", is(1))
      .body("emails[4][0].language", is("fi"))
      .body("emails[4][0].value", is("info@metatavu.fi"))
      .body("languages[4].size()", is(1))
      .body("languages[4][0]", is("fi"))
      .body("webPages[4].size()", is(0))
      .body("areas[4].size()", is(0))
      .body("addresses[4].size()", is(1))
      .body("addresses[4][0].type", is("Location"))
      .body("addresses[4][0].subtype", is("Single"))
      .body("addresses[4][0].postOfficeBox", nullValue())
      .body("addresses[4][0].postalCode", is("50100"))
      .body("addresses[4][0].postOffice.size()", is(2))
      .body("addresses[4][0].postOffice[0].value", is("MIKKELI"))
      .body("addresses[4][0].postOffice[0].language", is("sv"))
      .body("addresses[4][0].streetAddress.size()", is(1))
      .body("addresses[4][0].streetAddress[0].value", is("Rouhialankatu"))
      .body("addresses[4][0].streetAddress[0].language", is("fi"))
      .body("addresses[4][0].streetNumber", is("4"))
      .body("addresses[4][0].municipality.code", is("491"))
      .body("addresses[4][0].municipality.names.size()", is(2))
      .body("addresses[4][0].municipality.names[0].value", is("S:t Michel"))
      .body("addresses[4][0].municipality.names[0].language", is("sv"))
      .body("addresses[4][0].country", is("FI"))
      .body("addresses[4][0].latitude", is("6840381.653"))
      .body("addresses[4][0].longitude", is("514607.162"))
      .body("addresses[4][0].qualifier", nullValue())
      .body("addresses[4][0].additionalInformations.size()", is(1))
      .body("addresses[4][0].additionalInformations[0].value", is("käynti sisäpihalta"))
      .body("addresses[4][0].additionalInformations[0].language", is("fi"))

      .body("serviceHours[4].size()", is(3))
      .body("serviceHours[4][0].serviceHourType", is("Standard"))
      .body("serviceHours[4][1].serviceHourType", is("Standard"))
      .body("serviceHours[4][2].serviceHourType", is("Exception"))
      
      .body("serviceHours[4][0].validFrom", nullValue())
      .body("serviceHours[4][0].validTo", nullValue())
      .body("serviceHours[4][0].isClosed", is(false))
      .body("serviceHours[4][0].validForNow", is(true))
      .body("serviceHours[4][0].additionalInformation.size()", is(0))
      .body("serviceHours[4][0].openingHour.size()", is(5))
      
      .body("serviceHours[4][0].openingHour[0].dayFrom", is(1))
      .body("serviceHours[4][0].openingHour[0].dayTo", nullValue())
      .body("serviceHours[4][0].openingHour[0].from", is("09:00:00"))
      .body("serviceHours[4][0].openingHour[0].to", is("17:00:00"))
      .body("serviceHours[4][0].openingHour[0].isExtra", is(false))      
      
      .body("serviceHours[4][0].openingHour[1].dayFrom", is(2))
      .body("serviceHours[4][0].openingHour[1].dayTo", nullValue())
      .body("serviceHours[4][0].openingHour[1].from", is("09:00:00"))
      .body("serviceHours[4][0].openingHour[1].to", is("17:00:00"))
      .body("serviceHours[4][0].openingHour[1].isExtra", is(false))      
      
      .body("serviceHours[4][0].openingHour[2].dayFrom", is(3))
      .body("serviceHours[4][0].openingHour[2].dayTo", nullValue())
      .body("serviceHours[4][0].openingHour[2].from", is("09:00:00"))
      .body("serviceHours[4][0].openingHour[2].to", is("16:30:00"))
      .body("serviceHours[4][0].openingHour[2].isExtra", is(false))           
      
      .body("serviceHours[4][0].openingHour[3].dayFrom", is(4))
      .body("serviceHours[4][0].openingHour[3].dayTo", nullValue())
      .body("serviceHours[4][0].openingHour[3].from", is("09:00:00"))
      .body("serviceHours[4][0].openingHour[3].to", is("17:00:00"))
      .body("serviceHours[4][0].openingHour[3].isExtra", is(false))           
      
      .body("serviceHours[4][0].openingHour[4].dayFrom", is(5))
      .body("serviceHours[4][0].openingHour[4].dayTo", nullValue())
      .body("serviceHours[4][0].openingHour[4].from", is("10:00:00"))
      .body("serviceHours[4][0].openingHour[4].to", is("14:00:00"))
      .body("serviceHours[4][0].openingHour[4].isExtra", is(false))      
      
      .body("serviceHours[4][1].serviceHourType", is("Standard"))
      .body("serviceHours[4][1].validFrom", sameInstant(getInstant(2017, 4, 3, 0, 0, TIMEZONE_ID)))
      .body("serviceHours[4][1].validTo", sameInstant(getInstant(2017, 4, 9, 0, 0, TIMEZONE_ID)))
      .body("serviceHours[4][1].isClosed", is(false))
      .body("serviceHours[4][1].validForNow", is(false))
      .body("serviceHours[4][1].additionalInformation.size()", is(1))
      .body("serviceHours[4][1].additionalInformation[0].value", is("Huhtikuun ensimmäinen viikko"))
      .body("serviceHours[4][1].additionalInformation[0].language", is("fi"))
      .body("serviceHours[4][1].openingHour.size()", is(2))
      
      .body("serviceHours[4][1].openingHour[0].dayFrom", is(1))
      .body("serviceHours[4][1].openingHour[0].dayTo", nullValue())
      .body("serviceHours[4][1].openingHour[0].from", is("08:00:00"))
      .body("serviceHours[4][1].openingHour[0].to", is("16:00:00"))
      .body("serviceHours[4][1].openingHour[0].isExtra", is(false))      

      .body("serviceHours[4][1].openingHour[1].dayFrom", is(2))
      .body("serviceHours[4][1].openingHour[1].dayTo", nullValue())
      .body("serviceHours[4][1].openingHour[1].from", is("08:00:00"))
      .body("serviceHours[4][1].openingHour[1].to", is("17:00:00"))
      .body("serviceHours[4][1].openingHour[1].isExtra", is(false))      
      
      .body("serviceHours[4][2].serviceHourType", is("Exception"))
      .body("serviceHours[4][2].validFrom", sameInstant(getInstant(2017, 2, 27, 0, 0, TIMEZONE_ID)))
      .body("serviceHours[4][2].validTo", nullValue())
      .body("serviceHours[4][2].isClosed", is(false))
      .body("serviceHours[4][2].validForNow", is(false))
      .body("serviceHours[4][2].additionalInformation.size()", is(1))
      .body("serviceHours[4][2].additionalInformation[0].value", is("Poikkeuksellisesti suljettu"))
      .body("serviceHours[4][2].additionalInformation[0].language", is("fi"))
      .body("serviceHours[4][2].openingHour.size()", is(1))
      .body("serviceHours[4][2].openingHour[0].dayFrom", nullValue())
      .body("serviceHours[4][2].openingHour[0].dayTo", nullValue())
      .body("serviceHours[4][2].openingHour[0].from", is("01:00:00"))
      .body("serviceHours[4][2].openingHour[0].to", is("1:15:00"))
      .body("serviceHours[4][2].openingHour[0].isExtra", is(false))
      
      .body("publishingStatus[4]", is("Published"));
  }
  
  @Test
  public void testListServiceLocationChannelSearch() {
    if (skipElasticSearchTests()) {
      return;
    }
    
    String search = "(Test*)|(Metatavu*)";
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("/serviceLocationServiceChannels?search=%s", search))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(2))
      .body("names[0][0].value", is("Testi"))
      .body("names[1][0].value", is("Metatavun toimisto"));
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("/serviceLocationServiceChannels?search=%s&sortBy=SCORE&sortDir=DESC", search))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(2))
      .body("names[0][0].value", is("Metatavun toimisto"))
      .body("names[1][0].value", is("Testi"));
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("/serviceLocationServiceChannels?search=%s&sortBy=SCORE&sortDir=ASC", search))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(2))
      .body("names[0][0].value", is("Testi"))
      .body("names[1][0].value", is("Metatavun toimisto"));
  }

  @Test
  public void testFindWebPageChannel() throws InterruptedException {
    String channelId = givenReadonly()
      .contentType(ContentType.JSON)
      .get("/webPageServiceChannels")
      .body().jsonPath().getString("id[0]");
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/webPageServiceChannels/{channelId}", channelId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id", notNullValue())
      .body("organizationId", notNullValue())
      .body("names.size()", is(1))
      .body("names[0].language", is("fi"))
      .body("names[0].value", is("Testi verkkosivu"))
      .body("names[0].type", is("Name"))
      .body("descriptions.size()", is(2))
      .body("descriptions[1].language", is("fi"))
      .body("descriptions[1].value", is("Verkkosivu testausta varten"))
      .body("descriptions[1].type", is("Description"))
      .body("urls.size()", is(1))
      .body("urls[0].language", is("fi"))
      .body("urls[0].value", is("https://www.google.com"))
      .body("supportPhones.size()", is(0))
      .body("supportEmails.size()", is(0))
      .body("languages.size()", is(1))
      .body("languages[0]", is("fi"))
      .body("webPages.size()", is(0))
      .body("serviceHours.size()", is(0))
      .body("publishingStatus", is("Published"));
  }

  @Test
  public void testListWebPageChannel() throws InterruptedException {
    waitApiListCount("/webPageServiceChannels", TestPtvConsts.WEB_PAGE_SERVICE_CHANNELS.length);
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/webPageServiceChannels")
      .then()
      .assertThat()
      .statusCode(200)
      .body("id[0]", notNullValue())
      .body("organizationId[0]", notNullValue())
      .body("names[0].size()", is(1))
      .body("names[0][0].language", is("fi"))
      .body("names[0][0].value", is("Testi verkkosivu"))
      .body("names[0][0].type", is("Name"))
      .body("descriptions[0].size()", is(2))
      .body("descriptions[0][1].language", is("fi"))
      .body("descriptions[0][1].value", is("Verkkosivu testausta varten"))
      .body("descriptions[0][1].type", is("Description"))
      .body("urls[0].size()", is(1))
      .body("urls[0][0].language", is("fi"))
      .body("urls[0][0].value", is("https://www.google.com"))
      .body("supportPhones[0].size()", is(0))
      .body("supportEmails[0].size()", is(0))
      .body("languages[0].size()", is(1))
      .body("languages[0][0]", is("fi"))
      .body("webPages[0].size()", is(0))
      .body("serviceHours[0].size()", is(0))
      .body("publishingStatus[0]", is("Published"));
  }

  @Test
  public void testListElectronicChannelsLimits() throws InterruptedException {
    String channelsUrl = "/electronicServiceChannels";
    waitApiListCount(channelsUrl, TestPtvConsts.ELECTRONIC_CHANNEL_SERVICE_CHANNELS.length);
    assertListLimits(channelsUrl, TestPtvConsts.ELECTRONIC_CHANNEL_SERVICE_CHANNELS.length);
  }
  
  @Test
  public void testListPhoneChannelsLimits() throws InterruptedException {
    String channelsUrl = "/phoneServiceChannels";
    waitApiListCount(channelsUrl, TestPtvConsts.PHONE_SERVICE_CHANNELS.length);
    assertListLimits(channelsUrl, TestPtvConsts.PHONE_SERVICE_CHANNELS.length);
  }
  
  @Test
  public void testListPrintableFormChannelsLimits() throws InterruptedException {
    String channelsUrl = "/printableFormServiceChannels";
    waitApiListCount(channelsUrl, TestPtvConsts.PRINTABLE_FORM_SERVICE_CHANNELS.length);
    assertListLimits(channelsUrl, TestPtvConsts.PRINTABLE_FORM_SERVICE_CHANNELS.length);
  }
  
  @Test
  public void testListServiceLocationChannelLimits() throws InterruptedException {
    String channelsUrl = "/serviceLocationServiceChannels";
    waitApiListCount(channelsUrl, TestPtvConsts.SERVICE_LOCATION_SERVICE_CHANNELS.length);
    assertListLimits(channelsUrl, TestPtvConsts.SERVICE_LOCATION_SERVICE_CHANNELS.length);
  }
  
  @Test
  public void testListWebPageChannelsLimits() throws InterruptedException {
    String channelsUrl = "/webPageServiceChannels";
    waitApiListCount(channelsUrl, TestPtvConsts.WEB_PAGE_SERVICE_CHANNELS.length);
    assertListLimits(channelsUrl, TestPtvConsts.WEB_PAGE_SERVICE_CHANNELS.length);
  }
  
  @Test
  public void testElectronicChannelNotFound() throws InterruptedException {
    String electronicChannelId = getElectronicChannelId(0, TestPtvConsts.ELECTRONIC_CHANNEL_SERVICE_CHANNELS.length);
    String phoneChannelId = getPhoneChannelId(0, TestPtvConsts.PHONE_SERVICE_CHANNELS.length);
    String[] malformedIds = new String[] {"evil", "*", "/", "1", "-1", "~"};
    
    assertFound(String.format("/electronicServiceChannels/%s", electronicChannelId));
    
    for (String malformedId : malformedIds) {
      assertNotFound(String.format("/electronicServiceChannels/%s", malformedId));
    }
    
    assertNotFound(String.format("/electronicServiceChannels/%s", phoneChannelId));
  }
  
  @Test
  public void testPhoneChannelNotFound() throws InterruptedException {
    String electronicChannelId = getElectronicChannelId(0, TestPtvConsts.ELECTRONIC_CHANNEL_SERVICE_CHANNELS.length);
    String phoneChannelId = getPhoneChannelId(0, TestPtvConsts.PHONE_SERVICE_CHANNELS.length);
    String[] malformedIds = new String[] {"evil", "*", "/", "1", "-1", "~"};
    
    assertFound(String.format("/phoneServiceChannels/%s", phoneChannelId));
    
    for (String malformedId : malformedIds) {
      assertNotFound(String.format("/phoneServiceChannels/%s", malformedId));
    }
    
    assertNotFound(String.format("/phoneServiceChannels/%s", electronicChannelId));
  }
  
  @Test
  public void testPrintableFormChannelNotFound() throws InterruptedException {
    String electronicChannelId = getElectronicChannelId(0, TestPtvConsts.ELECTRONIC_CHANNEL_SERVICE_CHANNELS.length);
    String printableFormChannelId = getPrintableFormChannelId(0, TestPtvConsts.PRINTABLE_FORM_SERVICE_CHANNELS.length);
    String[] malformedIds = new String[] {"evil", "*", "/", "1", "-1", "~"};
    
    assertFound(String.format("/printableFormServiceChannels/%s", printableFormChannelId));
    
    for (String malformedId : malformedIds) {
      assertNotFound(String.format("/printableFormServiceChannels/%s", malformedId));
    }
    
    assertNotFound(String.format("/printableFormServiceChannels/%s", electronicChannelId));
  }

  @Test
  public void testServiceLocationChannelNotFound() throws InterruptedException {
    String electronicChannelId = getElectronicChannelId(0, TestPtvConsts.ELECTRONIC_CHANNEL_SERVICE_CHANNELS.length);
    String serviceLocationChannelId = getServiceLocationChannelId(0, TestPtvConsts.SERVICE_LOCATION_SERVICE_CHANNELS.length);
    String[] malformedIds = new String[] {"evil", "*", "/", "1", "-1", "~"};
    
    assertFound(String.format("/serviceLocationServiceChannels/%s", serviceLocationChannelId));
    
    for (String malformedId : malformedIds) {
      assertNotFound(String.format("/serviceLocationServiceChannels/%s", malformedId));
    }
    
    assertNotFound(String.format("/serviceLocationServiceChannels/%s", electronicChannelId));
  }

  @Test
  public void testWebPageChannelNotFound() throws InterruptedException {
    String electronicChannelId = getElectronicChannelId(0, TestPtvConsts.ELECTRONIC_CHANNEL_SERVICE_CHANNELS.length);
    String webPageChannelId = getWebPageChannelId(0, TestPtvConsts.WEB_PAGE_SERVICE_CHANNELS.length);
    String[] malformedIds = new String[] {"evil", "*", "/", "1", "-1", "~"};
    
    assertFound(String.format("/webPageServiceChannels/%s", webPageChannelId));
    
    for (String malformedId : malformedIds) {
      assertNotFound(String.format("/webPageServiceChannels/%s", malformedId));
    }
    
    assertNotFound(String.format("/webPageServiceChannels/%s", electronicChannelId));
  }
  
  // TODO : Service REMOVE tests
}
