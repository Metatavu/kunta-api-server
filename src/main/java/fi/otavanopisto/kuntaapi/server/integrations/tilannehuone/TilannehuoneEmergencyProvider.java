package fi.otavanopisto.kuntaapi.server.integrations.tilannehuone;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import fi.metatavu.kuntaapi.server.rest.model.Emergency;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierRelationController;
import fi.otavanopisto.kuntaapi.server.id.EmergencyId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.EmergencyProvider;
import fi.otavanopisto.kuntaapi.server.integrations.tilannehuone.resources.TilannehuoneEmergencyResourceContainer;

/**
 * Emergency provider for tilannehuone
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class TilannehuoneEmergencyProvider implements EmergencyProvider {
  
  @Inject
  private IdentifierRelationController identifierRelationController;
  
  @Inject
  private TilannehuoneEmergencyResourceContainer tilannehuoneEmergencyResourceContainer;
  
  @Override
  public Emergency findOrganizationEmergency(OrganizationId organizationId, EmergencyId emergencyId) {
    if (identifierRelationController.isChildOf(organizationId, emergencyId)) {
      return tilannehuoneEmergencyResourceContainer.get(emergencyId);
    }
    
    return null;
  }
  
  @Override
  public List<Emergency> listOrganizationEmergencies(OrganizationId organizationId, String location, OffsetDateTime before, OffsetDateTime after) {
    List<EmergencyId> emergencyIds = identifierRelationController.listEmergencyIdsBySourceAndParentId(TilannehuoneConsts.IDENTIFIER_NAME, organizationId);
    List<Emergency> emergencies = new ArrayList<>(emergencyIds.size());
    
    for (EmergencyId emergencyId : emergencyIds) {
      Emergency emergency = tilannehuoneEmergencyResourceContainer.get(emergencyId);
      if (emergency != null && isAcceptable(emergency, location, before, after)) {
        emergencies.add(emergency);
      }
    }
    
    return emergencies;
  }

  private boolean isAcceptable(Emergency emergency, String location, OffsetDateTime before, OffsetDateTime after) {
    if (before != null && (emergency.getTime() == null || before.isBefore(emergency.getTime()))) {
      return false;
    }

    if (after != null && (emergency.getTime() == null || after.isAfter(emergency.getTime()))) {
      return false;
    }
    
    return (location == null) || StringUtils.equals(location, emergency.getLocation());
  }

}
