package com.herodigital.wcm.internal.rest.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;

import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.sling.SlingServlet;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.herodigital.wcm.internal.rest.registry.HttpMethod;
import com.herodigital.wcm.internal.rest.registry.RestFunctionRegistry.ResolvedFunction;
import com.herodigital.wcm.internal.rest.registry.RestOperation;
import com.herodigital.wcm.internal.rest.service.SlingRestFunction;
import com.herodigital.wcm.internal.rest.service.SlingRestService;
import com.herodigital.wcm.internal.rest.service.SlingRestServiceException;
import com.herodigital.wcm.internal.rest.service.RestFunctionRegistryService;
import com.herodigital.wcm.internal.rest.service.RestFunctionRegistryService.FunctionMeta;
import com.herodigital.wcm.internal.rest.service.RestResponse;

/**
 * This servlet is essentially the "dispatcher" for all {@link SlingRestService} instances.
 * <p>
 * It routes requests to the appropriate {@link SlingRestFunction} methods and handles
 * the resulting JSON string or thrown {@link SlingRestServiceException}.
 * 
 * @author joelepps
 * @see SlingRestService
 */
@SlingServlet(
		metatype=false,
		resourceTypes="sling/servlet/default",
		selectors=SlingRestServiceServlet.SELECTOR,
		extensions=SlingRestServiceServlet.EXTENSION,
		methods={"GET", "POST", "DELETE", "PUT"},
		label="Sling REST Routing Servlet",
		description="Routes REST service requests to the appropriate methods"
)
public class SlingRestServiceServlet extends SlingAllMethodsServlet {
	
	public static final String WILDCARD_ATTRIBUTE_PREFIX = "ws.";
	public static final String SELECTOR = "ws";
	public static final String EXTENSION = "json";
	
	private static final long serialVersionUID = -2519870152628179333L;
	
	private static final Logger log = LoggerFactory.getLogger(SlingRestServiceServlet.class);
	
	@Reference
	private RestFunctionRegistryService registryService;
	
	@Override
	protected void doGet(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
		boolean executed = doBase(HttpMethod.GET, request, response);
		if (!executed) {
			log.debug("Could not find REST operation {}", request.getPathInfo());
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}
	
	@Override
	protected void doPost(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
		boolean executed = doBase(HttpMethod.POST, request, response);
		if (!executed) {
			log.debug("Could not find REST operation {}", request.getPathInfo());
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	@Override
	protected void doPut(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
		boolean executed = doBase(HttpMethod.PUT, request, response);
		if (!executed) {
			log.debug("Could not find REST operation {}", request.getPathInfo());
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}

	@Override
	protected void doDelete(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
		boolean executed = doBase(HttpMethod.DELETE, request, response);
		if (!executed) {
			log.debug("Could not find REST operation {}", request.getPathInfo());
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		}
	}
	
	private boolean doBase(final HttpMethod httpMethod, final SlingHttpServletRequest request, final SlingHttpServletResponse response) throws IOException {
		boolean executed = false;
		
		try {
			String path = request.getRequestPathInfo().getResourcePath();
			path = cleanPath(path);
			
			RestOperation op = new RestOperation(httpMethod, path);
			ResolvedFunction<FunctionMeta> foundFunction = registryService.getFunction(op);
			if (foundFunction != null) {
				updateRequestWithAttributes(request, foundFunction.getWildcards());
				
				SlingRestService heroWebService = foundFunction.getFunction().getSlingRestService();
				
				Object responseObj = invokeMethod(heroWebService, (String) foundFunction.getFunction().getJavaMethod(), request, response);
				
				// Evaluate response object. Supported types are RestResponse and String (typically JSON).
				// String is included for backwards compatibility, RestResponse is preferred.
				if (responseObj != null && responseObj instanceof RestResponse) {
					writeJsonResponse(response, (RestResponse) responseObj);
				} else if (responseObj != null && responseObj instanceof String) {
					writeJsonResponse(response, new RestResponse.Builder((String) responseObj).create());
				} else if (responseObj != null) {
					throw new IllegalStateException("Unsupported return type of " + responseObj.getClass().getCanonicalName() + " for " + responseObj);
				}
				
				executed = true;
			} else {
				executed = false;
			}
		} catch (SlingRestServiceException e) {
			// log non-validation exceptions
			// log validation exceptions if debug logging enabled
			if (!SlingRestServiceException.CATEGORY_VALIDATION.equals(e.getErrorCategory()) || 
					(SlingRestServiceException.CATEGORY_VALIDATION.equals(e.getErrorCategory()) && log.isDebugEnabled()) ) {
				log.error("Web service failure: ", e);
			}
			writeJsonResponse(response, e.toRestResponse());
			executed = true;
		} catch (Exception e) {
			log.error("Web service failure.", e);
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unexpected error occurred.");
			executed = true;
		}
		return executed;
	}
	
	/*
	 * As of AEM 6 SP2 {@code request.getRequestPathInfo().getResourcePath()} in some cases returns a path with selector and extension.
	 * <p>
	 * Unclear if this is a bug or expected new behavior for a non-existing resource. In any case this method strips any trailing
	 * selector and extension.
	 */
	private String cleanPath(String rawPath) {
		String tail = "." + SELECTOR + "." + EXTENSION;
		String cleaned = rawPath;
		if (rawPath.endsWith(tail)) {
			cleaned = rawPath.replaceAll(tail+"$", "");
			log.trace("Cleaned path {} -> {}", rawPath, cleaned);
		}
		return cleaned;
	}
	
	private void updateRequestWithAttributes(SlingHttpServletRequest request, Map<String, String> attributes) {
		for (Entry<String, String> attr : attributes.entrySet()) {
			request.setAttribute(WILDCARD_ATTRIBUTE_PREFIX + attr.getKey(), attr.getValue());
		}
	}

	protected Object invokeMethod(Object o, String name, Object... args) throws SlingRestServiceException, Exception {
		try {
			Method m = o.getClass().getMethod(name, SlingHttpServletRequest.class, SlingHttpServletResponse.class);
			return m.invoke(o, args);
		} catch (InvocationTargetException e) {
			if (e.getTargetException() != null) {
				throw (Exception) e.getTargetException();
			} else {
				throw e;
			}
		}
	}
	
	private void writeJsonResponse(SlingHttpServletResponse response, RestResponse restResponse) throws IOException {
		response.setStatus(restResponse.getHttpStatus());
		response.setContentType(restResponse.getContentType());
		response.setCharacterEncoding(restResponse.getCharSet());
		PrintWriter pw = response.getWriter();
		pw.print(restResponse.getJson());
		pw.close();
	}
	
}
