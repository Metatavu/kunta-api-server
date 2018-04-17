package fi.otavanopisto.kuntaapi.server.integrations.tpt.tasks;

import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.tpt.client.model.DocsEntry;

/**
 * Job update task for te-palvelut.fi -integration
 * 
 * @author Antti Leppä
 */
public class TptJobUpdateTask extends TptAbstractJobTask {
  
  private static final long serialVersionUID = -2120946422190891190L;
  
  private Long orderIndex;
  private DocsEntry tptJob;
  private OrganizationId kuntaApiOrganizationId;
  
  public TptJobUpdateTask() {
    // Zero-argument constructor
  }
  
  public TptJobUpdateTask(OrganizationId kuntaApiOrganizationId, DocsEntry tptJob, Long orderIndex) {
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
    return String.format("tpt-job-update-task-%s", tptJob.getId());
  }
  
}
