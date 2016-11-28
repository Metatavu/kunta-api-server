package fi.otavanopisto.kuntaapi.server.integrations.management;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.cache.BannerCache;
import fi.otavanopisto.kuntaapi.server.id.AttachmentId;
import fi.otavanopisto.kuntaapi.server.id.BannerId;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.AttachmentData;
import fi.otavanopisto.kuntaapi.server.integrations.BannerProvider;
import fi.otavanopisto.kuntaapi.server.rest.model.Attachment;
import fi.otavanopisto.kuntaapi.server.rest.model.Banner;
import fi.otavanopisto.mwp.client.ApiResponse;
import fi.otavanopisto.mwp.client.model.Attachment.MediaTypeEnum;

/**
 * Banner provider for management wordpress
 * 
 * @author Antti Leppä
 */
@RequestScoped
public class ManagementBannerProvider extends AbstractManagementProvider implements BannerProvider {
  
  @Inject
  private Logger logger;
  
  @Inject
  private BannerCache bannerCache;
  
  @Inject
  private ManagementImageLoader managementImageLoader;
  
  @Inject
  private ManagementApi managementApi;

  @Inject
  private IdController idController;

  @Override
  public List<Banner> listOrganizationBanners(OrganizationId organizationId) {
    List<BannerId> bannerIds = bannerCache.getOragnizationIds(organizationId);
    List<Banner> banners = new ArrayList<>(bannerIds.size());
    
    for (BannerId bannerId : bannerIds) {
      Banner banner = bannerCache.get(bannerId);
      if (banner != null) {
        banners.add(banner);
      }
    }
    
    return banners;
  }

  @Override
  public Banner findOrganizationBanner(OrganizationId organizationId, BannerId bannerId) {
    return bannerCache.get(bannerId);
  }

  @Override
  public List<Attachment> listOrganizationBannerImages(OrganizationId organizationId, BannerId bannerId) {
    fi.otavanopisto.mwp.client.model.Banner managementBanner = findBannerByBannerId(organizationId, bannerId);
    if (managementBanner != null) {
      Integer featuredMediaId = managementBanner.getFeaturedMedia();
      if (featuredMediaId != null) {
        fi.otavanopisto.mwp.client.model.Attachment featuredMedia = findMedia(organizationId, featuredMediaId);
        if ((featuredMedia != null) && (featuredMedia.getMediaType() == MediaTypeEnum.IMAGE)) {
          return Collections.singletonList(translateAttachment(organizationId, featuredMedia));
        }
      }
    }
  
    return Collections.emptyList();
  }

  @Override
  public Attachment findBannerImage(OrganizationId organizationId, BannerId bannerId, AttachmentId attachmentId) {
    fi.otavanopisto.mwp.client.model.Banner managementBanner = findBannerByBannerId(organizationId, bannerId);
    if (managementBanner != null) {
      Integer featuredMediaId = managementBanner.getFeaturedMedia();
      if (featuredMediaId != null) {
        AttachmentId managementAttachmentId = getImageAttachmentId(organizationId, featuredMediaId);
        if (!idController.idsEqual(attachmentId, managementAttachmentId)) {
          return null;
        }
        
        fi.otavanopisto.mwp.client.model.Attachment attachment = findMedia(organizationId, featuredMediaId);
        if (attachment != null) {
          return translateAttachment(organizationId, attachment);
        }
      }
    }
  
    return null;
  }

  @Override
  public AttachmentData getBannerImageData(OrganizationId organizationId, BannerId bannerId, AttachmentId attachmentId,
      Integer size) {
    
    Integer mediaId = getMediaId(attachmentId);
    if (mediaId == null) {
      return null;
    }
    
    fi.otavanopisto.mwp.client.model.Attachment featuredMedia = findMedia(organizationId, mediaId);
    if (featuredMedia.getMediaType() == MediaTypeEnum.IMAGE) {
      AttachmentData imageData = managementImageLoader.getImageData(featuredMedia.getSourceUrl());
      if (size != null) {
        return scaleImage(imageData, size);
      } else {
        return imageData;
      }
      
    }
    
    return null;
  }

  private fi.otavanopisto.mwp.client.model.Banner findBannerByBannerId(OrganizationId organizationId, BannerId bannerId) {
    BannerId kuntaApiId = idController.translateBannerId(bannerId, ManagementConsts.IDENTIFIER_NAME);
    if (kuntaApiId == null) {
      logger.severe(String.format("Failed to convert %s into MWP id", bannerId.toString()));
      return null;
    }
    
    ApiResponse<fi.otavanopisto.mwp.client.model.Banner> response = managementApi.getApi(organizationId).wpV2BannerIdGet(kuntaApiId.getId(), null);
    if (!response.isOk()) {
      logger.severe(String.format("Finding banner failed on [%d] %s", response.getStatus(), response.getMessage()));
    } else {
      return response.getResponse();
    }
    
    return null;
  }

}
