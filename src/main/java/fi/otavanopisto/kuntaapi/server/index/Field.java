package fi.otavanopisto.kuntaapi.server.index;

public @interface Field {

  String type() default "string";
  String index() default "analyzed";
  String analyzerFrom() default "";
  boolean ignore() default false;
  
}
