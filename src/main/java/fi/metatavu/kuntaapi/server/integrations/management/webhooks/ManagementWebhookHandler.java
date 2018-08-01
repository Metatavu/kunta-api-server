package fi.metatavu.kuntaapi.server.integrations.management.webhooks;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import fi.metatavu.management.client.DefaultApi;
import fi.metatavu.management.client.model.Menu;
import fi.metatavu.kuntaapi.server.id.AnnouncementId;
import fi.metatavu.kuntaapi.server.id.BannerId;
import fi.metatavu.kuntaapi.server.id.FragmentId;
import fi.metatavu.kuntaapi.server.id.IncidentId;
import fi.metatavu.kuntaapi.server.id.MenuId;
import fi.metatavu.kuntaapi.server.id.NewsArticleId;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.id.PageId;
import fi.metatavu.kuntaapi.server.id.ShortlinkId;
import fi.metatavu.kuntaapi.server.id.TileId;
import fi.metatavu.kuntaapi.server.integrations.management.ManagementConsts;
import fi.metatavu.kuntaapi.server.integrations.management.ManagementIdFactory;
import fi.metatavu.kuntaapi.server.integrations.management.client.ManagementApi;
import fi.metatavu.kuntaapi.server.tasks.IdTask;
import fi.metatavu.kuntaapi.server.tasks.IdTask.Operation;
import fi.metatavu.kuntaapi.server.tasks.TaskRequest;
import fi.metatavu.kuntaapi.server.webhooks.WebhookHandler;

@RequestScoped
@SuppressWarnings ({"squid:S1301", "squid:S3306"})
public class ManagementWebhookHandler implements WebhookHandler {
  
  @Inject
  private Logger logger;
  
  @Inject
  private ManagementIdFactory managementIdFactory;
  
  @Inject
  private ManagementApi managementApi;
  
  @Inject
  private Event<TaskRequest> taskRequest;
  
  @Override
  public String getType() {
    return "management";
  }

  @Override
  @SuppressWarnings ("squid:S128")
  public boolean handle(OrganizationId kuntaApiOrganizationId, HttpServletRequest request) {
    Payload payload = parsePayload(request);

    if (StringUtils.isBlank(payload.getHook())) {
      logger.log(Level.SEVERE, "Received a webhook without hook");
      return false;
    }
    
    switch (payload.getHook()) {
      case "edit_post":
        if (validateEditPost(payload)) {
          return handleEditPost(kuntaApiOrganizationId, payload);
        }
      default:
        logger.log(Level.WARNING, () -> String.format("Don't know how to handle hook %s", payload.getHook()));
      break;
    }
      
    return false;
  }

