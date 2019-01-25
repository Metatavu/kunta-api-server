package fi.metatavu.kuntaapi.server.integrations.environmentalwarnings.fmiweatheralerts;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import fi.metatavu.kuntaapi.server.discover.IdDiscoverJob;
import fi.metatavu.kuntaapi.server.integrations.GenericHttpClient;
import fi.metatavu.kuntaapi.server.integrations.GenericHttpClient.Response;
import fi.metatavu.kuntaapi.server.integrations.environmentalwarnings.fmiweatheralerts.tasks.FmiWeatherAlertEntityTask;
import fi.metatavu.kuntaapi.server.integrations.environmentalwarnings.fmiweatheralerts.tasks.FmiWeatherAlertRefreshTask;
import fi.metatavu.kuntaapi.server.integrations.environmentalwarnings.fmiweatheralerts.tasks.FmiWeatherAlertRefreshTaskQueue;
import fi.metatavu.kuntaapi.server.integrations.environmentalwarnings.fmiweatheralerts.tasks.FmiWeatherAlertTaskQueue;
import fi.metatavu.kuntaapi.server.integrations.environmentalwarnings.fmiweatheralerts.wfs.Feature;
import fi.metatavu.kuntaapi.server.integrations.environmentalwarnings.fmiweatheralerts.wfs.FeatureCollection;
import fi.metatavu.kuntaapi.server.settings.SystemSettingController;

/**
 * Discover job for FMI weather alerts
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class FmiWeatherAlertsIdDiscoverJob extends IdDiscoverJob {
  
  @Inject
  private Logger logger;
  
  @Inject
  private GenericHttpClient httpClient;

  @Inject
  private SystemSettingController systemSettingController;
  
  @Inject
  private FmiWeatherAlertRefreshTaskQueue fmiWeatherAlertRefreshTaskQueue;
  
  @Inject
  private FmiWeatherAlertTaskQueue fmiWeatherAlertTaskQueue;
  
  @Override
  public String getName() {
    return "fmiweatheralerts";
  }

  @Override
  public void timeout() {
    FmiWeatherAlertRefreshTask task = fmiWeatherAlertRefreshTaskQueue.next();
    if (task != null) {
      refreshWeatherAlerts();
    } else if (fmiWeatherAlertRefreshTaskQueue.isEmptyAndLocalNodeResponsible()) {
      fmiWeatherAlertRefreshTaskQueue.enqueueTask(new FmiWeatherAlertRefreshTask());
    }
  }
  
  /**
   * Refreshes weather alerts from FMI API
   */
  private void refreshWeatherAlerts() {
    String apiUrl = systemSettingController.getSettingValue(FmiWeatherAlertsConsts.SYSTEM_SETTING_API_URL);
    if (StringUtils.isBlank(apiUrl)) {
      logger.info("API URL is null");
      return;
    }
    
    URI uri;
    try {
      uri = new URI(apiUrl);
    } catch (URISyntaxException e) {
      if (logger.isLoggable(Level.SEVERE)) {
        logger.log(Level.SEVERE, String.format("Malformed URI %s", apiUrl), e);
      }
      
      return;
    }
    
    Response<FeatureCollection> response = httpClient.doGETRequest(uri, new GenericHttpClient.ResultType<FeatureCollection>() {});
    if (response.isOk()) {
      List<Feature> features = response.getResponseEntity().getFeatures();
      for (int i = 0; i < features.size(); i++) {
        fmiWeatherAlertTaskQueue.enqueueTask(new FmiWeatherAlertEntityTask(false, features.get(i), (long) i));
      }
    } else {
      logger.log(Level.SEVERE, () -> String.format("Failed to list weather alerts from FMI. API Returned [%d] %s", response.getStatus(), response.getMessage()));
    }
  }

}
