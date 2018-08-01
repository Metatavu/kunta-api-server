package fi.metatavu.kuntaapi.server.persistence.model;

import java.time.OffsetDateTime;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * JPA entity for storing organization wide settings
 * 
 * @author Antti Leppä
 */
@Entity
@Cacheable(true)
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "organizationKuntaApiId", "tokenType" }) })
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
public class OrganizationExternalAccessToken {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, name = "tokenType")
  @NotNull
  @NotEmpty
  private String tokenType;

  @Column(nullable = false)
  @NotNull
  @NotEmpty
  @Lob
  private String accessToken;

  @Column(nullable = false)
  @NotNull
  @NotEmpty
  private String organizationKuntaApiId;

  private OffsetDateTime expires;

  public Long getId() {
    return id;
  }

  public String getTokenType() {
    return tokenType;
  }

  public void setTokenType(String tokenType) {
    this.tokenType = tokenType;
  }

  public String getAccessToken() {
    return accessToken;
  }

  public void setAccessToken(String accessToken) {
    this.accessToken = accessToken;
  }

  public String getOrganizationKuntaApiId() {
    return organizationKuntaApiId;
  }

  public void setOrganizationKuntaApiId(String organizationKuntaApiId) {
    this.organizationKuntaApiId = organizationKuntaApiId;
  }

  public OffsetDateTime getExpires() {
    return expires;
  }

  public void setExpires(OffsetDateTime expires) {
    this.expires = expires;
  }

  public void setId(Long id) {
    this.id = id;
  }

}
