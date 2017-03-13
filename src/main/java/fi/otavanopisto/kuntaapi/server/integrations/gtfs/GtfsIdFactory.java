package fi.otavanopisto.kuntaapi.server.integrations.gtfs;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.id.AbstractIdFactory;

@ApplicationScoped
public class GtfsIdFactory extends AbstractIdFactory {
  
  @Override
  public String getSource() {
    return GtfsConsts.IDENTIFIER_NAME;
  }
  
}
