package fi.otavanopisto.kuntaapi.server.id;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Class representing service id
 * 
 * @author Antti Leppä
 */
public class OrganizationServiceId extends Id {

  /**
   * Zero-argument constructor for organization service id
   */
  public OrganizationServiceId() {
    super();
  }

  /**
   * Constructor that parses a stringified id into source and id 
   * 
   * @param id stringified id
   */
  public OrganizationServiceId(String id) {
    super(id);
  }
  
  /**
   * Constructor that accepts source and id
   * 
   * @param source source
   * @param id id
   */
  public OrganizationServiceId(String source, String id) {
    super(source, id);
  }
  
  @Override
  public IdType getType() {
    return IdType.ORGANIZATION_SERVICE;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof OrganizationServiceId) {
      OrganizationServiceId another = (OrganizationServiceId) obj;
      return StringUtils.equals(this.getSource(), another.getSource()) &&  StringUtils.equals(this.getId(), another.getId());
    }

    return false;
  }
  
  @Override
  public int hashCode() {
    return new HashCodeBuilder(149, 161)
      .append(getSource())
      .append(getId())
      .hashCode();
  }
  
}
