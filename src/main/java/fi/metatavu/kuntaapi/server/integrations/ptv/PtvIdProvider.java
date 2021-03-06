package fi.metatavu.kuntaapi.server.integrations.ptv;

import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.lang3.ArrayUtils;

import fi.metatavu.kuntaapi.server.id.IdType;
import fi.metatavu.kuntaapi.server.integrations.AbstractIdProvider;

/**
 * Id provider for palvelu tieto varanto
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class PtvIdProvider extends AbstractIdProvider {

  private static final IdType[] SUPPORTED_TYPES = new IdType[] {
    IdType.ORGANIZATION, 
    IdType.SERVICE,
    IdType.ELECTRONIC_SERVICE_CHANNEL,
    IdType.PHONE_SERVICE_CHANNEL, 
    IdType.PRINTABLE_FORM_SERVICE_CHANNEL, 
    IdType.SERVICE_LOCATION_SERVICE_CHANNEL, 
    IdType.WEBPAGE_SERVICE_CHANNEL
  };
  
  @Override
  public String getSource() {
    return PtvConsts.IDENTIFIER_NAME;
  }

  @Override
  public boolean isSupportedType(IdType type) {
    return ArrayUtils.contains(SUPPORTED_TYPES, type);
  }
  
}