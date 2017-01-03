package fi.otavanopisto.kuntaapi.server.integrations.casem;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.ejb.AccessTimeout;
import javax.ejb.Singleton;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import fi.metatavu.kuntaapi.server.rest.model.LocalizedValue;
import fi.metatavu.kuntaapi.server.rest.model.Page;
import fi.otavanopisto.casem.client.ApiResponse;
import fi.otavanopisto.casem.client.model.Content;
import fi.otavanopisto.casem.client.model.ContentClassification;
import fi.otavanopisto.casem.client.model.ContentList;
import fi.otavanopisto.casem.client.model.ExtendedProperty;
import fi.otavanopisto.casem.client.model.ExtendedPropertyList;
import fi.otavanopisto.casem.client.model.Node;
import fi.otavanopisto.casem.client.model.NodeList;
import fi.otavanopisto.casem.client.model.NodeName;
import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.controllers.PageController;
import fi.otavanopisto.kuntaapi.server.freemarker.FreemarkerRenderer;
import fi.otavanopisto.kuntaapi.server.id.FileId;
import fi.otavanopisto.kuntaapi.server.id.IdController;
import fi.otavanopisto.kuntaapi.server.id.OrganizationId;
import fi.otavanopisto.kuntaapi.server.id.PageId;
import fi.otavanopisto.kuntaapi.server.index.IndexRequest;
import fi.otavanopisto.kuntaapi.server.index.IndexablePage;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.integrations.casem.model.Councilmen;
import fi.otavanopisto.kuntaapi.server.integrations.casem.model.HistoryTopic;
import fi.otavanopisto.kuntaapi.server.integrations.casem.model.Meeting;
import fi.otavanopisto.kuntaapi.server.integrations.casem.model.MeetingItem;
import fi.otavanopisto.kuntaapi.server.integrations.casem.model.MeetingItemLink;
import fi.otavanopisto.kuntaapi.server.integrations.casem.model.Participant;
import fi.otavanopisto.kuntaapi.server.persistence.model.Identifier;
import fi.otavanopisto.kuntaapi.server.persistence.model.OrganizationSetting;
import fi.otavanopisto.kuntaapi.server.settings.OrganizationSettingController;

@ApplicationScoped
@Singleton
@AccessTimeout (unit = TimeUnit.HOURS, value = 1l)
@SuppressWarnings ("squid:S3306")
public class CaseMCacheUpdater {

  private static final String EXTENDED_HISTORY_TOPICS = "HistoryTopics";
  private static final String EXTENDED_IS_ADDITIONAL_TOPIC = "IsAdditionalTopic";
  private static final String EXTENDED_CORRECTIONINSTRUCTIONS = "Correctioninstructions";
  private static final String EXTENDED_DECISIONPROPOSAL = "Decisionproposal";
  private static final String EXTENDED_DECISION_NOTES = "DecisionNotes";
  private static final String EXTENDED_NAME = "Name";
  private static final String EXTENDED_DECISION = "Decision";
  private static final String EXTENDED_CASE_NATIVE_ID = "CaseNativeId";
  private static final String EXTENDED_DRAFTSMANS = "Draftsmans";
  private static final String EXTENDED_ARTICLE = "Article";
  private static final String EXTENDED_INFORM = "Inform";
  private static final String EXTENDED_PRESENTERS = "Presenters";
  private static final String EXTENDED_DESCRIPTION = "Description";
  private static final String EXTENDED_DRAFTING_NOTES = "DraftingNotes";
  private static final String EXTENDED_PARTICIPANT_COUNCILMAN = "Participant_councilman";
  private static final String EXTENDED_AGENDA_ATTACHMENT = "AgendaAttachment";
  private static final String EXTENDED_ATTACHMENTS = "Attachments";
  private static final String GROUP_VARAJASEN = "varajäsenet";
  private static final String GROUP_MEMBER = "member";
  private static final String GROUP_OTHER = "other";
  private static final int MAX_SLUG_LENGTH = 30;
  private static final DateTimeFormatter CSV_DATE_TIME = new DateTimeFormatterBuilder()
    .parseCaseInsensitive()
    .append(DateTimeFormatter.ISO_LOCAL_DATE)
    .appendLiteral(' ')
    .append(DateTimeFormatter.ISO_LOCAL_TIME).toFormatter();
  
