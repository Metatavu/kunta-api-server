
package fi.metatavu.kuntaapi.server.integrations.environmentalwarnings.fmiweatheralerts.wfs;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class FeatureCollection implements Serializable {

  private static final long serialVersionUID = 5415574167485707859L;
  
  @JsonProperty("type")
  private String type;
  @JsonProperty("totalFeatures")
  private Integer totalFeatures;
  @JsonProperty("features")
  private List<Feature> features = null;
  @JsonProperty("crs")
  private Crs crs;

  @JsonProperty("type")
  public String getType() {
    return type;
  }

  @JsonProperty("type")
  public void setType(String type) {
    this.type = type;
  }

  @JsonProperty("totalFeatures")
  public Integer getTotalFeatures() {
    return totalFeatures;
  }

  @JsonProperty("totalFeatures")
  public void setTotalFeatures(Integer totalFeatures) {
    this.totalFeatures = totalFeatures;
  }

  @JsonProperty("features")
  public List<Feature> getFeatures() {
    return features;
  }

  @JsonProperty("features")
  public void setFeatures(List<Feature> features) {
    this.features = features;
  }

  @JsonProperty("crs")
  public Crs getCrs() {
    return crs;
  }

  @JsonProperty("crs")
  public void setCrs(Crs crs) {
    this.crs = crs;
  }

}
