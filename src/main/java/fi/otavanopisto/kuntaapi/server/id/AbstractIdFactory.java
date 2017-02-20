package fi.otavanopisto.kuntaapi.server.id;

import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

public abstract class AbstractIdFactory {

  @Inject
  private Logger logger;
  
  public <T extends BaseId> T createKuntaApiId(Class<T> idClass, OrganizationId organizationId, Identifier identifier) {
    return createId(idClass, organizationId, KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
  }
  
  protected <T extends BaseId> T createId(Class<T> idClass, OrganizationId organizationId, String source, String id) {
    Constructor<T> idConstructor;
    try {
      idConstructor = idClass.getDeclaredConstructor(OrganizationId.class, String.class, String.class);
      return idConstructor.newInstance(organizationId, source, id);
    } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
      logger.log(Level.SEVERE, "Failed to construct id", e);
    }
    
    return null;
  }
  
}
