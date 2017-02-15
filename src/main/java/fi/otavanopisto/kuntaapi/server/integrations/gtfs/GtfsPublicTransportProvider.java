package fi.otavanopisto.kuntaapi.server.integrations.gtfs;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.rest.model.Agency;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierRelationController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PublicTransportAgencyId;
import fi.otavanopisto.kuntaapi.server.integrations.PublicTransportProvider;
import fi.otavanopisto.kuntaapi.server.integrations.gtfs.cache.GtfsPublicTransportAgencyCache;

public class GtfsPublicTransportProvider implements PublicTransportProvider {
  
  @Inject
  private IdentifierRelationController identifierRelationController;
  
  @Inject
  private GtfsPublicTransportAgencyCache gtfsPublicTransportAgencyCache;
  
  
  @Override
  public List<Agency> listAgencies(OrganizationId organizationId) {
    List<PublicTransportAgencyId> agencyIds = identifierRelationController.listPublicTransportAgencyIdsBySourceAndParentId(GtfsConsts.IDENTIFIER_NAME, organizationId);
    List<Agency> agencies = new ArrayList<>(agencyIds.size());
    
    for (PublicTransportAgencyId agencyId : agencyIds) {
      Agency agency = gtfsPublicTransportAgencyCache.get(agencyId);
      if (agency != null) {
        agencies.add(agency);
      }
    }
    
    return agencies;
  }
  
  @Override
  public Agency findAgency(OrganizationId organizationId, PublicTransportAgencyId agencyId) {
    if (!identifierRelationController.isChildOf(organizationId, agencyId)) {
      return null;
    }
    
    return gtfsPublicTransportAgencyCache.get(agencyId);
  }

}
