package fi.metatavu.kuntaapi.server.integrations.mikkelinyt;

import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.lang3.ArrayUtils;

import fi.metatavu.kuntaapi.server.id.IdType;
import fi.metatavu.kuntaapi.server.integrations.AbstractIdProvider;

/**
 * Id provider for Mikkeli Nyt
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class MikkeliNytIdProvider extends AbstractIdProvider {

  private static final IdType[] SUPPORTED_TYPES = new IdType[] {
    IdType.EVENT, 
    IdType.ATTACHMENT,
  };
  
  @Override
  public String getSource() {
    return MikkeliNytConsts.IDENTIFIER_NAME;
  }

  @Override
  public boolean isSupportedType(IdType type) {
    return ArrayUtils.contains(SUPPORTED_TYPES, type);
  }
  
}
