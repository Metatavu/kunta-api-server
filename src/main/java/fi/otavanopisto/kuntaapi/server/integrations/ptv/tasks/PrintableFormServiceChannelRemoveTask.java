package fi.otavanopisto.kuntaapi.server.integrations.ptv.tasks;

import fi.otavanopisto.kuntaapi.server.id.PrintableFormServiceChannelId;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask.Operation;

public class PrintableFormServiceChannelRemoveTask extends AbstractServiceChannelTask {

  private static final long serialVersionUID = -6265070352484262510L;

  private PrintableFormServiceChannelId printableFormServiceChannelId;

  public PrintableFormServiceChannelRemoveTask() {
    // Zero-argument constructor
  }

  public PrintableFormServiceChannelRemoveTask(PrintableFormServiceChannelId printableFormServiceChannelId) {
    super(Operation.REMOVE);
    this.printableFormServiceChannelId = printableFormServiceChannelId;
  }
  
  public PrintableFormServiceChannelId getPrintableFormServiceChannelId() {
    return printableFormServiceChannelId;
  }
  
  public void setPrintableFormServiceChannelId(PrintableFormServiceChannelId printableFormServiceChannelId) {
    this.printableFormServiceChannelId = printableFormServiceChannelId;
  }

  @Override
  public String getUniqueId() {
    return String.format("ptv-printable-form-service-channel-remove-task-%s", printableFormServiceChannelId.toString());
  }
  
}
