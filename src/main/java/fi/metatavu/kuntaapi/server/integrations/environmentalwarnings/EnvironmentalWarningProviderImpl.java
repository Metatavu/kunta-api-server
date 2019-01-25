package fi.metatavu.kuntaapi.server.integrations.environmentalwarnings;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.controllers.IdentifierRelationController;
import fi.metatavu.kuntaapi.server.id.EnvironmentalWarningId;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.integrations.EnvironmentalWarningProvider;
import fi.metatavu.kuntaapi.server.integrations.environmentalwarnings.resources.EnvironmentalWarningResourceContainer;
import fi.metatavu.kuntaapi.server.rest.model.EnvironmentalWarning;

/**
 * Event provider for environmental warnings
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class EnvironmentalWarningProviderImpl implements EnvironmentalWarningProvider {
  
  @Inject
  private EnvironmentalWarningResourceContainer environmentalWarningResourceContainer;

  @Inject
  private IdentifierRelationController identifierRelationController;

  @Override
  public EnvironmentalWarning findEnvironmentalWarning(OrganizationId organizationId, EnvironmentalWarningId environmentalWarningId) {
    if (!identifierRelationController.isChildOf(organizationId, environmentalWarningId)) {
      return null;
    }
    
    return environmentalWarningResourceContainer.get(environmentalWarningId);
  }
  
}
