package fi.otavanopisto.kuntaapi.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import fi.metatavu.management.client.model.Announcement;
import fi.metatavu.management.client.model.Attachment;
import fi.metatavu.management.client.model.Banner;
import fi.metatavu.management.client.model.Fragment;
import fi.metatavu.management.client.model.Menu;
import fi.metatavu.management.client.model.Menuitem;
import fi.metatavu.management.client.model.Pagemappings;
import fi.metatavu.management.client.model.Post;
import fi.metatavu.management.client.model.Tile;

public class ManagementMocker extends AbstractMocker {
  
  private static final String MEDIAS_PATH = "/wp-json/wp/v2/media";
  private static final String BANNERS_PATH = "/wp-json/wp/v2/banner";
  private static final String MENUS_PATH = "/wp-json/kunta-api/menus";
  private static final String MENU_ITEMS_PATH = "/wp-json/kunta-api/menus/%s/items";
  private static final String POSTS_PATH = "/wp-json/wp/v2/posts";
  private static final String TILES_PATH = "/wp-json/wp/v2/tile";
  private static final String FRAGMENTS_PATH = "/wp-json/wp/v2/fragment";
  private static final String ANNOUNCEMENTS_PATH = "/wp-json/wp/v2/announcement";
  private static final String PAGEMAPPINGS_PATH = "/wp-json/kunta-api/pagemappings";
  private static final String PATH_TEMPLATE = "%s/%s";
  
  private Pagemappings pagemappings = null;
  private List<Attachment> mediaList = new ArrayList<>();
  private List<Banner> bannerList = new ArrayList<>();
  private List<Menu> menuList = new ArrayList<>();
  private Map<String, List<Menuitem>> menuItems = new HashMap<>();
  private List<Post> postList = new ArrayList<>();
  private List<Tile> tileList = new ArrayList<>();
  private List<Announcement> announcementList = new ArrayList<>();
  private List<Fragment> fragmentList = new ArrayList<>();
    
  public ManagementMocker mockMedia(String... ids) {
    for (String id : ids) {
      Attachment media = readAttachmentFromJSONFile(String.format("management/medias/%s.json", id));
      
      String sourceUrl = String.format("/wp-content/%s", StringUtils.substringAfter(media.getSourceUrl(), "/wp-content/"));
      String fileName = StringUtils.substringAfterLast(sourceUrl, "/");
      
      mockGetBinary(sourceUrl, "image/jpeg", fileName);
      
      mockGetJSON(String.format(PATH_TEMPLATE, MEDIAS_PATH, id), media, null);
      mediaList.add(media);
    }     
    
    return this;
  }
  
  public ManagementMocker mockBanners(String... ids) {
    for (String id : ids) {
      Banner banner = readBannerFromJSONFile(String.format("management/banners/%s.json", id));
      mockGetJSON(String.format(PATH_TEMPLATE, BANNERS_PATH, id), banner, null);
      bannerList.add(banner);
    }     
    
    return this;
  }
  
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
  
  public ManagementMocker mockPosts(String... ids) {
    for (String id : ids) {
      Post post = readPostFromJSONFile(String.format("management/posts/%s.json", id));
      mockGetJSON(String.format(PATH_TEMPLATE, POSTS_PATH, id), post, null);
      postList.add(post);
    }     
    
    return this;
  }
  
  public ManagementMocker mockTiles(String... ids) {
    for (String id : ids) {
      Tile tile = readTileFromJSONFile(String.format("management/tiles/%s.json", id));
      mockGetJSON(String.format(PATH_TEMPLATE, TILES_PATH, id), tile, null);
      tileList.add(tile);
    }     
    
    return this;
  }
  
  public ManagementMocker mockAnnouncements(String... ids) {
    for (String id : ids) {
      Announcement announcement = readAnnouncementFromJSONFile(String.format("management/announcements/%s.json", id));
      mockGetJSON(String.format(PATH_TEMPLATE, ANNOUNCEMENTS_PATH, id), announcement, null);
      announcementList.add(announcement);
    }     
    
    return this;
  }
  
  public ManagementMocker mockFragments(String... ids) {
    for (String id : ids) {
      Fragment fragment = readFragmentFromJSONFile(String.format("management/fragments/%s.json", id));
      mockGetJSON(String.format(PATH_TEMPLATE, FRAGMENTS_PATH, id), fragment, null);
      fragmentList.add(fragment);
    }     
    
    return this;
  }
  
  
  /**
   * Reads JSON file as banner object
   * 
   * @param file path to JSON file
   * @return read object
   */    
  private Banner readBannerFromJSONFile(String file) {
    return readJSONFile(file, Banner.class);
  }
  
  /**
   * Reads JSON file as media object
   * 
   * @param file path to JSON file
   * @return read object
   */
  private Attachment readAttachmentFromJSONFile(String file) {
    return readJSONFile(file, Attachment.class);
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
  
  /**
   * Reads JSON file as post object
   * 
   * @param file path to JSON file
   * @return read object
   */    
  private Post readPostFromJSONFile(String file) {
    return readJSONFile(file, Post.class);
  }
  
  /**
   * Reads JSON file as tile object
   * 
   * @param file path to JSON file
   * @return read object
   */    
  private Tile readTileFromJSONFile(String file) {
    return readJSONFile(file, Tile.class);
  }
  
  /**
   * Reads JSON file as announcement object
   * 
   * @param file path to JSON file
   * @return read object
   */    
  private Announcement readAnnouncementFromJSONFile(String file) {
    return readJSONFile(file, Announcement.class);
  }
  
  /**
   * Reads JSON file as fragment object
   * 
   * @param file path to JSON file
   * @return read object
   */    
  private Fragment readFragmentFromJSONFile(String file) {
    return readJSONFile(file, Fragment.class);
  }

  @Override
  public void startMock() {
    Map<String, String> pageQuery100 = new HashMap<>();
    pageQuery100.put("per_page", "100");
    Map<String, String> pageQuery1001 = new HashMap<>();
    pageQuery1001.put("per_page", "100");
    pageQuery1001.put("page", "1");
    
    mockGetJSON(MEDIAS_PATH, mediaList, pageQuery100);
    mockGetJSON(BANNERS_PATH, bannerList, pageQuery100);
    mockGetJSON(MENUS_PATH, menuList, pageQuery100);
    mockGetJSON(POSTS_PATH, postList, pageQuery100);
    mockGetJSON(TILES_PATH, tileList, pageQuery100);
    mockGetJSON(ANNOUNCEMENTS_PATH, announcementList, pageQuery100);
    mockGetJSON(FRAGMENTS_PATH, fragmentList, pageQuery1001);
    
    mockGetJSON(MEDIAS_PATH, mediaList, null);
    mockGetJSON(BANNERS_PATH, bannerList, null);
    mockGetJSON(MENUS_PATH, menuList, null);
    mockGetJSON(POSTS_PATH, postList, null);
    mockGetJSON(TILES_PATH, tileList, null);
    mockGetJSON(ANNOUNCEMENTS_PATH, announcementList, null);
    mockGetJSON(FRAGMENTS_PATH, fragmentList, null);
    
    mockGetJSON(PAGEMAPPINGS_PATH, pagemappings, null);
    
    for (Entry<String, List<Menuitem>> entry : menuItems.entrySet()) {
      String menuId = entry.getKey();
      mockGetJSON(String.format(MENU_ITEMS_PATH, menuId), entry.getValue(), null); 
    }

    super.startMock();
  }
  
}
