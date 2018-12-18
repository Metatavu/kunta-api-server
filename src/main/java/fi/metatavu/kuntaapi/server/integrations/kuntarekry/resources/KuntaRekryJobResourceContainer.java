package fi.metatavu.kuntaapi.server.integrations.kuntarekry.resources;

import fi.metatavu.kuntaapi.server.resources.AbstractJobResourceContainer;

public class KuntaRekryJobResourceContainer extends AbstractJobResourceContainer {

  private static final long serialVersionUID = -3909462228089482785L;

  @Override
  public String getName() {
    return "kunta-rekry-jobs";
  }

}
