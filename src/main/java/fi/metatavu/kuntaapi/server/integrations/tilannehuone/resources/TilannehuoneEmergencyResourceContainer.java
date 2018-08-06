package fi.metatavu.kuntaapi.server.integrations.tilannehuone.resources;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.resources.AbstractEmergencyResourceContainer;

@ApplicationScoped
public class TilannehuoneEmergencyResourceContainer extends AbstractEmergencyResourceContainer {

  private static final long serialVersionUID = -5337250271045778494L;

  @Override
  public String getName() {
    return "tilannehuone-emergencies";
  }

}