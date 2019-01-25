package fi.metatavu.kuntaapi.server.integrations.environmentalwarnings.fmiweatheralerts;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.jboss.logmanager.Level;

import fi.metatavu.kuntaapi.server.id.EnvironmentalWarningId;
import fi.metatavu.kuntaapi.server.integrations.environmentalwarnings.fmiweatheralerts.wfs.Feature;
import fi.metatavu.kuntaapi.server.integrations.environmentalwarnings.fmiweatheralerts.wfs.Properties;
import fi.metatavu.kuntaapi.server.rest.model.EnvironmentalWarning;
import fi.metatavu.kuntaapi.server.rest.model.LocalizedValue;

/**
 * Translator for FMI Weather Alerts
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class FmiWeatherAlertsTranslator {
  
  private Logger logger;

  /**
   * Translates FMI Weather Alert feature into environmental warning
   * 
   * @param id environmental warning id
   * @param feature FMI Weather Alert feature
   * @return translated environmental warning
   */
  public EnvironmentalWarning translateWeatherAlert(EnvironmentalWarningId id, Feature feature) {
    if (feature == null) {
      return null;
    }

    Properties properties = feature.getProperties();
    if (properties == null) {
      logger.log(Level.WARNING, "FMI Weather Alert did not contain properties");
      return null;
    }

    List<LocalizedValue> description = new ArrayList<>();
    addLocalizedValue(description, "en", properties.getInfoEn());
    addLocalizedValue(description, "fi", properties.getInfoFi());
    addLocalizedValue(description, "sv", properties.getInfoSv());
    
    EnvironmentalWarning result = new EnvironmentalWarning();
    result.setActualizationProbability(properties.getActualizationProbability());
    result.setCauses(split(properties.getCauses()));
    result.setContext(properties.getWarningContext());
    result.setDescription(description);
    result.setEnd(parseTime(properties.getEffectiveUntil()));
    result.setId(id.getId());
    result.setSeverity(properties.getSeverity());
    result.setStart(parseTime(properties.getEffectiveFrom()));
    result.setType("WEATHER");

    return result;
  }

  /**
   * Parses ISO date string into OffsetDateTime -object
   * 
   * @param string ISO date string
   * @return parsed OffsetDateTime
   */
  private OffsetDateTime parseTime(String string) {
    if (StringUtils.isBlank(string)) {
      return null;
    }
    
    return OffsetDateTime.parse(string);
  }

  /**
   * Adds localized value into list if given value is not empty
   * 
   * @param result result list
   * @param language language
   * @param value value
   */
  private void addLocalizedValue(List<LocalizedValue> result, String language, String value) {
    if (StringUtils.isNotBlank(value)) {
      LocalizedValue localizedValue = new LocalizedValue();
      localizedValue.setLanguage(language);
      localizedValue.setValue(StringEscapeUtils.unescapeHtml4(value));
      result.add(localizedValue);
    }
  }

  /**
   * Splits space separated string into list of strings
   * 
   * @param string string
   * @return list of strings
   */
  private List<String> split(String string) {
    if (StringUtils.isBlank(string)) {
      return Collections.emptyList();
    }
    
    return Arrays.asList(StringUtils.split(string, ' '));
  }

}
