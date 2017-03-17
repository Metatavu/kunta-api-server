package fi.otavanopisto.kuntaapi.server.integrations.kuntarekry;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.rest.model.Job;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierRelationController;
import fi.otavanopisto.kuntaapi.server.id.JobId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.JobProvider;
import fi.otavanopisto.kuntaapi.server.integrations.kuntarekry.resources.KuntaRekryJobResourceContainer;

@RequestScoped
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
