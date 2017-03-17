package fi.otavanopisto.kuntaapi.server.cache;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.rest.model.Service;
import fi.otavanopisto.kuntaapi.server.id.ServiceId;
import fi.otavanopisto.kuntaapi.server.resources.AbstractResourceContainer;

@ApplicationScoped
public abstract class AbstractServiceCache extends AbstractResourceContainer<ServiceId, Service> {

  private static final long serialVersionUID = 1893149790953443553L;

  @Override
  public String getEntityType() {
    return "resource";
  }
  
}