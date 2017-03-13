package fi.otavanopisto.kuntaapi.test;

import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.github.tomakehurst.wiremock.matching.StringValuePattern;

import fi.metatavu.management.client.model.Post;

@SuppressWarnings ("squid:S1166")
public class ManagementPostMocker extends AbstractBaseMocker {

  private static final String PATH_TEMPLATE = "%s/%s";
  private static final String PAGES_PATH = "/wp-json/wp/v2/posts";
  
  private ResourceMocker<Integer, Post> postMocker = new ResourceMocker<>();

  public ManagementPostMocker() {
    mockLists();
  }
  
  @Override
  public void startMock() {
    super.startMock();
    postMocker.start();
  }
  
  @Override
  public void endMock() {
    postMocker.stop();
    super.endMock();
  }
  
  /**
   * Mocks management posts
   * 
   * @param ids post ids
   * @return mocker
   */
  public ManagementPostMocker mockPosts(Integer... ids) {
    try {
      for (Integer id : ids) {
        if (!postMocker.isMocked(id)) {
          Post post = readPostFromJSONFile(String.format("management/posts/%d.json", id));
          mockPost(post);
        } else {
          postMocker.setStatus(id, MockedResourceStatus.OK);
        }
      }
    } catch (JsonProcessingException e) {
      fail(e.getMessage());
    }
    
    return this;
  }
  
  /**
   * Unmocks management posts
   * 
   * @param ids post ids
   * @return mocker
   */
  public ManagementPostMocker unmockPosts(Integer... ids) {
    for (Integer id : ids) {
      postMocker.setStatus(id, MockedResourceStatus.NOT_FOUND);
    }
    
    return this;
  }
  
  public ManagementPostMocker mockLists() {
    Map<String, StringValuePattern> queryParams = new HashMap<>();
    queryParams.put("per_page", containing("100"));
    queryParams.put("page", containing("1"));
    
    postMocker.addStatusList(MockedResourceStatus.OK, urlPathEqualTo(PAGES_PATH));
    postMocker.addStatusList(MockedResourceStatus.OK, urlPathEqualTo(PAGES_PATH), queryParams);

    return this;
  }
  
  private void mockPost(Post post) throws JsonProcessingException {
    Integer postId = post.getId();
    String path = String.format(PATH_TEMPLATE, PAGES_PATH, postId);
    postMocker.add(postId, post, urlPathEqualTo(path));
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
  
}
