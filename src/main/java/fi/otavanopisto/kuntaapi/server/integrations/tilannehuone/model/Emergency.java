package fi.otavanopisto.kuntaapi.server.integrations.tilannehuone.model;

import java.io.Serializable;
import java.time.OffsetDateTime;
import java.util.List;

@SuppressWarnings ("squid:S3437")
public class Emergency implements Serializable {

  private static final long serialVersionUID = -3555320340660801293L;

  private String id;
  private String url;
  private String location;
  private String area;
  private OffsetDateTime time;
  private String description;
  private String extent;
  private String type;
  private List<EmergencySource> sources;
  private String latitude;
  private String longitude;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }

  public String getLocation() {
    return location;
  }

  public void setLocation(String location) {
    this.location = location;
  }
  
  public String getArea() {
    return area;
  }
  
  public void setArea(String area) {
    this.area = area;
  }

  public OffsetDateTime getTime() {
    return time;
  }

  public void setTime(OffsetDateTime time) {
    this.time = time;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getExtent() {
    return extent;
  }

  public void setExtent(String extent) {
    this.extent = extent;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    this.type = type;
  }

  public List<EmergencySource> getSources() {
    return sources;
  }

  public void setSources(List<EmergencySource> sources) {
    this.sources = sources;
  }
  
  public String getLatitude() {
    return latitude;
  }
  
  public void setLatitude(String latitude) {
    this.latitude = latitude;
  }
  
  public String getLongitude() {
    return longitude;
  }
  
  public void setLongitude(String longitude) {
    this.longitude = longitude;
  }

}
