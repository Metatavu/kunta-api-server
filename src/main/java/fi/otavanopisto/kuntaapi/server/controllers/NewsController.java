package fi.otavanopisto.kuntaapi.server.controllers;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import fi.otavanopisto.kuntaapi.server.id.AttachmentId;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.NewsArticleId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.index.NewsArticleSearcher;
import fi.otavanopisto.kuntaapi.server.index.SearchResult;
import fi.otavanopisto.kuntaapi.server.integrations.AttachmentData;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.integrations.NewsProvider;
import fi.otavanopisto.kuntaapi.server.utils.ListUtils;
import fi.metatavu.kuntaapi.server.rest.model.Attachment;
import fi.metatavu.kuntaapi.server.rest.model.NewsArticle;

@ApplicationScoped
@SuppressWarnings ("squid:S3306")
public class NewsController {

  @Inject
  private Logger logger;

  @Inject
  private EntityController entityController;

  @Inject
  private IdController idController;

  @Inject
  private NewsArticleSearcher newsArticleSearcher;

  @Inject
  private Instance<NewsProvider> newsProviders;

  public List<NewsArticle> listNewsArticles(String slug, String tag, OffsetDateTime publishedBefore, OffsetDateTime publishedAfter, Integer firstResult, Integer maxResults, OrganizationId organizationId) {
    if (StringUtils.isBlank(slug) && StringUtils.isNotBlank(tag)) {
      List<NewsArticle> result = searchNewsArticlesByTag(organizationId, tag, firstResult, maxResults);
      if (result != null) {
        return result;
      }
    }
    
    List<NewsArticle> result = new ArrayList<>();
   
    for (NewsProvider newsProvider : getNewsProviders()) {
      List<NewsArticle> newArticles = newsProvider.listOrganizationNews(organizationId, tag, publishedBefore, publishedAfter);
      if (newArticles != null && !newArticles.isEmpty()) {
        if (slug != null) {
          result.addAll(filterBySlug(newArticles, slug));
        } else {
          result.addAll(newArticles);
        }
      }
    }
    
    return ListUtils.limit(entityController.sortEntitiesInNaturalOrder(result), firstResult, maxResults);
  }

  public NewsArticle findNewsArticle(OrganizationId organizationId, NewsArticleId newsArticleId) {
    for (NewsProvider newsProvider : getNewsProviders()) {
      NewsArticle newsArticle = newsProvider.findOrganizationNewsArticle(organizationId, newsArticleId);
      if (newsArticle != null) {
        return newsArticle;
      }
    }
    
    return null;
  }

  public Attachment findNewsArticleImage(OrganizationId organizationId, NewsArticleId newsArticleId, AttachmentId attachmentId) {
    for (NewsProvider newsProvider : getNewsProviders()) {
      Attachment attachment = newsProvider.findNewsArticleImage(organizationId, newsArticleId, attachmentId);
      if (attachment != null) {
        return attachment;
      }
    }
    
    return null;
  }
  
  public AttachmentData getNewsArticleImageData(OrganizationId organizationId, NewsArticleId newsArticleId, AttachmentId attachmentId, Integer size) {
    for (NewsProvider newsProvider : getNewsProviders()) {
      AttachmentData attachmentData = newsProvider.getNewsArticleImageData(organizationId, newsArticleId, attachmentId, size);
      if (attachmentData != null) {
        return attachmentData;
      }
    }
    
    return null;
  }

  public List<Attachment> listNewsArticleImages(OrganizationId organizationId, NewsArticleId newsArticleId) {
    List<Attachment> result = new ArrayList<>();
    for (NewsProvider newsProvider : getNewsProviders()) {
      result.addAll(newsProvider.listNewsArticleImages(organizationId, newsArticleId));
    }
    
    return entityController.sortEntitiesInNaturalOrder(result);
  }
  
  @SuppressWarnings ("squid:S1168")
  private List<NewsArticle> searchNewsArticlesByTag(OrganizationId organizationId, String tag, Integer firstResult, Integer maxResults) { 
    OrganizationId kuntaApiOrganizationId = idController.translateOrganizationId(organizationId, KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiOrganizationId == null) {
      logger.severe(String.format("Failed to translate organization %s into Kunta API id", organizationId.toString()));
      return Collections.emptyList();
    }
    
    SearchResult<NewsArticleId> searchResult = newsArticleSearcher.searchNewsArticlesByTag(kuntaApiOrganizationId.getId(), tag, firstResult != null ? firstResult.longValue() : null, maxResults != null ? maxResults.longValue() : null);
    if (searchResult != null) {
      List<NewsArticleId> newsArticleIds = searchResult.getResult();

      List<NewsArticle> result = new ArrayList<>(newsArticleIds.size());

      for (NewsArticleId newsArticleId : newsArticleIds) {
        NewsArticle newsArticle = findNewsArticle(kuntaApiOrganizationId, newsArticleId);
        if (newsArticle != null) {
          result.add(newsArticle);
        }
      }
      
      return result;
    }
    
    return null;
  }
  
  private List<NewsArticle> filterBySlug(List<NewsArticle> newsArticles, String slug) {
    List<NewsArticle> result = new ArrayList<>();
   
    for (NewsArticle newsArticle : newsArticles) {
      if (StringUtils.equals(slug, newsArticle.getSlug())) {
        result.add(newsArticle);
      }
    }
    
    return result;
  }
  
  private List<NewsProvider> getNewsProviders() {
    List<NewsProvider> result = new ArrayList<>();
    
    Iterator<NewsProvider> iterator = newsProviders.iterator();
    while (iterator.hasNext()) {
      result.add(iterator.next());
    }
    
    return Collections.unmodifiableList(result);
  }
}
