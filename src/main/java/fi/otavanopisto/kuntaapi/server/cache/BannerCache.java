package fi.otavanopisto.kuntaapi.server.cache;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.id.BannerId;
import fi.otavanopisto.kuntaapi.server.rest.model.Banner;

@ApplicationScoped
public class BannerCache extends AbstractEntityCache<BannerId, Banner> {

  private static final long serialVersionUID = 6513524099128842893L;

  @Override
  public String getCacheName() {
    return "banners";
  }
  
}
