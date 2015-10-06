package com.herodigital.wcm.internal.rest.service;

import com.herodigital.wcm.internal.rest.registry.RestFunctionRegistry.ResolvedFunction;
import com.herodigital.wcm.internal.rest.registry.RestOperation;

public interface RestFunctionRegistryService {

	public static class FunctionMeta {

		private final SlingRestService heroWebService;
		private final String javaMethod;
		private final RestOperation operation; // convinience, not needed

		public FunctionMeta(SlingRestService heroWebService, String javaMethod, RestOperation operation) {
			super();
			this.heroWebService = heroWebService;
			this.javaMethod = javaMethod;
			this.operation = operation;
		}

		public SlingRestService getSlingRestService() {
			return heroWebService;
		}

		public String getJavaMethod() {
			return javaMethod;
		}

		public RestOperation getOperation() {
			return operation;
		}

	}

	public ResolvedFunction<FunctionMeta> getFunction(RestOperation restOperation);

}
