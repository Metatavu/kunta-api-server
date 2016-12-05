package fi.otavanopisto.kuntaapi.server.discover;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.BannerId;

public class BannerIdRemoveRequest extends AbstractIdRemoveRequest<BannerId> {

  private OrganizationId organizationId;
  
  public BannerIdRemoveRequest(OrganizationId organizationId, BannerId id) {
    super(id);
    this.organizationId = organizationId;
  }
  
  public OrganizationId getOrganizationId() {
    return organizationId;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof BannerIdRemoveRequest) {
      BannerIdRemoveRequest another = (BannerIdRemoveRequest) obj;
      return another.getId().equals(this.getId()) && another.getOrganizationId().equals(this.getOrganizationId());
    }

    return false;
  }
  
  @Override
  public int hashCode() {
    return new HashCodeBuilder(1153, 1165)
      .append(getOrganizationId())
      .append(getId())
      .hashCode();
  }
}
