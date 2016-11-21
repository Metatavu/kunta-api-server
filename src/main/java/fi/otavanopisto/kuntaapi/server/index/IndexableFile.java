package fi.otavanopisto.kuntaapi.server.index;

public class IndexableFile implements Indexable {
  
  @Field (index = "not_analyzed", store = true)
  private String fileId;

  @Field (index = "not_analyzed", store = true)
  private String pageId;
  
  @Field (index = "not_analyzed", store = true)
  private String organizationId;
  
  @Field (type = "attachment") 
  private IndexableAttachment data;

  @Override
  public String getId() {
    return String.format("%s", fileId);
  }

  @Override
  public String getType() {
    return "file";
  }
  
  public void setFileId(String fileId) {
    this.fileId = fileId;
  }
  
  public String getFileId() {
    return fileId;
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

  public IndexableAttachment getData() {
    return data;
  }
  
  public void setData(IndexableAttachment data) {
    this.data = data;
  }

}
