package fi.otavanopisto.kuntaapi.server.jobs;

import fi.metatavu.kuntaapi.server.rest.model.Job;
import fi.otavanopisto.kuntaapi.server.integrations.JobProvider.JobOrderDirection;

/**
 * Job comparator for publication start time
 * 
 * @author Antti Leppä
 */
public class PublicationStartComparator extends AbstractJobComparator {

  public PublicationStartComparator(JobOrderDirection orderDirection) {
    super(orderDirection);
  }

  @Override
  public int compare(Job o1, Job o2) {
    return this.compareOffsetDateTimes(o1.getPublicationStart(), o2.getPublicationStart());
  }
  
}