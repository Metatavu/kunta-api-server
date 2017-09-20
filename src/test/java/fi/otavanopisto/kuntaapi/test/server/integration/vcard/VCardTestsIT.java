package fi.otavanopisto.kuntaapi.test.server.integration.vcard;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertEquals;

import java.time.ZoneId;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.jayway.restassured.http.ContentType;

import fi.otavanopisto.kuntaapi.server.integrations.vcard.VCardConsts;
import fi.otavanopisto.kuntaapi.test.AbstractIntegrationTest;

@SuppressWarnings ("squid:S1192")
public class VCardTestsIT extends AbstractIntegrationTest{
  
  private static final ZoneId TIMEZONE_ID = ZoneId.of("Europe/Helsinki");
  
  /**
   * Starts WireMock
   */
  @Rule
  public WireMockRule wireMockRule = new WireMockRule(getWireMockPort());
  
  @Before
  public void beforeTest() throws InterruptedException {
    
    getPtvOrganizationMocker()
      .mock("9355a207-efd3-4cfb-a02b-67187f34c822");

    startMocks();

    waitApiListCount("/organizations", 1);
    
    createVCardSettings(getOrganizationId(0));

    waitApiListCount(String.format("/organizations/%s/contacts", getOrganizationId(0)), 3); 
  }

  @After
  public void afterClass() {
    String organizationId = getOrganizationId(0);
    deleteVCardSettings(organizationId);
  }
  
  @Test
  public void testListContacts() {
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/contacts", getOrganizationId(0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(3))
      .body("id[1]", notNullValue())
      .body("displayName[1]", is("Seppo Haku Esimerkki"))
      .body("firstName[1]", nullValue())
      .body("lastName[1]", nullValue())
      .body("title[1]", is("Test Person"))
      .body("organization[1]", is("City of Example"))
      .body("organizationUnits[1].size()", is(3))
      .body("organizationUnits[1][0]", is("Testing"))
      .body("additionalInformations[1].size()", is(1))
      .body("additionalInformations[1][0]", is("ma-pe 7.30-15"))
      .body("emails[1].size()", is(1))
      .body("emails[1][0]", is("seppo.esimerkki@example.fi"))
      .body("phones[1].size()", is(0))
      .body("addresses[1].size()", is(1))
      .body("addresses[1][0].latitude", nullValue())
      .body("addresses[1][0].longitude", nullValue())
      .body("addresses[1][0].coordinateState", nullValue())
      .body("addresses[1][0].type", nullValue())
      .body("addresses[1][0].postalCode", is("12345"))
      .body("addresses[1][0].postOfficeBox.size()", is(0))
      .body("addresses[1][0].postOffice.size()", is(1))
      .body("addresses[1][0].postOffice[0].language", is("fi"))
      .body("addresses[1][0].postOffice[0].value", is("Testia"))
      .body("addresses[1][0].postOffice[0].type", nullValue())
      .body("addresses[1][0].streetAddress.size()", is(1))
      .body("addresses[1][0].streetAddress[0].value", is("Seponkatu 22 PL 5"))
      .body("addresses[1][0].streetAddress[0].language", is("fi"))
      .body("addresses[1][0].streetAddress[0].type", nullValue())
      .body("addresses[1][0].additionalInformations.size()", is(0))
      .body("addresses[1][0].streetNumber", nullValue())
      .body("addresses[1][0].country", is("Suomi"))
      .body("addresses[1][0].municipality", nullValue())      
      .body("statuses[1].size()", is(0));
  }
  
