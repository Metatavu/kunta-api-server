package fi.metatavu.kuntaapi.server.integrations.tpt;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.rest.model.Job;
import fi.metatavu.kuntaapi.server.controllers.IdentifierRelationController;
import fi.metatavu.kuntaapi.server.id.JobId;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.integrations.JobProvider;
import fi.metatavu.kuntaapi.server.integrations.tpt.resources.TptJobResourceContainer;

/**
 * Job provider for te-palvelut.fi -integration
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class TptJobProvider implements JobProvider {
  
  @Inject
  private TptJobResourceContainer tptJobResourceContainer;

  @Inject
  private IdentifierRelationController identifierRelationController;

  @Override
  public Job findOrganizationJob(OrganizationId organizationId, JobId jobId) {
    if (identifierRelationController.isChildOf(organizationId, jobId)) {
      return tptJobResourceContainer.get(jobId);
    }
    
    return null;
  }

  @Override
  public List<Job> listOrganizationJobs(OrganizationId organizationId) {
    List<JobId> jobIds = identifierRelationController.listJobIdsBySourceAndParentId(TptConsts.IDENTIFIER_NAME, organizationId);
    List<Job> jobs = new ArrayList<>(jobIds.size());
    
    for (JobId jobId : jobIds) {
      Job job = tptJobResourceContainer.get(jobId);
      if (job != null) {
        jobs.add(job);
      }
    }
    
    return jobs;
  }

}
