package fi.otavanopisto.kuntaapi.server.integrations.casem.tasks;

import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PageId;
import fi.otavanopisto.kuntaapi.server.tasks.AbstractTask;

public class MeetingDataUpdateTask extends AbstractTask {

  private static final long serialVersionUID = -8254737854730680377L;
  
  private OrganizationId organizationId;
  private PageId meetingPageId;
  private String meetingItemContents;
  private String meetingContent;
  private Long orderIndex;
  
  public MeetingDataUpdateTask() {
    // Zero-argument constructor
  }

  public MeetingDataUpdateTask(OrganizationId organizationId, PageId meetingPageId, String meetingItemContents, String meetingContent, Long orderIndex) {
    super();
    this.organizationId = organizationId;
    this.meetingPageId = meetingPageId;
    this.meetingItemContents = meetingItemContents;
    this.meetingContent = meetingContent;
    this.orderIndex = orderIndex;
  }

  public OrganizationId getOrganizationId() {
    return organizationId;
  }

  public void setOrganizationId(OrganizationId organizationId) {
    this.organizationId = organizationId;
  }

  public PageId getMeetingPageId() {
    return meetingPageId;
  }

  public void setMeetingPageId(PageId meetingPageId) {
    this.meetingPageId = meetingPageId;
  }

  public String getMeetingContent() {
    return meetingContent;
  }
  
  public void setMeetingContent(String meetingContent) {
    this.meetingContent = meetingContent;
  }
  
  public String getMeetingItemContents() {
    return meetingItemContents;
  }
  
  public void setMeetingItemContents(String meetingItemContents) {
    this.meetingItemContents = meetingItemContents;
  }
  
  public Long getOrderIndex() {
    return orderIndex;
  }
  
  public void setOrderIndex(Long orderIndex) {
    this.orderIndex = orderIndex;
  }

}
