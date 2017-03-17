package fi.otavanopisto.kuntaapi.server.cache;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.rest.model.Schedule;
import fi.otavanopisto.kuntaapi.server.id.PublicTransportScheduleId;
import fi.otavanopisto.kuntaapi.server.resources.AbstractResourceContainer;

@ApplicationScoped
public abstract class AbstractPublicTransportScheduleCache extends AbstractResourceContainer<PublicTransportScheduleId, Schedule> {

  private static final long serialVersionUID = 522366384310662042L;

  @Override
  public String getEntityType() {
    return "resource";
  }
  
}