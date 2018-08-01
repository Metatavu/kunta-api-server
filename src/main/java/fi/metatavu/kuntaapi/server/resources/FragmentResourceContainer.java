package fi.metatavu.kuntaapi.server.resources;

import javax.enterprise.context.ApplicationScoped;

import fi.metatavu.kuntaapi.server.id.FragmentId;
import fi.metatavu.kuntaapi.server.rest.model.Fragment;

@ApplicationScoped
public class FragmentResourceContainer extends AbstractResourceContainer<FragmentId, Fragment> {

  private static final long serialVersionUID = -9054454730058146125L;

  @Override
  public String getName() {
    return "fragments";
  }
  
  @Override
  public String getEntityType() {
    return "resource";
  }

}