
package fi.metatavu.kuntaapi.server.integrations.environmentalwarnings.fmiweatheralerts.wfs;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Feature implements Serializable {

  private static final long serialVersionUID = 7519872151109838061L;
  
  @JsonProperty("type")
  private String type;
  @JsonProperty("id")
  private String id;
  @JsonProperty("geometry")
  private Object geometry;
  @JsonProperty("geometry_name")
  private String geometryName;
  @JsonProperty("properties")
  private Properties properties;

  @JsonProperty("type")
  public String getType() {
    return type;
  }

  @JsonProperty("type")
  public void setType(String type) {
    this.type = type;
  }

  @JsonProperty("id")
  public String getId() {
    return id;
  }

  @JsonProperty("id")
  public void setId(String id) {
    this.id = id;
  }

  @JsonProperty("geometry")
  public Object getGeometry() {
    return geometry;
  }

  @JsonProperty("geometry")
  public void setGeometry(Object geometry) {
    this.geometry = geometry;
  }

  @JsonProperty("geometry_name")
  public String getGeometryName() {
    return geometryName;
  }

  @JsonProperty("geometry_name")
  public void setGeometryName(String geometryName) {
    this.geometryName = geometryName;
  }

  @JsonProperty("properties")
  public Properties getProperties() {
    return properties;
  }

  @JsonProperty("properties")
  public void setProperties(Properties properties) {
    this.properties = properties;
  }  
  
  @JsonIgnore
  public String getAlertId() {
    return String.format("%s-%s-%s", getProperties().getCreationTime(), getProperties().getWarningContext(), getProperties().getReference());
  }

}
