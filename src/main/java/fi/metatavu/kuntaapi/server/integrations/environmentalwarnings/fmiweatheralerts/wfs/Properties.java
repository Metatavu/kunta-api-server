
package fi.metatavu.kuntaapi.server.integrations.environmentalwarnings.fmiweatheralerts.wfs;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)

public class Properties implements Serializable {

  private static final long serialVersionUID = 7970459479703155080L;
  @JsonProperty("identifier")
  private String identifier;
  @JsonProperty("warning_context")
  private String warningContext;
  @JsonProperty("publication_id")
  private String publicationId;
  @JsonProperty("publication_time")
  private String publicationTime;
  @JsonProperty("causes")
  private String causes;
  @JsonProperty("context_extension")
  private Object contextExtension;
  @JsonProperty("actualization_probability")
  private Double actualizationProbability;
  @JsonProperty("creation_time")
  private String creationTime;
  @JsonProperty("effective_from")
  private String effectiveFrom;
  @JsonProperty("effective_until")
  private String effectiveUntil;
  @JsonProperty("info_en")
  private String infoEn;
  @JsonProperty("info_fi")
  private String infoFi;
  @JsonProperty("info_sv")
  private String infoSv;
  @JsonProperty("severity")
  private String severity;
  @JsonProperty("physical_reference")
  private Object physicalReference;
  @JsonProperty("physical_value")
  private Object physicalValue;
  @JsonProperty("physical_unit")
  private Object physicalUnit;
  @JsonProperty("physical_direction")
  private Object physicalDirection;
  @JsonProperty("geom")
  private Object geom;
  @JsonProperty("representative_x")
  private Object representativeX;
  @JsonProperty("representative_y")
  private Object representativeY;
  @JsonProperty("reference")
  private String reference;
  @JsonProperty("coverage_references")
  private String coverageReferences;

  @JsonProperty("identifier")
  public String getIdentifier() {
    return identifier;
  }

  @JsonProperty("identifier")
  public void setIdentifier(String identifier) {
    this.identifier = identifier;
  }

  @JsonProperty("warning_context")
  public String getWarningContext() {
    return warningContext;
  }

  @JsonProperty("warning_context")
  public void setWarningContext(String warningContext) {
    this.warningContext = warningContext;
  }

  @JsonProperty("publication_id")
  public String getPublicationId() {
    return publicationId;
  }

  @JsonProperty("publication_id")
  public void setPublicationId(String publicationId) {
    this.publicationId = publicationId;
  }

  @JsonProperty("publication_time")
  public String getPublicationTime() {
    return publicationTime;
  }

  @JsonProperty("publication_time")
  public void setPublicationTime(String publicationTime) {
    this.publicationTime = publicationTime;
  }

  @JsonProperty("causes")
  public String getCauses() {
    return causes;
  }

  @JsonProperty("causes")
  public void setCauses(String causes) {
    this.causes = causes;
  }

  @JsonProperty("context_extension")
  public Object getContextExtension() {
    return contextExtension;
  }

  @JsonProperty("context_extension")
  public void setContextExtension(Object contextExtension) {
    this.contextExtension = contextExtension;
  }

  @JsonProperty("actualization_probability")
  public Double getActualizationProbability() {
    return actualizationProbability;
  }

  @JsonProperty("actualization_probability")
  public void setActualizationProbability(Double actualizationProbability) {
    this.actualizationProbability = actualizationProbability;
  }

  @JsonProperty("creation_time")
  public String getCreationTime() {
    return creationTime;
  }

  @JsonProperty("creation_time")
  public void setCreationTime(String creationTime) {
    this.creationTime = creationTime;
  }

  @JsonProperty("effective_from")
  public String getEffectiveFrom() {
    return effectiveFrom;
  }

  @JsonProperty("effective_from")
  public void setEffectiveFrom(String effectiveFrom) {
    this.effectiveFrom = effectiveFrom;
  }

  @JsonProperty("effective_until")
  public String getEffectiveUntil() {
    return effectiveUntil;
  }

  @JsonProperty("effective_until")
  public void setEffectiveUntil(String effectiveUntil) {
    this.effectiveUntil = effectiveUntil;
  }

  @JsonProperty("info_en")
  public String getInfoEn() {
    return infoEn;
  }

  @JsonProperty("info_en")
  public void setInfoEn(String infoEn) {
    this.infoEn = infoEn;
  }

  @JsonProperty("info_fi")
  public String getInfoFi() {
    return infoFi;
  }

  @JsonProperty("info_fi")
  public void setInfoFi(String infoFi) {
    this.infoFi = infoFi;
  }

  @JsonProperty("info_sv")
  public String getInfoSv() {
    return infoSv;
  }

  @JsonProperty("info_sv")
  public void setInfoSv(String infoSv) {
    this.infoSv = infoSv;
  }

  @JsonProperty("severity")
  public String getSeverity() {
    return severity;
  }

  @JsonProperty("severity")
  public void setSeverity(String severity) {
    this.severity = severity;
  }

  @JsonProperty("physical_reference")
  public Object getPhysicalReference() {
    return physicalReference;
  }

  @JsonProperty("physical_reference")
  public void setPhysicalReference(Object physicalReference) {
    this.physicalReference = physicalReference;
  }

  @JsonProperty("physical_value")
  public Object getPhysicalValue() {
    return physicalValue;
  }

  @JsonProperty("physical_value")
  public void setPhysicalValue(Object physicalValue) {
    this.physicalValue = physicalValue;
  }

  @JsonProperty("physical_unit")
  public Object getPhysicalUnit() {
    return physicalUnit;
  }

  @JsonProperty("physical_unit")
  public void setPhysicalUnit(Object physicalUnit) {
    this.physicalUnit = physicalUnit;
  }

  @JsonProperty("physical_direction")
  public Object getPhysicalDirection() {
    return physicalDirection;
  }

  @JsonProperty("physical_direction")
  public void setPhysicalDirection(Object physicalDirection) {
    this.physicalDirection = physicalDirection;
  }

  @JsonProperty("geom")
  public Object getGeom() {
    return geom;
  }

  @JsonProperty("geom")
  public void setGeom(Object geom) {
    this.geom = geom;
  }

  @JsonProperty("representative_x")
  public Object getRepresentativeX() {
    return representativeX;
  }

  @JsonProperty("representative_x")
  public void setRepresentativeX(Object representativeX) {
    this.representativeX = representativeX;
  }

  @JsonProperty("representative_y")
  public Object getRepresentativeY() {
    return representativeY;
  }

  @JsonProperty("representative_y")
  public void setRepresentativeY(Object representativeY) {
    this.representativeY = representativeY;
  }

  @JsonProperty("reference")
  public String getReference() {
    return reference;
  }

  @JsonProperty("reference")
  public void setReference(String reference) {
    this.reference = reference;
  }

  @JsonProperty("coverage_references")
  public String getCoverageReferences() {
    return coverageReferences;
  }

  @JsonProperty("coverage_references")
  public void setCoverageReferences(String coverageReferences) {
    this.coverageReferences = coverageReferences;
  }

}
