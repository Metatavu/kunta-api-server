package fi.metatavu.kuntaapi.test.server.integration.management;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import com.github.tomakehurst.wiremock.junit.WireMockRule;
import com.jayway.restassured.http.ContentType;

import fi.metatavu.kuntaapi.server.integrations.management.ManagementConsts;
import fi.metatavu.kuntaapi.test.AbstractIntegrationTest;

@SuppressWarnings ("squid:S1192")
public class MenuTestsIT extends AbstractIntegrationTest {
  
  /**
   * Starts WireMock
   */
  @Rule
  public WireMockRule wireMockRule = new WireMockRule(getWireMockPort());
  
  @Before
  public void beforeTest() throws InterruptedException {
    getPtvOrganizationMocker()
      .mock("9355a207-efd3-4cfb-a02b-67187f34c822");
    
    getManagementPageMocker()
      .mockPages(456, 567, 678);
    
    getManagementMediaMocker()
      .mockMedias(3001, 3002);
  
    getManagementMenuMocker()
      .mockMenus(5001l)
      .mockMenuItems(5001l, 6001l, 6002l, 6003l);

    startMocks();
    
    waitApiListCount("/organizations", 1);
    String organizationId = getOrganizationId(0);
    createManagementSettings(organizationId);
    waitApiListCount(String.format("/organizations/%s/pages", organizationId), 3);
    waitApiListCount(String.format("/organizations/%s/menus", organizationId), 1); 
    String menuId = getMenuId(organizationId, 0);
    waitApiListCount(String.format("/organizations/%s/menus/%s/items", organizationId, menuId), 3); 
  }

  @After
  public void afterClass() {
    String organizationId = getOrganizationId(0);
    deleteManagementSettings(organizationId);
  }
  
  @Test
  public void testFindMenus() {
    String organizationId = getOrganizationId(0);
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/menus/{menuId}", organizationId, getMenuId(organizationId, 0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id", notNullValue())
      .body("slug", is("menu-1"));
  } 
  
  @Test
  public void testListMenusBySlug() {
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/menus?slug=menu-1", getOrganizationId(0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(1))
      .body("id[0]", notNullValue())
      .body("slug[0]", is("menu-1"));
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/menus?slug=non-existing", getOrganizationId(0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(0));
  } 
  
  @Test
  public void testListMenus() {
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/menus", getOrganizationId(0))
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(1))
      .body("id[0]", notNullValue())
      .body("slug[0]", is("menu-1"));
  } 
  
  @Test
  public void testMenusNotFound() throws InterruptedException {
    String organizationId = getOrganizationId(0);
    String incorrectOrganizationId = getOrganizationId(1);
    String menuId = getMenuId(organizationId, 0);
    
    String[] malformedIds = new String[] {"evil", "*", "/", "1", "-1", "~"};
    assertFound(String.format("/organizations/%s/menus/%s", organizationId, menuId));
    assertEquals(1, countApiList(String.format("/organizations/%s/menus", organizationId)));
    
    for (String malformedId : malformedIds) {
      assertNotFound(String.format("/organizations/%s/menus/%s", organizationId, malformedId));
    }
    
    assertNotFound(String.format("/organizations/%s/menus/%s", incorrectOrganizationId, menuId));
    assertEquals(0, countApiList(String.format("/organizations/%s/menus", incorrectOrganizationId)));
  }
  
  @Test
  public void testFindMenuItem() {
    String organizationId = getOrganizationId(0);
    String menuId = getMenuId(organizationId, 0);
    String menuItemId = getMenuItemId(organizationId, menuId, 0);
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/menus/{menuId}/items/{menuItemId}", organizationId, menuId, menuItemId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id", notNullValue())
      .body("label", is("Bertha"))
      .body("parentItemId", nullValue())
      .body("type", is("PAGE"))      
      .body("pageId", notNullValue())
      .body("fileId", nullValue())
      .body("externalUrl", nullValue());
  }
  
  @Test
  public void testListMenuItems() {
    String organizationId = getOrganizationId(0);
    String menuId = getMenuId(organizationId, 0);
    
    givenReadonly()
      .contentType(ContentType.JSON)
      .get("/organizations/{organizationId}/menus/{menuId}/items", organizationId, menuId)
      .then()
      .assertThat()
      .statusCode(200)
      .body("id.size()", is(3))
      .body("label[1]", is("Abraham"))
      .body("parentItemId[1]", nullValue())
      .body("type[1]", is("PAGE"))      
      .body("pageId[1]", notNullValue())
      .body("fileId[1]", nullValue())
      .body("externalUrl[1]", nullValue());
  }
    
  private void createManagementSettings(String organizationId) {
    insertOrganizationSetting(organizationId, ManagementConsts.ORGANIZATION_SETTING_BASEURL, String.format("%s/wp-json", getWireMockBasePath(), BASE_URL));
    flushCache();
  }
   
  private void deleteManagementSettings(String organizationId) {
    deleteOrganizationSetting(organizationId, ManagementConsts.ORGANIZATION_SETTING_BASEURL);
  }
  
}
