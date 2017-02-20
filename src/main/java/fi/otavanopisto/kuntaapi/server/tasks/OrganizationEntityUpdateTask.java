package fi.otavanopisto.kuntaapi.server.tasks;

import fi.otavanopisto.kuntaapi.server.id.OrganizationId;

/**
 * Organization entity update task
 * 
 * @author Antti Leppä
 *
 */
public class OrganizationEntityUpdateTask extends AbstractTask {
  
  private static final long serialVersionUID = -1860630132331504727L;
  
  private OrganizationId organizationId;
  
  /**
   * Zero-argument constructor
   */
  public OrganizationEntityUpdateTask() {
    // Zero-argument constructor
  }
  
  /**
   * Constructor
   * 
   * @param organizationId organization id
   */
  public OrganizationEntityUpdateTask(OrganizationId organizationId) {
    super();
    this.organizationId = organizationId;
  }

  public OrganizationId getOrganizationId() {
    return organizationId;
  }
  
  public void setOrganizationId(OrganizationId organizationId) {
    this.organizationId = organizationId;
  }
  
}
