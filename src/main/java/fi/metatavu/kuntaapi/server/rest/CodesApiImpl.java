package fi.metatavu.kuntaapi.server.rest;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import javax.ejb.Stateful;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.EnumUtils;
import org.apache.commons.lang3.StringUtils;

import fi.metatavu.kuntaapi.server.rest.CodesApi;
import fi.metatavu.kuntaapi.server.rest.CodesApi;
import fi.metatavu.kuntaapi.server.rest.model.Code;
import fi.metatavu.kuntaapi.server.controllers.CodeController;
import fi.metatavu.kuntaapi.server.controllers.HttpCacheController;
import fi.metatavu.kuntaapi.server.controllers.CodeController;
import fi.metatavu.kuntaapi.server.id.OrganizationId;
import fi.metatavu.kuntaapi.server.id.CodeId;
import fi.metatavu.kuntaapi.server.integrations.KuntaApiConsts;
import fi.metatavu.kuntaapi.server.integrations.KuntaApiIdFactory;
import fi.metatavu.kuntaapi.server.integrations.CodeSortBy;
import fi.metatavu.kuntaapi.server.integrations.CodeSortBy;
import fi.metatavu.kuntaapi.server.integrations.SortDir;

/**
 * REST code implementation for CodesApi
 * 
 * @author Antti Leppä
 */
@RequestScoped
@Stateful
@SuppressWarnings ({ "squid:S3306", "unused" })
public class CodesApiImpl extends CodesApi {

  private static final String INVALID_VALUE_FOR_SORT_DIR = "Invalid value for sortDir";
  private static final String INVALID_VALUE_FOR_SORT_BY = "Invalid value for sortBy";
  private static final String NOT_FOUND = "Not Found";

  @Inject
  private KuntaApiIdFactory kuntaApiIdFactory;
  
  @Inject
  private CodeController codeController;
  
  @Inject
  private RestValidator restValidator;

  @Inject
  private HttpCacheController httpCacheController;
  
  @Inject
  private RestResponseBuilder restResponseBuilder;

  @Override
  public Response findCode(String codeIdParam, Request request) {
    CodeId codeId = kuntaApiIdFactory.createCodeId(codeIdParam);
    if (codeId == null) {
      return createNotFound(NOT_FOUND);
    }
    
    Response notModified = httpCacheController.getNotModified(request, codeId);
    if (notModified != null) {
      return notModified;
    }
    
    Code code = codeController.findCode(codeId);
    if (code != null) {
      return httpCacheController.sendModified(code, code.getId());
    }
      
    return createNotFound(NOT_FOUND);
  }
  
  @Override
  public Response listCodes(List<String> types, String search, String sortByParam, String sortDirParam, Long firstResult, Long maxResultsParam, Request request) {
    CodeSortBy sortBy = resolveCodeSortBy(sortByParam);
    if (sortBy == null) {
      return createBadRequest(INVALID_VALUE_FOR_SORT_BY);
    }
    
    SortDir sortDir = resolveSortDir(sortDirParam);
    if (sortDir == null) {
      return createBadRequest(INVALID_VALUE_FOR_SORT_DIR);
    }
    
    Long maxResults = maxResultsParam == null ? 50 : maxResultsParam;
    
    Response validationResponse = restValidator.validateListLimitParams(firstResult, maxResults);
    if (validationResponse != null) {
      return validationResponse;
    }
    
    return restResponseBuilder.buildResponse(codeController.searchCodes(search, processListParam(types), sortBy, sortDir, firstResult, maxResults), request);    
  }
  
  private SortDir resolveSortDir(String sortDirParam) {
    SortDir sortDir = SortDir.ASC;
    if (sortDirParam != null) {
      return EnumUtils.getEnum(SortDir.class, sortDirParam);
    }
    
    return sortDir;
  }

  private CodeSortBy resolveCodeSortBy(String sortByParam) {
    CodeSortBy sortBy = CodeSortBy.NATURAL;
    if (sortByParam != null) {
      return  EnumUtils.getEnum(CodeSortBy.class, sortByParam);
    }
    return sortBy;
  }
  
  private List<String> processListParam(List<String> items) {
    if (items == null || items.isEmpty()) {
      return Collections.emptyList();
    }
    
    if (items.size() == 1) {
      return Arrays.asList(StringUtils.split(items.get(0), ",")); 
    }
    
    return items;
  }
  
}

