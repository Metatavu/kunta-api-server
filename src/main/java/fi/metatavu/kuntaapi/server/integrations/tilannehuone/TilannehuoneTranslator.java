package fi.metatavu.kuntaapi.server.integrations.tilannehuone;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.rest.model.Emergency;
import fi.metatavu.kuntaapi.server.rest.model.EmergencySource;
import fi.metatavu.kuntaapi.server.id.EmergencyId;

@ApplicationScoped
public class TilannehuoneTranslator {

  public Emergency translateEmergency(EmergencyId kuntaApiEmergencyId, fi.metatavu.kuntaapi.server.integrations.tilannehuone.model.Emergency tilannehuoneEmergency) {
    Emergency result  = new Emergency();
    result.setDescription(tilannehuoneEmergency.getDescription());
    result.setExtent(tilannehuoneEmergency.getExtent());
    result.setId(kuntaApiEmergencyId.getId());
    result.setLatitude(tilannehuoneEmergency.getLatitude());
    result.setLongitude(tilannehuoneEmergency.getLongitude());
    result.setLocation(tilannehuoneEmergency.getLocation());
    result.setSources(translateSources(tilannehuoneEmergency.getSources()));
    result.setTime(tilannehuoneEmergency.getTime());
    result.setType(tilannehuoneEmergency.getType());
    result.setUrl(tilannehuoneEmergency.getUrl());
    return result;
  }

  private List<EmergencySource> translateSources(List<fi.metatavu.kuntaapi.server.integrations.tilannehuone.model.EmergencySource> tilannehuoneSources) {
    if (tilannehuoneSources == null) {
      return Collections.emptyList();
    }
    
    List<EmergencySource> result = new ArrayList<>(tilannehuoneSources.size());
    
    for (fi.metatavu.kuntaapi.server.integrations.tilannehuone.model.EmergencySource tilannehuoneSource : tilannehuoneSources) {
      EmergencySource emergencySource = new EmergencySource();
      emergencySource.setName(tilannehuoneSource.getName());
      emergencySource.setUrl(tilannehuoneSource.getUrl());
      result.add(emergencySource);
    }
    
    return result;
  }
  
}
