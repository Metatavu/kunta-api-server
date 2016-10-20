package fi.otavanopisto.kuntaapi.server.id;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Class representing job id
 * 
 * @author Antti Leppä
 */
public class JobId extends Id {
  
  /**
   * Zero-argument constructor for job id
   */
  public JobId() {
    super();
  }

  /**
   * Constructor that parses a stringified id into source and id 
   * 
   * @param id stringified id
   */
  public JobId(String id) {
    super(id);
  }
  
  /**
   * Constructor that accepts source and id
   * 
   * @param source source
   * @param id id
   */
  public JobId(String source, String id) {
    super(source, id);
  }
  
  @Override
  public IdType getType() {
    return IdType.JOB;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof JobId) {
      JobId another = (JobId) obj;
      return StringUtils.equals(this.getSource(), another.getSource()) &&  StringUtils.equals(this.getId(), another.getId());
    }

    return false;
  }
  
  @Override
  public int hashCode() {
    return new HashCodeBuilder(233, 245)
      .append(getSource())
      .append(getId())
      .hashCode();
  }
  
}
