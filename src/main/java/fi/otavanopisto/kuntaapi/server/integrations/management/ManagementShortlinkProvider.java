package fi.otavanopisto.kuntaapi.server.integrations.management;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import fi.metatavu.kuntaapi.server.rest.model.Shortlink;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierRelationController;
import fi.otavanopisto.kuntaapi.server.id.ShortlinkId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.ShortlinkProvider;
import fi.otavanopisto.kuntaapi.server.integrations.management.resources.ManagementShortlinkResourceContainer;

/**
 * Shortlink provider for management wordpress
 * 
 * @author Antti Leppä
 */
@RequestScoped
@SuppressWarnings ("squid:S3306")
public class ManagementShortlinkProvider extends AbstractManagementProvider implements ShortlinkProvider {
  
  @Inject
  private ManagementShortlinkResourceContainer managementShortlinkCache;
  
  @Inject
  private IdentifierRelationController identifierRelationController;
  
  @Override
  public List<Shortlink> listOrganizationShortlinks(OrganizationId organizationId, String path) {
    List<ShortlinkId> shortlinkIds = identifierRelationController.listShortlinkIdsBySourceAndParentId(ManagementConsts.IDENTIFIER_NAME, organizationId);
    List<Shortlink> shortlinks = new ArrayList<>(shortlinkIds.size());
    
    for (ShortlinkId shortlinkId : shortlinkIds) {
      Shortlink shortlink = managementShortlinkCache.get(shortlinkId);
      if (shortlink != null && isAcceptable(shortlink, path)) {
        shortlinks.add(shortlink);
      }
    }
    
    return shortlinks;
  }

  @Override
  public Shortlink findOrganizationShortlink(OrganizationId organizationId, ShortlinkId shortlinkId) {
    if (identifierRelationController.isChildOf(organizationId, shortlinkId)) {
      return managementShortlinkCache.get(shortlinkId);
    }
    
    return null;
  }

  private boolean isAcceptable(Shortlink shortlink, String path) {
    if (path == null) {
      return true;
    }
    
    return StringUtils.equals(path, shortlink.getPath());
  }
  
}
