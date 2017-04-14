package fi.otavanopisto.kuntaapi.server.integrations.ptv.servicechannels;

import java.util.ArrayList;
import java.util.List;

public class ServiceChannelIds {

  private List<String> electricChannels = new ArrayList<>();
  private List<String> locationServiceChannels = new ArrayList<>();
  private List<String> phoneChannels = new ArrayList<>();
  private List<String> printableFormChannels = new ArrayList<>();
  private List<String> webPageChannels = new ArrayList<>();

  public List<String> getElectricChannels() {
    return electricChannels;
  }

  public void setElectricChannels(List<String> electricChannels) {
    this.electricChannels = electricChannels;
  }

  public List<String> getLocationServiceChannels() {
    return locationServiceChannels;
  }

  public void setLocationServiceChannels(List<String> locationServiceChannels) {
    this.locationServiceChannels = locationServiceChannels;
  }

  public List<String> getPhoneChannels() {
    return phoneChannels;
  }

  public void setPhoneChannels(List<String> phoneChannels) {
    this.phoneChannels = phoneChannels;
  }

  public List<String> getPrintableFormChannels() {
    return printableFormChannels;
  }

  public void setPrintableFormChannels(List<String> printableFormChannels) {
    this.printableFormChannels = printableFormChannels;
  }

  public List<String> getWebPageChannels() {
    return webPageChannels;
  }

  public void setWebPageChannels(List<String> webPageChannels) {
    this.webPageChannels = webPageChannels;
  }

}
