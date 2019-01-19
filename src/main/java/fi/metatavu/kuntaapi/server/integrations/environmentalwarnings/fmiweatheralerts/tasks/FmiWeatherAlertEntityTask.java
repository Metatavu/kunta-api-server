package fi.metatavu.kuntaapi.server.integrations.environmentalwarnings.fmiweatheralerts.tasks;

import fi.metatavu.kuntaapi.server.integrations.environmentalwarnings.fmiweatheralerts.wfs.Feature;
import fi.metatavu.metaflow.tasks.impl.DefaultTaskImpl;

/**
 * FMI Weather alert update task
 * 
 * @author Antti Leppä
 */
public class FmiWeatherAlertEntityTask extends DefaultTaskImpl {
  
  private static final long serialVersionUID = 6997910987427757708L;
  
  private Feature feature;  
  private Long orderIndex;
  
  /**
   * Constructor
   */
  public FmiWeatherAlertEntityTask() {
    // Zero-argument constructor
  }
  
  /**
   * Constructor
   * 
   * @param priority is task priority task
   * @param feature alert WFS feature
   * @param orderIndex order index
   */
  public FmiWeatherAlertEntityTask(boolean priority, Feature feature, Long orderIndex) {
    super(String.format("fmiweatheralert-entity-task-%s", feature.getProperties().getIdentifier()), priority);
    this.feature = feature;
    this.orderIndex = orderIndex;
  }
  
  /**
   * Returns WFS feature
   * 
   * @return WFS feature
   */
  public Feature getFeature() {
    return feature;
  }
  
  /**
   * Sets WFS feature
   * 
   * @param feature WFS feature
   */
  public void setFeature(Feature feature) {
    this.feature = feature;
  }
  
  /**
   * Returns order index
   * 
   * @return order index
   */
  public Long getOrderIndex() {
    return orderIndex;
  }
  
  /**
   * Sets order index
   * 
   * @param orderIndex order index
   */
  public void setOrderIndex(Long orderIndex) {
    this.orderIndex = orderIndex;
  }

}
