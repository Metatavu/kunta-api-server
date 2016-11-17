package fi.otavanopisto.kuntaapi.server.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.id.JobId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.JobProvider;
import fi.otavanopisto.kuntaapi.server.rest.model.Job;

@ApplicationScoped
public class JobController {
  
  @Inject
  private Instance<JobProvider> jobProviders;

  public Job findJob(OrganizationId organizationId, JobId jobId) {
    for (JobProvider jobProvider : getJobProviders()) {
      Job job = jobProvider.findOrganizationJob(organizationId, jobId);
      if (job != null) {
        return job;
      }
    }
    
    return null;
  }
  
  public List<Job> listJobs(OrganizationId organizationId) {
    List<Job> result = new ArrayList<>();
    for (JobProvider jobProvider : getJobProviders()) {
      result.addAll(jobProvider.listOrganizationJobs(organizationId));
    }
    return result;
  }

  private List<JobProvider> getJobProviders() {
    List<JobProvider> result = new ArrayList<>();
    
    Iterator<JobProvider> iterator = jobProviders.iterator();
    while (iterator.hasNext()) {
      result.add(iterator.next());
    }
    
    return Collections.unmodifiableList(result);
  }
}
