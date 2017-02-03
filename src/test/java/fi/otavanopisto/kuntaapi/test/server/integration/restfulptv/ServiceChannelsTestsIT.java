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
      .mockStatutoryDescriptions("2ddfcd49-b0a8-4221-8d8f-4c4d3c5c0ab8")
      .mockServices("6c9926b9-4aa0-4635-b66a-471af07dfec3", "822d5347-8398-4866-bb9d-9cdc60b38fba")
      .mockElectronicServiceChannels("6c9926b9-4aa0-4635-b66a-471af07dfec3", "22472ece-95a0-4fef-a429-b4da689677b2", "44187ff9-71ed-40df-89f6-916be4f3baa6", "799e0e4f-4da7-4e7d-9e0e-f1370b80fc9a")
      .mockPhoneServiceChannels("6c9926b9-4aa0-4635-b66a-471af07dfec3", "108f0c61-bfba-4dd7-8f02-deb4e77c52d0", "626cdd7a-e205-42da-8ce5-82b3b7add258", "e9e86a9e-6593-469d-bc01-f1a59c28168d")
      .mockPrintableFormServiceChannels("6c9926b9-4aa0-4635-b66a-471af07dfec3", "02256ce8-2879-47e4-a6f5-339872f0f758", "1a17f994-b924-46ae-8708-c09938125119", "6fb56241-1b43-4e42-8231-43ba8d86be36")
      .mockServiceLocationServiceChannels("6c9926b9-4aa0-4635-b66a-471af07dfec3", "9a9f5def-92e4-4b79-a49a-ccf20a0f75b6", "c0681f51-d1b4-4a9b-bbbf-ddf9a5273cd1", "cf927001-8b45-4f08-b93b-c78fe8477928")
      .mockWebPageServiceChannels("6c9926b9-4aa0-4635-b66a-471af07dfec3", "4b08ae17-75ae-4746-9382-1316c4ec02c5", "aedae320-a2b2-4fe6-b23b-2e1a025ba415", "e9ec256b-5ca2-4663-9da6-d8a2faff21a8")
      .startMock();
    
    waitApiListCount("/organizations", 1);
    waitApiListCount("/services", 2);
  }

  @After
  public void afterClass() {
    getPtvMocker().endMock();
    deleteSettings();
    deleteAllServiceChannels();
    deleteAllServices();
  }
   
  private void createSettings() {
    insertSystemSetting(PtvConsts.SYSTEM_SETTING_BASEURL, String.format("%s%s", getWireMockBasePath(), BASE_URL));
  }
  
  private void deleteSettings() {
    deleteSystemSetting(PtvConsts.SYSTEM_SETTING_BASEURL);
  }
  
  private String getElectronicChannelId(String serviceId, int index) throws InterruptedException {
    waitApiListCount(String.format("/services/%s/electronicChannels", serviceId), 3);
    
    return given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/services/{serviceId}/electronicChannels", serviceId)
      .body()
      .jsonPath()
      .getString(String.format("id[%d]", index));
  }
  
  private String getPhoneChannelId(String serviceId, int index) throws InterruptedException {
    waitApiListCount(String.format("/services/%s/phoneChannels", serviceId), 3);
    
    return given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/services/{serviceId}/phoneChannels", serviceId)
      .body()
      .jsonPath()
      .getString(String.format("id[%d]", index));
  }
  
  private String getPrintableFormChannelId(String serviceId, int index) throws InterruptedException {
    waitApiListCount(String.format("/services/%s/printableFormChannels", serviceId), 3);
    
    return given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/services/{serviceId}/printableFormChannels", serviceId)
      .body()
      .jsonPath()
      .getString(String.format("id[%d]", index));
  }
  
  private String getServiceLocationChannelId(String serviceId, int index) throws InterruptedException {
    waitApiListCount(String.format("/services/%s/serviceLocationChannels", serviceId), 3);
    
    return given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/services/{serviceId}/serviceLocationChannels", serviceId)
      .body()
      .jsonPath()
      .getString(String.format("id[%d]", index));
  }
  
  private String getWebPageChannelId(String serviceId, int index) throws InterruptedException {
    waitApiListCount(String.format("/services/%s/webPageChannels", serviceId), 3);
    
    return given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/services/{serviceId}/webPageChannels", serviceId)
      .body()
      .jsonPath()
      .getString(String.format("id[%d]", index));
  }

  @Test
  public void testFindElectronicChannel() throws InterruptedException {
    String serviceId = getServiceId(0);
      
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
    String serviceId = getServiceId(0);
    
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
    String serviceId = getServiceId(0);
      
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
    String serviceId = getServiceId(0);
      
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
  
  @Test
  public void testFindPrintableFormChannel() throws InterruptedException {
    String serviceId = getServiceId(0);
      
    waitApiListCount(String.format("/services/%s/printableFormChannels", serviceId), 3);
    
    String channelId = given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/services/{serviceId}/printableFormChannels", serviceId)
      .body().jsonPath().getString("id[0]");

    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/services/{serviceId}/printableFormChannels/{channelId}", serviceId, channelId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id", notNullValue())
      .body("type", is("PrintableForm"))
      .body("organizationId", notNullValue())
      .body("names.size()", is(1))
      .body("names[0].language", is("fi"))
      .body("names[0].value", is("Hakemus muun kuin lähikoulun 7. luokalle"))
      .body("names[0].type", is("Name"))
      .body("descriptions.size()", is(2))
      .body("descriptions[0].language", is("fi"))
      .body("descriptions[0].value", is("Hakemus siirtymisestä 7. luokalle muuhun kuin osoitteenmukaiseen lähikouluun."))
      .body("descriptions[0].type", is("ShortDescription"))
      .body("formIdentifier", nullValue())
      .body("formReceiver", nullValue())
      .body("supportContacts.size()", is(1))
      .body("supportContacts[0].email", nullValue())
      .body("supportContacts[0].printableForm", nullValue())
      .body("supportContacts[0].printableFormChargeDescription", nullValue())
      .body("supportContacts[0].language", is("fi"))
      .body("supportContacts[0].serviceChargeTypes.size()", is(0))
      .body("deliveryAddress", nullValue())
      .body("channelUrls.size()", is(1))
      .body("channelUrls[0].language", is("fi"))
      .body("channelUrls[0].value", is("http://www.mikkeli.fi/sites/mikkeli.fi/files/atoms/files/hakemus_muun_kuin_lahikoulun_7.luokalle_2014.pdf"))
      .body("channelUrls[0].type", is("PDF"))
      .body("languages.size()", is(1))
      .body("languages[0]", is("fi"))
      .body("deliveryAddressDescriptions.size()", is(0))
      .body("attachments.size()", is(0))
      .body("webPages.size()", is(0))
      .body("serviceHours.size()", is(0))
      .body("publishingStatus", is("Published"));
  }

  @Test
  public void testListPrintableFormChannels() throws InterruptedException {
    String serviceId = getServiceId(0);
      
    waitApiListCount(String.format("/services/%s/printableFormChannels", serviceId), 3);

    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/services/{serviceId}/printableFormChannels", serviceId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id[1]", notNullValue())
      .body("type[1]", is("PrintableForm"))
      .body("organizationId[1]", notNullValue())
      .body("names[1].size()", is(1))
      .body("names[1][0].language", is("fi"))
      .body("names[1][0].value", is("Hakemus vieraan oppilasalueen kouluun"))
      .body("names[1][0].type", is("Name"))
      .body("descriptions[1].size()", is(2))
      .body("descriptions[1][0].language", is("fi"))
      .body("descriptions[1][0].value", is("Hakemus koulunkäyntiin muussa kuin omassa lähikoulussa."))
      .body("descriptions[1][0].type", is("ShortDescription"))
      .body("formIdentifier[1]", nullValue())
      .body("formReceiver[1]", nullValue())
      .body("supportContacts[1].size()", is(1))
      .body("supportContacts[1][0].email", nullValue())
      .body("supportContacts[1][0].printableForm", nullValue())
      .body("supportContacts[1][0].printableFormChargeDescription", nullValue())
      .body("supportContacts[1][0].language", is("fi"))
      .body("supportContacts[1][0].serviceChargeTypes.size()", is(0))
      .body("deliveryAddress[1]", nullValue())
      .body("channelUrls[1].size()", is(1))
      .body("channelUrls[1][0].language", is("fi"))
      .body("channelUrls[1][0].value", is("http://www.mikkeli.fi/sites/mikkeli.fi/files/atoms/files/hakemus_vieraan_oppilasalueen_kouluun_2014.pdf"))
      .body("channelUrls[1][0].type", is("PDF"))
      .body("languages[1].size()", is(1))
      .body("languages[1][0]", is("fi"))
      .body("deliveryAddressDescriptions[1].size()", is(0))
      .body("attachments[1].size()", is(0))
      .body("webPages[1].size()", is(0))
      .body("serviceHours[1].size()", is(0))
      .body("publishingStatus[1]", is("Published"));
  }
  
  @Test
  public void testFindServiceLocationChannel() throws InterruptedException {
    String serviceId = getServiceId(0);
      
    waitApiListCount(String.format("/services/%s/serviceLocationChannels", serviceId), 3);
    
    String channelId = given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/services/{serviceId}/serviceLocationChannels", serviceId)
      .body().jsonPath().getString("id[0]");
    
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/services/{serviceId}/serviceLocationChannels/{channelId}", serviceId, channelId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id", notNullValue())
      .body("type", is("ServiceLocation"))
      .body("organizationId", notNullValue())
      .body("names.size()", is(1))
      .body("names[0].language", is("fi"))
      .body("names[0].value", is("Ristiinan päiväkoti"))
      .body("names[0].type", is("Name"))
      .body("descriptions.size()", is(2))
      .body("descriptions[1].language", is("fi"))
      .body("descriptions[1].value", is("Ristiinan päiväkoti tarjoaa 1-6 -vuotiaille lapsille monipuolista varhaiskasvatusta ja esiopetusta."))
      .body("descriptions[1].type", is("ShortDescription"))
      .body("serviceAreaRestricted", is(true))
      .body("supportContacts.size()", is(0))
      .body("email", nullValue())
      .body("phone", is("040 755 4710"))
      .body("languages.size()", is(1))
      .body("languages[0]", is("fi"))
      .body("fax", nullValue())
      .body("latitude", is("6818871"))
      .body("longitude", is("514184"))
      .body("coordinateSystem", is("3067"))
      .body("coordinatesSetManually", is(true))
      .body("phoneServiceCharge", is(true))
      .body("webPages.size()", is(1))
      .body("webPages[0].description", nullValue())
      .body("webPages[0].url", is("http://www.mikkeli.fi/palvelut/ristiinan-paivakoti"))
      .body("webPages[0].language", is("fi"))
      .body("webPages[0].value", is("Ristiinan päiväkodin kotisivu"))
      .body("webPages[0].type", is("HomePage"))
      .body("serviceAreas.size()", is(1))
      .body("serviceAreas[0]", is("Mikkeli"))
      .body("phoneChargeDescriptions.size()", is(1))
      .body("phoneChargeDescriptions[0].language", is("fi"))
      .body("phoneChargeDescriptions[0].value", is("paikallisverkkomaksu / paikallispuhelumaksu"))
      .body("addresses.size()", is(1))
      .body("addresses[0].type", is("Visiting"))
      .body("addresses[0].postOfficeBox", nullValue())
      .body("addresses[0].postalCode", is("53200"))
      .body("addresses[0].postOffice", is("LAPPEENRANTA"))
      .body("addresses[0].streetAddress.size()", is(1))
      .body("addresses[0].streetAddress[0].value", is("Brahentie 54"))
      .body("addresses[0].streetAddress[0].language", is("fi"))
      .body("addresses[0].municipality", is("Mikkeli"))
      .body("addresses[0].country", is("FI"))
      .body("addresses[0].qualifier", nullValue())
      .body("addresses[0].additionalInformations.size()", is(0))
      .body("chargeTypes.size()", is(0))
      .body("serviceHours.size()", is(1))
      .body("serviceHours[0].type", is("Standard"))
      .body("serviceHours[0].validFrom", is((String) null))
      .body("serviceHours[0].validTo", is((String) null))
      .body("serviceHours[0].opens", is("06:30"))
      .body("serviceHours[0].closes", is("17:00"))
      .body("serviceHours[0].status", is("OPEN"))
      .body("serviceHours[0].days.size()", is(7))
      .body("serviceHours[0].days[0]", is(1))
      .body("serviceHours[0].days[1]", is(2))
      .body("serviceHours[0].days[2]", is(3))
      .body("serviceHours[0].days[3]", is(4))
      .body("serviceHours[0].days[4]", is(5))
      .body("serviceHours[0].days[5]", is(6))
      .body("serviceHours[0].days[6]", is(0))
      .body("serviceHours[0].additionalInformation.size()", is(0))
      .body("publishingStatus", is("Published"));
  }

  @Test
  public void testListServiceLocationChannel() throws InterruptedException {
    String serviceId = getServiceId(0);
      
    waitApiListCount(String.format("/services/%s/serviceLocationChannels", serviceId), 3);
    
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/services/{serviceId}/serviceLocationChannels", serviceId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id[1]", notNullValue())
      .body("type[1]", is("ServiceLocation"))
      .body("organizationId[1]", notNullValue())
      .body("names[1].size()", is(1))
      .body("names[1][0].language", is("fi"))
      .body("names[1][0].value", is("Saksalan päiväkoti"))
      .body("names[1][0].type", is("Name"))
      .body("descriptions[1].size()", is(2))
      .body("descriptions[1][1].language", is("fi"))
      .body("descriptions[1][1].value", is("Saksalan päiväkoti järjestää vuoropäivähoitoa ja esiopetusta kolmessa lapsiryhmässä."))
      .body("descriptions[1][1].type", is("ShortDescription"))
      .body("serviceAreaRestricted[1]", is(true))
      .body("supportContacts[1].size()", is(0))
      .body("email[1]", nullValue())
      .body("phone[1]", is("015 194 3392"))
      .body("languages[1].size()", is(1))
      .body("languages[1][0]", is("fi"))
      .body("fax[1]", nullValue())
      .body("latitude[1]", is("6839924"))
      .body("longitude[1]", is("515077"))
      .body("coordinateSystem[1]", is("3067"))
      .body("coordinatesSetManually[1]", is(true))
      .body("phoneServiceCharge[1]", is(true))
      .body("webPages[1].size()", is(2))
      .body("webPages[1][0].description", nullValue())
      .body("webPages[1][0].url", is("http://saksalapk.blogspot.fi/"))
      .body("webPages[1][0].language", is("fi"))
      .body("webPages[1][0].value", is("Saksalan päiväkodin blogi"))
      .body("webPages[1][0].type", is("SocialPage"))
      .body("serviceAreas[1].size()", is(1))
      .body("serviceAreas[1][0]", is("Mikkeli"))
      .body("phoneChargeDescriptions[1].size()", is(1))
      .body("phoneChargeDescriptions[1][0].language", is("fi"))
      .body("phoneChargeDescriptions[1][0].value", is("paikallisverkkomaksu / paikallispuhelumaksu"))
      .body("addresses[1].size()", is(1))
      .body("addresses[1][0].type", is("Visiting"))
      .body("addresses[1][0].postOfficeBox", nullValue())
      .body("addresses[1][0].postalCode", is("50170"))
      .body("addresses[1][0].postOffice", is("MIKKELI"))
      .body("addresses[1][0].streetAddress.size()", is(1))
      .body("addresses[1][0].streetAddress[0].value", is("Parraskuja 2"))
      .body("addresses[1][0].streetAddress[0].language", is("fi"))
      .body("addresses[1][0].municipality", is("Mikkeli"))
      .body("addresses[1][0].country", is("FI"))
      .body("addresses[1][0].qualifier", nullValue())
      .body("addresses[1][0].additionalInformations.size()", is(0))
      .body("chargeTypes[1].size()", is(0))
      .body("serviceHours[1].size()", is(2))
      .body("serviceHours[1][0].type", is("Standard"))
      .body("serviceHours[1][0].validFrom", is((String) null))
      .body("serviceHours[1][0].validTo", is((String) null))
      .body("serviceHours[1][0].opens", is("05:00"))
      .body("serviceHours[1][0].closes", is("23:00"))
      .body("serviceHours[1][0].status", is("OPEN"))
      .body("serviceHours[1][0].days.size()", is(5))
      .body("serviceHours[1][0].days[0]", is(1))
      .body("serviceHours[1][0].days[1]", is(2))
      .body("serviceHours[1][0].days[2]", is(3))
      .body("serviceHours[1][0].days[3]", is(4))
      .body("serviceHours[1][0].days[4]", is(5))
      .body("serviceHours[1][0].additionalInformation.size()", is(0))
      .body("publishingStatus[1]", is("Published"));
  }

  @Test
  public void testFindWebPageChannel() throws InterruptedException {
    String serviceId = getServiceId(0);
      
    waitApiListCount(String.format("/services/%s/webPageChannels", serviceId), 3);
    
    String channelId = given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/services/{serviceId}/webPageChannels", serviceId)
      .body().jsonPath().getString("id[0]");
    
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/services/{serviceId}/webPageChannels/{channelId}", serviceId, channelId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id", notNullValue())
      .body("type", is("WebPage"))
      .body("organizationId", notNullValue())
      .body("names.size()", is(1))
      .body("names[0].language", is("fi"))
      .body("names[0].value", is("Mikkelin kaupungin verkkosivut, asumisoikeusasunnot"))
      .body("names[0].type", is("Name"))
      .body("descriptions.size()", is(2))
      .body("descriptions[1].language", is("fi"))
      .body("descriptions[1].value", is("Mikkelin kaupungin sivuilla kerrotaan alueen asumisoikeusasunnoista ja järjestysnumeron hakumenettelystä."))
      .body("descriptions[1].type", is("ShortDescription"))
      .body("urls.size()", is(1))
      .body("urls[0].language", is("fi"))
      .body("urls[0].value", is("http://www.mikkeli.fi/palvelut/asumisoikeusasunnot"))
      .body("attachments.size()", is(0))
      .body("supportContacts.size()", is(1))
      .body("supportContacts[0].email", nullValue())
      .body("supportContacts[0].phone", nullValue())
      .body("supportContacts[0].phoneChargeDescription", nullValue())
      .body("supportContacts[0].language", is("fi"))
      .body("supportContacts[0].serviceChargeTypes.size()", is(0))
      .body("languages.size()", is(1))
      .body("languages[0]", is("fi"))
      .body("webPages.size()", is(0))
      .body("serviceHours.size()", is(0))
      .body("publishingStatus", is("Published"));
  }

  @Test
  public void testListWebPageChannel() throws InterruptedException {
    String serviceId = getServiceId(0);
      
    waitApiListCount(String.format("/services/%s/webPageChannels", serviceId), 3);
    
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/services/{serviceId}/webPageChannels", serviceId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id[1]", notNullValue())
      .body("type[1]", is("WebPage"))
      .body("organizationId[1]", notNullValue())
      .body("names[1].size()", is(1))
      .body("names[1][0].language", is("fi"))
      .body("names[1][0].value", is("Mikkelin kansalaisopiston Facebook-sivut"))
      .body("names[1][0].type", is("Name"))
      .body("descriptions[1].size()", is(2))
      .body("descriptions[1][1].language", is("fi"))
      .body("descriptions[1][1].value", is("Facebook-sivuille päivitetään Mikkelin kansalaisopiston kurssiesittelyjä ja ajankohtaisia tapahtumia."))
      .body("descriptions[1][1].type", is("ShortDescription"))
      .body("urls[1].size()", is(1))
      .body("urls[1][0].language", is("fi"))
      .body("urls[1][0].value", is("https://fi-fi.facebook.com/Mikkelin-kansalaisopisto-149624898432465/"))
      .body("attachments[1].size()", is(0))
      .body("supportContacts[1].size()", is(1))
      .body("supportContacts[1][0].email", is(""))
      .body("supportContacts[1][0].phone", is(""))
      .body("supportContacts[1][0].phoneChargeDescription", is(""))
      .body("supportContacts[1][0].language", is("fi"))
      .body("supportContacts[1][0].serviceChargeTypes.size()", is(0))
      .body("languages[1].size()", is(2))
      .body("languages[1][0]", is("fi"))
      .body("languages[1][1]", is("en"))
      .body("webPages[1].size()", is(0))
      .body("serviceHours[1].size()", is(0))
      .body("publishingStatus[1]", is("Published"));
  }

  @Test
  public void testListElectronicChannelsLimits() throws InterruptedException {
    String serviceId = getServiceId(0);
    String channelsUrl = String.format("/services/%s/electronicChannels", serviceId);
    waitApiListCount(channelsUrl, 3);
    assertListLimits(channelsUrl, 3);
  }
  
  @Test
  public void testListPhoneChannelsLimits() throws InterruptedException {
    String serviceId = getServiceId(0);
    String channelsUrl = String.format("/services/%s/phoneChannels", serviceId);
    waitApiListCount(channelsUrl, 3);
    assertListLimits(channelsUrl, 3);
  }
  
  @Test
  public void testListPrintableFormChannelsLimits() throws InterruptedException {
    String serviceId = getServiceId(0);
    String channelsUrl = String.format("/services/%s/printableFormChannels", serviceId);
    waitApiListCount(channelsUrl, 3);
    assertListLimits(channelsUrl, 3);
  }
  
  @Test
  public void testListServiceLocationChannelLimits() throws InterruptedException {
    String serviceId = getServiceId(0);
    String channelsUrl = String.format("/services/%s/serviceLocationChannels", serviceId);
    waitApiListCount(channelsUrl, 3);
    assertListLimits(channelsUrl, 3);
  }
  
  @Test
  public void testListWebPageChannelsLimits() throws InterruptedException {
    String serviceId = getServiceId(0);
    String channelsUrl = String.format("/services/%s/webPageChannels", serviceId);
    waitApiListCount(channelsUrl, 3);
    assertListLimits(channelsUrl, 3);
  }
  
  @Test
  public void testElectronicChannelNotFound() throws InterruptedException {
    String serviceId = getServiceId(0);
    String incorrectServiceId = getServiceId(1);
    String electronicChannelId = getElectronicChannelId(serviceId, 0);
    String phoneChannelId = getPhoneChannelId(serviceId, 0);
    String[] malformedIds = new String[] {"evil", "*", "/", "1", "-1", "~"};
    
    assertFound(String.format("/services/%s/electronicChannels/%s", serviceId, electronicChannelId));
    
    for (String malformedId : malformedIds) {
      assertNotFound(String.format("/services/%s/electronicChannels/%s", serviceId, malformedId));
    }
    
    assertNotFound(String.format("/services/%s/electronicChannels/%s", incorrectServiceId, electronicChannelId));
    assertNotFound(String.format("/services/%s/electronicChannels/%s", serviceId, phoneChannelId));
  }
  
  @Test
  public void testPhoneChannelNotFound() throws InterruptedException {
    String serviceId = getServiceId(0);
    String incorrectServiceId = getServiceId(1);
    String electronicChannelId = getElectronicChannelId(serviceId, 0);
    String phoneChannelId = getPhoneChannelId(serviceId, 0);
    String[] malformedIds = new String[] {"evil", "*", "/", "1", "-1", "~"};
    
    assertFound(String.format("/services/%s/phoneChannels/%s", serviceId, phoneChannelId));
    
    for (String malformedId : malformedIds) {
      assertNotFound(String.format("/services/%s/phoneChannels/%s", serviceId, malformedId));
    }
    
    assertNotFound(String.format("/services/%s/phoneChannels/%s", incorrectServiceId, phoneChannelId));
    assertNotFound(String.format("/services/%s/phoneChannels/%s", serviceId, electronicChannelId));
  }
  
  @Test
  public void testPrintableFormChannelNotFound() throws InterruptedException {
    String serviceId = getServiceId(0);
    String incorrectServiceId = getServiceId(1);
    String electronicChannelId = getElectronicChannelId(serviceId, 0);
    String printableFormChannelId = getPrintableFormChannelId(serviceId, 0);
    String[] malformedIds = new String[] {"evil", "*", "/", "1", "-1", "~"};
    
    assertFound(String.format("/services/%s/printableFormChannels/%s", serviceId, printableFormChannelId));
    
    for (String malformedId : malformedIds) {
      assertNotFound(String.format("/services/%s/printableFormChannels/%s", serviceId, malformedId));
    }
    
    assertNotFound(String.format("/services/%s/printableFormChannels/%s", incorrectServiceId, printableFormChannelId));
    assertNotFound(String.format("/services/%s/printableFormChannels/%s", serviceId, electronicChannelId));
  }

  @Test
  public void testServiceLocationChannelNotFound() throws InterruptedException {
    String serviceId = getServiceId(0);
    String incorrectServiceId = getServiceId(1);
    String electronicChannelId = getElectronicChannelId(serviceId, 0);
    String serviceLocationChannelId = getServiceLocationChannelId(serviceId, 0);
    String[] malformedIds = new String[] {"evil", "*", "/", "1", "-1", "~"};
    
    assertFound(String.format("/services/%s/serviceLocationChannels/%s", serviceId, serviceLocationChannelId));
    
    for (String malformedId : malformedIds) {
      assertNotFound(String.format("/services/%s/serviceLocationChannels/%s", serviceId, malformedId));
    }
    
    assertNotFound(String.format("/services/%s/serviceLocationChannels/%s", incorrectServiceId, serviceLocationChannelId));
    assertNotFound(String.format("/services/%s/serviceLocationChannels/%s", serviceId, electronicChannelId));
  }

  @Test
  public void testWebPageChannelNotFound() throws InterruptedException {
    String serviceId = getServiceId(0);
    String incorrectServiceId = getServiceId(1);
    String electronicChannelId = getElectronicChannelId(serviceId, 0);
    String webPageChannelId = getWebPageChannelId(serviceId, 0);
    String[] malformedIds = new String[] {"evil", "*", "/", "1", "-1", "~"};
    
    assertFound(String.format("/services/%s/webPageChannels/%s", serviceId, webPageChannelId));
    
    for (String malformedId : malformedIds) {
      assertNotFound(String.format("/services/%s/webPageChannels/%s", serviceId, malformedId));
    }
    
    assertNotFound(String.format("/services/%s/webPageChannels/%s", incorrectServiceId, webPageChannelId));
    assertNotFound(String.format("/services/%s/webPageChannels/%s", serviceId, electronicChannelId));
  }
  
}
