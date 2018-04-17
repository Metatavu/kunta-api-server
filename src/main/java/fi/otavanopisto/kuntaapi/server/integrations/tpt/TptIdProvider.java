package fi.otavanopisto.kuntaapi.server.integrations.tpt;

import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.lang3.ArrayUtils;

import fi.otavanopisto.kuntaapi.server.id.IdType;
import fi.otavanopisto.kuntaapi.server.integrations.AbstractIdProvider;

/**
 * Id provider for Kuntarekry
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class TptIdProvider extends AbstractIdProvider {

  private static final IdType[] SUPPORTED_TYPES = new IdType[] {
    IdType.JOB
  };
  
  @Override
  public String getSource() {
    return TptConsts.IDENTIFIER_NAME;
  }

  @Override
  public boolean isSupportedType(IdType type) {
    return ArrayUtils.contains(SUPPORTED_TYPES, type);
  }
  
}
