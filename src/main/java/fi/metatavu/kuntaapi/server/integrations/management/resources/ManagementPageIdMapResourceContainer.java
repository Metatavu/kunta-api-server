package fi.metatavu.kuntaapi.server.integrations.management.resources;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.resources.AbstractPageIdMapCache;

@ApplicationScoped
public class ManagementPageIdMapResourceContainer extends AbstractPageIdMapCache {

  private static final long serialVersionUID = 6300784323817741857L;

  @Override
  public String getName() {
    return "management-page-map-cache";
  }

}
