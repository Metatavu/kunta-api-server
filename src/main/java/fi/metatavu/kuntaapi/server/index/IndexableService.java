package fi.metatavu.kuntaapi.server.index;

import java.util.Collections;
import java.util.List;

@SuppressWarnings ("squid:MissingDeprecatedCheck")
public class IndexableService implements Indexable {

  @Field(index = "not_analyzed", store = true, type = "long")
  private Long orderIndex;

  @Field(index = "not_analyzed", store = true)
  private String serviceId;

  @Field(index = "not_analyzed", store = true)
  private List<String> organizationIds;
  
  @Field (analyzer = "finnish")
  private String nameFi;

  @Field (analyzer = "swedish")
  private String nameSv;

  @Field (analyzer = "english")
  private String nameEn;

  @Field (analyzer = "finnish")
  private String alternativeNameFi;

  @Field (analyzer = "swedish")
  private String alternativeNameSv;

  @Field (analyzer = "english")
  private String alternativeNameEn;
  
  @Field (analyzer = "finnish")
  private String shortDescriptionFi;

  @Field (analyzer = "swedish")
  private String shortDescriptionSv;

  @Field (analyzer = "english")
  private String shortDescriptionEn;
  
  @Field (analyzer = "finnish")
  private String userInstructionFi;

  @Field (analyzer = "swedish")
  private String userInstructionSv;

  @Field (analyzer = "english")
  private String userInstructionEn;
  
  @Field (analyzer = "finnish")
  private String descriptionFi;

  @Field (analyzer = "swedish")
  private String descriptionSv;

  @Field (analyzer = "english")
  private String descriptionEn;

  @Field (analyzer = "finnish")
  private List<String> keywordsFi;

  @Field (analyzer = "swedish")
  private List<String> keywordsSv;

  @Field (analyzer = "english")
  private List<String> keywordsEn;

  @Field(index = "not_analyzed", store = true)
  private List<String> electronicServiceChannelIds;

  @Field(index = "not_analyzed", store = true)
  private List<String> phoneServiceChannelIds;

  @Field(index = "not_analyzed", store = true)
  private List<String> serviceLocationServiceChannelIds;

  @Field(index = "not_analyzed", store = true)
  private List<String> printableFormServiceChannelIds;

  @Field(index = "not_analyzed", store = true)
  private List<String> webPageServiceChannelIds;

  @Override
  public String getId() {
    return getServiceId();
  }

  @Override
  public String getType() {
    return "service";
  }

  @Override
  public Long getOrderIndex() {
    return orderIndex;
  }

  public void setOrderIndex(Long orderIndex) {
    this.orderIndex = orderIndex;
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

  @Deprecated
  public String getName() {
    return null;
  }

  @Deprecated
  public String getAlternativeName() {
    return null;
  }

  @Deprecated
  public String getShortDescription() {
    return null;
  }

  @Deprecated
  public String getUserInstruction() {
    return null;
  }

  @Deprecated
  public String getDescription() {
    return null;
  }

  @Deprecated
  public List<String> getKeywords() {
    return Collections.emptyList();
  }
  
  public void setKeywordsEn(List<String> keywordsEn) {
    this.keywordsEn = keywordsEn;
  }
  
  public List<String> getKeywordsEn() {
    return keywordsEn;
  }
  
  public void setKeywordsFi(List<String> keywordsFi) {
    this.keywordsFi = keywordsFi;
  }
  
  public List<String> getKeywordsFi() {
    return keywordsFi;
  }
  
  public void setKeywordsSv(List<String> keywordsSv) {
    this.keywordsSv = keywordsSv;
  }
  
  public List<String> getKeywordsSv() {
    return keywordsSv;
  }

  public List<String> getElectronicServiceChannelIds() {
    return electronicServiceChannelIds;
  }

  public void setElectronicServiceChannelIds(List<String> electronicServiceChannelIds) {
    this.electronicServiceChannelIds = electronicServiceChannelIds;
  }

  public List<String> getPhoneServiceChannelIds() {
    return phoneServiceChannelIds;
  }

  public void setPhoneServiceChannelIds(List<String> phoneServiceChannelIds) {
    this.phoneServiceChannelIds = phoneServiceChannelIds;
  }

  public List<String> getServiceLocationServiceChannelIds() {
    return serviceLocationServiceChannelIds;
  }

  public void setServiceLocationServiceChannelIds(List<String> serviceLocationServiceChannelIds) {
    this.serviceLocationServiceChannelIds = serviceLocationServiceChannelIds;
  }

  public List<String> getPrintableFormServiceChannelIds() {
    return printableFormServiceChannelIds;
  }

  public void setPrintableFormServiceChannelIds(List<String> printableFormServiceChannelIds) {
    this.printableFormServiceChannelIds = printableFormServiceChannelIds;
  }

  public List<String> getWebPageServiceChannelIds() {
    return webPageServiceChannelIds;
  }

  public void setWebPageServiceChannelIds(List<String> webPageServiceChannelIds) {
    this.webPageServiceChannelIds = webPageServiceChannelIds;
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

  public String getAlternativeNameFi() {
    return alternativeNameFi;
  }

  public void setAlternativeNameFi(String alternativeNameFi) {
    this.alternativeNameFi = alternativeNameFi;
  }

  public String getAlternativeNameSv() {
    return alternativeNameSv;
  }

  public void setAlternativeNameSv(String alternativeNameSv) {
    this.alternativeNameSv = alternativeNameSv;
  }

  public String getAlternativeNameEn() {
    return alternativeNameEn;
  }

  public void setAlternativeNameEn(String alternativeNameEn) {
    this.alternativeNameEn = alternativeNameEn;
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

  public String getUserInstructionFi() {
    return userInstructionFi;
  }

  public void setUserInstructionFi(String userInstructionFi) {
    this.userInstructionFi = userInstructionFi;
  }

  public String getUserInstructionSv() {
    return userInstructionSv;
  }

  public void setUserInstructionSv(String userInstructionSv) {
    this.userInstructionSv = userInstructionSv;
  }

  public String getUserInstructionEn() {
    return userInstructionEn;
  }

  public void setUserInstructionEn(String userInstructionEn) {
    this.userInstructionEn = userInstructionEn;
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

}
