package fi.otavanopisto.kuntaapi.server.id;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Class representing electronic service channel id
 * 
 * @author Antti Leppä
 */
public class ElectronicServiceChannelId extends Id {

  /**
   * Zero-argument constructor for electronic service channel id
   */
  public ElectronicServiceChannelId() {
    super();
  }
  
  /**
   * Constructor that accepts source and id
   * 
   * @param source source
   * @param id id
   */
  public ElectronicServiceChannelId(String source, String id) {
    super(source, id);
  }
  
  @Override
  public IdType getType() {
    return IdType.ELECTRONIC_SERVICE_CHANNEL;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ElectronicServiceChannelId) {
      ElectronicServiceChannelId another = (ElectronicServiceChannelId) obj;
      return StringUtils.equals(this.getSource(), another.getSource()) &&  StringUtils.equals(this.getId(), another.getId());
    }

    return false;
  }
  
  @Override
  public int hashCode() {
    return new HashCodeBuilder(151, 163)
      .append(getSource())
      .append(getId())
      .hashCode();
  }
  
}
