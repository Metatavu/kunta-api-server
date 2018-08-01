package fi.metatavu.kuntaapi.server.integrations.management.tasks;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.integrations.management.ManagementConsts;
import fi.metatavu.kuntaapi.server.tasks.AbstractOrganizationEntityTaskQueue;

@ApplicationScoped
public class OrganizationFragmentsTaskQueue extends AbstractOrganizationEntityTaskQueue {

  @Override
  public String getSource() {
    return ManagementConsts.IDENTIFIER_NAME;
  }

  @Override
  public String getEntity() {
    return "fragments";
  }
  
}