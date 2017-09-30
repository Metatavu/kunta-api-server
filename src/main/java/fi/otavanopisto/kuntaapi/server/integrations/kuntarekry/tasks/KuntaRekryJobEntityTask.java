package fi.otavanopisto.kuntaapi.server.integrations.kuntarekry.tasks;

import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.kuntarekry.KuntaRekryJob;

public class KuntaRekryJobEntityTask extends AbstractKuntaRekryJobTask {

  private static final long serialVersionUID = 2631366388691557632L;
  
  private OrganizationId organizationId;
  private KuntaRekryJob entity;  
  private Long orderIndex;
  
  public KuntaRekryJobEntityTask() {
    // Zero-argument constructor
  }
  
  public KuntaRekryJobEntityTask(OrganizationId organizationId, KuntaRekryJob entity, Long orderIndex) {
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

  @Override
  public String getUniqueId() {
    return String.format("kuntarekry-job-entity-task-%s-%s", getOrganizationId().toString(), getEntity().getJobId());
  }
}
