package fi.otavanopisto.kuntaapi.server.integrations.linkedevents;

import javax.enterprise.context.ApplicationScoped;

import org.apache.commons.lang3.ArrayUtils;

import fi.otavanopisto.kuntaapi.server.id.IdType;
import fi.otavanopisto.kuntaapi.server.integrations.AbstractIdProvider;

/**
 * Id provider for linked Events
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class LinkedEventsIdProvider extends AbstractIdProvider {

  private static final IdType[] SUPPORTED_TYPES = new IdType[] {
    IdType.EVENT, 
    IdType.ATTACHMENT,
  };
  
  @Override
  public String getSource() {
    return LinkedEventsConsts.IDENTIFIER_NAME;
  }

  @Override
  public boolean isSupportedType(IdType type) {
    return ArrayUtils.contains(SUPPORTED_TYPES, type);
  }
  
}
