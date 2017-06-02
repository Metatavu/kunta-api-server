package fi.otavanopisto.kuntaapi.test;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;

import fi.metatavu.management.client.model.Attachment;

@SuppressWarnings ("squid:S1166")
public class ManagementMediaMocker extends AbstractBaseMocker {

  private static final String PATH_TEMPLATE = "%s/%s";
  private static final String RESOURCES_PATH = "/wp-json/wp/v2/media";
  private static final String FILE_PATH = "/wp-content/uploads/%s";
  
  private ManagementResourceMocker<Integer, Attachment> mediaMocker = new ManagementResourceMocker<>();
  private ManagementResourceMocker<Integer, File> fileMocker = new ManagementResourceMocker<>();

  public ManagementMediaMocker() {
    mockLists();
  }
  
  @Override
  public void startMock() {
    super.startMock();
    mediaMocker.start();
    fileMocker.start();
  }
  
  @Override
  public void endMock() {
    mediaMocker.stop();
    fileMocker.stop();
    super.endMock();
  }
  
  /**
   * Mocks management medias
   * 
   * @param ids media ids
   * @return mocker
   */
  public ManagementMediaMocker mockMedias(Integer... ids) {
    try {
      for (Integer id : ids) {
        if (!mediaMocker.isMocked(id)) {
          Attachment attachment = readAttachmentFromJSONFile(String.format("management/medias/%d.json", id));
          String imagePath = StringUtils.substringAfter(attachment.getSourceUrl(), "/uploads/");
          File imageFile = new File(getClass().getClassLoader().getResource(String.format("management/medias/%s", imagePath)).getFile());
              
          mockMedia(attachment);
          mockFile(id, imagePath, imageFile);
        } else {
          mediaMocker.setStatus(id, MockedResourceStatus.OK);
        }
      }
    } catch (IOException e) {
      fail(e.getMessage());
    }
    
    return this;
  }

  /**
   * Unmocks management medias
   * 
   * @param ids media ids
   * @return mocker
   */
  public ManagementMediaMocker unmockMedias(Integer... ids) {
    for (Integer id : ids) {
      mediaMocker.setStatus(id, MockedResourceStatus.NOT_FOUND);
    }
    
    return this;
  }
  
  public ManagementMediaMocker mockLists() {
    Map<String, StringValuePattern> queryParams = new HashMap<>();
    queryParams.put("per_page", containing("100"));
    queryParams.put("page", containing("1"));
    
    mediaMocker.addStatusList(MockedResourceStatus.OK, urlPathEqualTo(RESOURCES_PATH));
    mediaMocker.addStatusList(MockedResourceStatus.OK, urlPathEqualTo(RESOURCES_PATH), queryParams);

    return this;
  }
  
  private void mockMedia(Attachment attachment) throws JsonProcessingException {
    Integer mediaId = attachment.getId();
    String path = String.format(PATH_TEMPLATE, RESOURCES_PATH, mediaId);
    mediaMocker.add(mediaId, attachment, urlPathEqualTo(path));
  }
  
  private void mockFile(Integer mediaId, String imagePath, File imageFile) {
    String path = String.format(FILE_PATH, imagePath);
    fileMocker.add(mediaId, imageFile, urlPathEqualTo(path));
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
  
}
