package fi.otavanopisto.kuntaapi.server.integrations.casem;

import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.AccessTimeout;
import javax.ejb.Asynchronous;
import javax.ejb.Singleton;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;

import fi.metatavu.kuntaapi.server.rest.model.FileDef;
import fi.otavanopisto.kuntaapi.server.cache.ModificationHashCache;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.discover.FileIdRemoveRequest;
import fi.otavanopisto.kuntaapi.server.discover.FileIdUpdateRequest;
import fi.otavanopisto.kuntaapi.server.discover.IdUpdateRequestQueue;
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
import fi.otavanopisto.kuntaapi.server.integrations.casem.cache.CasemFileCache;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class CasemFileEntityUpdater extends EntityUpdater {

  private static final int TIMER_INTERVAL = 1000;

  @Inject
  private Logger logger;

  @Inject
  private SystemSettingController systemSettingController;
  
  @Inject
  private CaseMTranslator casemTranslator;
  
  @Inject
  private IdController idController;

  @Inject
  private IdentifierController identifierController;

  @Inject
  private CasemFileCache fileCache;
  
  @Inject
  private ModificationHashCache modificationHashCache;

  @Inject
  private CaseMFileController casemFileController;

  @Inject
  private Event<IndexRequest> indexRequest;

  @Inject
  private Event<IndexRemoveRequest> indexRemoveRequest;
  
  @Resource
  private TimerService timerService;

  private boolean stopped;
  private IdUpdateRequestQueue<FileIdUpdateRequest> queue;

  @PostConstruct
  public void init() {
    queue = new IdUpdateRequestQueue<>(CaseMConsts.IDENTIFIER_NAME);
  }

  @Override
  public String getName() {
    return "casem-files";
  }

  @Override
  public void startTimer() {
    startTimer(TIMER_INTERVAL);
  }

  private void startTimer(int duration) {
    stopped = false;
    TimerConfig timerConfig = new TimerConfig();
    timerConfig.setPersistent(false);
    timerService.createSingleActionTimer(duration, timerConfig);
  }

  @Override
  public void stopTimer() {
    stopped = true;
  }
  
  @Asynchronous
  public void onPageIdUpdateRequest(@Observes FileIdUpdateRequest event) {
    if (!stopped) {
      FileId fileId = event.getId();
      
      if (!StringUtils.equals(fileId.getSource(), CaseMConsts.IDENTIFIER_NAME)) {
        return;
      }
      
      queue.add(event);
    }
  }
  
  @Asynchronous
  public void onPageIdRemoveRequest(@Observes FileIdRemoveRequest event) {
    if (!stopped) {
      FileId fileId = event.getId();
      
      if (!StringUtils.equals(fileId.getSource(), CaseMConsts.IDENTIFIER_NAME)) {
        return;
      }
      
      deleteFile(event, fileId);
    }
  }
  
  @Timeout
  public void timeout(Timer timer) {
    if (!stopped) {
      if (systemSettingController.isNotTestingOrTestRunning()) {
        FileIdUpdateRequest updateRequest = queue.next();
        if (updateRequest != null) {
          updateCasemFile(updateRequest);
        }
      }
      
      startTimer(systemSettingController.inTestMode() ? 1000 : TIMER_INTERVAL);
    }
  }
  
  private void updateCasemFile(FileIdUpdateRequest updateRequest) {
    OrganizationId organizationId = updateRequest.getOrganizationId();
    FileId casemFileId = updateRequest.getId();
    PageId casemPageId = updateRequest.getPageId();
    
    DownloadMeta downloadMeta = casemFileController.getDownloadMeta(casemFileId);
    if (downloadMeta != null) {
      updateCasemFile(casemFileId, casemPageId, downloadMeta, updateRequest.getOrderIndex());
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
    Identifier identifier = identifierController.findIdentifierById(casemFileId);
    if (identifier == null) {
      identifier = identifierController.createIdentifier(parentId, orderIndex, casemFileId);
    } else {
      identifier = identifierController.updateIdentifier(identifier, parentId, orderIndex);
    }
    
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

  private void deleteFile(FileIdRemoveRequest event, FileId casemFileId) {
    OrganizationId organizationId = event.getOrganizationId();
    Identifier fileIdentifier = identifierController.findIdentifierById(casemFileId);
    if (fileIdentifier != null) {
      FileId kuntaApiFileId = new FileId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, fileIdentifier.getKuntaApiId());
      queue.remove(casemFileId);
      modificationHashCache.clear(fileIdentifier.getKuntaApiId());
      fileCache.clear(kuntaApiFileId);
      identifierController.deleteIdentifier(fileIdentifier);
      IndexRemoveFile indexRemove = new IndexRemoveFile();
      indexRemove.setFileId(kuntaApiFileId.getId());
      indexRemoveRequest.fire(new IndexRemoveRequest(indexRemove));
    }
  }
  
}
