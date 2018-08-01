package fi.metatavu.kuntaapi.server.integrations.tilannehuone;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.id.AbstractIdFactory;

@ApplicationScoped
public class TilannehuoneIdFactory extends AbstractIdFactory {
  
  @Override
  public String getSource() {
    return TilannehuoneConsts.IDENTIFIER_NAME;
  }
  
}
