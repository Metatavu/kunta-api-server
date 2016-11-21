package fi.otavanopisto.kuntaapi.server.id;

/**
 * Class representing event id
 * 
 * @author Antti Leppä
 */
public class NewsArticleId extends OrganizationBaseId {
  
  private static final long serialVersionUID = 294282084448652921L;

  /**
   * Zero-argument constructor for article id
   */
  public NewsArticleId() {
    super();
  }

  /**
   * Constructor that accepts source and id
   * 
   * @param source source
   * @param id id
   */
  public NewsArticleId(OrganizationId organizationId, String source, String id) {
    super(organizationId, source, id);
  }
  
  @Override
  public IdType getType() {
    return IdType.NEWS_ARTICLE;
  }
  
  @Override
  protected int getHashInitial() {
    return 135;
  }
  
  @Override
  protected int getHashMultiplier() {
    return 147;
  }
  
}
