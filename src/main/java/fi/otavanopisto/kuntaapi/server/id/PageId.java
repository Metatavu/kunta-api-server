package fi.otavanopisto.kuntaapi.server.id;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Class representing page id
 * 
 * @author Antti Leppä
 */
public class PageId extends Id {
  
  /**
   * Zero-argument constructor for page id
   */
  public PageId() {
    super();
  }
  
  /**
   * Constructor that accepts source and id
   * 
   * @param source source
   * @param id id
   */
  public PageId(String source, String id) {
    super(source, id);
  }
  
  @Override
  public IdType getType() {
    return IdType.PAGE;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof PageId) {
      PageId another = (PageId) obj;
      return StringUtils.equals(this.getSource(), another.getSource()) &&  StringUtils.equals(this.getId(), another.getId());
    }

    return false;
  }
  
  @Override
  public int hashCode() {
    return new HashCodeBuilder(147, 159)
      .append(getSource())
      .append(getId())
      .hashCode();
  }
  
}
