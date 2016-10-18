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
public class IdUpdaterInitializer {
  
  @Inject
  private Logger logger;
  
  @Inject
  @Any
  private Instance<IdUpdater> idUpdaters;

  @Start
  public void start() {
    Iterator<IdUpdater> updaters = idUpdaters.iterator();
    while (updaters.hasNext()) {
      IdUpdater updater = updaters.next();
      logger.info(String.format("Starting id updater %s", updater.getName()));
      updater.startTimer();
    }
  }
  
  @Stop
  public void stop() {
    Iterator<IdUpdater> updaters = idUpdaters.iterator();
    while (updaters.hasNext()) {
      IdUpdater updater = updaters.next();
      logger.info(String.format("Stopping id updater %s", updater.getName()));
      updater.stopTimer();
    }
  }
   
}
