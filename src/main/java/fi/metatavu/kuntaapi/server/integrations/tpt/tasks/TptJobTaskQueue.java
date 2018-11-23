package fi.metatavu.kuntaapi.server.integrations.tpt.tasks;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.tasks.metaflow.AbstractKuntaApiTaskQueue;

/**
 * Task queue for running te-palvelut.fi -integration job tasks
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class TptJobTaskQueue extends AbstractKuntaApiTaskQueue<TptAbstractJobTask> {

  @Override
  public String getName() {
    return "tpt-jobs";
  }
  
}