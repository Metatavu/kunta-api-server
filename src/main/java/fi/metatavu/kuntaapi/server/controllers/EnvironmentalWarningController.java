package fi.metatavu.kuntaapi.server.controllers;

import java.time.OffsetDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.logmanager.Level;

import fi.metatavu.kuntaapi.server.id.EnvironmentalWarningId;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.index.SearchResult;
import fi.metatavu.kuntaapi.server.index.search.EnvironmentalWarningSearcher;
import fi.metatavu.kuntaapi.server.integrations.EnvironmentalWarningProvider;
import fi.metatavu.kuntaapi.server.integrations.EnvironmentalWarningSortBy;
import fi.metatavu.kuntaapi.server.integrations.SortDir;
import fi.metatavu.kuntaapi.server.rest.model.EnvironmentalWarning;

/**
 * Controller for environmental warnings
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class EnvironmentalWarningController {

  @Inject
  private Logger logger;

  @Inject
  private EnvironmentalWarningSearcher environmentalWarningSearcher;

  @Inject
  private EnvironmentalWarningProvider environmentalWarningProvider;

  /**
   * Finds an environmental warning by organization id and environmental warning id
   * 
   * @param organizationId organization id
   * @param environmentalWarningId environmental warning id
   * @return found environmental warning or null if not found
   */
  public EnvironmentalWarning findEnvironmentalWarning(OrganizationId organizationId, EnvironmentalWarningId environmentalWarningId) {
    return environmentalWarningProvider.findEnvironmentalWarning(organizationId, environmentalWarningId);
  }
  
  /**
   * Search for environmental warnings
   * 
   * @param organizationId organization id. Id source must be Kunta API
   * @param contexts filter by context. Optional
   * @param startBefore include only warnings starting before specified time. Optional
   * @param startAfter include only warnings starting after specified time. Optional
   * @param sortBy sort by
   * @param sortDir sort direction
   * @param firstResult first result
   * @param maxResults max results
   * @return search result
   */
  public SearchResult<EnvironmentalWarning> searchEnvironmentalWarnings(OrganizationId organizationId, Collection<String> contexts, OffsetDateTime startBefore, OffsetDateTime startAfter, EnvironmentalWarningSortBy sortBy, SortDir sortDir, Integer firstResult, Integer maxResults) { 
    SearchResult<EnvironmentalWarningId> searchResult = environmentalWarningSearcher.searchEnvironmentalWarning(organizationId.getId(), 
        contexts, 
        startBefore,
        startAfter,
        sortBy,
        sortDir, 
        firstResult, 
        maxResults);
    
    if (searchResult != null) {
      List<EnvironmentalWarning> result = searchResult.getResult().stream()
        .map(environmentalWarningId -> findEnvironmentalWarning(organizationId, environmentalWarningId) )
        .filter(Objects::nonNull)
        .collect(Collectors.toList());
      return new SearchResult<>(result, searchResult.getTotalHits());
    } else {
      logger.log(Level.SEVERE, "Failed to execute environmental warnings search");
    }
    
    return SearchResult.emptyResult();
  }

}
