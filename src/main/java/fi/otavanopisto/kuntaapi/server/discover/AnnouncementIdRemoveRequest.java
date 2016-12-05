package fi.otavanopisto.kuntaapi.server.discover;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.AnnouncementId;

public class AnnouncementIdRemoveRequest extends AbstractIdRemoveRequest<AnnouncementId> {

  private OrganizationId organizationId;
  
  public AnnouncementIdRemoveRequest(OrganizationId organizationId, AnnouncementId id) {
    super(id);
    this.organizationId = organizationId;
  }
  
  public OrganizationId getOrganizationId() {
    return organizationId;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof AnnouncementIdRemoveRequest) {
      AnnouncementIdRemoveRequest another = (AnnouncementIdRemoveRequest) obj;
      return another.getId().equals(this.getId()) && another.getOrganizationId().equals(this.getOrganizationId());
    }

    return false;
  }
  
  @Override
  public int hashCode() {
    return new HashCodeBuilder(1155, 1173)
      .append(getOrganizationId())
      .append(getId())
      .hashCode();
  }
}
