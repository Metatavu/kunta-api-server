package fi.otavanopisto.kuntaapi.server.integrations;

/**
 * Attachment data
 * 
 * @author Antti Leppä
 * @author Heikki Kurhinen
 */
public class AttachmentData {

  private String type;
  private byte[] data;

  /**
   * Attachment constructor
   */
  public AttachmentData() {
    super();
  }

  /**
   * Attachment constructor
   * 
   * @param type attachment content type
   * @param data attachment data as byte array
   */
  public AttachmentData(String type, byte[] data) {
    super();
    this.type = type;
    this.data = data;
  }

  /**
   * Returns data
   * 
   * @return data
   */
  public byte[] getData() {
    return data;
  }

  /**
   * Returns mimetype
   * 
   * @return mimetype
   */
  public String getType() {
    return type;
  }
}
