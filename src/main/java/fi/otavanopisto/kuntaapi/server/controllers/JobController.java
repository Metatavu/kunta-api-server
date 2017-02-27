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
import fi.otavanopisto.kuntaapi.server.integrations.JobProvider.JobOrder;
import fi.otavanopisto.kuntaapi.server.integrations.JobProvider.JobOrderDirection;
import fi.otavanopisto.kuntaapi.server.utils.ListUtils;
import fi.metatavu.kuntaapi.server.rest.model.Job;

@ApplicationScoped
@SuppressWarnings ("squid:S3306")
public class JobController {

  @Inject
  private EntityController entityController;
  
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
  
  public List<Job> listJobs(OrganizationId organizationId, JobOrder order, JobOrderDirection orderDirection, Long firstResult, Long maxResults) {
    List<Job> result = new ArrayList<>();
    for (JobProvider jobProvider : getJobProviders()) {
      result.addAll(jobProvider.listOrganizationJobs(organizationId));
    }
    
    return ListUtils.limit(sortJobs(result, order, orderDirection), firstResult, maxResults);
  }
  
  private List<Job> sortJobs(List<Job> jobs, JobOrder order, JobOrderDirection orderDirection) {
    if (order == null) {
      return entityController.sortEntitiesInNaturalOrder(jobs);
    }
    
    List<Job> sorted = new ArrayList<>(jobs);
    
    switch (order) {
      case PUBLICATION_END:
        Collections.sort(sorted, (Job o1, Job o2)
          -> orderDirection != JobOrderDirection.ASCENDING 
            ? o2.getPublicationEnd().compareTo(o1.getPublicationEnd())
            : o1.getPublicationEnd().compareTo(o2.getPublicationEnd()));
      break;
      case PUBLICATION_START:
        Collections.sort(sorted, (Job o1, Job o2)
          -> orderDirection != JobOrderDirection.ASCENDING 
            ? o2.getPublicationStart().compareTo(o1.getPublicationStart())
            : o1.getPublicationStart().compareTo(o2.getPublicationStart()));
      break;
      default:
    }

    return sorted;
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
