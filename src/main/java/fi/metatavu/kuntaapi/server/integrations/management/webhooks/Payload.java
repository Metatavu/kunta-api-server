package fi.metatavu.kuntaapi.server.integrations.management.webhooks;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Payload {

  @JsonProperty("ID")
  private String id;

  @JsonProperty("post_status")
  private String postStatus;

  @JsonProperty("hook")
  private String hook;

  @JsonProperty("post_type")
  private String postType;

  @JsonProperty("order_index")
  private Long orderIndex;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getPostStatus() {
    return postStatus;
  }

  public void setPostStatus(String postStatus) {
    this.postStatus = postStatus;
  }

  public String getHook() {
    return hook;
  }

  public void setHook(String hook) {
    this.hook = hook;
  }

  public String getPostType() {
    return postType;
  }

  public void setPostType(String postType) {
    this.postType = postType;
  }
  
  public Long getOrderIndex() {
    return orderIndex;
  }
  
  public void setOrderIndex(Long orderIndex) {
    this.orderIndex = orderIndex;
  }

}