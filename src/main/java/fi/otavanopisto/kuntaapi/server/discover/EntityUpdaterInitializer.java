package fi.otavanopisto.kuntaapi.server.discover;

import java.util.Iterator;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.DependsOn;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

@Singleton
@Startup
@DependsOn ("SchedulerIntervalUpdater")
public class EntityUpdaterInitializer {
  
  @Inject
  private Logger logger;
  
  @Inject
  @Any
  private Instance<EntityUpdater> entityUpdaters;
  
  @PostConstruct
  public void start() {
    Iterator<EntityUpdater> updaters = entityUpdaters.iterator();
    while (updaters.hasNext()) {
      EntityUpdater updater = updaters.next();
      logger.info(String.format("Registering entity updater %s", updater.getName()));
    }
  }
   
}
