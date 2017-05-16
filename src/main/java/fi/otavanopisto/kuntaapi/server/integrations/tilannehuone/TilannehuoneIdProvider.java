package fi.otavanopisto.kuntaapi.server.integrations.tilannehuone;

import org.apache.commons.lang3.ArrayUtils;

import fi.otavanopisto.kuntaapi.server.id.IdType;
import fi.otavanopisto.kuntaapi.server.integrations.AbstractIdProvider;
import javax.enterprise.context.ApplicationScoped;

/**
 * Id provider for Tilannehuone integration
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class TilannehuoneIdProvider extends AbstractIdProvider {

  private static final IdType[] SUPPORTED_TYPES = new IdType[] {
    IdType.EMERGENCY
  };
  
  @Override
  public String getSource() {
    return TilannehuoneConsts.IDENTIFIER_NAME;
  }

  @Override
  public boolean isSupportedType(IdType type) {
    return ArrayUtils.contains(SUPPORTED_TYPES, type);
  }
  
}
