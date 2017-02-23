package fi.otavanopisto.kuntaapi.server.tasks;

import javax.enterprise.event.Observes;

import org.apache.commons.lang3.StringUtils;

import fi.otavanopisto.kuntaapi.server.id.BaseId;
import fi.otavanopisto.kuntaapi.server.id.IdType;

/**
 * Abstract base class for id tasks
 * 
 * @author Antti Leppä
 *
 * @param <I> id type
 */
public abstract class AbstractIdTaskQueue<I extends BaseId> extends AbstractTaskQueue<IdTask<I>> {
 
  /**
   * Returns type of queue ids
   * 
   * @return type of queue ids
   */
  public abstract IdType getType();
  
  /**
   * Returns source of queue ids
   * 
   * @return source of queue ids
   */
  public abstract String getSource();
  
  @Override
  public String getName() {
    return String.format("%s-%s-ids", getSource(), getType().name());
  }
  
  @SuppressWarnings("unchecked")
  public void onTaskRequest(@Observes TaskRequest request) {
    if (request.getTask() instanceof IdTask) {
      IdTask<?> task = (IdTask<?>) request.getTask();
      if (isAccetableId(task.getId())) {
        enqueueTask(request.isPriority(), (IdTask<I>) task);
      }
    }
  }

  @SuppressWarnings ("squid:UnusedPrivateMethod")
  private boolean isAccetableId(BaseId id) {
    return id.getType() == getType() && StringUtils.equals(id.getSource(), getSource());
  }
  
}
