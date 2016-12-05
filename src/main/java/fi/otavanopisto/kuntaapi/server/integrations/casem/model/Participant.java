package fi.otavanopisto.kuntaapi.server.integrations.casem.model;

import java.time.LocalDateTime;

public class Participant {

  private String name;
  private String title;
  private String email;
  private LocalDateTime arrived;
  private LocalDateTime left;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }
  
  public String getEmail() {
    return email;
  }
  
  public void setEmail(String email) {
    this.email = email;
  }

  public LocalDateTime getArrived() {
    return arrived;
  }

  public void setArrived(LocalDateTime arrived) {
    this.arrived = arrived;
  }

  public LocalDateTime getLeft() {
    return left;
  }

  public void setLeft(LocalDateTime left) {
    this.left = left;
  }

}
