package fi.metatavu.kuntaapi.server.integrations.ptv.resources;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.resources.AbstractPrintableFormServiceChannelResourceContainer;

@ApplicationScoped
public class PtvPrintableFormServiceChannelResourceContainer extends AbstractPrintableFormServiceChannelResourceContainer {

  private static final long serialVersionUID = -4729455384031092985L;

  @Override
  public String getName() {
    return "ptv-printable-form-service-channels";
  }

}