package fi.otavanopisto.kuntaapi.test.server.integration.restfulptv;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertNotNull;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.jayway.restassured.http.ContentType;

import fi.otavanopisto.kuntaapi.server.integrations.ptv.PtvConsts;
import fi.otavanopisto.kuntaapi.server.rest.model.LocalizedValue;
import fi.otavanopisto.kuntaapi.server.rest.model.ServiceChannelAttachment;
import fi.otavanopisto.kuntaapi.server.rest.model.ServiceHour;
import fi.otavanopisto.kuntaapi.server.rest.model.SupportContact;
import fi.otavanopisto.kuntaapi.test.AbstractIntegrationTest;
import fi.otavanopisto.restfulptv.client.model.FintoItem;
import fi.otavanopisto.restfulptv.client.model.LanguageItem;
import fi.otavanopisto.restfulptv.client.model.LocalizedListItem;
import fi.otavanopisto.restfulptv.client.model.WebPage;

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
  public void findListElectronicChannel() throws InterruptedException {
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
  
}