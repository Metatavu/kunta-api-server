package fi.otavanopisto.kuntaapi.server.integrations.casem;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.ejb.TimerService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.apache.commons.codec.binary.Base64;

import fi.metatavu.kuntaapi.server.rest.model.FileDef;
import fi.otavanopisto.kuntaapi.server.cache.ModificationHashCache;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierRelationController;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.id.BaseId;
import fi.otavanopisto.kuntaapi.server.id.FileId;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PageId;
import fi.otavanopisto.kuntaapi.server.index.IndexRemoveFile;
import fi.otavanopisto.kuntaapi.server.index.IndexRemoveRequest;
import fi.otavanopisto.kuntaapi.server.index.IndexRequest;
import fi.otavanopisto.kuntaapi.server.index.IndexableAttachment;
import fi.otavanopisto.kuntaapi.server.index.IndexableFile;
import fi.otavanopisto.kuntaapi.server.integrations.BinaryHttpClient.BinaryResponse;
import fi.otavanopisto.kuntaapi.server.integrations.BinaryHttpClient.DownloadMeta;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.integrations.casem.resources.CasemFileResourceContainer;
import fi.otavanopisto.kuntaapi.server.integrations.casem.tasks.FileIdTaskQueue;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask;
import fi.otavanopisto.kuntaapi.server.tasks.IdTask.Operation;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class CasemFileEntityUpdater extends EntityUpdater {

  @Inject
  private Logger logger;

  @Inject
  private CaseMTranslator casemTranslator;
  
  @Inject
  private IdController idController;

  @Inject
  private IdentifierController identifierController;

  @Inject
  private IdentifierRelationController identifierRelationController;

  @Inject
  private CasemFileResourceContainer fileCache;
  
  @Inject
  private ModificationHashCache modificationHashCache;

  @Inject
  private CaseMFileController casemFileController;

  @Inject
  private Event<IndexRequest> indexRequest;

  @Inject
  private Event<IndexRemoveRequest> indexRemoveRequest;

  @Inject
  private FileIdTaskQueue fileIdTaskQueue;

  @Resource
  private TimerService timerService;

  @Override
  public String getName() {
    return "casem-files";
  }

  @Override
  public void timeout() {
    executeNextTask();
  }
  
  @Override
  public TimerService getTimerService() {
    return timerService;
  }
  
  private void executeNextTask() {
    IdTask<FileId> task = fileIdTaskQueue.next();
    if (task != null) {
      if (task.getOperation() == Operation.UPDATE) {
        updateCasemFile((PageId) task.getParentId(), task.getId(), task.getOrderIndex()); 
      } else {
        deleteFile(task.getId());
      }
    }
  }
  
  private void updateCasemFile(PageId casemPageId, FileId casemFileId, Long orderIndex) {
    OrganizationId organizationId = casemFileId.getOrganizationId();
    
    DownloadMeta downloadMeta = casemFileController.getDownloadMeta(casemFileId);
    if (downloadMeta != null) {
      updateCasemFile(casemFileId, casemPageId, downloadMeta, orderIndex);
    } else {
      logger.log(Level.SEVERE, () -> String.format("Organization %s file %s meta could not be downloaded", organizationId.getId(), casemFileId.toString()));
    }
  }
  
  private void updateCasemFile(FileId casemFileId, PageId casemPageId, DownloadMeta downloadMeta, Long orderIndex) {
    OrganizationId organizationId = casemFileId.getOrganizationId();
    PageId kuntaApiPageId = null;

    if (casemPageId != null) {
      kuntaApiPageId = idController.translatePageId(casemPageId, KuntaApiConsts.IDENTIFIER_NAME);
      if (kuntaApiPageId == null) {
        logger.log(Level.SEVERE, () -> String.format("Could not translate %s page into kunta api page id", casemPageId.toString()));
      }
    }

    BaseId parentId = kuntaApiPageId != null ? kuntaApiPageId : organizationId;
    Identifier identifier = identifierController.acquireIdentifier(orderIndex, casemFileId);
    identifierRelationController.setParentId(identifier, parentId);
    
    FileId kuntaApiFileId = new FileId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
    FileDef fileDef = casemTranslator.translateFile(kuntaApiPageId, kuntaApiFileId, downloadMeta);
    if (fileDef != null) {
      fileCache.put(kuntaApiFileId, fileDef);
      modificationHashCache.put(kuntaApiFileId.getId(), createPojoHash(fileDef));
      
      BinaryResponse binaryResponse = casemFileController.downloadFile(casemFileId);
      if (binaryResponse != null) {
        indexFile(organizationId, kuntaApiFileId, kuntaApiPageId, binaryResponse);
      }
    } else {
      logger.log(Level.SEVERE, () -> String.format("Could not translate file %s", kuntaApiFileId.toString()));
    }
  }
  
  private void indexFile(OrganizationId organizationId, FileId kuntaApiFileId, PageId kuntaApiPageId, BinaryResponse binaryResponse) {
    String encodedData = Base64.encodeBase64String(binaryResponse.getData());

    IndexableAttachment data = new IndexableAttachment();
    data.setContent(encodedData);
    data.setContentType(binaryResponse.getType());
    data.setName(binaryResponse.getMeta().getFilename());
    
    IndexableFile indexableFile = new IndexableFile();
    indexableFile.setFileId(kuntaApiFileId.getId());
    indexableFile.setData(data);
    indexableFile.setOrganizationId(organizationId.getId());
    
    if (kuntaApiPageId != null) {
      indexableFile.setPageId(kuntaApiPageId.getId());
    }
    
    indexRequest.fire(new IndexRequest(indexableFile));
  }

  private void deleteFile(FileId casemFileId) {
    OrganizationId organizationId = casemFileId.getOrganizationId();
    Identifier fileIdentifier = identifierController.findIdentifierById(casemFileId);
    if (fileIdentifier != null) {
      FileId kuntaApiFileId = new FileId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, fileIdentifier.getKuntaApiId());
      modificationHashCache.clear(fileIdentifier.getKuntaApiId());
      fileCache.clear(kuntaApiFileId);
      identifierController.deleteIdentifier(fileIdentifier);
      IndexRemoveFile indexRemove = new IndexRemoveFile();
      indexRemove.setFileId(kuntaApiFileId.getId());
      indexRemoveRequest.fire(new IndexRemoveRequest(indexRemove));
    }
  }
  
}
