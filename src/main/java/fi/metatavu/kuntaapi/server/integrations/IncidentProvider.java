package fi.metatavu.kuntaapi.server.integrations;

import java.time.OffsetDateTime;
import java.util.List;

import fi.metatavu.kuntaapi.server.id.IncidentId;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.rest.model.Incident;

/**
 * Interface that describes a single incident provider
 * 
 * @author Antti Leppä
 */
public interface IncidentProvider {
  
  /**
   * Finds a single organization incident
   * 
   * @param organizationId organization id
   * @param incidentId incident id
   * @return single organization incident or null if not found
   */
  public Incident findOrganizationIncident(OrganizationId organizationId, IncidentId incidentId);

  /**
   * Lists incidents in an organization
   * 
   * @param organizationId organization id
   * @param slug slug
   * @param endAfter 
   * @param startBefore 
   * @return organization incidents
   */
  public List<Incident> listOrganizationIncidents(OrganizationId organizationId, String slug, OffsetDateTime startBefore, OffsetDateTime endAfter);
  
}