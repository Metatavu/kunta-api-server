package fi.metatavu.kuntaapi.server.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateful;
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

import fi.metatavu.kuntaapi.server.controllers.ClientContainer;
import fi.metatavu.kuntaapi.server.controllers.IdentifierController;
import fi.metatavu.kuntaapi.server.controllers.SecurityController;
import fi.metatavu.kuntaapi.server.controllers.ServiceController;
import fi.metatavu.kuntaapi.server.discover.AbstractDiscoverJob;
import fi.metatavu.kuntaapi.server.discover.EntityDiscoverJob;
import fi.metatavu.kuntaapi.server.discover.IdDiscoverJob;
import fi.metatavu.kuntaapi.server.discover.UpdaterHealth;
import fi.metatavu.kuntaapi.server.id.BaseId;
import fi.metatavu.kuntaapi.server.id.ElectronicServiceChannelId;
import fi.metatavu.kuntaapi.server.id.IdController;
import fi.metatavu.kuntaapi.server.id.NewsArticleId;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.id.PageId;
import fi.metatavu.kuntaapi.server.id.PhoneServiceChannelId;
import fi.metatavu.kuntaapi.server.id.PrintableFormServiceChannelId;
import fi.metatavu.kuntaapi.server.id.ServiceId;
import fi.metatavu.kuntaapi.server.id.ServiceLocationServiceChannelId;
import fi.metatavu.kuntaapi.server.id.WebPageServiceChannelId;
import fi.metatavu.kuntaapi.server.index.SearchResult;
import fi.metatavu.kuntaapi.server.integrations.KuntaApiIdFactory;
import fi.metatavu.kuntaapi.server.integrations.ServiceChannelSortBy;
import fi.metatavu.kuntaapi.server.integrations.ServiceSortBy;
import fi.metatavu.kuntaapi.server.integrations.SortDir;
import fi.metatavu.kuntaapi.server.integrations.management.ManagementConsts;
import fi.metatavu.kuntaapi.server.integrations.management.tasks.NewsArticleIdTaskQueue;
import fi.metatavu.kuntaapi.server.integrations.management.tasks.PageIdTaskQueue;
import fi.metatavu.kuntaapi.server.integrations.ptv.PtvConsts;
import fi.metatavu.kuntaapi.server.integrations.ptv.tasks.OrganizationIdTaskQueue;
import fi.metatavu.kuntaapi.server.integrations.ptv.tasks.ServiceChannelTasksQueue;
import fi.metatavu.kuntaapi.server.integrations.ptv.tasks.ServiceChannelUpdateTask;
import fi.metatavu.kuntaapi.server.integrations.ptv.tasks.ServiceIdTaskQueue;
import fi.metatavu.kuntaapi.server.persistence.model.Identifier;
import fi.metatavu.kuntaapi.server.rest.model.Service;
import fi.metatavu.kuntaapi.server.rest.model.ServiceLocationServiceChannel;
import fi.metatavu.kuntaapi.server.settings.SystemSettingController;
import fi.metatavu.kuntaapi.server.tasks.IdTask;
import fi.metatavu.kuntaapi.server.tasks.IdTask.Operation;
import fi.metatavu.kuntaapi.server.tasks.metaflow.AbstractKuntaApiTaskQueue;

/**
 * System REST Services
 * 
 * @author Antti Leppä
 * @author Heikki Kurhinen
 */
@Path ("/system")
@RequestScoped
@Stateful
@Produces (MediaType.APPLICATION_JSON)
@Consumes (MediaType.APPLICATION_JSON)
public class SystemRESTService {
  
  @PersistenceUnit
  private EntityManagerFactory entityManagerFactory;

  @Inject  
  private Logger logger;

  @Inject  
  private KuntaApiIdFactory kuntaApiIdFactory;

  @Inject  
  private IdController idController;

  @Inject  
  private IdentifierController identifierController;
  
  @Inject  
  private SystemSettingController systemSettingController;
  
  @Inject
  private ServiceController serviceController;
  
  @Inject
  private SecurityController securityController;

  @Inject
  private ClientContainer clientContainer;

  @Inject
  private ServiceChannelTasksQueue serviceChannelTasksQueue;
  
  @Inject
  private ServiceIdTaskQueue serviceIdTaskQueue;
  
  @Inject
  private PageIdTaskQueue pageIdTaskQueue;

  @Inject
  private NewsArticleIdTaskQueue newsArticleIdTaskQueue;

  @Inject
  private OrganizationIdTaskQueue organizationIdTaskQueue;
  
  @Inject  
  private Instance<AbstractKuntaApiTaskQueue<?>> taskQueues;
  
