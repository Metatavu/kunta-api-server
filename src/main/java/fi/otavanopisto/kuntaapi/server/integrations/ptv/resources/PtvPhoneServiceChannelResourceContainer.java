package fi.otavanopisto.kuntaapi.server.integrations.ptv.resources;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.resources.AbstractPhoneServiceChannelResourceContainer;

@ApplicationScoped
public class PtvPhoneServiceChannelResourceContainer extends AbstractPhoneServiceChannelResourceContainer {

  private static final long serialVersionUID = -6791960916772451642L;

  @Override
  public String getName() {
    return "ptv-phone-service-channels";
  }

}