package fi.metatavu.kuntaapi.server.integrations.kuntarekry.tasks;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.integrations.KuntaApiConsts;
import fi.metatavu.kuntaapi.server.tasks.AbstractOrganizationEntityTaskQueue;

@ApplicationScoped
public class OrganizationJobsTaskQueue extends AbstractOrganizationEntityTaskQueue {

  @Override
  public String getSource() {
    return KuntaApiConsts.IDENTIFIER_NAME;
  }

  @Override
  public String getEntity() {
    return "jobs";
  }
  
}