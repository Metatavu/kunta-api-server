package fi.otavanopisto.kuntaapi.server.index;

public class IndexRemovePage implements IndexRemove {

  private String pageId;
  private String language;

  @Override
  public String getId() {
    return String.format("%s_%s", pageId, language);
  }

  @Override
  public String getType() {
    return "page";
  }
  
  public String getLanguage() {
    return language;
  }
  
  public void setLanguage(String language) {
    this.language = language;
  }
  
  public String getPageId() {
    return pageId;
  }
  
  public void setPageId(String pageId) {
    this.pageId = pageId;
  }
  
}
