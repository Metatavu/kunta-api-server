package fi.otavanopisto.kuntaapi.server.integrations.ptv.tasks;

import fi.otavanopisto.kuntaapi.server.tasks.AbstractTask;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask.Operation;

public abstract class AbstractServiceChannelTask extends AbstractTask {

  private static final long serialVersionUID = 7658507520622077865L;
  
  private Operation operation;

  public AbstractServiceChannelTask() {
    // Zero-argument constructor
  }

  public AbstractServiceChannelTask(Operation operation) {
    super();
    this.operation = operation;
  }

  public Operation getOperation() {
    return operation;
  }
  
  public void setOperation(Operation operation) {
    this.operation = operation;
  }

}
