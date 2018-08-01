package fi.metatavu.kuntaapi.server.controllers;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.id.AttachmentId;
import fi.metatavu.kuntaapi.server.id.IdController;
import fi.metatavu.kuntaapi.server.id.NewsArticleId;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.index.SearchResult;
import fi.metatavu.kuntaapi.server.index.search.NewsArticleSearcher;
import fi.metatavu.kuntaapi.server.integrations.AttachmentData;
import fi.metatavu.kuntaapi.server.integrations.KuntaApiConsts;
import fi.metatavu.kuntaapi.server.integrations.NewsProvider;
import fi.metatavu.kuntaapi.server.integrations.NewsSortBy;
import fi.metatavu.kuntaapi.server.integrations.SortDir;
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
  
  @SuppressWarnings ("squid:S00107")
  public SearchResult<NewsArticle> searchNewsArticles(OrganizationId organizationId, String search, String tag, String slug, 
      OffsetDateTime publishedBefore, OffsetDateTime publishedAfter, NewsSortBy sortBy, SortDir sortDir, Integer firstResult, Integer maxResults) { 
    
    OrganizationId kuntaApiOrganizationId = idController.translateOrganizationId(organizationId, KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiOrganizationId == null) {
      logger.severe(() -> String.format("Failed to translate organization %s into Kunta API id", organizationId.toString()));
      return SearchResult.emptyResult();
    }
    
    SearchResult<NewsArticleId> searchResult = newsArticleSearcher.searchNewsArticles(kuntaApiOrganizationId.getId(), search, tag, slug, publishedBefore, publishedAfter, sortBy, sortDir, firstResult != null ? firstResult.longValue() : null, maxResults != null ? maxResults.longValue() : null);
    if (searchResult != null) {
      List<NewsArticleId> newsArticleIds = searchResult.getResult();

      List<NewsArticle> result = new ArrayList<>(newsArticleIds.size());

      for (NewsArticleId newsArticleId : newsArticleIds) {
        NewsArticle newsArticle = findNewsArticle(kuntaApiOrganizationId, newsArticleId);
        if (newsArticle != null) {
          result.add(newsArticle);
        }
      }
      
      return new SearchResult<>(result, searchResult.getTotalHits());
    }
    
    return null;
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
