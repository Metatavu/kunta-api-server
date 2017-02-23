package fi.otavanopisto.kuntaapi.server.integrations.casem.tasks;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.integrations.casem.CaseMConsts;
import fi.otavanopisto.kuntaapi.server.tasks.AbstractOrganizationEntityTaskQueue;

@ApplicationScoped
public class OrganizationNodesTaskQueue extends AbstractOrganizationEntityTaskQueue {

  @Override
  public String getSource() {
    return CaseMConsts.IDENTIFIER_NAME;
  }

  @Override
  public String getEntity() {
    return "nodes";
  }
  
}