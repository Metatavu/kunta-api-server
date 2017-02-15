package fi.otavanopisto.kuntaapi.server.integrations;

import java.util.List;

import fi.metatavu.kuntaapi.server.rest.model.Agency;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;

/**
 * Interface that describes a single public transport provider
 * 
 * @author Antti Leppä
 * @author Heikki Kurhinen
 */
public interface PublicTranportProvider {
  
  /**
   * Lists agencies in an organization
   * 
   * @param organizationId organization id
   * @return organization agencies
   */
  public List<Agency> listAgencies(OrganizationId organizationId);
  
}