package fi.otavanopisto.kuntaapi.server.persistence.model.clients;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;

/**
 * JPA entity representing an authorized organization for a API client
 * 
 * @author Antti Leppä
 */
@Entity
@Cacheable(true)
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
public class ClientOrganization {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @ManyToOne
  private Client client;
  
  @ManyToOne
  private Identifier organizationIdentifier;
  
}
