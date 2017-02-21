package fi.otavanopisto.kuntaapi.server.tasks;

import java.io.Serializable;

import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Abstract base class for all tasks
 * 
 * @author Antti Leppä
 * @author Heikki Kurhinen
 */
public abstract class AbstractTask implements Serializable {
  
  private static final long serialVersionUID = -4491072590312899600L;
  
  /**
   * Returns hash id for the task
   * 
   * @return hash id for the task
   */
  public int getTaskHash() {
    HashCodeBuilder hashCodeBuilder = new HashCodeBuilder(getTaskHashInitialOddNumber(), getMultiplierOddNumber());
    hashCodeBuilder.append(getHashParts());
    return hashCodeBuilder.toHashCode();
  }
  
  /**
   * Returns objects for calculating task hash id
   * 
   * @return objects for calculating task hash id
   */
  public abstract Object[] getHashParts();
  
  /**
   * Returns initial odd number of calculating hash id. 
   * This number should be unique across the application
   * 
   * @return initial odd number of calculating hash id
   */
  public abstract int getTaskHashInitialOddNumber();
  
  /**
   * Returns multiplier odd number of calculating hash id. 
   * This number should be unique across the application
   * 
   * @return multiplier odd number of calculating hash id
   */
  public abstract int getMultiplierOddNumber();
  
}
