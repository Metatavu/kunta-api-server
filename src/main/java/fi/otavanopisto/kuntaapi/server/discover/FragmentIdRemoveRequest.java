package fi.otavanopisto.kuntaapi.server.discover;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.FragmentId;

public class FragmentIdRemoveRequest extends AbstractIdRemoveRequest<FragmentId> {

  private OrganizationId organizationId;
  
  public FragmentIdRemoveRequest(OrganizationId organizationId, FragmentId id) {
    super(id);
    this.organizationId = organizationId;
  }
  
  public OrganizationId getOrganizationId() {
    return organizationId;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof FragmentIdRemoveRequest) {
      FragmentIdRemoveRequest another = (FragmentIdRemoveRequest) obj;
      return another.getId().equals(this.getId()) && another.getOrganizationId().equals(this.getOrganizationId());
    }

    return false;
  }
  
  @Override
  public int hashCode() {
    return new HashCodeBuilder(1183, 1185)
      .append(getOrganizationId())
      .append(getId())
      .hashCode();
  }
}
