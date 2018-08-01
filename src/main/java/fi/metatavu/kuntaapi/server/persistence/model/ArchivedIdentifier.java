package fi.metatavu.kuntaapi.server.persistence.model;

import java.time.OffsetDateTime;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * JPA entity representing archived identifier
 * 
 * @author Antti Leppä
 */
@Entity
@Table(uniqueConstraints = { 
  @UniqueConstraint(columnNames = { "organizationKuntaApiId", "type", "source", "sourceId" }),
  @UniqueConstraint(columnNames = { "organizationKuntaApiId", "type", "source", "kuntaApiId" }) 
})
@Cacheable(true)
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
public class ArchivedIdentifier {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @Column(nullable = false)
  @NotNull
  private Long orderIndex;
  
  @Column
  private String organizationKuntaApiId;

  @Column(nullable = false)
  @NotNull
  @NotEmpty
  private String kuntaApiId;

  @Column(nullable = false)
  @NotNull
  @NotEmpty
  private String type;

  @Column(nullable = false)
  @NotNull
  @NotEmpty
  private String source;

  @Column(nullable = false)
  @NotNull
  @NotEmpty
  private String sourceId;
  
  @Column(nullable = false)
  private OffsetDateTime archived;
  
  public Long getId() {
    return id;
  }
  
  public Long getOrderIndex() {
    return orderIndex;
  }
  
  public void setOrderIndex(Long orderIndex) {
    this.orderIndex = orderIndex;
  }
  
  public String getKuntaApiId() {
    return kuntaApiId;
  }
  
  public void setKuntaApiId(String kuntaApiId) {
    this.kuntaApiId = kuntaApiId;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public String getSourceId() {
    return sourceId;
  }

  public void setSourceId(String sourceId) {
    this.sourceId = sourceId;
  }
  
  public String getType() {
    return type;
  }
  
  public void setType(String type) {
    this.type = type;
  }
  
  public String getOrganizationKuntaApiId() {
    return organizationKuntaApiId;
  }
  
  public void setOrganizationKuntaApiId(String organizationKuntaApiId) {
    this.organizationKuntaApiId = organizationKuntaApiId;
  }
  
  public OffsetDateTime getArchived() {
    return archived;
  }
  
  public void setArchived(OffsetDateTime archived) {
    this.archived = archived;
  }
  
}
