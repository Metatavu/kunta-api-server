package fi.otavanopisto.kuntaapi.server.integrations.vcard.mecm;

import java.time.OffsetDateTime;

public class Status {

  private Long id;
  private String creator;
  private String reason;
  private String comment;
  private OffsetDateTime departure;
  private OffsetDateTime arrival;

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getCreator() {
    return creator;
  }

  public void setCreator(String creator) {
    this.creator = creator;
  }

  public String getReason() {
    return reason;
  }

  public void setReason(String reason) {
    this.reason = reason;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String comment) {
    this.comment = comment;
  }

  public OffsetDateTime getDeparture() {
    return departure;
  }

  public void setDeparture(OffsetDateTime departure) {
    this.departure = departure;
  }

  public OffsetDateTime getArrival() {
    return arrival;
  }

  public void setArrival(OffsetDateTime arrival) {
    this.arrival = arrival;
  }

}
