package fi.metatavu.kuntaapi.server.rest;

import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.List;

import javax.ejb.Stateful;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import fi.metatavu.kuntaapi.server.controllers.AnnouncementController;
import fi.metatavu.kuntaapi.server.controllers.BannerController;
import fi.metatavu.kuntaapi.server.controllers.ClientContainer;
import fi.metatavu.kuntaapi.server.controllers.ContactController;
import fi.metatavu.kuntaapi.server.controllers.EmergencyController;
import fi.metatavu.kuntaapi.server.controllers.EnvironmentalWarningController;
import fi.metatavu.kuntaapi.server.controllers.EventController;
import fi.metatavu.kuntaapi.server.controllers.FileController;
import fi.metatavu.kuntaapi.server.controllers.FragmentController;
import fi.metatavu.kuntaapi.server.controllers.HttpCacheController;
import fi.metatavu.kuntaapi.server.controllers.IncidentController;
import fi.metatavu.kuntaapi.server.controllers.MenuController;
import fi.metatavu.kuntaapi.server.controllers.NewsController;
import fi.metatavu.kuntaapi.server.controllers.OrganizationController;
import fi.metatavu.kuntaapi.server.controllers.PageController;
import fi.metatavu.kuntaapi.server.controllers.PublicTransportController;
import fi.metatavu.kuntaapi.server.controllers.SecurityController;
import fi.metatavu.kuntaapi.server.controllers.ShortlinkController;
import fi.metatavu.kuntaapi.server.controllers.TileController;
import fi.metatavu.kuntaapi.server.id.AnnouncementId;
import fi.metatavu.kuntaapi.server.id.AttachmentId;
import fi.metatavu.kuntaapi.server.id.BannerId;
import fi.metatavu.kuntaapi.server.id.ContactId;
import fi.metatavu.kuntaapi.server.id.EmergencyId;
import fi.metatavu.kuntaapi.server.id.EnvironmentalWarningId;
import fi.metatavu.kuntaapi.server.id.EventId;
import fi.metatavu.kuntaapi.server.id.FileId;
import fi.metatavu.kuntaapi.server.id.FragmentId;
import fi.metatavu.kuntaapi.server.id.IncidentId;
import fi.metatavu.kuntaapi.server.id.JobId;
import fi.metatavu.kuntaapi.server.id.MenuId;
import fi.metatavu.kuntaapi.server.id.MenuItemId;
import fi.metatavu.kuntaapi.server.id.NewsArticleId;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.id.PageId;
import fi.metatavu.kuntaapi.server.id.PublicTransportAgencyId;
import fi.metatavu.kuntaapi.server.id.PublicTransportRouteId;
import fi.metatavu.kuntaapi.server.id.PublicTransportScheduleId;
import fi.metatavu.kuntaapi.server.id.PublicTransportStopId;
import fi.metatavu.kuntaapi.server.id.PublicTransportStopTimeId;
import fi.metatavu.kuntaapi.server.id.PublicTransportTripId;
import fi.metatavu.kuntaapi.server.id.ShortlinkId;
import fi.metatavu.kuntaapi.server.id.TileId;
import fi.metatavu.kuntaapi.server.integrations.AnnouncementProvider;
import fi.metatavu.kuntaapi.server.integrations.AnnouncementProvider.AnnouncementOrder;
import fi.metatavu.kuntaapi.server.integrations.AnnouncementProvider.AnnouncementOrderDirection;
import fi.metatavu.kuntaapi.server.integrations.AttachmentData;
import fi.metatavu.kuntaapi.server.integrations.ContactSortBy;
import fi.metatavu.kuntaapi.server.integrations.EmergencySortBy;
import fi.metatavu.kuntaapi.server.integrations.EnvironmentalWarningSortBy;
import fi.metatavu.kuntaapi.server.integrations.EventProvider;
import fi.metatavu.kuntaapi.server.integrations.IncidentSortBy;
import fi.metatavu.kuntaapi.server.integrations.KuntaApiConsts;
import fi.metatavu.kuntaapi.server.integrations.KuntaApiIdFactory;
import fi.metatavu.kuntaapi.server.integrations.NewsSortBy;
import fi.metatavu.kuntaapi.server.integrations.OrganizationSortBy;
import fi.metatavu.kuntaapi.server.integrations.PageSortBy;
import fi.metatavu.kuntaapi.server.integrations.PublicTransportStopTimeSortBy;
import fi.metatavu.kuntaapi.server.integrations.SortDir;
import fi.metatavu.kuntaapi.server.rest.model.Agency;
import fi.metatavu.kuntaapi.server.rest.model.Announcement;
import fi.metatavu.kuntaapi.server.rest.model.Attachment;
import fi.metatavu.kuntaapi.server.rest.model.Banner;
import fi.metatavu.kuntaapi.server.rest.model.Contact;
import fi.metatavu.kuntaapi.server.rest.model.Emergency;
import fi.metatavu.kuntaapi.server.rest.model.EnvironmentalWarning;
import fi.metatavu.kuntaapi.server.rest.model.Event;
import fi.metatavu.kuntaapi.server.rest.model.FileDef;
import fi.metatavu.kuntaapi.server.rest.model.Fragment;
import fi.metatavu.kuntaapi.server.rest.model.Incident;
import fi.metatavu.kuntaapi.server.rest.model.Job;
import fi.metatavu.kuntaapi.server.rest.model.LocalizedValue;
import fi.metatavu.kuntaapi.server.rest.model.Menu;
import fi.metatavu.kuntaapi.server.rest.model.MenuItem;
import fi.metatavu.kuntaapi.server.rest.model.NewsArticle;
import fi.metatavu.kuntaapi.server.rest.model.Organization;
import fi.metatavu.kuntaapi.server.rest.model.OrganizationSetting;
import fi.metatavu.kuntaapi.server.rest.model.Page;
import fi.metatavu.kuntaapi.server.rest.model.Route;
import fi.metatavu.kuntaapi.server.rest.model.Schedule;
import fi.metatavu.kuntaapi.server.rest.model.Shortlink;
import fi.metatavu.kuntaapi.server.rest.model.Stop;
import fi.metatavu.kuntaapi.server.rest.model.StopTime;
import fi.metatavu.kuntaapi.server.rest.model.Tile;
import fi.metatavu.kuntaapi.server.rest.model.Trip;
import fi.metatavu.kuntaapi.server.system.OrganizationSettingProvider;

/**
 * REST Service implementation
 * 
 * @author Antti Leppä
 * @author Heikki Kurhinen
 */
@RequestScoped
@Stateful
@SuppressWarnings ("squid:S3306")
public class OrganizationsApiImpl extends OrganizationsApi {
  
  private static final String INVALID_VALUE_FOR_SORT_DIR = "Invalid value for sortDir";
  private static final String INVALID_VALUE_FOR_SORT_BY = "Invalid value for sortBy";
  private static final String INVALID_SETTING_ID = "Invalid setting id";
  private static final String MAX_RESULTS_MUST_BY_A_POSITIVE_INTEGER = "maxResults must by a positive integer";
  private static final String FIRST_RESULT_MUST_BY_A_POSITIVE_INTEGER = "firstResult must by a positive integer";
  private static final String NOT_FOUND = "Not Found";
  private static final String FORBIDDEN = "Forbidden";
  private static final String INTERNAL_SERVER_ERROR = "Internal Server Error";
  
  @Inject
  private OrganizationSettingProvider organizationSettingProvider;
  
  @Inject
  private OrganizationController organizationController;
  
  @Inject
  private PageController pageController;

  @Inject
  private FileController fileController;

  @Inject
  private FragmentController fragmentController;

  @Inject
  private MenuController menuController;
  
  @Inject
  private BannerController bannerController;
  
  @Inject
  private AnnouncementController announcementController;

  @Inject
  private TileController tileController;

  @Inject
  private NewsController newsController;
  
  @Inject
  private EventController eventController;

  @Inject
  private SecurityController securityController;

  @Inject
  private ContactController contactController;
  
  @Inject
  private ShortlinkController shortlinkController;

  @Inject
  private IncidentController incidentController;

  @Inject
  private EmergencyController emergencyController;
  
  @Inject
  private KuntaApiIdFactory kuntaApiIdFactory;
  
  @Inject
  private ClientContainer clientContainer;

  @Inject
  private HttpCacheController httpCacheController;

