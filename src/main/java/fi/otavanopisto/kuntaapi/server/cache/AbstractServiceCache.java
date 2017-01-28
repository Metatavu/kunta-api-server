package fi.otavanopisto.kuntaapi.server.cache;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.rest.model.Service;
import fi.otavanopisto.kuntaapi.server.id.ServiceId;

@ApplicationScoped
public abstract class AbstractServiceCache extends AbstractEntityCache<ServiceId, Service> {

  private static final long serialVersionUID = 1893149790953443553L;
  
}