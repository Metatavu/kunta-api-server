package fi.otavanopisto.kuntaapi.server.integrations.management;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.id.AttachmentId;
import fi.otavanopisto.kuntaapi.server.id.BannerId;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.AttachmentData;
import fi.otavanopisto.kuntaapi.server.integrations.BannerProvider;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
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
  private ManagementImageLoader managementImageLoader;
  
  @Inject
  private ManagementApi managementApi;

  @Inject
  private IdController idController;
  
  @Inject
  private IdentifierController identifierController;

  @Override
  public List<Banner> listOrganizationBanners(OrganizationId organizationId) {
    String context = null;
    Integer page = null;
    Integer perPage = null;
    String search = null;
    LocalDateTime after = null;
    LocalDateTime before = null;
    List<String> exclude = null;
    List<String> include = null;
    Integer offset = null;
    String order = null; 
    String orderby = null;
    String slug = null;
    String status = null;
    String filter = null;

    ApiResponse<List<fi.otavanopisto.mwp.client.model.Banner>> response = managementApi.getApi(organizationId).wpV2BannerGet(context, page, perPage, search, after,
        before, exclude, include, offset, order, orderby, slug, status, filter);

    if (!response.isOk()) {
      logger.severe(String.format("Banner listing failed on [%d] %s", response.getStatus(), response.getMessage()));
    } else {
      return translateBanners(response.getResponse());
    }
    
    return Collections.emptyList();
  }

  @Override
  public Banner findOrganizationBanner(OrganizationId organizationId, BannerId bannerId) {
    fi.otavanopisto.mwp.client.model.Banner managementBanner = findBannerByBannerId(organizationId, bannerId);
    if (managementBanner != null) {
      return translateBanner(managementBanner);
    }
  
    return null;
  }

  @Override
  public List<Attachment> listOrganizationBannerImages(OrganizationId organizationId, BannerId bannerId) {
    fi.otavanopisto.mwp.client.model.Banner managementBanner = findBannerByBannerId(organizationId, bannerId);
    if (managementBanner != null) {
      Integer featuredMediaId = managementBanner.getFeaturedMedia();
      if (featuredMediaId != null) {
        fi.otavanopisto.mwp.client.model.Attachment featuredMedia = findMedia(organizationId, featuredMediaId);
        if ((featuredMedia != null) && (featuredMedia.getMediaType() == MediaTypeEnum.IMAGE)) {
          return Collections.singletonList(translateAttachment(featuredMedia));
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
        AttachmentId managementAttachmentId = getImageAttachmentId(featuredMediaId);
        if (!idController.idsEqual(attachmentId, managementAttachmentId)) {
          return null;
        }
        
        fi.otavanopisto.mwp.client.model.Attachment attachment = findMedia(organizationId, featuredMediaId);
        if (attachment != null) {
          return translateAttachment(attachment);
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

  private List<Banner> translateBanners(List<fi.otavanopisto.mwp.client.model.Banner> managementBanners) {
    List<Banner> result = new ArrayList<>();
    
    for (fi.otavanopisto.mwp.client.model.Banner managementBanner : managementBanners) {
      result.add(translateBanner(managementBanner));
    }
    
    return result;
  }

  private Banner translateBanner(fi.otavanopisto.mwp.client.model.Banner managementBanner) {
    Banner banner = new Banner();
    
    BannerId managementBannerId = new BannerId(ManagementConsts.IDENTIFIER_NAME, String.valueOf(managementBanner.getId()));
    BannerId kuntaApiBannerId = idController.translateBannerId(managementBannerId, KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiBannerId == null) {
      logger.info(String.format("Found new news article %d", managementBanner.getId()));
      Identifier newIdentifier = identifierController.createIdentifier(managementBannerId);
      kuntaApiBannerId = new BannerId(KuntaApiConsts.IDENTIFIER_NAME, newIdentifier.getKuntaApiId());
    }
    
    banner.setContents(managementBanner.getContent().getRendered());
    banner.setId(kuntaApiBannerId.getId());
    banner.setLink(managementBanner.getBannerLink());
    banner.setTitle(managementBanner.getTitle().getRendered());
    
    return banner;
  }

}
