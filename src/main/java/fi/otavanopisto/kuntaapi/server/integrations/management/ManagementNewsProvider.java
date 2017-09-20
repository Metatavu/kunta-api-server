package fi.otavanopisto.kuntaapi.server.integrations.management;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.rest.model.Attachment;
import fi.metatavu.kuntaapi.server.rest.model.NewsArticle;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierRelationController;
import fi.otavanopisto.kuntaapi.server.id.AttachmentId;
import fi.otavanopisto.kuntaapi.server.id.NewsArticleId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.AttachmentData;
import fi.otavanopisto.kuntaapi.server.integrations.NewsProvider;
import fi.otavanopisto.kuntaapi.server.integrations.management.resources.ManagementAttachmentResourceContainer;
import fi.otavanopisto.kuntaapi.server.resources.NewsArticleResourceContainer;
import javax.enterprise.context.ApplicationScoped;

/**
 * News provider for management wordpress
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class ManagementNewsProvider extends AbstractManagementProvider implements NewsProvider {
  
  @Inject
  private IdentifierController identifierController;

  @Inject
  private IdentifierRelationController identifierRelationController;
  
  @Inject
  private NewsArticleResourceContainer newsArticleResourceContainer;
  
  @Inject
  private ManagementAttachmentResourceContainer managementAttachmentResourceContainer;
  
  @Override
  public List<NewsArticle> listOrganizationNews(OrganizationId organizationId, String tag, OffsetDateTime publishedBefore, OffsetDateTime publishedAfter) {
    if (organizationId == null) {
      return Collections.emptyList();
    }
    
    List<NewsArticleId> newsArticleIds = identifierController.listOrganizationNewsArticleIdsBySource(organizationId, ManagementConsts.IDENTIFIER_NAME);
    List<NewsArticle> result = new ArrayList<>(newsArticleIds.size());
    for (NewsArticleId newsArticleId : newsArticleIds) {
      NewsArticle newsArticle = newsArticleResourceContainer.get(newsArticleId);
      if (newsArticle != null && isAcceptable(newsArticle, tag, publishedBefore, publishedAfter)) {
        result.add(newsArticle);
      }
    }
    
    return result;
  }

  @Override
  public NewsArticle findOrganizationNewsArticle(OrganizationId organizationId, NewsArticleId newsArticleId) {
    if (!identifierRelationController.isChildOf(organizationId, newsArticleId)) {
      return null;
    }
    
    return newsArticleResourceContainer.get(newsArticleId);
  }

  @Override
  public List<Attachment> listNewsArticleImages(OrganizationId organizationId, NewsArticleId newsArticleId) {
    List<Attachment> result = new ArrayList<>();
    List<AttachmentId> attachmentIds = identifierRelationController.listAttachmentIdsBySourceAndParentId(ManagementConsts.IDENTIFIER_NAME, newsArticleId);
    
    for (AttachmentId attachmentId : attachmentIds) {
      Attachment attachment = managementAttachmentResourceContainer.get(attachmentId);
      if (attachment != null) {
        result.add(attachment);
      }
    }
    
    return result;
  }

  @Override
  public Attachment findNewsArticleImage(OrganizationId organizationId, NewsArticleId newsArticleId, AttachmentId attachmentId) {
    if (!identifierRelationController.isChildOf(newsArticleId, attachmentId)) {
      return null;
    }
    
    return managementAttachmentResourceContainer.get(attachmentId);
  }

  @Override
  public AttachmentData getNewsArticleImageData(OrganizationId organizationId, NewsArticleId newsArticleId, AttachmentId kuntaApiAttachmentId, Integer size) {
    if (!identifierRelationController.isChildOf(newsArticleId, kuntaApiAttachmentId)) {
      return null;
    }

    return getImageData(kuntaApiAttachmentId, size);
  }
  
  private boolean isAcceptable(NewsArticle newsArticle, String tag, OffsetDateTime publishedBefore, OffsetDateTime publishedAfter) {
    OffsetDateTime published = newsArticle.getPublished();
    if (publishedBefore != null && (published == null || published.isAfter(publishedBefore))) {
      return false;
    }
    
    if (publishedAfter != null && (published == null || published.isBefore(publishedAfter))) {
      return false;
    }
    
    if (tag != null) {
      return newsArticle.getTags() != null && newsArticle.getTags().contains(tag);
    }
    
    return true;
  }

}
