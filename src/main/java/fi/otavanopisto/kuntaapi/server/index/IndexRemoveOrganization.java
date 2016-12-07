package fi.otavanopisto.kuntaapi.server.index;

public class IndexRemoveOrganization implements IndexRemove {

  private String organizationId;

  private String language;

  @Override
  public String getId() {
    return String.format("%s_%s", organizationId, language);
  }

  @Override
  public String getType() {
    return "organization";
  }

  public String getOrganizationId() {
    return organizationId;
  }

  public void setOrganizationId(String organizationId) {
    this.organizationId = organizationId;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

}
