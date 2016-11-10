package fi.otavanopisto.kuntaapi.server.rest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateful;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import fi.otavanopisto.kuntaapi.server.controllers.OrganizationController;
import fi.otavanopisto.kuntaapi.server.controllers.PageController;
import fi.otavanopisto.kuntaapi.server.id.AttachmentId;
import fi.otavanopisto.kuntaapi.server.id.BannerId;
import fi.otavanopisto.kuntaapi.server.id.EventId;
import fi.otavanopisto.kuntaapi.server.id.JobId;
import fi.otavanopisto.kuntaapi.server.id.MenuId;
import fi.otavanopisto.kuntaapi.server.id.MenuItemId;
import fi.otavanopisto.kuntaapi.server.id.NewsArticleId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationServiceId;
import fi.otavanopisto.kuntaapi.server.id.PageId;
import fi.otavanopisto.kuntaapi.server.id.TileId;
import fi.otavanopisto.kuntaapi.server.integrations.AttachmentData;
import fi.otavanopisto.kuntaapi.server.integrations.BannerProvider;
import fi.otavanopisto.kuntaapi.server.integrations.EventProvider;
import fi.otavanopisto.kuntaapi.server.integrations.FileId;
import fi.otavanopisto.kuntaapi.server.integrations.JobProvider;
import fi.otavanopisto.kuntaapi.server.integrations.JobProvider.JobOrder;
import fi.otavanopisto.kuntaapi.server.integrations.JobProvider.JobOrderDirection;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.integrations.MenuProvider;
import fi.otavanopisto.kuntaapi.server.integrations.NewsProvider;
import fi.otavanopisto.kuntaapi.server.integrations.OrganizationServiceProvider;
import fi.otavanopisto.kuntaapi.server.integrations.TileProvider;
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
  private static final String FAILED_TO_STREAM_IMAGE_TO_CLIENT = "Failed to stream image to client";

  @Inject
  private Logger logger;
  
  @Inject
  private OrganizationSettingProvider organizationSettingProvider;
  
  @Inject
  private OrganizationController organizationController;
  
  @Inject
  private PageController pageController;
  
  @Inject
  private Instance<OrganizationServiceProvider> organizationServiceProviders;
  
  @Inject
  private Instance<EventProvider> eventProviders;

  @Inject
  private Instance<NewsProvider> newsProviders;

  @Inject
  private Instance<BannerProvider> bannerProviders;

  @Inject
  private Instance<TileProvider> tileProviders;

  @Inject
  private Instance<MenuProvider> menuProviders;

  @Inject
  private Instance<JobProvider> jobProviders;
  
  @Override
  public Response listOrganizations(String businessName, String businessCode, String search, Long firstResult, Long maxResults) {
    List<Organization> organizations;
    
    if (search != null) {
      organizations = organizationController.searchOrganizations(search, businessName, businessCode, firstResult, maxResults);
    } else {
      organizations = organizationController.listOrganizations(businessName, businessCode, firstResult, maxResults);
    }
    
    return Response.ok(organizations)
      .build();
  }
  
  @Override
  public Response findOrganization(String organizationIdParam) {
  	OrganizationId organizationId = toOrganizationId(organizationIdParam);
  	Organization organization = organizationController.findOrganization(organizationId);
  	
  	if (organization != null) {
      return Response.ok(organization)
        .build();
    }
    
    return Response
      .status(Status.NOT_FOUND)
      .build();
  }
  
  @Override
  public Response createOrganizationService(String organizationId, OrganizationService body) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }
  
  @Override
  public Response findOrganizationService(String organizationIdParam, String organizationServiceIdParam) {
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
    OrganizationServiceId organizationServiceId = toOrganizationServiceId(organizationServiceIdParam);
    
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
  public Response listOrganizationOrganizationServices(String organizationIdParam, Long firstResult, Long maxResults) {
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
      OrganizationService body) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }

  @Override
  public Response findOrganizationEvent(String organizationIdParam, String eventIdParam) {
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
    EventId eventId = toEventId(eventIdParam);
    
    for (EventProvider eventProvider : getEventProviders()) {
      Event event = eventProvider.findOrganizationEvent(organizationId, eventId);
      if (event != null) {
        return Response.ok(event)
          .build();
      }
    }
    
    return Response.status(Status.NOT_FOUND)
      .build();
  }

  @Override
  public Response findOrganizationEventImage(String organizationIdParam, String eventIdParam, String imageIdParam) {
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
    EventId eventId = toEventId(eventIdParam);
    AttachmentId attachmentId = new AttachmentId(KuntaApiConsts.IDENTIFIER_NAME, imageIdParam);
    
    for (EventProvider eventProvider : getEventProviders()) {
      Attachment attachment = eventProvider.findEventImage(organizationId, eventId, attachmentId);
      if (attachment != null) {
        return Response.ok(attachment)
          .build();
      }
    }
    
    return Response.status(Status.NOT_FOUND)
      .build();
  }

  @Override
  public Response getOrganizationEventImageData(String organizationIdParam, String eventIdParam, String imageIdParam, Integer size) {
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
    EventId eventId = toEventId(eventIdParam);
    AttachmentId attachmentId = new AttachmentId(KuntaApiConsts.IDENTIFIER_NAME, imageIdParam);
    
    for (EventProvider eventProvider : getEventProviders()) {
      AttachmentData attachmentData = eventProvider.getEventImageData(organizationId, eventId, attachmentId, size);
      if (attachmentData != null) {
        try (InputStream stream = new ByteArrayInputStream(attachmentData.getData())) {
          return Response.ok(stream, attachmentData.getType())
              .build();
        } catch (IOException e) {
          logger.log(Level.SEVERE, FAILED_TO_STREAM_IMAGE_TO_CLIENT, e);
          return Response.status(Status.INTERNAL_SERVER_ERROR)
            .entity(INTERNAL_SERVER_ERROR)
            .build();
        }
      }
    }
    
    return Response.status(Status.NOT_FOUND)
      .build();
  }

  @Override
  public Response listOrganizationEventImages(String organizationIdParam, String eventIdParam) {
    List<Attachment> result = new ArrayList<>();
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
    EventId eventId = toEventId(eventIdParam);
    
    for (EventProvider eventProvider : getEventProviders()) {
      result.addAll(eventProvider.listEventImages(organizationId, eventId));
    }
    
    return Response.ok(result)
      .build();
  }

  @Override
  public Response listOrganizationEvents(String organizationIdParam, 
      String startBefore, String startAfter,
      String endBefore, String endAfter,
      Integer firstResult, Integer maxResults,
      String orderBy, String orderDir) {
    
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
    
    List<Event> result = new ArrayList<>();
   
    for (EventProvider eventProvider : getEventProviders()) {
      result.addAll(eventProvider.listOrganizationEvents(organizationId, getDateTime(startBefore), getDateTime(startAfter), getDateTime(endBefore), getDateTime(endAfter), order, orderDirection, firstResult, maxResults));
    }
    
    return Response.ok(result)
      .build();
  }
  
  /* News */

  @Override
  public Response listOrganizationNews(String organizationIdParam, String publishedBefore, String publishedAfter,
      Integer firstResult, Integer maxResults) {
    
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
    
    List<NewsArticle> result = new ArrayList<>();
   
    for (NewsProvider newsProvider : getNewsProviders()) {
      result.addAll(newsProvider.listOrganizationNews(organizationId, getDateTime(publishedBefore), getDateTime(publishedAfter), firstResult, maxResults));
    }
    
    return Response.ok(result)
      .build();
    
  }

  @Override
  public Response findOrganizationNewsArticle(String organizationIdParam, String newsArticleIdParam) {
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
    NewsArticleId newsArticleId = toNewsArticleId(newsArticleIdParam);
    
    for (NewsProvider newsProvider : getNewsProviders()) {
      NewsArticle newsArticle = newsProvider.findOrganizationNewsArticle(organizationId, newsArticleId);
      if (newsArticle != null) {
        return Response.ok(newsArticle)
          .build();
      }
    }
    
    return Response.status(Status.NOT_FOUND)
      .build();
  }

  @Override
  public Response findOrganizationNewsArticleImage(String organizationIdParam, String newsArticleIdParam, String imageIdParam) {
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
    NewsArticleId newsArticleId = toNewsArticleId(newsArticleIdParam);
    AttachmentId attachmentId = toAttachmentId(imageIdParam);
    
    for (NewsProvider newsProvider : getNewsProviders()) {
      Attachment attachment = newsProvider.findNewsArticleImage(organizationId, newsArticleId, attachmentId);
      if (attachment != null) {
        return Response.ok(attachment)
          .build();
      }
    }
    
    return Response.status(Status.NOT_FOUND)
      .build();
  }

  @Override
  public Response getOrganizationNewsArticleImageData(String organizationIdParam, String newsArticleIdParam, String imageIdParam, Integer size) {
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
    NewsArticleId newsArticleId = toNewsArticleId(newsArticleIdParam);
    AttachmentId attachmentId = toAttachmentId(imageIdParam);
    
    for (NewsProvider newsProvider : getNewsProviders()) {
      AttachmentData attachmentData = newsProvider.getNewsArticleImageData(organizationId, newsArticleId, attachmentId, size);
      if (attachmentData != null) {
        try (InputStream stream = new ByteArrayInputStream(attachmentData.getData())) {
          return Response.ok(stream, attachmentData.getType())
              .build();
        } catch (IOException e) {
          logger.log(Level.SEVERE, FAILED_TO_STREAM_IMAGE_TO_CLIENT, e);
          return Response.status(Status.INTERNAL_SERVER_ERROR)
            .entity(INTERNAL_SERVER_ERROR)
            .build();
        }
      }
    }
    
    return Response.status(Status.NOT_FOUND)
      .build();
  }

  @Override
  public Response listOrganizationNewsArticleImages(String organizationIdParam, String newsArticleIdParam) {
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
    NewsArticleId newsArticleId = toNewsArticleId(newsArticleIdParam);
    
    List<Attachment> result = new ArrayList<>();
   
    for (NewsProvider newsProvider : getNewsProviders()) {
      result.addAll(newsProvider.listNewsArticleImages(organizationId, newsArticleId));
    }
    
    return Response.ok(result)
      .build();
  }

  /* Banners */
  
  @Override
  public Response listOrganizationBanners(String organizationIdParam) {
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
    
    List<Banner> result = new ArrayList<>();
   
    for (BannerProvider bannerProvider : getBannerProviders()) {
      result.addAll(bannerProvider.listOrganizationBanners(organizationId));
    }

    return Response.ok(result)
      .build();
  }

  @Override
  public Response findOrganizationBanner(String organizationIdParam, String bannerIdParam) {
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
    BannerId bannerId = toBannerId(bannerIdParam);
    
    for (BannerProvider bannerProvider : getBannerProviders()) {
      Banner banner = bannerProvider.findOrganizationBanner(organizationId, bannerId);
      if (banner != null) {
        return Response.ok(banner)
          .build();
      }
    }
    
    return Response.status(Status.NOT_FOUND)
      .build();
  }

  @Override
  public Response listOrganizationBannerImages(String organizationIdParam, String bannerIdParam) {
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
    BannerId bannerId = toBannerId(bannerIdParam);
    
    List<Attachment> result = new ArrayList<>();
   
    for (BannerProvider bannerProvider : getBannerProviders()) {
      result.addAll(bannerProvider.listOrganizationBannerImages(organizationId, bannerId));
    }
    
    return Response.ok(result)
      .build();
  }

  @Override
  public Response findOrganizationBannerImage(String organizationIdParam, String bannerIdParam, String imageIdParam) {
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
    BannerId bannerId = toBannerId(bannerIdParam);
    AttachmentId attachmentId = toAttachmentId(imageIdParam);
    
    for (BannerProvider bannerProvider : getBannerProviders()) {
      Attachment attachment = bannerProvider.findBannerImage(organizationId, bannerId, attachmentId);
      if (attachment != null) {
        return Response.ok(attachment)
          .build();
      }
    }
    
    return Response.status(Status.NOT_FOUND)
      .build();
  }

  @Override
  public Response getOrganizationBannerImageData(String organizationIdParam, String bannerIdParam, String imageIdParam, Integer size) {
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
    BannerId bannerId = toBannerId(bannerIdParam);
    AttachmentId attachmentId = toAttachmentId(imageIdParam);
    
    for (BannerProvider bannerProvider : getBannerProviders()) {
      AttachmentData attachmentData = bannerProvider.getBannerImageData(organizationId, bannerId, attachmentId, size);
      if (attachmentData != null) {
        try (InputStream stream = new ByteArrayInputStream(attachmentData.getData())) {
          return Response.ok(stream, attachmentData.getType())
              .build();
        } catch (IOException e) {
          logger.log(Level.SEVERE, FAILED_TO_STREAM_IMAGE_TO_CLIENT, e);
          return Response.status(Status.INTERNAL_SERVER_ERROR)
            .entity(INTERNAL_SERVER_ERROR)
            .build();
        }
      }
    }
    
    return Response.status(Status.NOT_FOUND)
      .build();
  }
  
  /* Tiles */
  
  @Override
  public Response listOrganizationTiles(String organizationIdParam) {
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
    
    List<Tile> result = new ArrayList<>();
   
    for (TileProvider tileProvider : getTileProviders()) {
      result.addAll(tileProvider.listOrganizationTiles(organizationId));
    }
    
    return Response.ok(result)
      .build();
  }

  @Override
  public Response findOrganizationTile(String organizationIdParam, String tileIdParam) {
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
    TileId tileId = toTileId(tileIdParam);
    
    for (TileProvider tileProvider : getTileProviders()) {
      Tile tile = tileProvider.findOrganizationTile(organizationId, tileId);
      if (tile != null) {
        return Response.ok(tile)
          .build();
      }
    }
    
    return Response.status(Status.NOT_FOUND)
      .build();
  }

  @Override
  public Response listOrganizationTileImages(String organizationIdParam, String tileIdParam) {
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
    TileId tileId = toTileId(tileIdParam);
    
    List<Attachment> result = new ArrayList<>();
   
    for (TileProvider tileProvider : getTileProviders()) {
      result.addAll(tileProvider.listOrganizationTileImages(organizationId, tileId));
    }
    
    return Response.ok(result)
      .build();
  }

  @Override
  public Response findOrganizationTileImage(String organizationIdParam, String tileIdParam, String imageIdParam) {
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
    TileId tileId = toTileId(tileIdParam);
    AttachmentId attachmentId = toAttachmentId(imageIdParam);
    
    for (TileProvider tileProvider : getTileProviders()) {
      Attachment attachment = tileProvider.findTileImage(organizationId, tileId, attachmentId);
      if (attachment != null) {
        return Response.ok(attachment)
          .build();
      }
    }
    
    return Response.status(Status.NOT_FOUND)
      .build();
  }

  @Override
  public Response getOrganizationTileImageData(String organizationIdParam, String tileIdParam, String imageIdParam, Integer size) {
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
    TileId tileId = toTileId(tileIdParam);
    AttachmentId attachmentId = toAttachmentId(imageIdParam);
    
    for (TileProvider tileProvider : getTileProviders()) {
      AttachmentData attachmentData = tileProvider.getTileImageData(organizationId, tileId, attachmentId, size);
      if (attachmentData != null) {
        try (InputStream stream = new ByteArrayInputStream(attachmentData.getData())) {
          return Response.ok(stream, attachmentData.getType())
              .build();
        } catch (IOException e) {
          logger.log(Level.SEVERE, FAILED_TO_STREAM_IMAGE_TO_CLIENT, e);
          return Response.status(Status.INTERNAL_SERVER_ERROR)
            .entity(INTERNAL_SERVER_ERROR)
            .build();
        }
      }
    }
    
    return Response.status(Status.NOT_FOUND)
      .build();
  }

  @Override
  @SuppressWarnings("squid:MethodCyclomaticComplexity")
  public Response createOrganizationSetting(String organizationIdParam, OrganizationSetting setting) {
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
  public Response listOrganizationSettings(String organizationIdParam, String key) {
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
  public Response findOrganizationSetting(String organizationIdParam, String settingIdParam) {
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
  public Response updateOrganizationSetting(String organizationIdParam, String settingIdParam, OrganizationSetting setting) {
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
  public Response deleteOrganizationSetting(String organizationIdParam, String settingIdParam) {
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
  public Response listOrganizationPages(String organizationIdParam, String parentIdParam, String path, String search, Long firstResult, Long maxResults) {
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
    if (organizationId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    if (search != null && (parentIdParam != null || path != null)) {
      return createNotImplemented("Search parameter can not be combined with path or parentId parameters");
    }
    
    List<Page> result = null;
    
    if (search != null) {
      result = pageController.searchPages(organizationId, search, firstResult, maxResults);
    } else {
      boolean onlyRootPages = StringUtils.equals("ROOT", parentIdParam);
      PageId parentId = onlyRootPages ? null : toPageId(parentIdParam);
      result = pageController.listPages(organizationId, path, onlyRootPages, parentId, firstResult, maxResults);
    }
    
    return Response.ok(result)
      .build();
  }

  @Override
  public Response findOrganizationPage(String organizationIdParam, String pageIdParam) {
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
    if (organizationId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    PageId pageId = toPageId(pageIdParam);
    if (pageId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    Page page = pageController.findPage(organizationId, pageId);
    if (page != null) {
      return Response.ok(page).build();
    }
    
    return createNotFound(NOT_FOUND);
  }
  
  @Override
  public Response findOrganizationPageContent(String organizationIdParam, String pageIdParam) {
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
    if (organizationId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    PageId pageId = toPageId(pageIdParam);
    if (pageId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    List<LocalizedValue> pageContents = pageController.getPageContents(organizationId, pageId);
    if (pageContents != null) {
      return Response.ok(pageContents).build();
    }
    
    return createNotFound(NOT_FOUND);
  }

  @Override
  public Response listOrganizationPageImages(String organizationIdParam, String pageIdParam) {
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
    PageId pageId = toPageId(pageIdParam);
    
    List<Attachment> result = pageController.listPages(organizationId, pageId);
    
    return Response.ok(result)
      .build();
  }

  @Override
  public Response findOrganizationPageImage(String organizationIdParam, String pageIdParam, String imageIdParam) {
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
    PageId pageId = toPageId(pageIdParam);
    AttachmentId attachmentId = toAttachmentId(imageIdParam);
    
    Attachment attachment = pageController.findPageImage(organizationId, pageId, attachmentId);
    if (attachment != null) {
      return Response.ok(attachment)
        .build();
    }
    
    return Response.status(Status.NOT_FOUND)
      .build();
  }

  @Override
  public Response getOrganizationPageImageData(String organizationIdParam, String pageIdParam, String imageIdParam, Integer size) {
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
    PageId pageId = toPageId(pageIdParam);
    AttachmentId attachmentId = toAttachmentId(imageIdParam);
    
    AttachmentData attachmentData = pageController.getPageAttachmentData(organizationId, pageId, attachmentId, size);
    if (attachmentData != null) {
      try (InputStream stream = new ByteArrayInputStream(attachmentData.getData())) {
        return Response.ok(stream, attachmentData.getType())
            .build();
      } catch (IOException e) {
        logger.log(Level.SEVERE, FAILED_TO_STREAM_IMAGE_TO_CLIENT, e);
        return Response.status(Status.INTERNAL_SERVER_ERROR)
          .entity(INTERNAL_SERVER_ERROR)
          .build();
      }
    }
    
    return Response.status(Status.NOT_FOUND)
      .build();
  }
  
  /* Menus */

  @Override
  public Response listOrganizationMenus(String organizationIdParam, String slug) {
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
    if (organizationId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    List<Menu> result = new ArrayList<>();
    
    for (MenuProvider menuProvider : getMenuProviders()) {
      result.addAll(menuProvider.listOrganizationMenus(organizationId, slug));
    }
    
    return Response.ok(result)
      .build();
  }
  
  @Override
  public Response findOrganizationMenu(String organizationIdParam, String menuIdParam) {
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
    if (organizationId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    MenuId menuId = toMenuId(menuIdParam);
    if (menuId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    for (MenuProvider menuProvider : getMenuProviders()) {
      Menu menu = menuProvider.findOrganizationMenu(organizationId, menuId);
      if (menu != null) {
        return Response.ok(menu).build();
      }
    }
    
    return createNotFound(NOT_FOUND);
  }
  
  /* Menu Items */

  @Override
  public Response listOrganizationMenuItems(String organizationIdParam, String menuIdParam) {
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
    if (organizationId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    MenuId menuId = toMenuId(menuIdParam);
    if (menuId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    List<MenuItem> result = new ArrayList<>();
    
    for (MenuProvider menuProvider : getMenuProviders()) {
      result.addAll(menuProvider.listOrganizationMenuItems(organizationId, menuId));
    }
    
    return Response.ok(result)
      .build();
  }

  @Override
  public Response findOrganizationMenuItem(String organizationIdParam, String menuIdParam, String menuItemIdParam) {
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
    if (organizationId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    MenuId menuId = toMenuId(menuIdParam);
    if (menuId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    MenuItemId menuItemId = toMenuItemId(menuItemIdParam);
    if (menuItemId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    for (MenuProvider menuProvider : getMenuProviders()) {
      MenuItem menuItem = menuProvider.findOrganizationMenuItem(organizationId, menuId, menuItemId);
      if (menuItem != null) {
        return Response.ok(menuItem).build();
      }
    }
    
    return createNotFound(NOT_FOUND);
  }
  
  /* Files */

  @Override
  public Response listOrganizationFiles(String organizationId, String pageId) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }
  
  @Override
  public Response findOrganizationFile(String organizationId, String fileId) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }

  @Override
  public Response getOrganizationFileData(String organizationId, String fileId) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }
  
  /* Jobs */

  @Override
  public Response findOrganizationJob(String organizationIdParam, String jobIdParam) {
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
    if (organizationId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    JobId jobId = toJobId(jobIdParam);
    if (jobId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    for (JobProvider jobProvider : getJobProviders()) {
      Job job = jobProvider.findOrganizationJob(organizationId, jobId);
      if (job != null) {
        return Response.ok(job).build();
      }
    }
    
    return createNotFound(NOT_FOUND);
  }

  @Override
  public Response listOrganizationJobs(String organizationIdParam, String sortBy, String sortDir) {
    OrganizationId organizationId = toOrganizationId(organizationIdParam);
    if (organizationId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    List<Job> result = new ArrayList<>();
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
    
    for (JobProvider jobProvider : getJobProviders()) {
      result.addAll(jobProvider.listOrganizationJobs(organizationId));
    }
    
    return Response.ok(sortJobs(result, order, orderDirection))
      .build();
  }
  
  /* Announcements */

  @Override
  public Response findOrganizationAnnouncement(String organizationId, String announcementId) {
    return null;
  }

  @Override
  public Response listOrganizationAnnouncements(String organizationId, Integer firstResult, Integer maxResults,
      String sortBy, String sortDir) {
    return null;
  }
  
  private List<Job> sortJobs(List<Job> jobs, JobOrder order, JobOrderDirection orderDirection) {
    if (order == null) {
      return jobs;
    }
    
    List<Job> sorted = new ArrayList<>(jobs);
    
    switch (order) {
      case PUBLICATION_END:
        Collections.sort(sorted, (Job o1, Job o2)
          -> orderDirection != JobOrderDirection.ASCENDING 
            ? o2.getPublicationEnd().compareTo(o1.getPublicationEnd())
            : o1.getPublicationEnd().compareTo(o2.getPublicationEnd()));
      break;
      case PUBLICATION_START:
        Collections.sort(sorted, (Job o1, Job o2)
          -> orderDirection != JobOrderDirection.ASCENDING 
            ? o2.getPublicationStart().compareTo(o1.getPublicationStart())
            : o1.getPublicationStart().compareTo(o2.getPublicationStart()));
      break;
      default:
    }

    return sorted;
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
  
  private BannerId toBannerId(String id) {
    if (StringUtils.isNotBlank(id)) {
      return new BannerId(KuntaApiConsts.IDENTIFIER_NAME, id);
    }
    
    return null;
  }
  
  private TileId toTileId(String id) {
    if (StringUtils.isNotBlank(id)) {
      return new TileId(KuntaApiConsts.IDENTIFIER_NAME, id);
    }
    
    return null;
  }
  
  private NewsArticleId toNewsArticleId(String id) {
    if (StringUtils.isNotBlank(id)) {
      return new NewsArticleId(KuntaApiConsts.IDENTIFIER_NAME, id);
    }
    
    return null;
  }

  private OrganizationId toOrganizationId(String id) {
    if (StringUtils.isNotBlank(id)) {
      return new OrganizationId(KuntaApiConsts.IDENTIFIER_NAME, id);
    }
    
    return null;
  }

  private OrganizationServiceId toOrganizationServiceId(String id) {
    if (StringUtils.isNotBlank(id)) {
      return new OrganizationServiceId(KuntaApiConsts.IDENTIFIER_NAME, id);
    }
    
    return null;
  }

  private EventId toEventId(String id) {
    if (StringUtils.isNotBlank(id)) {
      return new EventId(KuntaApiConsts.IDENTIFIER_NAME, id);
    }
    
    return null;
  }

  private AttachmentId toAttachmentId(String id) {
    if (StringUtils.isNotBlank(id)) {
      return new AttachmentId(KuntaApiConsts.IDENTIFIER_NAME, id);
    }
    
    return null;
  }
  
  @SuppressWarnings("unused")
  private FileId toFileId(String id) {
    if (StringUtils.isNotBlank(id)) {
      return new FileId(KuntaApiConsts.IDENTIFIER_NAME, id);
    }
    
    return null;
  }
  
  private PageId toPageId(String id) {
    if (StringUtils.isNotBlank(id)) {
      return new PageId(KuntaApiConsts.IDENTIFIER_NAME, id);
    }
    
    return null;
  }
  
  private MenuId toMenuId(String id) {
    if (StringUtils.isNotBlank(id)) {
      return new MenuId(KuntaApiConsts.IDENTIFIER_NAME, id);
    }
    
    return null;
  }
  
  private MenuItemId toMenuItemId(String id) {
    if (StringUtils.isNotBlank(id)) {
      return new MenuItemId(KuntaApiConsts.IDENTIFIER_NAME, id);
    }
    
    return null;
  }
  
  private JobId toJobId(String id) {
    if (StringUtils.isNotBlank(id)) {
      return new JobId(KuntaApiConsts.IDENTIFIER_NAME, id);
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
  
  private List<EventProvider> getEventProviders() {
    List<EventProvider> result = new ArrayList<>();
    
    Iterator<EventProvider> iterator = eventProviders.iterator();
    while (iterator.hasNext()) {
      result.add(iterator.next());
    }
    
    return Collections.unmodifiableList(result);
  }
  
  private List<NewsProvider> getNewsProviders() {
    List<NewsProvider> result = new ArrayList<>();
    
    Iterator<NewsProvider> iterator = newsProviders.iterator();
    while (iterator.hasNext()) {
      result.add(iterator.next());
    }
    
    return Collections.unmodifiableList(result);
  }
  
  private List<BannerProvider> getBannerProviders() {
    List<BannerProvider> result = new ArrayList<>();
    
    Iterator<BannerProvider> iterator = bannerProviders.iterator();
    while (iterator.hasNext()) {
      result.add(iterator.next());
    }
    
    return Collections.unmodifiableList(result);
  }
  
  private List<TileProvider> getTileProviders() {
    List<TileProvider> result = new ArrayList<>();
    
    Iterator<TileProvider> iterator = tileProviders.iterator();
    while (iterator.hasNext()) {
      result.add(iterator.next());
    }
    
    return Collections.unmodifiableList(result);
  }
  
  private List<MenuProvider> getMenuProviders() {
    List<MenuProvider> result = new ArrayList<>();
    
    Iterator<MenuProvider> iterator = menuProviders.iterator();
    while (iterator.hasNext()) {
      result.add(iterator.next());
    }
    
    return Collections.unmodifiableList(result);
  }

  private List<JobProvider> getJobProviders() {
    List<JobProvider> result = new ArrayList<>();
    
    Iterator<JobProvider> iterator = jobProviders.iterator();
    while (iterator.hasNext()) {
      result.add(iterator.next());
    }
    
    return Collections.unmodifiableList(result);
  }
  
}