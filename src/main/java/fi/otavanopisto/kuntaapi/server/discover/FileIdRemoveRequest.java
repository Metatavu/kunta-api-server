package fi.otavanopisto.kuntaapi.server.discover;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.FileId;

public class FileIdRemoveRequest extends AbstractIdRemoveRequest<FileId> {

  private OrganizationId organizationId;
  
  public FileIdRemoveRequest(OrganizationId organizationId, FileId id) {
    super(id);
    this.organizationId = organizationId;
  }
  
  public OrganizationId getOrganizationId() {
    return organizationId;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof FileIdRemoveRequest) {
      FileIdRemoveRequest another = (FileIdRemoveRequest) obj;
      return another.getId().equals(this.getId()) && another.getOrganizationId().equals(this.getOrganizationId());
    }

    return false;
  }
  
  @Override
  public int hashCode() {
    return new HashCodeBuilder(1187, 1189)
      .append(getOrganizationId())
      .append(getId())
      .hashCode();
  }
}
