package fi.otavanopisto.kuntaapi.server.discover;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import fi.otavanopisto.kuntaapi.server.id.OrganizationId;

public class OrganizationIdRemoveRequest extends AbstractIdRemoveRequest<OrganizationId> {
  
  public OrganizationIdRemoveRequest(OrganizationId id) {
    super(id);
  }
  
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof OrganizationIdRemoveRequest) {
      OrganizationIdRemoveRequest another = (OrganizationIdRemoveRequest) obj;
      return another.getId().equals(this.getId());
    }

    return false;
  }
  
  @Override
  public int hashCode() {
    return new HashCodeBuilder(1173, 1185)
      .append(getId())
      .hashCode();
  }
}
