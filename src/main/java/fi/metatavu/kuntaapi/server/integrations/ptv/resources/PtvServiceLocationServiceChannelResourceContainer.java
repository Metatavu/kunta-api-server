package fi.metatavu.kuntaapi.server.integrations.ptv.resources;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.resources.AbstractServiceLocationServiceChannelResourceContainer;

@ApplicationScoped
public class PtvServiceLocationServiceChannelResourceContainer extends AbstractServiceLocationServiceChannelResourceContainer {

  private static final long serialVersionUID = -4729455384031092985L;

  @Override
  public String getName() {
    return "ptv-service-location-service-channels";
  }

}