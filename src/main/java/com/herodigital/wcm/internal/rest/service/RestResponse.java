package com.herodigital.wcm.internal.rest.service;

/**
 * Holds the result and metadata for a completed REST function invocation. 
 * 
 * @author joelepps
 *
 */
public class RestResponse {
	
	public static final int HTTP_STATUS_DEFAULT = 200;
	public static final String CHARACTER_SET_DEFAULT = "utf-8";
	public static final String CONTENT_TYPE_DEFAULT = "application/json";

	private final int httpStatus;
	private final String json;
	private final String charSet;
	private final String contentType;

	private RestResponse(int httpStatus, String json, String charSet, String contentType) {
		this.httpStatus = httpStatus;
		this.json = json;
		this.charSet = charSet;
		this.contentType = contentType;
	}

	public int getHttpStatus() {
		return httpStatus;
	}

	public String getJson() {
		return json;
	}
	
	public String getCharSet() {
		return charSet;
	}

	public String getContentType() {
		return contentType;
	}

	public static class Builder {

		private int httpStatus;
		private String json;
		private String charSet;
		private String contentType;

		public Builder(String json) {
			this.httpStatus = HTTP_STATUS_DEFAULT;
			this.json = json;
			this.charSet = CHARACTER_SET_DEFAULT;
			this.contentType = CONTENT_TYPE_DEFAULT;
		}

		/**
		 * Default is {@link RestResponse#HTTP_STATUS_DEFAULT}
		 * 
		 * @param httpStatus
		 * @return
		 */
		public Builder setHttpStatus(int httpStatus) {
			this.httpStatus = httpStatus;
			return this;
		}

		public Builder setJson(String json) {
			this.json = json;
			return this;
		}

		/**
		 * Default is {@link RestResponse#CHARACTER_SET_DEFAULT}
		 * 
		 * @param charSet
		 */
		public void setCharSet(String charSet) {
			this.charSet = charSet;
		}

		/**
		 * Default is {@link RestResponse#CONTENT_TYPE_DEFAULT}
		 * 
		 * @param contentType
		 */
		public void setContentType(String contentType) {
			this.contentType = contentType;
		}

		public RestResponse create() {
			return new RestResponse(httpStatus, json, charSet, contentType);
		}

	}
}
