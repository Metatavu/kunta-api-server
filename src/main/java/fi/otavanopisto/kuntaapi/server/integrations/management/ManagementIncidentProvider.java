package fi.otavanopisto.kuntaapi.server.integrations.management;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import fi.metatavu.kuntaapi.server.rest.model.Incident;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierRelationController;
import fi.otavanopisto.kuntaapi.server.id.IncidentId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.IncidentProvider;
import fi.otavanopisto.kuntaapi.server.integrations.management.resources.ManagementIncidentResourceContainer;
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
      System.out.println("-------------------------");
      if (incident != null && isAcceptable(incident, slug, startBefore, endAfter)) {
        incidents.add(incident);
      } else {
        System.out.println("No match: " + incident.getId());
      }
      System.out.println("-------------------------");
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
    System.out.println("startBefore: " + startBefore + " istart: " + incident.getStart());
    System.out.println("endAfter: " + endAfter + " iend: " + incident.getEnd());
    
    if (startBefore != null && startBefore.isBefore(incident.getStart())) {
      System.out.println(String.format("%s NOT startBefore: %s ", incident.getStart(), startBefore));
      return false;
    }

    if (endAfter != null && endAfter.isAfter(incident.getEnd())) {
      System.out.println(String.format("%s NOT startBefore: %s ", incident.getEnd(), endAfter));
      return false;
    }
    
    return (slug == null) || StringUtils.equals(slug, incident.getSlug());
  }
  
}
