package fi.otavanopisto.kuntaapi.server.integrations.management;

import java.util.ArrayList;
import java.util.List;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.cache.BannerCache;
import fi.otavanopisto.kuntaapi.server.cache.BannerImageCache;
import fi.otavanopisto.kuntaapi.server.id.AttachmentId;
import fi.otavanopisto.kuntaapi.server.id.BannerId;
import fi.otavanopisto.kuntaapi.server.id.IdPair;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.AttachmentData;
import fi.otavanopisto.kuntaapi.server.integrations.BannerProvider;
import fi.otavanopisto.kuntaapi.server.rest.model.Attachment;
import fi.otavanopisto.kuntaapi.server.rest.model.Banner;
import fi.otavanopisto.mwp.client.model.Attachment.MediaTypeEnum;

/**
 * Banner provider for management wordpress
 * 
 * @author Antti Leppä
 */
@RequestScoped
public class ManagementBannerProvider extends AbstractManagementProvider implements BannerProvider {
  
  @Inject
  private BannerCache bannerCache;
  
  @Inject
  private BannerImageCache bannerImageCache;
  
  @Inject
  private ManagementImageLoader managementImageLoader;
  
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
    List<IdPair<BannerId,AttachmentId>> childIds = bannerImageCache.getChildIds(bannerId);
    List<Attachment> result = new ArrayList<>(childIds.size());
    
    for (IdPair<BannerId, AttachmentId> childId : childIds) {
      Attachment attachment = bannerImageCache.get(childId);
      if (attachment != null) {
        result.add(attachment);
      }
    }
    
    return result;
  }

  @Override
  public Attachment findBannerImage(OrganizationId organizationId, BannerId bannerId, AttachmentId attachmentId) {
    return bannerImageCache.get(new IdPair<BannerId, AttachmentId>(bannerId, attachmentId));
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

}