  @Inject  
  private Instance<IdDiscoverJob> idDiscoverJobs;
  
  @Inject  
  private Instance<EntityDiscoverJob<?>> entityDiscoverJobs;
  
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
  
  @GET
  @Path ("/utils/ptv/organizationServiceLocationChannelTasks")
  @Produces (MediaType.TEXT_PLAIN)
  @SuppressWarnings ("squid:S3776")
  public Response utilsPtvOrganizationServiceLocationChannelTasks(@QueryParam ("organizationId") String kuntaApiOrganizationIdParam, @QueryParam ("first") Long first, @QueryParam ("max") Long max) {
    if (inTestModeOrUnrestrictedClient()) {
      if (kuntaApiOrganizationIdParam == null || first == null || max == null) {
        return Response.status(Status.BAD_REQUEST).build();
      }
      
      OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(kuntaApiOrganizationIdParam);
      SearchResult<ServiceLocationServiceChannel> locationServiceChannels = serviceController.searchServiceLocationServiceChannels(organizationId, null, ServiceChannelSortBy.NATURAL, SortDir.DESC, first, max);
      if (locationServiceChannels != null) {
        for (ServiceLocationServiceChannel serviceLocationServiceChannel : locationServiceChannels.getResult()) {
          ServiceLocationServiceChannelId locationServiceChannelId = kuntaApiIdFactory.createServiceLocationServiceChannelId(serviceLocationServiceChannel.getId());
          if (locationServiceChannelId != null) {
            createServiceChannelUpdateTask(locationServiceChannelId);
          } else {
            logger.severe("Could not find kunta api id");
          }
        }
      }
      
      return Response.ok("ok").build();
    }
    
    return Response.status(Status.FORBIDDEN).build();
  }
  
  @GET
  @Path ("/utils/ptv/serviceLocationChannelTasks")
  @Produces (MediaType.TEXT_PLAIN)
  @SuppressWarnings ("squid:S3776")
  public Response utilsPtvServiceLocationChannelTasks(@QueryParam ("first") Integer first, @QueryParam ("max") Integer max) {
    if (inTestModeOrUnrestrictedClient()) {
      if (first == null || max == null) {
        return Response.status(Status.BAD_REQUEST).build();
      }
      
      List<ServiceLocationServiceChannelId> serviceChannelIds = identifierController.listServiceLocationServiceChannelIdsBySource(PtvConsts.IDENTIFIER_NAME, first, max);
      for (ServiceLocationServiceChannelId serviceChannelId : serviceChannelIds) {
        createServiceChannelUpdateTask(serviceChannelId); 
      }
      
      return Response.ok("ok").build();
    }
    
    return Response.status(Status.FORBIDDEN).build();
  }

  @GET
  @Path ("/utils/ptv/serviceLocationChannelTask")
  @Produces (MediaType.TEXT_PLAIN)
  @SuppressWarnings ("squid:S3776")
  public Response utilsPtvServiceLocationChannelTask(@QueryParam ("serviceLocationChannelId") String serviceLocationChannelIdParam) {
    if (inTestModeOrUnrestrictedClient()) {
      if (serviceLocationChannelIdParam == null) {
        return Response.status(Status.BAD_REQUEST).build();
      }
      
      ServiceLocationServiceChannelId serviceLocationChannelId = kuntaApiIdFactory.createServiceLocationServiceChannelId(serviceLocationChannelIdParam);
      if (serviceLocationChannelId == null) {
        return Response.status(Status.BAD_REQUEST).build();
      }

      createServiceChannelUpdateTask(serviceLocationChannelId);
      return Response.ok("ok").build();
    }
    
    return Response.status(Status.FORBIDDEN).build();
  }
  
  @GET
  @Path ("/utils/ptv/electronicChannelTasks")
  @Produces (MediaType.TEXT_PLAIN)
  @SuppressWarnings ("squid:S3776")
  public Response utilsPtvElectronicChannelTasks(@QueryParam ("first") Integer first, @QueryParam ("max") Integer max) {
    if (inTestModeOrUnrestrictedClient()) {
      if (first == null || max == null) {
        return Response.status(Status.BAD_REQUEST).build();
      }
      
      List<ElectronicServiceChannelId> serviceChannelIds = identifierController.listElectronicServiceChannelIdsBySource(PtvConsts.IDENTIFIER_NAME, first, max);
      for (ElectronicServiceChannelId serviceChannelId : serviceChannelIds) {
        createServiceChannelUpdateTask(serviceChannelId); 
      }
      
      return Response.ok("ok").build();
    }
    
    return Response.status(Status.FORBIDDEN).build();
  }

