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

import org.apache.commons.lang3.StringUtils;

import fi.otavanopisto.kuntaapi.server.controllers.ClientContainer;
import fi.otavanopisto.kuntaapi.server.controllers.SecurityController;
import fi.otavanopisto.kuntaapi.server.discover.AbstractUpdater;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.discover.IdUpdater;
import fi.otavanopisto.kuntaapi.server.discover.UpdaterHealth;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;
import fi.otavanopisto.kuntaapi.server.tasks.AbstractTaskQueue;

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
  
  /**
   * Updaters health
   * 
   * @return [OK, UNKNOWN, WARNING or CRITICAL]: Details
   */
  @GET
  @Path ("/updaters/health")
  @Produces (MediaType.TEXT_PLAIN)
  public Response getUpdatersHealth() {
    UpdaterHealth overallHealth = UpdaterHealth.OK;
    UpdaterDetails updaterDetails = new UpdaterDetails();
    
    for (IdUpdater idUpdater : idUpdaters) {
      overallHealth = minHealth(overallHealth, idUpdater.getHealth());
      updaterDetails.addUpdaterState(idUpdater);
    }

    for (EntityUpdater entityUpdater : entityUpdaters) {
      overallHealth = minHealth(overallHealth, entityUpdater.getHealth());
      updaterDetails.addUpdaterState(entityUpdater);
    }
    
    if (overallHealth == UpdaterHealth.OK) {
      return Response.ok(String.format("%s: ok: %d", 
        overallHealth.name(), 
        updaterDetails.getOkCount()
      )).build();
    } else {
      return Response.ok(String.format("%s: ok: %d, unknown: %d, warnings: %d , criticals: %d - %s", 
        overallHealth.name(), 
        updaterDetails.getOkCount(), 
        updaterDetails.getUnknownCount(),
        updaterDetails.getWarningCount(),
        updaterDetails.getCriticalCount(),
        StringUtils.join(updaterDetails.getDetails(), ", "))
     ).build();
    }
    
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
  
  private UpdaterHealth minHealth(UpdaterHealth health1, UpdaterHealth health2) {
    if (health2.ordinal() > health1.ordinal()) {
      return health2;
    }
    
    return health1;
  }
  
  private class UpdaterDetails {
    
    List<String> details = new ArrayList<>();
    int okCount = 0;
    int unknownCount = 0;
    int warningCount = 0;
    int criticalCount= 0;
    
    public void addUpdaterState(AbstractUpdater updater) {
      UpdaterHealth updaterHealth = updater.getHealth();
      
      if (updaterHealth == UpdaterHealth.UNKNOWN) {
        details.add(String.format("Updater %s health is %s", updater.getName(), updaterHealth));
      } else if (updaterHealth != UpdaterHealth.OK) {
        details.add(String.format("Updater %s health is %s (%d ms since last run)", updater.getName(), updaterHealth, updater.getSinceLastRun()));
      }
      
      switch (updaterHealth) {
        case OK:
          okCount++;
        break;
        case CRITICAL:
          criticalCount++;
        break;
        case WARNING:
          warningCount++;
        break;
        default:
          unknownCount++;
        break;
      }
    }
    
    public int getCriticalCount() {
      return criticalCount;
    }
    
    public List<String> getDetails() {
      return details;
    }
    
    public int getOkCount() {
      return okCount;
    }
    
    public int getUnknownCount() {
      return unknownCount;
    }
    
    public int getWarningCount() {
      return warningCount;
    }
    
  }

}
