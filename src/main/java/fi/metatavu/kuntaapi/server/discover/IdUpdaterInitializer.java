package fi.metatavu.kuntaapi.server.discover;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

@Startup
@Singleton
public class IdUpdaterInitializer {
  
  @Inject
  private Logger logger;
  
  @Inject
  @Any
  private Instance<IdUpdater> idUpdaters;

  @PostConstruct
  public void start() {
    Iterator<IdUpdater> updaters = idUpdaters.iterator();
    while (updaters.hasNext()) {
      IdUpdater updater = updaters.next();
      logger.log(Level.INFO, () -> String.format("Registering id updater %s", updater.getName()));
    }
  }
   
}
