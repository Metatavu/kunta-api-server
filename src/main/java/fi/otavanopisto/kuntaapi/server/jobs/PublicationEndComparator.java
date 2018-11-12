package fi.otavanopisto.kuntaapi.server.jobs;

import fi.metatavu.kuntaapi.server.rest.model.Job;
import fi.otavanopisto.kuntaapi.server.integrations.JobProvider.JobOrderDirection;

/**
 * Job comparator for publication end time
 * 
 * @author Antti Leppä
 */
public class PublicationEndComparator extends AbstractJobComparator {

  public PublicationEndComparator(JobOrderDirection orderDirection) {
    super(orderDirection);
  }

  @Override
  public int compare(Job o1, Job o2) {
    return this.compareOffsetDateTimes(o1.getPublicationEnd(), o2.getPublicationEnd());
  }
  
}