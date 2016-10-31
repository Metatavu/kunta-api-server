package fi.otavanopisto.kuntaapi.server.freemarker;

import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.time.temporal.TemporalAccessor;
import java.util.List;

import org.apache.commons.lang3.EnumUtils;

import freemarker.core.Environment;
import freemarker.ext.beans.StringModel;
import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModelException;

/**
 * Custom formatter for Java 8 time local date fields
 * 
 * @author Antti Leppä
 */
public class FormatDateTimeMethodModel implements TemplateMethodModelEx {

  @SuppressWarnings("rawtypes")
  @Override
  public Object exec(List arguments) throws TemplateModelException {
    if (arguments.size() != 2) {
      throw new TemplateModelException("Wrong arguments");
    }
    
    String styleParam = ((SimpleScalar) arguments.get(1)).getAsString();
    FormatStyle style = EnumUtils.getEnum(FormatStyle.class, styleParam);
    if (style == null) {
      throw new TemplateModelException(String.format("Invalid date style %s", styleParam));
    }
    
    Environment environment = Environment.getCurrentEnvironment();
    
    Object wrappedObject = ((StringModel) arguments.get(0)).getWrappedObject();
    if (wrappedObject instanceof TemporalAccessor) {
      return DateTimeFormatter
        .ofLocalizedDate(style)
        .withLocale(environment.getLocale())
        .format((TemporalAccessor) wrappedObject);
      
    } else {
      throw new TemplateModelException("First argument has to be instance of java time object");
    }
  }

  
}
