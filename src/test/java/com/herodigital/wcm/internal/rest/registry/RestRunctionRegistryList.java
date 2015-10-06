package com.herodigital.wcm.internal.rest.registry;

import java.util.ArrayList;
import java.util.List;

/**
 * Implementation of RestFunctionRegistry which uses a List to store functions.
 * <p>
 * This class is used only for performance testing to verify that the trie implementation
 * is faster.
 * <p>
 * This implementation does <b>not</b> populate the {@link ResolvedFunction#getWildcards()}.
 * @author JOEL
 *
 * @param <T>
 */
public class RestRunctionRegistryList<T> implements RestFunctionRegistry<T> {
	
	private List<Holder<T>> operationRegistry;
	
	public RestRunctionRegistryList() {
		this.operationRegistry = new ArrayList<>();
	}

	private static class Holder<T> {
		private List<PathSegment> segments;
		private T function;
		public Holder(List<PathSegment> segments, T function) {
			this.segments = segments;
			this.function = function;
		}
		@Override
		public String toString() {
			return "Holder [segments=" + segments + ", function=" + function
					+ "]";
		}
	}

	@Override
	public ResolvedFunction<T> getFunction(RestOperation operation) {
		List<Holder<T>> candidateMatches = new ArrayList<>();
		
		List<PathSegment> searchSegments = operation.getPathSegments();
		for (Holder<T> h : operationRegistry) {
			List<PathSegment> registeredSegments = h.segments;
			
			if (searchSegments.size() != registeredSegments.size()) continue;
			
			for (int i = 0; i < searchSegments.size(); i++) {
				if (i+1 > registeredSegments.size()) break;
				
				PathSegment ss = searchSegments.get(i);
				PathSegment rs = registeredSegments.get(i);
				if (ss.equals(rs) || rs.equals(PathSegment.WILDCARD)) {
					if (i + 1 == searchSegments.size()) {
						candidateMatches.add(h);
					}
					// go on to next segment
				} else {
					break;
				}
			}
		}
		
		
		int minWildcards = Integer.MAX_VALUE;
		Holder<T> bestMatch = null;
		for (Holder<T> h : candidateMatches) {
			int tmpWildcards = 0;
			for (PathSegment ps : h.segments) {
				if (ps.getValue().equals("*")) {
					tmpWildcards++;
				}
			}
			if (tmpWildcards <= minWildcards) {
				bestMatch = h;
			}
		}
		
		return (bestMatch == null) ? null : new ResolvedFunction<T>(bestMatch.function, null);
	}

	@Override
	public void addFunction(RestOperation operation, T function) {
		List<PathSegment> pathSegments = operation.getPathSegments();
		Holder<T> holder = new Holder<>(pathSegments, function);
		operationRegistry.add(holder);
	}

	@Override
	public void clear() {
		operationRegistry.clear();
	}

}
