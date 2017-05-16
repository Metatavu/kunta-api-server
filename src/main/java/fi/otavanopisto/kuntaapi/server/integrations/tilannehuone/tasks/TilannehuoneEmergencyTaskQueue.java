package fi.otavanopisto.kuntaapi.server.integrations.tilannehuone.tasks;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.tasks.AbstractTaskQueue;

@ApplicationScoped
public class TilannehuoneEmergencyTaskQueue extends AbstractTaskQueue<TilannehuoneEmergencyEntityTask> {

  @Override
  public String getName() {
    return "tilannehuone-emergency-entities";
  }
  
}