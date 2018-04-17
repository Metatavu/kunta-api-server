package fi.otavanopisto.kuntaapi.server.integrations.tpt;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.rest.model.Job;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierRelationController;
import fi.otavanopisto.kuntaapi.server.id.JobId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.JobProvider;
import fi.otavanopisto.kuntaapi.server.integrations.kuntarekry.resources.KuntaRekryJobResourceContainer;
import javax.enterprise.context.ApplicationScoped;

/**
 * Job provider for te-palvelut.fi -integration
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class TptJobProvider implements JobProvider {
  
  @Inject
  private KuntaRekryJobResourceContainer kuntaRekryJobCache;

  @Inject
  private IdentifierRelationController identifierRelationController;

  @Override
  public Job findOrganizationJob(OrganizationId organizationId, JobId jobId) {
    if (identifierRelationController.isChildOf(organizationId, jobId)) {
      return kuntaRekryJobCache.get(jobId);
    }
    
    return null;
  }

  @Override
  public List<Job> listOrganizationJobs(OrganizationId organizationId) {
    List<JobId> jobIds = identifierRelationController.listJobIdsBySourceAndParentId(TptConsts.IDENTIFIER_NAME, organizationId);
    List<Job> jobs = new ArrayList<>(jobIds.size());
    
    for (JobId jobId : jobIds) {
      Job job = kuntaRekryJobCache.get(jobId);
      if (job != null) {
        jobs.add(job);
      }
    }
    
    return jobs;
  }

}
