package fi.otavanopisto.kuntaapi.server.integrations.ptv.resources;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.resources.AbstractWebPageServiceChannelResourceContainer;

@ApplicationScoped
public class PtvWebPageServiceChannelResourceContainer extends AbstractWebPageServiceChannelResourceContainer {

  private static final long serialVersionUID = -4729455384031092985L;

  @Override
  public String getName() {
    return "ptv-web-page-service-channels";
  }

}