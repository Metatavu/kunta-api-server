package fi.metatavu.kuntaapi.server.resources;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.rest.model.PhoneServiceChannel;
import fi.metatavu.kuntaapi.server.id.PhoneServiceChannelId;

@ApplicationScoped
public abstract class AbstractPhoneServiceChannelResourceContainer extends AbstractResourceContainer<PhoneServiceChannelId, PhoneServiceChannel> {

  private static final long serialVersionUID = 3890539107234201153L;

  @Override
  public String getEntityType() {
    return "resource";
  }
  
}