  @GET
  @Path ("/utils/ptv/webPageChannelTasks")
  @Produces (MediaType.TEXT_PLAIN)
  @SuppressWarnings ("squid:S3776")
  public Response utilsPtvWebPageChannelTasks(@QueryParam ("first") Integer first, @QueryParam ("max") Integer max) {
    if (inTestModeOrUnrestrictedClient()) {
      if (first == null || max == null) {
        return Response.status(Status.BAD_REQUEST).build();
      }
      
      List<WebPageServiceChannelId> serviceChannelIds = identifierController.listWebPageServiceChannelIdsBySource(PtvConsts.IDENTIFIER_NAME, first, max);
      for (WebPageServiceChannelId serviceChannelId : serviceChannelIds) {
        createServiceChannelUpdateTask(serviceChannelId); 
      }
      
      return Response.ok("ok").build();
    }
    
    return Response.status(Status.FORBIDDEN).build();
  }
  
  @GET
  @Path ("/utils/ptv/printableFormChannelTasks")
  @Produces (MediaType.TEXT_PLAIN)
  @SuppressWarnings ("squid:S3776")
  public Response utilsPtvPrintableFormChannelTasks(@QueryParam ("first") Integer first, @QueryParam ("max") Integer max) {
    if (inTestModeOrUnrestrictedClient()) {
      if (first == null || max == null) {
        return Response.status(Status.BAD_REQUEST).build();
      }
      
      List<PrintableFormServiceChannelId> serviceChannelIds = identifierController.listPrintableFormServiceChannelIdsBySource(PtvConsts.IDENTIFIER_NAME, first, max);
      for (PrintableFormServiceChannelId serviceChannelId : serviceChannelIds) {
        createServiceChannelUpdateTask(serviceChannelId); 
      }
      
      return Response.ok("ok").build();
    }
    
    return Response.status(Status.FORBIDDEN).build();
  }
  
  @GET
  @Path ("/utils/ptv/phoneChannelTasks")
  @Produces (MediaType.TEXT_PLAIN)
  @SuppressWarnings ("squid:S3776")
  public Response utilsPtvPhoneChannelTasks(@QueryParam ("first") Integer first, @QueryParam ("max") Integer max) {
    if (inTestModeOrUnrestrictedClient()) {
      if (first == null || max == null) {
        return Response.status(Status.BAD_REQUEST).build();
      }
      
      List<PhoneServiceChannelId> serviceChannelIds = identifierController.listPhoneServiceChannelIdsBySource(PtvConsts.IDENTIFIER_NAME, first, max);
      for (PhoneServiceChannelId serviceChannelId : serviceChannelIds) {
        createServiceChannelUpdateTask(serviceChannelId); 
      }
      
      return Response.ok("ok").build();
    }
    
    return Response.status(Status.FORBIDDEN).build();
  }
  
  @GET
  @Path ("/utils/ptv/organizationServiceTasks")
  @Produces (MediaType.TEXT_PLAIN)
  @SuppressWarnings ("squid:S3776")
  public Response utilsPtvOrganizationServiceTasks(@QueryParam ("organizationId") String kuntaApiOrganizationIdParam, @QueryParam ("first") Long first, @QueryParam ("max") Long max) {
    if (inTestModeOrUnrestrictedClient()) {
      if (kuntaApiOrganizationIdParam == null || first == null || max == null) {
        return Response.status(Status.BAD_REQUEST).build();
      }
      
      OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(kuntaApiOrganizationIdParam);
      SearchResult<Service> searchResult = serviceController.searchServices(organizationId, null, null, null, null, null, null, ServiceSortBy.NATURAL, SortDir.DESC, first, max);
      if (searchResult != null) {
        for (Service service : searchResult.getResult()) {
          ServiceId serviceId = kuntaApiIdFactory.createServiceId(service.getId());
          if (serviceId != null) {
            ServiceId ptvServiceId = idController.translateServiceId(serviceId, PtvConsts.IDENTIFIER_NAME);
            if (ptvServiceId != null) {
              serviceIdTaskQueue.enqueueTask(new IdTask<ServiceId>(true, Operation.UPDATE, ptvServiceId));
            } else {
              logger.severe("Could not find ptv id");
            }
          } else {
            logger.severe("Could not find kunta api id");
          }
        }
      }
      
      return Response.ok("ok").build();
    }
    
    return Response.status(Status.FORBIDDEN).build();
  }
  
