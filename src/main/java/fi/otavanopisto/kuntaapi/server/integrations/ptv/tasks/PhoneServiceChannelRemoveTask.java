package fi.otavanopisto.kuntaapi.server.integrations.ptv.tasks;

import fi.otavanopisto.kuntaapi.server.id.PhoneServiceChannelId;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask.Operation;

public class PhoneServiceChannelRemoveTask extends AbstractServiceChannelTask {

  private static final long serialVersionUID = 2308760674153932715L;

  private PhoneServiceChannelId phoneServiceChannelId;

  public PhoneServiceChannelRemoveTask() {
    // Zero-argument constructor
  }

  public PhoneServiceChannelRemoveTask(PhoneServiceChannelId phoneServiceChannelId) {
    super(Operation.REMOVE);
    this.phoneServiceChannelId = phoneServiceChannelId;
  }
  
  public PhoneServiceChannelId getPhoneServiceChannelId() {
    return phoneServiceChannelId;
  }
  
  public void setPhoneServiceChannelId(PhoneServiceChannelId phoneServiceChannelId) {
    this.phoneServiceChannelId = phoneServiceChannelId;
  }
  
}
