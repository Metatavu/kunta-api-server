package fi.metatavu.kuntaapi.test.server.integration.ptv;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.core.Is.is;

import java.io.IOException;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.jayway.restassured.http.ContentType;

@SuppressWarnings ("squid:S1192")
public class ServiceChannelsTestsIT extends AbstractPtvTest {
  
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
  public void testFindElectronicChannelCompability() throws InterruptedException, JSONException, IOException {
    int channelIndex = 0;
    String channelId = getElectronicChannelId(channelIndex, TestPtvConsts.ELECTRONIC_CHANNEL_SERVICE_CHANNELS.length);
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/electronicServiceChannels/{channelId}", channelId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("descriptions[0].type", is("Summary"));
    
    givenReadonlyCompabilityMode()
      .contentType(ContentType.JSON)
      .get("/electronicServiceChannels/{channelId}", channelId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("descriptions[0].type", is("ShortDescription"));
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
  public void testListElectronicChannelsCompability() throws InterruptedException, IOException {
    int channelIndex = 0;

    waitApiListCount("/electronicServiceChannels", TestPtvConsts.ELECTRONIC_CHANNEL_SERVICE_CHANNELS.length);

    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/electronicServiceChannels")
      .then()
      .assertThat()
      .statusCode(200)
      .body("[0].descriptions[0].type", is("Summary"));

    givenReadonlyCompabilityMode()
      .contentType(ContentType.JSON)
      .get("/electronicServiceChannels")
      .then()
      .assertThat()
      .statusCode(200)
      .body("[0].descriptions[0].type", is("ShortDescription"));
  }
  
  @Test
  public void testFindPhoneChannelCompability() throws InterruptedException, JSONException, IOException {
    int channelIndex = 0;
    String channelId = getPhoneChannelId(channelIndex, TestPtvConsts.PHONE_SERVICE_CHANNELS.length);
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/phoneServiceChannels/{channelId}", channelId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("descriptions[0].type", is("Summary"));

    givenReadonlyCompabilityMode()
      .contentType(ContentType.JSON)
      .get("/phoneServiceChannels/{channelId}", channelId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("descriptions[0].type", is("ShortDescription"));
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
  public void testListPhoneChannelsCompability() throws InterruptedException, IOException {
    waitApiListCount("/phoneServiceChannels", TestPtvConsts.PHONE_SERVICE_CHANNELS.length);

    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/phoneServiceChannels")
      .then()
      .assertThat()
      .statusCode(200)
      .body("[0].descriptions[0].type", is("Summary"));


    givenReadonlyCompabilityMode()
      .contentType(ContentType.JSON)
      .get("/phoneServiceChannels")
      .then()
      .assertThat()
      .statusCode(200)
      .body("[0].descriptions[0].type", is("ShortDescription"));
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
  public void testFindPrintableFormChannelCompability() throws InterruptedException, JSONException, IOException {
    int channelIndex = 0;
    String channelId = getPrintableFormChannelId(channelIndex, TestPtvConsts.PRINTABLE_FORM_SERVICE_CHANNELS.length);
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/printableFormServiceChannels/{channelId}", channelId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("descriptions[1].type", is("Summary"));

    givenReadonlyCompabilityMode()
      .contentType(ContentType.JSON)
      .get("/printableFormServiceChannels/{channelId}", channelId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("descriptions[1].type", is("ShortDescription"));
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
  public void testListPrintableFormChannelsCompability() throws InterruptedException, IOException {
    waitApiListCount("/printableFormServiceChannels", TestPtvConsts.PRINTABLE_FORM_SERVICE_CHANNELS.length);

    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/printableFormServiceChannels")
      .then()
      .assertThat()
      .statusCode(200)
      .body("[0].descriptions[1].type", is("Summary"));

    givenReadonlyCompabilityMode()
      .contentType(ContentType.JSON)
      .get("/printableFormServiceChannels")
      .then()
      .assertThat()
      .statusCode(200)
      .body("[0].descriptions[1].type", is("ShortDescription"));
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
  public void testFindServiceLocationChannelCompability() throws InterruptedException, JSONException, IOException {
    int channelIndex = 0;
    String channelId = getServiceLocationChannelId(channelIndex, TestPtvConsts.SERVICE_LOCATION_SERVICE_CHANNELS.length);
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/serviceLocationServiceChannels/{channelId}", channelId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("descriptions[0].type", is("Summary"));

    givenReadonlyCompabilityMode()
      .contentType(ContentType.JSON)
      .get("/serviceLocationServiceChannels/{channelId}", channelId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("descriptions[0].type", is("ShortDescription"));
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

  @Test
  public void testListServiceLocationChannelCompability() throws InterruptedException, IOException {
    waitApiListCount("/serviceLocationServiceChannels", TestPtvConsts.SERVICE_LOCATION_SERVICE_CHANNELS.length);

    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/serviceLocationServiceChannels")
      .then()
      .assertThat()
      .statusCode(200)
      .body("[0].descriptions[0].type", is("Summary"));

    givenReadonlyCompabilityMode()
      .contentType(ContentType.JSON)
      .get("/serviceLocationServiceChannels")
      .then()
      .assertThat()
      .statusCode(200)
      .body("[0].descriptions[0].type", is("ShortDescription"));
  }
  
  @Test
  public void testListServiceLocationChannelSearch() throws InterruptedException {
    if (skipElasticSearchTests()) {
      return;
    }
    
    waitApiListCount("/serviceLocationServiceChannels", TestPtvConsts.SERVICE_LOCATION_SERVICE_CHANNELS.length);
    waitForElasticIndex();
    
    String search = "(Merimaskun*)|(koulun*)";
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("/serviceLocationServiceChannels?search=%s", search))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(5));
      
    givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("/serviceLocationServiceChannels?search=%s&sortBy=SCORE&sortDir=DESC", search))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(5))
      .body("names[0][0].value", is("Merimaskun koulu"));
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("/serviceLocationServiceChannels?search=%s&sortBy=SCORE&sortDir=ASC", search))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(5))
      .body("names[4][0].value", is("Merimaskun koulu"));
  }

  @Test
  public void testFindWebPageChannel() throws InterruptedException, JSONException, IOException {
    int channelIndex = 0;
    String channelId = getWebPageChannelId(channelIndex, TestPtvConsts.WEB_PAGE_SERVICE_CHANNELS.length);
    
    String response = givenReadonly()
      .contentType(ContentType.JSON)
      .get("/webPageServiceChannels/{channelId}", channelId)
      .body().asString();
    
    assertJSONFileEquals(String.format("ptv/kuntaapi/servicechannels/webpage/%d.json", channelIndex) , response, getServiceChannelCustomizations());
  }

  @Test
  public void testFindWebPageChannelCompability() throws InterruptedException, JSONException, IOException {
    int channelIndex = 0;
    String channelId = getWebPageChannelId(channelIndex, TestPtvConsts.WEB_PAGE_SERVICE_CHANNELS.length);
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/webPageServiceChannels/{channelId}", channelId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("descriptions[2].type", is("Summary"));
    
    givenReadonlyCompabilityMode()
      .contentType(ContentType.JSON)
      .get("/webPageServiceChannels/{channelId}", channelId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("descriptions[2].type", is("ShortDescription"));
  }

  @Test
  public void testListWebPageChannel() throws InterruptedException, IOException {
    int channelIndex = 0;
  
    waitApiListCount("/webPageServiceChannels", TestPtvConsts.WEB_PAGE_SERVICE_CHANNELS.length);
  
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/webPageServiceChannels")
      .then()
      .body("id.size()", is(TestPtvConsts.WEB_PAGE_SERVICE_CHANNELS.length))
      .body("[0]", jsonEqualsFile(String.format("ptv/kuntaapi/servicechannels/webpage/%d.json", channelIndex), getServiceChannelCustomizations()));
  }

  @Test
  public void testListWebPageChannelCompability() throws InterruptedException, IOException {
    waitApiListCount("/webPageServiceChannels", TestPtvConsts.WEB_PAGE_SERVICE_CHANNELS.length);
  
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/webPageServiceChannels")
      .then()
      .assertThat()
      .statusCode(200)
      .body("[0].descriptions[2].type", is("Summary"));

    givenReadonlyCompabilityMode()
      .contentType(ContentType.JSON)
      .get("/webPageServiceChannels")
      .then()
      .assertThat()
      .statusCode(200)
      .body("[0].descriptions[2].type", is("ShortDescription"));
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
