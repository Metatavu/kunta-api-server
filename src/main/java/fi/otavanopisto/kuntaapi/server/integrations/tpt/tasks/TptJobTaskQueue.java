package fi.otavanopisto.kuntaapi.server.integrations.tpt.tasks;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.tasks.AbstractTaskQueue;

/**
 * Task queue for running te-palvelut.fi -integration job tasks
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class TptJobTaskQueue extends AbstractTaskQueue<TptAbstractJobTask> {

  @Override
  public String getName() {
    return "tpt-jobs";
  }
  
}