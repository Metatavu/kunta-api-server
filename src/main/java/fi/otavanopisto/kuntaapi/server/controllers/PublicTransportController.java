package fi.otavanopisto.kuntaapi.server.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.rest.model.Agency;
import fi.metatavu.kuntaapi.server.rest.model.Schedule;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PublicTransportAgencyId;
import fi.otavanopisto.kuntaapi.server.id.PublicTransportScheduleId;
import fi.otavanopisto.kuntaapi.server.integrations.PublicTransportProvider;

@ApplicationScoped
@SuppressWarnings ("squid:S3306")
public class PublicTransportController {
  
  @Inject
  private EntityController entityController;
  
  @Inject
  private Instance<PublicTransportProvider> publicTransportProviders;
  
  public List<Agency> listAgencies(OrganizationId organizationId, Integer firstResult, Integer maxResults) {
    List<Agency> result = new ArrayList<>();
   
    for (PublicTransportProvider publicTransportProvider : getPublicTransportProviders()) {
      result.addAll(publicTransportProvider.listAgencies(organizationId));
    }
    
    int resultCount = result.size();
    int firstIndex = firstResult == null ? 0 : Math.min(firstResult.intValue(), resultCount);
    int toIndex = maxResults == null ? resultCount : Math.min(firstIndex + maxResults.intValue(), resultCount);
    
    return entityController.sortEntitiesInNaturalOrder(result.subList(firstIndex, toIndex));
  }

  public Agency findAgency(OrganizationId organizationId, PublicTransportAgencyId agencyId) {
    for (PublicTransportProvider publicTransportProvider : getPublicTransportProviders()) {
      Agency agency = publicTransportProvider.findAgency(organizationId, agencyId);
      if (agency != null) {
        return agency;
      }
    }
    
    return null;
  }
  
  public List<Schedule> listSchedules(OrganizationId organizationId, Integer firstResult, Integer maxResults) {
    List<Schedule> result = new ArrayList<>();
   
    for (PublicTransportProvider publicTransportProvider : getPublicTransportProviders()) {
      result.addAll(publicTransportProvider.listSchedules(organizationId));
    }
    
    int resultCount = result.size();
    int firstIndex = firstResult == null ? 0 : Math.min(firstResult.intValue(), resultCount);
    int toIndex = maxResults == null ? resultCount : Math.min(firstIndex + maxResults.intValue(), resultCount);
    
    return entityController.sortEntitiesInNaturalOrder(result.subList(firstIndex, toIndex));
  }
  
  public Schedule findSchedule(OrganizationId organizationId, PublicTransportScheduleId scheduleId) {
    for (PublicTransportProvider publicTransportProvider : getPublicTransportProviders()) {
      Schedule schedule = publicTransportProvider.findSchedule(organizationId, scheduleId);
      if (schedule != null) {
        return schedule;
      }
    }
    
    return null;
  }
  
  private List<PublicTransportProvider> getPublicTransportProviders() {
    List<PublicTransportProvider> result = new ArrayList<>();
    
    Iterator<PublicTransportProvider> iterator = publicTransportProviders.iterator();
    while (iterator.hasNext()) {
      result.add(iterator.next());
    }
    
    return Collections.unmodifiableList(result);
  }
  
}
