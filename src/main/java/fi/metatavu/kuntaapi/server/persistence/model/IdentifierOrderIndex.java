package fi.metatavu.kuntaapi.server.persistence.model;

/**
 * JPA Helper class for reading Identifier kuntaApiId and orderIndex fields
 * 
 * @author Antti Leppä
 */
public class IdentifierOrderIndex {

  private String kuntaApiId;
  private Long orderIndex;

  public IdentifierOrderIndex() {
    // Zero argument constructor
  }

  public IdentifierOrderIndex(String kuntaApiId, Long orderIndex) {
    super();
    this.kuntaApiId = kuntaApiId;
    this.orderIndex = orderIndex;
  }

  public String getKuntaApiId() {
    return kuntaApiId;
  }

  public void setKuntaApiId(String kuntaApiId) {
    this.kuntaApiId = kuntaApiId;
  }

  public Long getOrderIndex() {
    return orderIndex;
  }

  public void setOrderIndex(Long orderIndex) {
    this.orderIndex = orderIndex;
  }

}
