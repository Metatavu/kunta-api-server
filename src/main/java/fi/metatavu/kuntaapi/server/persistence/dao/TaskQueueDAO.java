package fi.metatavu.kuntaapi.server.persistence.dao;

import java.time.OffsetDateTime;
import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import fi.metatavu.kuntaapi.server.persistence.model.TaskQueue;
import fi.metatavu.kuntaapi.server.persistence.model.TaskQueue_;

/**
 * DAO class for Task entity
 * 
 * @author Antti Leppä
 */
@ApplicationScoped
public class TaskQueueDAO extends AbstractDAO<TaskQueue> {
  
  /**
   * Creates new task queue entity
   * @param name
   * @param responsibleNode 
   * @return created task queue
   */
  public TaskQueue create(String name, String responsibleNode) {
    TaskQueue taskQueue = new TaskQueue();
    taskQueue.setName(name);
    taskQueue.setResponsibleNode(responsibleNode);
    return persist(taskQueue);
  }
  
  /**
   * Returns task queue by name
   * 
   * @param name queue name
   * @param responsibleNode responsible node
   * @return
   */
  public TaskQueue findByName(String name) {
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<TaskQueue> criteria = criteriaBuilder.createQuery(TaskQueue.class);
    Root<TaskQueue> root = criteria.from(TaskQueue.class);
    criteria.select(root);
    criteria.where(criteriaBuilder.equal(root.get(TaskQueue_.name), name));
    
    return getSingleResult(entityManager.createQuery(criteria));
  }
  
  /**
   * Returns task queue by name and responsible node
   * 
   * @param name queue name
   * @param responsibleNode responsible node
   * @return
   */
  public TaskQueue findByNameAndResponsibleNode(String name, String responsibleNode) {
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<TaskQueue> criteria = criteriaBuilder.createQuery(TaskQueue.class);
    Root<TaskQueue> root = criteria.from(TaskQueue.class);
    criteria.select(root);
    criteria.where(
      criteriaBuilder.and(
        criteriaBuilder.equal(root.get(TaskQueue_.name), name),
        criteriaBuilder.equal(root.get(TaskQueue_.responsibleNode), responsibleNode)
      )
    );
    
    return getSingleResult(entityManager.createQuery(criteria));
  }
  
  /**
   * Returns task queue by index. List is ordered by id
   * 
   * @return task queue by index
   */
  public TaskQueue findTaskQueueByIndex(int index) {
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<TaskQueue> criteria = criteriaBuilder.createQuery(TaskQueue.class);
    Root<TaskQueue> root = criteria.from(TaskQueue.class);
    criteria.select(root);
    criteria.orderBy(criteriaBuilder.asc(root.get(TaskQueue_.id)));
    TypedQuery<TaskQueue> query = entityManager.createQuery(criteria);
    query.setFirstResult(index);
    query.setMaxResults(1);
    
    return getSingleResult(query);
  }
  
  /**
   * Returns all task queues in id order
   * 
   * @return all task queues
   */
  public List<TaskQueue> listAllTaskQueues() {
    EntityManager entityManager = getEntityManager();

    CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
    CriteriaQuery<TaskQueue> criteria = criteriaBuilder.createQuery(TaskQueue.class);
    Root<TaskQueue> root = criteria.from(TaskQueue.class);
    criteria.select(root);
    criteria.orderBy(criteriaBuilder.asc(root.get(TaskQueue_.id)));
    
    return entityManager.createQuery(criteria).getResultList();
  }

  /**
   * Updates a task queue responsible node
   * 
   * @param taskQueue task queue
   * @param responsibleNode responsible node
   * @return updated task queue
   */
  public TaskQueue updateResponsibleNode(TaskQueue taskQueue, String responsibleNode) {
    taskQueue.setResponsibleNode(responsibleNode);
    return persist(taskQueue);
  }
  
  /**
   * Updates when the last task of the queue has been retruned
   * 
   * @param taskQueue task queue
   * @param lastTaskReturned time when last task was returned
   * @return updated task queue
   */
  public TaskQueue updateLastTaskReturned(TaskQueue taskQueue, OffsetDateTime lastTaskReturned) {
    taskQueue.setLastTaskReturned(lastTaskReturned);
    return persist(taskQueue);
  }

}
