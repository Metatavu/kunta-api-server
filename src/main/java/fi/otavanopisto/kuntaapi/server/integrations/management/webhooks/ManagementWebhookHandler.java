package fi.otavanopisto.kuntaapi.server.integrations.management.webhooks;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import fi.metatavu.management.client.ApiResponse;
import fi.metatavu.management.client.DefaultApi;
import fi.metatavu.management.client.model.Banner;
import fi.metatavu.management.client.model.Page;
import fi.metatavu.management.client.model.Post;
import fi.metatavu.management.client.model.Tile;
import fi.otavanopisto.kuntaapi.server.id.BannerId;
import fi.otavanopisto.kuntaapi.server.id.NewsArticleId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PageId;
import fi.otavanopisto.kuntaapi.server.id.TileId;
import fi.otavanopisto.kuntaapi.server.integrations.management.ManagementApi;
import fi.otavanopisto.kuntaapi.server.integrations.management.ManagementConsts;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask.Operation;
import fi.otavanopisto.kuntaapi.server.tasks.TaskRequest;
import fi.otavanopisto.kuntaapi.server.webhooks.WebhookHandler;

@RequestScoped
@SuppressWarnings ({"squid:S1301", "squid:S3306"})
public class ManagementWebhookHandler implements WebhookHandler {
  
  @Inject
  private Logger logger;

  @Inject
  private ManagementApi managementApi;

  @Inject
  private Event<TaskRequest> taskRequest;
  
  @Override
  public String getType() {
    return "management";
  }

  @Override
  public boolean handle(OrganizationId organizationId, HttpServletRequest request) {
    Payload payload = parsePayload(request);

    if (StringUtils.isBlank(payload.getHook())) {
      logger.log(Level.SEVERE, "Received a webhook without hook");
      return false;
    }
    
    switch (payload.getHook()) {
      case "edit_post":
        if (validateEditPost(payload)) {
          return handleEditPost(organizationId, payload);
        }
      default:
    }
      
    return false;
  }

  private Payload parsePayload(HttpServletRequest request) {
    Payload payload = new Payload();
    payload.setId(request.getParameter("ID"));
    payload.setPostStatus(request.getParameter("post_status"));
    payload.setHook(request.getParameter("hook"));
    payload.setPostType(request.getParameter("post_type"));
    return payload;
  }

  private boolean validateEditPost(Payload payload) {
    if (StringUtils.isBlank(payload.getPostStatus())) {
      logger.log(Level.SEVERE, "Received a edit_post webhook without post_status");
      return false;
    }
    
    if (StringUtils.isBlank(payload.getId())) {
      logger.log(Level.SEVERE, "Received an edit_post webhook without ID");
      return false;
    }
    
    if (StringUtils.isBlank(payload.getPostType())) {
      logger.log(Level.SEVERE, "Received an edit_post webhook without post_type");
      return false;
    }
    
    return true;
  }

  private boolean handleEditPost(OrganizationId organizationId, Payload payload) {
    switch (payload.getPostStatus()) {
      case "trash":
        return handleTrash(organizationId, payload);
      case "publish":
        return handlePublish(organizationId, payload);
      default:
    }
    
    return false;
  }
  
  private boolean handlePublish(OrganizationId organizationId, Payload payload) {
    switch (payload.getPostType()) {
      case "page":
        return handlePublishPage(organizationId, payload);
      case "banner":
        return handlePublishBanner(organizationId, payload);
      case "post":
        return handlePublishPost(organizationId, payload);
      case "tile":
        return handleTilePublish(organizationId, payload);
      default:
    }
    
    return false;
  }
  
  private boolean handleTrash(OrganizationId organizationId, Payload payload) {
    switch (payload.getPostType()) {
      case "page":
        PageId pageId = new PageId(organizationId, ManagementConsts.IDENTIFIER_NAME, payload.getId());
        taskRequest.fire(new TaskRequest(false, new IdTask<PageId>(Operation.REMOVE, pageId)));
        return true;
      case "banner":
        BannerId bannerId = new BannerId(organizationId, ManagementConsts.IDENTIFIER_NAME, payload.getId());
        taskRequest.fire(new TaskRequest(false, new IdTask<BannerId>(Operation.REMOVE, bannerId)));
        return true;
      case "post":
        NewsArticleId newsArticleId = new NewsArticleId(organizationId, ManagementConsts.IDENTIFIER_NAME, payload.getId());
        taskRequest.fire(new TaskRequest(false, new IdTask<NewsArticleId>(Operation.REMOVE, newsArticleId)));
        return true;
      case "tile":
        TileId tileId = new TileId(organizationId, ManagementConsts.IDENTIFIER_NAME, payload.getId());
        taskRequest.fire(new TaskRequest(false, new IdTask<TileId>(Operation.REMOVE, tileId)));
        return true;
      default:
    }
    
    return false;
  }

  private boolean handlePublishPage(OrganizationId organizationId, Payload payload) {
    PageId pageId = new PageId(organizationId, ManagementConsts.IDENTIFIER_NAME, payload.getId());
    Long orderIndex = getPageOrderIndex(pageId);
    if (orderIndex != null) {
      taskRequest.fire(new TaskRequest(true, new IdTask<PageId>(Operation.UPDATE, pageId, orderIndex)));
    } else {
      logger.warning(String.format("Failed to resolve order index for page %s", pageId));
    }
    
    return true;
  }

