package fi.otavanopisto.kuntaapi.server.integrations.ptv.tasks;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.integrations.ptv.PtvConsts;
import fi.otavanopisto.kuntaapi.server.tasks.AbstractServiceEntityTaskQueue;

@ApplicationScoped
public class ServiceLocationChannelsTaskQueue extends AbstractServiceEntityTaskQueue {

  @Override
  public String getSource() {
    return PtvConsts.IDENTIFIER_NAME;
  }

  @Override
  public String getEntity() {
    return "location-channels";
  }
  
}