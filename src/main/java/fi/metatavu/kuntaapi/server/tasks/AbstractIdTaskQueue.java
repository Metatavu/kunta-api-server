package fi.metatavu.kuntaapi.server.tasks;

import fi.metatavu.kuntaapi.server.id.BaseId;
import fi.metatavu.kuntaapi.server.id.IdType;

/**
 * Abstract base class for id tasks
 * 
 * @author Antti Leppä
 *
 * @param <I> id type
 */
public abstract class AbstractIdTaskQueue<I extends BaseId> extends AbstractKuntaApiTaskQueue<IdTask<I>> {
 
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
  
}