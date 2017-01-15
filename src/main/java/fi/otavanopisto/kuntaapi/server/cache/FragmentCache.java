package fi.otavanopisto.kuntaapi.server.cache;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.id.FragmentId;
import fi.metatavu.kuntaapi.server.rest.model.Fragment;

@ApplicationScoped
public class FragmentCache extends AbstractEntityCache<FragmentId, Fragment> {

  private static final long serialVersionUID = -9054454730058146125L;

  @Override
  public String getCacheName() {
    return "fragments";
  }
  
}