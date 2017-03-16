package fi.otavanopisto.kuntaapi.server.cache;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.id.NewsArticleId;
import fi.metatavu.kuntaapi.server.rest.model.NewsArticle;

@ApplicationScoped
public class NewsArticleCache extends AbstractEntityCache<NewsArticleId, NewsArticle> {

  private static final long serialVersionUID = 1251806281893865654L;

  @Override
  public String getCacheName() {
    return "news-articles";
  }
  
  @Override
  public String getEntityType() {
    return "resource";
  }

}
