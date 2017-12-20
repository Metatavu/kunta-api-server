package fi.otavanopisto.kuntaapi.server.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.rest.model.Code;
import fi.otavanopisto.kuntaapi.server.id.CodeId;
import fi.otavanopisto.kuntaapi.server.index.SearchResult;
import fi.otavanopisto.kuntaapi.server.index.search.CodeSearcher;
import fi.otavanopisto.kuntaapi.server.integrations.CodeProvider;
import fi.otavanopisto.kuntaapi.server.integrations.CodeSortBy;
import fi.otavanopisto.kuntaapi.server.integrations.SortDir;

@ApplicationScoped
public class CodeController {

  @Inject
  private CodeSearcher codeSearcher;

  @Inject
  private Instance<CodeProvider> codeProviders;
  
  public Code findCode(CodeId codeId) {
    for (CodeProvider codeProvider : getCodeProviders()) {
      Code code = codeProvider.findCode(codeId);
      if (code != null) {
        return code;
      }
    }
    
    return null;
  }
  
  public SearchResult<Code> searchCodes(String queryString, List<String> types, CodeSortBy sortBy, SortDir sortDir, Long firstResult, Long maxResults) {
    SearchResult<CodeId> searchResult = codeSearcher.searchCodes(queryString, types, sortBy, sortDir, firstResult, maxResults);
    if (searchResult != null) {
      List<Code> codes = new ArrayList<>(searchResult.getResult().size());

      for (CodeId codeId : searchResult.getResult()) {
        Code code = findCode(codeId);
        if (code != null) {
          codes.add(code);
        }
      }

      return new SearchResult<>(codes, searchResult.getTotalHits());
    }
    
    return SearchResult.emptyResult();
  }
  
  private List<CodeProvider> getCodeProviders() {
    List<CodeProvider> result = new ArrayList<>();
    
    Iterator<CodeProvider> iterator = codeProviders.iterator();
    while (iterator.hasNext()) {
      result.add(iterator.next());
    }
    
    return Collections.unmodifiableList(result);
  }
  
}
