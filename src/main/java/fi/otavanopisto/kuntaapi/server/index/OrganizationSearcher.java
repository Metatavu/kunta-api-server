package fi.otavanopisto.kuntaapi.server.index;

import static org.elasticsearch.index.query.QueryBuilders.matchQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.index.query.MatchQueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;

import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;

@ApplicationScoped
public class OrganizationSearcher extends AbstractSearcher {
  
  @Inject
  private IndexReader indexReader;

  public SearchResult<OrganizationId> searchOrganizationsByBusinessCode(String businessCode) {
    SearchRequestBuilder requestBuilder = indexReader.requestBuilder("organization");
    
    MatchQueryBuilder query = matchQuery("businessCode", businessCode);
    requestBuilder.setQuery(query);
    
    return new SearchResult<>(getOrganizationIds(indexReader.search(requestBuilder)));
  }
  
  private List<OrganizationId> getOrganizationIds(SearchHit[] hits) {
    List<OrganizationId> result = new ArrayList<>(hits.length);
    
    for (SearchHit hit : hits) {
      Map<String, SearchHitField> fields = hit.getFields(); 
      SearchHitField searchHitField = fields.get("organizationId");
      String organizationId = searchHitField.getValue();
      if (StringUtils.isNotBlank(organizationId)) {
        result.add(new OrganizationId(KuntaApiConsts.IDENTIFIER_NAME, organizationId));
      }
    }
    
    return result;
  }
   
}
