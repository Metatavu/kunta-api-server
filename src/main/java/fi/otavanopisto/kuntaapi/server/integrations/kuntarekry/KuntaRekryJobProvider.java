package fi.otavanopisto.kuntaapi.server.integrations.kuntarekry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.rest.model.Job;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.JobId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.JobProvider;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;

@RequestScoped
public class KuntaRekryJobProvider implements JobProvider {
  
  @Inject
  private KuntaRekryClient kuntaRekryClient;
  
  @Inject
  private IdController idController;

  @Inject
  private Logger logger;

  @Override
  public Job findOrganizationJob(OrganizationId organizationId, JobId jobId) {
    KuntaRekryJob kuntaRekryJob = kuntaRekryClient.findJob(organizationId, jobId);
    return translateJob(organizationId, kuntaRekryJob);
  }

  @Override
  public List<Job> listOrganizationJobs(OrganizationId organizationId) {
    List<KuntaRekryJob> kuntaRekryJobs = kuntaRekryClient.listJobs(organizationId);
    return translateJobs(organizationId, kuntaRekryJobs);
  }
  
  private List<Job> translateJobs(OrganizationId organizationId, List<KuntaRekryJob> kuntaRekryJobs) {
    if (kuntaRekryJobs == null) {
      return Collections.emptyList();
    }
    
    List<Job> result = new ArrayList<>(kuntaRekryJobs.size());
    for (KuntaRekryJob kuntaRekryJob : kuntaRekryJobs) {
      Job job = translateJob(organizationId, kuntaRekryJob);
      if (job != null) {
        result.add(job);
      }
    }
    
    return result;
  }
  
  private Job translateJob(OrganizationId organizationId, KuntaRekryJob kuntaRekryJob) {
    if (kuntaRekryJob == null) {
      return null;
    }
    
    JobId kuntaRekry = new JobId(organizationId, KuntaRekryConsts.IDENTIFIER_NAME, String.valueOf(kuntaRekryJob.getJobId()));
    JobId kuntaApiId = idController.translateJobId(kuntaRekry, KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiId == null) {
      logger.severe(String.format("Failed to translate jobId %d into KuntaApiId", kuntaRekryJob.getJobId()));
      return null;
    }
    
    Job result = new Job();
    result.setId(kuntaApiId.getId());
    result.setTitle(kuntaRekryJob.getJobTitle());
    result.setDescription(kuntaRekryJob.getJobDescription());
    result.setEmploymentType(kuntaRekryJob.getEmploymentType());
    result.setLocation(kuntaRekryJob.getLocation());
    result.setOrganisationalUnit(kuntaRekryJob.getOrganisationalUnit());
    result.setDuration(kuntaRekryJob.getEmploymentDuration());
    result.setTaskArea(kuntaRekryJob.getTaskArea());
    result.setPublicationEnd(kuntaRekryJob.getPublicationTimeEnd());
    result.setPublicationStart(kuntaRekryJob.getPublicationTimeStart());
    result.setLink(kuntaRekryJob.getUrl());
    
    return result;
  }

}
