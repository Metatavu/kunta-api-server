package fi.otavanopisto.kuntaapi.server.integrations.management;

import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.lang3.ArrayUtils;

import fi.otavanopisto.kuntaapi.server.id.IdType;
import fi.otavanopisto.kuntaapi.server.integrations.AbstractIdProvider;

/**
 * Id provider for management Wordpress service
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class ManagementIdProvider extends AbstractIdProvider {

  private static final IdType[] SUPPORTED_TYPES = new IdType[] {
    IdType.NEWS_ARTICLE,
    IdType.ATTACHMENT,
    IdType.BANNER,
    IdType.TILE, 
    IdType.PAGE, 
    IdType.MENU, 
    IdType.FILE, 
    IdType.MENU_ITEM,
    IdType.SHORTLINK,
    IdType.INCIDENT
  };
  
  @Override
  public String getSource() {
    return ManagementConsts.IDENTIFIER_NAME;
  }

  @Override
  public boolean isSupportedType(IdType type) {
    return ArrayUtils.contains(SUPPORTED_TYPES, type);
  }
  
}
