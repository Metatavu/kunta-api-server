package fi.otavanopisto.kuntaapi.server.id;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Class representing menu id
 * 
 * @author Antti Leppä
 */
public class MenuId extends Id {
  
  /**
   * Zero-argument constructor for menu id
   */
  public MenuId() {
    super();
  }

  /**
   * Constructor that parses a stringified id into source and id 
   * 
   * @param id stringified id
   */
  public MenuId(String id) {
    super(id);
  }
  
  /**
   * Constructor that accepts source and id
   * 
   * @param source source
   * @param id id
   */
  public MenuId(String source, String id) {
    super(source, id);
  }
  
  @Override
  public IdType getType() {
    return IdType.MENU;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof MenuId) {
      MenuId another = (MenuId) obj;
      return StringUtils.equals(this.getSource(), another.getSource()) &&  StringUtils.equals(this.getId(), another.getId());
    }

    return false;
  }
  
  @Override
  public int hashCode() {
    return new HashCodeBuilder(143, 155)
      .append(getSource())
      .append(getId())
      .hashCode();
  }
  
}
