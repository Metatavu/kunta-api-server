package fi.otavanopisto.kuntaapi.server.integrations.casem.model;

import java.util.List;

import fi.otavanopisto.kuntaapi.server.freemarker.FormatDateTimeMethodModel;

public class MeetingItem {

  private String meetingTitle;
  private boolean memoApproved;
  private String article;
  private String name;
  private List<Participant> draftsmen;
  private String caseNativeId;
  private boolean additionalTopic;
  private String description;
  private String decisionProposal;
  private List<Participant> presenters;
  private String draftingNotes;
  private String decisionNotes;
  private String decision;
  private String inform;
  private List<String> attachments;
  private String correctioninstructions;
  private String agendaAttachment;
  private List<HistoryTopic> historyTopics;
  private FormatDateTimeMethodModel formatDateTime = new FormatDateTimeMethodModel();
  
  public String getMeetingTitle() {
    return meetingTitle;
  }
  
  public void setMeetingTitle(String meetingTitle) {
    this.meetingTitle = meetingTitle;
  }
  
  public boolean getMemoApproved() {
    return memoApproved;
  }
  
  public void setMemoApproved(boolean memoApproved) {
    this.memoApproved = memoApproved;
  }
  
  public String getArticle() {
    return article;
  }

  public void setArticle(String article) {
    this.article = article;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<Participant> getDraftsmen() {
    return draftsmen;
  }
  
  public void setDraftsmen(List<Participant> draftsmen) {
    this.draftsmen = draftsmen;
  }

  public String getCaseNativeId() {
    return caseNativeId;
  }

  public void setCaseNativeId(String caseNativeId) {
    this.caseNativeId = caseNativeId;
  }

  public boolean isAdditionalTopic() {
    return additionalTopic;
  }

  public void setAdditionalTopic(boolean additionalTopic) {
    this.additionalTopic = additionalTopic;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getDecisionProposal() {
    return decisionProposal;
  }

  public void setDecisionProposal(String decisionProposal) {
    this.decisionProposal = decisionProposal;
  }
  
  public List<Participant> getPresenters() {
    return presenters;
  }
  
  public void setPresenters(List<Participant> presenters) {
    this.presenters = presenters;
  }
  
  public String getDraftingNotes() {
    return draftingNotes;
  }

  public void setDraftingNotes(String draftingNotes) {
    this.draftingNotes = draftingNotes;
  }

  public String getDecisionNotes() {
    return decisionNotes;
  }

  public void setDecisionNotes(String decisionNotes) {
    this.decisionNotes = decisionNotes;
  }

  public String getDecision() {
    return decision;
  }

  public void setDecision(String decision) {
    this.decision = decision;
  }

  public String getInform() {
    return inform;
  }

  public void setInform(String inform) {
    this.inform = inform;
  }

  public List<String> getAttachments() {
    return attachments;
  }

  public void setAttachments(List<String> attachments) {
    this.attachments = attachments;
  }

  public String getCorrectioninstructions() {
    return correctioninstructions;
  }

  public void setCorrectioninstructions(String correctioninstructions) {
    this.correctioninstructions = correctioninstructions;
  }

  public String getAgendaAttachment() {
    return agendaAttachment;
  }

  public void setAgendaAttachment(String agendaAttachment) {
    this.agendaAttachment = agendaAttachment;
  }
  
  public List<HistoryTopic> getHistoryTopics() {
    return historyTopics;
  }
  
  public void setHistoryTopics(List<HistoryTopic> historyTopics) {
    this.historyTopics = historyTopics;
  }
  
  public FormatDateTimeMethodModel getFormatDateTime() {
    return formatDateTime;
  }

}
