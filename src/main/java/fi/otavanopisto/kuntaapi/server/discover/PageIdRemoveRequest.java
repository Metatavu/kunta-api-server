package fi.otavanopisto.kuntaapi.server.discover;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PageId;

public class PageIdRemoveRequest extends AbstractIdRemoveRequest<PageId> {

  private OrganizationId organizationId;
  
  public PageIdRemoveRequest(OrganizationId organizationId, PageId id) {
    super(id);
    this.organizationId = organizationId;
  }
  
  public OrganizationId getOrganizationId() {
    return organizationId;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof PageIdRemoveRequest) {
      PageIdRemoveRequest another = (PageIdRemoveRequest) obj;
      return another.getId().equals(this.getId()) && another.getOrganizationId().equals(this.getOrganizationId());
    }

    return false;
  }
  
  @Override
  public int hashCode() {
    return new HashCodeBuilder(1149, 1161)
      .append(getOrganizationId())
      .append(getId())
      .hashCode();
  }
}
