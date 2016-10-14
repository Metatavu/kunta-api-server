package fi.otavanopisto.kuntaapi.server.integrations;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Class representing service location channel id
 * 
 * @author Antti Leppä
 */
public class ServiceLocationChannelId extends Id {

  /**
   * Zero-argument constructor for service location channel id
   */
  public ServiceLocationChannelId() {
    super();
  }

  /**
   * Constructor that parses a stringified id into source and id 
   * 
   * @param id stringified id
   */
  public ServiceLocationChannelId(String id) {
    super(id);
  }
  
  /**
   * Constructor that accepts source and id
   * 
   * @param source source
   * @param id id
   */
  public ServiceLocationChannelId(String source, String id) {
    super(source, id);
  }
  
  @Override
  public IdType getType() {
    return IdType.SERVICE_LOCATION_CHANNEL;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof ServiceLocationChannelId) {
      ServiceLocationChannelId another = (ServiceLocationChannelId) obj;
      return StringUtils.equals(this.getSource(), another.getSource()) &&  StringUtils.equals(this.getId(), another.getId());
    }

    return false;
  }
  
  @Override
  public int hashCode() {
    return new HashCodeBuilder(211, 223)
      .append(getSource())
      .append(getId())
      .hashCode();
  }
  
}
