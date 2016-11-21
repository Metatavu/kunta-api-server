package fi.otavanopisto.kuntaapi.server.index;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.action.search.SearchRequestBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHitField;

import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.FileId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;

@ApplicationScoped
public class FileSearcher {
  
  private static final String TYPE = "file";
  private static final String FILE_ID_FIELD = "fileId";
  private static final String ORGANIZATION_ID_FIELD = "organizationId";
  
  @Inject
  private Logger logger;
  
  @Inject
  private IndexReader indexReader;

  public SearchResult<FileId> searchFiles(String organizationId, String queryString, Long firstResult, Long maxResults) {
    BoolQueryBuilder query = boolQuery()
      .must(matchQuery(ORGANIZATION_ID_FIELD, organizationId))
      .must(queryStringQuery(queryString));
    
    return searchFiles(query, firstResult, maxResults);
  }
  
  private SearchResult<FileId> searchFiles(QueryBuilder queryBuilder, Long firstResult, Long maxResults) {
    if (!indexReader.isEnabled()) {
      logger.warning("Could not execute search. Search functions are disabled");
      return null;
    }
    
    SearchRequestBuilder requestBuilder = indexReader
      .requestBuilder(TYPE)
      .storedFields(FILE_ID_FIELD, ORGANIZATION_ID_FIELD)
      .setQuery(queryBuilder);
    
    if (firstResult != null) {
      requestBuilder.setFrom(firstResult.intValue());
    }
    
    if (maxResults != null) {
      requestBuilder.setSize(maxResults.intValue());
    }
      
    return new SearchResult<>(getFileIds(indexReader.search(requestBuilder)));
  }
  
  private List<FileId> getFileIds(SearchHit[] hits) {
    List<FileId> result = new ArrayList<>(hits.length);
    
    for (SearchHit hit : hits) {
      Map<String, SearchHitField> fields = hit.getFields(); 
      SearchHitField fileHitField = fields.get(FILE_ID_FIELD);
      SearchHitField organizationHitField = fields.get(ORGANIZATION_ID_FIELD);
      
      String fileId = fileHitField.getValue();
      
      if (StringUtils.isNotBlank(fileId)) {
        OrganizationId organizationId = new OrganizationId(KuntaApiConsts.IDENTIFIER_NAME, organizationHitField.getValue());
        result.add(new FileId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, fileId));
      }
    }
    
    return result;
  }
   
}
