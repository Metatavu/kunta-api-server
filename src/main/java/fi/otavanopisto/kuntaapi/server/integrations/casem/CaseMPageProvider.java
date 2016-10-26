package fi.otavanopisto.kuntaapi.server.integrations.casem;

import java.util.Collections;
import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import fi.otavanopisto.kuntaapi.server.id.AttachmentId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PageId;
import fi.otavanopisto.kuntaapi.server.integrations.AttachmentData;
import fi.otavanopisto.kuntaapi.server.integrations.PageProvider;
import fi.otavanopisto.kuntaapi.server.rest.model.Attachment;
import fi.otavanopisto.kuntaapi.server.rest.model.LocalizedValue;
import fi.otavanopisto.kuntaapi.server.rest.model.Page;

/**
 * Page provider for CaseM
 * 
 * @author Antti Leppä
 * @author Heikki Kurhinen
 */
@Dependent
public class CaseMPageProvider implements PageProvider {
  
  @Inject
  private CaseMCache caseMCache;
  
  private CaseMPageProvider() {
  }
  
  @Override
  public List<Page> listOrganizationPages(OrganizationId organizationId, PageId parentId, boolean onlyRootPages, String path) {
    if (StringUtils.isNotBlank(path)) {
      Page page = caseMCache.findPageByPath(organizationId, path);
      if (page == null) {
        return Collections.emptyList();
      }
      
      return Collections.singletonList(page);
    } else {
      if (onlyRootPages) {
        return caseMCache.listRootPages(organizationId);
      } else {      
        return caseMCache.listPages(organizationId, parentId);
      }
    }
  }

  @Override
  public Page findOrganizationPage(OrganizationId organizationId, PageId pageId) {
    return caseMCache.findPage(organizationId, pageId);
  }
  
  @Override
  public List<LocalizedValue> findOrganizationPageContents(OrganizationId organizationId, PageId pageId) {
    return Collections.emptyList();
  }

  @Override
  public List<Attachment> listOrganizationPageImages(OrganizationId organizationId, PageId pageId) {
    return Collections.emptyList();
  }

  @Override
  public Attachment findPageImage(OrganizationId organizationId, PageId pageId, AttachmentId attachmentId) {
    return null;
  }

  @Override
  public AttachmentData getPageImageData(OrganizationId organizationId, PageId pageId, AttachmentId attachmentId, Integer size) {
    return null;
  }
  
}
