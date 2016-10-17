package fi.otavanopisto.kuntaapi.test.server.integration.restfulptv;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.jayway.restassured.http.ContentType;

import fi.otavanopisto.kuntaapi.server.integrations.ptv.PtvConsts;
import fi.otavanopisto.kuntaapi.test.AbstractIntegrationTest;

@SuppressWarnings ("squid:S1192")
public class ServiceChannelsTestsIT extends AbstractIntegrationTest {
  
  /**
   * Starts WireMock
   */
  @Rule
  public WireMockRule wireMockRule = new WireMockRule(getWireMockPort());
  
  @Before
  public void beforeTest() throws InterruptedException {
    createSettings();
    getPtvMocker()
      .mockOrganizations("0de268cf-1ea1-4719-8a6e-1150933b6b9e")
      .mockServices("6c9926b9-4aa0-4635-b66a-471af07dfec3")
      .mockElectronicServiceChannels("6c9926b9-4aa0-4635-b66a-471af07dfec3", "22472ece-95a0-4fef-a429-b4da689677b2", "44187ff9-71ed-40df-89f6-916be4f3baa6", "799e0e4f-4da7-4e7d-9e0e-f1370b80fc9a")
      .mockPhoneServiceChannels("6c9926b9-4aa0-4635-b66a-471af07dfec3", "108f0c61-bfba-4dd7-8f02-deb4e77c52d0", "626cdd7a-e205-42da-8ce5-82b3b7add258", "e9e86a9e-6593-469d-bc01-f1a59c28168d")
      .startMock();

    waitApiListCount("/organizations", 1);
    waitApiListCount("/services", 1);
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
  public void testFindElectronicChannel() throws InterruptedException {
    String serviceId = given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/services")
      .body().jsonPath().getString("id[0]");
      
    waitApiListCount(String.format("/services/%s/electronicChannels", serviceId), 3);
    
    String channelId = given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/services/{serviceId}/electronicChannels", serviceId)
      .body().jsonPath().getString("id[0]");

    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/services/{serviceId}/electronicChannels/{channelId}", serviceId, channelId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id", notNullValue())
      .body("type", is("EChannel"))
      .body("organizationId", notNullValue())
      .body("names.size()", is(1))
      .body("names[0].language", is("fi"))
      .body("names[0].value", is("Varhaiskasvatuksen asiointipalvelu DaisyNet"))
      .body("names[0].type", is("Name"))
      .body("descriptions.size()", is(2))
      .body("descriptions[1].language", is("fi"))
      .body("descriptions[1].value", is("DaisyNet on vanhempien ja päivähoidon henkilöstön viestinnän väline."))
      .body("descriptions[1].type", is("ShortDescription"))
      .body("signatureQuantity", is(0))
      .body("requiresSignature", is(false))
      .body("supportContacts.size()", is(1))
      .body("supportContacts[0].email", nullValue())
      .body("supportContacts[0].phone", nullValue())
      .body("supportContacts[0].phoneChargeDescription", is(""))
      .body("supportContacts[0].language", is("fi"))
      .body("supportContacts[0].serviceChargeTypes.size()", is(0))
      .body("requiresAuthentication", is(true))
      .body("urls.size()", is(1))
      .body("urls[0].language", is("fi"))
      .body("urls[0].value", is("https://dp1.daisynet.fi/mikkeli"))
      .body("languages.size()", is(1))
      .body("languages[0]", is("fi"))
      .body("attachments.size()", is(0))
      .body("webPages.size()", is(0))
      .body("serviceHours.size()", is(0))
      .body("publishingStatus", is("Published"));
  }
  
