package fi.otavanopisto.kuntaapi.server.rest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.ejb.Stateful;
import javax.enterprise.context.RequestScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.lang3.StringUtils;

import fi.otavanopisto.kuntaapi.server.id.ServiceId;
import fi.otavanopisto.kuntaapi.server.integrations.KuntaApiConsts;
import fi.otavanopisto.kuntaapi.server.integrations.ServiceProvider;
import fi.otavanopisto.kuntaapi.server.rest.model.ElectronicChannel;
import fi.otavanopisto.kuntaapi.server.rest.model.PhoneChannel;
import fi.otavanopisto.kuntaapi.server.rest.model.PrintableFormChannel;
import fi.otavanopisto.kuntaapi.server.rest.model.Service;
import fi.otavanopisto.kuntaapi.server.rest.model.ServiceLocationChannel;
import fi.otavanopisto.kuntaapi.server.rest.model.WebPageChannel;

/**
 * REST Service implementation
 * 
 * @author Antti Leppä
 */
@RequestScoped
@Stateful
@SuppressWarnings ("squid:S3306")
public class ServicesApiImpl extends ServicesApi {
  
  private static final String NOT_IMPLEMENTED = "Not implemented";
  
  @Inject
  private Instance<ServiceProvider> serviceProviders;
  
  @Override
  public Response createService(Service body) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }
  
  @Override
  public Response findService(String serviceIdParam) {
    ServiceId serviceId = toServiceId(serviceIdParam);
    
    for (ServiceProvider serviceProvider : getServiceProviders()) {
      Service service = serviceProvider.findService(serviceId);
      if (service != null) {
        return Response.ok(service)
          .build();
      }
    }
    
    return Response
        .status(Status.NOT_FOUND)
        .build();
  }
  
  @Override
  public Response listServices(Long firstResult, Long maxResults) {
    List<Service> result = new ArrayList<>();
    
    // TODO: Filters
    
    for (ServiceProvider serviceProvider : getServiceProviders()) {
      result.addAll(serviceProvider.listServices(null, null));
    }
    
    return Response.ok(result)
      .build();
  }
  
  @Override
  public Response updateService(String serviceId, Service body) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }
  
  @Override
  public Response createServiceElectronicChannel(String serviceId, ElectronicChannel body) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }

  @Override
  public Response createServicePhoneChannel(String serviceId, PhoneChannel body) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }

  @Override
  public Response createServicePrintableFormChannel(String serviceId, PrintableFormChannel body) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }

  @Override
  public Response createServiceServiceLocationChannel(String serviceId, ServiceLocationChannel body) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }

  @Override
  public Response createServiceWebPageChannel(String serviceId, WebPageChannel body) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }

  @Override
  public Response findServiceElectronicChannel(String serviceId, String electronicChannelId) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }

  @Override
  public Response findServicePhoneChannel(String serviceId, String phoneChannelId) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }

  @Override
  public Response findServicePrintableFormChannel(String serviceId, String printableFormChannelId) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }

  @Override
  public Response findServiceServiceLocationChannel(String serviceId, String serviceLocationChannelId) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }

  @Override
  public Response findServiceWebPageChannel(String serviceId, String webPageChannelId) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }

  @Override
  public Response listServiceElectronicChannels(String serviceId, Long firstResult, Long maxResults) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }

  @Override
  public Response listServicePhoneChannels(String serviceId, Long firstResult, Long maxResults) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }

  @Override
  public Response listServicePrintableFormChannels(String serviceId, Long firstResult, Long maxResults) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }

  @Override
  public Response listServiceServiceLocationChannels(String serviceId, Long firstResult, Long maxResults) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }

  @Override
  public Response listServiceWebPageChannels(String serviceId, Long firstResult, Long maxResults) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }

  @Override
  public Response updatePhoneChannel(String serviceId, String phoneChannelId, PhoneChannel body) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }

  @Override
  public Response updatePrintableFormChannel(String serviceId, String printableFormChannelId,
      PrintableFormChannel body) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }

  @Override
  public Response updateServiceElectronicChannel(String serviceId, String electronicChannelId, ElectronicChannel body) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }

  @Override
  public Response updateServiceLocationChannel(String serviceId, String serviceLocationChannelId,
      ServiceLocationChannel body) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }

  @Override
  public Response updateWebPageChannel(String serviceId, String webPageChannelId, WebPageChannel body) {
    return createNotImplemented(NOT_IMPLEMENTED);
  }
  
  private ServiceId toServiceId(String id) {
    if (StringUtils.isNotBlank(id)) {
      return new ServiceId(KuntaApiConsts.IDENTIFIER_NAME, id);
    }
    
    return null;
  }
  
  private List<ServiceProvider> getServiceProviders() {
    List<ServiceProvider> result = new ArrayList<>();
    
    Iterator<ServiceProvider> iterator = serviceProviders.iterator();
    while (iterator.hasNext()) {
      result.add(iterator.next());
    }
    
    return Collections.unmodifiableList(result);
  }

}

