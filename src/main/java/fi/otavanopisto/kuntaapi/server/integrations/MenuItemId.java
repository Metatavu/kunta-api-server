package fi.otavanopisto.kuntaapi.server.integrations;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Class representing menu item id
 * 
 * @author Antti Leppä
 */
public class MenuItemId extends Id {
  
  /**
   * Zero-argument constructor for menu item id
   */
  public MenuItemId() {
    super();
  }

  /**
   * Constructor that parses a stringified id into source and id 
   * 
   * @param id stringified id
   */
  public MenuItemId(String id) {
    super(id);
  }
  
  /**
   * Constructor that accepts source and id
   * 
   * @param source source
   * @param id id
   */
  public MenuItemId(String source, String id) {
    super(source, id);
  }
  
  @Override
  public IdType getType() {
    return IdType.MENU_ITEM;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof MenuItemId) {
      MenuItemId another = (MenuItemId) obj;
      return StringUtils.equals(this.getSource(), another.getSource()) &&  StringUtils.equals(this.getId(), another.getId());
    }

    return false;
  }
  
  @Override
  public int hashCode() {
    return new HashCodeBuilder(145, 157)
      .append(getSource())
      .append(getId())
      .hashCode();
  }
  
}
