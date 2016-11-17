package fi.otavanopisto.kuntaapi.server.id;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * Abstract class representing an id in the system
 * 
 * @author Antti Leppä
 */
public abstract class BaseId {

  private String source;
  private String id;
  
  protected BaseId() {
  }

  /**
   * Constructor that accepts source and id parameters
   * 
   * @param source source
   * @param id id
   */
  public BaseId(String source, String id) {
    super();
    this.source = source;
    this.id = id;
    
    if (this.id == null) {
      throw new MalformedIdException("Attempted to create null id");
    }
  }

  public String getId() {
    return id;
  }
  
  public void setId(String id) {
    this.id = id;
  }

  public String getSource() {
    return source;
  }
  
  public void setSource(String source) {
    this.source = source;
  }

  @JsonIgnore
  public abstract IdType getType();

  /**
   * Stringifies id
   */
  @Override
  public String toString() {
    return String.format("%s:%s", source, id);
  }

  /**
   * Returns a hard coded odd number used as the initial value when calculating the hash value
   *   
   * @return a hard coded odd number used as the initial value when calculating the hash value
   */
  protected abstract int getHashInitial();
  
  /**
   * Returns a hard coded odd number used as the multiplier when calculating the hash value
   * 
   * @return a hard coded odd number used as the multiplier when calculating the hash value
   */
  protected abstract int getHashMultiplier();
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof BaseId) {
      BaseId another = (BaseId) obj;
      return StringUtils.equals(this.getSource(), another.getSource()) &&  StringUtils.equals(this.getId(), another.getId());
    }

    return false;
  }
  
  @Override
  public int hashCode() {
    return new HashCodeBuilder(getHashInitial(), getHashMultiplier())
      .append(getSource())
      .append(getId())
      .toHashCode();
  }

}
