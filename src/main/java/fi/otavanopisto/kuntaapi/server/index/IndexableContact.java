package fi.otavanopisto.kuntaapi.server.index;

import java.util.List;

public class IndexableContact implements Indexable {

  @Field(index = "not_analyzed", store = true, type = "long")
  private Long orderIndex;

  @Field(index = "not_analyzed", store = true)
  private String contactId;
  
  @Field (index = "not_analyzed", store = true)
  private String organizationId;

  private String displayName;

  @Field (index = "not_analyzed", store = true)
  private String displayNameUT;

  private String firstName;

  private String lastName;

  private String title;

  private String organization;

  private List<String> organizationUnits;

  private List<String> additionalInformations;

  private List<String> emails;

  private List<String> phoneNumbers;
  
  @Field (index = "not_analyzed", store = true)
  private Boolean privateContact;

  @Override
  public String getId() {
    return contactId;
  }

  @Override
  public String getType() {
    return "contact";
  }

  @Override
  public Long getOrderIndex() {
    return orderIndex;
  }

  public void setOrderIndex(Long orderIndex) {
    this.orderIndex = orderIndex;
  }

  public String getContactId() {
    return contactId;
  }

  public void setContactId(String contactId) {
    this.contactId = contactId;
  }
  
  public String getOrganizationId() {
    return organizationId;
  }
  
  public void setOrganizationId(String organizationId) {
    this.organizationId = organizationId;
  }

  public String getDisplayName() {
    return displayName;
  }

  public void setDisplayName(String displayName) {
    this.displayName = displayName;
    this.displayNameUT = displayName;
  }
  
  public String getDisplayNameUT() {
    return displayNameUT;
  }
  
  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getOrganization() {
    return organization;
  }

  public void setOrganization(String organization) {
    this.organization = organization;
  }

  public List<String> getOrganizationUnits() {
    return organizationUnits;
  }

  public void setOrganizationUnits(List<String> organizationUnits) {
    this.organizationUnits = organizationUnits;
  }

  public List<String> getAdditionalInformations() {
    return additionalInformations;
  }

  public void setAdditionalInformations(List<String> additionalInformations) {
    this.additionalInformations = additionalInformations;
  }

  public List<String> getEmails() {
    return emails;
  }

  public void setEmails(List<String> emails) {
    this.emails = emails;
  }

  public List<String> getPhoneNumbers() {
    return phoneNumbers;
  }

  public void setPhoneNumbers(List<String> phoneNumbers) {
    this.phoneNumbers = phoneNumbers;
  }
  
  public Boolean getPrivateContact() {
    return privateContact;
  }
  
  public void setPrivateContact(Boolean privateContact) {
    this.privateContact = privateContact;
  }

}
