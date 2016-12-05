package fi.otavanopisto.kuntaapi.server.integrations.casem.model;

import java.util.ArrayList;
import java.util.List;

public class Councilmen {

  private List<Participant> members;
  private List<Participant> others;
  private List<Participant> away;
  
  public Councilmen() {
    members = new ArrayList<>();
    others = new ArrayList<>();
    away = new ArrayList<>();
  }
  
  public List<Participant> getMembers() {
    return members;
  }
  
  public void setMembers(List<Participant> members) {
    this.members = members;
  }
  
  public List<Participant> getOthers() {
    return others;
  }
  
  public void setOthers(List<Participant> others) {
    this.others = others;
  }
  
  public List<Participant> getAway() {
    return away;
  }
  
  public void setAway(List<Participant> away) {
    this.away = away;
  }

  public void addMember(Participant participant) {
    members.add(participant);
  }

  public void addOther(Participant participant) {
    others.add(participant);
  }

  public void addAway(Participant participant) {
    away.add(participant);
  }
}
