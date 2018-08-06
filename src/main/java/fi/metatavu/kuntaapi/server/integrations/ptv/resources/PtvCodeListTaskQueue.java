package fi.metatavu.kuntaapi.server.integrations.ptv.resources;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.tasks.AbstractKuntaApiTaskQueue;

@ApplicationScoped
public class PtvCodeListTaskQueue extends AbstractKuntaApiTaskQueue<PtvCodeListTask> {

  @Override
  public String getName() {
    return "ptv-code-entities";
  }
  
}