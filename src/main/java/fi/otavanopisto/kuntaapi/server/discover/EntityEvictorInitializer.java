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
public class EntityEvictorInitializer {
  
  @Inject
  private Logger logger;
  
  @Inject
  @Any
  private Instance<EntityEvictor> entityEvictors;
  
  @Start
  public void start() {
    Iterator<EntityEvictor> evictors = entityEvictors.iterator();
    while (evictors.hasNext()) {
      EntityEvictor evictor = evictors.next();
      logger.info(String.format("Starting entity evictor %s", evictor.getName()));
      evictor.startTimer();
    }
  }
  
  @Stop
  public void stop() {
    Iterator<EntityEvictor> evictors = entityEvictors.iterator();
    while (evictors.hasNext()) {
      EntityEvictor evictor = evictors.next();
      logger.info(String.format("Stopping entity evictor %s", evictor.getName()));
      evictor.stopTimer();
    }
  }
   
}
