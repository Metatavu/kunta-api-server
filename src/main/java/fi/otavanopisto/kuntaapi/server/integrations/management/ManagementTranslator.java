package fi.otavanopisto.kuntaapi.server.integrations.management;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import fi.otavanopisto.kuntaapi.server.id.AttachmentId;
import fi.otavanopisto.kuntaapi.server.id.BannerId;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PageId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.rest.model.Attachment;
import fi.otavanopisto.kuntaapi.server.rest.model.Banner;
import fi.otavanopisto.kuntaapi.server.rest.model.LocalizedValue;

@ApplicationScoped
public class ManagementTranslator {
  
  @Inject
  private Logger logger;
  
  @Inject
  private ManagementImageLoader managementImageLoader;
  
  @Inject
  private IdController idController;

  public Attachment translateAttachment(OrganizationId organizationId, fi.otavanopisto.mwp.client.model.Attachment featuredMedia) {
    Integer size = managementImageLoader.getImageSize(featuredMedia.getSourceUrl());
    AttachmentId id = getImageAttachmentId(organizationId, featuredMedia.getId());
    Attachment attachment = new Attachment();
    attachment.setContentType(featuredMedia.getMimeType());
    attachment.setId(id.getId());
    attachment.setSize(size != null ? size.longValue() : null);
    return attachment;
  }
  
  public List<LocalizedValue> translateLocalized(String value) {
    // TODO: Support multiple locales 
    
    List<LocalizedValue> result = new ArrayList<>();
    
    if (StringUtils.isNotBlank(value)) {
      LocalizedValue localizedValue = new LocalizedValue();
      localizedValue.setLanguage(ManagementConsts.DEFAULT_LOCALE);
      localizedValue.setValue(value);
      result.add(localizedValue);
    }
    
    return result;
  }
  
  public Attachment translateAttachment(AttachmentId kuntaApiAttachmentId, fi.otavanopisto.mwp.client.model.Attachment featuredMedia) {
    Integer size = managementImageLoader.getImageSize(featuredMedia.getSourceUrl());
    Attachment attachment = new Attachment();
    attachment.setContentType(featuredMedia.getMimeType());
    attachment.setId(kuntaApiAttachmentId.getId());
    attachment.setSize(size != null ? size.longValue() : null);
    return attachment;
  }
  
  public fi.otavanopisto.kuntaapi.server.rest.model.Page translatePage(OrganizationId organizationId, PageId kuntaApiPageId, fi.otavanopisto.mwp.client.model.Page managementPage) {
    fi.otavanopisto.kuntaapi.server.rest.model.Page page = new fi.otavanopisto.kuntaapi.server.rest.model.Page();
    PageId kuntaApiParentPageId = null;
    
    if (managementPage.getParent() != null && managementPage.getParent() > 0) {
      PageId managementParentPageId = new PageId(organizationId, ManagementConsts.IDENTIFIER_NAME,String.valueOf(managementPage.getParent()));
      kuntaApiParentPageId = idController.translatePageId(managementParentPageId, KuntaApiConsts.IDENTIFIER_NAME);
      if (kuntaApiParentPageId == null) {
        logger.severe(String.format("Could not translate %d parent page %d into management page id", managementPage.getParent(), managementPage.getId()));
        return null;
      } 
    }
    
    page.setTitles(translateLocalized(managementPage.getTitle().getRendered()));
    
    page.setId(kuntaApiPageId.getId());
    
    if (kuntaApiParentPageId != null) {
      page.setParentId(kuntaApiParentPageId.getId());
    }
    
    page.setSlug(managementPage.getSlug());
    
    return page;
  }

  public Banner translateBanner(BannerId kuntaApiBannerId, fi.otavanopisto.mwp.client.model.Banner managementBanner) {
    Banner banner = new Banner();
    
    banner.setContents(managementBanner.getContent().getRendered());
    banner.setId(kuntaApiBannerId.getId());
    banner.setLink(managementBanner.getBannerLink());
    banner.setTitle(managementBanner.getTitle().getRendered());
    
    return banner;
  }
  
  private AttachmentId getImageAttachmentId(OrganizationId organizationId, Integer id) {
    AttachmentId managementId = new AttachmentId(organizationId, ManagementConsts.IDENTIFIER_NAME, String.valueOf(id));
    AttachmentId kuntaApiId = idController.translateAttachmentId(managementId, KuntaApiConsts.IDENTIFIER_NAME);
    
    if (kuntaApiId == null) {
      logger.info(String.format("Could not translate management attachment %s into Kunta API Id", managementId));
      return null;
    }
    
    return kuntaApiId;
  }
  
}
