package fi.metatavu.kuntaapi.server.discover;

import java.util.Iterator;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.EJBContext;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.tasks.Job;

@Singleton
@ApplicationScoped
@Startup
public class Updater {

  @Inject
  @Any
  private Instance<AbstractDiscoverJob> jobs;

  @Resource
  private ManagedScheduledExecutorService managedScheduledExecutorService;
  
  @Resource
  private EJBContext ejbContext;
  
  @PostConstruct
  public void postConstruct() {
    Iterator<AbstractDiscoverJob> jobIterator = jobs.iterator();
    while (jobIterator.hasNext()) {
      startJob(jobIterator.next());
    }
  }
  
  @PreDestroy
  public void preDestroy() {
  }

  private void startJob(Job job) {
    System.out.println(String.format("Starting job %s", job.getName()));
    
    managedScheduledExecutorService.scheduleWithFixedDelay(job, job.getTimerWarmup(), job.getTimerInterval(), TimeUnit.MILLISECONDS);
  }


}