  private Payload parsePayload(HttpServletRequest request) {
    String orderIndexParam = request.getParameter("order_index");
    Long orderIndex = NumberUtils.isNumber(orderIndexParam) ? NumberUtils.createLong(orderIndexParam) : null;
    
    Payload payload = new Payload();
    payload.setId(request.getParameter("ID"));
    payload.setPostStatus(request.getParameter("post_status"));
    payload.setHook(request.getParameter("hook"));
    payload.setPostType(request.getParameter("post_type"));
    payload.setOrderIndex(orderIndex);
    
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

  private boolean handleEditPost(OrganizationId kuntaApiOrganizationId, Payload payload) {
    switch (payload.getPostStatus()) {
      case "trash":
      case "draft":
      case "expiration":
      case "future":
      case "private":
      case "pending":
        return handleTrash(kuntaApiOrganizationId, payload);
      case "publish":
        return handlePublish(kuntaApiOrganizationId, payload);
      default:
        logger.log(Level.WARNING, () -> String.format("Don't know how to handle post status %s", payload.getPostStatus()));
      break;
    }

    return false;
  }
  
  private boolean handlePublish(OrganizationId kuntaApiOrganizationId, Payload payload) {
    logger.log(Level.INFO, () -> String.format("Processing publish of post type %s", payload.getPostType()));
    
    switch (payload.getPostType()) {
      case "page":
        return handlePublishPage(kuntaApiOrganizationId, payload);
      case "banner":
        return handlePublishBanner(kuntaApiOrganizationId, payload);
      case "post":
        return handlePublishPost(kuntaApiOrganizationId, payload);
      case "tile":
        return handleTilePublish(kuntaApiOrganizationId, payload);
      case "shortlink":
        return handleShortlinkPublish(kuntaApiOrganizationId, payload);
      case "incident":
        return handleIncidentPublish(kuntaApiOrganizationId, payload);
      case "fragment":
        return handleFragmentPublish(kuntaApiOrganizationId, payload);
      case "announcement":
        return handleAnnouncementPublish(kuntaApiOrganizationId, payload);
      case "customize_changeset":
      case "nav_menu_item":
        return handleMenuItemPublish(kuntaApiOrganizationId);
      default:
        logger.log(Level.WARNING, () -> String.format("Don't know how to handle publish of post type %s", payload.getPostType()));
      break;
    }
    
    return false;
  }

  private boolean handleTrash(OrganizationId kuntaApiOrganizationId, Payload payload) {
    logger.log(Level.INFO, () -> String.format("Processing trash of post type %s", payload.getPostType()));
    
    switch (payload.getPostType()) {
      case "page":
        return handleTrashPage(kuntaApiOrganizationId, payload);
      case "banner":
        return handleTrashBanner(kuntaApiOrganizationId, payload);
      case "post":
        return handleTrashPost(kuntaApiOrganizationId, payload);
      case "tile":
        return handleTrashTile(kuntaApiOrganizationId, payload);
      case "customize_changeset":
      case "nav_menu_item":
        return handleMenuItemTrash(kuntaApiOrganizationId);
      case "shortlink":
        return handleShortlinkTrash(kuntaApiOrganizationId, payload);
      case "incident":
        return handleIncidentTrash(kuntaApiOrganizationId, payload);
      case "fragment":
        return handleFragmentTrash(kuntaApiOrganizationId, payload);
      case "announcement":
        return handleAnnouncementTrash(kuntaApiOrganizationId, payload);
      default:
        logger.log(Level.WARNING, () -> String.format("Don't know how to handle trashing of post type %s", payload.getPostType()));
      break;
    }
    
    return false;
  }

  private boolean handleTrashTile(OrganizationId kuntaApiOrganizationId, Payload payload) {
    TileId tileId = new TileId(kuntaApiOrganizationId, ManagementConsts.IDENTIFIER_NAME, payload.getId());
    taskRequest.fire(new TaskRequest(false, new IdTask<TileId>(Operation.REMOVE, tileId)));
    return true;
  }

  private boolean handleTrashPost(OrganizationId kuntaApiOrganizationId, Payload payload) {
    NewsArticleId newsArticleId = new NewsArticleId(kuntaApiOrganizationId, ManagementConsts.IDENTIFIER_NAME, payload.getId());
    taskRequest.fire(new TaskRequest(false, new IdTask<NewsArticleId>(Operation.REMOVE, newsArticleId)));
    return true;
  }

  private boolean handleTrashBanner(OrganizationId kuntaApiOrganizationId, Payload payload) {
    BannerId bannerId = new BannerId(kuntaApiOrganizationId, ManagementConsts.IDENTIFIER_NAME, payload.getId());
    taskRequest.fire(new TaskRequest(false, new IdTask<BannerId>(Operation.REMOVE, bannerId)));
    return true;
  }

  private boolean handleTrashPage(OrganizationId kuntaApiOrganizationId, Payload payload) {
    PageId pageId = new PageId(kuntaApiOrganizationId, ManagementConsts.IDENTIFIER_NAME, payload.getId());
    taskRequest.fire(new TaskRequest(false, new IdTask<PageId>(Operation.REMOVE, pageId)));
    return true;
  }

  private boolean handleFragmentTrash(OrganizationId kuntaApiOrganizationId, Payload payload) {
    FragmentId fragmentId = managementIdFactory.createFragmentId(kuntaApiOrganizationId, payload.getId());
    taskRequest.fire(new TaskRequest(false, new IdTask<FragmentId>(Operation.REMOVE, fragmentId)));
    return true;
  }
  
  private boolean handleShortlinkTrash(OrganizationId kuntaApiOrganizationId, Payload payload) {
    ShortlinkId shortlinkId = managementIdFactory.createShortlinkId(kuntaApiOrganizationId, payload.getId());
    taskRequest.fire(new TaskRequest(false, new IdTask<ShortlinkId>(Operation.REMOVE, shortlinkId)));
    return true;
  }
  
  private boolean handleIncidentTrash(OrganizationId kuntaApiOrganizationId, Payload payload) {
    IncidentId incidentId = managementIdFactory.createIncidentId(kuntaApiOrganizationId, payload.getId());
    taskRequest.fire(new TaskRequest(false, new IdTask<IncidentId>(Operation.REMOVE, incidentId)));
    return true;
  }

  private boolean handleAnnouncementTrash(OrganizationId kuntaApiOrganizationId, Payload payload) {
    AnnouncementId announcementId = new AnnouncementId(kuntaApiOrganizationId, ManagementConsts.IDENTIFIER_NAME, payload.getId());
    taskRequest.fire(new TaskRequest(false, new IdTask<AnnouncementId>(Operation.REMOVE, announcementId, null)));
    return true;
  }

  private boolean handlePublishPage(OrganizationId organizationId, Payload payload) {
    PageId pageId = new PageId(organizationId, ManagementConsts.IDENTIFIER_NAME, payload.getId());
    taskRequest.fire(new TaskRequest(true, new IdTask<PageId>(Operation.UPDATE, pageId, payload.getOrderIndex())));
    return true;
  }

  private boolean handleMenuItemTrash(OrganizationId kuntaApiOrganizationId) {
    return updateAllMenus(kuntaApiOrganizationId);
  }

  private boolean handlePublishBanner(OrganizationId organizationId, Payload payload) {
    BannerId bannerId = new BannerId(organizationId, ManagementConsts.IDENTIFIER_NAME, payload.getId());
    taskRequest.fire(new TaskRequest(true, new IdTask<BannerId>(Operation.UPDATE, bannerId, payload.getOrderIndex())));
    return true;
  }

  private boolean handlePublishPost(OrganizationId organizationId, Payload payload) {
    NewsArticleId newsArticleId = new NewsArticleId(organizationId, ManagementConsts.IDENTIFIER_NAME, payload.getId());
    taskRequest.fire(new TaskRequest(true, new IdTask<NewsArticleId>(Operation.UPDATE, newsArticleId, payload.getOrderIndex())));
    return true;
  }

  private boolean handleTilePublish(OrganizationId organizationId, Payload payload) {
    TileId tileId = new TileId(organizationId, ManagementConsts.IDENTIFIER_NAME, payload.getId());
    taskRequest.fire(new TaskRequest(true, new IdTask<TileId>(Operation.UPDATE, tileId, payload.getOrderIndex())));
    return true;
  }

  private boolean handleFragmentPublish(OrganizationId kuntaApiOrganizationId, Payload payload) {
    FragmentId fragmentId = new FragmentId(kuntaApiOrganizationId, ManagementConsts.IDENTIFIER_NAME, payload.getId());
    taskRequest.fire(new TaskRequest(true, new IdTask<FragmentId>(Operation.UPDATE, fragmentId, payload.getOrderIndex())));
    return true;
  }

  private boolean handleAnnouncementPublish(OrganizationId kuntaApiOrganizationId, Payload payload) {
    AnnouncementId announcementId = new AnnouncementId(kuntaApiOrganizationId, ManagementConsts.IDENTIFIER_NAME, payload.getId());
    taskRequest.fire(new TaskRequest(true, new IdTask<AnnouncementId>(Operation.UPDATE, announcementId, payload.getOrderIndex())));
    return true;
  }

  private boolean handleShortlinkPublish(OrganizationId kuntaApiOrganizationId, Payload payload) {
    ShortlinkId shortlinkId = new ShortlinkId(kuntaApiOrganizationId, ManagementConsts.IDENTIFIER_NAME, payload.getId());
    taskRequest.fire(new TaskRequest(true, new IdTask<ShortlinkId>(Operation.UPDATE, shortlinkId, payload.getOrderIndex())));
    return true;
  }

  private boolean handleIncidentPublish(OrganizationId kuntaApiOrganizationId, Payload payload) {
    IncidentId incidentId = new IncidentId(kuntaApiOrganizationId, ManagementConsts.IDENTIFIER_NAME, payload.getId());
    taskRequest.fire(new TaskRequest(true, new IdTask<IncidentId>(Operation.UPDATE, incidentId, payload.getOrderIndex())));
    return true;
  }

  private boolean handleMenuItemPublish(OrganizationId kuntaApiOrganizationId) {
    return updateAllMenus(kuntaApiOrganizationId);
  }
  
  private boolean updateAllMenus(OrganizationId kuntaApiOrganizationId) {
    DefaultApi api = managementApi.getApi(kuntaApiOrganizationId);
    
    fi.metatavu.management.client.ApiResponse<List<Menu>> response = api.kuntaApiMenusGet(null);
    if (response.isOk()) {
      for (Menu menu : response.getResponse()) {
        MenuId menuId = managementIdFactory.createMenuId(kuntaApiOrganizationId, String.valueOf(menu.getId()));
        taskRequest.fire(new TaskRequest(true, new IdTask<MenuId>(Operation.UPDATE, menuId, null)));
      }      
      
      return true;
    } else {
      logger.warning(String.format("Listing organization %s menus failed on [%d] %s", kuntaApiOrganizationId.getId(), response.getStatus(), response.getMessage()));
    }
    
    return false;
  }

}
