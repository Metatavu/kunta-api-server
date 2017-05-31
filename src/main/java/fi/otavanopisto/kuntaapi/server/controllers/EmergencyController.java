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

import fi.otavanopisto.kuntaapi.server.id.EmergencyId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.EmergencyProvider;
import fi.otavanopisto.kuntaapi.server.integrations.EmergencySortBy;
import fi.otavanopisto.kuntaapi.server.integrations.SortDir;
import fi.otavanopisto.kuntaapi.server.utils.ListUtils;
import fi.metatavu.kuntaapi.server.rest.model.Emergency;

@ApplicationScoped
@SuppressWarnings ({"squid:S3306","squid:S00107"})
public class EmergencyController {
  
  @Inject
  private EntityController entityController;
  
  @Inject
  private Instance<EmergencyProvider> emergencyProviders;
  
  public List<Emergency> listEmergencies(OrganizationId organizationId, String location, OffsetDateTime before, OffsetDateTime after, EmergencySortBy sortBy, SortDir sortDir, Integer firstResult, Integer maxResults) {
    List<Emergency> result = new ArrayList<>();
   
    for (EmergencyProvider emergencyProvider : getEmergencyProviders()) {
      result.addAll(emergencyProvider.listOrganizationEmergencies(organizationId, location, before, after));
    }
    
    if (sortBy == EmergencySortBy.NATURAL) {
      result = entityController.sortEntitiesInNaturalOrder(result, sortDir);
    } else {
      Collections.sort(result, new EmergencyTimeComparator(sortDir));
    }
    
    return ListUtils.limit(result, firstResult, maxResults);
  }

  public Emergency findEmergency(OrganizationId organizationId, EmergencyId emergencyId) {
    for (EmergencyProvider emergencyProvider : getEmergencyProviders()) {
      Emergency emergency = emergencyProvider.findOrganizationEmergency(organizationId, emergencyId);
      if (emergency != null) {
        return emergency;
      }
    }
    
    return null;
  }
  
  private List<EmergencyProvider> getEmergencyProviders() {
    List<EmergencyProvider> result = new ArrayList<>();
    
    Iterator<EmergencyProvider> iterator = emergencyProviders.iterator();
    while (iterator.hasNext()) {
      result.add(iterator.next());
    }
    
    return Collections.unmodifiableList(result);
  }
  
  private class EmergencyTimeComparator implements Comparator<Emergency> {
    
    private SortDir sortDir;
    
    public EmergencyTimeComparator(SortDir sortDir) {
      this.sortDir = sortDir;
    }
    
    @Override
    public int compare(Emergency emergency1, Emergency emergency2) {
      int result = compareTimes(emergency1, emergency2);

      if (sortDir == SortDir.DESC) {
        return -result;
      } 
      
      return result;
    }

    private int compareTimes(Emergency emergency1, Emergency emergency2) {
      return compareDates(emergency1.getTime(), emergency2.getTime());
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
