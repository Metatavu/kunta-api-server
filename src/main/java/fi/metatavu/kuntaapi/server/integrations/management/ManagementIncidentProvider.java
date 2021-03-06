package fi.metatavu.kuntaapi.server.integrations.management;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import fi.metatavu.kuntaapi.server.rest.model.Incident;
import fi.metatavu.kuntaapi.server.controllers.IdentifierRelationController;
import fi.metatavu.kuntaapi.server.id.IncidentId;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.integrations.IncidentProvider;
import fi.metatavu.kuntaapi.server.integrations.management.resources.ManagementIncidentResourceContainer;
import javax.enterprise.context.ApplicationScoped;

/**
 * Incident provider for management wordpress
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
@SuppressWarnings ("squid:S3306")
public class ManagementIncidentProvider extends AbstractManagementProvider implements IncidentProvider {
  
  @Inject
  private ManagementIncidentResourceContainer managementIncidentCache;
  
  @Inject
  private IdentifierRelationController identifierRelationController;
  
  @Override
  public List<Incident> listOrganizationIncidents(OrganizationId organizationId, String slug, OffsetDateTime startBefore, OffsetDateTime endAfter) {
    List<IncidentId> incidentIds = identifierRelationController.listIncidentIdsBySourceAndParentId(ManagementConsts.IDENTIFIER_NAME, organizationId);
    List<Incident> incidents = new ArrayList<>(incidentIds.size());
    
    for (IncidentId incidentId : incidentIds) {
      Incident incident = managementIncidentCache.get(incidentId);
      if (incident != null && isAcceptable(incident, slug, startBefore, endAfter)) {
        incidents.add(incident);
      }
    }
    
    return incidents;
  }

  @Override
  public Incident findOrganizationIncident(OrganizationId organizationId, IncidentId incidentId) {
    if (identifierRelationController.isChildOf(organizationId, incidentId)) {
      return managementIncidentCache.get(incidentId);
    }
    
    return null;
  }

  private boolean isAcceptable(Incident incident, String slug, OffsetDateTime startBefore, OffsetDateTime endAfter) {
    if (startBefore != null && (incident.getStart() == null || startBefore.isBefore(incident.getStart()))) {
      return false;
    }

    if (endAfter != null && (incident.getEnd() == null || endAfter.isAfter(incident.getEnd()))) {
      return false;
    }
    
    return (slug == null) || StringUtils.equals(slug, incident.getSlug());
  }
  
}
