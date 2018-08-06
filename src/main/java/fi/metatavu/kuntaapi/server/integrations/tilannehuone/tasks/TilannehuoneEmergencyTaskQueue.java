package fi.metatavu.kuntaapi.server.integrations.tilannehuone.tasks;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.tasks.AbstractKuntaApiTaskQueue;

@ApplicationScoped
public class TilannehuoneEmergencyTaskQueue extends AbstractKuntaApiTaskQueue<TilannehuoneEmergencyEntityTask> {

  @Override
  public String getName() {
    return "tilannehuone-emergency-entities";
  }
  
}