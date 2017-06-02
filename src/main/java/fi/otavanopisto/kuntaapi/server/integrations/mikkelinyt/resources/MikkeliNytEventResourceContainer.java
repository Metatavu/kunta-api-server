package fi.otavanopisto.kuntaapi.server.integrations.mikkelinyt.resources;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.resources.AbstractEventResourceContainer;

@ApplicationScoped
public class MikkeliNytEventResourceContainer extends AbstractEventResourceContainer {
  
  private static final long serialVersionUID = -4814807444228342335L;

  @Override
  public String getName() {
    return "events";
  }
  
  @Override
  public String getEntityType() {
    return "resource";
  }

}
