package fi.otavanopisto.kuntaapi.server.controllers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;

import org.wildfly.clustering.group.Group;
import org.wildfly.clustering.group.Node;

@ApplicationScoped
@Singleton
public class ClusterController {

  @Resource(lookup = "java:jboss/clustering/group/web")  
  private Group channelGroup;  

  public String getLocalNodeName() {
    Node localNode = channelGroup.getLocalNode();
    if (localNode != null) {
      return localNode.getName();
    }
    
    return "UNKNOWN";
  }
  
  public List<String> getNodeNames() {
    List<Node> nodes = channelGroup.getNodes();
    
    List<String> result = new ArrayList<>(nodes.size());
    for (Node node : nodes) {
      result.add(node.getName());
    }
    
    Collections.sort(result);
    
    return result;
  }
  
}
