package fi.otavanopisto.kuntaapi.server.cache;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.id.AttachmentId;
import fi.otavanopisto.kuntaapi.server.id.NewsArticleId;
import fi.otavanopisto.kuntaapi.server.rest.model.Attachment;

@ApplicationScoped
public class NewsArticleImageCache extends AbstractEntityRelationCache<NewsArticleId, AttachmentId, Attachment> {
 
  private static final long serialVersionUID = -8714266457895787874L;

  @Override
  public String getCacheName() {
    return "news-article-images";
  }
  
}
