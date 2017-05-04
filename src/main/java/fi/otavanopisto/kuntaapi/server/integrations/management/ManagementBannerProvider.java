package fi.otavanopisto.kuntaapi.server.integrations.management;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.rest.model.Attachment;
import fi.metatavu.kuntaapi.server.rest.model.Banner;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierRelationController;
import fi.otavanopisto.kuntaapi.server.id.AttachmentId;
import fi.otavanopisto.kuntaapi.server.id.BannerId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.AttachmentData;
import fi.otavanopisto.kuntaapi.server.integrations.BannerProvider;
import fi.otavanopisto.kuntaapi.server.integrations.management.resources.ManagementAttachmentResourceContainer;
import fi.otavanopisto.kuntaapi.server.resources.BannerResourceContainer;
import javax.enterprise.context.ApplicationScoped;

/**
 * Banner provider for management wordpress
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class ManagementBannerProvider extends AbstractManagementProvider implements BannerProvider {
  
  @Inject
  private IdentifierRelationController identifierRelationController;
  
  @Inject
  private BannerResourceContainer bannerResourceContainer;
  
  @Inject
  private ManagementAttachmentResourceContainer managementAttachmentResourceContainer;
  
  @Override
  public List<Banner> listOrganizationBanners(OrganizationId organizationId) {
    List<BannerId> bannerIds = identifierRelationController.listBannerIdsBySourceAndParentId(ManagementConsts.IDENTIFIER_NAME, organizationId);
    List<Banner> banners = new ArrayList<>(bannerIds.size());
    
    for (BannerId bannerId : bannerIds) {
      Banner banner = bannerResourceContainer.get(bannerId);
      if (banner != null) {
        banners.add(banner);
      }
    }
    
    return banners;
  }

  @Override
  public Banner findOrganizationBanner(OrganizationId organizationId, BannerId bannerId) {
    if (!identifierRelationController.isChildOf(organizationId, bannerId)) {
      return null;
    }
    
    return bannerResourceContainer.get(bannerId);
  }

  @Override
  public List<Attachment> listOrganizationBannerImages(OrganizationId organizationId, BannerId bannerId) {
    List<AttachmentId> attachmentIds = identifierRelationController.listAttachmentIdsBySourceAndParentId(ManagementConsts.IDENTIFIER_NAME, bannerId);
    List<Attachment> result = new ArrayList<>(attachmentIds.size());
    
    for (AttachmentId attachmentId : attachmentIds) {
      Attachment attachment = managementAttachmentResourceContainer.get(attachmentId);
      if (attachment != null) {
        result.add(attachment);
      }
    }
    
    return result;
  }

  @Override
  public Attachment findBannerImage(OrganizationId organizationId, BannerId bannerId, AttachmentId attachmentId) {
    if (!identifierRelationController.isChildOf(bannerId, attachmentId)) {
      return null;
    }
    
    return managementAttachmentResourceContainer.get(attachmentId);
  }

  @Override
  public AttachmentData getBannerImageData(OrganizationId organizationId, BannerId bannerId, AttachmentId attachmentId, Integer size) {
    if (!identifierRelationController.isChildOf(bannerId, attachmentId)) {
      return null;
    }

    AttachmentData imageData = getAttachmentData(organizationId, attachmentId);
    if (size != null) {
      return scaleImage(imageData, size);
    } else {
      return imageData;
    }
  }

}
