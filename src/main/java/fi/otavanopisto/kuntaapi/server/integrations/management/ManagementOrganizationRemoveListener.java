package fi.otavanopisto.kuntaapi.server.integrations.management;

import java.util.List;

import javax.ejb.Asynchronous;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.discover.BannerIdRemoveRequest;
import fi.otavanopisto.kuntaapi.server.discover.NewsArticleIdRemoveRequest;
import fi.otavanopisto.kuntaapi.server.discover.OrganizationIdRemoveRequest;
import fi.otavanopisto.kuntaapi.server.discover.PageIdRemoveRequest;
import fi.otavanopisto.kuntaapi.server.id.BannerId;
import fi.otavanopisto.kuntaapi.server.id.NewsArticleId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PageId;

@ApplicationScoped
public class ManagementOrganizationRemoveListener {
  
  @Inject
  private IdentifierController identifierController;
  
  @Inject
  private Event<PageIdRemoveRequest> pageIdRemoveRequest;

  @Inject
  private Event<BannerIdRemoveRequest> bannerIdRemoveRequest;

  @Inject
  private Event<NewsArticleIdRemoveRequest> newsArticleIdRemoveRequest;

  @Asynchronous
  public void onOrganizationIdRemoveRequest(@Observes OrganizationIdRemoveRequest event) {
    OrganizationId organizationId = event.getId();
    
    List<PageId> pageIds = identifierController.listOrganizationPageIdsBySource(organizationId, ManagementConsts.IDENTIFIER_NAME);
    for (PageId pageId : pageIds) {
      pageIdRemoveRequest.fire(new PageIdRemoveRequest(organizationId, pageId));
    }
    
    List<BannerId> bannerIds = identifierController.listOrganizationBannerIdsBySource(organizationId, ManagementConsts.IDENTIFIER_NAME);
    for (BannerId bannerId : bannerIds) {
      bannerIdRemoveRequest.fire(new BannerIdRemoveRequest(organizationId, bannerId));
    }
    
    List<NewsArticleId> newsArticleIds = identifierController.listOrganizationNewsArticleIdsBySource(organizationId, ManagementConsts.IDENTIFIER_NAME);
    for (NewsArticleId newsArticleId : newsArticleIds) {
      newsArticleIdRemoveRequest.fire(new NewsArticleIdRemoveRequest(organizationId, newsArticleId));
    }
  }
  
}
