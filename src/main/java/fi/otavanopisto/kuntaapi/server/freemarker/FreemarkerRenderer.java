package fi.otavanopisto.kuntaapi.server.freemarker;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import freemarker.cache.ClassTemplateLoader;
import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateExceptionHandler;
import freemarker.template.Version;

@ApplicationScoped
public class FreemarkerRenderer {
  
  private static final Version VERSION = Configuration.VERSION_2_3_23;

  @Inject
  private Logger logger;
  
  private Configuration configuration;
  
  @PostConstruct
  public void init() {
    configuration = new Configuration(VERSION); 
    configuration.setTemplateLoader(new ClassTemplateLoader(getClass().getClassLoader(), "/fi/otavanopisto/kuntaapi/server"));
    configuration.setDefaultEncoding("UTF-8");
    configuration.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER); 
    configuration.setLogTemplateExceptions(false);
    configuration.setObjectWrapper(new BeansWrapperBuilder(VERSION).build());
  }
  
  public String render(String templateName, Object dataModel, Locale locale) {
    Template template = getTemplate(templateName);
    Writer out = new StringWriter();
    template.setLocale(locale);
    
    try {
      template.process(dataModel, out);
    } catch (TemplateException | IOException e) {
      logger.log(Level.SEVERE, "Failed to render template", e);
    }
    
    return out.toString();
  }
  
  private Template getTemplate(String name) {
    try {
      return configuration.getTemplate(name);
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Failed to load template", e);
    }
    
    return null;
  }
  
}
