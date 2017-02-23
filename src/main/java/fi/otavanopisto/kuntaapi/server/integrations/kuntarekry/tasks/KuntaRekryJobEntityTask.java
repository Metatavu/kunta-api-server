package fi.otavanopisto.kuntaapi.server.integrations.kuntarekry.tasks;

import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.kuntarekry.KuntaRekryJob;
import fi.otavanopisto.kuntaapi.server.tasks.AbstractTask;

public class KuntaRekryJobEntityTask extends AbstractTask {

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
  public Object[] getHashParts() {
    return new Object[] { organizationId, getEntity().getJobId() };
  }

  @Override
  public int getTaskHashInitialOddNumber() {
    return 1593;
  }

  @Override
  public int getMultiplierOddNumber() {
    return 1595;
  }
  
}