  @Test
  public void testListElectronicChannels() throws InterruptedException {
    String serviceId = given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/services")
      .body().jsonPath().getString("id[0]");
    
    waitApiListCount(String.format("/services/%s/electronicChannels", serviceId), 3);

    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/services/{serviceId}/electronicChannels", serviceId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(3))
      .body("id[1]", notNullValue())
      .body("type[1]", is("EChannel"))
      .body("organizationId[1]", notNullValue())
      .body("names[1].size()", is(1))
      .body("names[1][0].language", is("fi"))
      .body("names[1][0].value", is("Mikkelin kansalaisopiston kurssitarjonta ja ilmoittautuminen"))
      .body("names[1][0].type", is("Name"))
      .body("descriptions[1].size()", is(2))
      .body("descriptions[1][0].language", is("fi"))
      .body("descriptions[1][0].value", is("Kansalaisopiston ilmoittautumissivuilla voit selata kurssitarjontaa ja ilmoittautua kursseille."))
      .body("descriptions[1][0].type", is("ShortDescription"))
      .body("signatureQuantity[1]", is(0))
      .body("requiresSignature[1]", is(false))
      .body("supportContacts[1].size()", is(1))
      .body("supportContacts[1][0].email", is("kansalaisopisto@mikkeli.fi"))
      .body("supportContacts[1][0].phone", is("015 194 2929"))
      .body("supportContacts[1][0].phoneChargeDescription", is("paikallisverkkomaksu/paikallispuhelumaksu"))
      .body("supportContacts[1][0].language", is("fi"))
      .body("supportContacts[1][0].serviceChargeTypes.size()", is(0))
      .body("requiresAuthentication[1]", is(false))
      .body("urls[1].size()", is(1))
      .body("urls[1][0].language", is("fi"))
      .body("urls[1][0].value", is("https://opistopalvelut.fi/mikkeli/index.php"))
      .body("languages[1].size()", is(2))
      .body("languages[1][0]", is("fi"))
      .body("languages[1][1]", is("en"))
      .body("attachments[1].size()", is(0))
      .body("webPages[1].size()", is(0))
      .body("serviceHours[1].size()", is(0))
      .body("publishingStatus[1]", is("Published"));
  } 
  
