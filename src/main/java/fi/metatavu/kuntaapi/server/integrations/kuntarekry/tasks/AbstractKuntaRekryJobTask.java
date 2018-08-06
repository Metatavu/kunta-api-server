package fi.metatavu.kuntaapi.server.integrations.kuntarekry.tasks;

import fi.metatavu.metaflow.tasks.impl.DefaultTaskImpl;

public abstract class AbstractKuntaRekryJobTask extends DefaultTaskImpl {

private static final long serialVersionUID = -1297045522910954754L;

  public AbstractKuntaRekryJobTask() {
    super();
  }

  public AbstractKuntaRekryJobTask(String uniqueId, boolean priority) {
   super(uniqueId, priority);
  }

}
