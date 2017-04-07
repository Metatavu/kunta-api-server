package fi.otavanopisto.kuntaapi.server.integrations.management;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.id.AbstractIdFactory;

@ApplicationScoped
public class ManagementIdFactory extends AbstractIdFactory {
  
  @Override
  public String getSource() {
    return ManagementConsts.IDENTIFIER_NAME;
  }
  
}
