package fi.otavanopisto.kuntaapi.server.integrations.casem;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.discover.OrganizationIdRemoveRequest;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PageId;

@ApplicationScoped
public class CaseMOrganizationSettingsRemoveListener {

  @Inject
  private CaseMCache caseMCache;
  
  public void onOrganizationIdRemoveRequest(@Observes OrganizationIdRemoveRequest event) {
    OrganizationId organizationId = event.getId();
    
    List<PageId> pageIds = caseMCache.listOrganizationPageIds(organizationId);
    for (PageId pageId : pageIds) {
      caseMCache.removePage(pageId);
    }
    
  }
  

}
