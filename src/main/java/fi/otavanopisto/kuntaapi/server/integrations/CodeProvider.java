package fi.otavanopisto.kuntaapi.server.integrations;

import fi.metatavu.kuntaapi.server.rest.model.Code;
import fi.otavanopisto.kuntaapi.server.id.CodeId;

/**
 * Interface that describes a single code provider
 * 
 * @author Antti Leppä
 */
public interface CodeProvider {
  
  /**
   * Finds a code
   * 
   * @param codeId
   * @return list of codes
   */
  public Code findCode(CodeId codeId);
  
}