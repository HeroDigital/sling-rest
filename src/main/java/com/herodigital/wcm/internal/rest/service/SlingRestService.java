package com.herodigital.wcm.internal.rest.service;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;

import com.herodigital.wcm.internal.rest.servlet.SlingRestServiceServlet;

/**
 * Marker interface for a SlingRestService.
 * <p>
 * <h2>Contract</h2>
 * <ul>
 * <li>A REST function may take any action whatsoever.
 * <li>A method is registered as a REST function with the {@link SlingRestFunction} annotation.
 * <li>Every registered method must have a parameter signature of ({@link SlingHttpServletRequest}, {@link SlingHttpServletResponse}).
 * <li>Every registered method must return a populated {@link RestResponse} object.
 * <li>If the method encounters an error, it must throw a {@link SlingRestServiceException}.
 * </ul>
 * 
 * <h2>Web Service Function Methods</h2>
 * <p>
 * Methods that are to be surfaced as REST functions are annotated with the {@link SlingRestFunction} annotation.
 * 
 * <p>
 * <h2>Example Implementation</h2>
 * <pre>
 * 
 * &#64;Component(immediate = true, metatype = false, label="Example Web Service")
 * &#64;Service(SlingRestService.class)
 * &#64;Properties({
 *   &#64;Property(name = "service.vendor", value = "Hero Digital"),
 * })
 * public class ExampleWebService implements SlingRestService {
 *   
 *   &#64;SlingRestFunction("POST:/api/v1/user/register")
 *   public RestResponse doRegister(SlingHttpServletRequest request, SlingHttpServletResponse response) throws SlingRestServiceException {
 *       try  {
 *          ...
 *          Gson gson = new GsonBuilder().create();
 *          return new RestResponse.Builder(gson.toJson(returnObject)).create();
 *       } catch (ApplicationException e) {
 *          throw new SlingRestServiceException.Builder(SlingRestServiceException.CATEGORY_ERROR, "Public message for end user")
 *          .setException(e)
 *          .setInternalMessage("Internal message that will be in logs")
 *          .create();
 *       }
 *   }
 *   
 *   &#64;SlingRestFunction("GET:/api/v1/user/{id}")
 *   public RestResponse userInfo(SlingHttpServletRequest request, SlingHttpServletResponse response) throws SlingRestServiceException {
 *       String id = (String) request.getAttribute("ws.id");
 *       try  {
 *          ...
 *          Gson gson = new GsonBuilder().create();
 *          return new RestResponse.Builder(gson.toJson(returnObject)).create();
 *       } catch (ApplicationException e) {
 *          throw new SlingRestServiceException.Builder(SlingRestServiceException.CATEGORY_ERROR, "Public message for end user")
 *          .setException(e)
 *          .setInternalMessage("Internal message that will be in logs for " + id)
 *          .create();
 *       }
 *   }
 *   
 * }
 * </pre>
 * 
 * The special marker {wildcard} is used to mark segments of a path as a wild card. For instance, taking the above example, 
 * if the API call were {@code /api/v1/user/123}, then the request attribute 'ws.id' will be set to 123 prior to forwarding on to 
 * the {@code userInfo} method. All wildcard attributes are prefixed with 'ws.'.
 * <p>
 * @author joelepps
 * 
 * @see SlingRestServiceServlet
 */
public interface SlingRestService {
	
}
