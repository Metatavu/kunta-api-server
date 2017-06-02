package fi.otavanopisto.kuntaapi.server.integrations.linkedevents;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.id.AbstractIdFactory;

@ApplicationScoped
public class LinkedEventsIdFactory extends AbstractIdFactory {
  
  @Override
  public String getSource() {
    return LinkedEventsConsts.IDENTIFIER_NAME;
  }
  
}