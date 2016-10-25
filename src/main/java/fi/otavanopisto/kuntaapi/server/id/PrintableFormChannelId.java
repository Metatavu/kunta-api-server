package fi.otavanopisto.kuntaapi.server.id;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Class representing printable form service channel id
 * 
 * @author Antti Leppä
 */
public class PrintableFormChannelId extends Id {

  /**
   * Zero-argument constructor for printable form service channel id
   */
  public PrintableFormChannelId() {
    super();
  }

  /**
   * Constructor that parses a stringified id into source and id 
   * 
   * @param id stringified id
   */
  public PrintableFormChannelId(String id) {
    super(id);
  }
  
  /**
   * Constructor that accepts source and id
   * 
   * @param source source
   * @param id id
   */
  public PrintableFormChannelId(String source, String id) {
    super(source, id);
  }
  
  @Override
  public IdType getType() {
    return IdType.PRINTABLE_FORM_CHANNEL;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof PrintableFormChannelId) {
      PrintableFormChannelId another = (PrintableFormChannelId) obj;
      return StringUtils.equals(this.getSource(), another.getSource()) &&  StringUtils.equals(this.getId(), another.getId());
    }

    return false;
  }
  
  @Override
  public int hashCode() {
    return new HashCodeBuilder(191, 203)
      .append(getSource())
      .append(getId())
      .hashCode();
  }
  
}
