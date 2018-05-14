package fi.otavanopisto.kuntaapi.server.controllers;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.rest.model.Emergency;
import fi.otavanopisto.kuntaapi.server.id.EmergencyId;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.index.SearchResult;
import fi.otavanopisto.kuntaapi.server.index.search.EmergencySearcher;
import fi.otavanopisto.kuntaapi.server.integrations.EmergencyProvider;
import fi.otavanopisto.kuntaapi.server.integrations.EmergencySortBy;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.integrations.SortDir;

@ApplicationScoped
@SuppressWarnings ({"squid:S3306","squid:S00107"})
public class EmergencyController {
  
  @Inject
  private Logger logger;

  @Inject
  private EmergencySearcher emergencySearcher;
  
  @Inject
  private IdController idController;
  
  @Inject
  private Instance<EmergencyProvider> emergencyProviders;
  
  /**
   * Search emergencies. 
   * 
   * @param organizationId organization id
   * @param location emergency location
   * @param before filter by time before. Optional
   * @param after filter by time after. Optional
   * @param sortBy sort by
   * @param sortDir sort direction
   * @param firstResult first result index
   * @param maxResults max results
   * @return result
   */
  public SearchResult<Emergency> searchEmergencies(OrganizationId organizationId, String location, OffsetDateTime before, OffsetDateTime after, EmergencySortBy sortBy, SortDir sortDir, Integer firstResult, Integer maxResults) {
    OrganizationId kuntaApiOrganizationId = idController.translateOrganizationId(organizationId, KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiOrganizationId == null) {
      logger.severe(() -> String.format("Failed to translate organization %s into Kunta API id", organizationId.toString()));
      return SearchResult.emptyResult();
    }
    
    SearchResult<EmergencyId> searchResult = emergencySearcher.searchEmergencies(kuntaApiOrganizationId.getId(), null, location, before, after, sortBy, sortDir, firstResult, maxResults);
    if (searchResult != null) {
      List<Emergency> result = searchResult.getResult().stream().map((emergencyId) -> {
        return findEmergency(kuntaApiOrganizationId, emergencyId);
      }).collect(Collectors.toList());

      return new SearchResult<>(result, searchResult.getTotalHits());
    }
    
    return SearchResult.emptyResult();
  }

  /**
   * Finds an emergency
   * 
   * @param organizationId organization id
   * @param emergencyId emergency id
   * @return found emergency or null if not found
   */
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

}
