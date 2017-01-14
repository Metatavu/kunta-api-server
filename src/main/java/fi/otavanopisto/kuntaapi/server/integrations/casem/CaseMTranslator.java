package fi.otavanopisto.kuntaapi.server.integrations.casem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.lang3.StringUtils;

import fi.metatavu.kuntaapi.server.rest.model.LocalizedValue;
import fi.metatavu.kuntaapi.server.rest.model.Page;
import fi.metatavu.kuntaapi.server.rest.model.PageMeta;
import fi.otavanopisto.casem.client.model.Node;
import fi.otavanopisto.casem.client.model.NodeName;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PageId;
import fi.otavanopisto.kuntaapi.server.persistence.model.OrganizationSetting;
import fi.otavanopisto.kuntaapi.server.settings.OrganizationSettingController;

@ApplicationScoped
public class CaseMTranslator {

  private static final int MAX_SLUG_LENGTH = 30;
  
  @Inject
  private OrganizationSettingController organizationSettingController;
  
  public Page translatePage(PageId kuntaApiPageId, PageId kuntaApiParentPageId, Node node) {
    List<LocalizedValue> titles = translateNodeNames(kuntaApiPageId.getOrganizationId(), node.getNames());
    String slug = slugify(titles.isEmpty() ? kuntaApiPageId.getId() : titles.get(0).getValue());
    return translatePage(kuntaApiPageId, kuntaApiParentPageId, slug, titles);
  }

  public Page translatePage(PageId kuntaApiPageId, PageId kuntaApiParentPageId, String title, String slug) {
    return translatePage(kuntaApiPageId, kuntaApiParentPageId, slug, toTitles(title));
  }
  
  private Page translatePage(PageId kuntaApiPageId, PageId kuntaApiParentPageId, String slug, List<LocalizedValue> titles) {
    PageMeta meta = new PageMeta();
    meta.setHideMenuChildren(true);
    
    Page page = new Page();
    page.setId(kuntaApiPageId.getId());
    page.setTitles(titles);
    page.setSlug(slug);
    page.setParentId(kuntaApiParentPageId != null ? kuntaApiParentPageId.getId() : null);
    page.setMeta(meta);
      
    return page;
  }
  
  public String slugify(String title) {
    return slugify(title, MAX_SLUG_LENGTH );
  }
  
  private List<LocalizedValue> toTitles(String title) {
    LocalizedValue localizedValue = new LocalizedValue();
    localizedValue.setLanguage(CaseMConsts.DEFAULT_LANGUAGE);
    localizedValue.setValue(title);
    return Collections.singletonList(localizedValue);
  }
  
  private String slugify(String text, int maxLength) {
    String urlName = StringUtils.normalizeSpace(text);
    if (StringUtils.isBlank(urlName))
      return UUID.randomUUID().toString();
    
    urlName = StringUtils.lowerCase(StringUtils.substring(StringUtils.stripAccents(urlName.replaceAll(" ", "_")).replaceAll("[^a-zA-Z0-9\\-\\.\\_]", ""), 0, maxLength));
    if (StringUtils.isBlank(urlName)) {
      urlName = UUID.randomUUID().toString();
    }
    
    return urlName;
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
  
}
