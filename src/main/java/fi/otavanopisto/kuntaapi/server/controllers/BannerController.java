package fi.otavanopisto.kuntaapi.server.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.id.AttachmentId;
import fi.otavanopisto.kuntaapi.server.id.BannerId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.AttachmentData;
import fi.otavanopisto.kuntaapi.server.integrations.BannerProvider;
import fi.otavanopisto.kuntaapi.server.rest.model.Attachment;
import fi.otavanopisto.kuntaapi.server.rest.model.Banner;

@ApplicationScoped
public class BannerController {
  
  @Inject
  private Instance<BannerProvider> bannerProviders;
  
  public List<Banner> listBanners(OrganizationId organizationId) {
    List<Banner> result = new ArrayList<>();
   
    for (BannerProvider bannerProvider : getBannerProviders()) {
      result.addAll(bannerProvider.listOrganizationBanners(organizationId));
    }
    return result;
  }

  public Banner findBanner(OrganizationId organizationId, BannerId bannerId) {
    for (BannerProvider bannerProvider : getBannerProviders()) {
      Banner banner = bannerProvider.findOrganizationBanner(organizationId, bannerId);
      if (banner != null) {
        return banner;
      }
    }
    
    return null;
  }

  public List<Attachment> listBannerImages(OrganizationId organizationId, BannerId bannerId) {
    List<Attachment> result = new ArrayList<>();
   
    for (BannerProvider bannerProvider : getBannerProviders()) {
      result.addAll(bannerProvider.listOrganizationBannerImages(organizationId, bannerId));
    }
    return result;
  }

  public Attachment findBannerImage(OrganizationId organizationId, BannerId bannerId, AttachmentId attachmentId) {
    for (BannerProvider bannerProvider : getBannerProviders()) {
      Attachment attachment = bannerProvider.findBannerImage(organizationId, bannerId, attachmentId);
      if (attachment != null) {
        return attachment;
      }
    }
    
    return null;
  }
  
  public AttachmentData getBannerImageData(OrganizationId organizationId, BannerId bannerId, AttachmentId attachmentId, Integer size) {
    for (BannerProvider bannerProvider : getBannerProviders()) {
      AttachmentData attachmentData = bannerProvider.getBannerImageData(organizationId, bannerId, attachmentId, size);
      if (attachmentData != null) {
        return attachmentData;
      }
    }
    
    return null;
  }
  
  private List<BannerProvider> getBannerProviders() {
    List<BannerProvider> result = new ArrayList<>();
    
    Iterator<BannerProvider> iterator = bannerProviders.iterator();
    while (iterator.hasNext()) {
      result.add(iterator.next());
    }
    
    return Collections.unmodifiableList(result);
  }
  
}
