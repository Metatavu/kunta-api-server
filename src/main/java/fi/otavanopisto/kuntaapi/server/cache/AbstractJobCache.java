package fi.otavanopisto.kuntaapi.server.cache;

import fi.metatavu.kuntaapi.server.rest.model.Job;
import fi.otavanopisto.kuntaapi.server.id.JobId;

public abstract class AbstractJobCache extends AbstractEntityCache<JobId, Job> {

  private static final long serialVersionUID = -6572380742687259874L;

  @Override
  public String getEntityType() {
    return "resource";
  }
   
}