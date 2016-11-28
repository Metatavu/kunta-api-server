package fi.otavanopisto.kuntaapi.server.rest;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;

@WebFilter (urlPatterns = "/v1/*")
public class SlowRequestFilter implements Filter {
  
  private static final int INFO_THRESHOLD = 200;
  private static final int WARNING_THRESHOLD = 500;
  private static final int SEVERE_THRESHOLD = 1000;
  
  @Inject
  private Logger logger;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    // Nothing to initialize
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

  private void logRequest(HttpServletRequest request, long requestTime, Level level) {
    String path = request.getPathInfo();
    logger.log(level, String.format("Request %s took %d ms", path, requestTime));
  }

  @Override
  public void destroy() {
 // Nothing to destroy
  }

}
