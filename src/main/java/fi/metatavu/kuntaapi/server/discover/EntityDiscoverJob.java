package fi.metatavu.kuntaapi.server.discover;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.apache.commons.codec.digest.DigestUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.metatavu.kuntaapi.server.tasks.ExecutableJob;
import fi.metatavu.metaflow.tasks.Task;

public abstract class EntityDiscoverJob <T extends Task> extends AbstractDiscoverJob implements ExecutableJob<T> {

  @Inject
  private Logger logger;

  @Override
  public long getTestModeTimerInterval() {
    return 200l;
  }

  @Override
  public long getTestModeTimerWarmup() {
    return 200l;
  }

  @Override
  public String getSettingPrefix() {
    return "entity-updater";
  }

  protected String createPojoHash(Object entity) {
    try {
      return DigestUtils.md5Hex(new ObjectMapper().writeValueAsBytes(entity));
    } catch (JsonProcessingException e) {
      logger.log(Level.SEVERE, "Failed to create hash", e);
    }
    
    return null;
  }
  
}
