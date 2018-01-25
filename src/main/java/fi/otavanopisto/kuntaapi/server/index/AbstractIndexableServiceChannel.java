package fi.otavanopisto.kuntaapi.server.index;

public abstract class AbstractIndexableServiceChannel implements Indexable, IndexRemove {

  @Field(index = "not_analyzed", store = true, type = "long")
  private Long orderIndex;

  @Field(index = "not_analyzed", store = true)
  private String serviceChannelId;

  @Field(index = "not_analyzed", store = true)
  private String organizationId;

  @Field(analyzer = "finnish")
  private String nameFi;

  @Field(analyzer = "swedish")
  private String nameSv;

  @Field(analyzer = "english")
  private String nameEn;

  @Field(analyzer = "finnish")
  private String descriptionFi;

  @Field(analyzer = "swedish")
  private String descriptionSv;

  @Field(analyzer = "english")
  private String descriptionEn;

  @Field(analyzer = "finnish")
  private String shortDescriptionFi;

  @Field(analyzer = "swedish")
  private String shortDescriptionSv;

  @Field(analyzer = "english")
  private String shortDescriptionEn;
  
  public AbstractIndexableServiceChannel() {
    // Zero-argument constructor
  }
  
  public AbstractIndexableServiceChannel(String serviceChannelId) {
    setServiceChannelId(serviceChannelId);
  }

  @Override
  public String getId() {
    return getServiceChannelId();
  }

  @Override
  public Long getOrderIndex() {
    return orderIndex;
  }

  public void setOrderIndex(Long orderIndex) {
    this.orderIndex = orderIndex;
  }

  public String getOrganizationId() {
    return organizationId;
  }

  public void setOrganizationId(String organizationId) {
    this.organizationId = organizationId;
  }

  public String getServiceChannelId() {
    return serviceChannelId;
  }

  public void setServiceChannelId(String serviceChannelId) {
    this.serviceChannelId = serviceChannelId;
  }

  public String getNameFi() {
    return nameFi;
  }

  public void setNameFi(String nameFi) {
    this.nameFi = nameFi;
  }

  public String getNameSv() {
    return nameSv;
  }

  public void setNameSv(String nameSv) {
    this.nameSv = nameSv;
  }

  public String getNameEn() {
    return nameEn;
  }

  public void setNameEn(String nameEn) {
    this.nameEn = nameEn;
  }

  public String getDescriptionFi() {
    return descriptionFi;
  }

  public void setDescriptionFi(String descriptionFi) {
    this.descriptionFi = descriptionFi;
  }

  public String getDescriptionSv() {
    return descriptionSv;
  }

  public void setDescriptionSv(String descriptionSv) {
    this.descriptionSv = descriptionSv;
  }

  public String getDescriptionEn() {
    return descriptionEn;
  }

  public void setDescriptionEn(String descriptionEn) {
    this.descriptionEn = descriptionEn;
  }

  public String getShortDescriptionFi() {
    return shortDescriptionFi;
  }

  public void setShortDescriptionFi(String shortDescriptionFi) {
    this.shortDescriptionFi = shortDescriptionFi;
  }

  public String getShortDescriptionSv() {
    return shortDescriptionSv;
  }

  public void setShortDescriptionSv(String shortDescriptionSv) {
    this.shortDescriptionSv = shortDescriptionSv;
  }

  public String getShortDescriptionEn() {
    return shortDescriptionEn;
  }

  public void setShortDescriptionEn(String shortDescriptionEn) {
    this.shortDescriptionEn = shortDescriptionEn;
  }

}
