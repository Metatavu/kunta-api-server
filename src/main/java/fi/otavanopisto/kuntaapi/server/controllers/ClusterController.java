package fi.otavanopisto.kuntaapi.server.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.wildfly.clustering.group.Group;
import org.wildfly.clustering.group.Node;

@ApplicationScoped
public class ClusterController {

  @Inject
  private Logger logger;
  
  private Group channelGroup;
  
  @PostConstruct
  private void postConstruct() {
    try {  
      channelGroup = (Group) new InitialContext().lookup("java:jboss/clustering/group/web");
    } catch (NamingException ex) {
      logger.log(Level.SEVERE, "Cannot find java:jboss/clustering/group/web disabling task distribution");
    }
  }

  public String getLocalNodeName() {
    if (channelGroup == null) {
      return "UNKNOWN";
    }
    
    Node localNode = channelGroup.getLocalNode();
    if (localNode != null) {
      return localNode.getName();
    }
    
    return "UNKNOWN";
  }
  
  public List<String> getNodeNames() {
    if (channelGroup == null) {
      return Collections.singletonList("UNKNOWN");
    }
    
    List<Node> nodes = channelGroup.getNodes();
    
    List<String> result = new ArrayList<>(nodes.size());
    for (Node node : nodes) {
      result.add(node.getName());
    }
    
    Collections.sort(result);
    
    return result;
  }
  
}