  @GET
  @Path ("/utils/ptv/organizationTasks")
  @Produces (MediaType.TEXT_PLAIN)
  @SuppressWarnings ("squid:S3776")
  public Response utilsPtvOrganizationTasks(@QueryParam ("first") Integer first, @QueryParam ("max") Integer max) {
    if (inTestModeOrUnrestrictedClient()) {
      List<OrganizationId> organizationIds  = identifierController.listOrganizationIdsBySource(PtvConsts.IDENTIFIER_NAME, first, max);
      for (OrganizationId organizationId : organizationIds) {
        organizationIdTaskQueue.enqueueTask(new IdTask<OrganizationId>(true, Operation.UPDATE, organizationId));
      }
      
      return Response.ok("ok").build();
    }
    
    return Response.status(Status.FORBIDDEN).build();
  }
  
  @GET
  @Path ("/utils/management/pageTasks")
  @Produces (MediaType.TEXT_PLAIN)
  @SuppressWarnings ("squid:S3776")
  public Response utilsManagementPageTasks(@QueryParam ("first") Integer first, @QueryParam ("max") Integer max) {
    if (inTestModeOrUnrestrictedClient()) {
      if (first == null || max == null) {
        return Response.status(Status.BAD_REQUEST).build();
      }
      
      List<PageId> pageIds = identifierController.listPageIdsBySource(ManagementConsts.IDENTIFIER_NAME, first, max);
      for (PageId pageId : pageIds) {
        PageId managementPageId = idController.translatePageId(pageId, ManagementConsts.IDENTIFIER_NAME);
        if (managementPageId != null) {
          pageIdTaskQueue.enqueueTask(new IdTask<PageId>(true, Operation.UPDATE, managementPageId));
        } else {
          logger.severe("Could not find management id");
        }
      }
      
      return Response.ok("ok").build();
    }
    
    return Response.status(Status.FORBIDDEN).build();
  }
  
  @GET
  @Path ("/utils/management/newsTasks")
  @Produces (MediaType.TEXT_PLAIN)
  @SuppressWarnings ("squid:S3776")
  public Response utilsManagementNewsTasks(@QueryParam ("first") Integer first, @QueryParam ("max") Integer max) {
    if (inTestModeOrUnrestrictedClient()) {
      if (first == null || max == null) {
        return Response.status(Status.BAD_REQUEST).build();
      }
      
      List<NewsArticleId> newsArticleIds = identifierController.listNewsArticleIdsBySource(ManagementConsts.IDENTIFIER_NAME, first, max);
      for (NewsArticleId newsArticleId : newsArticleIds) {
        NewsArticleId managementNewsArticleId = idController.translateNewsArticleId(newsArticleId, ManagementConsts.IDENTIFIER_NAME);
        if (managementNewsArticleId != null) {
          newsArticleIdTaskQueue.enqueueTask(new IdTask<NewsArticleId>(true, Operation.UPDATE, managementNewsArticleId));
        } else {
          logger.severe("Could not find management id");
        }
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
      for (AbstractKuntaApiTaskQueue<?> taskQueue : taskQueues) {
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
    
    for (IdDiscoverJob idDiscoverJob : idDiscoverJobs) {
      overallHealth = minHealth(overallHealth, idDiscoverJob.getHealth());
      updaterDetails.addUpdaterState(idDiscoverJob);
    }

    for (EntityDiscoverJob<?> entityUpdater : entityDiscoverJobs) {
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

  @SuppressWarnings("unchecked")
  private <T extends BaseId> void createServiceChannelUpdateTask(T serviceChannelId) {  
    T ptvServiceChannelId = (T) idController.translateId(serviceChannelId, PtvConsts.IDENTIFIER_NAME);
    if (ptvServiceChannelId != null) {
      Identifier identifier = identifierController.findIdentifierById(ptvServiceChannelId);
      if (identifier != null) {
        Long orderIndex = identifier.getOrderIndex();
        serviceChannelTasksQueue.enqueueTask(new ServiceChannelUpdateTask(true, ptvServiceChannelId.getId(), orderIndex));
      } else {
        logger.severe("Could not find identifier");
      }
    } else {
      logger.severe("Could not find ptv id");
    }
  }
  
  private class UpdaterDetails {
    
    List<String> details = new ArrayList<>();
    int okCount = 0;
    int unknownCount = 0;
    int warningCount = 0;
    int criticalCount= 0;
    
    public void addUpdaterState(AbstractDiscoverJob updater) {
      UpdaterHealth updaterHealth = updater.getHealth();
      
      if (updaterHealth == UpdaterHealth.UNKNOWN) {
        details.add(String.format("DiscoverJobInitializer %s health is %s", updater.getName(), updaterHealth));
      } else if (updaterHealth != UpdaterHealth.OK) {
        details.add(String.format("DiscoverJobInitializer %s health is %s (%d ms since last run)", updater.getName(), updaterHealth, updater.getSinceLastRun()));
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
