package fi.otavanopisto.kuntaapi.server.integrations.ptv.resources;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.tasks.AbstractTaskQueue;

@ApplicationScoped
public class PtvCodeListTaskQueue extends AbstractTaskQueue<PtvCodeListTask> {

  @Override
  public String getName() {
    return "ptv-code-entities";
  }
  
}