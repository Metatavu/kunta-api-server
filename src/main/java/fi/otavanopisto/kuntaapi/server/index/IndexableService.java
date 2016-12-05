package fi.otavanopisto.kuntaapi.server.index;

import java.util.List;

public class IndexableService implements Indexable {

  @Field(index = "not_analyzed", store = true)
  private String serviceId;
  
  @Field(index = "not_analyzed", store = true)
  private List<String> organizationIds;

  private String name;

  private String alternativeName;

  private String shortDescription;

  private String userInstruction;

  private String description;

  @Field(index = "not_analyzed")
  private String language;

  private List<String> keywords;

  @Override
  public String getId() {
    return String.format("%s_%s", serviceId, language);
  }

  @Override
  public String getType() {
    return "service";
  }

  public String getServiceId() {
    return serviceId;
  }

  public void setServiceId(String serviceId) {
    this.serviceId = serviceId;
  }
  
  public List<String> getOrganizationIds() {
    return organizationIds;
  }
  
  public void setOrganizationIds(List<String> organizationIds) {
    this.organizationIds = organizationIds;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getAlternativeName() {
    return alternativeName;
  }

  public void setAlternativeName(String alternativeName) {
    this.alternativeName = alternativeName;
  }

  public String getShortDescription() {
    return shortDescription;
  }

  public void setShortDescription(String shortDescription) {
    this.shortDescription = shortDescription;
  }

  public String getUserInstruction() {
    return userInstruction;
  }

  public void setUserInstruction(String userInstruction) {
    this.userInstruction = userInstruction;
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

  public List<String> getKeywords() {
    return keywords;
  }

  public void setKeywords(List<String> keywords) {
    this.keywords = keywords;
  }

}