  @Inject
  private PublicTransportController publicTransportController;

  @Inject
  private EnvironmentalWarningController environmentalWarningController;
  
  @Inject
  private RestResponseBuilder restResponseBuilder;
  
  @Override
  public Response listOrganizations(String businessName, String businessCode, String search, String sortByParam,
      String sortDirParam, Long firstResult, Long maxResults, Request request) {
    
    Response validateResponse = validateListLimitParams(firstResult, maxResults);
    if (validateResponse != null) {
      return validateResponse;
    }
    
    OrganizationSortBy sortBy = resolveOrganizationSortBy(sortByParam);
    if (sortBy == null) {
      return createBadRequest(INVALID_VALUE_FOR_SORT_BY);
    }
    
    SortDir sortDir = resolveSortDir(sortDirParam);
    if (sortDir == null) {
      return createBadRequest(INVALID_VALUE_FOR_SORT_DIR);
    }
    
    if (search != null || businessCode != null || businessName != null) {
      return restResponseBuilder.buildResponse(organizationController.searchOrganizations(search, businessName, businessCode, sortBy, sortDir, firstResult, maxResults), request);
    } else {
      return restResponseBuilder.buildResponse(organizationController.listOrganizations(firstResult, maxResults), null, request);
    }
  }
  
  @Override
  public Response findOrganization(String organizationIdParam, @Context Request request) {
	  OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
  	if (organizationId == null) {
  	  return createNotFound(NOT_FOUND);
  	}
  	
  	Response notModified = restResponseBuilder.getNotModified(request, organizationId);
  	if (notModified != null) {
  	  return notModified;
  	}
  	
  	Organization organization = organizationController.findOrganization(organizationId);
  	if (organization != null) {
      return restResponseBuilder.sendModified(organization, organization.getId());
    }
      
    return createNotFound(NOT_FOUND);
  }
  
  @Override
  public Response findOrganizationEvent(String organizationIdParam, String eventIdParam, @Context Request request) {
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    EventId eventId = toEventId(organizationId, eventIdParam);
    
    Response notModified = restResponseBuilder.getNotModified(request, eventId);
    if (notModified != null) {
      return notModified;
    }
    
    Event event = eventController.findEvent(organizationId, eventId);
    if (event != null) {
      return restResponseBuilder.sendModified(event, event.getId());
    }    
    
    return createNotFound(NOT_FOUND);
  }

  @Override
  public Response findOrganizationEventImage(String organizationIdParam, String eventIdParam, String imageIdParam, @Context Request request) {
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    EventId eventId = toEventId(organizationId, eventIdParam);
    AttachmentId attachmentId = new AttachmentId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, imageIdParam);
    
    Response notModified = restResponseBuilder.getNotModified(request, attachmentId);
    if (notModified != null) {
      return notModified;
    }
    
    Attachment attachment = eventController.findEventImage(organizationId, eventId, attachmentId);
    if (attachment != null) {
      return restResponseBuilder.sendModified(attachment, attachment.getId());
    }    
    
