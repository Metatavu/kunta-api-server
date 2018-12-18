package fi.metatavu.kuntaapi.server.integrations.tpt.tasks;

import fi.metatavu.kuntaapi.server.id.JobId;

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
  
  public TptJobRemoveTask(boolean priority, JobId removedTptJobId) {
    super(String.format("tpt-job-removed-task-%s", removedTptJobId.getId()), priority);
    this.removedTptJobId = removedTptJobId;
  }
  
  public JobId getRemovedTptJobId() {
    return removedTptJobId;
  }
  
  public void setRemovedTptJobId(JobId removedTptJobId) {
    this.removedTptJobId = removedTptJobId;
  }
  
}
