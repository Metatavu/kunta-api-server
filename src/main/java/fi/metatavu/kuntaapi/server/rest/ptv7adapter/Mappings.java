package fi.metatavu.kuntaapi.server.rest.ptv7adapter;

import java.util.Map;

public final class Mappings {

  private Map<String, Map<String, String>> printableFormServiceChannel;
  private Map<String, Map<String, String>> webPageServiceChannel;
  private Map<String, Map<String, String>> service;
  private Map<String, Map<String, String>> organization;
  private Map<String, Map<String, String>> phoneServiceChannel;
  private Map<String, Map<String, String>> serviceLocationServiceChannel;
  private Map<String, Map<String, String>> electoricServiceChannel;

  public Map<String, Map<String, String>> getPrintableFormServiceChannel() {
    return printableFormServiceChannel;
  }

  public void setPrintableFormServiceChannel(Map<String, Map<String, String>> printableFormServiceChannel) {
    this.printableFormServiceChannel = printableFormServiceChannel;
  }

  public Map<String, Map<String, String>> getWebPageServiceChannel() {
    return webPageServiceChannel;
  }

  public void setWebPageServiceChannel(Map<String, Map<String, String>> webPageServiceChannel) {
    this.webPageServiceChannel = webPageServiceChannel;
  }

  public Map<String, Map<String, String>> getService() {
    return service;
  }

  public void setService(Map<String, Map<String, String>> service) {
    this.service = service;
  }

  public Map<String, Map<String, String>> getOrganization() {
    return organization;
  }

  public void setOrganization(Map<String, Map<String, String>> organization) {
    this.organization = organization;
  }

  public Map<String, Map<String, String>> getPhoneServiceChannel() {
    return phoneServiceChannel;
  }

  public void setPhoneServiceChannel(Map<String, Map<String, String>> phoneServiceChannel) {
    this.phoneServiceChannel = phoneServiceChannel;
  }

  public Map<String, Map<String, String>> getServiceLocationServiceChannel() {
    return serviceLocationServiceChannel;
  }

  public void setServiceLocationServiceChannel(Map<String, Map<String, String>> serviceLocationServiceChannel) {
    this.serviceLocationServiceChannel = serviceLocationServiceChannel;
  }

  public Map<String, Map<String, String>> getElectoricServiceChannel() {
    return electoricServiceChannel;
  }

  public void setElectoricServiceChannel(Map<String, Map<String, String>> electoricServiceChannel) {
    this.electoricServiceChannel = electoricServiceChannel;
  }

}