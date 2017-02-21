package fi.otavanopisto.kuntaapi.server.integrations.mikkelinyt.tasks;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.integrations.mikkelinyt.MikkeliNytConsts;
import fi.otavanopisto.kuntaapi.server.tasks.AbstractOrganizationEntityTaskQueue;

@ApplicationScoped
public class OrganizationEventsTaskQueue extends AbstractOrganizationEntityTaskQueue {

  @Override
  public String getSource() {
    return MikkeliNytConsts.IDENTIFIER_NAME;
  }

  @Override
  public String getEntity() {
    return "events";
  }
  
}