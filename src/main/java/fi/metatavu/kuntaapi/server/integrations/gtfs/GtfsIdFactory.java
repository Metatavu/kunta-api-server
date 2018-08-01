package fi.metatavu.kuntaapi.server.integrations.gtfs;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.id.AbstractIdFactory;

@ApplicationScoped
public class GtfsIdFactory extends AbstractIdFactory {
  
  @Override
  public String getSource() {
    return GtfsConsts.IDENTIFIER_NAME;
  }
  
}
