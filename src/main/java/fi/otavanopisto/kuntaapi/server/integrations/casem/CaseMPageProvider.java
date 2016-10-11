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
import fi.otavanopisto.casem.client.model.NodeName;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.integrations.AttachmentData;
import fi.otavanopisto.kuntaapi.server.integrations.AttachmentId;
import fi.otavanopisto.kuntaapi.server.integrations.IdController;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.integrations.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.PageId;
import fi.otavanopisto.kuntaapi.server.integrations.PageProvider;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.persistence.model.OrganizationSetting;
import fi.otavanopisto.kuntaapi.server.rest.model.Attachment;
import fi.otavanopisto.kuntaapi.server.rest.model.LocalizedValue;
import fi.otavanopisto.kuntaapi.server.rest.model.Page;
import fi.otavanopisto.kuntaapi.server.settings.OrganizationSettingController;

/**
 * Page provider for CaseM
 * 
 * @author Antti Leppä
 * @author Heikki Kurhinen
 */
@Dependent
public class CaseMPageProvider implements PageProvider {
  
  @Inject
  private Logger logger;
  
  @Inject
  private CaseMApi caseApi;

  @Inject
  private IdController idController;
  
  @Inject
  private IdentifierController identifierController;
  
  @Inject
  private OrganizationSettingController organizationSettingController;
  
  private CaseMPageProvider() {
  }
  
  @Override
  public List<Page> listOrganizationPages(OrganizationId organizationId, PageId parentId, boolean onlyRootPages, String path) {
    if (onlyRootPages) {
      Long rootNodeId = getRootNodeId(organizationId);
      if (rootNodeId == null) {
        logger.severe(String.format("Organization %s CaseM root node is not defined", organizationId.toString()));
        return Collections.emptyList();
      }
      
      List<Node> rootNodes = getChildNodes(organizationId, rootNodeId);
      return translateNodes(organizationId, rootNodeId, rootNodes);
    }
    
    return Collections.emptyList();
  }

  private List<Node> getChildNodes(OrganizationId organizationId, Long caseMParentId) {
    List<Node> result = new ArrayList<>();
    Long skipToken = null;
    
    do {
      NodeList nodeList = getChildNodeList(organizationId, caseMParentId, skipToken);
      if (nodeList == null) {
        break;
      }
      
      result.addAll(nodeList.getValue());
      
      skipToken = getSkipToken(nodeList.getOdataNextLink());
    } while (skipToken != null);
    
    Collections.sort(result, new NodeComparator());
    
    return result;
  }
  
  private NodeList getChildNodeList(OrganizationId organizationId, Long caseMParentId, Long skipToken) {
    ApiResponse<NodeList> response = caseApi.getNodesApi(organizationId)
      .listSubNodes(caseMParentId, skipToken != null ? String.valueOf(skipToken) : null);
    
    if (!response.isOk()) {
      logger.severe(String.format("Listing nodes by parent %d failed on [%d] %s", caseMParentId, response.getStatus(), response.getMessage()));
      return null;
    } else {
      return response.getResponse();
    }
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
  
  @Override
  public Page findOrganizationPage(OrganizationId organizationId, PageId pageId) {
    return null;
  }
  
  @Override
  public List<LocalizedValue> findOrganizationPageContents(OrganizationId organizationId, PageId pageId) {
    return Collections.emptyList();
  }

  @Override
  public List<Attachment> listOrganizationPageImages(OrganizationId organizationId, PageId pageId) {
    return Collections.emptyList();
  }

  @Override
  public Attachment findPageImage(OrganizationId organizationId, PageId pageId, AttachmentId attachmentId) {
    return null;
  }

  @Override
  public AttachmentData getPageImageData(OrganizationId organizationId, PageId pageId, AttachmentId attachmentId, Integer size) {
    return null;
  }
  
  private List<Page> translateNodes(OrganizationId organizationId, Long rootNodeId, List<Node> nodes) {
    List<Page> result = new ArrayList<>(nodes.size());
    for (Node node : nodes) {
      result.add(translateNode(organizationId, rootNodeId, node));
    }

    return result;
  }
  
  private Page translateNode(OrganizationId organizationId, Long rootNodeId, Node node) {
    Page page = new Page();

    PageId kuntaApiId = translateNodeId(node.getNodeId());
    PageId kuntaApiParentId = rootNodeId.equals(node.getParentId()) ? null : translateNodeId(node.getParentId());

    page.setId(kuntaApiId.getId());
    page.setParentId(kuntaApiParentId != null ? kuntaApiParentId.getId() : null);
    page.setTitles(translateNodeNames(organizationId, node.getNames()));
    // TODO: Pretty url
    page.setSlug(kuntaApiId.getId());
    
    return page;
  }
  
  private PageId translateNodeId(Long nodeId) {
    if (nodeId == null) {
      return null;
    }
    
    String casemNodeId = String.valueOf(nodeId);
    
    PageId casemId = new PageId(CaseMConsts.IDENTIFIER_NAME, casemNodeId);
    PageId kuntaApiId = idController.translatePageId(casemId, KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiId == null) {
      logger.info(String.format("Found new page %s", casemNodeId));
      Identifier newIdentifier = identifierController.createIdentifier(casemId);
      kuntaApiId = new PageId(KuntaApiConsts.IDENTIFIER_NAME, newIdentifier.getKuntaApiId());
    }
    
    return kuntaApiId;
  }
  
  private List<LocalizedValue> translateNodeNames(OrganizationId organizationId, List<NodeName> names) {
    List<LocalizedValue> result = new ArrayList<>(names.size());
    
    for (NodeName name : names) {
      LocalizedValue localizedValue = new LocalizedValue();
      
      String language = translateLanguage(organizationId, name.getLanguageId());
      localizedValue.setLanguage(language);
      localizedValue.setValue(name.getName());
      result.add(localizedValue);
    }
    
    return result;
  }
  
  private String translateLanguage(OrganizationId organizationId, Long localeId) {
    OrganizationSetting localeSetting = organizationSettingController.findOrganizationSettingByKey(organizationId, String.format(CaseMConsts.ORGANIZATION_SETTING_LOCALE_ID, localeId));
    if (localeSetting != null) {
      return localeSetting.getValue();
    }
    
    return CaseMConsts.DEFAULT_LANGUAGE;
  }
  
  private Long getRootNodeId(OrganizationId organizationId) {
    String rootNode = organizationSettingController.getSettingValue(organizationId, CaseMConsts.ORGANIZATION_SETTING_ROOT_NODE);
    if (StringUtils.isNumeric(rootNode)) {
      return NumberUtils.createLong(rootNode);
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
