package fi.metatavu.kuntaapi.server.integrations.management;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.ActivationConfigProperty;
import javax.ejb.MessageDriven;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.codec.digest.DigestUtils;
import org.jboss.ejb3.annotation.Pool;

import fi.metatavu.kuntaapi.server.cache.ModificationHashCache;
import fi.metatavu.kuntaapi.server.controllers.IdentifierController;
import fi.metatavu.kuntaapi.server.controllers.IdentifierRelationController;
import fi.metatavu.kuntaapi.server.id.AttachmentId;
import fi.metatavu.kuntaapi.server.id.BannerId;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.images.ScaledImageStore;
import fi.metatavu.kuntaapi.server.integrations.AttachmentData;
import fi.metatavu.kuntaapi.server.integrations.KuntaApiConsts;
import fi.metatavu.kuntaapi.server.integrations.management.client.ManagementApi;
import fi.metatavu.kuntaapi.server.integrations.management.resources.ManagementAttachmentDataResourceContainer;
import fi.metatavu.kuntaapi.server.integrations.management.resources.ManagementAttachmentResourceContainer;
import fi.metatavu.kuntaapi.server.integrations.management.tasks.BannerIdTaskQueue;
import fi.metatavu.kuntaapi.server.persistence.model.Identifier;
import fi.metatavu.kuntaapi.server.resources.BannerResourceContainer;
import fi.metatavu.kuntaapi.server.settings.OrganizationSettingController;
import fi.metatavu.kuntaapi.server.tasks.IdTask;
import fi.metatavu.kuntaapi.server.tasks.IdTask.Operation;
import fi.metatavu.kuntaapi.server.tasks.jms.AbstractJmsJob;
import fi.metatavu.kuntaapi.server.tasks.jms.JmsQueueProperties;
import fi.metatavu.management.client.ApiResponse;
import fi.metatavu.management.client.DefaultApi;
import fi.metatavu.management.client.model.Attachment;
import fi.metatavu.management.client.model.Banner;

@ApplicationScoped
@SuppressWarnings ("squid:S3306")
@MessageDriven (
  activationConfig = {
    @ActivationConfigProperty (propertyName = JmsQueueProperties.DESTINATION_LOOKUP, propertyValue = BannerIdTaskQueue.JMS_QUEUE)
  }
)
@Pool(JmsQueueProperties.NO_CONCURRENCY_POOL)
public class ManagementBannerEntityDiscoverJob extends AbstractJmsJob<IdTask<BannerId>> {

  @Inject
  private Logger logger;

  @Inject
  private OrganizationSettingController organizationSettingController; 

  @Inject
  private ManagementTranslator managementTranslator;
  
  @Inject
  private BannerResourceContainer bannerResourceContainer;
  
  @Inject
  private ManagementAttachmentResourceContainer managementAttachmentResourceContainer;

  @Inject
  private ManagementAttachmentDataResourceContainer managementAttachmentDataResourceContainer;

  @Inject
  private ManagementApi managementApi;
  
  @Inject
  private ManagementImageLoader managementImageLoader;
  
  @Inject
  private IdentifierController identifierController;

  @Inject
  private IdentifierRelationController identifierRelationController;
  
  @Inject
  private ModificationHashCache modificationHashCache;

  @Inject
  private ScaledImageStore scaledImageStore;
  
  @Override
  public void execute(IdTask<BannerId> task) {
    if (task.getOperation() == Operation.UPDATE) {
      updateManagementBanner(task.getId(), task.getOrderIndex()); 
    } else if (task.getOperation() == Operation.REMOVE) {
      deleteManagementBanner(task.getId());
    }
  }

  private void updateManagementBanner(BannerId managementBannerId, Long orderIndex) {
    OrganizationId organizationId = managementBannerId.getOrganizationId();
    if (!organizationSettingController.hasSettingValue(organizationId, ManagementConsts.ORGANIZATION_SETTING_BASEURL)) {
      logger.log(Level.INFO, "Organization management baseUrl not set, skipping update"); 
      return;
    }
    
    DefaultApi api = managementApi.getApi(organizationId);
    
    fi.metatavu.management.client.ApiResponse<Banner> response = api.wpV2BannerIdGet(managementBannerId.getId(), null, null, null);
    if (response.isOk()) {
      updateManagementBanner(api, organizationId, response.getResponse(), orderIndex);
    } else {
      logger.warning(() -> String.format("Finding organization %s banner failed on [%d] %s", managementBannerId.getId(), response.getStatus(), response.getMessage()));
    }
  }
  
