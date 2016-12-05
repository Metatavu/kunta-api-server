package fi.otavanopisto.kuntaapi.server.integrations.casem.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Person {

  @JsonProperty("Name")
  private String name;

  @JsonProperty("Title")
  private String title;

  @JsonProperty("Phone")
  private String phone;

  @JsonProperty("Email")
  private String email;

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

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

}
