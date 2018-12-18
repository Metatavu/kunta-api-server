package fi.metatavu.kuntaapi.server.jobs;

import java.time.OffsetDateTime;
import java.util.Comparator;

import fi.metatavu.kuntaapi.server.rest.model.Job;
import fi.metatavu.kuntaapi.server.integrations.JobProvider.JobOrderDirection;

/**
 * Abstract base class for job comparators
 * 
 * @author Antti Leppä
 */
public abstract class AbstractJobComparator implements Comparator<Job> {
  
  private JobOrderDirection orderDirection;
  
  /**
   * Constructor
   * 
   * @param orderDirection order direction
   */
  protected AbstractJobComparator(JobOrderDirection orderDirection) {
    this.orderDirection = orderDirection;
  }
  
  /**
   * Returns order direction
   * 
   * @return order direction
   */
  protected JobOrderDirection getOrderDirection() {
    return orderDirection;
  }
  
  /**
   * Compares date times
   * 
   * @param o1 date time 1
   * @param o2 date time 2
   * @return results
   */
  protected int compareOffsetDateTimes(OffsetDateTime o1, OffsetDateTime o2) {
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
  
}