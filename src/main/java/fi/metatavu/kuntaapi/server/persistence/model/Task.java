package fi.metatavu.kuntaapi.server.persistence.model;

import java.time.OffsetDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

/**
 * JPA entity representing queued task
 * 
 * @author Antti Leppä
 */
@Entity
public class Task {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false)
  private Boolean priority;
  
  @ManyToOne 
  private TaskQueue queue;
  
  @Column(nullable = false)
  @NotNull
  @NotEmpty
  private byte[] data;

  @Column(nullable = false)
  private OffsetDateTime created;
  
  @Column(nullable = false)
  @NotNull
  @NotEmpty
  private String uniqueId;
  
  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }
  
  public Boolean getPriority() {
    return priority;
  }

  public void setPriority(Boolean priority) {
    this.priority = priority;
  }

  public TaskQueue getQueue() {
    return queue;
  }
  
  public void setQueue(TaskQueue queue) {
    this.queue = queue;
  }

  public byte[] getData() {
    return data;
  }

  public void setData(byte[] data) {
    this.data = data;
  }

  public OffsetDateTime getCreated() {
    return created;
  }

  public void setCreated(OffsetDateTime created) {
    this.created = created;
  }
  
  /**
   * Returns unique id for the task in the queue. Property is used to ensure that task is added only once to the queue
   * 
   * @return unique id
   */
  public String getUniqueId() {
    return uniqueId;
  }
  
  /**
   * Sets unique id for the task in the queue.
   * 
   * @param uniqueId unique id
   */
  public void setUniqueId(String uniqueId) {
    this.uniqueId = uniqueId;
  }
  
}
