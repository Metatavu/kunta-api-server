package fi.otavanopisto.kuntaapi.server.integrations.management.webhooks;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

import fi.otavanopisto.kuntaapi.server.discover.PageIdUpdateRequest;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PageId;
import fi.otavanopisto.kuntaapi.server.integrations.management.ManagementConsts;
import fi.otavanopisto.kuntaapi.server.webhooks.WebhookHandler;

@RequestScoped
@SuppressWarnings ("squid:S1301")
public class ManagementWebhookHandler implements WebhookHandler {
  
  @Inject
  private Logger logger;

  @Inject
  private Event<PageIdUpdateRequest> pageIdUpdateRequest;

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
        return handleEditPost(organizationId, payload);
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

  private boolean handleEditPost(OrganizationId organizationId, Payload payload) {
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
    
    switch (payload.getPostStatus()) {
      case "trash":
      break;
      case "publish":
        return handlePublish(organizationId, payload);
      default:
    }
    
    return false;
  }

  private boolean handlePublish(OrganizationId organizationId, Payload payload) {
    switch (payload.getPostType()) {
      case "page":
        PageId pageId = new PageId(organizationId, ManagementConsts.IDENTIFIER_NAME, payload.getId());
        pageIdUpdateRequest.fire(new PageIdUpdateRequest(organizationId, pageId, true));
        return true;
      default:
    }
    
    return false;
  }

}
