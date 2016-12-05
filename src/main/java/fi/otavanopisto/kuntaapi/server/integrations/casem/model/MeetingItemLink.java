package fi.otavanopisto.kuntaapi.server.integrations.casem.model;

public class MeetingItemLink {
  
  private Integer article;
  private String text;
  private String slug;
  private boolean hasAttachments;
  
  public Integer getArticle() {
    return article;
  }
  
  public void setArticle(Integer article) {
    this.article = article;
  }
 
  public String getText() {
    return text;
  }
  
  public void setText(String text) {
    this.text = text;
  }
  
  public String getSlug() {
    return slug;
  }
  
  public void setSlug(String slug) {
    this.slug = slug;
  }
  
  public boolean getHasAttachments() {
    return hasAttachments;
  }
  
  public void setHasAttachments(boolean hasAttachments) {
    this.hasAttachments = hasAttachments;
  }

}
