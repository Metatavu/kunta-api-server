package fi.metatavu.kuntaapi.test.server.integration.linkedevents;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.time.ZoneId;

import org.apache.commons.codec.digest.DigestUtils;
import org.awaitility.Awaitility;
import org.awaitility.Duration;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.jayway.restassured.http.ContentType;

import fi.metatavu.kuntaapi.server.integrations.linkedevents.LinkedEventsConsts;
import fi.metatavu.kuntaapi.test.AbstractIntegrationTest;
import fi.metatavu.kuntaapi.test.server.integration.ptv.TestPtvConsts;

@SuppressWarnings ("squid:S1192")
public class LinkedEventsTestsIT extends AbstractIntegrationTest {

  private static final ZoneId TIMEZONE_ID = ZoneId.of("UTC");

  /**
   * Starts WireMock
   */
  @Rule
  public WireMockRule wireMockRule = new WireMockRule(getWireMockPort());
  
  @Before
  public void beforeTest() throws InterruptedException {
    getPtvOrganizationMocker()
      .mock(TestPtvConsts.ORGANIZATIONS[2]);
    
    getLinkedEventsEventMocker()
      .mockImages("2", "3")
      .mockEvents("mantyharju:pm-maastojuoksukilpailut-la-27","mantyharju:tanssit-la-872017-klo-2000","mantyharju:venetsialaiset-la-2682017-klo-");
    
    startMocks();

    waitApiListCount("/organizations", 1);
    
    createLinkedEventsSettings(getOrganizationId(0));

    waitApiListCount(String.format("/organizations/%s/events", getOrganizationId(0)), 3); 
  }

  @After
  public void afterClass() {
    String organizationId = getOrganizationId(0);
    deleteLinkedEventsSettings(organizationId);
  }
  
