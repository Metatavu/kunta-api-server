package fi.metatavu.kuntaapi.server.integrations.ptv.resources;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.resources.AbstractElectronicServiceChannelResourceContainer;

@ApplicationScoped
public class PtvElectronicServiceChannelResourceContainer extends AbstractElectronicServiceChannelResourceContainer {

  private static final long serialVersionUID = -4729455384031092985L;

  @Override
  public String getName() {
    return "ptv-electronic-service-channels";
  }

}