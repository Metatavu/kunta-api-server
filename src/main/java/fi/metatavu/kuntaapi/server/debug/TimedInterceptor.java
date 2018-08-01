package fi.metatavu.kuntaapi.server.debug;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.apache.commons.lang3.StringUtils;

@Interceptor
@Timed
@SuppressWarnings ("squid:S2629")
public class TimedInterceptor implements Serializable {
  
  private static final long serialVersionUID = 5589726291615678816L;
  
  private static final String METHOD_THRESHOLD_EXCEEDED_MESSAGE = "Method %s invokation took %d ms";
  
  @Inject
  private Logger logger;

  @AroundInvoke
  @SuppressWarnings ("squid:S00112")
  public Object aroundInvoke(InvocationContext invocationContext) throws Exception{
    Method method = invocationContext.getMethod();
    Timed timed = method.getAnnotation(Timed.class);
    long startTime = System.currentTimeMillis();
    try {
      return invocationContext.proceed();
    } finally {
      long duration = System.currentTimeMillis() - startTime;
      String methodDescription = getMethodDescription(method, invocationContext.getParameters());
      
      if (duration > timed.severeThreshold()) {
        logger.severe(String.format(METHOD_THRESHOLD_EXCEEDED_MESSAGE, methodDescription, duration));
      } else if (duration > timed.warningThreshold()) {
        logger.warning(String.format(METHOD_THRESHOLD_EXCEEDED_MESSAGE, methodDescription, duration));
      } else if (duration > timed.infoThreshold()) {
        logger.info(String.format(METHOD_THRESHOLD_EXCEEDED_MESSAGE, methodDescription, duration));
      }
    }
  }
  
  private String getMethodDescription(Method method, Object[] parameters) {
    List<String> parameterStrings = new ArrayList<>(parameters != null ? parameters.length : 0);
    if (parameters != null) {
      for (Object parameter : parameters) {
        parameterStrings.add(String.valueOf(parameter));
      }
    }
    
    Class<?> methodClass = method.getDeclaringClass();
    return String.format("%s.%s(%s)", methodClass.getName(), method.getName(), StringUtils.join(parameterStrings, ','));
  }

}
