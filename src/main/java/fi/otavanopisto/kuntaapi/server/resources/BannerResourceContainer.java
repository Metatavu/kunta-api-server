package fi.otavanopisto.kuntaapi.server.resources;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.id.BannerId;
import fi.metatavu.kuntaapi.server.rest.model.Banner;

@ApplicationScoped
public class BannerResourceContainer extends AbstractResourceContainer<BannerId, Banner> {

  private static final long serialVersionUID = 6513524099128842893L;

  @Override
  public String getName() {
    return "banners";
  }
  
  @Override
  public String getEntityType() {
    return "resource";
  }
  
}
