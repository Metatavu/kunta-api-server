package fi.otavanopisto.kuntaapi.server.index;

public class IndexableServiceLocationServiceChannel implements Indexable {

  @Field (index = "not_analyzed", store = true, type = "long")
  private Long orderIndex;
  
  @Field(index = "not_analyzed", store = true)
  private String serviceLocationServiceChannelId;
  
  @Field(index = "not_analyzed", store = true)
  private String organizationId;

  private String name;

  private String description;

  @Field(index = "not_analyzed")
  private String language;

  @Override
  public String getId() {
    return String.format("%s_%s", serviceLocationServiceChannelId, language);
  }

  @Override
  public String getType() {
    return "service-location-service-channel";
  }

  @Override
  public Long getOrderIndex() {
    return orderIndex;
  }
  
  public void setOrderIndex(Long orderIndex) {
    this.orderIndex = orderIndex;
  }
  
  public String getDescription() {
    return description;
  }
  
  public void setDescription(String description) {
    this.description = description;
  }
  
  public String getLanguage() {
    return language;
  }
  
  public void setLanguage(String language) {
    this.language = language;
  }
  
  public String getName() {
    return name;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public String getOrganizationId() {
    return organizationId;
  }
  
  public void setOrganizationId(String organizationId) {
    this.organizationId = organizationId;
  }
  
  public String getServiceLocationServiceChannelId() {
    return serviceLocationServiceChannelId;
  }
  
  public void setServiceLocationServiceChannelId(String serviceLocationServiceChannelId) {
    this.serviceLocationServiceChannelId = serviceLocationServiceChannelId;
  }

}
