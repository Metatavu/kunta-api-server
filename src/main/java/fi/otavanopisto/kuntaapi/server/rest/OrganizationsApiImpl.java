 package fi.otavanopisto.kuntaapi.server.rest;

import java.time.OffsetDateTime;
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

import fi.metatavu.kuntaapi.server.rest.OrganizationsApi;
import fi.metatavu.kuntaapi.server.rest.model.Agency;
import fi.metatavu.kuntaapi.server.rest.model.Announcement;
import fi.metatavu.kuntaapi.server.rest.model.Attachment;
import fi.metatavu.kuntaapi.server.rest.model.Banner;
import fi.metatavu.kuntaapi.server.rest.model.Contact;
import fi.metatavu.kuntaapi.server.rest.model.Event;
import fi.metatavu.kuntaapi.server.rest.model.FileDef;
import fi.metatavu.kuntaapi.server.rest.model.Fragment;
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
import fi.otavanopisto.kuntaapi.server.controllers.AnnouncementController;
import fi.otavanopisto.kuntaapi.server.controllers.BannerController;
import fi.otavanopisto.kuntaapi.server.controllers.ClientContainer;
import fi.otavanopisto.kuntaapi.server.controllers.ContactController;
import fi.otavanopisto.kuntaapi.server.controllers.EventController;
import fi.otavanopisto.kuntaapi.server.controllers.FileController;
import fi.otavanopisto.kuntaapi.server.controllers.FragmentController;
import fi.otavanopisto.kuntaapi.server.controllers.HttpCacheController;
import fi.otavanopisto.kuntaapi.server.controllers.JobController;
import fi.otavanopisto.kuntaapi.server.controllers.MenuController;
import fi.otavanopisto.kuntaapi.server.controllers.NewsController;
import fi.otavanopisto.kuntaapi.server.controllers.OrganizationController;
import fi.otavanopisto.kuntaapi.server.controllers.PageController;
import fi.otavanopisto.kuntaapi.server.controllers.PublicTransportController;
import fi.otavanopisto.kuntaapi.server.controllers.SecurityController;
import fi.otavanopisto.kuntaapi.server.controllers.ShortlinkController;
import fi.otavanopisto.kuntaapi.server.controllers.TileController;
import fi.otavanopisto.kuntaapi.server.id.AnnouncementId;
import fi.otavanopisto.kuntaapi.server.id.AttachmentId;
import fi.otavanopisto.kuntaapi.server.id.BannerId;
import fi.otavanopisto.kuntaapi.server.id.ContactId;
import fi.otavanopisto.kuntaapi.server.id.EventId;
import fi.otavanopisto.kuntaapi.server.id.FileId;
import fi.otavanopisto.kuntaapi.server.id.FragmentId;
import fi.otavanopisto.kuntaapi.server.id.JobId;
import fi.otavanopisto.kuntaapi.server.id.MenuId;
import fi.otavanopisto.kuntaapi.server.id.MenuItemId;
import fi.otavanopisto.kuntaapi.server.id.NewsArticleId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PageId;
import fi.otavanopisto.kuntaapi.server.id.PublicTransportAgencyId;
import fi.otavanopisto.kuntaapi.server.id.PublicTransportRouteId;
import fi.otavanopisto.kuntaapi.server.id.PublicTransportScheduleId;
import fi.otavanopisto.kuntaapi.server.id.PublicTransportStopId;
import fi.otavanopisto.kuntaapi.server.id.PublicTransportStopTimeId;
import fi.otavanopisto.kuntaapi.server.id.PublicTransportTripId;
import fi.otavanopisto.kuntaapi.server.id.ShortlinkId;
import fi.otavanopisto.kuntaapi.server.id.TileId;
import fi.otavanopisto.kuntaapi.server.integrations.AnnouncementProvider;
import fi.otavanopisto.kuntaapi.server.integrations.AnnouncementProvider.AnnouncementOrder;
import fi.otavanopisto.kuntaapi.server.integrations.AnnouncementProvider.AnnouncementOrderDirection;
import fi.otavanopisto.kuntaapi.server.integrations.AttachmentData;
import fi.otavanopisto.kuntaapi.server.integrations.EventProvider;
import fi.otavanopisto.kuntaapi.server.integrations.JobProvider;
import fi.otavanopisto.kuntaapi.server.integrations.JobProvider.JobOrder;
import fi.otavanopisto.kuntaapi.server.integrations.JobProvider.JobOrderDirection;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiIdFactory;
import fi.otavanopisto.kuntaapi.server.integrations.PublicTransportStopTimeSortBy;
import fi.otavanopisto.kuntaapi.server.integrations.SortDir;
import fi.otavanopisto.kuntaapi.server.system.OrganizationSettingProvider;

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
  
  private static final String INVALID_SETTING_ID = "Invalid setting id";
  private static final String MAX_RESULTS_MUST_BY_A_POSITIVE_INTEGER = "maxResults must by a positive integer";
  private static final String FIRST_RESULT_MUST_BY_A_POSITIVE_INTEGER = "firstResult must by a positive integer";
  private static final String NOT_FOUND = "Not Found";
  private static final String FORBIDDEN = "Forbidden";
  private static final String NOT_IMPLEMENTED = "Not implemented";
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
  private JobController jobController;
  
  @Inject
  private SecurityController securityController;

  @Inject
  private ContactController contactController;
  
  @Inject
  private ShortlinkController shortlinkController;
  
  @Inject
  private KuntaApiIdFactory kuntaApiIdFactory;
  
  @Inject
  private ClientContainer clientContainer;
  
  @Inject
  private HttpCacheController httpCacheController;
  
  @Inject
  private PublicTransportController publicTransportController;
  
  @Override
  public Response listOrganizations(String businessName, String businessCode, String search, Long firstResult, Long maxResults, @Context Request request) {
    Response validateResponse = validateListLimitParams(firstResult, maxResults);
    if (validateResponse != null) {
      return validateResponse;
    }
    
    List<Organization> organizations;
    
    if (search != null) {
      organizations = organizationController.searchOrganizations(search, businessName, businessCode, firstResult, maxResults);
    } else {
      organizations = organizationController.listOrganizations(businessName, businessCode, firstResult, maxResults);
    }
    
    List<String> ids = httpCacheController.getEntityIds(organizations);
    Response notModified = httpCacheController.getNotModified(request, ids);
    if (notModified != null) {
      return notModified;
    }

    return httpCacheController.sendModified(organizations, ids);
  }
  
  @Override
  public Response findOrganization(String organizationIdParam, @Context Request request) {
  	  OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    	if (organizationId == null) {
    	  return createNotFound(NOT_FOUND);
    	}
    	
    	Response notModified = httpCacheController.getNotModified(request, organizationId);
    	if (notModified != null) {
    	  return notModified;
    	}
    	
    	Organization organization = organizationController.findOrganization(organizationId);
    	if (organization != null) {
      return httpCacheController.sendModified(organization, organization.getId());
    }
      
    return createNotFound(NOT_FOUND);
  }

  @Override
  public Response findOrganizationEvent(String organizationIdParam, String eventIdParam, @Context Request request) {
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    EventId eventId = toEventId(organizationId, eventIdParam);
    
    Response notModified = httpCacheController.getNotModified(request, eventId);
    if (notModified != null) {
      return notModified;
    }
    
    Event event = eventController.findEvent(organizationId, eventId);
    if (event != null) {
      return httpCacheController.sendModified(event, event.getId());
    }    
    
    return createNotFound(NOT_FOUND);
  }

  @Override
  public Response findOrganizationEventImage(String organizationIdParam, String eventIdParam, String imageIdParam, @Context Request request) {
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    EventId eventId = toEventId(organizationId, eventIdParam);
    AttachmentId attachmentId = new AttachmentId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, imageIdParam);
    
    Response notModified = httpCacheController.getNotModified(request, attachmentId);
    if (notModified != null) {
      return notModified;
    }
    
    Attachment attachment = eventController.findEventImage(organizationId, eventId, attachmentId);
    if (attachment != null) {
      return httpCacheController.sendModified(attachment, attachment.getId());
    }    
    
    return createNotFound(NOT_FOUND);
  }

  @Override
  public Response getOrganizationEventImageData(String organizationIdParam, String eventIdParam, String imageIdParam, Integer size, @Context Request request) {
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    EventId eventId = toEventId(organizationId, eventIdParam);
    AttachmentId attachmentId = new AttachmentId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, imageIdParam);
    
    
    Response notModified = httpCacheController.getNotModified(request, attachmentId);
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
      String publishedAfter, String search, Integer firstResult, Integer maxResults, Request request) {
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    
    List<NewsArticle> result = newsController.listNewsArticles(slug, tag, getDateTime(publishedBefore), getDateTime(publishedAfter), search, firstResult, maxResults, organizationId);
    
    List<String> ids = httpCacheController.getEntityIds(result);
    Response notModified = httpCacheController.getNotModified(request, ids);
    if (notModified != null) {
      return notModified;
    }

    return httpCacheController.sendModified(result, ids);
  }

  @Override
  public Response findOrganizationNewsArticle(String organizationIdParam, String newsArticleIdParam, @Context Request request) {
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    NewsArticleId newsArticleId = toNewsArticleId(organizationId, newsArticleIdParam);
    
    Response notModified = httpCacheController.getNotModified(request, newsArticleId);
    if (notModified != null) {
      return notModified;
    }
    
    NewsArticle newsArticle = newsController.findNewsArticle(organizationId, newsArticleId);
    if (newsArticle != null) {
      return httpCacheController.sendModified(newsArticle, newsArticle.getId());
    }
    
    return createNotFound(NOT_FOUND);
  }

  @Override
  public Response findOrganizationNewsArticleImage(String organizationIdParam, String newsArticleIdParam, String imageIdParam, @Context Request request) {
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    NewsArticleId newsArticleId = toNewsArticleId(organizationId, newsArticleIdParam);
    AttachmentId attachmentId = toAttachmentId(organizationId, imageIdParam);
    
    Response notModified = httpCacheController.getNotModified(request, attachmentId);
    if (notModified != null) {
      return notModified;
    }
    
    Attachment attachment = newsController.findNewsArticleImage(organizationId, newsArticleId, attachmentId);
    if (attachment != null) {
      return httpCacheController.sendModified(attachment, attachment.getId());
    }
    
    return createNotFound(NOT_FOUND);
  }

  @Override
  public Response getOrganizationNewsArticleImageData(String organizationIdParam, String newsArticleIdParam, String imageIdParam, Integer size, @Context Request request) {
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    NewsArticleId newsArticleId = toNewsArticleId(organizationId, newsArticleIdParam);
    AttachmentId attachmentId = toAttachmentId(organizationId, imageIdParam);
   
    Response notModified = httpCacheController.getNotModified(request, attachmentId);
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
    
    Response notModified = httpCacheController.getNotModified(request, bannerId);
    if (notModified != null) {
      return notModified;
    }

    Banner banner = bannerController.findBanner(organizationId, bannerId);
    if (banner != null) {
      return httpCacheController.sendModified(banner, banner.getId());
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
    
    Response notModified = httpCacheController.getNotModified(request, attachmentId);
    if (notModified != null) {
      return notModified;
    }

    Attachment attachment = bannerController.findBannerImage(organizationId, bannerId, attachmentId);
    if (attachment != null) {
      return httpCacheController.sendModified(attachment, attachment.getId());
    }
    
    return createNotFound(NOT_FOUND);
  }

  @Override
  public Response getOrganizationBannerImageData(String organizationIdParam, String bannerIdParam, String imageIdParam, Integer size, @Context Request request) {
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    BannerId bannerId = toBannerId(organizationId, bannerIdParam);
    AttachmentId attachmentId = toAttachmentId(organizationId, imageIdParam);
    
    Response notModified = httpCacheController.getNotModified(request, attachmentId);
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
    
    Response notModified = httpCacheController.getNotModified(request, tileId);
    if (notModified != null) {
      return notModified;
    }

    Tile tile = tileController.findTile(organizationId, tileId);
    if (tile != null) {
      return httpCacheController.sendModified(tile, tile.getId());
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
    
    Response notModified = httpCacheController.getNotModified(request, attachmentId);
    if (notModified != null) {
      return notModified;
    }

    Attachment attachment = tileController.findTileImage(organizationId, tileId, attachmentId);
    if (attachment != null) {
      return httpCacheController.sendModified(attachment, attachment.getId());
    }
    
    return createNotFound(NOT_FOUND);
  }

  @Override
  public Response getOrganizationTileImageData(String organizationIdParam, String tileIdParam, String imageIdParam, Integer size, @Context Request request) {
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    TileId tileId = toTileId(organizationId, tileIdParam);
    AttachmentId attachmentId = toAttachmentId(organizationId, imageIdParam);
    
    Response notModified = httpCacheController.getNotModified(request, attachmentId);
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
  public Response listOrganizationPages(String organizationIdParam, String parentIdParam, String path, String search, Long firstResult, Long maxResults, @Context Request request) {
    Response validateResponse = validateListLimitParams(firstResult, maxResults);
    if (validateResponse != null) {
      return validateResponse;
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
    
    List<Page> result = listOrganizationPages(organizationId, onlyRootPages, parentId, path, search, firstResult, maxResults);
    List<String> ids = httpCacheController.getEntityIds(result);
    Response notModified = httpCacheController.getNotModified(request, ids);
    if (notModified != null) {
      return notModified;
    }

    return httpCacheController.sendModified(result, ids);
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

    Response notModified = httpCacheController.getNotModified(request, pageId);
    if (notModified != null) {
      return notModified;
    }
    
    Page page = pageController.findPage(organizationId, pageId);
    if (page != null) {
      return httpCacheController.sendModified(page, page.getId());
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

    Response notModified = httpCacheController.getNotModified(request, pageId);
    if (notModified != null) {
      return notModified;
    }
    
    if (pageController.findPage(organizationId, pageId) != null) {
      List<LocalizedValue> pageContents = pageController.getPageContents(organizationId, pageId);
      if (pageContents != null) {
        return httpCacheController.sendModified(pageContents, pageId.getId());
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
    
    Response notModified = httpCacheController.getNotModified(request, attachmentId);
    if (notModified != null) {
      return notModified;
    }
    
    Attachment attachment = pageController.findPageImage(organizationId, pageId, attachmentId);
    if (attachment != null) {
      return httpCacheController.sendModified(attachment, attachment.getId());
    }
    
    return createNotFound(NOT_FOUND);
  }

  @Override
  public Response getOrganizationPageImageData(String organizationIdParam, String pageIdParam, String imageIdParam, Integer size, @Context Request request) {
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    PageId pageId = toPageId(organizationId, pageIdParam);
    AttachmentId attachmentId = toAttachmentId(organizationId, imageIdParam);
    
    Response notModified = httpCacheController.getNotModified(request, attachmentId);
    if (notModified != null) {
      return notModified;
    }
    
    AttachmentData attachmentData = pageController.getPageAttachmentData(organizationId, pageId, attachmentId, size);
    if (attachmentData != null) {
      return httpCacheController.streamModified(attachmentData.getData(), attachmentData.getType(), attachmentId);
    }
    
    return Response.status(Status.NOT_FOUND).build();
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

    Response notModified = httpCacheController.getNotModified(request, fragmentId);
    if (notModified != null) {
      return notModified;
    }
    
    Fragment fragment = fragmentController.findFragment(organizationId, fragmentId);
    if (fragment != null) {
      return httpCacheController.sendModified(fragment, fragment.getId());
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
    
    Response notModified = httpCacheController.getNotModified(request, menuId);
    if (notModified != null) {
      return notModified;
    }
    
    Menu menu = menuController.findMenu(organizationId, menuId);
    if (menu != null) {
      return httpCacheController.sendModified(menu, menu.getId());
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
    Response notModified = httpCacheController.getNotModified(request, menuItemId);
    if (notModified != null) {
      return notModified;
    }
    
    MenuItem menuItem = menuController.findMenuItem(organizationId, menuId, menuItemId);
    if (menuItem != null) {
      return httpCacheController.sendModified(menuItem, menuItem.getId());
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
    
    Response notModified = httpCacheController.getNotModified(request, fileId);
    if (notModified != null) {
      return notModified;
    }
    
    FileDef file = fileController.findFile(organizationId, fileId);
    if (file != null) {
      return httpCacheController.sendModified(file, file.getId());
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
    
    Response notModified = httpCacheController.getNotModified(request, fileId);
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
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    if (organizationId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    JobId jobId = toJobId(organizationId, jobIdParam);
    if (jobId == null) {
      return createNotFound(NOT_FOUND);
    }

    Response notModified = httpCacheController.getNotModified(request, jobId);
    if (notModified != null) {
      return notModified;
    }

    Job job = jobController.findJob(organizationId, jobId);
    if (job != null) {
      return httpCacheController.sendModified(job, job.getId());
    }
    
    return createNotFound(NOT_FOUND);
  }

  @Override
  public Response listOrganizationJobs(String organizationIdParam, String sortBy, String sortDir, Long firstResult, Long maxResults, @Context Request request) {
    Response validateResponse = validateListLimitParams(firstResult, maxResults);
    if (validateResponse != null) {
      return validateResponse;
    }
    
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    if (organizationId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    JobOrder order = null;
    JobOrderDirection orderDirection = null;
    
    if (StringUtils.isNotBlank(sortBy)) {
      order = EnumUtils.getEnum(JobProvider.JobOrder.class, sortBy);
      if (order == null) {
        return createBadRequest("Invalid value for sortBy");
      }
    }
    
    if (StringUtils.isNotBlank(sortDir)) {
      orderDirection = EnumUtils.getEnum(JobOrderDirection.class, sortDir);
      if (orderDirection == null) {
        return createBadRequest("Invalid value for sortDir");
      }
    }
    
    return listOrganizationJobs(request, organizationId, order, orderDirection, firstResult, maxResults);
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

    Response notModified = httpCacheController.getNotModified(request, announcementId);
    if (notModified != null) {
      return notModified;
    }
    
    Announcement announcement = announcementController.findAnnouncement(organizationId, announcementId);
    if (announcement != null) {
      return httpCacheController.sendModified(announcement, announcement.getId());
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
        return createBadRequest("Invalid value for sortBy");
      }
    }
    
    if (StringUtils.isNotBlank(sortDir)) {
      orderDirection = EnumUtils.getEnum(AnnouncementProvider.AnnouncementOrderDirection.class, sortDir);
      if (orderDirection == null) {
        return createBadRequest("Invalid value for sortDir");
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

    Response notModified = httpCacheController.getNotModified(request, contactId);
    if (notModified != null) {
      return notModified;
    }
    
    Contact contact = contactController.findContact(organizationId, contactId);
    if (contact != null) {
      return httpCacheController.sendModified(contact, contact.getId());
    }
    
    return createNotFound(NOT_FOUND);
  }

  @Override
  public Response listOrganizationContacts(String organizationIdParam, Request request) {
    OrganizationId organizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
    if (organizationId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    return listOrganizationContacts(request, organizationId, null, null);
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

    Response notModified = httpCacheController.getNotModified(request, agencyId);
    if (notModified != null) {
      return notModified;
    }
    
    Agency agency = publicTransportController.findAgency(organizationId, agencyId);
    if (agency != null) {
      return httpCacheController.sendModified(agency, agency.getId());
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

    Response notModified = httpCacheController.getNotModified(request, routeId);
    if (notModified != null) {
      return notModified;
    }
    
    Route route = publicTransportController.findRoute(organizationId, routeId);
    if (route != null) {
      return httpCacheController.sendModified(route, route.getId());
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

    Response notModified = httpCacheController.getNotModified(request, scheduleId);
    if (notModified != null) {
      return notModified;
    }
    
    Schedule schedule = publicTransportController.findSchedule(organizationId, scheduleId);
    if (schedule != null) {
      return httpCacheController.sendModified(schedule, schedule.getId());
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

    Response notModified = httpCacheController.getNotModified(request, stopId);
    if (notModified != null) {
      return notModified;
    }
    
    Stop stop = publicTransportController.findStop(organizationId, stopId);
    if (stop != null) {
      return httpCacheController.sendModified(stop, stop.getId());
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

    Response notModified = httpCacheController.getNotModified(request, stopTimeId);
    if (notModified != null) {
      return notModified;
    }
    
    StopTime stopTime = publicTransportController.findStopTime(organizationId, stopTimeId);
    if (stopTime != null) {
      return httpCacheController.sendModified(stopTime, stopTime.getId());
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

    Response notModified = httpCacheController.getNotModified(request, tripId);
    if (notModified != null) {
      return notModified;
    }
    
    Trip trip = publicTransportController.findTrip(organizationId, tripId);
    if (trip != null) {
      return httpCacheController.sendModified(trip, trip.getId());
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
    
    List<StopTime> result = publicTransportController.listStopTimes(organizationId, stopId, departureTime, sortBy, sortDir, firstResult, maxResults);
    List<String> ids = httpCacheController.getEntityIds(result);
    Response notModified = httpCacheController.getNotModified(request, ids);
    if (notModified != null) {
      return notModified;
    }

    return httpCacheController.sendModified(result, ids);
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

    Response notModified = httpCacheController.getNotModified(request, shortlinkId);
    if (notModified != null) {
      return notModified;
    }
    
    Shortlink shortlink = shortlinkController.findShortlink(organizationId, shortlinkId);
    if (shortlink != null) {
      return httpCacheController.sendModified(shortlink, shortlink.getId());
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
  public Response findOrganizationIncident(String organizationId, String incidentId, Request request) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }

  @Override
  public Response listOrganizationIncidents(String organizationId, String startBefore, String endAfter, Integer area, Integer firstResult, Integer maxResults, String orderBy, String orderDir, Request request) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }
  
  private List<Page> listOrganizationPages(OrganizationId organizationId, boolean onlyRootPages, PageId parentId, String path, String search, Long firstResult, Long maxResults) {
    if (search != null) {
      return pageController.searchPages(organizationId, search, firstResult, maxResults);
    } else {
      return pageController.listPages(organizationId, path, onlyRootPages, parentId, firstResult, maxResults);
    }
  }

  private Response listOrganizationJobs(Request request, OrganizationId organizationId, JobOrder order, JobOrderDirection orderDirection, Long firstResult, Long maxResults) {
    List<Job> result = jobController.listJobs(organizationId, order, orderDirection, firstResult, maxResults);
    
    List<String> ids = httpCacheController.getEntityIds(result);
    Response notModified = httpCacheController.getNotModified(request, ids);
    if (notModified != null) {
      return notModified;
    }

    return httpCacheController.sendModified(result, ids);
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

  private Response listOrganizationContacts(Request request, OrganizationId organizationId, Integer firstResult, Integer maxResults) {
    List<Contact> result = contactController.listContacts(organizationId, firstResult, maxResults);
    
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