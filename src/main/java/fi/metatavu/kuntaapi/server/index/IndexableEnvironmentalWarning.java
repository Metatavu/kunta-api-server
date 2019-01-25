package fi.metatavu.kuntaapi.server.index;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Search index entity for environmental warning
 * 
 * @author Antti Leppä
 */
public class IndexableEnvironmentalWarning implements Indexable {

  public static final String TYPE = "environmentalwarning";
  public static final String ENVIRONMENTAL_WARNING_ID_FIELD = "environmentalWarningId";
  public static final String ORGANIZATION_ID_FIELD = "organizationId";
  public static final String ORDER_INDEX_FIELD = "orderIndex";
  public static final String WARNING_TYPE_FIELD = "warningType";
  public static final String CONTEXT_FIELD = "context";
  public static final String SEVERITY_FIELD = "severity";
  public static final String START_FIELD = "start";
  public static final String END_FIELD = "end";

  @Field (index = "not_analyzed", store = true)
  private String environmentalWarningId;
  
  @Field (index = "not_analyzed", store = true)
  private String organizationId;
  
  @Field(index = "not_analyzed", store = true, type = "long")
  private Long orderIndex;

  @Field(index = "not_analyzed", store = true)
  private String warningType;

  @Field(index = "not_analyzed", store = true)
  private String context;

  @Field(index = "not_analyzed", store = true)
  private String severity;

  @Field(analyzer = "finnish")
  private String descriptionFi;

  @Field(analyzer = "swedish")
  private String descriptionSv;

  @Field(analyzer = "english")
  private String descriptionEn;

  @Field(index = "not_analyzed")
  private List<String> causes;

  @Field(index = "not_analyzed", type = "double")
  private Double actualizationProbability;

  @Field(index = "not_analyzed", store = true, type = "date")
  private OffsetDateTime start;

  @Field(index = "not_analyzed", store = true, type = "date")
  private OffsetDateTime end;

  @Override
  public String getId() {
    return getEnvironmentalWarningId();
  }

  @Override
  public String getType() {
    return TYPE;
  }
  
  public String getWarningType() {
    return warningType;
  }
  
  public void setWarningType(String warningType) {
    this.warningType = warningType;
  }
  
  public String getEnvironmentalWarningId() {
    return environmentalWarningId;
  }
  
  public void setEnvironmentalWarningId(String environmentalWarningId) {
    this.environmentalWarningId = environmentalWarningId;
  }

  @Override
  public Long getOrderIndex() {
    return orderIndex;
  }

  public String getContext() {
    return context;
  }

  public void setContext(String context) {
    this.context = context;
  }

  public String getSeverity() {
    return severity;
  }

  public void setSeverity(String severity) {
    this.severity = severity;
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

  public List<String> getCauses() {
    return causes;
  }

  public void setCauses(List<String> causes) {
    this.causes = causes;
  }

  public Double getActualizationProbability() {
    return actualizationProbability;
  }

  public void setActualizationProbability(Double actualizationProbability) {
    this.actualizationProbability = actualizationProbability;
  }

  public OffsetDateTime getStart() {
    return start;
  }

  public void setStart(OffsetDateTime start) {
    this.start = start;
  }

  public OffsetDateTime getEnd() {
    return end;
  }

  public void setEnd(OffsetDateTime end) {
    this.end = end;
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
  
}
