package fi.otavanopisto.kuntaapi.server.webhooks;

import java.io.IOException;
import java.io.Writer;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;

import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiIdFactory;

@WebServlet (urlPatterns = "/webhooks/*")
@Transactional
public class WebHookServlet extends HttpServlet {
  
  private static final long serialVersionUID = -1381193888860326407L;

  @Inject
  private Logger logger;
  
  @Inject
  private KuntaApiIdFactory kuntaApiIdFactory;
  
  @Inject
  private Instance<WebhookHandler> webhookHandlers;
  
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    String type = StringUtils.stripStart(request.getPathInfo(), "/");
    if (StringUtils.isBlank(type)) {
      sendResponse(response, HttpServletResponse.SC_NOT_FOUND, "Type not defined");
      return;
    }
    
    logger.log(Level.INFO, () -> String.format("Received webhook for %s", type));
    
    String organizationIdParam = request.getParameter("organizationId");
    if (StringUtils.isBlank(organizationIdParam)) {
      sendResponse(response, HttpServletResponse.SC_BAD_REQUEST, "Missing organizationId");
      return;
    }
    
    OrganizationId kuntaApiOrganizationId = kuntaApiIdFactory.createOrganizationId(organizationIdParam);
        
    Iterator<WebhookHandler> webhookHandlerIterator = webhookHandlers.iterator();
    while (webhookHandlerIterator.hasNext()) {
      WebhookHandler webhookHandler = webhookHandlerIterator.next();
      if (StringUtils.equals(webhookHandler.getType(), type)) {
        if (webhookHandler.handle(kuntaApiOrganizationId, request)) {
          logger.log(Level.INFO, () -> String.format("Webhook (%s) processed succesfully", type));
          response.setStatus(HttpServletResponse.SC_OK);
        } else {
          logger.log(Level.WARNING, () -> String.format("Webhook (%s) processing failed", type));
          response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
        
        return;
      }
    }
    
    sendResponse(response, HttpServletResponse.SC_NOT_FOUND, "Not found");
    return;
  }

  public void sendResponse(HttpServletResponse response, int status, String message) {
    try (Writer writer = response.getWriter()) {
      writer.write(message);
      response.setStatus(status);
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Could not write response", e);
    }
  }
  
}
