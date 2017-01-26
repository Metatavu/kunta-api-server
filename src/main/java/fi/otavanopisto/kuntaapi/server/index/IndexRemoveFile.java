package fi.otavanopisto.kuntaapi.server.index;

public class IndexRemoveFile implements IndexRemove {

  private String fileId;

  @Override
  public String getId() {
    return String.format("%s", fileId);
  }

  @Override
  public String getType() {
    return "file";
  }
  
  public String getFileId() {
    return fileId;
  }
  
  public void setFileId(String fileId) {
    this.fileId = fileId;
  }
  
}
