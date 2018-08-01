package fi.metatavu.kuntaapi.server.integrations.ptv.resources;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.rest.model.Code;
import fi.metatavu.kuntaapi.server.id.CodeId;
import fi.metatavu.kuntaapi.server.resources.AbstractResourceContainer;

@ApplicationScoped
public class PtvCodeResourceContainer extends AbstractResourceContainer<CodeId, Code> {

  private static final long serialVersionUID = -4989278189579417978L;

  @Override
  public String getName() {
    return "ptv-code";
  }

  @Override
  public String getEntityType() {
    return "resource";
  }

}
