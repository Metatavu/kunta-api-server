package fi.metatavu.kuntaapi.server.index;

import java.time.OffsetDateTime;

public class IndexableEmergency implements Indexable {

  @Field(index = "not_analyzed", store = true, type = "long")
  private Long orderIndex;

  @Field
  private String location;

  @Field(index = "not_analyzed", store = true, type = "date")
  private OffsetDateTime time;

  @Field(index = "not_analyzed", store = true)
  private String emergencyId;

  @Field(index = "not_analyzed", store = true)
  private String organizationId;

  @Field
  private String description;

  @Override
  public String getId() {
    return getEmergencyId();
  }

  @Override
  public String getType() {
    return "emergency";
  }

  @Override
  public Long getOrderIndex() {
    return orderIndex;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }

  public OffsetDateTime getTime() {
    return time;
  }

  public void setTime(OffsetDateTime time) {
    this.time = time;
  }

  public String getEmergencyId() {
    return emergencyId;
  }

  public void setEmergencyId(String emergencyId) {
    this.emergencyId = emergencyId;
  }

  public String getOrganizationId() {
    return organizationId;
  }

  public void setOrganizationId(String organizationId) {
    this.organizationId = organizationId;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setOrderIndex(Long orderIndex) {
    this.orderIndex = orderIndex;
  }

}
