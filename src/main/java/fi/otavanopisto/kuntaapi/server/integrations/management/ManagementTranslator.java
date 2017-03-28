package fi.otavanopisto.kuntaapi.server.integrations.management;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import fi.metatavu.kuntaapi.server.rest.model.Attachment;
import fi.metatavu.kuntaapi.server.rest.model.Banner;
import fi.metatavu.kuntaapi.server.rest.model.Fragment;
import fi.metatavu.kuntaapi.server.rest.model.LocalizedValue;
import fi.metatavu.kuntaapi.server.rest.model.NewsArticle;
import fi.metatavu.kuntaapi.server.rest.model.PageMeta;
import fi.metatavu.kuntaapi.server.rest.model.Tile;
import fi.metatavu.management.client.model.Announcement;
import fi.metatavu.management.client.model.Post;
import fi.metatavu.management.client.model.PostExcerpt;
import fi.metatavu.management.client.model.Shortlink;
import fi.otavanopisto.kuntaapi.server.id.AnnouncementId;
import fi.otavanopisto.kuntaapi.server.id.AttachmentId;
import fi.otavanopisto.kuntaapi.server.id.BannerId;
import fi.otavanopisto.kuntaapi.server.id.FragmentId;
import fi.otavanopisto.kuntaapi.server.id.NewsArticleId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PageId;
import fi.otavanopisto.kuntaapi.server.id.ShortlinkId;
import fi.otavanopisto.kuntaapi.server.id.TileId;

@ApplicationScoped
@SuppressWarnings ("squid:S3306")
public class ManagementTranslator {
  
  @Inject
  private ManagementImageLoader managementImageLoader;
  
  public List<LocalizedValue> translateLocalized(String value) {
    List<LocalizedValue> result = new ArrayList<>();
    
    if (StringUtils.isNotBlank(value)) {
      LocalizedValue localizedValue = new LocalizedValue();
      localizedValue.setLanguage(ManagementConsts.DEFAULT_LOCALE);
      localizedValue.setValue(value);
      result.add(localizedValue);
    }
    
    return result;
  }
  
  public Attachment translateAttachment(AttachmentId kuntaApiAttachmentId, fi.metatavu.management.client.model.Attachment featuredMedia, String type) {
    Integer size = managementImageLoader.getImageSize(featuredMedia.getSourceUrl());
    Attachment attachment = new Attachment();
    attachment.setContentType(featuredMedia.getMimeType());
    attachment.setId(kuntaApiAttachmentId.getId());
    attachment.setSize(size != null ? size.longValue() : null);
    attachment.setType(type);
    return attachment;
  }
  
  public fi.metatavu.kuntaapi.server.rest.model.Page translatePage(PageId kuntaApiPageId, PageId kuntaApiParentPageId, String unmappedParentId, fi.metatavu.management.client.model.Page managementPage) {
    PageMeta meta = new PageMeta();
    meta.setHideMenuChildren(false);
    meta.setUnmappedParentId(unmappedParentId);
    
    fi.metatavu.kuntaapi.server.rest.model.Page page = new fi.metatavu.kuntaapi.server.rest.model.Page();
    page.setTitles(translateLocalized(managementPage.getTitle().getRendered()));
    page.setId(kuntaApiPageId.getId());
    page.setMeta(meta);
    
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
    banner.setBackgroundColor(managementBanner.getBannerBackgroundColor());
    banner.setTextColor(managementBanner.getBannerTextColor());

    if (!Boolean.TRUE.equals(managementBanner.getBannerHideTitle())) {
      banner.setTitle(managementBanner.getTitle().getRendered());
    }
    
    return banner;
  }
  
  public NewsArticle translateNewsArticle(NewsArticleId kuntaApiNewsArticleId, Post post) {
    NewsArticle newsArticle = new NewsArticle();
    
    newsArticle.setAbstract(cleanExcerpt(post.getExcerpt()));
    newsArticle.setContents(post.getContent().getRendered());
    newsArticle.setId(kuntaApiNewsArticleId.getId());
    newsArticle.setPublished(toOffsetDateTime(post.getDate()));
    newsArticle.setTitle(post.getTitle().getRendered());
    newsArticle.setSlug(post.getSlug());
    
    return newsArticle;
  }
  
  public fi.metatavu.kuntaapi.server.rest.model.Announcement translateAnnouncement(AnnouncementId kuntaApiAnnouncementId, Announcement managementAnnouncement) {
    fi.metatavu.kuntaapi.server.rest.model.Announcement result = new fi.metatavu.kuntaapi.server.rest.model.Announcement();
      
    result.setContents(managementAnnouncement.getContent().getRendered());
    result.setId(kuntaApiAnnouncementId.getId());
    result.setPublished(toOffsetDateTime(managementAnnouncement.getDate()));
    result.setTitle(managementAnnouncement.getTitle().getRendered());
    result.setSlug(managementAnnouncement.getSlug());
    
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
  
  public Fragment translateFragment(FragmentId kuntaApiFragmentId, fi.metatavu.management.client.model.Fragment managementFragment) {
    Fragment fragment = new Fragment();
    fragment.setContents(managementFragment.getContent().getRendered());
    fragment.setId(kuntaApiFragmentId.getId());
    fragment.setSlug(managementFragment.getSlug());
    return fragment;
  }
  
  public AttachmentId createManagementAttachmentId(OrganizationId organizationId, Integer attachmentId, String type) {
    return new AttachmentId(organizationId, ManagementConsts.IDENTIFIER_NAME, String.format("%d-%s", attachmentId, type));
  }

  public fi.metatavu.kuntaapi.server.rest.model.Shortlink translateShortlink(ShortlinkId kuntaApiShortlinkId, Shortlink managementShortlink) {
    fi.metatavu.kuntaapi.server.rest.model.Shortlink result = new fi.metatavu.kuntaapi.server.rest.model.Shortlink();
    result.setId(kuntaApiShortlinkId.getId());
    result.setName(managementShortlink.getTitle().getRendered());
    result.setPath(managementShortlink.getPath());
    result.setUrl(managementShortlink.getUrl());
    return result;
  }
  
  private OffsetDateTime toOffsetDateTime(LocalDateTime date) {
    if (date == null) {
      return null;
    }
    
    return date.atZone(ZoneId.systemDefault()).toOffsetDateTime();
  }

  private String cleanExcerpt(PostExcerpt postExcerpt) {
    if (postExcerpt != null && StringUtils.isNotBlank(postExcerpt.getRendered())) {
      return postExcerpt.getRendered().replaceAll("<a.*more-link.*a>", "");
    }
    
    return null;
  }

}
