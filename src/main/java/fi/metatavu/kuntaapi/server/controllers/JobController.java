package fi.metatavu.kuntaapi.server.controllers;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.id.JobId;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.integrations.JobProvider;
import fi.metatavu.kuntaapi.server.integrations.JobProvider.JobOrder;
import fi.metatavu.kuntaapi.server.integrations.JobProvider.JobOrderDirection;
import fi.metatavu.kuntaapi.server.utils.ListUtils;
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
        Collections.sort(sorted, (Job o1, Job o2) -> compareOffsetDateTimes(o1.getPublicationEnd(), o2.getPublicationEnd(), orderDirection));
      break;
      case PUBLICATION_START:
        Collections.sort(sorted, (Job o1, Job o2) -> compareOffsetDateTimes(o1.getPublicationStart(), o2.getPublicationStart(), orderDirection));
      break;
      default:
    }

    return sorted;
  }
  
  private int compareOffsetDateTimes(OffsetDateTime o1, OffsetDateTime o2, JobOrderDirection orderDirection) {
    if (o2 == o1) {
      return 0;
    }
    
    if (o2 == null) {
      return orderDirection != JobOrderDirection.ASCENDING ? -1 : 1;
    }
    
    if (o1 == null) {
      return orderDirection != JobOrderDirection.ASCENDING ? 1 : -1;
    }
    
    return orderDirection != JobOrderDirection.ASCENDING ? o2.compareTo(o1) : o1.compareTo(o2);
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
