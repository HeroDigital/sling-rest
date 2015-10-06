package com.herodigital.wcm.internal.rest.service;

import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.annotations.Expose;

/**
 * All web service methods should throw this exception when encountering a checked exception.
 * <p>
 * Use {@link Builder} to construct. 
 * 
 * @author joelepps
 *
 */
public class SlingRestServiceException extends Exception {
	
	public static final String CATEGORY_ERROR = "error";
	public static final String CATEGORY_VALIDATION = "validation";

	private static final long serialVersionUID = 1417470012775654514L;

	private static final Gson GSON = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

	@Expose
	private final String errorCategory;
	@Expose
	private final String errorCode;
	@Expose
	private final String errorMessage;
	@Expose
	private final String redirect;
	private final String internalMessage;
	private final Exception exception;
	private final int httpStatusCode;

	private SlingRestServiceException(Builder b) {
		super( b.getHttpStatusCode() + ": " + ((StringUtils.isBlank(b.getInternalMessage())) ? b.getMessage() : b.getInternalMessage()), b.getException());
		this.errorCategory = b.getErrorCategory();
		this.errorCode = b.getErrorCode();
		this.errorMessage = b.getMessage();
		this.internalMessage = b.getInternalMessage();
		this.redirect = b.getRedirect();
		this.exception = b.getException();
		this.httpStatusCode = b.getHttpStatusCode();
	}

	/**
	 * The category of the error. May be anything but provided options are {@code error} and {@code validation}. 
	 * 
	 * @return
	 */
	public String getErrorCategory() {
		return errorCategory;
	}

	/**
	 * The error code. This would typically be a number or ID string.
	 * 
	 * @return
	 */
	public String getErrorCode() {
		return errorCode;
	}

	/**
	 * The error message. This should be vague enough for end user consumption.
	 * 
	 * @return
	 * @see {@link #getInternalMessage()}
	 */
	public String getErrorMessage() {
		return errorMessage;
	}

	/**
	 * A redirect URL or path the user should be taken to.
	 * 
	 * @return
	 */
	public String getRedirect() {
		return redirect;
	}

	/**
	 * The error message suitable for error logs and developers.
	 * 
	 * @return
	 * @see {@link #getErrorMessage()}
	 */
	public String getInternalMessage() {
		return internalMessage;
	}

	/**
	 * Chained exception, if any.
	 * 
	 * @return
	 */
	public Exception getException() {
		return exception;
	}

	/**
	 * HTTP status code that should be returned.
	 * 
	 * @return
	 */
	public int getHttpStatusCode() {
		return httpStatusCode;
	}
	
	/**
	 * JSON representation suitable for public consumption. Some fields are excluded.
	 * 
	 * @return
	 */
	public String toJson() {
		return GSON.toJson(this);
	}
	
	public RestResponse toRestResponse() {
		return new RestResponse.Builder(toJson())
				.setHttpStatus(getHttpStatusCode())
				.create();
	}

	public static class Builder {
		
		private String errorCategory;
		private String errorCode;
		private String message;
		private String redirect;
		private String internalMessage;
		private Exception exception;
		private int httpStatusCode;
		
		public Builder(String errorCategory, String message) {
			this.errorCategory = errorCategory;
			this.message = message;
		}

		public SlingRestServiceException create() {
			return new SlingRestServiceException(this);
		}

		public String getErrorCategory() {
			return StringUtils.isBlank(errorCategory) ? SlingRestServiceException.CATEGORY_ERROR : errorCategory;
		}

		/**
		 * Defaults to {@link SlingRestServiceException#CATEGORY_ERROR}. 
		 * 
		 * @param errorCategory
		 * @return
		 */
		public Builder setErrorCategory(String errorCategory) {
			this.errorCategory = errorCategory;
			return this;
		}

		public String getErrorCode() {
			return StringUtils.trimToEmpty(errorCode);
		}

		public Builder setErrorCode(String errorCode) {
			this.errorCode = errorCode;
			return this;
		}

		public String getMessage() {
			return StringUtils.trimToEmpty(message);
		}

		public Builder setMessage(String message) {
			this.message = message;
			return this;
		}

		public String getRedirect() {
			return StringUtils.trimToEmpty(redirect);
		}

		public Builder setRedirect(String redirect) {
			this.redirect = redirect;
			return this;
		}

		public String getInternalMessage() {
			return StringUtils.trimToEmpty(internalMessage);
		}

		public Builder setInternalMessage(String internalMessage) {
			this.internalMessage = internalMessage;
			return this;
		}

		public Exception getException() {
			return exception;
		}

		public Builder setException(Exception exception) {
			this.exception = exception;
			return this;
		}

		public int getHttpStatusCode() {
			if (httpStatusCode <= 0) {
				if (CATEGORY_VALIDATION.equals(getErrorCategory())) {
					return 400; // Bad Request
				} else {
					return HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
				}
			}
			return httpStatusCode;
		}

		/**
		 * If not set and {@code category} is {@link SlingRestServiceException#CATEGORY_VALIDATION} then default to 400 (bad request).
		 * <p>
		 * If not set and {@code category} is anything else, then default to 500 (internal server error)
		 * 
		 * @param httpStatusCode
		 * @return
		 */
		public Builder setHttpStatusCode(int httpStatusCode) {
			this.httpStatusCode = httpStatusCode;
			return this;
		}

	}

}
