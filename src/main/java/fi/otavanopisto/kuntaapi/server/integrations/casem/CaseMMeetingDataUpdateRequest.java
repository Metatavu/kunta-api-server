package fi.otavanopisto.kuntaapi.server.integrations.casem;

public class CaseMMeetingDataUpdateRequest {

  private CaseMMeetingData meetingData;
  
  public CaseMMeetingDataUpdateRequest(CaseMMeetingData meetingData) {
    this.meetingData = meetingData;
  }
  
  public CaseMMeetingData getMeetingData() {
    return meetingData;
  }

}
