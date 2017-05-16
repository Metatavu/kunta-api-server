package fi.otavanopisto.kuntaapi.server.integrations.tilannehuone.tasks;

import fi.otavanopisto.kuntaapi.server.integrations.tilannehuone.model.Emergency;
import fi.otavanopisto.kuntaapi.server.tasks.AbstractTask;

public class TilannehuoneEmergencyEntityTask extends AbstractTask {
  
  private static final long serialVersionUID = -5874811911961670142L;
  
  private Emergency tilannehuoneEmergency;
  
  public TilannehuoneEmergencyEntityTask() {
    // Zero-argument constructor
  }
  
  public TilannehuoneEmergencyEntityTask(Emergency tilannehuoneEmergency) {
    this.tilannehuoneEmergency = tilannehuoneEmergency;
  }
  
  public Emergency getTilannehuoneEmergency() {
    return tilannehuoneEmergency;
  }
  
  public void setTilannehuoneEmergency(Emergency tilannehuoneEmergency) {
    this.tilannehuoneEmergency = tilannehuoneEmergency;
  }
  
}
