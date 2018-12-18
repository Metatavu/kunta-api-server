package fi.metatavu.kuntaapi.server.integrations.ptv.tasks;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.integrations.ptv.PtvConsts;
import fi.metatavu.kuntaapi.server.tasks.metaflow.AbstractOrganizationEntityTaskQueue;

@ApplicationScoped
public class OrganizationServicesTaskQueue extends AbstractOrganizationEntityTaskQueue {

  @Override
  public String getSource() {
    return PtvConsts.IDENTIFIER_NAME;
  }

  @Override
  public String getEntity() {
    return "organization-services";
  }
  
}