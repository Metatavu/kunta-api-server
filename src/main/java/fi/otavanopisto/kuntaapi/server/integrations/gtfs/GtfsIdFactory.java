package fi.otavanopisto.kuntaapi.server.integrations.gtfs;

import fi.otavanopisto.kuntaapi.server.id.AbstractIdFactory;

public class GtfsIdFactory extends AbstractIdFactory {
  
  @Override
  public String getSource() {
    return GtfsConsts.IDENTIFIER_NAME;
  }
  
}
