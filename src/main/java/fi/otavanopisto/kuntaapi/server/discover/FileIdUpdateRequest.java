package fi.otavanopisto.kuntaapi.server.discover;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PageId;
import fi.otavanopisto.kuntaapi.server.id.FileId;

public class FileIdUpdateRequest extends AbstractIdUpdateRequest<FileId> {

  private OrganizationId organizationId;
  private PageId pageId;
  
  public FileIdUpdateRequest(OrganizationId organizationId, FileId id, PageId pageId, Long orderIndex, boolean priority) {
    super(id, orderIndex, priority);
    this.pageId = pageId;
    this.organizationId = organizationId;
  }
  
  public OrganizationId getOrganizationId() {
    return organizationId;
  }
  
  public PageId getPageId() {
    return pageId;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof FileIdUpdateRequest) {
      FileIdUpdateRequest another = (FileIdUpdateRequest) obj;
      return another.getId().equals(this.getId()) && another.getOrganizationId().equals(this.getOrganizationId());
    }

    return false;
  }
  
  @Override
  public int hashCode() {
    return new HashCodeBuilder(1189, 1191)
      .append(getOrganizationId())
      .append(getId())
      .hashCode();
  }
}
