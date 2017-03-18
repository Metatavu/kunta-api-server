package fi.otavanopisto.kuntaapi.server.integrations.casem.model;

import java.time.LocalDateTime;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

@JsonIgnoreProperties (ignoreUnknown = true)
public class HistoryTopic {

  @JsonProperty("Description")
  private String description;

  @JsonProperty("DescisionProposal")
  private String decisionProposal;

  @JsonProperty("Descision")
  private String decision;

  @JsonProperty("Party")
  private String party;

  @JsonProperty("PartySv")
  private String partySv;
  
  @JsonProperty("Article")
  private Integer article;

  @JsonProperty("MeetingDate")
  @JsonDeserialize (using = CaseMJSONDateDeserializer.class)
  private LocalDateTime meetingDate;

  @JsonProperty("Draftsmen")
  private List<Person> draftsmen;

  @JsonProperty("Presenters")
  private List<Person> presenters;

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

  public String getDecision() {
    return decision;
  }
  
  public void setDecision(String decision) {
    this.decision = decision;
  }

  public String getParty() {
    return party;
  }

  public void setParty(String party) {
    this.party = party;
  }

  public String getPartySv() {
    return partySv;
  }

  public void setPartySv(String partySv) {
    this.partySv = partySv;
  }
 
  public Integer getArticle() {
    return article;
  }

  public void setArticle(Integer article) {
    this.article = article;
  }

  public LocalDateTime getMeetingDate() {
    return meetingDate;
  }
  
  public void setMeetingDate(LocalDateTime meetingDate) {
    this.meetingDate = meetingDate;
  }
  
  public List<Person> getDraftsmen() {
    return draftsmen;
  }

  public void setDraftsmen(List<Person> draftsmen) {
    this.draftsmen = draftsmen;
  }

  public List<Person> getPresenters() {
    return presenters;
  }

  public void setPresenters(List<Person> presenters) {
    this.presenters = presenters;
  }

}
