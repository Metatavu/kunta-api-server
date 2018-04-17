package fi.otavanopisto.kuntaapi.server.integrations.tpt.client.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@SuppressWarnings ("squid:S00116")
@JsonIgnoreProperties (ignoreUnknown = true)
public class ResponseHeader implements Serializable {

  private static final long serialVersionUID = 6432250874166877639L;

  @JsonProperty("qtime")
  private Integer qtime;

  @JsonProperty("QTime")
  private Integer QTime2;

  @JsonProperty("params")
  private Params params;

  @JsonProperty("status")
  private Integer status;

  public Integer getQtime() {
    return qtime;
  }

  public void setQtime(Integer qtime) {
    this.qtime = qtime;
  }
  
  public Integer getQTime2() {
    return QTime2;
  }
  
  public void setQTime2(Integer qTime2) {
    QTime2 = qTime2;
  }
  
  public Params getParams() {
    return params;
  }

  public void setParams(Params params) {
    this.params = params;
  }

  public Integer getStatus() {
    return status;
  }

  public void setStatus(Integer status) {
    this.status = status;
  }

}
