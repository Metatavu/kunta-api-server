package fi.otavanopisto.kuntaapi.server.discover;

import java.util.Iterator;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import com.bertoncelj.wildflysingletonservice.Start;
import com.bertoncelj.wildflysingletonservice.Stop;

@ApplicationScoped
@SuppressWarnings ("squid:S3306")
public class EntityUpdaterInitializer {
  
  @Inject
  private Logger logger;
  
  @Inject
  @Any
  private Instance<EntityUpdater> entityUpdaters;
  
  @Start
  public void start() {
    Iterator<EntityUpdater> updaters = entityUpdaters.iterator();
    while (updaters.hasNext()) {
      EntityUpdater updater = updaters.next();
      logger.info(String.format("Starting entity updater %s", updater.getName()));
      updater.startTimer();
    }
  }
  
  @Stop
  public void stop() {
    Iterator<EntityUpdater> updaters = entityUpdaters.iterator();
    while (updaters.hasNext()) {
      EntityUpdater updater = updaters.next();
      logger.info(String.format("Stopping entity updater %s", updater.getName()));
      updater.stopTimer();
    }
  }
   
}