    return createNotFound(NOT_FOUND);
  }

  @Override
  public Response getOrganizationEventImageData(String organizationIdParam, String eventIdParam, String imageIdParam, Integer size, @Context Request request) {
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    EventId eventId = toEventId(organizationId, eventIdParam);
    AttachmentId attachmentId = new AttachmentId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, imageIdParam);
    
    
    Response notModified = restResponseBuilder.getNotModified(request, attachmentId);
    if (notModified != null) {
      return notModified;
    }
    
    AttachmentData attachmentData = eventController.getEventImageData(size, organizationId, eventId, attachmentId);
    if (attachmentData != null) {
      return httpCacheController.streamModified(attachmentData.getData(), attachmentData.getType(), attachmentId);
    }
    
    return Response.status(Status.NOT_FOUND).build();
  }

  @Override
  public Response listOrganizationEventImages(String organizationIdParam, String eventIdParam, @Context Request request) {
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    EventId eventId = toEventId(organizationId, eventIdParam);
    
    List<Attachment> result = eventController.listEventImages(organizationId, eventId);
    List<String> ids = httpCacheController.getEntityIds(result);
    Response notModified = httpCacheController.getNotModified(request, ids);
    if (notModified != null) {
      return notModified;
    }

    return httpCacheController.sendModified(result, ids);
  }

  @Override
  public Response listOrganizationEvents(String organizationIdParam, 
      String startBefore, String startAfter,
      String endBefore, String endAfter,
      Integer firstResult, Integer maxResults,
      String orderBy, String orderDir, @Context Request request) {
 
    Response validateResponse = validateListLimitParams(firstResult, maxResults);
    if (validateResponse != null) {
      return validateResponse;
    }
    
    EventProvider.EventOrder order = EventProvider.EventOrder.START_DATE;
    EventProvider.EventOrderDirection orderDirection = EventProvider.EventOrderDirection.DESCENDING;
    
    if (StringUtils.isNotBlank(orderBy)) {
      order = EnumUtils.getEnum(EventProvider.EventOrder.class, orderBy);
      if (order == null) {
        return createBadRequest(String.format("Invalid event order %s", orderBy));
      }
    }
    
    if (StringUtils.isNotBlank(orderDir)) {
      orderDirection = EnumUtils.getEnum(EventProvider.EventOrderDirection.class, orderDir);
      if (orderDirection == null) {
        return createBadRequest(String.format("Invalid event order direction %s", orderDir));
      }
    }
    
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    
    List<Event> result = eventController.listEvents(getDateTime(startBefore), getDateTime(startAfter), getDateTime(endBefore), getDateTime(endAfter), 
        firstResult, maxResults, order, orderDirection, organizationId);

    List<String> ids = httpCacheController.getEntityIds(result);
    Response notModified = httpCacheController.getNotModified(request, ids);
    if (notModified != null) {
      return notModified;
    }

    return httpCacheController.sendModified(result, ids);
  }
  
  /* News */
  
  @Override
  public Response listOrganizationNews(String organizationIdParam, String slug, String tag, String publishedBefore,
      String publishedAfter, String search, String sortByParam, String sortDirParam, Integer firstResult, Integer maxResults, Request request) {
    
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    
    NewsSortBy sortBy = resolveNewsSortBy(sortByParam);
    if (sortBy == null) {
      return createBadRequest(INVALID_VALUE_FOR_SORT_BY);
    }
    
    SortDir sortDir = resolveSortDir(sortDirParam);
    if (sortDir == null) {
      return createBadRequest(INVALID_VALUE_FOR_SORT_DIR);
    }
    
    return restResponseBuilder.buildResponse(newsController.searchNewsArticles(organizationId, search, tag, slug, getDateTime(publishedBefore), getDateTime(publishedAfter), 
        sortBy, sortDir, firstResult, maxResults), request);
  }

  @Override
  public Response findOrganizationNewsArticle(String organizationIdParam, String newsArticleIdParam, @Context Request request) {
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    NewsArticleId newsArticleId = toNewsArticleId(organizationId, newsArticleIdParam);
    
    Response notModified = restResponseBuilder.getNotModified(request, newsArticleId);
    if (notModified != null) {
      return notModified;
    }
    
    NewsArticle newsArticle = newsController.findNewsArticle(organizationId, newsArticleId);
    if (newsArticle != null) {
      return restResponseBuilder.sendModified(newsArticle, newsArticle.getId());
    }
    
    return createNotFound(NOT_FOUND);
  }

  @Override
  public Response findOrganizationNewsArticleImage(String organizationIdParam, String newsArticleIdParam, String imageIdParam, @Context Request request) {
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    NewsArticleId newsArticleId = toNewsArticleId(organizationId, newsArticleIdParam);
    AttachmentId attachmentId = toAttachmentId(organizationId, imageIdParam);
    
    Response notModified = restResponseBuilder.getNotModified(request, attachmentId);
    if (notModified != null) {
      return notModified;
    }
    
    Attachment attachment = newsController.findNewsArticleImage(organizationId, newsArticleId, attachmentId);
    if (attachment != null) {
      return restResponseBuilder.sendModified(attachment, attachment.getId());
    }
    
    return createNotFound(NOT_FOUND);
  }

  @Override
  public Response getOrganizationNewsArticleImageData(String organizationIdParam, String newsArticleIdParam, String imageIdParam, Integer size, @Context Request request) {
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    NewsArticleId newsArticleId = toNewsArticleId(organizationId, newsArticleIdParam);
    AttachmentId attachmentId = toAttachmentId(organizationId, imageIdParam);
   
    Response notModified = restResponseBuilder.getNotModified(request, attachmentId);
    if (notModified != null) {
      return notModified;
    }
    
    AttachmentData attachmentData = newsController.getNewsArticleImageData(organizationId, newsArticleId, attachmentId, size);
    if (attachmentData != null) {
      return httpCacheController.streamModified(attachmentData.getData(), attachmentData.getType(), attachmentId);
    }
    
    return Response.status(Status.NOT_FOUND).build();
  }

  @Override
  public Response listOrganizationNewsArticleImages(String organizationIdParam, String newsArticleIdParam, @Context Request request) {
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    NewsArticleId newsArticleId = toNewsArticleId(organizationId, newsArticleIdParam);
    
    List<Attachment> result = newsController.listNewsArticleImages(organizationId, newsArticleId);
    List<String> ids = httpCacheController.getEntityIds(result);
    Response notModified = httpCacheController.getNotModified(request, ids);
    if (notModified != null) {
      return notModified;
    }

    return httpCacheController.sendModified(result, ids);
  }

  /* Banners */
  
  @Override
  public Response listOrganizationBanners(String organizationIdParam, @Context Request request) {
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    
    List<Banner> result = bannerController.listBanners(organizationId);
    List<String> ids = httpCacheController.getEntityIds(result);
    Response notModified = httpCacheController.getNotModified(request, ids);
    if (notModified != null) {
      return notModified;
    }

    return httpCacheController.sendModified(result, ids);
  }

  @Override
  public Response findOrganizationBanner(String organizationIdParam, String bannerIdParam, @Context Request request) {
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    BannerId bannerId = toBannerId(organizationId, bannerIdParam);
    
    Response notModified = restResponseBuilder.getNotModified(request, bannerId);
    if (notModified != null) {
      return notModified;
    }

    Banner banner = bannerController.findBanner(organizationId, bannerId);
    if (banner != null) {
      return restResponseBuilder.sendModified(banner, banner.getId());
    }
    
    return createNotFound(NOT_FOUND);
  }

  @Override
  public Response listOrganizationBannerImages(String organizationIdParam, String bannerIdParam, @Context Request request) {
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    BannerId bannerId = toBannerId(organizationId, bannerIdParam);
    
    List<Attachment> result = bannerController.listBannerImages(organizationId, bannerId);
    List<String> ids = httpCacheController.getEntityIds(result);
    Response notModified = httpCacheController.getNotModified(request, ids);
    if (notModified != null) {
      return notModified;
    }

    return httpCacheController.sendModified(result, ids);
  }

  @Override
  public Response findOrganizationBannerImage(String organizationIdParam, String bannerIdParam, String imageIdParam, @Context Request request) {
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    BannerId bannerId = toBannerId(organizationId, bannerIdParam);
    AttachmentId attachmentId = toAttachmentId(organizationId, imageIdParam);
    
    Response notModified = restResponseBuilder.getNotModified(request, attachmentId);
    if (notModified != null) {
      return notModified;
    }

    Attachment attachment = bannerController.findBannerImage(organizationId, bannerId, attachmentId);
    if (attachment != null) {
      return restResponseBuilder.sendModified(attachment, attachment.getId());
    }
    
    return createNotFound(NOT_FOUND);
  }

  @Override
  public Response getOrganizationBannerImageData(String organizationIdParam, String bannerIdParam, String imageIdParam, Integer size, @Context Request request) {
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    BannerId bannerId = toBannerId(organizationId, bannerIdParam);
    AttachmentId attachmentId = toAttachmentId(organizationId, imageIdParam);
    
    Response notModified = restResponseBuilder.getNotModified(request, attachmentId);
    if (notModified != null) {
      return notModified;
    }
    
    AttachmentData attachmentData = bannerController.getBannerImageData(organizationId, bannerId, attachmentId, size);
    if (attachmentData != null) {
      return httpCacheController.streamModified(attachmentData.getData(), attachmentData.getType(), attachmentId);
    }
    
    return Response.status(Status.NOT_FOUND).build();
  }
  
  /* Tiles */
  
  @Override
  public Response listOrganizationTiles(String organizationIdParam, @Context Request request) {
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    
    List<Tile> result = tileController.listTiles(organizationId);
    List<String> ids = httpCacheController.getEntityIds(result);
    Response notModified = httpCacheController.getNotModified(request, ids);
    if (notModified != null) {
      return notModified;
    }

    return httpCacheController.sendModified(result, ids);
  }

  @Override
  public Response findOrganizationTile(String organizationIdParam, String tileIdParam, @Context Request request) {
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    TileId tileId = toTileId(organizationId, tileIdParam);
    
    Response notModified = restResponseBuilder.getNotModified(request, tileId);
    if (notModified != null) {
      return notModified;
    }

    Tile tile = tileController.findTile(organizationId, tileId);
    if (tile != null) {
      return restResponseBuilder.sendModified(tile, tile.getId());
    }
    
    return createNotFound(NOT_FOUND);
  }

  @Override
  public Response listOrganizationTileImages(String organizationIdParam, String tileIdParam, @Context Request request) {
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    TileId tileId = toTileId(organizationId, tileIdParam);
    
    List<Attachment> result = tileController.listTileImages(organizationId, tileId);
    List<String> ids = httpCacheController.getEntityIds(result);
    Response notModified = httpCacheController.getNotModified(request, ids);
    if (notModified != null) {
      return notModified;
    }

    return httpCacheController.sendModified(result, ids);
  }

  @Override
  public Response findOrganizationTileImage(String organizationIdParam, String tileIdParam, String imageIdParam, @Context Request request) {
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    TileId tileId = toTileId(organizationId, tileIdParam);
    AttachmentId attachmentId = toAttachmentId(organizationId, imageIdParam);
    
    Response notModified = restResponseBuilder.getNotModified(request, attachmentId);
    if (notModified != null) {
      return notModified;
    }

    Attachment attachment = tileController.findTileImage(organizationId, tileId, attachmentId);
    if (attachment != null) {
      return restResponseBuilder.sendModified(attachment, attachment.getId());
    }
    
    return createNotFound(NOT_FOUND);
  }

  @Override
  public Response getOrganizationTileImageData(String organizationIdParam, String tileIdParam, String imageIdParam, Integer size, @Context Request request) {
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    TileId tileId = toTileId(organizationId, tileIdParam);
    AttachmentId attachmentId = toAttachmentId(organizationId, imageIdParam);
    
    Response notModified = restResponseBuilder.getNotModified(request, attachmentId);
    if (notModified != null) {
      return notModified;
    }
    
    AttachmentData attachmentData = tileController.getTileImageData(organizationId, tileId, attachmentId, size);
    if (attachmentData != null) {
      return httpCacheController.streamModified(attachmentData.getData(), attachmentData.getType(), attachmentId);
    }
    
    return Response.status(Status.NOT_FOUND).build();
  }

  @Override
  @SuppressWarnings("squid:MethodCyclomaticComplexity")
  public Response createOrganizationSetting(String organizationIdParam, OrganizationSetting setting, @Context Request request) {
    if (!securityController.isUnrestrictedClient(clientContainer.getClient())) {
      return createForbidden(FORBIDDEN);
    }
    
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    if (organizationId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    if (StringUtils.isBlank(setting.getKey())) {
      return createBadRequest("Key is required");
    }

    if (StringUtils.isBlank(setting.getValue())) {
      return createBadRequest("Value is required");
    }
    
    List<OrganizationSetting> organizationSettings = organizationSettingProvider.listOrganizationSettings(organizationId, setting.getKey());
    if (!organizationSettings.isEmpty()) {
      return createBadRequest("Setting already exists");
    }
    
    OrganizationSetting organizationSetting = organizationSettingProvider.createOrganizationSetting(organizationId, setting.getKey(), setting.getValue());
    if (organizationSetting == null) {
      return createInternalServerError(INTERNAL_SERVER_ERROR);
    }
    
    return Response.ok()
        .entity(organizationSetting)
        .build();
  }

  @Override
  public Response listOrganizationSettings(String organizationIdParam, String key, @Context Request request) {
    if (!securityController.isUnrestrictedClient(clientContainer.getClient())) {
      return createForbidden(FORBIDDEN);
    }
    
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    if (organizationId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    List<OrganizationSetting> result = organizationSettingProvider.listOrganizationSettings(organizationId, key);

    return Response.ok()
        .entity(result)
        .build();
  }
  
  @Override
  public Response findOrganizationSetting(String organizationIdParam, String settingIdParam, @Context Request request) {
    if (!securityController.isUnrestrictedClient(clientContainer.getClient())) {
      return createForbidden(FORBIDDEN);
    }
    
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    if (organizationId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    if (!StringUtils.isNumeric(settingIdParam)) {
      return createBadRequest(INVALID_SETTING_ID);
    }
    
    Long settingId = NumberUtils.createLong(settingIdParam); 
    
    OrganizationSetting organizationSetting = organizationSettingProvider.findOrganizationSetting(organizationId, settingId);
    if (organizationSetting == null) {
      return createNotFound(NOT_FOUND);
    }

    return Response.ok()
        .entity(organizationSetting)
        .build();
  }
  
  @Override
  @SuppressWarnings ("squid:MethodCyclomaticComplexity")
  public Response updateOrganizationSetting(String organizationIdParam, String settingIdParam, OrganizationSetting setting, @Context Request request) {
    if (!securityController.isUnrestrictedClient(clientContainer.getClient())) {
      return createForbidden(FORBIDDEN);
    }
    
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    if (organizationId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    if (StringUtils.isBlank(setting.getKey())) {
      return createBadRequest("Key is required");
    }

    if (StringUtils.isBlank(setting.getValue())) {
      return createBadRequest("Value is required");
    }
    
    if (!StringUtils.isNumeric(settingIdParam)) {
      return createBadRequest(INVALID_SETTING_ID);
    }
    
    Long settingId = NumberUtils.createLong(settingIdParam); 

    OrganizationSetting organizationSetting = organizationSettingProvider.findOrganizationSetting(organizationId, settingId);
    if (organizationSetting == null) {
      return createNotFound(NOT_FOUND);
    }

    if (!StringUtils.equals(organizationSetting.getKey(), setting.getKey())) {
      return createBadRequest("Cannot update setting key");
    }
    
    OrganizationSetting updatedOrganizationSetting = organizationSettingProvider.updateOrganizationSetting(settingId, setting.getValue());
    
    if (updatedOrganizationSetting == null) {
      return createNotFound(NOT_FOUND);
    }
    
    return Response.ok()
        .entity(updatedOrganizationSetting)
        .build();
  }

  @Override
  public Response deleteOrganizationSetting(String organizationIdParam, String settingIdParam, @Context Request request) {
    if (!securityController.isUnrestrictedClient(clientContainer.getClient())) {
      return createForbidden(FORBIDDEN);
    }
    
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    if (organizationId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    if (!StringUtils.isNumeric(settingIdParam)) {
      return createBadRequest(INVALID_SETTING_ID);
    }
    
    Long settingId = NumberUtils.createLong(settingIdParam); 
    
    OrganizationSetting organizationSetting = organizationSettingProvider.findOrganizationSetting(organizationId, settingId);
    if (organizationSetting == null) {
      return createNotFound(NOT_FOUND);
    }
    
    organizationSettingProvider.deleteOrganizationSetting(settingId);
    
    return Response.noContent()
        .build();
  }

  /* Pages */
  
  @Override
  public Response listOrganizationPages(String organizationIdParam, String parentIdParam, String path, String search,
      String sortByParam, String sortDirParam, Long firstResult, Long maxResults, Request request) {
    
    Response validateResponse = validateListLimitParams(firstResult, maxResults);
    if (validateResponse != null) {
      return validateResponse;
    }
    
    PageSortBy sortBy = resolvePageSortBy(sortByParam);
    if (sortBy == null) {
      return createBadRequest(INVALID_VALUE_FOR_SORT_BY);
    }
    
    SortDir sortDir = resolveSortDir(sortDirParam);
    if (sortDir == null) {
      return createBadRequest(INVALID_VALUE_FOR_SORT_DIR);
    }
    
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    if (organizationId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    if (search != null && (parentIdParam != null || path != null)) {
      return createNotImplemented("Search parameter can not be combined with path or parentId parameters");
    }
    
    boolean onlyRootPages = StringUtils.equals("ROOT", parentIdParam);
    PageId parentId = onlyRootPages ? null : toPageId(organizationId, parentIdParam);

    if (path != null) {
      return restResponseBuilder.buildResponse(pageController.listPages(organizationId, path, onlyRootPages, parentId, firstResult, maxResults), null, request);
    } else {
      return restResponseBuilder.buildResponse(pageController.searchPages(organizationId, search, sortBy, sortDir, onlyRootPages, parentId, firstResult, maxResults), request);
    }
  }

  @Override
  public Response findOrganizationPage(String organizationIdParam, String pageIdParam, @Context Request request) {
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    if (organizationId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    PageId pageId = toPageId(organizationId, pageIdParam);
    if (pageId == null) {
      return createNotFound(NOT_FOUND);
    }

    Response notModified = restResponseBuilder.getNotModified(request, pageId);
    if (notModified != null) {
      return notModified;
    }
    
    Page page = pageController.findPage(organizationId, pageId);
    if (page != null) {
      return restResponseBuilder.sendModified(page, page.getId());
    }
    
    return createNotFound(NOT_FOUND);
  }
  
  @Override
  public Response findOrganizationPageContent(String organizationIdParam, String pageIdParam, @Context Request request) {
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    if (organizationId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    PageId pageId = toPageId(organizationId, pageIdParam);
    if (pageId == null) {
      return createNotFound(NOT_FOUND);
    }

    Response notModified = restResponseBuilder.getNotModified(request, pageId);
    if (notModified != null) {
      return notModified;
    }
    
    if (pageController.findPage(organizationId, pageId) != null) {
      List<LocalizedValue> pageContents = pageController.getPageContents(organizationId, pageId);
      if (pageContents != null) {
        return restResponseBuilder.sendModified(pageContents, pageId.getId());
      }
    }
    
    return createNotFound(NOT_FOUND);
  }
  

  @Override
  public Response listOrganizationPageImages(String organizationIdParam, String pageIdParam, String type, Request request) {
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    PageId pageId = toPageId(organizationId, pageIdParam);
    
    List<Attachment> result = pageController.listPageImages(organizationId, pageId, type);
    
    List<String> ids = httpCacheController.getEntityIds(result);
    Response notModified = httpCacheController.getNotModified(request, ids);
    if (notModified != null) {
      return notModified;
    }

    if (pageController.findPage(organizationId, pageId) == null) {
      return createNotFound(NOT_FOUND); 
    }
    
    return httpCacheController.sendModified(result, ids);
  }

  @Override
  public Response findOrganizationPageImage(String organizationIdParam, String pageIdParam, String imageIdParam, @Context Request request) {
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    PageId pageId = toPageId(organizationId, pageIdParam);
    AttachmentId attachmentId = toAttachmentId(organizationId, imageIdParam);
    
    Response notModified = restResponseBuilder.getNotModified(request, attachmentId);
    if (notModified != null) {
      return notModified;
    }
    
    Attachment attachment = pageController.findPageImage(organizationId, pageId, attachmentId);
    if (attachment != null) {
      return restResponseBuilder.sendModified(attachment, attachment.getId());
    }
    
    return createNotFound(NOT_FOUND);
  }

  @Override
  public Response getOrganizationPageImageData(String organizationIdParam, String pageIdParam, String imageIdParam, Integer size, @Context Request request) {
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    PageId pageId = toPageId(organizationId, pageIdParam);
    AttachmentId attachmentId = toAttachmentId(organizationId, imageIdParam);
    
    Response notModified = restResponseBuilder.getNotModified(request, attachmentId);
    if (notModified != null) {
      return notModified;
    }
    
    AttachmentData attachmentData = pageController.getPageAttachmentData(organizationId, pageId, attachmentId, size);
    if (attachmentData != null) {
      return httpCacheController.streamModified(attachmentData.getData(), attachmentData.getType(), attachmentId);
    }
    
    return Response.status(Status.NOT_FOUND).build();
  }

  @Override
  public Response deleteOrganizationPage(String organizationIdParam, String pageIdParam, Request request) {
    if (!securityController.isUnrestrictedClient(clientContainer.getClient())) {
      return createForbidden(FORBIDDEN);
    }
    
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    if (organizationId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    PageId pageId = toPageId(organizationId, pageIdParam);
    if (pageId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    if (pageController.deletePage(organizationId, pageId)) {
      return Response.noContent().build();
    }
    
    return createNotFound(NOT_FOUND);
  }
    
  /* Fragments */
  
  @Override
  public Response findOrganizationFragment(String organizationIdParam, String fragmentIdParam, Request request) {
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    if (organizationId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    FragmentId fragmentId = toFragmentId(organizationId, fragmentIdParam);
    if (fragmentId == null) {
      return createNotFound(NOT_FOUND);
    }

    Response notModified = restResponseBuilder.getNotModified(request, fragmentId);
    if (notModified != null) {
      return notModified;
    }
    
    Fragment fragment = fragmentController.findFragment(organizationId, fragmentId);
    if (fragment != null) {
      return restResponseBuilder.sendModified(fragment, fragment.getId());
    }
    
    return createNotFound(NOT_FOUND);
  }
  
  @Override
  public Response listOrganizationFragments(String organizationIdParam, String slug, Request request) {
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    if (organizationId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    List<Fragment> result = fragmentController.listFragments(organizationId, slug, null, null);
    List<String> ids = httpCacheController.getEntityIds(result);
    Response notModified = httpCacheController.getNotModified(request, ids);
    if (notModified != null) {
      return notModified;
    }

    return httpCacheController.sendModified(result, ids);
  }
  
  /* Menus */

  @Override
  public Response listOrganizationMenus(String organizationIdParam, String slug, @Context Request request) {
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    if (organizationId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    List<Menu> result = menuController.listMenus(slug, organizationId);
    List<String> ids = httpCacheController.getEntityIds(result);
    Response notModified = httpCacheController.getNotModified(request, ids);
    if (notModified != null) {
      return notModified;
    }

    return httpCacheController.sendModified(result, ids);
  }
  
  @Override
  public Response findOrganizationMenu(String organizationIdParam, String menuIdParam, @Context Request request) {
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    if (organizationId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    MenuId menuId = toMenuId(organizationId, menuIdParam);
    if (menuId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    Response notModified = restResponseBuilder.getNotModified(request, menuId);
    if (notModified != null) {
      return notModified;
    }
    
    Menu menu = menuController.findMenu(organizationId, menuId);
    if (menu != null) {
      return restResponseBuilder.sendModified(menu, menu.getId());
    }
    
    return createNotFound(NOT_FOUND);
  }
  
  /* Menu Items */

  @Override
  public Response listOrganizationMenuItems(String organizationIdParam, String menuIdParam, @Context Request request) {
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    if (organizationId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    MenuId menuId = toMenuId(organizationId, menuIdParam);
    if (menuId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    List<MenuItem> result = menuController.listMenuItems(organizationId, menuId);
    List<String> ids = httpCacheController.getEntityIds(result);
    Response notModified = httpCacheController.getNotModified(request, ids);
    if (notModified != null) {
      return notModified;
    }

    return httpCacheController.sendModified(result, ids);
  }

  @Override
  public Response findOrganizationMenuItem(String organizationIdParam, String menuIdParam, String menuItemIdParam, @Context Request request) {
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    if (organizationId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    MenuId menuId = toMenuId(organizationId, menuIdParam);
    if (menuId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    MenuItemId menuItemId = toMenuItemId(organizationId, menuItemIdParam);
    if (menuItemId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    return findOrganizationMenuItem(organizationId, menuId, menuItemId, request);
  }

  private Response findOrganizationMenuItem(OrganizationId organizationId, MenuId menuId, MenuItemId menuItemId, Request request) {
    Response notModified = restResponseBuilder.getNotModified(request, menuItemId);
    if (notModified != null) {
      return notModified;
    }
    
    MenuItem menuItem = menuController.findMenuItem(organizationId, menuId, menuItemId);
    if (menuItem != null) {
      return restResponseBuilder.sendModified(menuItem, menuItem.getId());
    }
    
    return createNotFound(NOT_FOUND);
  }
  
  /* Files */
  
  @Override
  public Response listOrganizationFiles(String organizationIdParam, String pageIdParam, String search, Long firstResult, Long maxResults, Request request) {
    Response validateResponse = validateListLimitParams(firstResult, maxResults);
    if (validateResponse != null) {
      return validateResponse;
    }
    
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    if (organizationId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    PageId pageId = toPageId(organizationId, pageIdParam);
    List<FileDef> result;
    
    if (search != null) {
      result = fileController.searchFiles(organizationId, pageId, search, firstResult, maxResults);
    } else {
      result = fileController.listFiles(organizationId, pageId, firstResult, maxResults);
    }
    
    List<String> ids = httpCacheController.getEntityIds(result);
    Response notModified = httpCacheController.getNotModified(request, ids);
    if (notModified != null) {
      return notModified;
    }

    return httpCacheController.sendModified(result, ids);
  }

  @Override
  public Response findOrganizationFile(String organizationIdParam, String fileIdParam, @Context Request request) {
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    if (organizationId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    FileId fileId = toFileId(organizationId, fileIdParam);
    if (fileId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    Response notModified = restResponseBuilder.getNotModified(request, fileId);
    if (notModified != null) {
      return notModified;
    }
    
    FileDef file = fileController.findFile(organizationId, fileId);
    if (file != null) {
      return restResponseBuilder.sendModified(file, file.getId());
    }
    
    return createNotFound(NOT_FOUND);
  }
  
  @Override
  public Response deleteOrganizationFile(String organizationIdParam, String fileIdParam, Request request) {
    if (!securityController.isUnrestrictedClient(clientContainer.getClient())) {
      return createForbidden(FORBIDDEN);
    }
    
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    if (organizationId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    FileId fileId = toFileId(organizationId, fileIdParam);
    if (fileId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    if (fileController.deleteFile(organizationId, fileId)) {
      return Response.noContent().build();
    }
    
    return createNotFound(NOT_FOUND);
  }

  @Override
  public Response getOrganizationFileData(String organizationIdParam, String fileIdParam, @Context Request request) {
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    if (organizationId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    FileId fileId = toFileId(organizationId, fileIdParam);
    if (fileId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    Response notModified = restResponseBuilder.getNotModified(request, fileId);
    if (notModified != null) {
      return notModified;
    }
    
    AttachmentData data = fileController.getFileData(organizationId, fileId);
    if (data != null) {
      return httpCacheController.streamModified(data.getData(), data.getType(), fileId);
    }
    
    return Response.status(Status.NOT_FOUND).build();
  }
  
  /* Jobs */

  @Override
  public Response findOrganizationJob(String organizationIdParam, String jobIdParam, @Context Request request) {
    return createNotImplemented("This API is moved to jobs-api.kunta-api.fi");
  }

  @Override
  public Response listOrganizationJobs(String organizationIdParam, String sortBy, String sortDir, Long firstResult, Long maxResults, @Context Request request) {
    return createNotImplemented("This API is moved to jobs-api.kunta-api.fi");
  }
  
  /* Announcements */

  @Override
  public Response findOrganizationAnnouncement(String organizationIdParam, String announcementIdParam, @Context Request request) {
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    if (organizationId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    AnnouncementId announcementId = toAnnouncementId(organizationId, announcementIdParam);
    if (announcementId == null) {
      return createNotFound(NOT_FOUND);
    }

    Response notModified = restResponseBuilder.getNotModified(request, announcementId);
    if (notModified != null) {
      return notModified;
    }
    
    Announcement announcement = announcementController.findAnnouncement(organizationId, announcementId);
    if (announcement != null) {
      return restResponseBuilder.sendModified(announcement, announcement.getId());
    }
    
    return createNotFound(NOT_FOUND);
  }
  
  @Override
  public Response listOrganizationAnnouncements(String organizationIdParam, String slug, Integer firstResult, Integer maxResults, String sortBy, String sortDir, @Context Request request) {
    Response validateResponse = validateListLimitParams(firstResult, maxResults);
    if (validateResponse != null) {
      return validateResponse;
    }
    
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    if (organizationId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    AnnouncementProvider.AnnouncementOrder order = null;
    AnnouncementProvider.AnnouncementOrderDirection orderDirection = null;
    
    if (StringUtils.isNotBlank(sortBy)) {
      order = EnumUtils.getEnum(AnnouncementProvider.AnnouncementOrder.class, sortBy);
      if (order == null) {
        return createBadRequest(INVALID_VALUE_FOR_SORT_BY);
      }
    }
    
    if (StringUtils.isNotBlank(sortDir)) {
      orderDirection = EnumUtils.getEnum(AnnouncementProvider.AnnouncementOrderDirection.class, sortDir);
      if (orderDirection == null) {
        return createBadRequest(INVALID_VALUE_FOR_SORT_DIR);
      }
    }
    
    return listOrganizationAnnouncements(request, organizationId, slug, order, orderDirection, firstResult, maxResults);
  }
  
  /* Contacts */
  
  @Override
  public Response findOrganizationContact(String organizationIdParam, String contactIdParam, Request request) {
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    if (organizationId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    ContactId contactId = toContactId(organizationId, contactIdParam);
    if (contactId == null) {
      return createNotFound(NOT_FOUND);
    }

    Response notModified = restResponseBuilder.getNotModified(request, contactId);
    if (notModified != null) {
      return notModified;
    }
    
    Contact contact = contactController.findContact(organizationId, contactId);
    if (contact != null) {
      return restResponseBuilder.sendModified(contact, contact.getId());
    }
    
    return createNotFound(NOT_FOUND);
  }
  
  @Override
  public Response listOrganizationContacts(String organizationIdParam, String search, String sortByParam, String sortDirParam,
      Long firstResult, Long maxResults, Request request) {    
    Response validateResponse = validateListLimitParams(firstResult, maxResults);
    if (validateResponse != null) {
      return validateResponse;
    }
    
    ContactSortBy sortBy = resolveContactSortBy(sortByParam);
    if (sortBy == null) {
      return createBadRequest(INVALID_VALUE_FOR_SORT_BY);
    }
    
    SortDir sortDir = resolveSortDir(sortDirParam);
    if (sortDir == null) {
      return createBadRequest(INVALID_VALUE_FOR_SORT_DIR);
    }
    
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    if (organizationId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    if (search != null) {
      return restResponseBuilder.buildResponse(contactController.searchContacts(organizationId, search, sortBy, sortDir, firstResult, maxResults), request);
    } else {
      return restResponseBuilder.buildResponse(contactController.listContacts(organizationId, firstResult, maxResults), null, request);
    }
  }
  
  /* Public transport */
  
  @Override
  public Response findOrganizationPublicTransportAgency(String organizationIdParam, String agencyIdParam, Request request) {
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    if (organizationId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    PublicTransportAgencyId agencyId = toPublicTransportAgencyId(organizationId, agencyIdParam);
    if (agencyId == null) {
      return createNotFound(NOT_FOUND);
    }

    Response notModified = restResponseBuilder.getNotModified(request, agencyId);
    if (notModified != null) {
      return notModified;
    }
    
    Agency agency = publicTransportController.findAgency(organizationId, agencyId);
    if (agency != null) {
      return restResponseBuilder.sendModified(agency, agency.getId());
    }
    
    return createNotFound(NOT_FOUND);
  }

  @Override
  public Response findOrganizationPublicTransportRoute(String organizationIdParam, String routeIdParam, Request request) {
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    if (organizationId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    PublicTransportRouteId routeId = toPublicTransportRouteId(organizationId, routeIdParam);
    if (routeId == null) {
      return createNotFound(NOT_FOUND);
    }

    Response notModified = restResponseBuilder.getNotModified(request, routeId);
    if (notModified != null) {
      return notModified;
    }
    
    Route route = publicTransportController.findRoute(organizationId, routeId);
    if (route != null) {
      return restResponseBuilder.sendModified(route, route.getId());
    }
    
    return createNotFound(NOT_FOUND);
  }

  @Override
  public Response findOrganizationPublicTransportSchedule(String organizationIdParam, String scheduleIdParam, Request request) {
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    if (organizationId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    PublicTransportScheduleId scheduleId = toPublicTransportScheduleId(organizationId, scheduleIdParam);
    if (scheduleId == null) {
      return createNotFound(NOT_FOUND);
    }

    Response notModified = restResponseBuilder.getNotModified(request, scheduleId);
    if (notModified != null) {
      return notModified;
    }
    
    Schedule schedule = publicTransportController.findSchedule(organizationId, scheduleId);
    if (schedule != null) {
      return restResponseBuilder.sendModified(schedule, schedule.getId());
    }
    
    return createNotFound(NOT_FOUND);
  }

  @Override
  public Response listOrganizationPublicTransportAgencies(String organizationIdParam, Request request) {
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    if (organizationId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    List<Agency> result = publicTransportController.listAgencies(organizationId, null, null);
    List<String> ids = httpCacheController.getEntityIds(result);
    Response notModified = httpCacheController.getNotModified(request, ids);
    if (notModified != null) {
      return notModified;
    }

    return httpCacheController.sendModified(result, ids);
  }
  
  @Override
  public Response findOrganizationPublicTransportStop(String organizationIdParam, String stopIdParam, Request request) {
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    if (organizationId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    PublicTransportStopId stopId = toPublicTransportStopId(organizationId, stopIdParam);
    if (stopId == null) {
      return createNotFound(NOT_FOUND);
    }

    Response notModified = restResponseBuilder.getNotModified(request, stopId);
    if (notModified != null) {
      return notModified;
    }
    
    Stop stop = publicTransportController.findStop(organizationId, stopId);
    if (stop != null) {
      return restResponseBuilder.sendModified(stop, stop.getId());
    }
    
    return createNotFound(NOT_FOUND);
  }

  @Override
  public Response findOrganizationPublicTransportStopTime(String organizationIdParam, String stopTimeIdParam, Request request) {
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    if (organizationId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    PublicTransportStopTimeId stopTimeId = toPublicTransportStopTimeId(organizationId, stopTimeIdParam);
    if (stopTimeId == null) {
      return createNotFound(NOT_FOUND);
    }

    Response notModified = restResponseBuilder.getNotModified(request, stopTimeId);
    if (notModified != null) {
      return notModified;
    }
    
    StopTime stopTime = publicTransportController.findStopTime(organizationId, stopTimeId);
    if (stopTime != null) {
      return restResponseBuilder.sendModified(stopTime, stopTime.getId());
    }
    
    return createNotFound(NOT_FOUND);
  }

  @Override
  public Response findOrganizationPublicTransportTrip(String organizationIdParam, String tripIdParam, Request request) {
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    if (organizationId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    PublicTransportTripId tripId = toPublicTransportTripId(organizationId, tripIdParam);
    if (tripId == null) {
      return createNotFound(NOT_FOUND);
    }

    Response notModified = restResponseBuilder.getNotModified(request, tripId);
    if (notModified != null) {
      return notModified;
    }
    
    Trip trip = publicTransportController.findTrip(organizationId, tripId);
    if (trip != null) {
      return restResponseBuilder.sendModified(trip, trip.getId());
    }
    
    return createNotFound(NOT_FOUND);
  }

  @Override
  public Response listOrganizationPublicTransportStopTimes(String organizationIdParam, String stopIdParam, Integer departureTime,
      String sortByParam, String sortDirParam, Long firstResult, Long maxResults, Request request) {
    
    Response validateResponse = validateListLimitParams(firstResult, maxResults);
    if (validateResponse != null) {
      return validateResponse;
    }
    
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    if (organizationId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    PublicTransportStopId stopId = null;
    SortDir sortDir = null;
    PublicTransportStopTimeSortBy sortBy = null;
    
    if (StringUtils.isNotBlank(stopIdParam)) {
      stopId = toPublicTransportStopId(organizationId, stopIdParam);
      if (stopId == null) {
        return createBadRequest(String.format("Malformed stopId %s", stopIdParam));
      }
    }

    if (StringUtils.isNotBlank(sortByParam)) {
      sortBy = EnumUtils.getEnum(PublicTransportStopTimeSortBy.class, sortByParam);
    }
    
    if (StringUtils.isNotBlank(sortDirParam)) {
      sortDir = EnumUtils.getEnum(SortDir.class, sortDirParam);
    }
    
    return restResponseBuilder.buildResponse(publicTransportController.searchStopTimes(organizationId, null, stopId, departureTime, sortBy, sortDir, firstResult, maxResults), request);
  }

  @Override
  public Response listOrganizationPublicTransportStops(String organizationIdParam, Request request) {
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    if (organizationId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    List<Stop> result = publicTransportController.listStops(organizationId, null, null);
    List<String> ids = httpCacheController.getEntityIds(result);
    Response notModified = httpCacheController.getNotModified(request, ids);
    if (notModified != null) {
      return notModified;
    }

    return httpCacheController.sendModified(result, ids);
  }

  @Override
  public Response listOrganizationPublicTransportTrips(String organizationIdParam, Request request) {
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    if (organizationId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    List<Trip> result = publicTransportController.listTrips(organizationId, null, null);
    List<String> ids = httpCacheController.getEntityIds(result);
    Response notModified = httpCacheController.getNotModified(request, ids);
    if (notModified != null) {
      return notModified;
    }

    return httpCacheController.sendModified(result, ids);
  }

  @Override
  public Response listOrganizationPublicTransportRoutes(String organizationIdParam, Request request) {
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    if (organizationId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    List<Route> result = publicTransportController.listRoutes(organizationId, null, null);
    List<String> ids = httpCacheController.getEntityIds(result);
    Response notModified = httpCacheController.getNotModified(request, ids);
    if (notModified != null) {
      return notModified;
    }

    return httpCacheController.sendModified(result, ids);
  }

  @Override
  public Response listOrganizationPublicTransportSchedules(String organizationIdParam, Request request) {
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    if (organizationId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    List<Schedule> result = publicTransportController.listSchedules(organizationId, null, null);
    List<String> ids = httpCacheController.getEntityIds(result);
    Response notModified = httpCacheController.getNotModified(request, ids);
    if (notModified != null) {
      return notModified;
    }

    return httpCacheController.sendModified(result, ids);
  }
  
  /* Shortlinks */
  
  @Override
  public Response findOrganizationShortlink(String organizationIdParam, String shortlinkIdParam, Request request) {
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    if (organizationId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    ShortlinkId shortlinkId = kuntaApiIdFactory.createShortlinkId(organizationId, shortlinkIdParam);
    if (shortlinkId == null) {
      return createNotFound(NOT_FOUND);
    }

    Response notModified = restResponseBuilder.getNotModified(request, shortlinkId);
    if (notModified != null) {
      return notModified;
    }
    
    Shortlink shortlink = shortlinkController.findShortlink(organizationId, shortlinkId);
    if (shortlink != null) {
      return restResponseBuilder.sendModified(shortlink, shortlink.getId());
    }
    
    return createNotFound(NOT_FOUND);
  }
  
  @Override
  public Response listOrganizationShortlinks(String organizationIdParam, String path, Long firstResult, Long maxResults, Request request) {
    Response validateResponse = validateListLimitParams(firstResult, maxResults);
    if (validateResponse != null) {
      return validateResponse;
    }
    
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    if (organizationId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    List<Shortlink> result = shortlinkController.listShortlinks(organizationId, path, null, null);
    List<String> ids = httpCacheController.getEntityIds(result);
    Response notModified = httpCacheController.getNotModified(request, ids);
    if (notModified != null) {
      return notModified;
    }

    return httpCacheController.sendModified(result, ids);
  }

  /* Incidents */
  
  @Override
  public Response findOrganizationIncident(String organizationIdParam, String incidentIdParam, Request request) {
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    if (organizationId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    IncidentId incidentId = kuntaApiIdFactory.createIncidentId(organizationId, incidentIdParam);
    if (incidentId == null) {
      return createNotFound(NOT_FOUND);
    }

    Response notModified = restResponseBuilder.getNotModified(request, incidentId);
    if (notModified != null) {
      return notModified;
    }
    
    Incident incident = incidentController.findIncident(organizationId, incidentId);
    if (incident != null) {
      return restResponseBuilder.sendModified(incident, incident.getId());
    }
    
    return createNotFound(NOT_FOUND);
  }
  
  @Override
  public Response listOrganizationIncidents(String organizationIdParam, String slug, String startBefore, String endAfter,
      Integer area, Integer firstResult, Integer maxResults, String sortByParam, String sortDirParam, Request request) {
    
    Response validateResponse = validateListLimitParams(firstResult, maxResults);
    if (validateResponse != null) {
      return validateResponse;
    }
    
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    if (organizationId == null) {
      return createNotFound(NOT_FOUND);
    }

    IncidentSortBy sortBy = resolveIncidentSortBy(sortByParam);
    if (sortBy == null) {
      return createBadRequest(INVALID_VALUE_FOR_SORT_BY);
    }
    
    SortDir sortDir = resolveSortDir(sortDirParam);
    if (sortDir == null) {
      return createBadRequest(INVALID_VALUE_FOR_SORT_DIR);
    }
    
    List<Incident> result = incidentController.listIncidents(organizationId, slug, getDateTime(startBefore), getDateTime(endAfter), 
        sortBy, sortDir, firstResult, maxResults);
    
    List<String> ids = httpCacheController.getEntityIds(result);
    Response notModified = httpCacheController.getNotModified(request, ids);
    if (notModified != null) {
      return notModified;
    }

    return httpCacheController.sendModified(result, ids);
  }

  /* Emergencies */
  
  @Override
  public Response findOrganizationEmergency(String organizationIdParam, String emergencyIdParam, Request request) {
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    if (organizationId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    EmergencyId emergencyId = kuntaApiIdFactory.createEmergencyId(organizationId, emergencyIdParam);
    if (emergencyId == null) {
      return createNotFound(NOT_FOUND);
    }

    Response notModified = restResponseBuilder.getNotModified(request, emergencyId);
    if (notModified != null) {
      return notModified;
    }
    
    Emergency emergency = emergencyController.findEmergency(organizationId, emergencyId);
    if (emergency != null) {
      return restResponseBuilder.sendModified(emergency, emergency.getId());
    }
    
    return createNotFound(NOT_FOUND);
  }
  
  @Override
  public Response listOrganizationEmergencies(String organizationIdParam, String location, String before, String after,
      Integer firstResult, Integer maxResults, String sortByParam, String sortDirParam, Request request) {
    
    Response validateResponse = validateListLimitParams(firstResult, maxResults);
    if (validateResponse != null) {
      return validateResponse;
    }
    
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    if (organizationId == null) {
      return createNotFound(NOT_FOUND);
    }

    EmergencySortBy sortBy = resolveEmergencySortBy(sortByParam);
    if (sortBy == null) {
      return createBadRequest(INVALID_VALUE_FOR_SORT_BY);
    }
    
    SortDir sortDir = resolveSortDir(sortDirParam);
    if (sortDir == null) {
      return createBadRequest(INVALID_VALUE_FOR_SORT_DIR);
    }

    return restResponseBuilder.buildResponse(emergencyController.searchEmergencies(organizationId, location, getDateTime(before), getDateTime(after), 
      sortBy, sortDir, firstResult, maxResults), request);
  }

  @Override
  public Response findOrganizationEnvironmentalWarning(String organizationIdParam, String environmentalWarningIdParam, Request request) {
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    if (organizationId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    EnvironmentalWarningId environmentalWarningId = kuntaApiIdFactory.createEnvironmentalWarningId(organizationId, environmentalWarningIdParam);
    if (environmentalWarningId == null) {
      return createNotFound(NOT_FOUND);
    }

    Response notModified = restResponseBuilder.getNotModified(request, environmentalWarningId);
    if (notModified != null) {
      return notModified;
    }
    
    EnvironmentalWarning environmentalWarning = environmentalWarningController.findEnvironmentalWarning(organizationId, environmentalWarningId);
    if (environmentalWarning != null) {
      return restResponseBuilder.sendModified(environmentalWarning, environmentalWarning.getId());
    }
    
    return createNotFound(NOT_FOUND);
  }

  @Override
  public Response listOrganizationEnvironmentalWarnings(String organizationIdParam, Integer firstResult, String contextsParam, String startBefore, String startAfter, Integer maxResults, String sortByParam, String sortDirParam, Request request) {
    Response validateResponse = validateListLimitParams(firstResult, maxResults);
    if (validateResponse != null) {
      return validateResponse;
    }
    
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    if (organizationId == null) {
      return createNotFound(NOT_FOUND);
    }

    EnvironmentalWarningSortBy sortBy = resolveEnvironmentalWarningSortBy(sortByParam);
    if (sortBy == null) {
      return createBadRequest(INVALID_VALUE_FOR_SORT_BY);
    }
    
    SortDir sortDir = resolveSortDir(sortDirParam);
    if (sortDir == null) {
      return createBadRequest(INVALID_VALUE_FOR_SORT_DIR);
    }
    
    List<String> contexts = StringUtils.isBlank(contextsParam) ? null : Arrays.asList(StringUtils.split(contextsParam, ','));

    return restResponseBuilder.buildResponse(environmentalWarningController.searchEnvironmentalWarnings(organizationId, 
        contexts,
        getDateTime(startBefore), 
        getDateTime(startAfter),
        sortBy, 
        sortDir, 
        firstResult, 
        maxResults), 
        request);
  }

  private SortDir resolveSortDir(String sortDirParam) {
    SortDir sortDir = SortDir.ASC;
    if (sortDirParam != null) {
      return EnumUtils.getEnum(SortDir.class, sortDirParam);
    }
    
    return sortDir;
  }

  private OrganizationSortBy resolveOrganizationSortBy(String sortByParam) {
    OrganizationSortBy sortBy = OrganizationSortBy.NATURAL;
    if (sortByParam != null) {
      return  EnumUtils.getEnum(OrganizationSortBy.class, sortByParam);
    }
    return sortBy;
  }

  private IncidentSortBy resolveIncidentSortBy(String sortByParam) {
    IncidentSortBy sortBy = IncidentSortBy.NATURAL;
    if (sortByParam != null) {
      return  EnumUtils.getEnum(IncidentSortBy.class, sortByParam);
    }
    return sortBy;
  }

  private EmergencySortBy resolveEmergencySortBy(String sortByParam) {
    EmergencySortBy sortBy = EmergencySortBy.NATURAL;
    if (sortByParam != null) {
      return  EnumUtils.getEnum(EmergencySortBy.class, sortByParam);
    }
    return sortBy;
  }
  
  /**
   * Resolves sort by value for environmental warnings list
   * 
   * @param sortByParam parameter value
   * @return sort by value
   */
  private EnvironmentalWarningSortBy resolveEnvironmentalWarningSortBy(String sortByParam) {
    EnvironmentalWarningSortBy sortBy = EnvironmentalWarningSortBy.NATURAL;
    if (sortByParam != null) {
      return  EnumUtils.getEnum(EnvironmentalWarningSortBy.class, sortByParam);
    }
    return sortBy;
  }

  private NewsSortBy resolveNewsSortBy(String sortByParam) {
    NewsSortBy sortBy = NewsSortBy.NATURAL;
    if (sortByParam != null) {
      return  EnumUtils.getEnum(NewsSortBy.class, sortByParam);
    }
    return sortBy;
  }

  private PageSortBy resolvePageSortBy(String sortByParam) {
    PageSortBy sortBy = PageSortBy.NATURAL;
    if (sortByParam != null) {
      return EnumUtils.getEnum(PageSortBy.class, sortByParam);
    }
    
    return sortBy;
  }

  private ContactSortBy resolveContactSortBy(String sortByParam) {
    ContactSortBy sortBy = ContactSortBy.NATURAL;
    if (sortByParam != null) {
      return EnumUtils.getEnum(ContactSortBy.class, sortByParam);
    }
    
    return sortBy;
  }

  private Response listOrganizationAnnouncements(Request request, OrganizationId organizationId, String slug, AnnouncementOrder order, AnnouncementOrderDirection orderDirection, Integer firstResult, Integer maxResults) {
    List<Announcement> result = announcementController.listAnnouncements(organizationId, slug, order, orderDirection, firstResult, maxResults);
    
    List<String> ids = httpCacheController.getEntityIds(result);
    Response notModified = httpCacheController.getNotModified(request, ids);
    if (notModified != null) {
      return notModified;
    }

    return httpCacheController.sendModified(result, ids);
  }

  private Response validateListLimitParams(Integer firstResult, Integer maxResults) {
    return validateListLimitParams(firstResult != null ? firstResult.longValue() : null, maxResults != null ? maxResults.longValue() : null);
  }
  
  private Response validateListLimitParams(Long firstResult, Long maxResults) {
    if (firstResult != null && firstResult < 0) {
      return createBadRequest(FIRST_RESULT_MUST_BY_A_POSITIVE_INTEGER);
    }
    
    if (maxResults != null && maxResults < 0) {
      return createBadRequest(MAX_RESULTS_MUST_BY_A_POSITIVE_INTEGER);
    }
    
    return null;
  }
  
  private BannerId toBannerId(OrganizationId organizationId, String id) {
    if (StringUtils.isNotBlank(id)) {
      return new BannerId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, id);
    }
    
    return null;
  }
  
  private TileId toTileId(OrganizationId organizationId, String id) {
    if (StringUtils.isNotBlank(id)) {
      return new TileId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, id);
    }
    
    return null;
  }
  
  private NewsArticleId toNewsArticleId(OrganizationId organizationId, String id) {
    if (StringUtils.isNotBlank(id)) {
      return new NewsArticleId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, id);
    }
    
    return null;
  }
  
  private EventId toEventId(OrganizationId organizationId, String id) {
    if (StringUtils.isNotBlank(id)) {
      return new EventId(organizationId,KuntaApiConsts.IDENTIFIER_NAME, id);
    }
    
    return null;
  }

  private AttachmentId toAttachmentId(OrganizationId organizationId, String id) {
    if (StringUtils.isNotBlank(id)) {
      return new AttachmentId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, id);
    }
    
    return null;
  }
  
  private FileId toFileId(OrganizationId organizationId, String id) {
    if (StringUtils.isNotBlank(id)) {
      return new FileId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, id);
    }
    
    return null;
  }
  
  private PageId toPageId(OrganizationId organizationId, String id) {
    if (StringUtils.isNotBlank(id)) {
      return new PageId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, id);
    }
    
    return null;
  }

  private FragmentId toFragmentId(OrganizationId organizationId, String id) {
    if (StringUtils.isNotBlank(id)) {
      return new FragmentId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, id);
    }
    
    return null;
  }
  
  private PublicTransportAgencyId toPublicTransportAgencyId(OrganizationId organizationId, String id) {
    if (StringUtils.isNotBlank(id)) {
      return new PublicTransportAgencyId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, id);
    }
    
    return null;
  }
  
  private PublicTransportScheduleId toPublicTransportScheduleId(OrganizationId organizationId, String id) {
    if (StringUtils.isNotBlank(id)) {
      return new PublicTransportScheduleId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, id);
    }
    
    return null;
  }
  
  private PublicTransportRouteId toPublicTransportRouteId(OrganizationId organizationId, String id) {
    if (StringUtils.isNotBlank(id)) {
      return new PublicTransportRouteId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, id);
    }
    
    return null;
  }

  private PublicTransportStopId toPublicTransportStopId(OrganizationId organizationId, String id) {
    if (StringUtils.isNotBlank(id)) {
      return new PublicTransportStopId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, id);
    }
    
    return null;
  }
  
  private PublicTransportStopTimeId toPublicTransportStopTimeId(OrganizationId organizationId, String id) {
    if (StringUtils.isNotBlank(id)) {
      return new PublicTransportStopTimeId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, id);
    }
    
    return null;
  }
    
  private PublicTransportTripId toPublicTransportTripId(OrganizationId organizationId, String id) {
    if (StringUtils.isNotBlank(id)) {
      return new PublicTransportTripId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, id);
    }
    
    return null;
  }
  
  private MenuId toMenuId(OrganizationId organizationId, String id) {
    if (StringUtils.isNotBlank(id)) {
      return new MenuId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, id);
    }
    
    return null;
  }
  
  private MenuItemId toMenuItemId(OrganizationId organizationId, String id) {
    if (StringUtils.isNotBlank(id)) {
      return new MenuItemId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, id);
    }
    
    return null;
  }
  
  private JobId toJobId(OrganizationId organizationId, String id) {
    if (StringUtils.isNotBlank(id)) {
      return new JobId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, id);
    }
    
    return null;
  }

  private AnnouncementId toAnnouncementId(OrganizationId organizationId, String id) {
    if (StringUtils.isNotBlank(id)) {
      return new AnnouncementId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, id);
    }
    
    return null;
  }

  private ContactId toContactId(OrganizationId organizationId, String id) {
    if (StringUtils.isNotBlank(id)) {
      return new ContactId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, id);
    }
    
    return null;
  }
  
  private OffsetDateTime getDateTime(String timeString) {
    if (StringUtils.isNotBlank(timeString)) {
      return OffsetDateTime.parse(timeString);
    }
    
    return null;
  }

  
}