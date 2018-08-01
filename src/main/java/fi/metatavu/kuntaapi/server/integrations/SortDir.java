package fi.metatavu.kuntaapi.server.integrations;

import org.elasticsearch.search.sort.SortOrder;

public enum SortDir {

  ASC,
  
  DESC;
  
  public SortOrder toElasticSortOrder() {
    return this == SortDir.DESC ? SortOrder.DESC : SortOrder.ASC;
  }
    
}
