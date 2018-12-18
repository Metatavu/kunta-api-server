package fi.metatavu.kuntaapi.server.resources;

import java.io.InputStream;

public class StoredBinaryData {

  private InputStream dataStream;
  
  private String contentType;

  public StoredBinaryData(String contentType, InputStream dataStream) {
    super();
    this.dataStream = dataStream;
    this.contentType = contentType;
  }
  
  public InputStream getDataStream() {
    return dataStream;
  }
  
  public String getContentType() {
    return contentType;
  }
  
}
