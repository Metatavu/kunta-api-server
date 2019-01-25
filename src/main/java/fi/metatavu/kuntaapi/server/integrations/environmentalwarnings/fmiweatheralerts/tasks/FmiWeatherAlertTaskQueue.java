package fi.metatavu.kuntaapi.server.integrations.environmentalwarnings.fmiweatheralerts.tasks;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.tasks.jms.DefaultJmsTaskQueue;

/**
 * Task queue for FMI Weather alert tasks
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class FmiWeatherAlertTaskQueue extends DefaultJmsTaskQueue<FmiWeatherAlertEntityTask> {
  
  public static final String NAME = "fmiweatheralerts";
  public static final String JMS_QUEUE = JMS_QUEUE_PREFIX + NAME;
  
  @Override
  public String getName() {
    return NAME;
  }

}