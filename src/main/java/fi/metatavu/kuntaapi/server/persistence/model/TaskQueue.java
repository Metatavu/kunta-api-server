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
import javax.validation.constraints.NotEmpty;

/**
 * JPA entity representing task queue
 * 
 * @author Antti Leppä
 */
@Entity
@Cacheable(true)
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "name", "responsibleNode" }) })
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
public class TaskQueue {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  @NotNull
  @NotEmpty
  private String name;
  
  @Column(nullable = false)
  @NotNull
  @NotEmpty
  private String responsibleNode;

  private OffsetDateTime lastTaskReturned;
  
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getResponsibleNode() {
    return responsibleNode;
  }

  public void setResponsibleNode(String responsibleNode) {
    this.responsibleNode = responsibleNode;
  }
  
  public OffsetDateTime getLastTaskReturned() {
    return lastTaskReturned;
  }
  
  public void setLastTaskReturned(OffsetDateTime lastTaskReturned) {
    this.lastTaskReturned = lastTaskReturned;
  }

}