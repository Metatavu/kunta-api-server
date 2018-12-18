package fi.metatavu.kuntaapi.server.integrations.ptv.tasks;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.id.IdType;
import fi.metatavu.kuntaapi.server.id.ServiceLocationServiceChannelId;
import fi.metatavu.kuntaapi.server.integrations.ptv.PtvConsts;
import fi.metatavu.kuntaapi.server.tasks.metaflow.AbstractIdTaskQueue;

@ApplicationScoped
public class ServiceLocationServiceChannelIdTaskQueue extends AbstractIdTaskQueue<ServiceLocationServiceChannelId> {

  @Override
  public String getSource() {
    return PtvConsts.IDENTIFIER_NAME;
  }
  
  @Override
  public IdType getType() {
    return IdType.SERVICE_LOCATION_SERVICE_CHANNEL;
  }
  
}