  @Inject
  private Logger logger;
  
  @Inject
  private CaseMCache caseMCache;
  
  @Inject
  private CaseMApi caseMApi;
  
  @Inject
  private OrganizationSettingController organizationSettingController;
  
  @Inject
  private FreemarkerRenderer freemarkerRenderer;
  
  @Inject
  private Event<CaseMMeetingDataUpdateRequest> meetingDataUpdateRequest;
  
  @Inject
  private Event<FileUpdateRequest> fileUpdateRequest;
  
  @Inject
  private IdController idController;
  
  @Inject
  private IdentifierController identifierController;
  
  @Inject
  private PageController pageController;

  @Inject
  private Event<IndexRequest> indexRequest;

  private Map<PageId, PageId> discoveredPageIds;

  @PostConstruct
  public void init() {
    discoveredPageIds = new HashMap<>();
  }
  
  public void updateNodes(OrganizationId organizationId) {
    logger.fine(String.format("Updating CaseM nodes for organization %s", organizationId));
    
    Long caseMRootNodeId = getCaseMRootNodeId(organizationId);
    if (caseMRootNodeId == null) {
      logger.severe(String.format("Organization %s CaseM root node is not defined", organizationId.toString()));
    }
    
    List<Node> nodes = getChildNodes(organizationId, caseMRootNodeId, Collections.emptyList());
    cacheNodeTree(organizationId, caseMRootNodeId, null, nodes, new ArrayList<>()); 
    
    logger.fine(String.format("Done updating CaseM nodes for organization %s", organizationId));
  }
  
  public void updateContents(OrganizationId organizationId) {
    logger.fine(String.format("Updating CaseM contents list for organization %s", organizationId));
    
    cacheContents(organizationId);
    
    logger.fine(String.format("Done updating CaseM meeting list for organization %s", organizationId));
  }
  
  public void updateMeeting(Long orderIndex, CaseMMeetingData meetingData) {
    logger.fine(String.format("Updating CaseM meeting %s with %d items", meetingData.getMeetingPageId().toString(), meetingData.getMeetingItemContents().size()));
    
    OrganizationId organizationId = meetingData.getOrganizationId();
    PageId meetingPageId = translatePageId(orderIndex, meetingData.getMeetingPageId(), true);
    Content meetingContent = meetingData.getMeetingContent();
    List<Content> meetingItemContents = meetingData.getMeetingItemContents();
    
    Locale locale = new Locale(CaseMConsts.DEFAULT_LANGUAGE);
    String downloadUrl = getCaseMDownloadUrl(organizationId);
    
    Page meetingPage = caseMCache.findPage(organizationId, meetingPageId);
    if (meetingPage == null) {
      logger.severe(String.format("Meeting page %s could not be found", meetingPageId.toString()));
      return;
    }
    
    PageId meetingParentPageId = new PageId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, meetingPage.getParentId());
    Page meetingParentPage = caseMCache.findPage(organizationId, meetingParentPageId);
    if (meetingParentPage == null) {
      logger.severe(String.format("Meeting parent page %s could not be found", meetingParentPageId.toString()));
      return;
    }
    
    String meetingTitle = String.format("%s, %s", getFirstTitle(meetingParentPage.getTitles()), StringUtils.uncapitalize(getFirstTitle(meetingPage.getTitles())));
    List<ExtendedProperty> meetingExtendedProperties = listExtendedProperties(organizationId, meetingContent);
    boolean memoApproved = isMeetingMemoApproved(meetingExtendedProperties);
    
    List<MeetingItemLink> itemLinks = new ArrayList<>(meetingItemContents.size());
    for (int i = 0; i < meetingItemContents.size(); i++) {
      Content meetingItemContent = meetingItemContents.get(i);
      Long orderNumber = (long) i;
      List<ExtendedProperty> itemExtendedProperties = listExtendedProperties(organizationId, meetingItemContent);
      MeetingItemLink itemLink = createMeetingItemLink(itemExtendedProperties);
      Page meetingItemPage = translateContent(orderNumber, organizationId, meetingPage, meetingItemContent, itemLink.getText(), itemLink.getSlug());
      PageId meetingItemPageId = new PageId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, meetingItemPage.getId());
      
