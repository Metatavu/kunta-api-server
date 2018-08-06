package fi.metatavu.kuntaapi.server.integrations.ptv.tasks;

import fi.metatavu.kuntaapi.server.id.PrintableFormServiceChannelId;
import fi.metatavu.kuntaapi.server.tasks.IdTask.Operation;

public class PrintableFormServiceChannelRemoveTask extends AbstractServiceChannelTask {

  private static final long serialVersionUID = -6265070352484262510L;

  private PrintableFormServiceChannelId printableFormServiceChannelId;

  public PrintableFormServiceChannelRemoveTask() {
    // Zero-argument constructor
  }

  public PrintableFormServiceChannelRemoveTask(boolean priority, PrintableFormServiceChannelId printableFormServiceChannelId) {
    super(String.format("ptv-printable-form-service-channel-remove-task-%s", printableFormServiceChannelId.toString()),priority, Operation.REMOVE);
    this.printableFormServiceChannelId = printableFormServiceChannelId;
  }
  
  public PrintableFormServiceChannelId getPrintableFormServiceChannelId() {
    return printableFormServiceChannelId;
  }
  
  public void setPrintableFormServiceChannelId(PrintableFormServiceChannelId printableFormServiceChannelId) {
    this.printableFormServiceChannelId = printableFormServiceChannelId;
  }
  
}
