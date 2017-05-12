package fi.otavanopisto.kuntaapi.server.controllers;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.id.IncidentId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.IncidentProvider;
import fi.otavanopisto.kuntaapi.server.integrations.IncidentSortBy;
import fi.otavanopisto.kuntaapi.server.integrations.SortDir;
import fi.otavanopisto.kuntaapi.server.utils.ListUtils;
import fi.metatavu.kuntaapi.server.rest.model.Incident;

@ApplicationScoped
@SuppressWarnings ("squid:S3306")
public class IncidentController {
  
  @Inject
  private EntityController entityController;
  
  @Inject
  private Instance<IncidentProvider> incidentProviders;
  
  public List<Incident> listIncidents(OrganizationId organizationId, String slug, OffsetDateTime startBefore, OffsetDateTime endAfter, IncidentSortBy sortBy, SortDir sortDir, Integer firstResult, Integer maxResults) {
    List<Incident> result = new ArrayList<>();
   
    for (IncidentProvider incidentProvider : getIncidentProviders()) {
      result.addAll(incidentProvider.listOrganizationIncidents(organizationId, slug, startBefore, endAfter));
    }
    
    int resultCount = result.size();
    int firstIndex = firstResult == null ? 0 : Math.min(firstResult.intValue(), resultCount);
    int toIndex = maxResults == null ? resultCount : Math.min(firstIndex + maxResults.intValue(), resultCount);
    
    if (sortBy == IncidentSortBy.NATURAL) {
      result = entityController.sortEntitiesInNaturalOrder(result, sortDir);
    } else {
      Collections.sort(result, new IncidentComparator(sortBy, sortDir));
    }
    
    return ListUtils.limit(result, firstIndex, toIndex);
  }

  public Incident findIncident(OrganizationId organizationId, IncidentId incidentId) {
    for (IncidentProvider incidentProvider : getIncidentProviders()) {
      Incident incident = incidentProvider.findOrganizationIncident(organizationId, incidentId);
      if (incident != null) {
        return incident;
      }
    }
    
    return null;
  }
  
  private List<IncidentProvider> getIncidentProviders() {
    List<IncidentProvider> result = new ArrayList<>();
    
    Iterator<IncidentProvider> iterator = incidentProviders.iterator();
    while (iterator.hasNext()) {
      result.add(iterator.next());
    }
    
    return Collections.unmodifiableList(result);
  }
  
  private class IncidentComparator implements Comparator<Incident> {
    
    private IncidentSortBy sortBy;
    private SortDir sortDir;
    
    public IncidentComparator(IncidentSortBy sortBy, SortDir sortDir) {
      this.sortBy = sortBy;
      this.sortDir = sortDir;
    }
    
    @Override
    public int compare(Incident incident1, Incident incident2) {
      int result;
      
      switch (sortBy) {
        case END:
          result = compareEndDates(incident1, incident2);
        break;
        case START:
          result = compareStartDates(incident1, incident2);
        break;
        default:
          result = 0;
        break;
      }
      
      if (sortDir == SortDir.ASC) {
        return -result;
      } 
      
      return result;
    }

    private int compareStartDates(Incident incident1, Incident incident2) {
      return compareDates(incident1.getStart(), incident2.getStart());
    }

    private int compareEndDates(Incident incident1, Incident incident2) {
      return compareDates(incident1.getEnd(), incident2.getEnd());
    }
    
    private int compareDates(OffsetDateTime dateTime1, OffsetDateTime dateTime2) {
      if (dateTime1 == null && dateTime2 == null) {
        return 0;
      }
      
      if (dateTime1 == null) {
        return 1;
      } else if (dateTime2 == null) {
        return -1;
      }
              
      return dateTime1.compareTo(dateTime2);
    }
    
  }
  
}
