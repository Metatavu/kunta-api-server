package fi.otavanopisto.kuntaapi.server.integrations.ptv;

import javax.enterprise.context.Dependent;

import org.apache.commons.lang3.ArrayUtils;

import fi.otavanopisto.kuntaapi.server.id.IdType;
import fi.otavanopisto.kuntaapi.server.integrations.AbstractIdProvider;

/**
 * Id provider for palvelu tieto varanto
 * 
 * @author Otavan Opisto
 */
@Dependent
public class PtvIdProvider extends AbstractIdProvider {

  private static final IdType[] SUPPORTED_TYPES = new IdType[] {
    IdType.ORGANIZATION, 
    IdType.SERVICE,
    IdType.ELECTRONIC_SERVICE_CHANNEL,
    IdType.PHONE_CHANNEL, 
    IdType.PRINTABLE_FORM_CHANNEL, 
    IdType.SERVICE_LOCATION_CHANNEL, 
    IdType.WEBPAGE_CHANNEL, 
    IdType.ORGANIZATION_SERVICE
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