  @Test
  public void testFindEvents() {
    String organizationId = getOrganizationId(0);
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/events/{eventId}", organizationId, getOrganizationEventId(organizationId, 0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id", notNullValue())
      .body("originalUrl", nullValue())
      .body("name", is("PM-Maastojuoksukilpailut La 27.5.2017 klo 11.00"))
      .body("description", is("Sarjoja 9-15 -vuotiaille ja aikuisille Sarjat ja matkat: * 9v. 1 km * 10v. 1,5 km * 11v. 1,5 km * 12v. 2 km * 13v. 2 km * 14v. 3 km * 15v. 3 km * naiset 3 km * miehet 6 km Ilmoittautuminen 20.5. mennessä kilpailukalenteri.fin kautta. Jälki-ilmoittautumiset osoitteeseen mari.pilssari@virkistys.fi Osallistumismaksut * 9-13 v. 5 €, jälki-ilmoittautuminen 10 € * 14-15 v. ja aikuiset 10 €, jälki-ilmoittautuminen 15 € Maksu tilille: Mäntyharjun Virkistys/yleisurheilu, FI93 5271 2120 0102 90 Lisätietoja: www.virkistys.fi, Anniina Kähkönen, anniinakahkonen@hotmail.com\nTapahtumapaikka: Mäntyharjun urheilupuisto, Urheilutie 1-3, 52700 Mäntyharju\nJärjestäjä: Mäntyharjun Virkistys, yleisurheilujaosto\n"))
      .body("start", sameInstant(getInstant(2017, 5, 26, 21, 00, TIMEZONE_ID)))
      .body("end", nullValue())
      .body("city", nullValue())
      .body("place", nullValue())
      .body("address", nullValue())
      .body("zip", nullValue());
  } 
  
  @Test
  public void testListEventImages() throws IOException {
    String organizationId = getOrganizationId(0);
    String eventId = getOrganizationEventId(organizationId, 0);
    String imageId = getEventImageId(organizationId, eventId, 0);
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/events/{eventId}/images", organizationId, eventId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(1))
      .body("contentType[0]", is("image/jpeg"));
      
    String exprectedMd5 = getResourceMd5("test-image-1000-667.jpg");    
    assertNotNull(exprectedMd5);
    assertEquals(exprectedMd5, getImageMd5(organizationId, eventId, imageId));
  } 

  @Test
  public void testListEventImageChange() throws IOException {
    String organizationId = getOrganizationId(0);
    String eventId = getOrganizationEventId(organizationId, 0);
    
    String originaldMd5 = getResourceMd5("test-image-1000-667.jpg");    
    assertNotNull(originaldMd5);
    assertEquals(originaldMd5, getImageMd5(organizationId, eventId, getEventImageId(organizationId, eventId, 0)));
    
    getLinkedEventsEventMocker()
      .mockAlternative("mantyharju:pm-maastojuoksukilpailut-la-27", "image3");

    Awaitility.await()
      .atMost(Duration.FIVE_MINUTES)
      .until(() -> !originaldMd5.equals(getImageMd5(organizationId, eventId, getEventImageId(organizationId, eventId, 0))));
    
    String changedMd5 = getResourceMd5("test-image-667-1000.jpg");    
    assertNotNull(changedMd5);
    assertEquals(changedMd5, getImageMd5(organizationId, eventId, getEventImageId(organizationId, eventId, 0)));
  } 
  
  @Test
  public void testListEvents() {
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/events", getOrganizationId(0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(3))
      .body("originalUrl[1]", nullValue())
      .body("name[1]", is("Tanssit La 8.7.2017 klo 20.00"))
      .body("description[1]", is("Kake Randelin & Company. Ravintolassa karaoke\nTapahtumapaikka: Nurmaan lava\nJärjestäjä: Nurmaanseudun maamiesseura\n"))
      .body("start[1]", sameInstant(getInstant(2017, 7, 7, 21, 00, TIMEZONE_ID)))
      .body("end[1]", nullValue())
      .body("city[1]", nullValue())
      .body("place[1]", nullValue())
      .body("address[1]", nullValue())
      .body("zip[1]", nullValue());    
  } 
  
  @Test
  public void testOrganizationEventsNotFound() throws InterruptedException {
    String organizationId = getOrganizationId(0);
    String incorrectOrganizationId = getOrganizationId(1);
    String organizationEventId = getOrganizationEventId(organizationId, 0);
    
    String[] malformedIds = new String[] {"evil", "*", "/", "1", "-1", "~"};
    assertFound(String.format("/organizations/%s/events/%s", organizationId, organizationEventId));
    assertEquals(3, countApiList(String.format("/organizations/%s/events", organizationId)));
    
    for (String malformedId : malformedIds) {
      assertNotFound(String.format("/organizations/%s/events/%s", organizationId, malformedId));
    }
    
    assertNotFound(String.format("/organizations/%s/events/%s", incorrectOrganizationId, organizationEventId));
    assertEquals(0, countApiList(String.format("/organizations/%s/events", incorrectOrganizationId)));
  }
  
  /**
   * Calculates MD5 checksum for event image
   * 
   * @param organizationId organization id
   * @param eventId event id
   * @param imageId image id
   * @return MD5 checksum
   * @throws IOException thrown when image downloading fails
   */
  private String getImageMd5(String organizationId, String eventId, String imageId) throws IOException {
    InputStream imageData = givenReadonly()
      .get("/organizations/{organizationId}/events/{eventId}/images/{imageId}/data", organizationId, eventId, imageId)
      .asInputStream();
    
    String result = DigestUtils.md5Hex(imageData);
    assertNotNull(result);
    return result;
  }
  
  private void createLinkedEventsSettings(String organizationId) {
    insertOrganizationSetting(organizationId, LinkedEventsConsts.ORGANIZATION_SETTING_BASEURL, String.format("%s/v1", getWireMockBasePath(), BASE_URL));
    flushCache();
  }
   
  private void deleteLinkedEventsSettings(String organizationId) {
    deleteOrganizationSetting(organizationId, LinkedEventsConsts.ORGANIZATION_SETTING_BASEURL);
  }
  
}
