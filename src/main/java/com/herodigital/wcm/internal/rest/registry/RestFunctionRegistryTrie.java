package com.herodigital.wcm.internal.rest.registry;

import java.util.HashMap;
import java.util.Map;

/**
 * RestFunctionRegistry implementation that uses a trie data structure.
 * 
 * @author joel.epps
 *
 * @param <T> Function class/type
 */
public class RestFunctionRegistryTrie<T> implements RestFunctionRegistry<T> {
	
	private RestFunctionRegistryTrieNode<T> root;
	
	public RestFunctionRegistryTrie() {
		root = new RestFunctionRegistryTrieNode<T>();
	}

	@Override
	public ResolvedFunction<T> getFunction(RestOperation operation) {
		Map<String, String> wildcardMap = new HashMap<String, String>();
		T function = root.getFunction(operation.getPathSegments(), wildcardMap);
		return (function == null) ? null : new ResolvedFunction<T>(function, wildcardMap);
	}

	@Override
	public void addFunction(RestOperation operation, T function) {
		root.addPathSegments(operation.getPathSegments(), function);
	}

	@Override
	public void clear() {
		root = new RestFunctionRegistryTrieNode<T>();
	}
	
	@Override
	public String toString() {
		return root.toString();
	}
	
	
	
}
