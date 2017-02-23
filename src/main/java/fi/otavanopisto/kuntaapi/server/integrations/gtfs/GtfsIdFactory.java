package fi.otavanopisto.kuntaapi.server.integrations.gtfs;

import fi.otavanopisto.kuntaapi.server.id.AbstractIdFactory;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PublicTransportAgencyId;
import fi.otavanopisto.kuntaapi.server.id.PublicTransportRouteId;
import fi.otavanopisto.kuntaapi.server.id.PublicTransportScheduleId;
import fi.otavanopisto.kuntaapi.server.id.PublicTransportStopId;
import fi.otavanopisto.kuntaapi.server.id.PublicTransportStopTimeId;
import fi.otavanopisto.kuntaapi.server.id.PublicTransportTripId;

public class GtfsIdFactory extends AbstractIdFactory {
  
  public PublicTransportAgencyId createAgencyId(OrganizationId kuntaApiOrganizationId, String gtfsAgencyId) {
    return createId(PublicTransportAgencyId.class, kuntaApiOrganizationId, GtfsConsts.IDENTIFIER_NAME, gtfsAgencyId);
  }

  public PublicTransportScheduleId createScheduleId(OrganizationId kuntaApiOrganizationId, String gtfsScheduleId) {
    return createId(PublicTransportScheduleId.class, kuntaApiOrganizationId, GtfsConsts.IDENTIFIER_NAME, gtfsScheduleId);
  }

  public PublicTransportRouteId createRouteId(OrganizationId kuntaApiOrganizationId, String gtfsRouteId) {
    return createId(PublicTransportRouteId.class, kuntaApiOrganizationId, GtfsConsts.IDENTIFIER_NAME, gtfsRouteId);
  }
  
  public PublicTransportStopId createStopId(OrganizationId kuntaApiOrganizationId, String gtfsStopId) {
    return createId(PublicTransportStopId.class, kuntaApiOrganizationId, GtfsConsts.IDENTIFIER_NAME, gtfsStopId);
  }
  
  public PublicTransportStopTimeId createStopTimeId(OrganizationId kuntaApiOrganizationId, String gtfsStopTimeId) {
    return createId(PublicTransportStopTimeId.class, kuntaApiOrganizationId, GtfsConsts.IDENTIFIER_NAME, gtfsStopTimeId);
  }
    
  public PublicTransportTripId createTripId(OrganizationId kuntaApiOrganizationId, String gtfsTripId) {
    return createId(PublicTransportTripId.class, kuntaApiOrganizationId, GtfsConsts.IDENTIFIER_NAME, gtfsTripId);
  }
  
}
