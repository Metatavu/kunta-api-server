package fi.otavanopisto.kuntaapi.server.index;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class AnalyzerMapper {

  private static final Map<String, String> LANGUAGE_ANALYZERS;
  
  private AnalyzerMapper() {
  }
  
  public static String getLanguageAnalyzer(String language) {
    if (StringUtils.isNotBlank(language)) {
      return LANGUAGE_ANALYZERS.get(language);
    }
    
    return null;
  }

  static {
    LANGUAGE_ANALYZERS = new HashMap<>();
    LANGUAGE_ANALYZERS.put("en", "english");
    LANGUAGE_ANALYZERS.put("fi", "finnish");
  }
  
}
