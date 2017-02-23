package fi.otavanopisto.kuntaapi.server.system;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import fi.otavanopisto.kuntaapi.server.cache.AbstractCache;

@ApplicationScoped
public class SystemHealthCheck {
  
  private static final String LINE = "------------------------------";
  
  @Inject
  private Logger logger;

  @Inject
  @Any
  private Instance<AbstractCache<?, ?>> caches;
   
  public void init(@Observes @Initialized(ApplicationScoped.class) Object init) {
    if (StringUtils.equalsIgnoreCase("true", System.getProperty("kuntaApi.skipHealthCheck"))) { 
      return;
    }
    
    List<String> problems = new ArrayList<>();
    checkCacheHealth(problems);
    
    logger.info(LINE);
    logger.info("System health check");
    logger.info(LINE);
    
    if (!problems.isEmpty()) {
      logger.warning("Following system health problems detected:");
      for (String problem : problems) {
        logger.warning(problem);
      }
    } else {
      logger.info("System health ok");
    }
    
    logger.info(LINE);
  }
  
  private void checkCacheHealth(List<String> problems) {
    for (AbstractCache<?, ?> cache : caches) {
      if (!cache.isHealthy()) {
        problems.add(String.format("Configuration problems detected with %s cache", cache.getCacheName()));
      }
    }
  }
  
}
