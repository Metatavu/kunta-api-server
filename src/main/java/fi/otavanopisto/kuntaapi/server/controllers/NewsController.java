package fi.otavanopisto.kuntaapi.server.controllers;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.id.AttachmentId;
import fi.otavanopisto.kuntaapi.server.id.NewsArticleId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.AttachmentData;
import fi.otavanopisto.kuntaapi.server.integrations.NewsProvider;
import fi.otavanopisto.kuntaapi.server.rest.model.Attachment;
import fi.otavanopisto.kuntaapi.server.rest.model.NewsArticle;

@ApplicationScoped
public class NewsController {

  @Inject
  private Instance<NewsProvider> newsProviders;

  public List<NewsArticle> listNewsArticles(OffsetDateTime publishedBefore, OffsetDateTime publishedAfter, Integer firstResult, Integer maxResults, OrganizationId organizationId) {
    List<NewsArticle> result = new ArrayList<>();
   
    for (NewsProvider newsProvider : getNewsProviders()) {
      result.addAll(newsProvider.listOrganizationNews(organizationId, publishedBefore, publishedAfter, firstResult, maxResults));
    }
    return result;
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
