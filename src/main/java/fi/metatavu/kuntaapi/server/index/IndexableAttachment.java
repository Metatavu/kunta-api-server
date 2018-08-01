package fi.metatavu.kuntaapi.server.index;

import com.fasterxml.jackson.annotation.JsonProperty;

public class IndexableAttachment {

  @JsonProperty ("_content_type")
  private String contentType;
  
  @JsonProperty ("_name")
  private String name;
  
  @JsonProperty ("_language")
  private String language;
  
  @JsonProperty ("_content")
  private String content;

  public String getContentType() {
    return contentType;
  }

  public void setContentType(String contentType) {
    this.contentType = contentType;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

}
