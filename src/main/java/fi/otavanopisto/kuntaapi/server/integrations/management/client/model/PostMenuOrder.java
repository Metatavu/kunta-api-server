package fi.otavanopisto.kuntaapi.server.integrations.management.client.model;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * JSON Model class that represents a response from https://github.com/Metatavu/wordpress-rest-menu-order plugin
 * 
 * @author Antti Leppä
 */
public class PostMenuOrder {
  
  @JsonProperty ("menu_order")
  private Integer menuOrder;
  
  public Integer getMenuOrder() {
    return menuOrder;
  }
  
  public void setMenuOrder(Integer menuOrder) {
    this.menuOrder = menuOrder;
  }

}
