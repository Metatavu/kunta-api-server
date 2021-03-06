package fi.metatavu.kuntaapi.server.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.rest.model.FileDef;
import fi.metatavu.kuntaapi.server.id.FileId;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.id.PageId;
import fi.metatavu.kuntaapi.server.index.FileSearcher;
import fi.metatavu.kuntaapi.server.index.SearchResult;
import fi.metatavu.kuntaapi.server.integrations.AttachmentData;
import fi.metatavu.kuntaapi.server.integrations.FileProvider;

@ApplicationScoped
@SuppressWarnings ("squid:S3306")
public class FileController {

  @Inject
  private FileSearcher fileSearcher;

  @Inject
  private Instance<FileProvider> fileProviders;

  public List<FileDef> searchFiles(OrganizationId organizationId, PageId pageId, String queryString, Long firstResult, Long maxResults) {
    SearchResult<FileId> searchResult = fileSearcher.searchFiles(organizationId, pageId, queryString, firstResult, maxResults);
    return processSearchResult(organizationId, searchResult);
  }
  
  public List<FileDef> listFiles(OrganizationId organizationId, PageId pageId, Long firstResult, Long maxResults) {
    SearchResult<FileId> searchResult = fileSearcher.searchFiles(organizationId, pageId, null, firstResult, maxResults);
    return processSearchResult(organizationId, searchResult);
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
  
  /**
   * Deletes a file
   * 
   * @param organizationId organization id
   * @param fileId fileId
   * @return returns whether file was deleted or not
   */
  public boolean deleteFile(OrganizationId organizationId, FileId fileId) {
    for (FileProvider fileProvider : getFileProviders()) {
      if (fileProvider.deleteOrganizationFile(organizationId, fileId)) {
        return true;
      }
    }
    
    return false;
  }

  public AttachmentData getFileData(OrganizationId organizationId, FileId fileId) {
    for (FileProvider fileProvider : getFileProviders()) {
      AttachmentData data = fileProvider.getOrganizationFileData(organizationId, fileId);
      if (data != null) {
        return data;
      }
    }
    
    return null;
  }

  private List<FileDef> processSearchResult(OrganizationId organizationId, SearchResult<FileId> searchResult) {
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
  
  private List<FileProvider> getFileProviders() {
    List<FileProvider> result = new ArrayList<>();
    
    Iterator<FileProvider> iterator = fileProviders.iterator();
    while (iterator.hasNext()) {
      result.add(iterator.next());
    }
    
    return Collections.unmodifiableList(result);
  }
  
}
