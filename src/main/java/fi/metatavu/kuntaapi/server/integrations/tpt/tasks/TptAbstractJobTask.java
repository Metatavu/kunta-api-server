package fi.metatavu.kuntaapi.server.integrations.tpt.tasks;

import fi.metatavu.metaflow.tasks.impl.DefaultTaskImpl;

public abstract class TptAbstractJobTask extends DefaultTaskImpl {

  private static final long serialVersionUID = 5620529319670316500L;

  public TptAbstractJobTask() {
    super();
  }

  public TptAbstractJobTask(String uniqueId, boolean priority) {
    super(uniqueId, priority);
  }

}
