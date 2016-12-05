package fi.otavanopisto.kuntaapi.server.controllers;

import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.id.BaseId;
import fi.otavanopisto.kuntaapi.server.id.IdType;
import fi.otavanopisto.kuntaapi.server.id.MissingOrganizationIdException;
import fi.otavanopisto.kuntaapi.server.id.OrganizationBaseId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.persistence.dao.IdentifierDAO;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;

/**
 * Identifier controller
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class IdentifierController {
  
  @Inject
  private IdentifierDAO identifierDAO;
  
  /**
   * Creates new identifier.
   * 
   * @param id identifier
   * @return created identifier
   */
  public Identifier createIdentifier(BaseId id) {
    String organizationKuntaApiId = null;
    if (id instanceof OrganizationBaseId) {
      OrganizationBaseId organizationBaseId = (OrganizationBaseId) id;
      if (organizationBaseId.getOrganizationId() == null) {
        throw new MissingOrganizationIdException("Attempted to create organizationBaseId without organization");
      }

      organizationKuntaApiId = getOrganizationKuntaApiId(organizationBaseId);
      if (organizationKuntaApiId == null) {
        throw new MissingOrganizationIdException(String.format("Could not find organiztion %s for id %s", organizationBaseId.getOrganizationId().toString(), organizationBaseId.toString()));
      }
    }
    
    String kuntaApiId = UUID.randomUUID().toString();
    return createIdentifier(id.getType().toString(), kuntaApiId, id.getSource(), id.getId(), organizationKuntaApiId);
  }
  
  public Identifier findIdentifierById(BaseId id) {
    String organizationKuntaApiId = null;
    if (id instanceof OrganizationBaseId) {
      organizationKuntaApiId = getOrganizationKuntaApiId((OrganizationBaseId) id);
    }
    
    return findIdentifierByTypeSourceAndIdOrganizationId(id.getType(), id.getSource(), id.getId(), organizationKuntaApiId);
  }

  public Identifier findIdentifierByTypeSourceAndKuntaApiId(String type, String source, String kuntaApiId) {
    return identifierDAO.findByTypeSourceAndKuntaApiId(type, source, kuntaApiId);
  }

  public Identifier findIdentifierByTypeSourceAndKuntaApiId(IdType type, String source, String kuntaApiId) {
    return findIdentifierByTypeSourceAndKuntaApiId(type.toString(), source, kuntaApiId);
  }

  public void deleteIdentifier(Identifier identifier) {
    identifierDAO.delete(identifier);
  }

  private Identifier createIdentifier(String type, String kuntaApiId, String source, String sourceId, String organizationKuntaApiId) {
    return identifierDAO.create(type, kuntaApiId, source, sourceId, organizationKuntaApiId);
  }

  private Identifier findIdentifierByTypeSourceIdAndOrganizationId(String type, String source, String sourceId, String organizationKuntaApiId) {
    return identifierDAO.findByTypeSourceSourceIdAndOrganizationKuntaApiId(type, source, sourceId, organizationKuntaApiId);
  }

  private Identifier findIdentifierByTypeSourceAndIdOrganizationId(IdType type, String source, String sourceId, String organizationKuntaApiId) {
    return findIdentifierByTypeSourceIdAndOrganizationId(type.toString(), source, sourceId, organizationKuntaApiId);
  }
  
  private String getOrganizationKuntaApiId(OrganizationBaseId organizationBaseId) {
    OrganizationId organizationId = organizationBaseId.getOrganizationId();
    if (KuntaApiConsts.IDENTIFIER_NAME.equals(organizationId.getSource())) {
      return organizationId.getId();
    }
    
    Identifier organizationIdentifier = findIdentifierByTypeSourceAndIdOrganizationId(organizationId.getType(), organizationId.getSource(), organizationId.getId(), null);
    if (organizationIdentifier != null) {
      return organizationIdentifier.getKuntaApiId();
    }
    
    return null;
  }
}
