package fi.otavanopisto.kuntaapi.server.integrations;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Class representing webpage service channel id
 * 
 * @author Antti Leppä
 */
public class WebPageChannelId extends Id {

  /**
   * Zero-argument constructor for webpage service channel id
   */
  public WebPageChannelId() {
    super();
  }

  /**
   * Constructor that parses a stringified id into source and id 
   * 
   * @param id stringified id
   */
  public WebPageChannelId(String id) {
    super(id);
  }
  
  /**
   * Constructor that accepts source and id
   * 
   * @param source source
   * @param id id
   */
  public WebPageChannelId(String source, String id) {
    super(source, id);
  }
  
  @Override
  public IdType getType() {
    return IdType.WEBPAGE_CHANNEL;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof WebPageChannelId) {
      WebPageChannelId another = (WebPageChannelId) obj;
      return StringUtils.equals(this.getSource(), another.getSource()) &&  StringUtils.equals(this.getId(), another.getId());
    }

    return false;
  }
  
  @Override
  public int hashCode() {
    return new HashCodeBuilder(231, 243)
      .append(getSource())
      .append(getId())
      .hashCode();
  }
  
}
