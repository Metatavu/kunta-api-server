package fi.otavanopisto.kuntaapi.server.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import fi.otavanopisto.kuntaapi.server.controllers.ClientContainer;
import fi.otavanopisto.kuntaapi.server.controllers.SecurityController;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.discover.IdUpdater;
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
  private Logger logger;
  
  @Inject  
  private SystemSettingController systemSettingController;

  @Inject
  private SecurityController securityController;

  @Inject
  private ClientContainer clientContainer;
  
  @Inject  
  private Instance<AbstractTaskQueue<?>> taskQueues;

  @Inject  
  private Instance<IdUpdater> idUpdaters;
  
  @Inject  
  private Instance<EntityUpdater> entityUpdaters;

  /**
   * Returns pong
   * 
   * @return pong in plain text
   */
  @GET
  @Path ("/ping")
  @Produces (MediaType.TEXT_PLAIN)
  public Response getPing() {
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
    if (inTestModeOrUnrestrictedClient()) {
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
    if (inTestModeOrUnrestrictedClient()) {
      for (AbstractTaskQueue<?> taskQueue : taskQueues) {
        taskQueue.clear();
      }
      
      return Response.ok("ok").build();
    }
    
    return Response.status(Status.FORBIDDEN).build();
  }
  
  /**
   * Stops all tasks
   * 
   * @return "ok"
   */
  @GET
  @Path ("/tasks/stop")
  @Produces (MediaType.TEXT_PLAIN)
  public Response stopTasks() {
    if (inTestModeOrUnrestrictedClient()) {
      for (AbstractTaskQueue<?> taskQueue : taskQueues) {
        taskQueue.stop();
      }
      
      return Response.ok("ok").build();
    }
    
    return Response.status(Status.FORBIDDEN).build();
  }
 
  /**
   * Stops all tasks
   * 
   * @return "ok"
   */
  @GET
  @Path ("/updaters/all/stop")
  @Produces (MediaType.TEXT_PLAIN)
  public Response stopAllUpdaters(@QueryParam ("cancelTimers") Boolean cancelTimers) {
    if (inTestModeOrUnrestrictedClient()) {
      
      for (IdUpdater idUpdater : idUpdaters) {
        idUpdater.stop(cancelTimers);
      }
      
      for (EntityUpdater entityUpdater : entityUpdaters) {
        entityUpdater.stop(cancelTimers);
      }
      
      return Response.ok("ok").build();
    }
    
    return Response.status(Status.FORBIDDEN).build();
  }
  
  @GET
  @Path ("/log")
  @Produces (MediaType.TEXT_PLAIN)
  public Response log(@QueryParam ("text") String text) {
    if (!inTestModeOrUnrestrictedClient()) {
      return Response.status(Status.FORBIDDEN).build();
    }
    
    logger.log(Level.INFO, text);
    
    return Response.ok("ok").build();
  }
  
  private boolean inTestModeOrUnrestrictedClient() {
    return systemSettingController.inTestMode() || securityController.isUnrestrictedClient(clientContainer.getClient());
  }
  
}
