package fi.otavanopisto.kuntaapi.server.integrations.gtfs;

import java.util.List;

import fi.metatavu.kuntaapi.server.rest.model.Agency;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.PublicTranportProvider;

public class GtfsPublicTransportProvider implements PublicTranportProvider {

  @Override
  public List<Agency> listAgencies(OrganizationId organizationId) {
    // TODO Auto-generated method stub
    return null;
  }

}
