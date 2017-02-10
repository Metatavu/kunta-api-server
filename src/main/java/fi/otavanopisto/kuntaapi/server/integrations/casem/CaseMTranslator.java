package fi.otavanopisto.kuntaapi.server.integrations.casem;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

import fi.metatavu.kuntaapi.server.rest.model.FileDef;
import fi.metatavu.kuntaapi.server.rest.model.LocalizedValue;
import fi.metatavu.kuntaapi.server.rest.model.Page;
import fi.metatavu.kuntaapi.server.rest.model.PageMeta;
import fi.otavanopisto.casem.client.model.Node;
import fi.otavanopisto.casem.client.model.NodeName;
import fi.otavanopisto.kuntaapi.server.id.FileId;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PageId;
import fi.otavanopisto.kuntaapi.server.integrations.BinaryHttpClient.DownloadMeta;
import fi.otavanopisto.kuntaapi.server.persistence.model.OrganizationSetting;
import fi.otavanopisto.kuntaapi.server.settings.OrganizationSettingController;

@ApplicationScoped
public class CaseMTranslator {
  
  private static final int MAX_SLUG_LENGTH = 30;

  @Inject
  private Logger logger;

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
  
  public List<LocalizedValue> translateLocalized(String content) {
    LocalizedValue localizedValue = new LocalizedValue();
    localizedValue.setLanguage(CaseMConsts.DEFAULT_LANGUAGE);
    localizedValue.setValue(content);
    
    return Collections.singletonList(localizedValue);
  }
  
  public FileDef translateFile(PageId kuntaApiPageId, FileId kuntaApiFileId, DownloadMeta meta) {
    String filename = sanitizeFilename(meta.getFilename());
    
    FileDef file = new FileDef();
    file.setContentType(meta.getContentType());
    file.setId(kuntaApiFileId.getId());
    file.setSize(meta.getSize() != null ? meta.getSize().longValue() : null);
    file.setSlug(slugify(filename));
    file.setTitle(filename);
    
    if (kuntaApiPageId != null) {
      file.setPageId(kuntaApiPageId.getId());
    }
    
    return file;
  }
  
  private String sanitizeFilename(String filename) {
    if (StringUtils.isBlank(filename)) {
      return "noname";
    }
    
    try {
      InputStream latinStream = IOUtils.toInputStream(filename, "ISO-8859-1");
      String unicodeString = IOUtils.toString(latinStream, "UTF-8");
      if (StringUtils.isNotBlank(unicodeString)) {
        return StringUtils.replace(unicodeString, ".pdf.pdf", ".pdf");
      }
    } catch (IOException e) {
      if (logger.isLoggable(Level.WARNING)) {
        logger.log(Level.WARNING, String.format("Failed to sanitize CaseM filename %s", filename), e);
      }
    }

    return filename;
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
