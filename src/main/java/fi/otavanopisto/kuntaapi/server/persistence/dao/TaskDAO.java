package fi.otavanopisto.kuntaapi.server.persistence.dao;

import java.time.OffsetDateTime;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import fi.otavanopisto.kuntaapi.server.persistence.model.Task;
import fi.otavanopisto.kuntaapi.server.persistence.model.Task_;

/**
 * DAO class for Task entity
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class TaskDAO extends AbstractDAO<Task> {
  
  public Task create(String queue, Boolean priority, byte[] data, OffsetDateTime created) {
    Task task = new Task();
    task.setCreated(created);
    task.setData(data);
    task.setPriority(priority);
    task.setQueue(queue);
    
    return persist(task);
  }
  
  public Task findByNextInQueue(String queue) {
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Task> criteria = criteriaBuilder.createQuery(Task.class);
    Root<Task> root = criteria.from(Task.class);
    criteria.select(root);
    criteria.where(criteriaBuilder.equal(root.get(Task_.queue), queue));
    criteria.orderBy(criteriaBuilder.desc(root.get(Task_.priority)), criteriaBuilder.asc(root.get(Task_.created)), criteriaBuilder.asc(root.get(Task_.id)));
    TypedQuery<Task> query = entityManager.createQuery(criteria);
    query.setMaxResults(1);
    
    return getSingleResult(query);
  }
  
}
