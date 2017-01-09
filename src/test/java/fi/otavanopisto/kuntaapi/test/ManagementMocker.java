package fi.otavanopisto.kuntaapi.test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fi.metatavu.management.client.model.Announcement;
import fi.metatavu.management.client.model.Banner;
import fi.metatavu.management.client.model.Menu;
import fi.metatavu.management.client.model.Page;
import fi.metatavu.management.client.model.Post;
import fi.metatavu.management.client.model.Tile;

public class ManagementMocker extends AbstractMocker {
  
  private static final String BANNERS = "/wp-json/wp/v2/banner";
  private static final String MENUS = "/wp-json/kunta-api/menus";
  private static final String PAGES = "/wp-json/wp/v2/pages";
  private static final String POSTS = "/wp-json/wp/v2/posts";
  private static final String TILES = "/wp-json/wp/v2/tile";
  private static final String ANNOUNCEMENTS = "/wp-json/wp/v2/announcement";

  private List<Banner> bannerList = new ArrayList<>();
  private List<Menu> menuList = new ArrayList<>();
  private List<Page> pageList = new ArrayList<>();
  private List<Post> postList = new ArrayList<>();
  private List<Tile> tileList = new ArrayList<>();
  private List<Announcement> announcementList = new ArrayList<>();
  
  public ManagementMocker mockBanners(String... ids) {
    for (String id : ids) {
      Banner banner = readBannerFromJSONFile(String.format("management/banners/%s.json", id));
      mockGetJSON(String.format("%s/%s", BANNERS, id), banner, null);
      bannerList.add(banner);
    }     
    
    return this;
  }
  
  public ManagementMocker mockMenus(String... ids) {
    for (String id : ids) {
      Menu menu = readMenuFromJSONFile(String.format("management/menus/%s.json", id));
      mockGetJSON(String.format("%s/%s", MENUS, id), menu, null);
      menuList.add(menu);
    }     
    
    return this;
  }
  
  public ManagementMocker mockPages(String... ids) {
    for (String id : ids) {
      Page page = readPageFromJSONFile(String.format("management/pages/%s.json", id));
      mockGetJSON(String.format("%s/%s", POSTS, id), page, null);
      pageList.add(page);
    }     
    
    return this;
  }
  
  public ManagementMocker mockPosts(String... ids) {
    for (String id : ids) {
      Post post = readPostFromJSONFile(String.format("management/posts/%s.json", id));
      mockGetJSON(String.format("%s/%s", POSTS, id), post, null);
      postList.add(post);
    }     
    
    return this;
  }
  
  public ManagementMocker mockTiles(String... ids) {
    for (String id : ids) {
      Tile tile = readTileFromJSONFile(String.format("management/tiles/%s.json", id));
      mockGetJSON(String.format("%s/%s", TILES, id), tile, null);
      tileList.add(tile);
    }     
    
    return this;
  }
  
  public ManagementMocker mockAnnouncements(String... ids) {
    for (String id : ids) {
      Announcement announcement = readAnnouncementFromJSONFile(String.format("management/announcements/%s.json", id));
      mockGetJSON(String.format("%s/%s", ANNOUNCEMENTS, id), announcement, null);
      announcementList.add(announcement);
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
   * Reads JSON file as menu object
   * 
   * @param file path to JSON file
   * @return read object
   */    
  private Menu readMenuFromJSONFile(String file) {
    return readJSONFile(file, Menu.class);
  }
  
  /**
   * Reads JSON file as page object
   * 
   * @param file path to JSON file
   * @return read object
   */    
  private Page readPageFromJSONFile(String file) {
    return readJSONFile(file, Page.class);
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
  
  @Override
  public void startMock() {
    Map<String, String> pageQuery = new HashMap<>();
    pageQuery.put("per_page", "100");
    
    mockGetJSON(BANNERS, bannerList, pageQuery);
    mockGetJSON(MENUS, menuList, pageQuery);
    mockGetJSON(PAGES, pageList, pageQuery);
    mockGetJSON(POSTS, postList, pageQuery);
    mockGetJSON(TILES, tileList, pageQuery);
    mockGetJSON(ANNOUNCEMENTS, announcementList, pageQuery);

    mockGetJSON(BANNERS, bannerList, null);
    mockGetJSON(MENUS, menuList, null);
    mockGetJSON(PAGES, pageList, null);
    mockGetJSON(POSTS, postList, null);
    mockGetJSON(TILES, tileList, null);
    mockGetJSON(ANNOUNCEMENTS, announcementList, null);

    super.startMock();
  }
  
}