  private void updateManagementBanner(DefaultApi api, OrganizationId organizationId, Banner managementBanner, Long orderIndex) {
    BannerId managementBannerId = new BannerId(organizationId, ManagementConsts.IDENTIFIER_NAME, String.valueOf(managementBanner.getId()));

    Identifier identifier = identifierController.acquireIdentifier(orderIndex, managementBannerId);
    identifierRelationController.setParentId(identifier, organizationId);
    
    BannerId bannerKuntaApiId = new BannerId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
    fi.metatavu.kuntaapi.server.rest.model.Banner banner = managementTranslator.translateBanner(bannerKuntaApiId, managementBanner);
    if (banner == null) {
      logger.severe(() -> String.format("Could not translate banner %d into Kunta API banner", managementBanner.getId()));
      return;
    }
    
    bannerResourceContainer.put(bannerKuntaApiId, banner);
    modificationHashCache.put(identifier.getKuntaApiId(), createPojoHash(banner));
    
    List<AttachmentId> existingAttachmentIds = identifierRelationController.listAttachmentIdsBySourceAndParentId(ManagementConsts.IDENTIFIER_NAME, bannerKuntaApiId);

    if (managementBanner.getFeaturedMedia() != null && managementBanner.getFeaturedMedia() > 0) {
      AttachmentId attachmentId = updateFeaturedMedia(organizationId, api, identifier, managementBanner.getFeaturedMedia());
      if (attachmentId != null) {
        existingAttachmentIds.remove(attachmentId);
      }
    }

    for (AttachmentId existingAttachmentId : existingAttachmentIds) {
      identifierRelationController.removeChild(bannerKuntaApiId, existingAttachmentId);
    }
  }
  
  private AttachmentId updateFeaturedMedia(OrganizationId organizationId, DefaultApi api, Identifier bannerIdentifier, Integer featuredMedia) {
    ApiResponse<fi.metatavu.management.client.model.Attachment> response = api.wpV2MediaIdGet(String.valueOf(featuredMedia), null, null);
    if (!response.isOk()) {
      logger.severe(() -> String.format("Finding media failed on [%d] %s", response.getStatus(), response.getMessage()));
      return null;
    } else {
      Attachment managementAttachment = response.getResponse();
      AttachmentId managementAttachmentId = managementTranslator.createManagementAttachmentId(organizationId, managementAttachment.getId(), ManagementConsts.ATTACHMENT_TYPE_BANNER);
      Long orderIndex = 0l;
      
      Identifier identifier = identifierController.acquireIdentifier(orderIndex, managementAttachmentId);
      identifierRelationController.addChild(bannerIdentifier, identifier);
      
      AttachmentId kuntaApiAttachmentId = new AttachmentId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
      fi.metatavu.kuntaapi.server.rest.model.Attachment attachment = managementTranslator.translateAttachment(kuntaApiAttachmentId, managementAttachment, ManagementConsts.ATTACHMENT_TYPE_BANNER);
      
      if (attachment != null) {
        managementAttachmentResourceContainer.put(kuntaApiAttachmentId, attachment);
        
        AttachmentData imageData = managementImageLoader.getImageData(managementAttachment.getSourceUrl());
        if (imageData != null) {
          String dataHash = DigestUtils.md5Hex(imageData.getData());
          if (!dataHash.equals(modificationHashCache.get(identifier.getKuntaApiId()))) {
            modificationHashCache.put(identifier.getKuntaApiId(), dataHash);
            managementAttachmentDataResourceContainer.put(kuntaApiAttachmentId, imageData);
            scaledImageStore.purgeStoredImages(kuntaApiAttachmentId);
          }
        }
      }
      
      return kuntaApiAttachmentId;
    }
  }

  private void deleteManagementBanner(BannerId managementBannerId) {
    OrganizationId organizationId = managementBannerId.getOrganizationId();
    Identifier bannerIdentifier = identifierController.findIdentifierById(managementBannerId);
    if (bannerIdentifier != null) {
      BannerId kuntaApiBannerId = new BannerId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, bannerIdentifier.getKuntaApiId());
      modificationHashCache.clear(bannerIdentifier.getKuntaApiId());
      bannerResourceContainer.clear(kuntaApiBannerId);
      identifierController.deleteIdentifier(bannerIdentifier);
    }
  }
  
}
