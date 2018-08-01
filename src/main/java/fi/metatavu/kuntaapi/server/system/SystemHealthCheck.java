package fi.metatavu.kuntaapi.server.system;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Initialized;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import fi.metatavu.kuntaapi.server.settings.SystemSettingController;

@ApplicationScoped
public class SystemHealthCheck {
  
  private static final String LINE = "------------------------------";
  
  @Inject
  private Logger logger;

  @Inject
  private SystemSettingController systemSettingController;

  public void init(@Observes @Initialized(ApplicationScoped.class) Object init) {
    if (StringUtils.equalsIgnoreCase("true", System.getProperty("kuntaApi.skipHealthCheck"))) { 
      return;
    }
    
    List<String> problems = new ArrayList<>();
    
    if (systemSettingController.inFailsafeMode()) {
      problems.add("System is running in failsafe mode");
    }
    
    if (systemSettingController.inTestMode()) {
      problems.add("System is running in test mode");
    }
    
    logger.info(LINE);
    logger.info("System health check");
    
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
  
}
