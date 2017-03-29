package fi.otavanopisto.kuntaapi.server.index;

import static org.elasticsearch.index.query.QueryBuilders.boolQuery;
import static org.elasticsearch.index.query.QueryBuilders.matchQuery;
import static org.elasticsearch.index.query.QueryBuilders.queryStringQuery;

import java.util.ArrayList;
import java.util.Collections;
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
import org.elasticsearch.search.sort.SortOrder;

import fi.otavanopisto.kuntaapi.server.id.FileId;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PageId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;

@ApplicationScoped
public class FileSearcher {
  
  private static final String TYPE = "file";
  private static final String FILE_ID_FIELD = "fileId";
  private static final String PAGE_ID_FIELD = "pageId";
  private static final String ORGANIZATION_ID_FIELD = "organizationId";
  
  @Inject
  private Logger logger;
  
  @Inject
  private IdController idController;
  
  @Inject
  private IndexReader indexReader;

  public SearchResult<FileId> searchFiles(OrganizationId organizationId, PageId pageId, String queryString, Long firstResult, Long maxResults) {
    OrganizationId kuntaApiOrganizationId = idController.translateOrganizationId(organizationId, KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiOrganizationId == null) {
      logger.severe(String.format("Failed to translate organization %s into Kunta API id", organizationId.toString()));
      return new SearchResult<>(Collections.emptyList());
    }

    BoolQueryBuilder query = boolQuery()
        .must(matchQuery(ORGANIZATION_ID_FIELD, kuntaApiOrganizationId.getId()));
    
    if (queryString != null) {
      query.must(queryStringQuery(queryString));
    }

    if (pageId != null) {
      PageId kuntaApiPageId = idController.translatePageId(pageId, KuntaApiConsts.IDENTIFIER_NAME);
      if (kuntaApiPageId == null) {
        logger.severe(String.format("Failed to translate page %s into Kunta API id", pageId.toString()));
        return new SearchResult<>(Collections.emptyList());
      }
      
      query.must(matchQuery(PAGE_ID_FIELD, kuntaApiPageId.getId()));
    }
    
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
    
    requestBuilder.setFrom(firstResult != null ? firstResult.intValue() : 0);
    requestBuilder.setSize(maxResults != null ? maxResults.intValue() : IndexReader.MAX_RESULTS);
    requestBuilder.addSort(AbstractIndexHander.ORDER_INDEX_FIELD, SortOrder.ASC);
    
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
