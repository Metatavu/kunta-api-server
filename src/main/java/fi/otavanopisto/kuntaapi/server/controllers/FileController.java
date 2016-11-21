package fi.otavanopisto.kuntaapi.server.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.id.FileId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PageId;
import fi.otavanopisto.kuntaapi.server.index.FileSearcher;
import fi.otavanopisto.kuntaapi.server.index.SearchResult;
import fi.otavanopisto.kuntaapi.server.integrations.FileProvider;
import fi.otavanopisto.kuntaapi.server.rest.model.FileDef;

@ApplicationScoped
public class FileController {
  
  @Inject
  private Logger logger;
  
  @Inject
  private FileSearcher fileSearcher;

  @Inject
  private Instance<FileProvider> fileProviders;

  public List<FileDef> searchFiles(OrganizationId organizationId, PageId pageId, String queryString, Long firstResult, Long maxResults) {
    SearchResult<FileId> searchResult = fileSearcher.searchFiles(organizationId, pageId, queryString, firstResult, maxResults);
    
    if (searchResult != null) {
      List<FileDef> result = new ArrayList<>(searchResult.getResult().size());
      
      for (FileId fileId : searchResult.getResult()) {
        FileDef file = findFile(organizationId, fileId);
        if (file != null) {
          result.add(file);
        }
      }
      
      return result;
    }
    
    return Collections.emptyList();
  }
  
  public List<FileDef> listFiles(OrganizationId organizationId, PageId pageId, Long firstResult, Long maxResults) {
    List<FileDef> result = new ArrayList<>();
    
    for (FileProvider fileProvider : getFileProviders()) {
      List<FileDef> files = fileProvider.listOrganizationFiles(organizationId, pageId);
      if (files != null) {
        result.addAll(files);
      } else {
        logger.severe(String.format("File provider %s returned null when listing files", fileProvider.getClass().getName())); 
      }
    }
    
    int resultCount = result.size();
    int firstIndex = firstResult == null ? 0 : Math.min(firstResult.intValue(), resultCount);
    int toIndex = maxResults == null ? resultCount : Math.min(firstIndex + maxResults.intValue(), resultCount);
    
    return result.subList(firstIndex, toIndex);
  }
  
  public FileDef findFile(OrganizationId organizationId, FileId fileId) {
    for (FileProvider fileProvider : getFileProviders()) {
      FileDef file = fileProvider.findOrganizationFile(organizationId, fileId);
      if (file != null) {
        return file;
      }
    }
    
    return null;
  }
  
  private List<FileProvider> getFileProviders() {
    List<FileProvider> result = new ArrayList<>();
    
    Iterator<FileProvider> iterator = fileProviders.iterator();
    while (iterator.hasNext()) {
      result.add(iterator.next());
    }
    
    return Collections.unmodifiableList(result);
  }
  
}
