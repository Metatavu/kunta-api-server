package fi.otavanopisto.kuntaapi.server.integrations.casem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
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

import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.id.FileId;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PageId;
import fi.otavanopisto.kuntaapi.server.index.IndexRequest;
import fi.otavanopisto.kuntaapi.server.index.IndexableAttachment;
import fi.otavanopisto.kuntaapi.server.index.IndexableFile;
import fi.otavanopisto.kuntaapi.server.integrations.BinaryHttpClient.BinaryResponse;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.system.SystemUtils;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class CaseMAttachmentEntityUpdater extends EntityUpdater {

  private static final int TIMER_INTERVAL = 1000;
  
  @Inject
  private Logger logger;
  
  @Inject
  private CaseMFileController caseMFileController;
  
  @Inject
  private IdController idController;
  
  @Inject
  private IdentifierController identifierController; 
  
  @Inject
  private Event<IndexRequest> indexRequest;
  
  @Resource
  private TimerService timerService;
  
  private boolean stopped;
  
  private List<FileUpdateRequest> queue;

  @PostConstruct
  public void init() {
    queue = Collections.synchronizedList(new ArrayList<>());
  }

  @Override
  public String getName() {
    return "casem-attachments";
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
  public void onCaseMMeetingDataUpdateRequest(@Observes FileUpdateRequest event) {
    if (!stopped) {
      FileId fileId = event.getFileId();
      if (StringUtils.equals(fileId.getSource(), CaseMConsts.IDENTIFIER_NAME)) {
        queue.add(event);
      }
    }
  }

  @Timeout
  public void timeout(Timer timer) {
    if (!stopped) {
      if (!queue.isEmpty()) {
        updateFile(queue.remove(0));
      }

      startTimer(SystemUtils.inTestMode() ? 1000 : TIMER_INTERVAL);
    }
  }

  private void updateFile(FileUpdateRequest request) {
    PageId caseMPageId = request.getPageId();
    PageId kuntaApiPageId = null;
    if (caseMPageId != null) {
      kuntaApiPageId = idController.translatePageId(caseMPageId, KuntaApiConsts.IDENTIFIER_NAME);
    }

    FileId caseMFileId = request.getFileId();
    OrganizationId organizationId = idController.translateOrganizationId(caseMFileId.getOrganizationId(), KuntaApiConsts.IDENTIFIER_NAME);
    if (organizationId == null) {
      logger.info(String.format("Could not translate organization %s into kunta api id", caseMFileId.getOrganizationId().toString()));
      return;
    }
    
    Identifier identifier = identifierController.findIdentifierById(caseMFileId);
    if (identifier == null) {
      identifier = identifierController.createIdentifier(caseMFileId);
    }
    
    FileId kuntaApiFileId = new FileId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
    
    BinaryResponse binaryResponse = caseMFileController.downloadFile(caseMFileId);
    if (binaryResponse != null) {
      indexFile(organizationId, kuntaApiFileId, kuntaApiPageId, binaryResponse);
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

}
