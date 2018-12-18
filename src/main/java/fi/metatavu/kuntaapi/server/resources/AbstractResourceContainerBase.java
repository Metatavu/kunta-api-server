package fi.metatavu.kuntaapi.server.resources;

import java.io.Serializable;

public abstract class AbstractResourceContainerBase implements Serializable {
  
  private static final long serialVersionUID = 4394386250480036877L;

  public abstract String getEntityType();
  
  public abstract String getName();
  
}
