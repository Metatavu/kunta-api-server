package fi.otavanopisto.kuntaapi.server.index;

public class IndexRemoveNewsArticle implements IndexRemove {

  private String newsArticleId;

  @Override
  public String getId() {
    return newsArticleId;
  }

  @Override
  public String getType() {
    return "newsarticle";
  }
  
  public String getNewsArticleId() {
    return newsArticleId;
  }
  
  public void setNewsArticleId(String newsArticleId) {
    this.newsArticleId = newsArticleId;
  }
  
}