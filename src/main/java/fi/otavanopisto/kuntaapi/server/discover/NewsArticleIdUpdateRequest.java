package fi.otavanopisto.kuntaapi.server.discover;

import org.apache.commons.lang3.builder.HashCodeBuilder;

import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.NewsArticleId;

public class NewsArticleIdUpdateRequest extends AbstractIdUpdateRequest<NewsArticleId> {

  private OrganizationId organizationId;
  
  public NewsArticleIdUpdateRequest(OrganizationId organizationId, NewsArticleId id, Long orderIndex, boolean priority) {
    super(id, orderIndex, priority);
    this.organizationId = organizationId;
  }
  
  public OrganizationId getOrganizationId() {
    return organizationId;
  }
  
  @Override
  public boolean equals(Object obj) {
    if (obj instanceof NewsArticleIdUpdateRequest) {
      NewsArticleIdUpdateRequest another = (NewsArticleIdUpdateRequest) obj;
      return another.getId().equals(this.getId()) && another.getOrganizationId().equals(this.getOrganizationId());
    }

    return false;
  }
  
  @Override
  public int hashCode() {
    return new HashCodeBuilder(1149, 1161)
      .append(getOrganizationId())
      .append(getId())
      .hashCode();
  }
}
