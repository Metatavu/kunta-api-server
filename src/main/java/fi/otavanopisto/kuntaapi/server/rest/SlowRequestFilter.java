package fi.otavanopisto.kuntaapi.server.rest;

import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;

@WebFilter (urlPatterns = "/v1/*")
public class SlowRequestFilter implements Filter {
  
  private static final int INFO_THRESHOLD = 200;
  private static final int WARNING_THRESHOLD = 500;
  private static final int SEVERE_THRESHOLD = 1000;
  
  @Inject
  private Logger logger;
  
  private Set<Pattern> excludes;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    excludes = new HashSet<>();
    loadSettings();
  }
  
  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    if (request instanceof HttpServletRequest) { 
      long requestStartTime = System.currentTimeMillis();
      try {
        chain.doFilter(request, response);
      } finally {
        long requestTime = System.currentTimeMillis() - requestStartTime;
        if (requestTime > SEVERE_THRESHOLD) {
          logRequest((HttpServletRequest) request, requestTime, Level.SEVERE);
        } else if (requestTime > WARNING_THRESHOLD) {
          logRequest((HttpServletRequest) request, requestTime, Level.WARNING);
        } else if (requestTime > INFO_THRESHOLD) {
          logRequest((HttpServletRequest) request, requestTime, Level.INFO);
        }
      }
    }
  }

  private boolean isPathExcluded(String path) {
    for (Pattern exclude : excludes) {
      Matcher matcher = exclude.matcher(path);
      if (matcher.matches()) {
        return true;
      }
    }

    return false;
  }

  @Override
  public void destroy() {
    // Nothing to destroy
  }
  
  /**
   * Logs slow request
   * 
   * @param request request
   * @param requestTime request time
   * @param level log level
   */
  private void logRequest(HttpServletRequest request, long requestTime, Level level) {
    String path = request.getPathInfo();
    if (!isPathExcluded(path)) {
      logger.log(level, () -> String.format("Request %s took %d ms", path, requestTime));
    }
  }
  
  /**
   * Loads settings from the properties file
   */
  private void loadSettings() {
    Properties properties = new Properties();
    try {
      properties.load(getClass().getClassLoader().getResourceAsStream("slow-request-filter.properties"));
    } catch (IOException e) {
      logger.log(Level.SEVERE, "Failed to load slow-request-filter.properties", e);
    }
    
    String excludePatterns = (String) properties.get("excludes");
    if (StringUtils.isNotBlank(excludePatterns)) {
      for (String excludePattern : StringUtils.split(excludePatterns, ',')) {
        excludes.add(Pattern.compile(excludePattern));
      }
    }
  }

}
