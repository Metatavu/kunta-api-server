package fi.otavanopisto.kuntaapi.server.integrations.ptv;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.metatavu.kuntaapi.server.rest.model.Code;
import fi.otavanopisto.kuntaapi.server.id.CodeId;
import fi.otavanopisto.kuntaapi.server.integrations.CodeProvider;
import fi.otavanopisto.kuntaapi.server.integrations.ptv.resources.PtvCodeResourceContainer;

/**
 * PTV code provider
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class PtvCodeProvider implements CodeProvider {
  
  @Inject
  private PtvCodeResourceContainer codeResourceContainer;

  @Override
  public Code findCode(CodeId codeId) {
    return codeResourceContainer.get(codeId);
  }

}
