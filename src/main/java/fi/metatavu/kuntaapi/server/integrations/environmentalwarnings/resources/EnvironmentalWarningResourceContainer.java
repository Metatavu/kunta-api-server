package fi.metatavu.kuntaapi.server.integrations.environmentalwarnings.resources;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.id.EnvironmentalWarningId;
import fi.metatavu.kuntaapi.server.resources.AbstractResourceContainer;
import fi.metatavu.kuntaapi.server.rest.model.EnvironmentalWarning;

/**
 * Resource container for environmental warnings
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class EnvironmentalWarningResourceContainer extends AbstractResourceContainer<EnvironmentalWarningId, EnvironmentalWarning> {
  
  private static final long serialVersionUID = -3854105422591182571L;

  @Override
  public String getEntityType() {
    return "resource";
  }

  @Override
  public String getName() {
    return "environmentalwarnings";
  }

}
