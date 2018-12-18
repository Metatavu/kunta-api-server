package fi.metatavu.kuntaapi.server.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.id.BaseId;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.id.PageId;
import fi.metatavu.kuntaapi.server.integrations.IdMapProvider;

@ApplicationScoped
@SuppressWarnings ("squid:S3306")
public class IdMapController {
  
  @Inject
  private Instance<IdMapProvider> idMapProviders;

  public BaseId findMappedPageParentId(OrganizationId organizationId, PageId pageId) {
    for (IdMapProvider idMapProvider : getIdMapProviders()) {
      BaseId parentId = idMapProvider.findMappedPageParentId(organizationId, pageId);
      if (parentId != null) {
        return parentId;
      }
    }
    
    return null;
  }
  
  private List<IdMapProvider> getIdMapProviders() {
    List<IdMapProvider> result = new ArrayList<>();
    
    Iterator<IdMapProvider> iterator = idMapProviders.iterator();
    while (iterator.hasNext()) {
      result.add(iterator.next());
    }
    
    return Collections.unmodifiableList(result);
  }
  
}
