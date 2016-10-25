package fi.otavanopisto.kuntaapi.server.integrations.kuntarekry;

import javax.enterprise.context.Dependent;

import org.apache.commons.lang3.ArrayUtils;

import fi.otavanopisto.kuntaapi.server.id.IdType;
import fi.otavanopisto.kuntaapi.server.integrations.AbstractIdProvider;

/**
 * Id provider for Kuntarekry
 * 
 * @author Antti Leppä
 */
@Dependent
public class KuntaRekryIdProvider extends AbstractIdProvider {

  private static final IdType[] SUPPORTED_TYPES = new IdType[] {
    IdType.JOB
  };
  
  @Override
  public String getSource() {
    return KuntaRekryConsts.IDENTIFIER_NAME;
  }

  @Override
  public boolean isSupportedType(IdType type) {
    return ArrayUtils.contains(SUPPORTED_TYPES, type);
  }
  
}
