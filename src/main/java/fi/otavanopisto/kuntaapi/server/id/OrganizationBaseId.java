package fi.otavanopisto.kuntaapi.server.id;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;

public abstract class OrganizationBaseId extends BaseId {
 
  private OrganizationId organizationId;
  
  public OrganizationBaseId() {
    super();
  }

  public OrganizationBaseId(OrganizationId organizationId, String source, String id) {
    super(source, id);
    this.organizationId = organizationId;
    
    if (organizationId == null) {
      throw new MalformedIdException("Attempted to create organization base id with null organizationId");
    }
  }
  
  public OrganizationId getOrganizationId() {
    return organizationId;
  }
  
  public void setOrganizationId(OrganizationId organizationId) {
    this.organizationId = organizationId;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof OrganizationBaseId) {
      OrganizationBaseId another = (OrganizationBaseId) obj;
      if (StringUtils.equals(this.getSource(), another.getSource()) &&  StringUtils.equals(this.getId(), another.getId())) {
        if (!this.getSource().equals(KuntaApiConsts.IDENTIFIER_NAME)) {
          return getOrganizationId().equals(another.getOrganizationId());
        } else {
          return true;
        }
      }
    }

    return false;
  }
  
  @Override
  public int hashCode() {
    return new HashCodeBuilder(getHashInitial(), getHashMultiplier())
      .append(getOrganizationId().getSource())
      .append(getOrganizationId().getId())
      .append(getSource())
      .append(getId())
      .hashCode();
  }
  
  
  
}
