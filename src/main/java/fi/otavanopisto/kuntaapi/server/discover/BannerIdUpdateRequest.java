package fi.otavanopisto.kuntaapi.server.discover;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.BannerId;

public class BannerIdUpdateRequest extends AbstractIdUpdateRequest<BannerId> {

  private OrganizationId organizationId;
  
  public BannerIdUpdateRequest(OrganizationId organizationId, BannerId id, Long orderIndex, boolean priority) {
    super(id, orderIndex, priority);
    this.organizationId = organizationId;
  }
  
  public OrganizationId getOrganizationId() {
    return organizationId;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof BannerIdUpdateRequest) {
      BannerIdUpdateRequest another = (BannerIdUpdateRequest) obj;
      return another.getId().equals(this.getId()) && another.getOrganizationId().equals(this.getOrganizationId());
    }

    return false;
  }
  
  @Override
  public int hashCode() {
    return new HashCodeBuilder(1151, 1163)
      .append(getOrganizationId())
      .append(getId())
      .hashCode();
  }
}
