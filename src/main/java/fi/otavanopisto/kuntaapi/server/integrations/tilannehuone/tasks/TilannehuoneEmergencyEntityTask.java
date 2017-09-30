package fi.otavanopisto.kuntaapi.server.integrations.tilannehuone.tasks;

import fi.otavanopisto.kuntaapi.server.integrations.tilannehuone.model.Emergency;
import fi.otavanopisto.kuntaapi.server.tasks.AbstractTask;

public class TilannehuoneEmergencyEntityTask extends AbstractTask {
  
  private static final long serialVersionUID = -5874811911961670142L;
  
  private Long orderIndex;
  private Emergency tilannehuoneEmergency;
  
  public TilannehuoneEmergencyEntityTask() {
    // Zero-argument constructor
  }
  
  public TilannehuoneEmergencyEntityTask(Emergency tilannehuoneEmergency, Long orderIndex) {
    this.tilannehuoneEmergency = tilannehuoneEmergency;
    this.orderIndex = orderIndex;
  }
  
  public Emergency getTilannehuoneEmergency() {
    return tilannehuoneEmergency;
  }
  
  public void setTilannehuoneEmergency(Emergency tilannehuoneEmergency) {
    this.tilannehuoneEmergency = tilannehuoneEmergency;
  }
  
  public Long getOrderIndex() {
    return orderIndex;
  }
  
  public void setOrderIndex(Long orderIndex) {
    this.orderIndex = orderIndex;
  }

  @Override
  public String getUniqueId() {
    return String.format("tilannehuone-emergency-entity-task-%s", tilannehuoneEmergency.toString());
  }
  
}
