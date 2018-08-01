package fi.metatavu.kuntaapi.server.resources;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.rest.model.Schedule;
import fi.metatavu.kuntaapi.server.id.PublicTransportScheduleId;

@ApplicationScoped
public abstract class AbstractPublicTransportScheduleResourceContainer extends AbstractResourceContainer<PublicTransportScheduleId, Schedule> {

  private static final long serialVersionUID = 522366384310662042L;

  @Override
  public String getEntityType() {
    return "resource";
  }
  
}