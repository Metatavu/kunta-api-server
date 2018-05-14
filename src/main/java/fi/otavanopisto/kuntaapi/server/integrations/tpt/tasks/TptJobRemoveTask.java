package fi.otavanopisto.kuntaapi.server.integrations.tpt.tasks;

import fi.otavanopisto.kuntaapi.server.id.JobId;

/**
 * Job update task for te-palvelut.fi -integration
 * 
 * @author Antti Leppä
 */
public class TptJobRemoveTask extends TptAbstractJobTask {
  
  private static final long serialVersionUID = 4108209204117137108L;
  
  private JobId removedTptJobId;
  
  public TptJobRemoveTask() {
    // Zero-argument constructor
  }
  
  public TptJobRemoveTask(JobId removedTptJobId) {
    this.removedTptJobId = removedTptJobId;
  }
  
  public JobId getRemovedTptJobId() {
    return removedTptJobId;
  }
  
  public void setRemovedTptJobId(JobId removedTptJobId) {
    this.removedTptJobId = removedTptJobId;
  }
  
  @Override
  public String getUniqueId() {
    return String.format("tpt-job-removed-task-%s", getRemovedTptJobId().getId());
  }
  
}