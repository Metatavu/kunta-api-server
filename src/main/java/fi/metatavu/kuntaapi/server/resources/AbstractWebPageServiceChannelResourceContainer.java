package fi.metatavu.kuntaapi.server.resources;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.rest.model.WebPageServiceChannel;
import fi.metatavu.kuntaapi.server.id.WebPageServiceChannelId;

@ApplicationScoped
public abstract class AbstractWebPageServiceChannelResourceContainer extends AbstractResourceContainer<WebPageServiceChannelId, WebPageServiceChannel> {

  private static final long serialVersionUID = 707081793285628885L;

  @Override
  public String getEntityType() {
    return "resource";
  }
  
}