package fi.metatavu.kuntaapi.server.index;

public class IndexableStopTime implements Indexable {
  
  @Field (index = "not_analyzed", store = true, type = "long")
  private Long orderIndex;
  
  @Field(index = "not_analyzed", store = true)
  private String id;

  @Field(index = "not_analyzed", store = true)
  private String organizationId;

  @Field(index = "not_analyzed", store = true)
  private String tripId;

  @Field(index = "not_analyzed", store = true)
  private String stopId;

  @Field(index = "not_analyzed", store = true)
  private Integer arrivalTime;

  @Field(index = "not_analyzed", store = true)
  private Integer departureTime;

  @Override
  public String getId() {
    return String.valueOf(id);
  }

  @Override
  public String getType() {
    return "stoptime";
  }

  public String getTripId() {
    return tripId;
  }

  @Override
  public Long getOrderIndex() {
    return orderIndex;
  }
  
  public void setOrderIndex(Long orderIndex) {
    this.orderIndex = orderIndex;
  }
  
  public void setTripId(String tripId) {
    this.tripId = tripId;
  }

  public String getStopId() {
    return stopId;
  }

  public void setStopId(String stopId) {
    this.stopId = stopId;
  }

  public Integer getArrivalTime() {
    return arrivalTime;
  }

  public void setArrivalTime(Integer arrivalTime) {
    this.arrivalTime = arrivalTime;
  }

  public Integer getDepartureTime() {
    return departureTime;
  }

  public void setDepartureTime(Integer departureTime) {
    this.departureTime = departureTime;
  }

  public void setId(String id) {
    this.id = id;
  }
  
  public void setOrganizationId(String organizationId) {
    this.organizationId = organizationId;
  }
  
  public String getOrganizationId() {
    return organizationId;
  }

}
