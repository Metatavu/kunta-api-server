package fi.metatavu.kuntaapi.server.resources;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.rest.model.ElectronicServiceChannel;
import fi.metatavu.kuntaapi.server.id.ElectronicServiceChannelId;

@ApplicationScoped
public abstract class AbstractElectronicServiceChannelResourceContainer extends AbstractResourceContainer<ElectronicServiceChannelId, ElectronicServiceChannel> {

  private static final long serialVersionUID = 6370820961793708603L;

  @Override
  public String getEntityType() {
    return "resource";
  }
  
}