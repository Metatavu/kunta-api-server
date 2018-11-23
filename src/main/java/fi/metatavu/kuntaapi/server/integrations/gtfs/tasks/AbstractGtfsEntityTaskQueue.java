package fi.metatavu.kuntaapi.server.integrations.gtfs.tasks;

import fi.metatavu.kuntaapi.server.integrations.gtfs.GtfsConsts;
import fi.metatavu.kuntaapi.server.tasks.metaflow.AbstractKuntaApiTaskQueue;

public abstract class AbstractGtfsEntityTaskQueue <T extends AbstractGtfsEntityTask<?>> extends AbstractKuntaApiTaskQueue<T> {

  public abstract String getEntityType();
  
  @Override
  public String getName() {
    return String.format("%s-%s", GtfsConsts.IDENTIFIER_NAME, getEntityType());
  }
  
}
