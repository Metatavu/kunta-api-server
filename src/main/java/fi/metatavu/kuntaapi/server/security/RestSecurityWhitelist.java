package fi.metatavu.kuntaapi.server.security;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class RestSecurityWhitelist {

  @Inject
  private Logger logger;
  
  private Set<String> whitelist;
  
  @PostConstruct
  public void init() {
    whitelist = loadWhitelist();
  }
  
  /**
   * Returns whether path is whitelisted
   * 
   * @param path path
   * @return whether path is whitelisted
   */
  public boolean isWhitelisted(String path) {
    return whitelist.contains(path);
  }
  
  /**
   * Loads settings from the properties file
   */
  private Set<String> loadWhitelist() {
    Properties properties = new Properties();
    try {
      properties.load(getClass().getClassLoader().getResourceAsStream("security-whitelist.properties"));
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Failed to load slow-request-filter.properties", e);
    }
    
    Set<Object> keySet = properties.keySet();
    Set<String> result = new HashSet<>(keySet.size());
    for (Object key : keySet) {
      result.add((String) key);
    }
    
    return Collections.unmodifiableSet(result);
  }

}
