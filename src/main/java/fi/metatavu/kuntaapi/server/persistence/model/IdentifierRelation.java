package fi.metatavu.kuntaapi.server.persistence.model;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * JPA entity representing a parent - child relation between identifiers
 * 
 * @author Antti Leppä
 */
@Entity
@Table(uniqueConstraints = { 
  @UniqueConstraint(columnNames = { "parent_id", "child_id" }) 
})
@Cacheable(true)
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL)
public class IdentifierRelation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @ManyToOne 
  private Identifier child;
  
  @ManyToOne 
  private Identifier parent;
  
  public Long getId() {
    return id;
  }
  
  public void setId(Long id) {
    this.id = id;
  }
  
  public void setChild(Identifier child) {
    this.child = child;
  }
  
  public Identifier getChild() {
    return child;
  }
 
  public void setParent(Identifier parent) {
    this.parent = parent;
  }
  
  public Identifier getParent() {
    return parent;
  }
  
}
