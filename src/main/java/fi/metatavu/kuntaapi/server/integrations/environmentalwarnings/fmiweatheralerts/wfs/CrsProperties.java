
package fi.metatavu.kuntaapi.server.integrations.environmentalwarnings.fmiweatheralerts.wfs;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "name" })
public class CrsProperties implements Serializable {

  private static final long serialVersionUID = 4678117992419475279L;
  
  @JsonProperty("name")
  private String name;

  @JsonProperty("name")
  public String getName() {
    return name;
  }

  @JsonProperty("name")
  public void setName(String name) {
    this.name = name;
  }

}
