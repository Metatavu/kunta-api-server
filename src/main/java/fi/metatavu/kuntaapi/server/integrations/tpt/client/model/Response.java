package fi.metatavu.kuntaapi.server.integrations.tpt.client.model;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties (ignoreUnknown = true)
public class Response implements Serializable {

  private static final long serialVersionUID = -2188575950004364800L;

  @JsonProperty("docs")
  private List<DocsEntry> docs;

  @JsonProperty("numFound")
  private Integer numFound;

  @JsonProperty("start")
  private Integer start;

  public List<DocsEntry> getDocs() {
    return docs;
  }

  public void setDocs(List<DocsEntry> docs) {
    this.docs = docs;
  }

  public Integer getNumFound() {
    return numFound;
  }

  public void setNumFound(Integer numFound) {
    this.numFound = numFound;
  }

  public Integer getStart() {
    return start;
  }

  public void setStart(Integer start) {
    this.start = start;
  }

}
