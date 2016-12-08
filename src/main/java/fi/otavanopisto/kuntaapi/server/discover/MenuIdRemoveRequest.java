package fi.otavanopisto.kuntaapi.server.discover;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.MenuId;

public class MenuIdRemoveRequest extends AbstractIdRemoveRequest<MenuId> {

  private OrganizationId organizationId;
  
  public MenuIdRemoveRequest(OrganizationId organizationId, MenuId id) {
    super(id);
    this.organizationId = organizationId;
  }
  
  public OrganizationId getOrganizationId() {
    return organizationId;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof MenuIdRemoveRequest) {
      MenuIdRemoveRequest another = (MenuIdRemoveRequest) obj;
      return another.getId().equals(this.getId()) && another.getOrganizationId().equals(this.getOrganizationId());
    }

    return false;
  }
  
  @Override
  public int hashCode() {
    return new HashCodeBuilder(1175, 1177)
      .append(getOrganizationId())
      .append(getId())
      .hashCode();
  }
}
