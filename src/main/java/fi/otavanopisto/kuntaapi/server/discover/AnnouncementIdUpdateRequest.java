package fi.otavanopisto.kuntaapi.server.discover;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.AnnouncementId;

public class AnnouncementIdUpdateRequest extends AbstractIdUpdateRequest<AnnouncementId> {

  private OrganizationId organizationId;
  
  public AnnouncementIdUpdateRequest(OrganizationId organizationId, AnnouncementId id, Long orderIndex, boolean priority) {
    super(id, orderIndex, priority);
    this.organizationId = organizationId;
  }
  
  public OrganizationId getOrganizationId() {
    return organizationId;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof AnnouncementIdUpdateRequest) {
      AnnouncementIdUpdateRequest another = (AnnouncementIdUpdateRequest) obj;
      return another.getId().equals(this.getId()) && another.getOrganizationId().equals(this.getOrganizationId());
    }

    return false;
  }
  
  @Override
  public int hashCode() {
    return new HashCodeBuilder(1153, 1171)
      .append(getOrganizationId())
      .append(getId())
      .hashCode();
  }
}
