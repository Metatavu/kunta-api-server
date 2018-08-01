package fi.metatavu.kuntaapi.server.integrations.tpt.resources;

import fi.metatavu.kuntaapi.server.resources.AbstractJobResourceContainer;

/**
 * Job resource container for te-palvelut.fi -integration
 * 
 * @author Antti Leppä
 */
public class TptJobResourceContainer extends AbstractJobResourceContainer {

  private static final long serialVersionUID = 2471098011896505702L;

  @Override
  public String getName() {
    return "tpt-jobs";
  }

}
