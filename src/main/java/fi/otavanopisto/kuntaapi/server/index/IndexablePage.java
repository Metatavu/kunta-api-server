package fi.otavanopisto.kuntaapi.server.index;

import java.util.List;

public class IndexablePage implements Indexable, IndexRemove {

  @Field (index = "not_analyzed", store = true, type = "long")
  private Long orderIndex;
  
  @Field (index = "not_analyzed", store = true, type = "integer")
  private Integer orderNumber;
  
  @Field (index = "not_analyzed", store = true)
  private Integer menuOrder;
  
  @Field (index = "not_analyzed", store = true)
  private String pageId;

  @Field (index = "not_analyzed", store = true)
  private String parentId;
  
  @Field (index = "not_analyzed", store = true)
  private String organizationId;
  
  @Field (index = "not_analyzed", store = true)
  private String titleRaw;
  
  @Field(analyzer = "finnish")
  private String titleFi;
  
  @Field(analyzer = "swedish")
  private String titleSv;
  
  @Field(analyzer = "english")
  private String titleEn;

  @Field(analyzer = "finnish")
  private String contentFi;

  @Field(analyzer = "swedish")
  private String contentSv;

  @Field(analyzer = "english")
  private String contentEn;

  @Field (index = "not_analyzed")
  private List<String> tags;
  
  @Override
  public String getId() {
    return String.format("%s", pageId);
  }

  @Override
  public String getType() {
    return "page";
  }

  @Override
  public Long getOrderIndex() {
    return orderIndex;
  }
  
  public void setOrderIndex(Long orderIndex) {
    this.orderIndex = orderIndex;
  }
  
  public Integer getOrderNumber() {
    return orderNumber;
  }
  
  public void setOrderNumber(Integer orderNumber) {
    this.orderNumber = orderNumber;
  }

  public Integer getMenuOrder() {
    return menuOrder;
  }

  public void setMenuOrder(Integer menuOrder) {
    this.menuOrder = menuOrder;
  }

  public String getPageId() {
    return pageId;
  }

  public void setPageId(String pageId) {
    this.pageId = pageId;
  }
  
  public String getParentId() {
    return parentId;
  }

  public void setParentId(String parentId) {
    this.parentId = parentId;
  }

  public String getOrganizationId() {
    return organizationId;
  }

  public void setOrganizationId(String organizationId) {
    this.organizationId = organizationId;
  }
  
  public String getTitleRaw() {
    return titleRaw;
  }

  public void setTitleRaw(String titleRaw) {
    this.titleRaw = titleRaw;
  }

  public String getTitleFi() {
    return titleFi;
  }

  public void setTitleFi(String titleFi) {
    this.titleFi = titleFi;
  }

  public String getTitleSv() {
    return titleSv;
  }

  public void setTitleSv(String titleSv) {
    this.titleSv = titleSv;
  }

  public String getTitleEn() {
    return titleEn;
  }

  public void setTitleEn(String titleEn) {
    this.titleEn = titleEn;
  }

  public String getContentFi() {
    return contentFi;
  }

  public void setContentFi(String contentFi) {
    this.contentFi = contentFi;
  }

  public String getContentSv() {
    return contentSv;
  }

  public void setContentSv(String contentSv) {
    this.contentSv = contentSv;
  }

  public String getContentEn() {
    return contentEn;
  }

  public void setContentEn(String contentEn) {
    this.contentEn = contentEn;
  }
  
  public List<String> getTags() {
    return tags;
  }
  
  public void setTags(List<String> tags) {
    this.tags = tags;
  }
  
}
