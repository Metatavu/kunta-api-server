package fi.otavanopisto.kuntaapi.server.integrations.tpt.tasks;

import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.tpt.client.model.DocsEntry;
import fi.otavanopisto.kuntaapi.server.tasks.AbstractTask;

/**
 * Job update task for te-palvelut.fi -integration
 * 
 * @author Antti Leppä
 */
public class TptJobEntityTask extends AbstractTask {
  
  private static final long serialVersionUID = -8809460557685004243L;
  
  private Long orderIndex;
  private DocsEntry tptJob;
  private OrganizationId kuntaApiOrganizationId;
  
  public TptJobEntityTask() {
    // Zero-argument constructor
  }
  
  public TptJobEntityTask(OrganizationId kuntaApiOrganizationId, DocsEntry tptJob, Long orderIndex) {
    this.kuntaApiOrganizationId = kuntaApiOrganizationId;
    this.tptJob = tptJob;
    this.orderIndex = orderIndex;
  }

  public DocsEntry getTptJob() {
    return tptJob;
  }
  
  public void setTptJob(DocsEntry tptJob) {
    this.tptJob = tptJob;
  }
  
  public Long getOrderIndex() {
    return orderIndex;
  }
  
  public void setOrderIndex(Long orderIndex) {
    this.orderIndex = orderIndex;
  }
  
  public OrganizationId getKuntaApiOrganizationId() {
    return kuntaApiOrganizationId;
  }
  
  public void setKuntaApiOrganizationId(OrganizationId kuntaApiOrganizationId) {
    this.kuntaApiOrganizationId = kuntaApiOrganizationId;
  }

  @Override
  public String getUniqueId() {
    return String.format("tpt-job-entity-task-%s", tptJob.getId());
  }
  
}
