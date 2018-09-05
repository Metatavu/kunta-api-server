package fi.metatavu.kuntaapi.test.server.integration.ptv;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.core.Is.is;

import java.io.IOException;
import java.time.ZoneId;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.jayway.restassured.http.ContentType;

@SuppressWarnings ("squid:S1192")
public class ServiceChannelsTestsIT extends AbstractPtvTest {
  
  private static final ZoneId TIMEZONE_ID = ZoneId.of("Europe/Helsinki");

  /**
   * Starts WireMock
   */
  @Rule
  public WireMockRule wireMockRule = new WireMockRule(wireMockConfig().port(getWireMockPort()), false);

  @Before
  public void beforeTest() throws InterruptedException {
    getPtvOrganizationMocker().mock(TestPtvConsts.ORGANIZATIONS);
    getPtvServiceMocker().mock(TestPtvConsts.SERVICES);    
    getPtvServiceChannelMocker().mock(TestPtvConsts.SERVICE_CHANNELS);

    startMocks();

    waitApiListCount("/organizations", TestPtvConsts.ORGANIZATIONS.length);
  }

  @Test
  public void testFindElectronicChannel() throws InterruptedException, JSONException, IOException {
    int channelIndex = 0;
    String channelId = getElectronicChannelId(channelIndex, TestPtvConsts.ELECTRONIC_CHANNEL_SERVICE_CHANNELS.length);
    
    String response = givenReadonly()
      .contentType(ContentType.JSON)
      .get("/electronicServiceChannels/{channelId}", channelId)
      .body().asString();
      
    assertJSONFileEquals(String.format("ptv/kuntaapi/servicechannels/electronic/%d.json", channelIndex) , response, getServiceChannelCustomizations());
  }
  
  @Test
  public void testListElectronicChannels() throws InterruptedException, IOException {
    int channelIndex = 0;

    waitApiListCount("/electronicServiceChannels", TestPtvConsts.ELECTRONIC_CHANNEL_SERVICE_CHANNELS.length);

    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/electronicServiceChannels")
      .then()
      .body("id.size()", is(TestPtvConsts.ELECTRONIC_CHANNEL_SERVICE_CHANNELS.length))
      .body("[0]", jsonEqualsFile(String.format("ptv/kuntaapi/servicechannels/electronic/%d.json", channelIndex), getServiceChannelCustomizations()));
  }
  
  @Test
  public void testFindPhoneChannel() throws InterruptedException, JSONException, IOException {
    int channelIndex = 0;
    String channelId = getPhoneChannelId(channelIndex, TestPtvConsts.PHONE_SERVICE_CHANNELS.length);
    
    String response = givenReadonly()
      .contentType(ContentType.JSON)
      .get("/phoneServiceChannels/{channelId}", channelId)
      .body().asString();
      
    assertJSONFileEquals(String.format("ptv/kuntaapi/servicechannels/phone/%d.json", channelIndex) , response, getServiceChannelCustomizations());
  }
  
  @Test
  public void testListPhoneChannels() throws InterruptedException, IOException {
    int channelIndex = 0;

    waitApiListCount("/phoneServiceChannels", TestPtvConsts.PHONE_SERVICE_CHANNELS.length);

    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/phoneServiceChannels")
      .then()
      .body("id.size()", is(TestPtvConsts.PHONE_SERVICE_CHANNELS.length))
      .body("[0]", jsonEqualsFile(String.format("ptv/kuntaapi/servicechannels/phone/%d.json", channelIndex), getServiceChannelCustomizations()));
  }
  
  @Test
  public void testFindPrintableFormChannel() throws InterruptedException, JSONException, IOException {
    int channelIndex = 0;
    String channelId = getPrintableFormChannelId(channelIndex, TestPtvConsts.PRINTABLE_FORM_SERVICE_CHANNELS.length);
    
    String response = givenReadonly()
      .contentType(ContentType.JSON)
      .get("/printableFormServiceChannels/{channelId}", channelId)
      .body().asString();
      
    assertJSONFileEquals(String.format("ptv/kuntaapi/servicechannels/printableform/%d.json", channelIndex) , response, getServiceChannelCustomizations());
  }

