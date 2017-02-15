package fi.otavanopisto.kuntaapi.server.integrations.gtfs;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.rest.model.Agency;
import fi.otavanopisto.kuntaapi.server.id.PublicTransportAgencyId;

@ApplicationScoped
public class GtfsTranslator {

  public Agency translateAgengy(PublicTransportAgencyId kuntaApiId, org.onebusaway.gtfs.model.Agency agency) {
    Agency result = new Agency();
    result.setId(kuntaApiId.getId());
    result.setName(agency.getName());
    result.setUrl(agency.getUrl());
    result.setTimezone(agency.getTimezone());
    return result;
  }
  
}
