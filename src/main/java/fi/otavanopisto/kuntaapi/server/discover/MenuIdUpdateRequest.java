package fi.otavanopisto.kuntaapi.server.discover;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.MenuId;

public class MenuIdUpdateRequest extends AbstractIdUpdateRequest<MenuId> {

  private OrganizationId organizationId;
  
  public MenuIdUpdateRequest(OrganizationId organizationId, MenuId id, boolean priority) {
    super(id, priority);
    this.organizationId = organizationId;
  }
  
  public OrganizationId getOrganizationId() {
    return organizationId;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof MenuIdUpdateRequest) {
      MenuIdUpdateRequest another = (MenuIdUpdateRequest) obj;
      return another.getId().equals(this.getId()) && another.getOrganizationId().equals(this.getOrganizationId());
    }

    return false;
  }
  
  @Override
  public int hashCode() {
    return new HashCodeBuilder(1157, 1169)
      .append(getOrganizationId())
      .append(getId())
      .hashCode();
  }
}
