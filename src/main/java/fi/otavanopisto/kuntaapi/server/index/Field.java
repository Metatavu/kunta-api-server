package fi.otavanopisto.kuntaapi.server.index;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Field {

  String type() default "string";
  String index() default "analyzed";
  boolean store() default false;
  
}
