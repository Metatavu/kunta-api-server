package fi.otavanopisto.kuntaapi.server.cache;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.id.FragmentId;
import fi.otavanopisto.kuntaapi.server.resources.AbstractResourceContainer;
import fi.metatavu.kuntaapi.server.rest.model.Fragment;

@ApplicationScoped
public class FragmentCache extends AbstractResourceContainer<FragmentId, Fragment> {

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