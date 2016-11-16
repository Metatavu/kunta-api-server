package fi.otavanopisto.kuntaapi.server.id;

public class MissingOrganizationIdException extends RuntimeException {

  private static final long serialVersionUID = -369052831209710513L;

  public MissingOrganizationIdException(String message) {
    super(message);
  }
  
}
