package fi.otavanopisto.kuntaapi.server.integrations.management.tasks;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.id.IdType;
import fi.otavanopisto.kuntaapi.server.id.IncidentId;
import fi.otavanopisto.kuntaapi.server.integrations.management.ManagementConsts;
import fi.otavanopisto.kuntaapi.server.tasks.AbstractIdTaskQueue;

@ApplicationScoped
public class IncidentIdTaskQueue extends AbstractIdTaskQueue<IncidentId> {

  @Override
  public String getSource() {
    return ManagementConsts.IDENTIFIER_NAME;
  }
  
  @Override
  public IdType getType() {
    return IdType.INCIDENT;
  }
  
}