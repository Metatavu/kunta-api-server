package fi.metatavu.kuntaapi.server.integrations.environmentalwarnings.fmiweatheralerts.tasks;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.metaflow.tasks.Task;

/**
 * Refresh task for FMI Weather alerts
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class FmiWeatherAlertRefreshTask implements Task {

  private static final long serialVersionUID = -4737423228187488674L;

  @Override
  public String getUniqueId() {
    return "fmi-weather-alerts-refresh";
  }

  @Override
  public boolean getPriority() {
    return false;
  }
  
}