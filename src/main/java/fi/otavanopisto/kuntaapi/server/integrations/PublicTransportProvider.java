package fi.otavanopisto.kuntaapi.server.integrations;

import java.util.List;

import fi.metatavu.kuntaapi.server.rest.model.Agency;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PublicTransportAgencyId;

/**
 * Interface that describes a single public transport provider
 * 
 * @author Antti Leppä
 * @author Heikki Kurhinen
 */
public interface PublicTransportProvider {
  
  /**
   * Lists agencies in an organization
   * 
   * @param organizationId organization id
   * @return organization agencies
   */
  public List<Agency> listAgencies(OrganizationId organizationId);
  
  
  /**
   * Finds single agency in an organization by id
   * 
   * @param organizationId organization id
   * @param agencyId agency id
   * @return agency or null if not found
   */
  public Agency findAgency(OrganizationId organizationId, PublicTransportAgencyId agencyId);
  
}