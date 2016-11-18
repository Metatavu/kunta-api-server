package fi.otavanopisto.kuntaapi.server.rest;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.ejb.Stateful;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import fi.otavanopisto.kuntaapi.server.controllers.BannerController;
import fi.otavanopisto.kuntaapi.server.controllers.EventController;
import fi.otavanopisto.kuntaapi.server.controllers.HttpCacheController;
import fi.otavanopisto.kuntaapi.server.controllers.JobController;
import fi.otavanopisto.kuntaapi.server.controllers.MenuController;
import fi.otavanopisto.kuntaapi.server.controllers.NewsController;
import fi.otavanopisto.kuntaapi.server.controllers.OrganizationController;
import fi.otavanopisto.kuntaapi.server.controllers.PageController;
import fi.otavanopisto.kuntaapi.server.controllers.TileController;
import fi.otavanopisto.kuntaapi.server.id.AttachmentId;
import fi.otavanopisto.kuntaapi.server.id.BannerId;
import fi.otavanopisto.kuntaapi.server.id.EventId;
import fi.otavanopisto.kuntaapi.server.id.FileId;
import fi.otavanopisto.kuntaapi.server.id.JobId;
import fi.otavanopisto.kuntaapi.server.id.MenuId;
import fi.otavanopisto.kuntaapi.server.id.MenuItemId;
import fi.otavanopisto.kuntaapi.server.id.NewsArticleId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationServiceId;
import fi.otavanopisto.kuntaapi.server.id.PageId;
import fi.otavanopisto.kuntaapi.server.id.TileId;
import fi.otavanopisto.kuntaapi.server.integrations.AttachmentData;
import fi.otavanopisto.kuntaapi.server.integrations.EventProvider;
import fi.otavanopisto.kuntaapi.server.integrations.JobProvider;
import fi.otavanopisto.kuntaapi.server.integrations.JobProvider.JobOrder;
import fi.otavanopisto.kuntaapi.server.integrations.JobProvider.JobOrderDirection;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.integrations.OrganizationServiceProvider;
import fi.otavanopisto.kuntaapi.server.rest.model.Attachment;
import fi.otavanopisto.kuntaapi.server.rest.model.Banner;
import fi.otavanopisto.kuntaapi.server.rest.model.Event;
import fi.otavanopisto.kuntaapi.server.rest.model.Job;
import fi.otavanopisto.kuntaapi.server.rest.model.LocalizedValue;
import fi.otavanopisto.kuntaapi.server.rest.model.Menu;
import fi.otavanopisto.kuntaapi.server.rest.model.MenuItem;
import fi.otavanopisto.kuntaapi.server.rest.model.NewsArticle;
import fi.otavanopisto.kuntaapi.server.rest.model.Organization;
import fi.otavanopisto.kuntaapi.server.rest.model.OrganizationService;
import fi.otavanopisto.kuntaapi.server.rest.model.OrganizationSetting;
import fi.otavanopisto.kuntaapi.server.rest.model.Page;
import fi.otavanopisto.kuntaapi.server.rest.model.Tile;
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
  private static final String NOT_IMPLEMENTED = "Not implemented";
  private static final String INTERNAL_SERVER_ERROR = "Internal Server Error";
  
  @Inject
  private OrganizationSettingProvider organizationSettingProvider;
  
  @Inject
  private OrganizationController organizationController;
  
  @Inject
  private PageController pageController;

  @Inject
  private MenuController menuController;
  
  @Inject
  private BannerController bannerController;

  @Inject
  private TileController tileController;

  @Inject
  private NewsController newsController;
  
  @Inject
  private EventController eventController;
  
  @Inject
  private JobController jobController;
  
  @Inject
  private HttpCacheController httpCacheController;
  
  @Inject
  private Instance<OrganizationServiceProvider> organizationServiceProviders;
  
  @Override
  public Response listOrganizations(String businessName, String businessCode, String search, Long firstResult, Long maxResults, @Context Request request) {
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
  	  OrganizationId organizationId = toOrganizationId(organizationIdParam);
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
  public Response createOrganizationService(String organizationId, OrganizationService body, @Context Request request) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }
  
  @Override
  public Response findOrganizationService(String organizationIdParam, String organizationServiceIdParam, @Context Request request) {
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
    OrganizationServiceId organizationServiceId = toOrganizationServiceId(organizationId, organizationServiceIdParam);
    
    for (OrganizationServiceProvider organizationServiceProvider : getOrganizationServiceProviders()) {
      OrganizationService organizationService = organizationServiceProvider.findOrganizationService(organizationId, organizationServiceId);
      if (organizationService != null) {
        return Response.ok(organizationService)
          .build();
      }
    }
    
    return Response
        .status(Status.NOT_FOUND)
        .build();
  }
  
  @Override
  public Response listOrganizationOrganizationServices(String organizationIdParam, Long firstResult, Long maxResults, @Context Request request) {
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
    if (organizationId == null) {
      return Response.status(Status.BAD_REQUEST)
        .entity("Organization parameter is mandatory")
        .build();
    }
    
    Response validationResponse = validateListLimitParams(firstResult, maxResults);
    if (validationResponse != null) {
      return validationResponse;
    }
    
    List<OrganizationService> result = new ArrayList<>();
    
    for (OrganizationServiceProvider organizationServiceProvider : getOrganizationServiceProviders()) {
      result.addAll(organizationServiceProvider.listOrganizationServices(organizationId));
    }
    
    int resultCount = result.size();
    int firstIndex = firstResult == null ? 0 : Math.min(firstResult.intValue(), resultCount);
    int toIndex = maxResults == null ? resultCount : Math.min(firstIndex + maxResults.intValue(), resultCount);
    
    return Response.ok(result.subList(firstIndex, toIndex))
      .build();
  }
  
  @Override
  public Response updateOrganizationService(String organizationId, String organizationServiceId,
      OrganizationService body, @Context Request request) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }

  @Override
  public Response findOrganizationEvent(String organizationIdParam, String eventIdParam, @Context Request request) {
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
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
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
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
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
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

    return Response.status(Status.NOT_FOUND)
      .build();
  }

  @Override
  public Response listOrganizationEventImages(String organizationIdParam, String eventIdParam, @Context Request request) {
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
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
    
    EventProvider.EventOrder order = EventProvider.EventOrder.START_DATE;
    EventProvider.EventOrderDirection orderDirection = EventProvider.EventOrderDirection.DESCENDING;
    
    if (StringUtils.isNotBlank(orderBy)) {
      order = EnumUtils.getEnum(EventProvider.EventOrder.class, orderBy);
      if (order == null) {
        return Response.status(Status.BAD_REQUEST)
          .entity(String.format("Invalid event order %s", orderBy))
          .build();
      }
    }
    
    if (StringUtils.isNotBlank(orderDir)) {
      orderDirection = EnumUtils.getEnum(EventProvider.EventOrderDirection.class, orderDir);
      if (orderDirection == null) {
        return Response.status(Status.BAD_REQUEST)
          .entity(String.format("Invalid event order direction %s", orderDir))
          .build();
      }
    }
    
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
    
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
  public Response listOrganizationNews(String organizationIdParam, String publishedBefore, String publishedAfter,
      Integer firstResult, Integer maxResults, @Context Request request) {
    
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
    
    List<NewsArticle> result = newsController.listNewsArticles(getDateTime(publishedBefore), getDateTime(publishedAfter), firstResult, maxResults, organizationId);
    
    List<String> ids = httpCacheController.getEntityIds(result);
    Response notModified = httpCacheController.getNotModified(request, ids);
    if (notModified != null) {
      return notModified;
    }

    return httpCacheController.sendModified(result, ids);
  }

  @Override
  public Response findOrganizationNewsArticle(String organizationIdParam, String newsArticleIdParam, @Context Request request) {
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
    NewsArticleId newsArticleId = toNewsArticleId(organizationId, newsArticleIdParam);
    
    Response notModified = httpCacheController.getNotModified(request, newsArticleId);
    if (notModified != null) {
      return notModified;
    }
    
    NewsArticle newsArticle = newsController.findNewsArticle(organizationId, newsArticleId);
    if (newsArticle != null) {
      return httpCacheController.sendModified(newsArticle, newsArticle.getId());
    }    
    
    return Response.status(Status.NOT_FOUND)
      .build();
  }

  @Override
  public Response findOrganizationNewsArticleImage(String organizationIdParam, String newsArticleIdParam, String imageIdParam, @Context Request request) {
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
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
    
    return Response.status(Status.NOT_FOUND)
      .build();
  }

  @Override
  public Response getOrganizationNewsArticleImageData(String organizationIdParam, String newsArticleIdParam, String imageIdParam, Integer size, @Context Request request) {
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
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

    return Response.status(Status.NOT_FOUND)
      .build();
  }

  @Override
  public Response listOrganizationNewsArticleImages(String organizationIdParam, String newsArticleIdParam, @Context Request request) {
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
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
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
    
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
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
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
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
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
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
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
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
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

    return Response.status(Status.NOT_FOUND)
      .build();
  }
  
  /* Tiles */
  
  @Override
  public Response listOrganizationTiles(String organizationIdParam, @Context Request request) {
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
    
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
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
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
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
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
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
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
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
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
    
    return Response.status(Status.NOT_FOUND)
      .build();
  }

  @Override
  @SuppressWarnings("squid:MethodCyclomaticComplexity")
  public Response createOrganizationSetting(String organizationIdParam, OrganizationSetting setting, @Context Request request) {
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
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
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
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
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
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
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
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
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
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
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
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
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
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
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
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
    
    List<LocalizedValue> pageContents = pageController.getPageContents(organizationId, pageId);
    if (pageContents != null) {
      return httpCacheController.sendModified(pageContents, pageId.getId());
    }
    
    return createNotFound(NOT_FOUND);
  }

  @Override
  public Response listOrganizationPageImages(String organizationIdParam, String pageIdParam, @Context Request request) {
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
    PageId pageId = toPageId(organizationId, pageIdParam);
    
    List<Attachment> result = pageController.listPageImages(organizationId, pageId);
    
    List<String> ids = httpCacheController.getEntityIds(result);
    Response notModified = httpCacheController.getNotModified(request, ids);
    if (notModified != null) {
      return notModified;
    }

    return httpCacheController.sendModified(result, ids);
  }

  @Override
  public Response findOrganizationPageImage(String organizationIdParam, String pageIdParam, String imageIdParam, @Context Request request) {
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
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
    
    return Response.status(Status.NOT_FOUND)
      .build();
  }

  @Override
  public Response getOrganizationPageImageData(String organizationIdParam, String pageIdParam, String imageIdParam, Integer size, @Context Request request) {
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
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
    
    return Response.status(Status.NOT_FOUND)
      .build();
  }
  
  /* Menus */

  @Override
  public Response listOrganizationMenus(String organizationIdParam, String slug, @Context Request request) {
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
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
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
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
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
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
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
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
  public Response listOrganizationFiles(String organizationId, String pageId, @Context Request request) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }
  
  @Override
  public Response findOrganizationFile(String organizationId, String fileId, @Context Request request) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }

  @Override
  public Response getOrganizationFileData(String organizationId, String fileId, @Context Request request) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }
  
  /* Jobs */

  @Override
  public Response findOrganizationJob(String organizationIdParam, String jobIdParam, @Context Request request) {
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
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
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
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
  public Response findOrganizationAnnouncement(String organizationId, String announcementId, @Context Request request) {
    return null;
  }

  @Override
  public Response listOrganizationAnnouncements(String organizationId, Integer firstResult, Integer maxResults,
      String sortBy, String sortDir, @Context Request request) {
    return null;
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

  private OrganizationId toOrganizationId(String id) {
    if (StringUtils.isNotBlank(id)) {
      return new OrganizationId(KuntaApiConsts.IDENTIFIER_NAME, id);
    }
    
    return null;
  }

  private OrganizationServiceId toOrganizationServiceId(OrganizationId organizationId, String id) {
    if (StringUtils.isNotBlank(id)) {
      return new OrganizationServiceId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, id);
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
  
  @SuppressWarnings("unused")
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
  
  private OffsetDateTime getDateTime(String timeString) {
    if (StringUtils.isNotBlank(timeString)) {
      return OffsetDateTime.parse(timeString);
    }
    
    return null;
  }
  
  private List<OrganizationServiceProvider> getOrganizationServiceProviders() {
    List<OrganizationServiceProvider> result = new ArrayList<>();
    
    Iterator<OrganizationServiceProvider> iterator = organizationServiceProviders.iterator();
    while (iterator.hasNext()) {
      result.add(iterator.next());
    }
    
    return Collections.unmodifiableList(result);
  }
  
}