      String meetingItemPageContents = renderContentMeetingItem(createMeetingItemModel(downloadUrl, meetingTitle, memoApproved, itemExtendedProperties), locale);
      caseMCache.cachePageContents(organizationId, meetingItemPageId, meetingItemPageContents);
      caseMCache.cacheNode(organizationId, meetingItemPage);
      indexRequest.fire(new IndexRequest(createIndexablePage(organizationId, meetingItemPageId, locale.getLanguage(), meetingItemPageContents, itemLink.getText())));

      itemLinks.add(itemLink);
      
      for (FileId fileId : getAttachmentFileIds(organizationId, itemExtendedProperties)) {
        fileUpdateRequest.fire(new FileUpdateRequest(meetingItemPageId, fileId));
      }
    }
    
    Collections.sort(itemLinks, (MeetingItemLink o1, MeetingItemLink o2) -> o1.getArticle().compareTo(o2.getArticle()));
    
    Meeting meeting = createMeetingModel(downloadUrl, meetingTitle, memoApproved, itemLinks, meetingExtendedProperties);
    String meetingPageContents = renderContentMeeting(meeting, locale);
    caseMCache.cachePageContents(organizationId, meetingPageId, meetingPageContents);
    indexRequest.fire(new IndexRequest(createIndexablePage(organizationId, meetingPageId, locale.getLanguage(), meetingPageContents, meetingTitle)));

    for (FileId fileId : getAttachmentFileIds(organizationId, meetingExtendedProperties)) {
      fileUpdateRequest.fire(new FileUpdateRequest(meetingPageId, fileId));
    }

    logger.fine(String.format("Done updating CaseM meeting %s", meetingPageId.toString()));
  }
  
  private void cacheContents(OrganizationId organizationId) {
    
    Map<Long, Content> meetingMap = new HashMap<>();
    Map<Long, List<Content>> meetingItemMap = new HashMap<>();
    Map<Long, Long> meetingOrderMap = new HashMap<>();

    mapContents(organizationId, meetingMap, meetingItemMap, meetingOrderMap);
    Set<Entry<Long, List<Content>>> meetingEntries = meetingItemMap.entrySet();
    
    for (Entry<Long,List<Content>> meetingEntry : meetingEntries) {
      Long meetingId = meetingEntry.getKey();
      Long orderIndex = meetingOrderMap.get(meetingId);
      PageId meetingPageId = toNodeId(organizationId, meetingId);
      List<Content> meetingItemContents = meetingEntry.getValue();
      Content meetingContent = meetingMap.get(meetingId);
      CaseMMeetingData meetingData = new CaseMMeetingData(organizationId, meetingPageId, meetingItemContents, meetingContent);
      meetingDataUpdateRequest.fire(new CaseMMeetingDataUpdateRequest(orderIndex, meetingData));
    }
  }

  private void mapContents(OrganizationId organizationId, Map<Long, Content> meetingMap, Map<Long, List<Content>> meetingItemMap, Map<Long, Long> meetingOrderMap) {
    List<Content> contents = getContents(organizationId);
    for (int i = 0; i < contents.size(); i++) {
      Content content = contents.get(i);
      Long orderIndex = (long) i;
      
      Long nodeId = getContentNodeId(content);
      if (nodeId == null) {
        continue;
      }
      
      if (isMeetingPage(content)) {
        meetingMap.put(nodeId, content);
        meetingOrderMap.put(orderIndex, orderIndex);
      } else {
        List<Content> meetingItems = meetingItemMap.get(nodeId);
        if (meetingItems == null) {
          meetingItems = new ArrayList<>();
          meetingItemMap.put(nodeId, meetingItems);
        }
        
        meetingItems.add(content);
      }
    }
  }

  private MeetingItemLink createMeetingItemLink(List<ExtendedProperty> itemExtendedProperties) {
    String name = getName(itemExtendedProperties);
    String slug = slugify(name);
    
    MeetingItemLink itemLink = new MeetingItemLink();
    itemLink.setArticle(getArticle(itemExtendedProperties));
    itemLink.setSlug(slug);
    itemLink.setText(name);
    itemLink.setHasAttachments(hasAttachments(itemExtendedProperties));
    
    return itemLink;
  }

  private boolean isMeetingMemoApproved(List<ExtendedProperty> extendedProperties) {
    for (ExtendedProperty extendedProperty : extendedProperties) {
      if (StringUtils.isNotBlank(extendedProperty.getText()) && EXTENDED_ATTACHMENTS.equals(extendedProperty.getName())) {
        return true;
      }
    }

    return false;
  }

  private String getFirstTitle(List<LocalizedValue> titles) {
    if (titles == null || titles.isEmpty()) {
      return null;
    }
    
    return titles.get(0).getValue();
  }

  private Integer getArticle(List<ExtendedProperty> extendedProperties) {
    for (ExtendedProperty extendedProperty : extendedProperties) {
      if (EXTENDED_ARTICLE.equals(extendedProperty.getName()) && StringUtils.isNumeric(extendedProperty.getText())) {
        return NumberUtils.createInteger(extendedProperty.getText());
      }
    }
    
    return null;
  }

  private String getName(List<ExtendedProperty> extendedProperties) {
    for (ExtendedProperty extendedProperty : extendedProperties) {
      if (EXTENDED_NAME.equals(extendedProperty.getName())) {
        return extendedProperty.getText();
      }
    }
    
    return null;
  }
  
  private List<FileId> getAttachmentFileIds(OrganizationId organizationId, List<ExtendedProperty> extendedProperties) {
    List<FileId> result = new ArrayList<>();
    
    for (ExtendedProperty extendedProperty : extendedProperties) {
      String name = extendedProperty.getName();
      
      if (EXTENDED_ATTACHMENTS.equals(name) || EXTENDED_AGENDA_ATTACHMENT.equals(name)) {
        result.addAll(parseAttachmentFileIds(organizationId, extendedProperty.getText()));
      }
    }
    
    return result;
  }
  
  private List<FileId> parseAttachmentFileIds(OrganizationId organizationId, String text) {
    List<FileId> result = new ArrayList<>();
    
    Pattern pattern = Pattern.compile("(download.aspx\\?ID=)([0-9]*)\\&GUID=\\{([0-9a-zA-Z-]*)\\}");
    Matcher matcher = pattern.matcher(text);
    while (matcher.find()) {
      Long id = NumberUtils.createLong(matcher.group(2));
      String guid = matcher.group(3);
      result.add(new FileId(organizationId, CaseMConsts.IDENTIFIER_NAME, String.format("{%s}/%d", guid, id)));
    }
    
    return result;
  }
  
  private String renderContentMeeting(Meeting meeting, Locale locale) {
    return freemarkerRenderer.render("integrations/casem/content-meeting.ftlh", meeting, locale);
  }
  
  private String renderContentMeetingItem(MeetingItem meetingItem, Locale locale) {
    return freemarkerRenderer.render("integrations/casem/content-meeting-item.ftlh", meetingItem, locale);
  }

  private Meeting createMeetingModel(String downloadUrl, String meetingTitle, boolean memoApproved, List<MeetingItemLink> itemLinks, List<ExtendedProperty> extendedProperties) {
    Meeting model = new Meeting();
    List<String> attachments = new ArrayList<>();
    List<String> agendaAttachments = new ArrayList<>();
    
    for (ExtendedProperty extendedProperty : extendedProperties) {
      String name = extendedProperty.getName();
      String text = extendedProperty.getText();
      
      if (StringUtils.isBlank(text)) {
        continue;
      }
      
      switch (name) {
        case EXTENDED_PARTICIPANT_COUNCILMAN:
          model.setCouncilmen(parseCouncilmen(text));
        break;
        case EXTENDED_ATTACHMENTS:
          attachments.add(parseAttachments(downloadUrl, text));
        break;
        case EXTENDED_AGENDA_ATTACHMENT:
          agendaAttachments.add(parseAttachments(downloadUrl, text));
        break;
        default:
          logger.warning(String.format("Unrecognized extended meeting property %s", name));
        break;
      }
    }   
    
    model.setItemLinks(itemLinks);
    model.setMeetingTitle(meetingTitle);
    model.setMemoApproved(memoApproved);
    model.setAttachments(attachments);
    model.setAgendaAttachments(agendaAttachments);
    
    return model;
  }
  
  private boolean hasAttachments(List<ExtendedProperty> extendedProperties) {
    for (ExtendedProperty extendedProperty : extendedProperties) {
      String name = extendedProperty.getName();
      String text = extendedProperty.getText();
      
      if (StringUtils.isBlank(text)) {
        continue;
      }
      
      if (EXTENDED_ATTACHMENTS.equals(name)) {
        return true;
      }
    }
    
    return false;
  }
  
  private MeetingItem createMeetingItemModel(String downloadUrl, String meetingTitle, boolean memoApproved, List<ExtendedProperty> extendedProperties) {
    MeetingItem model = new MeetingItem();

    for (ExtendedProperty extendedProperty : extendedProperties) {
      String name = extendedProperty.getName();
      String text = extendedProperty.getText();
      
      if (StringUtils.isBlank(text)) {
        continue;
      }
      
      mapMeetingItemProperty(downloadUrl, model, name, text);
    }          
    
    model.setMeetingTitle(meetingTitle);
    model.setMemoApproved(memoApproved);
    
    return model;
  }

  @SuppressWarnings ("squid:MethodCyclomaticComplexity")
  private void mapMeetingItemProperty(String downloadUrl, MeetingItem model, String name, String text) {
    switch (name) {
      case EXTENDED_DRAFTING_NOTES:
        model.setDraftingNotes(text);
      break;
      case EXTENDED_DESCRIPTION:
        model.setDescription(text);
      break;
      case EXTENDED_PRESENTERS:
        model.setPresenters(parsePresenters(text));
      break;
      case EXTENDED_INFORM:
        model.setInform(text);
      break;
      case EXTENDED_ARTICLE:
        model.setArticle(text);
      break;
      case EXTENDED_DRAFTSMANS:
        model.setDraftsmen(parsePresenters(text));
      break;
      case EXTENDED_ATTACHMENTS:
        if (model.getAttachments() == null) {
          model.setAttachments(new ArrayList<>());
        }
        model.getAttachments().add(parseAttachments(downloadUrl, text));
      break;
      case EXTENDED_CASE_NATIVE_ID:
        model.setCaseNativeId(text);
      break;
      case EXTENDED_DECISION:
        model.setDecision(text);
      break;
      case EXTENDED_NAME:
        model.setName(text);
      break;
      case EXTENDED_DECISION_NOTES:
        model.setDecisionNotes(text);
      break;
      case EXTENDED_DECISIONPROPOSAL:
        model.setDecisionProposal(text);
      break;
      case EXTENDED_CORRECTIONINSTRUCTIONS:
        model.setCorrectioninstructions(text);
      break;
      case EXTENDED_IS_ADDITIONAL_TOPIC:
        model.setAdditionalTopic("True".equals(text));
      break;
      case EXTENDED_HISTORY_TOPICS:
        model.setHistoryTopics(parseHistoryTopics(text));
      break;
      default:
        logger.warning(String.format("Unrecognized extended meeting item property %s", name));
      break;
    }
  }
  
  private String parseAttachments(String downloadUrl, String text) {
    String result = text;
    Pattern pattern = Pattern.compile("(download.aspx\\?ID=)([0-9]*)\\&GUID=\\{([0-9a-zA-Z-]*)\\}");
    Matcher matcher = pattern.matcher(result);
    while (matcher.find()) {
      Long id = NumberUtils.createLong(matcher.group(2));
      String guid = matcher.group(3);
      String url = String.format("%s/{%s}/%d", downloadUrl, guid, id);
      result = matcher.replaceFirst(url);
      matcher = pattern.matcher(result);
    }
    
    return result;
  }
  
  private List<ExtendedProperty> listExtendedProperties(OrganizationId organizationId, Content content) {
    ApiResponse<ExtendedPropertyList> propertiesResult = caseMApi.getContentsApi(organizationId).listExtendedPropertiesByContent(content.getContentId(), null);
    if (!propertiesResult.isOk()) {
      logger.severe(String.format("Extended property listing failed on [%d] %s", propertiesResult.getStatus(), propertiesResult.getMessage()));
      return Collections.emptyList();
    }
    
    return propertiesResult.getResponse().getValue();
  }
  
  private List<Participant> parsePresenters(String text) {
    List<Participant> result = new ArrayList<>();
    
    List<String[]> lines = parseCaseMCSV(text);
    for (String[] line : lines) {
      if (line.length > 0) {
        Participant participant = new Participant();
        participant.setName(line[0]);
        if (line.length > 1) {
          participant.setTitle(line[1]);
        }

        if (line.length > 2) {
          participant.setEmail(line[2]);
        }
        
        result.add(participant);
      }
    }
    
    return result;
  }

  private Councilmen parseCouncilmen(String text) {
    Councilmen result = new Councilmen();
    
    List<String[]> lines = parseCaseMCSV(text);
    for (String[] line : lines) {
      if (line.length > 5) {
        logger.severe(String.format("Unexpected cell count %d", line.length));
      } else {
        parseCouncilman(result, line);
      }
    }
    
    return result;
  }

  private void parseCouncilman(Councilmen result, String[] line) {
    Participant participant = new Participant();
  
    participant.setName(line[0]);
    String group = GROUP_OTHER;
    
    if (line.length > 1) {
      participant.setTitle(line[1]);
      group = resolveCouncilmanAttributes(line, participant);
    }

    addCouncilman(result, participant, group);
  }

  private String resolveCouncilmanAttributes(String[] line, Participant participant) {
    String group = GROUP_OTHER;
    
    if (line.length > 2) {
      group = line[line.length - 1];
    }        
    
    if (line.length == 3) {
      if (isDateField(line[2])) {
        participant.setArrived(parseCSVDateTime(line[2]));
        group = GROUP_OTHER;
      }
    } else if (line.length == 4) {
      if (isDateField(line[2])) {
        participant.setArrived(parseCSVDateTime(line[2]));
      }
      
      if (isDateField(line[3])) {
        participant.setLeft(parseCSVDateTime(line[3]));
        group = GROUP_OTHER;
      }
    } else if (line.length == 5) {
      if (isDateField(line[2])) {
        participant.setArrived(parseCSVDateTime(line[2]));
      }
      
      if (isDateField(line[3])) {
        participant.setLeft(parseCSVDateTime(line[3]));
      }
    }
    
    return group;
  }

  private void addCouncilman(Councilmen result, Participant participant, String group) {
    switch (group) {
      case GROUP_MEMBER:
        result.addMember(participant);
      break;
      case GROUP_OTHER:
        result.addOther(participant);
      break;
      case GROUP_VARAJASEN:
        result.addAway(participant);
      break;
      default:
        logger.severe(String.format("Unrecognized participant group %s", group));
      break;
    }
  }
  
  @SuppressWarnings ("squid:S1166")
  private boolean isDateField(String text) {
    try {
      if (parseCSVDateTime(text) != null) {
        return true;
      }
    } catch (Exception e) {
      // parser throws an error on invalid dates
    }
    
    return false;
  }

  private LocalDateTime parseCSVDateTime(String text) {
    if (StringUtils.isBlank(text)) {
      return null;
    }
    
    return LocalDateTime.parse(text, CSV_DATE_TIME);
  }

  private List<String[]> parseCaseMCSV(String text) {
    List<String[]> result = new ArrayList<>();
    
    for (String line : StringUtils.splitByWholeSeparator(StringUtils.stripEnd(text, "#"), "!#")) {
      String[] values = StringUtils.splitByWholeSeparator(line, ";#");
      result.add(ArrayUtils.subarray(values, 0, values.length - 1));
    }
    
    return result;
  }
  
  private List<HistoryTopic> parseHistoryTopics(String text) {
    ObjectMapper objectMapper = new ObjectMapper();
    try {
      return objectMapper.readValue(text, new TypeReference<List<HistoryTopic>>() {});
    } catch (IOException e) {
      logger.log(Level.SEVERE, String.format("Failed to parse history topics from text %s", text), e);
    }
    
    return Collections.emptyList();
  }

  private boolean isMeetingPage(Content content) {
    List<ContentClassification> classifications = content.getClassifications();
    if (classifications == null || classifications.isEmpty()) {
      logger.severe(String.format("Skipped contents node %d because it had no classifications", content.getContentId()));
      return false;
    }
    
    for (ContentClassification classification : classifications) {
      if (classification.getType().equals(43l)) {
        return true;
      }
    }
    
    return false;
  }

  private Long getContentNodeId(Content content) {
    List<ContentClassification> classifications = content.getClassifications();
    if (classifications == null || classifications.isEmpty()) {
      logger.severe(String.format("Skipped contents node %d because it had no classifications", content.getContentId()));
      return null;
    }
    
    return classifications.get(classifications.size() - 1).getNodeId();
  }

  private List<Content> getContents(OrganizationId organizationId) {
    List<Content> result = new ArrayList<>();
    Long skipToken = null;
    
    do {
      ContentList contentList = getContentList(organizationId, skipToken);
      if (contentList == null) {
        break;
      }
      
      result.addAll(contentList.getValue());
      
      skipToken = getSkipToken(contentList.getOdataNextLink());
    } while (skipToken != null);
    
    return result;
  }
  
  private ContentList getContentList(OrganizationId organizationId, Long skipToken) {
    ApiResponse<ContentList> response = caseMApi.getContentsApi(organizationId).listContents(skipToken != null ? String.valueOf(skipToken) : null);
    if (!response.isOk()) {
      logger.severe(String.format("Listing contents failed on [%d] %s", response.getStatus(), response.getMessage()));
      return null;
    } else {
      return response.getResponse();
    }
  }

  private void cacheNodeTree(OrganizationId organizationId, Long caseMRootNodeId, Node parentNode, List<Node> nodes, List<Long> caseMParentIds) {
    for (int i = 0; i < nodes.size(); i++) {
      Node node = nodes.get(i);
      Long orderIndex = (long) i;
      
      List<Long> childCaseMParentIds = new ArrayList<>(caseMParentIds);
      childCaseMParentIds.add(node.getNodeId());
      
      if (node.getType().equals(0l)) {
        // Type 0 pages are hidden levels that should be flatted out
      } else {
        Page page = translateNode(orderIndex, organizationId, caseMRootNodeId, parentNode, node);
        caseMCache.cacheNode(organizationId, page);
      }
      
      cacheNodeTree(organizationId, caseMRootNodeId, node, getChildNodes(organizationId, caseMRootNodeId, childCaseMParentIds), childCaseMParentIds);
    }
  }
  
  private Long getCaseMRootNodeId(OrganizationId organizationId) {
    String rootNode = organizationSettingController.getSettingValue(organizationId, CaseMConsts.ORGANIZATION_SETTING_ROOT_NODE);
    if (StringUtils.isNumeric(rootNode)) {
      return NumberUtils.createLong(rootNode);
    }
    
   return null;
  }
  
  private String getCaseMDownloadUrl(OrganizationId organizationId) {
    String baseUrl = organizationSettingController.getSettingValue(organizationId, CaseMConsts.ORGANIZATION_SETTING_BASEURL);
    String path = organizationSettingController.getSettingValue(organizationId, CaseMConsts.ORGANIZATION_SETTING_DOWNLOAD_PATH);
    return String.format("%s/%s", baseUrl, path);
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
  
  private Page translateNode(Long orderIndex, OrganizationId organizationId, Long caseMRootNodeId, Node parentNode, Node node) {
    Page page = new Page();
    PageId kuntaApiId = translatePageId(orderIndex, toNodeId(organizationId, node.getNodeId()), true);
    PageId kuntaApiParentId = null;
    
    if (parentNode != null && !parentNode.getNodeId().equals(caseMRootNodeId)) {
      if (parentNode.getType().equals(0l)) {
        kuntaApiParentId = parentNode.getParentId() != null ? translatePageId(toNodeId(organizationId, parentNode.getParentId())) : null;
      } else {
        kuntaApiParentId = translatePageId(toNodeId(organizationId, parentNode.getNodeId()));
      }
    }
    
    if (kuntaApiParentId == null) {
      kuntaApiParentId = resolveRootFolderId(organizationId);
    }
    
    List<LocalizedValue> titles = translateNodeNames(organizationId, node.getNames());
    page.setId(kuntaApiId.getId());
    page.setParentId(kuntaApiParentId != null ? kuntaApiParentId.getId() : null);
    page.setTitles(titles);
    page.setSlug(slugify(titles.isEmpty() ? kuntaApiId.getId() : titles.get(0).getValue()));
    
    return page;
  }
  
  private PageId resolveRootFolderId(OrganizationId organizationId) {
    String path = organizationSettingController.getSettingValue(organizationId, CaseMConsts.ORGANIZATION_SETTING_ROOT_FOLDER);
    if (StringUtils.isNotBlank(path)) {
      List<Page> pages = pageController.listPages(organizationId, path, false, null, 0l, 1l);
      if (!pages.isEmpty()) {
        return new PageId(organizationId, KuntaApiConsts.IDENTIFIER_NAME, pages.get(0).getId());
      }
    }
    
    return null;
  }

  private Page translateContent(Long orderIndex, OrganizationId organizationId, Page parentPage, Content content, String title, String slug) {
    Page page = new Page();
    
    page.setId(translatePageId(orderIndex, toContentId(organizationId, content.getContentId()), true).getId());
    page.setParentId(parentPage.getId());
    page.setTitles(toTitles(title));
    page.setSlug(slug);

    return page;
  }

  private PageId translatePageId(PageId pageId) {
    return translatePageId(null, pageId, false);
  }
  
  private PageId translatePageId(Long orderIndex, PageId pageId, boolean createMissing) {
    PageId result = discoveredPageIds.get(pageId);
    if (result != null) {
      return result;
    }
    
    result = idController.translatePageId(pageId, KuntaApiConsts.IDENTIFIER_NAME);
    if (result != null) {
      return result;
    }
    
    if (createMissing) {
      Identifier identifier = identifierController.createIdentifier(orderIndex, pageId);
      result = new PageId(pageId.getOrganizationId(), KuntaApiConsts.IDENTIFIER_NAME, identifier.getKuntaApiId());
      discoveredPageIds.put(pageId, result);
    }
    
    return result;
  }
  
  private PageId toNodeId(OrganizationId organizationId, Long nodeId) {
    return new PageId(organizationId, CaseMConsts.IDENTIFIER_NAME, String.format("NODE|%d", nodeId));
  }
  
  private PageId toContentId(OrganizationId organizationId, Long contentId) {
    return new PageId(organizationId, CaseMConsts.IDENTIFIER_NAME, String.format("CONTENT|%d", contentId));
  }
  

  private String slugify(String title) {
    return slugify(title, MAX_SLUG_LENGTH );
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
  
  private List<LocalizedValue> toTitles(String title) {
    LocalizedValue localizedValue = new LocalizedValue();
    localizedValue.setLanguage(CaseMConsts.DEFAULT_LANGUAGE);
    localizedValue.setValue(title);
    return Collections.singletonList(localizedValue);
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
  
  private IndexablePage createIndexablePage(OrganizationId organizationId, PageId pageId, String language, String content, String title) {
    OrganizationId kuntaApiOrganizationId = idController.translateOrganizationId(organizationId, KuntaApiConsts.IDENTIFIER_NAME);
    if (kuntaApiOrganizationId == null) {
      logger.severe(String.format("Failed to translate organizationId %s into KuntaAPI id", organizationId.toString()));
      return null;
    }
    
    PageId kuntaApiPageId = translatePageId(pageId);
    if (kuntaApiPageId == null) {
      logger.severe(String.format("Failed to translate pageId %s into KuntaAPI id", pageId.toString()));
      return null;
    }
    
    IndexablePage indexablePage = new IndexablePage();
    indexablePage.setContent(content);
    indexablePage.setLanguage(language);
    indexablePage.setOrganizationId(kuntaApiOrganizationId.getId());
    indexablePage.setPageId(kuntaApiPageId.getId());
    indexablePage.setTitle(title);
    
    return indexablePage;
  }
  
  private class NodeComparator implements Comparator<Node> {
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
  
}

  
  