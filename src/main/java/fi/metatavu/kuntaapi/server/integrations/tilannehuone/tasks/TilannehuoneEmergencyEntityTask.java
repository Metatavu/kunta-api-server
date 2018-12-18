package fi.metatavu.kuntaapi.server.integrations.tilannehuone.tasks;

import fi.metatavu.kuntaapi.server.integrations.tilannehuone.model.Emergency;
import fi.metatavu.metaflow.tasks.impl.DefaultTaskImpl;

public class TilannehuoneEmergencyEntityTask extends DefaultTaskImpl {
  
  private static final long serialVersionUID = -5874811911961670142L;
  
  private Long orderIndex;
  private Emergency tilannehuoneEmergency;
  
  public TilannehuoneEmergencyEntityTask() {
    // Zero-argument constructor
  }
  
  public TilannehuoneEmergencyEntityTask(boolean priority, Emergency tilannehuoneEmergency, Long orderIndex) {
    super(String.format("tilannehuone-emergency-entity-task-%s", tilannehuoneEmergency.toString()), priority);
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
  
}
