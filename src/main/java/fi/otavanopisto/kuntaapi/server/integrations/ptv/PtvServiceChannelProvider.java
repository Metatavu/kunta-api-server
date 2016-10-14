package fi.otavanopisto.kuntaapi.server.integrations.ptv;

import java.util.List;
import java.util.logging.Logger;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import fi.otavanopisto.kuntaapi.server.controllers.IdentifierController;
import fi.otavanopisto.kuntaapi.server.integrations.IdController;
import fi.otavanopisto.kuntaapi.server.integrations.OrganizationId;
import fi.otavanopisto.kuntaapi.server.integrations.ServiceChannelProvider;
import fi.otavanopisto.kuntaapi.server.integrations.ServiceId;
import fi.otavanopisto.kuntaapi.server.rest.model.ElectronicChannel;
import fi.otavanopisto.kuntaapi.server.rest.model.PhoneChannel;
import fi.otavanopisto.kuntaapi.server.rest.model.PrintableFormChannel;
import fi.otavanopisto.kuntaapi.server.rest.model.ServiceLocationChannel;
import fi.otavanopisto.kuntaapi.server.rest.model.WebPageChannel;

/**
 * Service channel provider for PTV
 * 
 * @author Antti Leppä
 * @author Heikki Kurhinen
 */
@Dependent
public class PtvServiceChannelProvider extends AbstractPtvProvider implements ServiceChannelProvider {

  @Inject
  private Logger logger;
  
  @Inject
  private PtvApi ptvApi;
  
  @Inject
  private IdController idController;

  @Inject
  private IdentifierController identifierController;
  
  private PtvServiceChannelProvider() {
  }

  @Override
  public List<ElectronicChannel> listElectronicChannels(OrganizationId organizationId, ServiceId serviceId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<PhoneChannel> listPhoneChannels(OrganizationId organizationId, ServiceId serviceId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<PrintableFormChannel> listPrintableFormChannels(OrganizationId organizationId, ServiceId serviceId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<ServiceLocationChannel> listServiceLocationChannels(OrganizationId organizationId, ServiceId serviceId) {
    // TODO Auto-generated method stub
    return null;
  }

  @Override
  public List<WebPageChannel> listWebPageChannelsChannels(OrganizationId organizationId, ServiceId serviceId) {
    // TODO Auto-generated method stub
    return null;
  }
  
}
