package fi.otavanopisto.kuntaapi.server.integrations.casem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import fi.otavanopisto.casem.client.ApiResponse;
import fi.otavanopisto.casem.client.model.Node;
import fi.otavanopisto.casem.client.model.NodeList;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.settings.OrganizationSettingController;

@Dependent
public class CaseMCacheUpdater {

  @Inject
  private Logger logger;
  
  @Inject
  private CaseMCache caseMCache;
  
  @Inject
  private CaseMApi caseMApi;
  
  @Inject
  private OrganizationSettingController organizationSettingController;
  
  public void refreshContents(OrganizationId organizationId) {
    Long caseMRootNodeId = getCaseMRootNodeId(organizationId);
    if (caseMRootNodeId == null) {
      logger.severe(String.format("Organization %s CaseM root node is not defined", organizationId.toString()));
    }
    
    List<Node> nodes = getChildNodes(organizationId, caseMRootNodeId, Collections.emptyList());
    cacheNodeTree(organizationId, caseMRootNodeId, nodes, new ArrayList<>()); 
  }
  
  private void cacheNodeTree(OrganizationId organizationId, Long caseMRootNodeId, List<Node> nodes, List<Long> caseMParentIds) {
    for (Node node : nodes) {
      caseMCache.cacheNode(organizationId, caseMRootNodeId, node);
      List<Long> childCaseMParentIds = new ArrayList<>(caseMParentIds);
      childCaseMParentIds.add(node.getNodeId());
      cacheNodeTree(organizationId, caseMRootNodeId, getChildNodes(organizationId, caseMRootNodeId, childCaseMParentIds), childCaseMParentIds);
    }
  }
  
  private Long getCaseMRootNodeId(OrganizationId organizationId) {
    String rootNode = organizationSettingController.getSettingValue(organizationId, CaseMConsts.ORGANIZATION_SETTING_ROOT_NODE);
    if (StringUtils.isNumeric(rootNode)) {
      return NumberUtils.createLong(rootNode);
    }
    
   return null;
  }

  private List<Node> getChildNodes(OrganizationId organizationId, Long caseMRootNodeId, List<Long> caseMParentIds) {
    List<Node> result = new ArrayList<>();
    Long skipToken = null;
    
    do {
      NodeList nodeList = getChildNodeList(organizationId, caseMRootNodeId, caseMParentIds, skipToken);
      if (nodeList == null) {
        break;
      }
      
      result.addAll(nodeList.getValue());
      
      skipToken = getSkipToken(nodeList.getOdataNextLink());
    } while (skipToken != null);
    
    Collections.sort(result, new NodeComparator());
    
    return result;
  }
  
  private NodeList getChildNodeList(OrganizationId organizationId, Long caseMRootNodeId, List<Long> caseMParentIds, Long skipToken) {
    String pathQuery = getSubNodePath(caseMParentIds);
    
    ApiResponse<NodeList> response = caseMApi.getNodesApi(organizationId)
      .listSubNodes(caseMRootNodeId, pathQuery, skipToken != null ? String.valueOf(skipToken) : null);
    
    if (!response.isOk()) {
      logger.severe(String.format("Listing nodes by rootNode %d and pathQuery %s failed on [%d] %s", caseMRootNodeId, pathQuery, response.getStatus(), response.getMessage()));
      return null;
    } else {
      return response.getResponse();
    }
  }
  
  private String getSubNodePath(List<Long> caseMParentIds) {
    StringBuilder result = new StringBuilder();
    
    for (Long caseMParentId : caseMParentIds) {
      result.append(String.format("SubNodes(%d)/", caseMParentId));
    }
    
    result.append("SubNodes()");
    
    return result.toString();
  }
  
  private Long getSkipToken(String nextLink) {
    if (StringUtils.isBlank(nextLink)) {
      return null;
    }
    
    Pattern pattern = Pattern.compile("(.*\\$skiptoken=)([0-9]*)");
    Matcher matcher = pattern.matcher(nextLink);
    if (matcher.matches() && (matcher.groupCount() > 1)) {
      return NumberUtils.createLong(matcher.group(2));
    }
    
    return null;
  }

  private class NodeComparator implements Comparator<Node> {
    @Override
    public int compare(Node node1, Node node2) {
      Integer sortOrder1 = node1.getSortOrder();
      if (sortOrder1 == null) {
        sortOrder1 = 0;
      }
      
      Integer sortOrder2 = node1.getSortOrder();
      if (sortOrder2 == null) {
        sortOrder2 = 0;
      }

      return sortOrder1.compareTo(sortOrder2);
    }
  }
}