  @Test
  public void testFindContact() {
    String organizationId = getOrganizationId(0);
    String contactId = getContactId(organizationId, 0);
    
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/contacts/{contactId}", organizationId, contactId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id", is(contactId))
      .body("displayName", is("Erkki Esimerkki"))
      .body("firstName", nullValue())
      .body("lastName", nullValue())
      .body("title", is("Test Person"))
      .body("organization", is("City of Example"))
      .body("organizationUnits.size()", is(3))
      .body("organizationUnits[1]", is("Integration Testing"))
      .body("additionalInformations.size()", is(0))
      .body("emails.size()", is(1))
      .body("emails[0]", is("erkki.esimerkki@example.fi"))
      .body("phones.size()", is(9))
      .body("phones[0].type", is("work"))
      .body("phones[0].number", is("123456"))
      .body("phones[8].type", is("fax"))
      .body("phones[8].number", is("987987666"))
      .body("addresses.size()", is(1))
      .body("addresses[0].latitude", nullValue())
      .body("addresses[0].longitude", nullValue())
      .body("addresses[0].coordinateState", nullValue())
      .body("addresses[0].type", nullValue())
      .body("addresses[0].postalCode", is("12345"))
      .body("addresses[0].postOfficeBox.size()", is(0))
      .body("addresses[0].postOffice.size()", is(1))
      .body("addresses[0].postOffice[0].language", is("fi"))
      .body("addresses[0].postOffice[0].value", is("Testia"))
      .body("addresses[0].postOffice[0].type", nullValue())
      .body("addresses[0].streetAddress.size()", is(1))
      .body("addresses[0].streetAddress[0].value", is("Erkinkuja 3"))
      .body("addresses[0].streetAddress[0].language", is("fi"))
      .body("addresses[0].streetAddress[0].type", nullValue())
      .body("addresses[0].additionalInformations.size()", is(0))
      .body("addresses[0].streetNumber", nullValue())
      .body("addresses[0].country", is("Suomi"))
      .body("addresses[0].municipality", nullValue())      
      .body("statuses.size()", is(1))
      .body("statuses[0].start", sameInstant(getOffsetDateTime(2020, 2, 15, 16, 30, TIMEZONE_ID).toInstant()))
      .body("statuses[0].end", sameInstant(getOffsetDateTime(2020, 3, 1, 8, 30, TIMEZONE_ID).toInstant()))
      .body("statuses[0].text", is("Vacation"));
  }
  
  @Test
  public void testOrganizationContactsNotFound() throws InterruptedException {
    String organizationId = getOrganizationId(0);
    String incorrectOrganizationId = getOrganizationId(1);
    String organizationContactId = getContactId(organizationId, 0);
    
    String[] malformedIds = new String[] {"evil", "*", "/", "1", "-1", "~"};
    assertFound(String.format("/organizations/%s/contacts/%s", organizationId, organizationContactId));
    assertEquals(3, countApiList(String.format("/organizations/%s/contacts", organizationId)));
    
    for (String malformedId : malformedIds) {
      assertNotFound(String.format("/organizations/%s/contacts/%s", organizationId, malformedId));
    }
    
    assertNotFound(String.format("/organizations/%s/contacts/%s", incorrectOrganizationId, organizationContactId));
    assertEquals(0, countApiList(String.format("/organizations/%s/contacts", incorrectOrganizationId)));
  }
  
  @Test
  public void testListContactsSearch() {
    if (skipElasticSearchTests()) {
      return;
    }
    
    waitForElasticIndex();
    
    String search = "(Haku)|(Pirkko^10)";
    String organizationId = getOrganizationId(0);
    
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get(String.format("/organizations/{organizationId}/contacts?search=%s&sortBy=SCORE&sortDir=DESC", search), organizationId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(2))
      .body("displayName[0]", is("Pirkko Haku Esimerkki"))
      .body("displayName[1]", is("Seppo Haku Esimerkki"));
    
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get(String.format("/organizations/{organizationId}/contacts?search=%s&sortBy=SCORE&sortDir=ASC", search), organizationId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(2))
      .body("displayName[0]", is("Seppo Haku Esimerkki"))
      .body("displayName[1]", is("Pirkko Haku Esimerkki"));
  }

  @Test
  public void testRemoveContacts() throws InterruptedException {
    String organizationId = getOrganizationId(0);
    
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/contacts", organizationId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(3))
      .body("displayName[0]", is("Erkki Esimerkki"))
      .body("displayName[1]", is("Seppo Haku Esimerkki"))
      .body("displayName[2]", is("Pirkko Haku Esimerkki"));
    
    changeVCardFile(organizationId, "vcard/test-removed.vcard");

    waitApiListCount(String.format("/organizations/%s/contacts", getOrganizationId(0)), 2);

    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/contacts", organizationId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(2))
      .body("displayName[0]", is("Erkki Esimerkki"))
      .body("displayName[1]", is("Pirkko Haku Esimerkki"));
  }

  @Test
  public void testListContactsSearchRemoved() throws InterruptedException {
    if (skipElasticSearchTests()) {
      return;
    }
    
    waitForElasticIndex();
    
    String search = "(Haku)|(Pirkko^10)";
    String organizationId = getOrganizationId(0);
    
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get(String.format("/organizations/{organizationId}/contacts?search=%s&sortBy=SCORE&sortDir=DESC", search), organizationId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(2))
      .body("displayName[0]", is("Pirkko Haku Esimerkki"))
      .body("displayName[1]", is("Seppo Haku Esimerkki"));
    
    changeVCardFile(organizationId, "vcard/test-removed.vcard");
    waitApiListCount(String.format("/organizations/%s/contacts", getOrganizationId(0)), 2);
    
    given() 
      .baseUri(getApiBasePath())
      .contentType(ContentType.JSON)
      .get(String.format("/organizations/{organizationId}/contacts?search=%s&sortBy=SCORE", search), organizationId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(1))
      .body("displayName[0]", is("Pirkko Haku Esimerkki"));

    
  }
  private void changeVCardFile(String organizationId, String file) {
    deleteVCardSettings(organizationId);
    insertVCardFile(organizationId, file);
  }
  
  private void createVCardSettings(String organizationId) {
    insertVCardFile(organizationId, "vcard/test.vcard");
  }
  
  private void insertVCardFile(String organizationId, String file) {
    insertOrganizationSetting(organizationId, VCardConsts.ORGANIZATION_VCARD_FILE, getClass().getClassLoader().getResource(file).getFile());
    flushCache();
  }
   
  private void deleteVCardSettings(String organizationId) {
    deleteOrganizationSetting(organizationId, VCardConsts.ORGANIZATION_VCARD_FILE);
  }
}
