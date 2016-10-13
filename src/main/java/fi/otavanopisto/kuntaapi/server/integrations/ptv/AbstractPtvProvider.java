package fi.otavanopisto.kuntaapi.server.integrations.ptv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import fi.otavanopisto.kuntaapi.server.rest.model.LocalizedValue;
import fi.otavanopisto.restfulptv.client.model.LanguageItem;
import fi.otavanopisto.restfulptv.client.model.LocalizedListItem;

/**
 * Abstract base class for all PTV providers
 * 
 * @author Antti Leppä
 */
public abstract class AbstractPtvProvider {

  protected List<LocalizedValue> translateLocalizedItems(String type, List<LocalizedListItem> items) {
    if (items != null && !items.isEmpty()) {
      List<LocalizedValue> result = new ArrayList<>();
      
      for (LocalizedListItem item : items) {
        if (StringUtils.equalsIgnoreCase(type, item.getType())) {
          LocalizedValue localizedValue = new LocalizedValue();
          localizedValue.setLanguage(item.getLanguage());
          localizedValue.setValue(item.getValue());
          result.add(localizedValue);
        }
      }
    
      return result;
    }
    
    return Collections.emptyList();
  }
  
  protected List<LocalizedValue> translateLanguageItems(List<LanguageItem> items) {
    if (items != null && !items.isEmpty()) {
      List<LocalizedValue> result = new ArrayList<>();
      
      for (LanguageItem item : items) {
        LocalizedValue localizedValue = new LocalizedValue();
        localizedValue.setLanguage(item.getLanguage());
        localizedValue.setValue(item.getValue());
        result.add(localizedValue);
      }
    
      return result;
    }
    
    return Collections.emptyList();
  }
  
}
