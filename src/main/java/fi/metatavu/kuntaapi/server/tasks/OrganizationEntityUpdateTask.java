package fi.metatavu.kuntaapi.server.tasks;

import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.metaflow.tasks.impl.DefaultTaskImpl;

/**
 * Organization entity update task
 * 
 * @author Antti Leppä
 *
 */
public class OrganizationEntityUpdateTask extends DefaultTaskImpl {
  
  private static final long serialVersionUID = -1860630132331504727L;
  
  private OrganizationId organizationId;
  private int offset;
  
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
  public OrganizationEntityUpdateTask(OrganizationId organizationId, int offset) {
    super(String.format("organization-entity-update-task-%s-%d", organizationId.toString(), offset), false);
    this.organizationId = organizationId;
    this.offset = offset;
  }

  public OrganizationId getOrganizationId() {
    return organizationId;
  }
  
  public void setOrganizationId(OrganizationId organizationId) {
    this.organizationId = organizationId;
  }
  
  public int getOffset() {
    return offset;
  }
  
  public void setOffset(int offset) {
    this.offset = offset;
  }
  
}
