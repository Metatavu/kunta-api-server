package fi.otavanopisto.kuntaapi.server.integrations;

import java.time.OffsetDateTime;
import java.util.List;

import fi.otavanopisto.kuntaapi.server.id.EmergencyId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.rest.model.Emergency;

/**
 * Interface that describes a single emergency provider
 * 
 * @author Antti Leppä
 */
public interface EmergencyProvider {
  
  /**
   * Finds a single organization emergency
   * 
   * @param organizationId organization id
   * @param emergencyId emergency id
   * @return single organization emergency or null if not found
   */
  public Emergency findOrganizationEmergency(OrganizationId organizationId, EmergencyId emergencyId);

  /**
   * Lists emergencies in an organization
   * 
   * @param organizationId organization id
   * @param slug slug
   * @param endAfter 
   * @param startBefore 
   * @return organization emergencies
   */
  public List<Emergency> listOrganizationEmergencies(OrganizationId organizationId, String location, OffsetDateTime before, OffsetDateTime after);
  
}