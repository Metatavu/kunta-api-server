package fi.metatavu.kuntaapi.server.resources;

import fi.metatavu.kuntaapi.server.rest.model.Incident;
import fi.metatavu.kuntaapi.server.id.IncidentId;

public abstract class AbstractIncidentResourceContainer extends AbstractResourceContainer<IncidentId, Incident> {

  private static final long serialVersionUID = -6044907381091939938L;

  @Override
  public String getEntityType() {
    return "resource";
  }
  
}
