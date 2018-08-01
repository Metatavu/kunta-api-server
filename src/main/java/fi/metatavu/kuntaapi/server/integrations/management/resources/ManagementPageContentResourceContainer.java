package fi.metatavu.kuntaapi.server.integrations.management.resources;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.resources.AbstractPageContentCache;

@ApplicationScoped
public class ManagementPageContentResourceContainer extends AbstractPageContentCache {

  private static final long serialVersionUID = 4103968741744421851L;

  @Override
  public String getName() {
    return "management-page-contents";
  }

}
