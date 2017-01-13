package fi.otavanopisto.kuntaapi.server.discover;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.FragmentId;

public class FragmentIdUpdateRequest extends AbstractIdUpdateRequest<FragmentId> {

  private OrganizationId organizationId;
  
  public FragmentIdUpdateRequest(OrganizationId organizationId, FragmentId id, Long orderIndex, boolean priority) {
    super(id, orderIndex, priority);
    this.organizationId = organizationId;
  }
  
  public OrganizationId getOrganizationId() {
    return organizationId;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof FragmentIdUpdateRequest) {
      FragmentIdUpdateRequest another = (FragmentIdUpdateRequest) obj;
      return another.getId().equals(this.getId()) && another.getOrganizationId().equals(this.getOrganizationId());
    }

    return false;
  }
  
  @Override
  public int hashCode() {
    return new HashCodeBuilder(1185, 1187)
      .append(getOrganizationId())
      .append(getId())
      .hashCode();
  }
}
