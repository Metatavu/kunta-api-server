package fi.metatavu.kuntaapi.server.integrations.ptv.tasks;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.id.IdType;
import fi.metatavu.kuntaapi.server.integrations.ptv.PtvConsts;
import fi.metatavu.kuntaapi.server.tasks.metaflow.AbstractIdTaskQueue;

@ApplicationScoped
public class OrganizationIdTaskQueue extends AbstractIdTaskQueue<OrganizationId> {

  @Override
  public String getSource() {
    return PtvConsts.IDENTIFIER_NAME;
  }
  
  @Override
  public IdType getType() {
    return IdType.ORGANIZATION;
  }
  
}