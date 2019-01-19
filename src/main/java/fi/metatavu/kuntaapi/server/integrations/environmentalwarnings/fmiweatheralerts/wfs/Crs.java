
package fi.metatavu.kuntaapi.server.integrations.environmentalwarnings.fmiweatheralerts.wfs;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class Crs implements Serializable {

  private static final long serialVersionUID = -1191819774513149536L;
  
  @JsonProperty("type")
  private String type;
  @JsonProperty("properties")
  private CrsProperties properties;

  @JsonProperty("type")
  public String getType() {
    return type;
  }

  @JsonProperty("type")
  public void setType(String type) {
    this.type = type;
  }

  @JsonProperty("properties")
  public CrsProperties getProperties() {
    return properties;
  }

  @JsonProperty("properties")
  public void setProperties(CrsProperties properties) {
    this.properties = properties;
  }

}
