package com.herodigital.wcm.internal.rest.registry;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@code RestOperation} is the combination of a {@link HttpMethod} and path.
 * <p>
 * Example: {@link GET:/api/user/123}
 * 
 * @author joel.epps
 *
 */
public class RestOperation {

	private HttpMethod method;
	private String path;
	
	public RestOperation(HttpMethod method, String path) {
		this.method = method;
		this.path = path;
	}

	public HttpMethod getMethod() {
		return method;
	}

	public String getPath() {
		return path;
	}

	/**
	 * Splits the path into a list {@link PathSegment} objects.
	 * 
	 * @return List of {@link PathSegment} objects starting with {@link HttpMethod}
	 */
	public List<PathSegment> getPathSegments() {
		List<PathSegment> result = new ArrayList<>();
		result.add(new PathSegment(method.toString()));
		
		String thePath = path;
		if (path.startsWith("/")) {
			thePath = path.substring(1);
		}
		
		String[] segments = thePath.split("/");
		for (String segment : segments) {
			PathSegment ps = new PathSegment(segment);
			result.add(ps);
		}
		return result;
	}
	
	public static RestOperation fromString(String s) {
		String[] split = s.split(":");
		if (split.length != 2) {
			throw new IllegalArgumentException("Failed to parse RestOperation " + s);
		}
		HttpMethod method = HttpMethod.valueOf(split[0]);
		return new RestOperation(method, split[1]);
	}

	@Override
	public String toString() {
		return method + ":" + path;
	}
	
}
