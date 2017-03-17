package fi.otavanopisto.kuntaapi.server.integrations.ptv;

import fi.otavanopisto.kuntaapi.server.resources.AbstractServiceResourceContainer;

public class PtvServiceCache extends AbstractServiceResourceContainer {

  private static final long serialVersionUID = 7520071744540725295L;

  @Override
  public String getName() {
    return "ptv-services";
  }

}
