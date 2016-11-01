package fi.otavanopisto.kuntaapi.server.id;

public class MalformedIdException extends RuntimeException {

  private static final long serialVersionUID = -285523325013408935L;

  public MalformedIdException(String message) {
    super(message);
  }
  
}
