package fi.otavanopisto.kuntaapi.server.integrations;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Class representing phone service channel id
 * 
 * @author Antti Leppä
 */
public class PhoneChannelId extends Id {

  /**
   * Zero-argument constructor for phone service channel id
   */
  public PhoneChannelId() {
    super();
  }

  /**
   * Constructor that parses a stringified id into source and id 
   * 
   * @param id stringified id
   */
  public PhoneChannelId(String id) {
    super(id);
  }
  
  /**
   * Constructor that accepts source and id
   * 
   * @param source source
   * @param id id
   */
  public PhoneChannelId(String source, String id) {
    super(source, id);
  }
  
  @Override
  public IdType getType() {
    return IdType.PHONE_CHANNEL;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof PhoneChannelId) {
      PhoneChannelId another = (PhoneChannelId) obj;
      return StringUtils.equals(this.getSource(), another.getSource()) &&  StringUtils.equals(this.getId(), another.getId());
    }

    return false;
  }
  
  @Override
  public int hashCode() {
    return new HashCodeBuilder(171, 183)
      .append(getSource())
      .append(getId())
      .hashCode();
  }
  
}
