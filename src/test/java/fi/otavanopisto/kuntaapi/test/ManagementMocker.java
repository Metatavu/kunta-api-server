package fi.otavanopisto.kuntaapi.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import fi.metatavu.management.client.model.Menu;
import fi.metatavu.management.client.model.Menuitem;
import fi.metatavu.management.client.model.Pagemappings;

public class ManagementMocker extends AbstractMocker {
  
  private static final String MENUS_PATH = "/wp-json/kunta-api/menus";
  private static final String MENU_ITEMS_PATH = "/wp-json/kunta-api/menus/%s/items";
  private static final String PAGEMAPPINGS_PATH = "/wp-json/kunta-api/pagemappings";
  private static final String PATH_TEMPLATE = "%s/%s";
  
  private Pagemappings pagemappings = null;
  private List<Menu> menuList = new ArrayList<>();
  private Map<String, List<Menuitem>> menuItems = new HashMap<>();
    
  public ManagementMocker mockMenus(String... ids) {
    for (String id : ids) {
      Menu menu = readMenuFromJSONFile(String.format("management/menus/%s.json", id));
      mockGetJSON(String.format(PATH_TEMPLATE, MENUS_PATH, id), menu, null);
      menuList.add(menu);
    }     
    
    return this;
  }
  
  public ManagementMocker mockMenuItems(String menuId, String... ids) {
    for (String id : ids) {
      Menuitem menuItem = readMenuItemFromJSONFile(String.format("management/menuitems/%s.json", id));
      mockGetJSON(String.format(PATH_TEMPLATE, MENUS_PATH, id), menuItem, null);
      if (!menuItems.containsKey(menuId)) {
        menuItems.put(menuId, new ArrayList<>());
      }
      
      menuItems.get(menuId).add(menuItem);
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
  
  @Override
  public void startMock() {
    Map<String, String> pageQuery100 = new HashMap<>();
    pageQuery100.put("per_page", "100");
    Map<String, String> pageQuery1001 = new HashMap<>();
    pageQuery1001.put("per_page", "100");
    pageQuery1001.put("page", "1");
    
    mockGetJSON(MENUS_PATH, menuList, pageQuery100);
    mockGetJSON(MENUS_PATH, menuList, null);
    mockGetJSON(PAGEMAPPINGS_PATH, pagemappings, null);
    
    for (Entry<String, List<Menuitem>> entry : menuItems.entrySet()) {
      String menuId = entry.getKey();
      mockGetJSON(String.format(MENU_ITEMS_PATH, menuId), entry.getValue(), null); 
    }

    super.startMock();
  }
  
}
