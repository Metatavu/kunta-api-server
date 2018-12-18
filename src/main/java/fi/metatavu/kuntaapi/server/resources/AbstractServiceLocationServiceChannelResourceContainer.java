package fi.metatavu.kuntaapi.server.resources;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.rest.model.ServiceLocationServiceChannel;
import fi.metatavu.kuntaapi.server.id.ServiceLocationServiceChannelId;

@ApplicationScoped
public abstract class AbstractServiceLocationServiceChannelResourceContainer extends AbstractResourceContainer<ServiceLocationServiceChannelId, ServiceLocationServiceChannel> {

  private static final long serialVersionUID = 4095301604637136239L;

  @Override
  public String getEntityType() {
    return "resource";
  }
  
}