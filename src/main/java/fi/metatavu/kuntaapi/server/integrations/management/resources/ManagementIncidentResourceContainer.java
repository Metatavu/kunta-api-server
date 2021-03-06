package fi.metatavu.kuntaapi.server.integrations.management.resources;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.resources.AbstractIncidentResourceContainer;

@ApplicationScoped
public class ManagementIncidentResourceContainer extends AbstractIncidentResourceContainer {

  private static final long serialVersionUID = 1008548316608174593L;

  @Override
  public String getName() {
    return "management-incidents";
  }

}
