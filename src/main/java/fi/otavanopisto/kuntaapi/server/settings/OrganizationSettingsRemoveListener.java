package fi.otavanopisto.kuntaapi.server.settings;

import java.util.List;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.discover.OrganizationIdRemoveRequest;
import fi.otavanopisto.kuntaapi.server.persistence.model.OrganizationSetting;

@ApplicationScoped
public class OrganizationSettingsRemoveListener {
  
  @Inject
  private OrganizationSettingController organizationSettingController;

  public void onOrganizationIdRemoveRequest(@Observes OrganizationIdRemoveRequest event) {
    List<OrganizationSetting> organizationSettings = organizationSettingController.listOrganizationSettings(event.getId());
    for (OrganizationSetting organizationSetting : organizationSettings) {
      organizationSettingController.deleteOrganizationSetting(organizationSetting);
    }
  }
  

}
