package fi.metatavu.kuntaapi.server.integrations.ptv.tasks;

import fi.metatavu.kuntaapi.server.id.PhoneServiceChannelId;
import fi.metatavu.kuntaapi.server.tasks.IdTask.Operation;

public class PhoneServiceChannelRemoveTask extends AbstractServiceChannelTask {

  private static final long serialVersionUID = 2308760674153932715L;

  private PhoneServiceChannelId phoneServiceChannelId;

  public PhoneServiceChannelRemoveTask() {
    // Zero-argument constructor
  }

  public PhoneServiceChannelRemoveTask(boolean priority, PhoneServiceChannelId phoneServiceChannelId) {
    super(String.format("ptv-phone-service-channel-remove-task-%s", phoneServiceChannelId.toString()), priority, Operation.REMOVE);
    this.phoneServiceChannelId = phoneServiceChannelId;
  }
  
  public PhoneServiceChannelId getPhoneServiceChannelId() {
    return phoneServiceChannelId;
  }
  
  public void setPhoneServiceChannelId(PhoneServiceChannelId phoneServiceChannelId) {
    this.phoneServiceChannelId = phoneServiceChannelId;
  }


}
