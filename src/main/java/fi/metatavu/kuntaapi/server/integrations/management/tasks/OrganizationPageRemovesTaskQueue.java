package fi.metatavu.kuntaapi.server.integrations.management.tasks;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.integrations.management.ManagementConsts;
import fi.metatavu.kuntaapi.server.tasks.metaflow.AbstractOrganizationEntityTaskQueue;

@ApplicationScoped
public class OrganizationPageRemovesTaskQueue extends AbstractOrganizationEntityTaskQueue {

  @Override
  public String getSource() {
    return ManagementConsts.IDENTIFIER_NAME;
  }

  @Override
  public String getEntity() {
    return "page-removes";
  }
  
}