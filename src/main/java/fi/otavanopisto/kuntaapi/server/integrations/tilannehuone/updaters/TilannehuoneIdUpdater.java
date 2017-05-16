package fi.otavanopisto.kuntaapi.server.integrations.tilannehuone.updaters;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import fi.otavanopisto.kuntaapi.server.discover.IdUpdater;
import fi.otavanopisto.kuntaapi.server.integrations.tilannehuone.TilannehuoneConsts;
import fi.otavanopisto.kuntaapi.server.integrations.tilannehuone.model.Emergency;
import fi.otavanopisto.kuntaapi.server.integrations.tilannehuone.tasks.TilannehuoneEmergencyEntityTask;
import fi.otavanopisto.kuntaapi.server.integrations.tilannehuone.tasks.TilannehuoneEmergencyTaskQueue;
import fi.otavanopisto.kuntaapi.server.settings.SystemSettingController;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class TilannehuoneIdUpdater extends IdUpdater {
  
  @Inject
  private Logger logger;
  
  @Inject
  private TilannehuoneEmergencyTaskQueue tilannehuoneEmergencyTaskQueue;

  @Inject
  private SystemSettingController systemSettingController;
  
  @Override
  public String getName() {
    return "tilannehuone-ids";
  }
  
  @Override
  public void timeout() {
    updateTilannehuoneEntities();
  }
  
  private void updateTilannehuoneEntities() {
    String importFile = systemSettingController.getSettingValue(TilannehuoneConsts.SYSTEM_SETTING_TILANNEHUONE_IMPORT_FILE);
    if (importFile == null) {
      logger.log(Level.WARNING, "Tilannehuone import file not specified");
      return;
    }
    
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    
    try (InputStream fileStream = new FileInputStream(importFile)) {
      List<Emergency> tilannehuoneEmergencies = objectMapper.readValue(fileStream, new TypeReference<List<Emergency>>() {});
      if (tilannehuoneEmergencies != null) {
        long orderIndex = System.currentTimeMillis();
        for (Emergency tilannehuoneEmergency : tilannehuoneEmergencies) {
          tilannehuoneEmergencyTaskQueue.enqueueTask(false, new TilannehuoneEmergencyEntityTask(tilannehuoneEmergency, orderIndex));
          orderIndex++;
        }
      }
    } catch (FileNotFoundException e) {
      logger.log(Level.SEVERE, "Failed to read tilannehuone import file", e);
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Failed to parse tilannehuone import file", e);
    }
  }
  
}