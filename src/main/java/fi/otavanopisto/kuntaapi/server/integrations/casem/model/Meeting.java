package fi.otavanopisto.kuntaapi.server.integrations.casem.model;

import java.util.List;

public class Meeting {
  
  private String meetingTitle;
  private boolean memoApproved;
  private List<MeetingItemLink> itemLinks;
  private Councilmen councilmen;
  private List<String> attachments;
  private List<String> agendaAttachments;
  
  public void setMemoApproved(boolean memoApproved) {
    this.memoApproved = memoApproved;
  }
  
  public boolean isMemoApproved() {
    return memoApproved;
  }
  
  public String getMeetingTitle() {
    return meetingTitle;
  }
  
  public void setMeetingTitle(String meetingTitle) {
    this.meetingTitle = meetingTitle;
  }
  
  public List<MeetingItemLink> getItemLinks() {
    return itemLinks;
  }
  
  public void setItemLinks(List<MeetingItemLink> itemLinks) {
    this.itemLinks = itemLinks;
  }

  public Councilmen getCouncilmen() {
    return councilmen;
  }
  
  public void setCouncilmen(Councilmen councilmen) {
    this.councilmen = councilmen;
  }
  
  public List<String> getAttachments() {
    return attachments;
  }
  
  public void setAttachments(List<String> attachments) {
    this.attachments = attachments;
  }
  
  public void setAgendaAttachments(List<String> agendaAttachments) {
    this.agendaAttachments = agendaAttachments;
  }
  
  public List<String> getAgendaAttachments() {
    return agendaAttachments;
  }
  
}
