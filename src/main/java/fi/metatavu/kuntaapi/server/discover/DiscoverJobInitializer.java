package fi.metatavu.kuntaapi.server.discover;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.lifecycle.BeforeShutdownEvent;
import fi.metatavu.kuntaapi.server.tasks.metaflow.Job;

@Singleton
@ApplicationScoped
@Startup
public class DiscoverJobInitializer {

  @Inject
  private Logger logger;
  
  @Inject
  @Any
  private Instance<AbstractDiscoverJob> jobs;

  @Resource
  private ManagedScheduledExecutorService managedScheduledExecutorService;
  
  @Resource
  private EJBContext ejbContext;

  private List<ScheduledFuture<?>> futures;

  @PostConstruct
  public void postConstruct() {
    futures = new ArrayList<>();
    
    Iterator<AbstractDiscoverJob> jobIterator = jobs.iterator();
    while (jobIterator.hasNext()) {
      startJob(jobIterator.next());
    }
  }
  
  /**
   * Event listener that listens server shutdown event
   * 
   * @param event event
   */
  public void onBeforeShutdown(@Observes BeforeShutdownEvent event) {
    logger.info("Cancelling tasks");
    
    futures.stream().forEach(future -> {
      future.cancel(false);
    });

    logger.info("Tasks cancelled");
  }

  /**
   * Starts job
   * 
   * @param job job
   */
  private void startJob(Job job) {
    logger.log(Level.INFO, () -> String.format("Starting job %s", job.getName()));    
    futures.add(managedScheduledExecutorService.scheduleWithFixedDelay(job, job.getTimerWarmup(), job.getTimerInterval(), TimeUnit.MILLISECONDS));
  }

}
