package fi.otavanopisto.kuntaapi.server.integrations;

import java.util.List;

import fi.otavanopisto.kuntaapi.server.id.JobId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.rest.model.Job;

/**
 * Interafce that describes a single event provider
 * 
 * @author Antti Leppä
 */
public interface JobProvider {
  
  /**
   * Finds a single organization job
   * 
   * @param organizationId organization id
   * @param jobId job id
   * @return single organization job or null if not found
   */
  public Job findOrganizationJob(OrganizationId organizationId, JobId jobId);

  /**
   * Lists jobs in an organization
   * 
   * @param organizationId organization id
   * @param order order
   * @param orderDirection ordering direction
   * @return organization jobs
   */
  public List<Job> listOrganizationJobs(OrganizationId organizationId, JobOrder order, JobOrderDirection orderDirection);

  /**
   * Job order direction
   * 
   * @author Antti Leppä
   */
  public enum JobOrderDirection {
    
    ASCENDING,
    
    DESCENDING
  }
  
  /**
   * Job order
   * 
   * @author Antti Leppä
   */
  public enum JobOrder {
    
    PUBLICATION_START,
    
    PUBLICATION_END
  }

  
  
}
