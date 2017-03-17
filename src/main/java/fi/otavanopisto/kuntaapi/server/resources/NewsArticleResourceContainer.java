package fi.otavanopisto.kuntaapi.server.resources;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.id.NewsArticleId;
import fi.metatavu.kuntaapi.server.rest.model.NewsArticle;

@ApplicationScoped
public class NewsArticleResourceContainer extends AbstractResourceContainer<NewsArticleId, NewsArticle> {

  private static final long serialVersionUID = 1251806281893865654L;

  @Override
  public String getName() {
    return "news-articles";
  }
  
  @Override
  public String getEntityType() {
    return "resource";
  }

}
