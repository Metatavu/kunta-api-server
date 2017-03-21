package fi.otavanopisto.kuntaapi.server.integrations.ptv.tasks;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.id.WebPageServiceChannelId;
import fi.otavanopisto.kuntaapi.server.id.IdType;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.PtvConsts;
import fi.otavanopisto.kuntaapi.server.tasks.AbstractIdTaskQueue;

@ApplicationScoped
public class WebPageServiceChannelIdTaskQueue extends AbstractIdTaskQueue<WebPageServiceChannelId> {

  @Override
  public String getSource() {
    return PtvConsts.IDENTIFIER_NAME;
  }
  
  @Override
  public IdType getType() {
    return IdType.WEBPAGE_SERVICE_CHANNEL;
  }
  
}