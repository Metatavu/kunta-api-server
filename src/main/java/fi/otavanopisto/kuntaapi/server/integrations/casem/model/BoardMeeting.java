package fi.otavanopisto.kuntaapi.server.integrations.casem.model;

public class BoardMeeting {
  
  private String title;
  private String slug;
  
  public String getSlug() {
    return slug;
  }
  
  public void setSlug(String slug) {
    this.slug = slug;
  }
  
  public String getTitle() {
    return title;
  }
  
  public void setTitle(String title) {
    this.title = title;
  }
  
}