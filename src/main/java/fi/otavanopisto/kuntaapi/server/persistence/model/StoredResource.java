package fi.otavanopisto.kuntaapi.server.persistence.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

/**
 * JPA entity representing stored resource
 * 
 * @author Antti Leppä
 */
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = { "identifier_id" }) })
public class StoredResource {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
  
  @ManyToOne 
  private Identifier identifier;
  
  @Column(nullable = false)
  @NotNull
  @NotEmpty
  private String type;
  
  @Column(nullable = false)
  @NotNull
  @NotEmpty
  @Lob
  private String data;
  
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }
  
  public String getData() {
    return data;
  }
  
  public void setData(String data) {
    this.data = data;
  }
  
  public Identifier getIdentifier() {
    return identifier;
  }
  
  public void setIdentifier(Identifier identifier) {
    this.identifier = identifier;
  }
  
  public String getType() {
    return type;
  }
  
  public void setType(String type) {
    this.type = type;
  }
  
}
