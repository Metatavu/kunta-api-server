package fi.otavanopisto.kuntaapi.server.integrations.casem;

import java.util.List;

import fi.otavanopisto.casem.client.model.Content;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PageId;

public class CaseMMeetingData {

  private OrganizationId organizationId;
  private PageId meetingPageId;
  private List<Content> meetingItemContents;
  private Content meetingContent;
  
  public CaseMMeetingData() {
    // Zero-argument constructor
  }

  public CaseMMeetingData(OrganizationId organizationId, PageId meetingPageId, List<Content> meetingItemContents,
      Content meetingContent) {
    super();
    this.organizationId = organizationId;
    this.meetingPageId = meetingPageId;
    this.meetingItemContents = meetingItemContents;
    this.meetingContent = meetingContent;
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

  public List<Content> getMeetingItemContents() {
    return meetingItemContents;
  }

  public void setMeetingItemContents(List<Content> meetingItemContents) {
    this.meetingItemContents = meetingItemContents;
  }

  public Content getMeetingContent() {
    return meetingContent;
  }

  public void setMeetingContent(Content meetingContent) {
    this.meetingContent = meetingContent;
  }

}
