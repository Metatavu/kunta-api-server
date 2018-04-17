package fi.otavanopisto.kuntaapi.server.integrations.tpt.client.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties (ignoreUnknown = true)
public class Params implements Serializable {

  private static final long serialVersionUID = 1089190638409665567L;

  @JsonProperty("q")
  private String q;

  @JsonProperty("start")
  private Integer start;

  @JsonProperty("rows")
  private Integer rows;

  @JsonProperty("wt")
  private String wt;

  public String getQ() {
    return q;
  }

  public void setQ(String q) {
    this.q = q;
  }

  public Integer getStart() {
    return start;
  }

  public void setStart(Integer start) {
    this.start = start;
  }

  public Integer getRows() {
    return rows;
  }

  public void setRows(Integer rows) {
    this.rows = rows;
  }

  public String getWt() {
    return wt;
  }

  public void setWt(String wt) {
    this.wt = wt;
  }

}
