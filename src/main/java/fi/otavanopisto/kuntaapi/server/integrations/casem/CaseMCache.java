package fi.otavanopisto.kuntaapi.server.integrations.casem;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.codec.digest.DigestUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.metatavu.kuntaapi.server.rest.model.LocalizedValue;
import fi.metatavu.kuntaapi.server.rest.model.Page;
import fi.otavanopisto.kuntaapi.server.cache.ModificationHashCache;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PageId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.integrations.casem.resources.CaseMPageResourceContainer;
import fi.otavanopisto.kuntaapi.server.integrations.casem.resources.CaseMPageContentResourceContainer;

@ApplicationScoped
@Singleton
@SuppressWarnings ("squid:S3306")
public class CaseMCache {

  @Inject
  private Logger logger;

  @Inject
  private CaseMPageResourceContainer pageCache;
  
  @Inject
  private CaseMPageContentResourceContainer pageContentCache;
  
  @Inject
  private ModificationHashCache modificationHashCache;

  public Page findPage(PageId pageId) {
    return pageCache.get(pageId);
  }
  
  public void cachePage(OrganizationId organizationId, Page page, List<LocalizedValue> content) {
    if (page == null) {
      logger.severe(String.format("Tried to cache null CaseM page in oranization %s", organizationId));
      return;
    }

    PageId pageId = new PageId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, page.getId());
    pageCache.put(pageId, page);
    if (content != null) {
      pageContentCache.put(pageId, content);
    }
    
    Map<String, Object> hashData = new HashMap<>();
    hashData.put("page", page);
    hashData.put("content", content != null ? content : pageContentCache.get(pageId));
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      String cacheHash = DigestUtils.md5Hex(objectMapper.writeValueAsBytes(hashData));
      modificationHashCache.put(pageId.getId(), cacheHash);
    } catch (JsonProcessingException e) {
      logger.log(Level.SEVERE, "Failed to serialize cache hash object", e);
    }
  }

  public void removePage(PageId pageId) {
    pageContentCache.clear(pageId);
    pageCache.clear(pageId);
  }

  public List<LocalizedValue> getPageContent(PageId pageId) {
    return pageContentCache.get(pageId);
  }
  
}
