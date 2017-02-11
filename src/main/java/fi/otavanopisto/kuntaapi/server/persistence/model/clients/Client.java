package fi.otavanopisto.kuntaapi.server.persistence.model.clients;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * JPA entity representing an API client application
 * 
 * @author Antti Leppä
 */
@Entity
@Cacheable(true)
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
public class Client {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @NotNull
  @NotEmpty
  @Column (nullable = false)
  private String name;
  
  @NotNull
  @NotEmpty
  @Column (nullable = false, unique = true)
  private String clientId;
  
  @NotNull
  @NotEmpty
  @Column (nullable = false)
  private String clientSecret;
  
  @Column (nullable = false)
  @Enumerated (EnumType.STRING)
  private ClientAccessType accessType;   
  
}
