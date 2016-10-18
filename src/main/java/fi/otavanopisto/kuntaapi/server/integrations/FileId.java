package fi.otavanopisto.kuntaapi.server.integrations;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import fi.otavanopisto.kuntaapi.server.id.Id;
import fi.otavanopisto.kuntaapi.server.id.IdType;

/**
 * Class representing file id
 * 
 * @author Antti Leppä
 */
public class FileId extends Id {
  
  /**
   * Zero-argument constructor for file id
   */
  public FileId() {
    super();
  }

  /**
   * Constructor that parses a stringified id into source and id 
   * 
   * @param id stringified id
   */
  public FileId(String id) {
    super(id);
  }
  
  /**
   * Constructor that accepts source and id
   * 
   * @param source source
   * @param id id
   */
  public FileId(String source, String id) {
    super(source, id);
  }
  
  @Override
  public IdType getType() {
    return IdType.FILE;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof FileId) {
      FileId another = (FileId) obj;
      return StringUtils.equals(this.getSource(), another.getSource()) &&  StringUtils.equals(this.getId(), another.getId());
    }

    return false;
  }
  
  @Override
  public int hashCode() {
    return new HashCodeBuilder(141, 153)
      .append(getSource())
      .append(getId())
      .hashCode();
  }
  
}
