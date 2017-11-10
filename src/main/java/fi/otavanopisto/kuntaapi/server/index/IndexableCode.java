package fi.otavanopisto.kuntaapi.server.index;

public class IndexableCode implements Indexable {

  @Field (index = "not_analyzed", store = true, type = "long")
  private Long orderIndex;
  
  @Field (index = "not_analyzed", store = true)
  private String codeId;
  
  @Field (index = "not_analyzed", store = true)
  private String codeType;
  
  @Field (index = "not_analyzed", store = true)
  private String code;

  @Field (analyzer = "finnish")
  private String nameFi;

  @Field (analyzer = "swedish")
  private String nameSv;

  @Field (analyzer = "english")
  private String nameEn;
  
  protected IndexableCode() {
  }
  
  public IndexableCode(Long orderIndex, String codeId, String codeType, String code, String nameFi, String nameSv, String nameEn) {
    super();
    this.orderIndex = orderIndex;
    this.codeId = codeId;
    this.codeType = codeType;
    this.code = code;
    this.nameEn = nameEn;
    this.nameSv = nameSv;
    this.nameFi = nameFi;
  }

  @Override
  public String getId() {
    return codeId;
  }

  @Override
  public String getType() {
    return "code";
  }

  @Override
  public Long getOrderIndex() {
    return orderIndex;
  }
  
  public void setOrderIndex(Long orderIndex) {
    this.orderIndex = orderIndex;
  }
  
  public String getCode() {
    return code;
  }
  
  public String getCodeId() {
    return codeId;
  }
  
  public String getNameFi() {
    return nameFi;
  }
  
  public void setNameFi(String nameFi) {
    this.nameFi = nameFi;
  }
  
  public String getNameEn() {
    return nameEn;
  }
  
  public void setNameEn(String nameEn) {
    this.nameEn = nameEn;
  }
  
  public String getNameSv() {
    return nameSv;
  }
  
  public void setNameSv(String nameSv) {
    this.nameSv = nameSv;
  }
  
  public String getCodeType() {
    return codeType;
  }

}
