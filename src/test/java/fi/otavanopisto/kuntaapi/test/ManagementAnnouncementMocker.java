package fi.otavanopisto.kuntaapi.test;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;

import fi.metatavu.management.client.model.Announcement;

@SuppressWarnings ("squid:S1166")
public class ManagementAnnouncementMocker extends AbstractBaseMocker {

  private static final String PATH_TEMPLATE = "%s/%s";
  private static final String RESOURCES_PATH = "/wp-json/wp/v2/announcement";
  
  private ManagementResourceMocker<Integer, Announcement> announcementMocker = new ManagementResourceMocker<>();

  public ManagementAnnouncementMocker() {
    mockLists();
  }
  
  @Override
  public void startMock() {
    super.startMock();
    announcementMocker.start();
  }
  
  @Override
  public void endMock() {
    announcementMocker.stop();
    super.endMock();
  }
  
  /**
   * Mocks management announcements
   * 
   * @param ids announcement ids
   * @return mocker
   */
  public ManagementAnnouncementMocker mockAnnouncements(Integer... ids) {
    try {
      for (Integer id : ids) {
        if (!announcementMocker.isMocked(id)) {
          Announcement announcement = readAnnouncementFromJSONFile(String.format("management/announcements/%d.json", id));
          mockAnnouncement(announcement);
        } else {
          announcementMocker.setStatus(id, MockedResourceStatus.OK);
        }
      }
    } catch (JsonProcessingException e) {
      fail(e.getMessage());
    }
    
    return this;
  }
  
  /**
   * Unmocks management announcements
   * 
   * @param ids announcement ids
   * @return mocker
   */
  public ManagementAnnouncementMocker unmockAnnouncements(Integer... ids) {
    for (Integer id : ids) {
      announcementMocker.setStatus(id, MockedResourceStatus.NOT_FOUND);
    }
    
    return this;
  }
  
  public ManagementAnnouncementMocker mockLists() {
    Map<String, StringValuePattern> queryParams = new HashMap<>();
    queryParams.put("per_page", containing("100"));
    queryParams.put("page", containing("1"));
    
    announcementMocker.addStatusList(MockedResourceStatus.OK, urlPathEqualTo(RESOURCES_PATH));
    announcementMocker.addStatusList(MockedResourceStatus.OK, urlPathEqualTo(RESOURCES_PATH), queryParams);

    return this;
  }
  
  private void mockAnnouncement(Announcement announcement) throws JsonProcessingException {
    Integer announcementId = announcement.getId();
    String path = String.format(PATH_TEMPLATE, RESOURCES_PATH, announcementId);
    announcementMocker.add(announcementId, announcement, urlPathEqualTo(path));
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
  
}
