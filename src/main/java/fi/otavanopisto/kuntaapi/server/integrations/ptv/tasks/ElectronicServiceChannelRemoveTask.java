package fi.otavanopisto.kuntaapi.server.integrations.ptv.tasks;

import fi.otavanopisto.kuntaapi.server.id.ElectronicServiceChannelId;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask.Operation;

public class ElectronicServiceChannelRemoveTask extends AbstractServiceChannelTask {

  private static final long serialVersionUID = -245199841957177005L;

  private ElectronicServiceChannelId electronicServiceChannelId;

  public ElectronicServiceChannelRemoveTask() {
    // Zero-argument constructor
  }

  public ElectronicServiceChannelRemoveTask(ElectronicServiceChannelId electronicServiceChannelId) {
    super(Operation.REMOVE);
    this.electronicServiceChannelId = electronicServiceChannelId;
  }
  
  public ElectronicServiceChannelId getElectronicServiceChannelId() {
    return electronicServiceChannelId;
  }
  
  public void setElectronicServiceChannelId(ElectronicServiceChannelId electronicServiceChannelId) {
    this.electronicServiceChannelId = electronicServiceChannelId;
  }
  
  @Override
  public String getUniqueId() {
    return String.format("ptv-electronic-service-channel-remove-task-%s", getElectronicServiceChannelId().toString());
  }
  
}
