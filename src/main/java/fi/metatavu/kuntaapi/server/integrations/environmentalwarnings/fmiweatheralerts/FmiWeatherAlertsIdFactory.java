package fi.metatavu.kuntaapi.server.integrations.environmentalwarnings.fmiweatheralerts;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.id.AbstractIdFactory;

/**
 * Id factory for FMI weather alerts
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class FmiWeatherAlertsIdFactory extends AbstractIdFactory {
  
  @Override
  public String getSource() {
    return FmiWeatherAlertsConsts.IDENTIFIER_NAME;
  }
  
}
