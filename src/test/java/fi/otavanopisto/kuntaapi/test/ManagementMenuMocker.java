package fi.otavanopisto.kuntaapi.test;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;

import fi.metatavu.management.client.model.Menu;
import fi.metatavu.management.client.model.Menuitem;

@SuppressWarnings ({"squid:S1166","squid:S1075"})
public class ManagementMenuMocker extends AbstractBaseMocker {

  private static final String PATH_TEMPLATE = "%s/%s";
  private static final String RESOURCES_PATH = "/wp-json/kunta-api/menus";
  private static final String MENU_ITEMS_PATH = "/wp-json/kunta-api/menus/%s/items";
  
  private ManagementResourceMocker<Long, Menu> menuMocker = new ManagementResourceMocker<>();

  public ManagementMenuMocker() {
    mockLists();
  }
  
  @Override
  public void startMock() {
    super.startMock();
    menuMocker.start();
  }
  
  @Override
  public void endMock() {
    menuMocker.stop();
    super.endMock();
  }
  
  /**
   * Mocks management menus
   * 
   * @param ids menu ids
   * @return mocker
   */
  public ManagementMenuMocker mockMenus(Long... ids) {
    try {
      for (Long id : ids) {
        if (!menuMocker.isMocked(id)) {
          Menu menu = readMenuFromJSONFile(String.format("management/menus/%d.json", id));
          mockMenu(menu);
          
          ManagementResourceMocker<Long, Menuitem> itemMocker = new ManagementResourceMocker<>();
          itemMocker.addStatusList(MockedResourceStatus.OK, urlPathEqualTo(String.format(MENU_ITEMS_PATH, id)));
          menuMocker.addSubMocker(id, itemMocker);
          
        } else {
          menuMocker.setStatus(id, MockedResourceStatus.OK);
        }
      }
    } catch (JsonProcessingException e) {
      fail(e.getMessage());
    }
    
    return this;
  }
  
  /**
   * Unmocks management menus
   * 
   * @param ids menu ids
   * @return mocker
   */
  public ManagementMenuMocker unmockMenus(Long... ids) {
    for (Long id : ids) {
      menuMocker.setStatus(id, MockedResourceStatus.NOT_FOUND);
    }
    
    return this;
  }
  
  public ManagementMenuMocker mockLists() {
    Map<String, StringValuePattern> queryParams = new HashMap<>();
    queryParams.put("per_page", containing("100"));
    queryParams.put("page", containing("1"));
    
    menuMocker.addStatusList(MockedResourceStatus.OK, urlPathEqualTo(RESOURCES_PATH));
    menuMocker.addStatusList(MockedResourceStatus.OK, urlPathEqualTo(RESOURCES_PATH), queryParams);

    return this;
  }
  
  private void mockMenu(Menu menu) throws JsonProcessingException {
    Long menuId = menu.getId();
    String path = String.format(PATH_TEMPLATE, RESOURCES_PATH, menuId);
    menuMocker.add(menuId, menu, urlPathEqualTo(path));
  }

  public ManagementMenuMocker mockMenuItems(Long menuId, Long... menuItemIds) {
    for (Long menuItemId : menuItemIds) {
      @SuppressWarnings("unchecked")
      ManagementResourceMocker<Long, Menuitem> itemMocker = (ManagementResourceMocker<Long, Menuitem>) menuMocker.getSubMocker(menuId, 0);
      if (!itemMocker.isMocked(menuItemId)) {
        itemMocker.add(menuItemId, readMenuItemFromJSONFile(String.format("management/menuitems/%s.json", menuItemId)), urlPathEqualTo(String.format(PATH_TEMPLATE, String.format(MENU_ITEMS_PATH, menuId), menuItemId)));
      } else {
        itemMocker.setStatus(menuItemId, MockedResourceStatus.OK);
      }
    }
  
   return this;
  }

  /**
   * Reads JSON file as menu object
   * 
   * @param file path to JSON file
   * @return read object
   */    
  private Menu readMenuFromJSONFile(String file) {
    return readJSONFile(file, Menu.class);
  }

  /**
   * Reads JSON file as menu item object
   * 
   * @param file path to JSON file
   * @return read object
   */    
  private Menuitem readMenuItemFromJSONFile(String file) {
    return readJSONFile(file, Menuitem.class);
}
}
