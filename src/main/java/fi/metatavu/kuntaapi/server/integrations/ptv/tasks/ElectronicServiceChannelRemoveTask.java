package fi.metatavu.kuntaapi.server.integrations.ptv.tasks;

import fi.metatavu.kuntaapi.server.id.ElectronicServiceChannelId;
import fi.metatavu.kuntaapi.server.tasks.IdTask.Operation;

public class ElectronicServiceChannelRemoveTask extends AbstractServiceChannelTask {

  private static final long serialVersionUID = -245199841957177005L;

  private ElectronicServiceChannelId electronicServiceChannelId;

  public ElectronicServiceChannelRemoveTask() {
    // Zero-argument constructor
  }

  public ElectronicServiceChannelRemoveTask(boolean priority, ElectronicServiceChannelId electronicServiceChannelId) {
    super(String.format("ptv-electronic-service-channel-remove-task-%s", electronicServiceChannelId.toString()), priority, Operation.REMOVE);
    this.electronicServiceChannelId = electronicServiceChannelId;
  }
  
  public ElectronicServiceChannelId getElectronicServiceChannelId() {
    return electronicServiceChannelId;
  }
  
  public void setElectronicServiceChannelId(ElectronicServiceChannelId electronicServiceChannelId) {
    this.electronicServiceChannelId = electronicServiceChannelId;
  }
  
}
