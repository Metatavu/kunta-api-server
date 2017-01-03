package fi.otavanopisto.kuntaapi.server.discover;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PageId;

public class PageIdUpdateRequest extends AbstractIdUpdateRequest<PageId> {

  private OrganizationId organizationId;
  
  public PageIdUpdateRequest(OrganizationId organizationId, PageId id, Long orderIndex, boolean priority) {
    super(id, orderIndex, priority);
    this.organizationId = organizationId;
  }
  
  public OrganizationId getOrganizationId() {
    return organizationId;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof PageIdUpdateRequest) {
      PageIdUpdateRequest another = (PageIdUpdateRequest) obj;
      return another.getId().equals(this.getId()) && another.getOrganizationId().equals(this.getOrganizationId());
    }

    return false;
  }
  
  @Override
  public int hashCode() {
    return new HashCodeBuilder(1147, 1159)
      .append(getOrganizationId())
      .append(getId())
      .hashCode();
  }
}
