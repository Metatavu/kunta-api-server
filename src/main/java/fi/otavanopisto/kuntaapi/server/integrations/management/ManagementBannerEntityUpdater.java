package fi.otavanopisto.kuntaapi.server.integrations.management;

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
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import fi.otavanopisto.kuntaapi.server.cache.BannerCache;
import fi.otavanopisto.kuntaapi.server.cache.BannerImageCache;
import fi.otavanopisto.kuntaapi.server.cache.ModificationHashCache;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.discover.BannerIdRemoveRequest;
import fi.otavanopisto.kuntaapi.server.discover.BannerIdUpdateRequest;
import fi.otavanopisto.kuntaapi.server.discover.EntityUpdater;
import fi.otavanopisto.kuntaapi.server.id.AttachmentId;
import fi.otavanopisto.kuntaapi.server.id.BannerId;
import fi.otavanopisto.kuntaapi.server.id.IdPair;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.AttachmentData;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.settings.OrganizationSettingController;
import fi.otavanopisto.kuntaapi.server.system.SystemUtils;
import fi.metatavu.management.client.ApiResponse;
import fi.metatavu.management.client.DefaultApi;
import fi.metatavu.management.client.model.Attachment;
import fi.metatavu.management.client.model.Banner;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class ManagementBannerEntityUpdater extends EntityUpdater {

  private static final int TIMER_INTERVAL = 5000;

  @Inject
  private Logger logger;
  
  @Inject
  private ManagementTranslator managementTranslator;
  
  @Inject
  private BannerCache bannerCache;
  
  @Inject
  private BannerImageCache bannerImageCache;
  
  @Inject
  private ManagementApi managementApi;
  
  @Inject
  private ManagementImageLoader managementImageLoader;
  
  @Inject
  private OrganizationSettingController organizationSettingController; 
  
  @Inject
  private IdentifierController identifierController;
  
  @Inject
  private ModificationHashCache modificationHashCache;
  
  @Resource
  private TimerService timerService;

  private boolean stopped;
  private List<BannerIdUpdateRequest> queue;

  @PostConstruct
  public void init() {
    queue = Collections.synchronizedList(new ArrayList<>());
  }

  @Override
  public String getName() {
    return "management-banners";
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
  public void onBannerIdUpdateRequest(@Observes BannerIdUpdateRequest event) {
    if (!stopped) {
      OrganizationId organizationId = event.getOrganizationId();
      
      if (organizationSettingController.getSettingValue(organizationId, ManagementConsts.ORGANIZATION_SETTING_BASEURL) == null) {
        return;
      }
      
      if (event.isPriority()) {
        queue.remove(event);
        queue.add(0, event);
      } else {
        if (!queue.contains(event)) {
          queue.add(event);
        }
      }
    }
  }
  
  @Asynchronous
  public void onBannerIdRemoveRequest(@Observes BannerIdRemoveRequest event) {
    if (!stopped) {
      BannerId bannerId = event.getId();
      
      if (!StringUtils.equals(bannerId.getSource(), ManagementConsts.IDENTIFIER_NAME)) {
        return;
      }
      
      deleteBanner(event.getOrganizationId(), bannerId);
    }
  }

  @Timeout
  public void timeout(Timer timer) {
    if (!stopped) {
      if (!queue.isEmpty()) {
        BannerIdUpdateRequest updateRequest = queue.remove(0);
        OrganizationId organizationId = updateRequest.getOrganizationId();
        DefaultApi api = managementApi.getApi(organizationId);
        
        updateManagementBanner(api, organizationId, updateRequest.getId());
      }

      startTimer(SystemUtils.inTestMode() ? 1000 : TIMER_INTERVAL);
    }
  }

  private void updateManagementBanner(DefaultApi api, OrganizationId organizationId, BannerId managementBannerId) {
    fi.metatavu.management.client.ApiResponse<Banner> response = api.wpV2BannerIdGet(managementBannerId.getId(), null, null);
    if (response.isOk()) {
      updateManagementBanner(api, organizationId, response.getResponse());
    } else {
      logger.warning(String.format("Finding organization %s banner failed on [%d] %s", managementBannerId.getId(), response.getStatus(), response.getMessage()));
    }
  }
  
  private void updateManagementBanner(DefaultApi api, OrganizationId organizationId, Banner managementBanner) {
    BannerId bannerId = new BannerId(organizationId, ManagementConsts.IDENTIFIER_NAME, String.valueOf(managementBanner.getId()));

    Identifier identifier = identifierController.findIdentifierById(bannerId);
    if (identifier == null) {
      identifier = identifierController.createIdentifier(bannerId);
    }
    
    BannerId bannerKuntaApiId = new BannerId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
    
    fi.otavanopisto.kuntaapi.server.rest.model.Banner banner = managementTranslator.translateBanner(bannerKuntaApiId, managementBanner);
    if (banner == null) {
      logger.severe(String.format("Could not translate banner %d into Kunta API banner", managementBanner.getId()));
      return;
    }
    
    bannerCache.put(bannerKuntaApiId, banner);
    modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(banner));
    
    if (managementBanner.getFeaturedMedia() != null && managementBanner.getFeaturedMedia() > 0) {
      updateFeaturedMedia(organizationId, api, bannerKuntaApiId, managementBanner.getFeaturedMedia()); 
    }

  }
  
  private void updateFeaturedMedia(OrganizationId organizationId, DefaultApi api, BannerId bannerId, Integer featuredMedia) {
    ApiResponse<fi.metatavu.management.client.model.Attachment> response = api.wpV2MediaIdGet(String.valueOf(featuredMedia), null, null);
    if (!response.isOk()) {
      logger.severe(String.format("Finding media failed on [%d] %s", response.getStatus(), response.getMessage()));
    } else {
      Attachment managementAttachment = response.getResponse();
      AttachmentId managementAttachmentId = new AttachmentId(organizationId, ManagementConsts.IDENTIFIER_NAME, String.valueOf(managementAttachment.getId()));
      
      Identifier identifier = identifierController.findIdentifierById(managementAttachmentId);
      if (identifier == null) {
        identifier = identifierController.createIdentifier(managementAttachmentId);
      }
      
      AttachmentId kuntaApiAttachmentId = new AttachmentId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
      fi.otavanopisto.kuntaapi.server.rest.model.Attachment attachment = managementTranslator.translateAttachment(kuntaApiAttachmentId, managementAttachment);
      if (attachment != null) {
        bannerImageCache.put(new IdPair<>(bannerId, kuntaApiAttachmentId), attachment);
        
        AttachmentData imageData = managementImageLoader.getImageData(managementAttachment.getSourceUrl());
        if (imageData != null) {
          String dataHash = DigestUtils.md5Hex(imageData.getData());
          modificationHashCache.put(identifier.getKuntaApiId(), dataHash);
        }
      }
    }
  }

  private void deleteBanner(OrganizationId organizationId, BannerId bannerId) {
    Identifier bannerIdentifier = identifierController.findIdentifierById(bannerId);
    if (bannerIdentifier != null) {
      BannerId kuntaApiBannerId = new BannerId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, bannerIdentifier.getKuntaApiId());
      queue.remove(new BannerIdUpdateRequest(organizationId, kuntaApiBannerId, false));

      modificationHashCache.clear(bannerIdentifier.getKuntaApiId());
      bannerCache.clear(kuntaApiBannerId);
      identifierController.deleteIdentifier(bannerIdentifier);
      
      List<IdPair<BannerId,AttachmentId>> bannerImageIds = bannerImageCache.getChildIds(kuntaApiBannerId);
      for (IdPair<BannerId,AttachmentId> bannerImageId : bannerImageIds) {
        AttachmentId attachmentId = bannerImageId.getChild();
        bannerImageCache.clear(bannerImageId);
        modificationHashCache.clear(attachmentId.getId());
        
        Identifier imageIdentifier = identifierController.findIdentifierById(attachmentId);
        if (imageIdentifier != null) {
          identifierController.deleteIdentifier(imageIdentifier);
        }
      }
    }
  }
  
}
