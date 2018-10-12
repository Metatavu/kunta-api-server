package fi.metatavu.kuntaapi.test.server.integration.ptv;

import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;

import java.io.IOException;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.jayway.restassured.http.ContentType;

@SuppressWarnings ("squid:S1192")
public class OrganizationsTestsIT extends AbstractPtvTest {
  
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
  public void findOrganization() throws InterruptedException, IOException, JSONException {
    int organizationIndex = 2;
    String organizationId = getOrganizationId(organizationIndex);
    waitOrganizationServices(organizationIndex);
    
    String response = givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}", organizationId)
      .body().asString();
    
    assertJSONFileEquals(String.format("ptv/kuntaapi/organizations/%d.json", organizationIndex) , response, getOrganizationCustomizations());
  }

  @Test
  public void findOrganizationCompability() throws InterruptedException, IOException, JSONException {
    int organizationIndex = 1;
    String organizationId = getOrganizationId(organizationIndex);
    waitOrganizationServices(organizationIndex);
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}", organizationId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("areaType", is("Nationwide"));

    givenReadonlyCompabilityMode()
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}", organizationId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("areaType", is("WholeCountry"));
  }
  
  @Test
  public void testListOrganizations() throws IOException {
    int organizationIndex = 0;
    waitOrganizationServices(organizationIndex);
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations")
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(3))
      .body("[0]", jsonEqualsFile(String.format("ptv/kuntaapi/organizations/%d.json", organizationIndex), getOrganizationCustomizations()));
  }
  
  @Test
  public void testListOrganizationsCompability() throws IOException {
    int organizationIndex = 2;
    waitOrganizationServices(organizationIndex);
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations")
      .then()
      .assertThat()
      .statusCode(200)
      .body("[2].areaType", is("Nationwide"));
  
    givenReadonlyCompabilityMode()
      .contentType(ContentType.JSON)
      .get("/organizations")
      .then()
      .assertThat()
      .statusCode(200)
      .body("[2].areaType", is("WholeCountry"));
  }

  @Test
  public void testListOrganizationsByBusinessCode() {
    if (skipElasticSearchTests()) {
      return;
    }
    
    waitForElasticIndex();
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations?businessCode=0209602-6")
      .then()
      .assertThat()
      .statusCode(200)
      .body("size()", is(1))
      .body("id[0]", notNullValue())
      .body("businessName[0]", is("Vaasa"))
      .body("businessCode[0]", is("0209602-6"));
    
    givenReadonly()
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
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations?businessName=Vaasa")
      .then()
      .assertThat()
      .statusCode(200)
      .body("size()", is(1))
      .body("id[0]", notNullValue())
      .body("businessName[0]", is("Vaasa"))
      .body("businessCode[0]", is("0209602-6"));
    
    givenReadonly()
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
    
    String search = "(V*)|(Va*)";
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("/organizations?search=%s", search))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(2))
      .body("businessName[0]", is("Vaasa"))
      .body("businessName[1]", is("Väestörekisterikeskus"));
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("/organizations?search=%s&sortBy=SCORE&sortDir=DESC", search))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(2))
      .body("businessName[0]", is("Vaasa"))
      .body("businessName[1]", is("Väestörekisterikeskus"));
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get(String.format("/organizations?search=%s&sortBy=SCORE&sortDir=ASC", search))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(2))
      .body("businessName[0]", is("Väestörekisterikeskus"))
      .body("businessName[1]", is("Vaasa"));
  }

}