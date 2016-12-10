package fi.otavanopisto.kuntaapi.server.discover;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.TileId;

public class TileIdRemoveRequest extends AbstractIdRemoveRequest<TileId> {

  private OrganizationId organizationId;
  
  public TileIdRemoveRequest(OrganizationId organizationId, TileId id) {
    super(id);
    this.organizationId = organizationId;
  }
  
  public OrganizationId getOrganizationId() {
    return organizationId;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof TileIdRemoveRequest) {
      TileIdRemoveRequest another = (TileIdRemoveRequest) obj;
      return another.getId().equals(this.getId()) && another.getOrganizationId().equals(this.getOrganizationId());
    }

    return false;
  }
  
  @Override
  public int hashCode() {
    return new HashCodeBuilder(1177, 1179)
      .append(getOrganizationId())
      .append(getId())
      .hashCode();
  }
}
