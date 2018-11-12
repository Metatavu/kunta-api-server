package fi.otavanopisto.kuntaapi.server.jobs;

import java.util.Comparator;

import org.apache.commons.lang3.StringUtils;

import fi.metatavu.kuntaapi.server.rest.model.Job;
import fi.otavanopisto.kuntaapi.server.integrations.JobProvider.JobOrderDirection;

/**
 * Job comparator for priority titles
 * 
 * @author Antti Leppä
 */
public class PriorityTitleJobComparator implements Comparator<Job> {

  private JobOrderDirection orderDirection;
  private String priorityTitle;

  /**
   * Constructor
   * 
   * @param priorityTitle priority title
   * @param orderDirection order direction
   */
  public PriorityTitleJobComparator(String priorityTitle, JobOrderDirection orderDirection) {
    super();
    this.priorityTitle = priorityTitle;
    this.orderDirection = orderDirection;
  }

  @Override
  public int compare(Job job1, Job job2) {
    if (priorityTitle == null) {
      return 0;
    }

    boolean priority1 = StringUtils.containsIgnoreCase(job1.getTitle(), priorityTitle);
    boolean priority2 = StringUtils.containsIgnoreCase(job2.getTitle(), priorityTitle);
    
    if (priority1 == priority2) {
      return 0;
    }
    
    if (priority1) {
      return orderDirection != JobOrderDirection.ASCENDING ? -1 : 1;
    }
    
    return orderDirection != JobOrderDirection.ASCENDING ? 1 : -1;
  }

}