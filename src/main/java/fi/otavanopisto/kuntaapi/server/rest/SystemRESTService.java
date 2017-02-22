package fi.otavanopisto.kuntaapi.server.rest;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import fi.otavanopisto.kuntaapi.server.cache.SystemController;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;
import fi.otavanopisto.kuntaapi.server.tasks.AbstractTaskQueue;
import fi.otavanopisto.kuntaapi.server.tasks.TaskQueueStatistics;

/**
 * System REST Services
 * 
 * @author Antti Leppä
 * @author Heikki Kurhinen
 */
@Path ("/system")
@RequestScoped
@Produces (MediaType.APPLICATION_JSON)
@Consumes (MediaType.APPLICATION_JSON)
public class SystemRESTService {

  @PersistenceUnit
  private EntityManagerFactory entityManagerFactory;
  
  @Inject
  private SystemController systemController; 

  @Inject  
  private SystemSettingController systemSettingController;
  
  @Inject  
  private Instance<AbstractTaskQueue<?>> taskQueues;

  /**
   * Returns pong
   * 
   * @return pong in plain text
   */
  @GET
  @Path ("/ping")
  @Produces (MediaType.TEXT_PLAIN)
  public Response getPing() {
    if (!systemController.isCacheContainerOk()) {
      return Response.status(Status.SERVICE_UNAVAILABLE).build();
    }
    
    return Response.ok("pong").build();
  }
  
  /**
   * Flushes jpa entity cache
   * 
   * @return "ok"
   */
  @GET
  @Path ("/jpa/cache/flush")
  @Produces (MediaType.TEXT_PLAIN)
  public Response flushCaches() {
    if (systemSettingController.inTestMode()) {
      entityManagerFactory.getCache().evictAll();
      return Response.ok("ok").build();
    }
    
    return Response.status(Status.FORBIDDEN).build();
  }
  
  /**
   * Returns statistics for task queue
   * 
   * @return "ok"
   */
  @GET
  @Path ("/tasks/statistics")
  public Response getTaskQueueStatistics() {
    List<TaskQueueStatistics> statistics = new ArrayList<>();
    
    for (AbstractTaskQueue<?> taskQueue : taskQueues) {
      statistics.add(taskQueue.getStatistics());
    }
    
    return Response
      .ok()
      .entity(statistics)
      .build();
  }
  
  /**
   * Flushes all tasks
   * 
   * @return "ok"
   */
  @GET
  @Path ("/tasks/clear")
  @Produces (MediaType.TEXT_PLAIN)
  public Response flushTasks() {
    if (systemSettingController.inTestMode()) {
      for (AbstractTaskQueue<?> taskQueue : taskQueues) {
        taskQueue.clear();
      }
      
      return Response.ok("ok").build();
    }
    
    return Response.status(Status.FORBIDDEN).build();
  }
  
}