  private boolean handlePublishBanner(OrganizationId organizationId, Payload payload) {
    BannerId bannerId = new BannerId(organizationId, ManagementConsts.IDENTIFIER_NAME, payload.getId());
    Long orderIndex = getBannerOrderIndex(bannerId);
    if (orderIndex != null) {
      taskRequest.fire(new TaskRequest(true, new IdTask<BannerId>(Operation.UPDATE, bannerId, orderIndex)));
    } else {
      logger.warning(String.format("Failed to resolve order index for banner %s", bannerId));
    }
    
    return true;
  }

  private boolean handlePublishPost(OrganizationId organizationId, Payload payload) {
    NewsArticleId newsArticleId = new NewsArticleId(organizationId, ManagementConsts.IDENTIFIER_NAME, payload.getId());
    Long orderIndex = getPostOrderIndex(newsArticleId);
    if (orderIndex != null) {
      taskRequest.fire(new TaskRequest(true, new IdTask<NewsArticleId>(Operation.UPDATE, newsArticleId, orderIndex)));
    } else {
      logger.warning(String.format("Failed to resolve order index for news article %s", newsArticleId));
    }
    
    return true;
  }

  private boolean handleTilePublish(OrganizationId organizationId, Payload payload) {
    TileId tileId = new TileId(organizationId, ManagementConsts.IDENTIFIER_NAME, payload.getId());
    Long orderIndex = getTileOrderIndex(tileId);
    if (orderIndex != null) {
      taskRequest.fire(new TaskRequest(true, new IdTask<TileId>(Operation.UPDATE, tileId, orderIndex)));
    } else {
      logger.warning(String.format("Failed to resolve order index for tile %s", tileId));
    }
    
    return true;
  }
  
  private Long getPageOrderIndex(PageId managementPageId) {
    OrganizationId organizationId = managementPageId.getOrganizationId();
    DefaultApi api = managementApi.getApi(organizationId);
    
    ApiResponse<List<Page>> response = api.wpV2PagesGet(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    if (response.isOk()) {
      List<Page> pages = response.getResponse();
      for (int i = 0; i < pages.size(); i++) {
        if (StringUtils.equals(String.valueOf(pages.get(i).getId()), managementPageId.getId())) {
          return (long) i;
        }
      }
    } else {
      logger.warning(String.format("Listing organization %s pages failed on [%d] %s", organizationId.getId(), response.getStatus(), response.getMessage()));
    }
    
    return null;
  }
  
  private Long getBannerOrderIndex(BannerId managementBannerId) {
    OrganizationId organizationId = managementBannerId.getOrganizationId();
    DefaultApi api = managementApi.getApi(organizationId);
    
    fi.metatavu.management.client.ApiResponse<List<Banner>> response = api.wpV2BannerGet(null, null, null, null, null, null, null, null, null, null, null, null, null);
    if (response.isOk()) {
      List<Banner> banners = response.getResponse();
      for (int i = 0; i < banners.size(); i++) {
        if (StringUtils.equals(String.valueOf(banners.get(i).getId()), managementBannerId.getId())) {
          return (long) i;
        }
      }
    } else {
      logger.warning(String.format("Listing organization %s banners failed on [%d] %s", organizationId.getId(), response.getStatus(), response.getMessage()));
    }
    
    return null;
  }
  
  private Long getPostOrderIndex(NewsArticleId managementNewsArticleId) {
    OrganizationId organizationId = managementNewsArticleId.getOrganizationId();
    DefaultApi api = managementApi.getApi(organizationId);
    
    ApiResponse<List<Post>> response = api.wpV2PostsGet(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    if (response.isOk()) {
      List<Post> posts = response.getResponse();
      for (int i = 0; i < posts.size(); i++) {
        if (StringUtils.equals(String.valueOf(posts.get(i).getId()), managementNewsArticleId.getId())) {
          return (long) i;
        }
      }
    } else {
      logger.warning(String.format("Listing organization %s posts failed on [%d] %s", organizationId.getId(), response.getStatus(), response.getMessage()));
    }
    
    return null;
  }
  
  private Long getTileOrderIndex(TileId managementTileId) {
    OrganizationId organizationId = managementTileId.getOrganizationId();
    DefaultApi api = managementApi.getApi(organizationId);
    
    ApiResponse<List<Tile>> response = api.wpV2TileGet(null, null, null, null, null, null, null, null, null, null, null, null, null);
    if (response.isOk()) {
      List<Tile> tiles = response.getResponse();
      for (int i = 0; i < tiles.size(); i++) {
        if (StringUtils.equals(String.valueOf(tiles.get(i).getId()), managementTileId.getId())) {
          return (long) i;
        }
      }
    } else {
      logger.warning(String.format("Listing organization %s tiles failed on [%d] %s", organizationId.getId(), response.getStatus(), response.getMessage()));
    }
    
    return null;
  }


}
