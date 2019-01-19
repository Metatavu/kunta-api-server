package fi.metatavu.kuntaapi.server.integrations;

import fi.metatavu.kuntaapi.server.id.EnvironmentalWarningId;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.rest.model.EnvironmentalWarning;

/**
 * Interface that describes an environmental warning provider
 * 
 * @author Antti Leppä
 */
public interface EnvironmentalWarningProvider {

  /**
   * Finds a single environmental warning
   * 
   * @param organizationId organization id
   * @param environmentalWarningId environmental warning id
   * @return found environmental warning or null if not found
   */
  public EnvironmentalWarning findEnvironmentalWarning(OrganizationId organizationId, EnvironmentalWarningId environmentalWarningId);
  
}
