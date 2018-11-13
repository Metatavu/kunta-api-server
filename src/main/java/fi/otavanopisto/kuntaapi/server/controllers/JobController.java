package fi.otavanopisto.kuntaapi.server.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.rest.model.Job;
import fi.otavanopisto.kuntaapi.server.id.JobId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.JobProvider;
import fi.otavanopisto.kuntaapi.server.integrations.JobProvider.JobOrder;
import fi.otavanopisto.kuntaapi.server.integrations.JobProvider.JobOrderDirection;
import fi.otavanopisto.kuntaapi.server.jobs.PriorityTitleJobComparator;
import fi.otavanopisto.kuntaapi.server.jobs.PublicationEndComparator;
import fi.otavanopisto.kuntaapi.server.jobs.PublicationStartComparator;
import fi.otavanopisto.kuntaapi.server.settings.OrganizationSettingController;
import fi.otavanopisto.kuntaapi.server.utils.ListUtils;

@ApplicationScoped
@SuppressWarnings ("squid:S3306")
public class JobController {
  
  private static final String ORGANIZATION_SETTING_JOBS_PRIORTY_TITLE = "jobs.priority-title";
  
  @Inject
  private EntityController entityController;
  
  @Inject
  private Instance<JobProvider> jobProviders;

  @Inject
  private OrganizationSettingController organizationSettingController;

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
    
    return ListUtils.limit(sortJobs(organizationId, result, order, orderDirection), firstResult, maxResults);
  }
  
  private List<Job> sortJobs(OrganizationId organizationId, List<Job> jobs, JobOrder order, JobOrderDirection orderDirection) {
    if (order == null) {
      return entityController.sortEntitiesInNaturalOrder(jobs);
    }
    
    List<Job> sorted = new ArrayList<>(jobs);
    
    switch (order) {
      case PUBLICATION_END:
        Collections.sort(sorted, new PublicationEndComparator(orderDirection));
      break;
      case PUBLICATION_START:
        Collections.sort(sorted, new PublicationStartComparator(orderDirection));
      break;
      case PRIORITY_TITLE_PUBLICATION_END:
        String priorityTitle = getPriorityTitle(organizationId);
        Collections.sort(sorted, new PriorityTitleJobComparator(priorityTitle, orderDirection)
          .thenComparing(new PublicationEndComparator(orderDirection)));
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
  
  /**
   * Returns priority title for an organization
   * 
   * @param organizationId organization id
   * @return priority title for an organization
   */
  private String getPriorityTitle(OrganizationId organizationId) {
    return organizationSettingController.getSettingValue(organizationId, ORGANIZATION_SETTING_JOBS_PRIORTY_TITLE);
  }
  
}
