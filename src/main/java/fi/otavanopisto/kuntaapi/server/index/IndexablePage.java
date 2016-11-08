package fi.otavanopisto.kuntaapi.server.index;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class IndexablePage implements Indexable {
  
  @Field (index = "not_analyzed")
  private String pageId;
  
  @Field (index = "not_analyzed")
  private String organizationId;
  
  @Field (index = "not_analyzed")
  private String language;
  
  @Field
  private String title;

  @Field
  private String content;

  @Override
  public String getId() {
    return String.format("%s_%s", pageId, language);
  }

  @Override
  public String getType() {
    return "page";
  }
  
  @JsonIgnore
  public String getLanguageAnalyzer() {
    return AnalyzerMapper.getLanguageAnalyzer(language);
  }

  public String getPageId() {
    return pageId;
  }

  public void setPageId(String pageId) {
    this.pageId = pageId;
  }

  public String getOrganizationId() {
    return organizationId;
  }

  public void setOrganizationId(String organizationId) {
    this.organizationId = organizationId;
  }

  public String getLanguage() {
    return language;
  }
  
  public void setLanguage(String language) {
    this.language = language;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

}
