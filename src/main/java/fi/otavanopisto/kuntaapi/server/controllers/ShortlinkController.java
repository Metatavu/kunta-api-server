package fi.otavanopisto.kuntaapi.server.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.id.ShortlinkId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.ShortlinkProvider;
import fi.otavanopisto.kuntaapi.server.utils.ListUtils;
import fi.metatavu.kuntaapi.server.rest.model.Shortlink;

@ApplicationScoped
@SuppressWarnings ("squid:S3306")
public class ShortlinkController {
  
  @Inject
  private EntityController entityController;
  
  @Inject
  private Instance<ShortlinkProvider> shortlinkProviders;
  
  public List<Shortlink> listShortlinks(OrganizationId organizationId, String path, Integer firstResult, Integer maxResults) {
    List<Shortlink> result = new ArrayList<>();
   
    for (ShortlinkProvider shortlinkProvider : getShortlinkProviders()) {
      result.addAll(shortlinkProvider.listOrganizationShortlinks(organizationId, path));
    }
    
    int resultCount = result.size();
    int firstIndex = firstResult == null ? 0 : Math.min(firstResult.intValue(), resultCount);
    int toIndex = maxResults == null ? resultCount : Math.min(firstIndex + maxResults.intValue(), resultCount);
    
    return ListUtils.limit(entityController.sortEntitiesInNaturalOrder(result), firstIndex, toIndex);
  }

  public Shortlink findShortlink(OrganizationId organizationId, ShortlinkId shortlinkId) {
    for (ShortlinkProvider shortlinkProvider : getShortlinkProviders()) {
      Shortlink shortlink = shortlinkProvider.findOrganizationShortlink(organizationId, shortlinkId);
      if (shortlink != null) {
        return shortlink;
      }
    }
    
    return null;
  }
  
  private List<ShortlinkProvider> getShortlinkProviders() {
    List<ShortlinkProvider> result = new ArrayList<>();
    
    Iterator<ShortlinkProvider> iterator = shortlinkProviders.iterator();
    while (iterator.hasNext()) {
      result.add(iterator.next());
    }
    
    return Collections.unmodifiableList(result);
  }
  
}
