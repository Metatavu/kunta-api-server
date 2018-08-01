package fi.metatavu.kuntaapi.server.integrations.gtfs.tasks;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.integrations.ptv.PtvConsts;
import fi.metatavu.kuntaapi.server.tasks.AbstractOrganizationEntityTaskQueue;

@ApplicationScoped
public class OrganizationGtfsTaskQueue extends AbstractOrganizationEntityTaskQueue {

  @Override
  public String getSource() {
    return PtvConsts.IDENTIFIER_NAME;
  }

  @Override
  public String getEntity() {
    return "gtfs";
  }
  
}