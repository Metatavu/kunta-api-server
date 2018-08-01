package fi.metatavu.kuntaapi.server.integrations.tpt.client.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties (ignoreUnknown = true)
public class ApiResponse implements Serializable {

  private static final long serialVersionUID = 2187544710700669517L;

  @JsonProperty("response")
  private Response response;

  @JsonProperty("responseHeader")
  private ResponseHeader responseHeader;

  public Response getResponse() {
    return response;
  }

  public void setResponse(Response response) {
    this.response = response;
  }

  public ResponseHeader getResponseHeader() {
    return responseHeader;
  }

  public void setResponseHeader(ResponseHeader responseHeader) {
    this.responseHeader = responseHeader;
  }

}
