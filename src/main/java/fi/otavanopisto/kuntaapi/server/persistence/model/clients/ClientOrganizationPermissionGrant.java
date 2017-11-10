package fi.otavanopisto.kuntaapi.server.persistence.model.clients;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;

/**
 * JPA entity representing a granted permission for client into an organization
 * 
 * @author Antti Leppä
 */
@Entity
@Cacheable(true)
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
public class ClientOrganizationPermissionGrant {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @ManyToOne
  private Client client;
  
  @ManyToOne
  private Identifier organizationIdentifier;
  
  @Enumerated (EnumType.STRING)
  @Column (nullable = false)
  @NotNull
  private ClientOrganizationPermission permission;
  
  /**
   * Returns entity id
   * 
   * @return entity id
   */
  public Long getId() {
    return id;
  }
  
  /**
   * Sets entity id
   * 
   * @param id id
   */
  public void setId(Long id) {
    this.id = id;
  }
  
  /**
   * Returns client
   * 
   * @return client
   */
  public Client getClient() {
    return client;
  }
  
  /**
   * Sets client
   * 
   * @param client client
   */
  public void setClient(Client client) {
    this.client = client;
  }
  
  /**
   * Returns an organization identifier
   * 
   * @return organization identifier
   */
  public Identifier getOrganizationIdentifier() {
    return organizationIdentifier;
  }
  
  /**
   * Sets an organization identifier
   * 
   * @param organizationIdentifier organization identifier
   */
  public void setOrganizationIdentifier(Identifier organizationIdentifier) {
    this.organizationIdentifier = organizationIdentifier;
  }
  
  /**
   * Returns a permission that is granted to the client
   * 
   * @return permission that is granted to the client
   */
  public ClientOrganizationPermission getPermission() {
    return permission;
  }
  
  /**
   * Sets a permission that is granted to the client
   * 
   * @param permission permission that is granted to the client
   */
  public void setPermission(ClientOrganizationPermission permission) {
    this.permission = permission;
  }
  
}
