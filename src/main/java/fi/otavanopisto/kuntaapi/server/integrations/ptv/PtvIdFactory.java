package fi.otavanopisto.kuntaapi.server.integrations.ptv;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.id.AbstractIdFactory;

@ApplicationScoped
public class PtvIdFactory extends AbstractIdFactory {
  
  @Override
  public String getSource() {
    return PtvConsts.IDENTIFIER_NAME;
  }
  
}
