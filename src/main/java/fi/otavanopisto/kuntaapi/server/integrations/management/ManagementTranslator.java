package fi.otavanopisto.kuntaapi.server.integrations.management;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import fi.metatavu.management.client.model.Announcement;
import fi.metatavu.management.client.model.Post;
import fi.otavanopisto.kuntaapi.server.id.AnnouncementId;
import fi.otavanopisto.kuntaapi.server.id.AttachmentId;
import fi.otavanopisto.kuntaapi.server.id.BannerId;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.NewsArticleId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PageId;
import fi.otavanopisto.kuntaapi.server.id.TileId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.rest.model.Attachment;
import fi.otavanopisto.kuntaapi.server.rest.model.Banner;
import fi.otavanopisto.kuntaapi.server.rest.model.LocalizedValue;
import fi.otavanopisto.kuntaapi.server.rest.model.NewsArticle;
import fi.otavanopisto.kuntaapi.server.rest.model.Tile;

@ApplicationScoped
@SuppressWarnings ("squid:S3306")
public class ManagementTranslator {
  
  @Inject
  private Logger logger;
  
  @Inject
  private ManagementImageLoader managementImageLoader;
  
  @Inject
  private IdController idController;

  public Attachment translateAttachment(OrganizationId organizationId, fi.metatavu.management.client.model.Attachment featuredMedia) {
    Integer size = managementImageLoader.getImageSize(featuredMedia.getSourceUrl());
    AttachmentId id = getImageAttachmentId(organizationId, featuredMedia.getId());
    if (id == null) {
      logger.severe(String.format("Could not translate featured media %d into Kunta API id", featuredMedia.getId()));
      return null;
    }
    
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
  
  public Attachment translateAttachment(AttachmentId kuntaApiAttachmentId, fi.metatavu.management.client.model.Attachment featuredMedia) {
    Integer size = managementImageLoader.getImageSize(featuredMedia.getSourceUrl());
    Attachment attachment = new Attachment();
    attachment.setContentType(featuredMedia.getMimeType());
    attachment.setId(kuntaApiAttachmentId.getId());
    attachment.setSize(size != null ? size.longValue() : null);
    return attachment;
  }
  
  public fi.otavanopisto.kuntaapi.server.rest.model.Page translatePage(OrganizationId organizationId, PageId kuntaApiPageId, fi.metatavu.management.client.model.Page managementPage) {
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

  public Banner translateBanner(BannerId kuntaApiBannerId, fi.metatavu.management.client.model.Banner managementBanner) {
    Banner banner = new Banner();
    
    banner.setContents(managementBanner.getContent().getRendered());
    banner.setId(kuntaApiBannerId.getId());
    banner.setLink(managementBanner.getBannerLink());
    banner.setTitle(managementBanner.getTitle().getRendered());
    
    return banner;
  }
  
  public NewsArticle translateNewsArticle(NewsArticleId kuntaApiNewsArticleId, Post post) {
    NewsArticle newsArticle = new NewsArticle();
    
    newsArticle.setAbstract(post.getExcerpt().getRendered());
    newsArticle.setContents(post.getContent().getRendered());
    newsArticle.setId(kuntaApiNewsArticleId.getId());
    newsArticle.setPublished(toOffsetDateTime(post.getDate()));
    newsArticle.setTitle(post.getTitle().getRendered());
    newsArticle.setSlug(post.getSlug());
    
    return newsArticle;
  }

  public fi.otavanopisto.kuntaapi.server.rest.model.Announcement translateAnnouncement(AnnouncementId kuntaApiAnnouncementId, Announcement managementAnnouncement) {
    fi.otavanopisto.kuntaapi.server.rest.model.Announcement result = new fi.otavanopisto.kuntaapi.server.rest.model.Announcement();
      
    result.setContents(managementAnnouncement.getContent().getRendered());
    result.setId(kuntaApiAnnouncementId.getId());
    result.setPublished(toOffsetDateTime(managementAnnouncement.getDate()));
    result.setTitle(managementAnnouncement.getTitle().getRendered());
    
    return result;
  }
  
  public Tile translateTile(TileId kuntaApiTileId, fi.metatavu.management.client.model.Tile managementTile) {
    Tile tile = new Tile();
    tile.setContents(managementTile.getContent().getRendered());
    tile.setId(kuntaApiTileId.getId());
    tile.setLink(managementTile.getTileLink());
    tile.setTitle(managementTile.getTitle().getRendered());
    
    return tile;
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
  
  private OffsetDateTime toOffsetDateTime(LocalDateTime date) {
    if (date == null) {
      return null;
    }
    
    return date.atZone(ZoneId.systemDefault()).toOffsetDateTime();
  }
  
}
