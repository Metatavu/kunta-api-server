package fi.otavanopisto.kuntaapi.server.discover;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.TileId;

public class TileIdUpdateRequest extends AbstractIdUpdateRequest<TileId> {

  private OrganizationId organizationId;
  
  public TileIdUpdateRequest(OrganizationId organizationId, TileId id, boolean priority) {
    super(id, priority);
    this.organizationId = organizationId;
  }
  
  public OrganizationId getOrganizationId() {
    return organizationId;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof TileIdUpdateRequest) {
      TileIdUpdateRequest another = (TileIdUpdateRequest) obj;
      return another.getId().equals(this.getId()) && another.getOrganizationId().equals(this.getOrganizationId());
    }

    return false;
  }
  
  @Override
  public int hashCode() {
    return new HashCodeBuilder(1181, 1183)
      .append(getOrganizationId())
      .append(getId())
      .hashCode();
  }
}
