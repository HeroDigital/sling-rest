package com.herodigital.wcm.internal.rest.registry;

import java.util.Map;

/**
 * Registry of REST functions. 
 * <p>
 * Functions are registered by {@link RestOperation}. Functions may be any
 * class/object.
 * 
 * @author joel.epps
 *
 * @param <T> Function class
 */
public interface RestFunctionRegistry<T> {
	
	/**
	 * 
	 * @author joel.epps
	 *
	 * @param <T>
	 */
	public static class ResolvedFunction<T> {
		private T function;
		private Map<String, String> wildcards;

		public ResolvedFunction(T function, Map<String, String> wildcards) {
			super();
			this.function = function;
			this.wildcards = wildcards;
		}

		public T getFunction() {
			return function;
		}

		public Map<String, String> getWildcards() {
			return wildcards;
		}

	}

	/**
	 * Get the REST function. The returned {@link ResolvedFunction} also contains
	 * any wildcard names and values part of the path.
	 * 
	 * @param operation operation
	 * @return {@link ResolvedFunction} or null if no match is found
	 */
	public ResolvedFunction<T> getFunction(RestOperation operation);

	/**
	 * Add a REST function to the registry.
	 * 
	 * @param operation Key for function
	 * @param function function to add
	 * @throws IllegalArgumentException if {@code operation} already has a function associated with it
	 * or a wildcard segment with a different name already exists (ex. /api/{wild1}/op1 and /api/{wild2}/op2).
	 */
	public void addFunction(RestOperation operation, T function) throws IllegalArgumentException;

	/**
	 * Clear the registry
	 */
	public void clear();

}
