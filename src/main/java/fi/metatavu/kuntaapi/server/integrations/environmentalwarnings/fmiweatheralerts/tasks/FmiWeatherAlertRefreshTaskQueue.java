package fi.metatavu.kuntaapi.server.integrations.environmentalwarnings.fmiweatheralerts.tasks;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.tasks.metaflow.AbstractKuntaApiTaskQueue;

/**
 * Task queue for FMI Weather alerts
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class FmiWeatherAlertRefreshTaskQueue extends AbstractKuntaApiTaskQueue<FmiWeatherAlertRefreshTask> {
  
  @Override
  public String getName() {
    return "fmi-weather-alerts-refresh";
  }

}