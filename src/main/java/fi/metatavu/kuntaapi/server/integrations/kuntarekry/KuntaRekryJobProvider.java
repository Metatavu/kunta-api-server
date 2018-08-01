package fi.metatavu.kuntaapi.server.integrations.kuntarekry;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.rest.model.Job;
import fi.metatavu.kuntaapi.server.controllers.IdentifierRelationController;
import fi.metatavu.kuntaapi.server.id.JobId;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.integrations.JobProvider;
import fi.metatavu.kuntaapi.server.integrations.kuntarekry.resources.KuntaRekryJobResourceContainer;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class KuntaRekryJobProvider implements JobProvider {
  
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
    List<JobId> jobIds = identifierRelationController.listJobIdsBySourceAndParentId(KuntaRekryConsts.IDENTIFIER_NAME, organizationId);
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
