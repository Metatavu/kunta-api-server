package fi.metatavu.kuntaapi.server.tasks.jms;

import java.io.Serializable;

import fi.metatavu.metaflow.tasks.Task;

/**
 * Default class for all JMS based task queues
 * 
 * @author Antti Leppä
 *
 * @param <T> task
 */
public abstract class DefaultJmsTaskQueue<T extends Task> extends AbstractJmsTaskQueue<T, Serializable> {

}