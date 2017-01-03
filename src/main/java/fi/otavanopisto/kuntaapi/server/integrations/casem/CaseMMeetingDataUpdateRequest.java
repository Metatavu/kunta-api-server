package fi.otavanopisto.kuntaapi.server.integrations.casem;

public class CaseMMeetingDataUpdateRequest {

  private Long orderIndex;
  private CaseMMeetingData meetingData;
  
  public CaseMMeetingDataUpdateRequest(Long orderIndex, CaseMMeetingData meetingData) {
    this.orderIndex = orderIndex;
    this.meetingData = meetingData;
  }
  
  public CaseMMeetingData getMeetingData() {
    return meetingData;
  }
  
  public Long getOrderIndex() {
    return orderIndex;
  }

}
