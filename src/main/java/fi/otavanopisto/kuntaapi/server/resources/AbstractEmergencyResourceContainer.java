package fi.otavanopisto.kuntaapi.server.resources;

import fi.metatavu.kuntaapi.server.rest.model.Emergency;
import fi.otavanopisto.kuntaapi.server.id.EmergencyId;

public abstract class AbstractEmergencyResourceContainer extends AbstractResourceContainer<EmergencyId, Emergency> {

  private static final long serialVersionUID = 8451922816781337156L;

  @Override
  public String getEntityType() {
    return "resource";
  }
  
}
