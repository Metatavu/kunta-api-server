package fi.otavanopisto.kuntaapi.server.integrations.gtfs.tasks;

import fi.otavanopisto.kuntaapi.server.integrations.gtfs.GtfsConsts;
import fi.otavanopisto.kuntaapi.server.tasks.AbstractTask;
import fi.otavanopisto.kuntaapi.server.tasks.AbstractTaskQueue;

public abstract class AbstractGtfsEntityTaskQueue <T extends AbstractTask> extends AbstractTaskQueue<T> {

  public abstract String getEntityType();
  
  @Override
  public String getName() {
    return String.format("%s-%s", GtfsConsts.IDENTIFIER_NAME, getEntityType());
  }
  
}
