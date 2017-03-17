package fi.otavanopisto.kuntaapi.server.integrations.management.resources;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.resources.AbstractPageResourceContainer;

@ApplicationScoped
public class ManagementPageResourceContainer extends AbstractPageResourceContainer {

  private static final long serialVersionUID = 3226908290673289499L;

  @Override
  public String getName() {
    return "management-pages";
  }

}
