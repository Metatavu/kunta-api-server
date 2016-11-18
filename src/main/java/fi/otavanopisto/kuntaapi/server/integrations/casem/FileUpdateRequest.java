package fi.otavanopisto.kuntaapi.server.integrations.casem;

import fi.otavanopisto.kuntaapi.server.id.FileId;
import fi.otavanopisto.kuntaapi.server.id.PageId;

public class FileUpdateRequest {

  private PageId parentId;
  private FileId fileId;

  public FileUpdateRequest(PageId parentId, FileId fileId) {
    super();
    this.parentId = parentId;
    this.fileId = fileId;
  }

  public FileId getFileId() {
    return fileId;
  }
  
  public PageId getPageId() {
    return parentId;
  }
  
}
