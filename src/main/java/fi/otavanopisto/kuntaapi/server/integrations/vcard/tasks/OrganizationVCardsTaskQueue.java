package fi.otavanopisto.kuntaapi.server.integrations.vcard.tasks;

import javax.enterprise.context.ApplicationScoped;

import fi.otavanopisto.kuntaapi.server.integrations.vcard.VCardConsts;
import fi.otavanopisto.kuntaapi.server.tasks.AbstractOrganizationEntityTaskQueue;

@ApplicationScoped
public class OrganizationVCardsTaskQueue extends AbstractOrganizationEntityTaskQueue {

  @Override
  public String getSource() {
    return VCardConsts.IDENTIFIER_NAME;
  }

  @Override
  public String getEntity() {
    return "vcards";
  }
  
}