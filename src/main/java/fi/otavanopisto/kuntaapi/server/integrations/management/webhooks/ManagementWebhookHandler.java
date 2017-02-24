package fi.otavanopisto.kuntaapi.server.integrations.management.webhooks;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import fi.otavanopisto.kuntaapi.server.id.BannerId;
import fi.otavanopisto.kuntaapi.server.id.NewsArticleId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PageId;
import fi.otavanopisto.kuntaapi.server.id.TileId;
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
  private Event<TaskRequest> taskRequest;
  
  @Override
  public String getType() {
    return "management";
  }

  @Override
  @SuppressWarnings ("squid:S128")
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
    taskRequest.fire(new TaskRequest(true, new IdTask<PageId>(Operation.UPDATE, pageId, null)));
    return true;
  }

  private boolean handlePublishBanner(OrganizationId organizationId, Payload payload) {
    BannerId bannerId = new BannerId(organizationId, ManagementConsts.IDENTIFIER_NAME, payload.getId());
    taskRequest.fire(new TaskRequest(true, new IdTask<BannerId>(Operation.UPDATE, bannerId, null)));
    return true;
  }

  private boolean handlePublishPost(OrganizationId organizationId, Payload payload) {
    NewsArticleId newsArticleId = new NewsArticleId(organizationId, ManagementConsts.IDENTIFIER_NAME, payload.getId());
    taskRequest.fire(new TaskRequest(true, new IdTask<NewsArticleId>(Operation.UPDATE, newsArticleId, null)));
    return true;
  }

  private boolean handleTilePublish(OrganizationId organizationId, Payload payload) {
    TileId tileId = new TileId(organizationId, ManagementConsts.IDENTIFIER_NAME, payload.getId());
    taskRequest.fire(new TaskRequest(true, new IdTask<TileId>(Operation.UPDATE, tileId, null)));
    return true;
  }

}
