package fi.otavanopisto.kuntaapi.server.integrations.gtfs;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.rest.model.Agency;
import fi.metatavu.kuntaapi.server.rest.model.Schedule;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierRelationController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PublicTransportAgencyId;
import fi.otavanopisto.kuntaapi.server.id.PublicTransportScheduleId;
import fi.otavanopisto.kuntaapi.server.integrations.PublicTransportProvider;
import fi.otavanopisto.kuntaapi.server.integrations.gtfs.cache.GtfsPublicTransportAgencyCache;
import fi.otavanopisto.kuntaapi.server.integrations.gtfs.cache.GtfsPublicTransportScheduleCache;

public class GtfsPublicTransportProvider implements PublicTransportProvider {
  
  @Inject
  private IdentifierRelationController identifierRelationController;
  
  @Inject
  private GtfsPublicTransportAgencyCache gtfsPublicTransportAgencyCache;
  
  @Inject
  private GtfsPublicTransportScheduleCache gtfsPublicTransportScheduleCache;
  
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

  @Override
  public List<Schedule> listSchedules(OrganizationId organizationId) {
    List<PublicTransportScheduleId> scheduleIds = identifierRelationController.listPublicTransportScheduleIdsBySourceAndParentId(GtfsConsts.IDENTIFIER_NAME, organizationId);
    List<Schedule> schedules = new ArrayList<>(scheduleIds.size());
    
    for (PublicTransportScheduleId scheduleId : scheduleIds) {
      Schedule schedule = gtfsPublicTransportScheduleCache.get(scheduleId);
      if (schedule != null) {
        schedules.add(schedule);
      }
    }
    
    return schedules;
  }

  @Override
  public Schedule findSchedule(OrganizationId organizationId, PublicTransportScheduleId scheduleId) {
    if (!identifierRelationController.isChildOf(organizationId, scheduleId)) {
      return null;
    }
    
    return gtfsPublicTransportScheduleCache.get(scheduleId);
  }

}
