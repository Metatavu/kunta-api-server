package fi.metatavu.kuntaapi.server.integrations.kuntarekry.tasks;

import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.integrations.kuntarekry.KuntaRekryJob;

public class KuntaRekryJobEntityTask extends AbstractKuntaRekryJobTask {

  private static final long serialVersionUID = 2631366388691557632L;
  
  private OrganizationId organizationId;
  private KuntaRekryJob entity;  
  private Long orderIndex;
  
  public KuntaRekryJobEntityTask() {
    // Zero-argument constructor
  }
  
  public KuntaRekryJobEntityTask(boolean priority, OrganizationId organizationId, KuntaRekryJob entity, Long orderIndex) {
	super(String.format("kuntarekry-job-entity-task-%s-%s", organizationId.toString(), entity.getJobId()), priority);
    this.organizationId = organizationId;
    this.entity = entity;
    this.orderIndex = orderIndex;
  }
  
  public OrganizationId getOrganizationId() {
    return organizationId;
  }
  
  public void setOrganizationId(OrganizationId organizationId) {
    this.organizationId = organizationId;
  }
  
  public KuntaRekryJob getEntity() {
    return entity;
  }
  
  public void setEntity(KuntaRekryJob entity) {
    this.entity = entity;
  }
  
  public Long getOrderIndex() {
    return orderIndex;
  }
  
  public void setOrderIndex(Long orderIndex) {
    this.orderIndex = orderIndex;
  }

}
