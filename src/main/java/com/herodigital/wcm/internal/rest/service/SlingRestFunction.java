package com.herodigital.wcm.internal.rest.service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.sling.api.SlingHttpServletRequest;

/**
 * Annotation that is placed on {@link SlingRestService} methods so that they will be registered as REST functions.
 * <p>
 * Value is in the format [http_method]:[path]. The {@code path} may have wildcard segments marked with curly braces.
 * <p>
 * Wildcard values can be accessed by a {@link SlingRestFunction} method via {@link SlingHttpServletRequest#getAttribute(String)}. 
 * Attribute names will be prefixed with "ws.".
 * <p>
 * Examples:
 * <ul>
 * <li>POST:/api/user/register
 * <li>GET:/api/user/{userId}
 * <li>GET:/api/{group}/attribute/{attributeId}
 * </ul>
 * 
 * @author joelepps
 *
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface SlingRestFunction {

	String[] value();
	
}
