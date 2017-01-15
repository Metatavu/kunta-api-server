package fi.otavanopisto.kuntaapi.server.integrations.casem;

import java.util.Comparator;

import fi.otavanopisto.casem.client.model.Node;

class CasemNodeComparator implements Comparator<Node> {
  
  @Override
  public int compare(Node node1, Node node2) {
    Integer sortOrder1 = node1.getSortOrder();
    if (sortOrder1 == null) {
      sortOrder1 = 0;
    }
    
    Integer sortOrder2 = node2.getSortOrder();
    if (sortOrder2 == null) {
      sortOrder2 = 0;
    }

    return sortOrder1.compareTo(sortOrder2);
  }
  
}