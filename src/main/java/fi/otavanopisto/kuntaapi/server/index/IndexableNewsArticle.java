package fi.otavanopisto.kuntaapi.server.index;

import java.time.OffsetDateTime;
import java.util.List;

public class IndexableNewsArticle implements Indexable {

  @Field (index = "not_analyzed", store = true, type = "long")
  private Long orderIndex;
  
  @Field (index = "not_analyzed", store = true, type = "date")
  private OffsetDateTime published;  

  @Field (index = "not_analyzed", store = true)
  private String newsArticleId;
  
  @Field (index = "not_analyzed", store = true)
  private String organizationId;
  
  @Field
  private String title;

  @Field
  private String newsAbstract;

  @Field
  private String contents;
  
  @Field (index = "not_analyzed")
  private List<String> tags;
  
  @Override
  public String getId() {
    return newsArticleId;
  }

  @Override
  public String getType() {
    return "newsarticle";
  }

  @Override
  public Long getOrderIndex() {
    return orderIndex;
  }
  
  public void setOrderIndex(Long orderIndex) {
    this.orderIndex = orderIndex;
  }

  public OffsetDateTime getPublished() {
    return published;
  }

  public void setPublished(OffsetDateTime published) {
    this.published = published;
  }

  public String getNewsArticleId() {
    return newsArticleId;
  }

  public void setNewsArticleId(String newsArticleId) {
    this.newsArticleId = newsArticleId;
  }

  public String getOrganizationId() {
    return organizationId;
  }

  public void setOrganizationId(String organizationId) {
    this.organizationId = organizationId;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public void setNewsAbstract(String newsAbstract) {
    this.newsAbstract = newsAbstract;
  }
  
  public String getNewsAbstract() {
    return newsAbstract;
  }
  
  public String getContents() {
    return contents;
  }

  public void setContents(String contents) {
    this.contents = contents;
  }

  public List<String> getTags() {
    return tags;
  }

  public void setTags(List<String> tags) {
    this.tags = tags;
  }
  
}