  @Test
  public void testListPrintableFormChannels() throws InterruptedException, IOException {
    int channelIndex = 0;

    waitApiListCount("/printableFormServiceChannels", TestPtvConsts.PRINTABLE_FORM_SERVICE_CHANNELS.length);

    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/printableFormServiceChannels")
      .then()
      .body("id.size()", is(TestPtvConsts.PRINTABLE_FORM_SERVICE_CHANNELS.length))
      .body("[0]", jsonEqualsFile(String.format("ptv/kuntaapi/servicechannels/printableform/%d.json", channelIndex), getServiceChannelCustomizations()));
  }
  
  @Test
  public void testFindServiceLocationChannel() throws InterruptedException, JSONException, IOException {
    int channelIndex = 0;
    String channelId = getServiceLocationChannelId(channelIndex, TestPtvConsts.SERVICE_LOCATION_SERVICE_CHANNELS.length);
    
    String response = givenReadonly()
      .contentType(ContentType.JSON)
      .get("/serviceLocationServiceChannels/{channelId}", channelId)
      .body().asString();
    
    assertJSONFileEquals(String.format("ptv/kuntaapi/servicechannels/servicelocation/%d.json", channelIndex) , response, getServiceChannelCustomizations());
  }

  @Test
  public void testListServiceLocationChannel() throws InterruptedException, IOException {
    int channelIndex = 0;

    waitApiListCount("/serviceLocationServiceChannels", TestPtvConsts.SERVICE_LOCATION_SERVICE_CHANNELS.length);

    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/serviceLocationServiceChannels")
      .then()
      .body("id.size()", is(TestPtvConsts.SERVICE_LOCATION_SERVICE_CHANNELS.length))
      .body("[0]", jsonEqualsFile(String.format("ptv/kuntaapi/servicechannels/servicelocation/%d.json", channelIndex), getServiceChannelCustomizations()));
  }
  
//  @Test
//  public void testListServiceLocationChannelSearch() {
//    if (skipElasticSearchTests()) {
//      return;
//    }
//    
//    String search = "(Test*)|(Metatavu*)";
//    
//    givenReadonly()
//      .contentType(ContentType.JSON)
//      .get(String.format("/serviceLocationServiceChannels?search=%s", search))
//      .then()
//      .assertThat()
//      .statusCode(200)
//      .body("id.size()", is(2))
//      .body("names[0][0].value", is("Testi"))
//      .body("names[1][0].value", is("Metatavun toimisto"));
//    
//    givenReadonly()
//      .contentType(ContentType.JSON)
//      .get(String.format("/serviceLocationServiceChannels?search=%s&sortBy=SCORE&sortDir=DESC", search))
//      .then()
//      .assertThat()
//      .statusCode(200)
//      .body("id.size()", is(2))
//      .body("names[0][0].value", is("Metatavun toimisto"))
//      .body("names[1][0].value", is("Testi"));
//    
//    givenReadonly()
//      .contentType(ContentType.JSON)
//      .get(String.format("/serviceLocationServiceChannels?search=%s&sortBy=SCORE&sortDir=ASC", search))
//      .then()
//      .assertThat()
//      .statusCode(200)
//      .body("id.size()", is(2))
//      .body("names[0][0].value", is("Testi"))
//      .body("names[1][0].value", is("Metatavun toimisto"));
//  }
//
//  @Test
//  public void testFindWebPageChannel() throws InterruptedException {
//    String channelId = givenReadonly()
//      .contentType(ContentType.JSON)
//      .get("/webPageServiceChannels")
//      .body().jsonPath().getString("id[0]");
//    
//    givenReadonly()
//      .contentType(ContentType.JSON)
//      .get("/webPageServiceChannels/{channelId}", channelId)
//      .then()
//      .assertThat()
//      .statusCode(200)
//      .body("id", notNullValue())
//      .body("organizationId", notNullValue())
//      .body("names.size()", is(1))
//      .body("names[0].language", is("fi"))
//      .body("names[0].value", is("Testi verkkosivu"))
//      .body("names[0].type", is("Name"))
//      .body("descriptions.size()", is(2))
//      .body("descriptions[1].language", is("fi"))
//      .body("descriptions[1].value", is("Verkkosivu testausta varten"))
//      .body("descriptions[1].type", is("Description"))
//      .body("urls.size()", is(1))
//      .body("urls[0].language", is("fi"))
//      .body("urls[0].value", is("https://www.google.com"))
//      .body("supportPhones.size()", is(0))
//      .body("supportEmails.size()", is(0))
//      .body("languages.size()", is(1))
//      .body("languages[0]", is("fi"))
//      .body("webPages.size()", is(0))
//      .body("serviceHours.size()", is(0))
//      .body("publishingStatus", is("Published"));
//  }
//
//  @Test
//  public void testListWebPageChannel() throws InterruptedException {
//    waitApiListCount("/webPageServiceChannels", TestPtvConsts.WEB_PAGE_SERVICE_CHANNELS.length);
//    
//    givenReadonly()
//      .contentType(ContentType.JSON)
//      .get("/webPageServiceChannels")
//      .then()
//      .assertThat()
//      .statusCode(200)
//      .body("id[0]", notNullValue())
//      .body("organizationId[0]", notNullValue())
//      .body("names[0].size()", is(1))
//      .body("names[0][0].language", is("fi"))
//      .body("names[0][0].value", is("Testi verkkosivu"))
//      .body("names[0][0].type", is("Name"))
//      .body("descriptions[0].size()", is(2))
//      .body("descriptions[0][1].language", is("fi"))
//      .body("descriptions[0][1].value", is("Verkkosivu testausta varten"))
//      .body("descriptions[0][1].type", is("Description"))
//      .body("urls[0].size()", is(1))
//      .body("urls[0][0].language", is("fi"))
//      .body("urls[0][0].value", is("https://www.google.com"))
//      .body("supportPhones[0].size()", is(0))
//      .body("supportEmails[0].size()", is(0))
//      .body("languages[0].size()", is(1))
//      .body("languages[0][0]", is("fi"))
//      .body("webPages[0].size()", is(0))
//      .body("serviceHours[0].size()", is(0))
//      .body("publishingStatus[0]", is("Published"));
//  }
//
//  @Test
//  public void testListElectronicChannelsLimits() throws InterruptedException {
//    String channelsUrl = "/electronicServiceChannels";
//    waitApiListCount(channelsUrl, TestPtvConsts.ELECTRONIC_CHANNEL_SERVICE_CHANNELS.length);
//    assertListLimits(channelsUrl, TestPtvConsts.ELECTRONIC_CHANNEL_SERVICE_CHANNELS.length);
//  }
//  
//  @Test
//  public void testListPhoneChannelsLimits() throws InterruptedException {
//    String channelsUrl = "/phoneServiceChannels";
//    waitApiListCount(channelsUrl, TestPtvConsts.PHONE_SERVICE_CHANNELS.length);
//    assertListLimits(channelsUrl, TestPtvConsts.PHONE_SERVICE_CHANNELS.length);
//  }
//  
//  @Test
//  public void testListPrintableFormChannelsLimits() throws InterruptedException {
//    String channelsUrl = "/printableFormServiceChannels";
//    waitApiListCount(channelsUrl, TestPtvConsts.PRINTABLE_FORM_SERVICE_CHANNELS.length);
//    assertListLimits(channelsUrl, TestPtvConsts.PRINTABLE_FORM_SERVICE_CHANNELS.length);
//  }
//  
//  @Test
//  public void testListServiceLocationChannelLimits() throws InterruptedException {
//    String channelsUrl = "/serviceLocationServiceChannels";
//    waitApiListCount(channelsUrl, TestPtvConsts.SERVICE_LOCATION_SERVICE_CHANNELS.length);
//    assertListLimits(channelsUrl, TestPtvConsts.SERVICE_LOCATION_SERVICE_CHANNELS.length);
//  }
//  
//  @Test
//  public void testListWebPageChannelsLimits() throws InterruptedException {
//    String channelsUrl = "/webPageServiceChannels";
//    waitApiListCount(channelsUrl, TestPtvConsts.WEB_PAGE_SERVICE_CHANNELS.length);
//    assertListLimits(channelsUrl, TestPtvConsts.WEB_PAGE_SERVICE_CHANNELS.length);
//  }
//  
//  @Test
//  public void testElectronicChannelNotFound() throws InterruptedException {
//    String electronicChannelId = getElectronicChannelId(0, TestPtvConsts.ELECTRONIC_CHANNEL_SERVICE_CHANNELS.length);
//    String phoneChannelId = getPhoneChannelId(0, TestPtvConsts.PHONE_SERVICE_CHANNELS.length);
//    String[] malformedIds = new String[] {"evil", "*", "/", "1", "-1", "~"};
//    
//    assertFound(String.format("/electronicServiceChannels/%s", electronicChannelId));
//    
//    for (String malformedId : malformedIds) {
//      assertNotFound(String.format("/electronicServiceChannels/%s", malformedId));
//    }
//    
//    assertNotFound(String.format("/electronicServiceChannels/%s", phoneChannelId));
//  }
//  
//  @Test
//  public void testPhoneChannelNotFound() throws InterruptedException {
//    String electronicChannelId = getElectronicChannelId(0, TestPtvConsts.ELECTRONIC_CHANNEL_SERVICE_CHANNELS.length);
//    String phoneChannelId = getPhoneChannelId(0, TestPtvConsts.PHONE_SERVICE_CHANNELS.length);
//    String[] malformedIds = new String[] {"evil", "*", "/", "1", "-1", "~"};
//    
//    assertFound(String.format("/phoneServiceChannels/%s", phoneChannelId));
//    
//    for (String malformedId : malformedIds) {
//      assertNotFound(String.format("/phoneServiceChannels/%s", malformedId));
//    }
//    
//    assertNotFound(String.format("/phoneServiceChannels/%s", electronicChannelId));
//  }
//  
//  @Test
//  public void testPrintableFormChannelNotFound() throws InterruptedException {
//    String electronicChannelId = getElectronicChannelId(0, TestPtvConsts.ELECTRONIC_CHANNEL_SERVICE_CHANNELS.length);
//    String printableFormChannelId = getPrintableFormChannelId(0, TestPtvConsts.PRINTABLE_FORM_SERVICE_CHANNELS.length);
//    String[] malformedIds = new String[] {"evil", "*", "/", "1", "-1", "~"};
//    
//    assertFound(String.format("/printableFormServiceChannels/%s", printableFormChannelId));
//    
//    for (String malformedId : malformedIds) {
//      assertNotFound(String.format("/printableFormServiceChannels/%s", malformedId));
//    }
//    
//    assertNotFound(String.format("/printableFormServiceChannels/%s", electronicChannelId));
//  }
//
//  @Test
//  public void testServiceLocationChannelNotFound() throws InterruptedException {
//    String electronicChannelId = getElectronicChannelId(0, TestPtvConsts.ELECTRONIC_CHANNEL_SERVICE_CHANNELS.length);
//    String serviceLocationChannelId = getServiceLocationChannelId(0, TestPtvConsts.SERVICE_LOCATION_SERVICE_CHANNELS.length);
//    String[] malformedIds = new String[] {"evil", "*", "/", "1", "-1", "~"};
//    
//    assertFound(String.format("/serviceLocationServiceChannels/%s", serviceLocationChannelId));
//    
//    for (String malformedId : malformedIds) {
//      assertNotFound(String.format("/serviceLocationServiceChannels/%s", malformedId));
//    }
//    
//    assertNotFound(String.format("/serviceLocationServiceChannels/%s", electronicChannelId));
//  }
//
//  @Test
//  public void testWebPageChannelNotFound() throws InterruptedException {
//    String electronicChannelId = getElectronicChannelId(0, TestPtvConsts.ELECTRONIC_CHANNEL_SERVICE_CHANNELS.length);
//    String webPageChannelId = getWebPageChannelId(0, TestPtvConsts.WEB_PAGE_SERVICE_CHANNELS.length);
//    String[] malformedIds = new String[] {"evil", "*", "/", "1", "-1", "~"};
//    
//    assertFound(String.format("/webPageServiceChannels/%s", webPageChannelId));
//    
//    for (String malformedId : malformedIds) {
//      assertNotFound(String.format("/webPageServiceChannels/%s", malformedId));
//    }
//    
//    assertNotFound(String.format("/webPageServiceChannels/%s", electronicChannelId));
//  }
//  
  // TODO : Service REMOVE tests
}
