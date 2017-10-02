package fi.otavanopisto.kuntaapi.server.persistence.dao;

import java.time.OffsetDateTime;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import fi.otavanopisto.kuntaapi.server.persistence.model.Task;
import fi.otavanopisto.kuntaapi.server.persistence.model.TaskQueue;
import fi.otavanopisto.kuntaapi.server.persistence.model.Task_;

/**
 * DAO class for Task entity
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class TaskDAO extends AbstractDAO<Task> {
  
  /**
   * Creates new task entity
   * 
   * @param queue queue the task belongs to
   * @param uniqueId unique id for the task. Property is used to ensure that task is added only once to the queue
   * @param priority whether the task is a priority task or not
   * @param data serialized task data
   * @param created creation time
   * @return created task
   */
  public Task create(TaskQueue queue, String uniqueId, Boolean priority, byte[] data, OffsetDateTime created) {
    Task task = new Task();
    task.setUniqueId(uniqueId);
    task.setCreated(created);
    task.setData(data);
    task.setPriority(priority);
    task.setQueue(queue);
    Task result = persist(task);
    flush();
    return result;
  }
  
  /**
   * Finds task by queue and unique id
   * 
   * @param queue queue
   * @param uniqueId unique id
   * @return task
   */
  public Task findByQueueAndUniqueId(TaskQueue queue, String uniqueId) {
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Task> criteria = criteriaBuilder.createQuery(Task.class);
    Root<Task> root = criteria.from(Task.class);
    criteria.select(root);
    criteria.where(
      criteriaBuilder.and(
        criteriaBuilder.equal(root.get(Task_.queue), queue),
        criteriaBuilder.equal(root.get(Task_.uniqueId), uniqueId)
      )
    );
    
    return getSingleResult(entityManager.createQuery(criteria));
  }
  
  /**
   * Counts tasks by queue and unique id
   * 
   * @param queue queue
   * @param uniqueId unique id
   * @return count of tasks by queue and unique id
   */
  public Long countByQueueAndUniqueId(TaskQueue queue, String uniqueId) {
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Long> criteria = criteriaBuilder.createQuery(Long.class);
    Root<Task> root = criteria.from(Task.class);
    criteria.select(criteriaBuilder.count(root.get(Task_.id)));
    criteria.where(
      criteriaBuilder.and(
        criteriaBuilder.equal(root.get(Task_.queue), queue),
        criteriaBuilder.equal(root.get(Task_.uniqueId), uniqueId)
      )
    );
    
    return getSingleResult(entityManager.createQuery(criteria));
  }
  
  /**
   * Returns next scheduled task for the specified queue
   * 
   * @param queue queue
   * @return Next scheduled task for the specified queue
   */
  public Task findNextInQueue(TaskQueue queue) {
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
  
  public Long countByQueue(TaskQueue queue) {
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<Long> criteria = criteriaBuilder.createQuery(Long.class);
    Root<Task> root = criteria.from(Task.class);
    criteria.select(criteriaBuilder.count(root));
    criteria.where(criteriaBuilder.equal(root.get(Task_.queue), queue));
    
    return getSingleResult(entityManager.createQuery(criteria));
  }

  /**
   * Updates task's priority property
   * 
   * @param task task
   * @param priority priority
   * @return Updated task
   */
  public Task updatePriority(Task task, Boolean priority) {
    task.setPriority(priority);
    return persist(task);
  }
  
}
