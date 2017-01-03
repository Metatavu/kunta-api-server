package fi.otavanopisto.kuntaapi.server.integrations.management;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.cache.NewsArticleCache;
import fi.otavanopisto.kuntaapi.server.cache.NewsArticleImageCache;
import fi.otavanopisto.kuntaapi.server.id.AttachmentId;
import fi.otavanopisto.kuntaapi.server.id.IdPair;
import fi.otavanopisto.kuntaapi.server.id.NewsArticleId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.AttachmentData;
import fi.otavanopisto.kuntaapi.server.integrations.NewsProvider;
import fi.metatavu.kuntaapi.server.rest.model.Attachment;
import fi.metatavu.kuntaapi.server.rest.model.NewsArticle;
import fi.metatavu.management.client.model.Attachment.MediaTypeEnum;

/**
 * News provider for management wordpress
 * 
 * @author Antti Leppä
 */
@RequestScoped
public class ManagementNewsProvider extends AbstractManagementProvider implements NewsProvider {
  
  @Inject
  private ManagementImageLoader managementImageLoader;
  
  @Inject
  private NewsArticleCache newsArticleCache;
  
  @Inject
  private NewsArticleImageCache newsArticleImageCache;
  
  @Override
  public List<NewsArticle> listOrganizationNews(OrganizationId organizationId, OffsetDateTime publishedBefore, OffsetDateTime publishedAfter) {
    if (organizationId == null) {
      return Collections.emptyList();
    }
    
    List<NewsArticleId> newsArticleIds = newsArticleCache.getOragnizationIds(organizationId);
    
    List<NewsArticle> result = new ArrayList<>(newsArticleIds.size());
    for (NewsArticleId newsArticleId : newsArticleIds) {
      NewsArticle newsArticle = newsArticleCache.get(newsArticleId);
      if (newsArticle != null && isAccetable(newsArticle, publishedBefore, publishedAfter)) {
        result.add(newsArticle);
      }
    }
    
    return result;
  }

  @Override
  public NewsArticle findOrganizationNewsArticle(OrganizationId organizationId, NewsArticleId newsArticleId) {
    return newsArticleCache.get(newsArticleId);
  }

  @Override
  public List<Attachment> listNewsArticleImages(OrganizationId organizationId, NewsArticleId newsArticleId) {
    List<Attachment> result = new ArrayList<>();
    
    List<IdPair<NewsArticleId,AttachmentId>> childIds = newsArticleImageCache.getChildIds(newsArticleId);
    for (IdPair<NewsArticleId,AttachmentId> childId : childIds) {
      Attachment attachment = newsArticleImageCache.get(childId);
      if (attachment != null) {
        result.add(attachment);
      }
    }
    
    return result;
  }

  @Override
  public Attachment findNewsArticleImage(OrganizationId organizationId, NewsArticleId newsArticleId, AttachmentId attachmentId) {
    return newsArticleImageCache.get(new IdPair<>(newsArticleId, attachmentId));
  }

  @Override
  public AttachmentData getNewsArticleImageData(OrganizationId organizationId, NewsArticleId newsArticleId,
      AttachmentId attachmentId, Integer size) {
    
    Integer mediaId = getMediaId(attachmentId);
    if (mediaId == null) {
      return null;
    }
    
    fi.metatavu.management.client.model.Attachment featuredMedia = findMedia(organizationId, mediaId);
    if (featuredMedia.getMediaType() == MediaTypeEnum.IMAGE) {
      AttachmentData imageData = managementImageLoader.getImageData(featuredMedia.getSourceUrl());
      
      if (size != null) {
        return scaleImage(imageData, size);
      } else {
        return imageData;
      }
      
    }
    
    return null;
  }
  
  private boolean isAccetable(NewsArticle newsArticle, OffsetDateTime publishedBefore, OffsetDateTime publishedAfter) {
    OffsetDateTime published = newsArticle.getPublished();
    if (publishedBefore != null && (published == null || published.isAfter(publishedBefore))) {
      return false;
    }
    
    if (publishedAfter != null && (published == null || published.isBefore(publishedAfter))) {
      return false;
    }
    
    return true;
  }

}