  @Test
  public void testFindPhoneChannel() throws InterruptedException {
    String serviceId = given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/services")
      .body().jsonPath().getString("id[0]");
      
    waitApiListCount(String.format("/services/%s/phoneChannels", serviceId), 3);
    
    String channelId = given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/services/{serviceId}/phoneChannels", serviceId)
      .body().jsonPath().getString("id[0]");

    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/services/{serviceId}/phoneChannels/{channelId}", serviceId, channelId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id", notNullValue())
      .body("type", is("Phone"))
      .body("organizationId", notNullValue())
      .body("names.size()", is(1))
      .body("names[0].language", is("fi"))
      .body("names[0].value", is("Mikkelin kansalaisopisto"))
      .body("names[0].type", is("Name"))
      .body("descriptions.size()", is(1))
      .body("descriptions[0].language", is("fi"))
      .body("descriptions[0].value", is("Numerossa palvellaan kansalaisopiston kursseja ja kursseille ilmoittautumista koskevissa asioissa."))
      .body("descriptions[0].type", is("Description"))
      .body("phoneType", is("Phone"))
      .body("chargeTypes.size()", is(0))
      .body("supportContacts.size()", is(1))
      .body("supportContacts[0].email", is("kansalaisopisto@mikkeli.fi"))
      .body("supportContacts[0].phone", nullValue())
      .body("supportContacts[0].phoneChargeDescription", nullValue())
      .body("supportContacts[0].language", is("fi"))
      .body("supportContacts[0].serviceChargeTypes.size()", is(0))
      .body("phoneNumbers.size()", is(1))
      .body("phoneNumbers[0].value", is("+35815 1942929"))
      .body("phoneNumbers[0].language", is("fi"))
      .body("languages.size()", is(1))
      .body("languages[0]", is("fi"))
      .body("phoneChargeDescriptions.size()", is(1))
      .body("phoneChargeDescriptions[0].value", is("paikallisverkkomaksu (pvm), matkapuhelinmaksu (mpm), ulkomaanpuhelumaksu"))
      .body("phoneChargeDescriptions[0].language", is("fi"))
      .body("webPages.size()", is(1))
      .body("webPages[0].description", nullValue())
      .body("webPages[0].url", is("http://kansalaisopisto.mikkeli.fi/"))
      .body("webPages[0].language", is("fi"))
      .body("webPages[0].value", is("Mikkelin kansalaisopisto kotisivu"))
      .body("webPages[0].type", is("HomePage"))
      .body("serviceHours.size()", is(2))
      .body("serviceHours[0].type", is("Standard"))
      .body("serviceHours[0].validFrom", is((String) null))
      .body("serviceHours[0].validTo", is((String) null))
      .body("serviceHours[0].opens", is("09:00"))
      .body("serviceHours[0].closes", is("16:00"))
      .body("serviceHours[0].status", is("OPEN"))
      .body("serviceHours[0].days.size()", is(4))
      .body("serviceHours[0].days[0]", is(1))
      .body("serviceHours[0].days[1]", is(2))
      .body("serviceHours[0].days[2]", is(3))
      .body("serviceHours[0].days[3]", is(4))
      .body("serviceHours[0].additionalInformation.size()", is(1))
      .body("serviceHours[0].additionalInformation[0].value", is("Suljettu klo 12.00-12.30 välisenä aikana."))
      .body("serviceHours[0].additionalInformation[0].language", is("fi"))
      .body("serviceHours[1].type", is("Standard"))
      .body("serviceHours[1].validFrom", nullValue())
      .body("serviceHours[1].validTo", nullValue())
      .body("serviceHours[1].opens", nullValue())
      .body("serviceHours[1].closes", nullValue())
      .body("serviceHours[1].status", is("CLOSED"))
      .body("serviceHours[1].days.size()", is(3))
      .body("serviceHours[1].days[0]", is(5))
      .body("serviceHours[1].days[1]", is(6))
      .body("serviceHours[1].days[2]", is(0))
      .body("serviceHours[1].additionalInformation.size()", is(0))
      .body("publishingStatus", is("Published"));
  }
  
  @Test
  public void testListPhoneChannels() throws InterruptedException {
    String serviceId = given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/services")
      .body().jsonPath().getString("id[0]");
      
    waitApiListCount(String.format("/services/%s/phoneChannels", serviceId), 3);

    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/services/{serviceId}/phoneChannels", serviceId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(3))
      .body("id[1]", notNullValue())
      .body("type[1]", is("Phone"))
      .body("organizationId[1]", notNullValue())
      .body("names[1].size()", is(1))
      .body("names[1][0].language", is("fi"))
      .body("names[1][0].value", is("Osoitepalvelupuhelin"))
      .body("names[1][0].type", is("Name"))
      .body("descriptions[1].size()", is(1))
      .body("descriptions[1][0].language", is("fi"))
      .body("descriptions[1][0].value", is("Puhelinpalvelusta voit saada lähes kaikkien Suomessa vakinaisesti asuvien henkilöiden ajantasaiset osoitteet. Palvelusta löytyvät myös ulkomailla asuvien suomalaisten osoitteet, mikäli he ovat ilmoittaneet voimassa olevan osoitteensa maistraatille. Palvelun tiedot perustuvat Väestörekisterikeskuksen ja maistraattien ylläpitämän väestötietojärjestelmän tietoihin. Palveluun voit soittaa ainoastaan Suomesta."))
      .body("descriptions[1][0].type", is("Description"))
      .body("phoneType[1]", is("Phone"))
      .body("chargeTypes[1].size()", is(0))
      .body("supportContacts[1].size()", is(0))
      .body("phoneNumbers[1].size()", is(1))
      .body("phoneNumbers[1][0].value", is("0600 0 1000"))
      .body("phoneNumbers[1][0].language", is("fi"))
      .body("languages[1].size()", is(1))
      .body("languages[1][0]", is("fi"))
      .body("phoneChargeDescriptions[1].size()", is(1))
      .body("phoneChargeDescriptions[1][0].value", is("Palvelun hinta on 1,98 euroa/minuutti + pvm/mpm."))
      .body("phoneChargeDescriptions[1][0].language", is("fi"))
      .body("webPages[1].size()", is(0))
      .body("supportContacts[1].size()", is(0))
      .body("serviceHours[1].size()", is(1))
      .body("serviceHours[1][0].type", is("Standard"))
      .body("serviceHours[1][0].validFrom", is((String) null))
      .body("serviceHours[1][0].validTo", is((String) null))
      .body("serviceHours[1][0].opens", is("08:00"))
      .body("serviceHours[1][0].closes", is("22:00"))
      .body("serviceHours[1][0].status", is("OPEN"))
      .body("serviceHours[1][0].days.size()", is(7))
      .body("serviceHours[1][0].days[0]", is(1))
      .body("serviceHours[1][0].days[1]", is(2))
      .body("serviceHours[1][0].days[2]", is(3))
      .body("serviceHours[1][0].days[3]", is(4))
      .body("serviceHours[1][0].days[4]", is(5))
      .body("serviceHours[1][0].days[5]", is(6))
      .body("serviceHours[1][0].days[6]", is(0))
      .body("serviceHours[1][0].additionalInformation.size()", is(0))
      .body("publishingStatus[1]", is("Published"));
  }
  
}
