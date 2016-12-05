package fi.otavanopisto.kuntaapi.server.discover;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.NewsArticleId;

public class NewsArticleIdRemoveRequest extends AbstractIdRemoveRequest<NewsArticleId> {

  private OrganizationId organizationId;
  
  public NewsArticleIdRemoveRequest(OrganizationId organizationId, NewsArticleId id) {
    super(id);
    this.organizationId = organizationId;
  }
  
  public OrganizationId getOrganizationId() {
    return organizationId;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof NewsArticleIdRemoveRequest) {
      NewsArticleIdRemoveRequest another = (NewsArticleIdRemoveRequest) obj;
      return another.getId().equals(this.getId()) && another.getOrganizationId().equals(this.getOrganizationId());
    }

    return false;
  }
  
  @Override
  public int hashCode() {
    return new HashCodeBuilder(1155, 1167)
      .append(getOrganizationId())
      .append(getId())
      .hashCode();
  }
}
