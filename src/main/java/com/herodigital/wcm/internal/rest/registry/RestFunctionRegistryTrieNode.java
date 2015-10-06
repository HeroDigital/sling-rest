package com.herodigital.wcm.internal.rest.registry;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class RestFunctionRegistryTrieNode<T> {
	private final PathSegment segment;
	private final Map<PathSegment, RestFunctionRegistryTrieNode<T>> children;
	private T function;
	
	RestFunctionRegistryTrieNode() {
		this.segment = null;
		this.children = new HashMap<>();
		this.function = null;
	}
	
	public RestFunctionRegistryTrieNode(PathSegment segment) {
		this.segment = segment;
		this.children = new HashMap<>();
		this.function = null;
	}
	
	public void addPathSegments(List<PathSegment> pathSegments, T function) throws IllegalArgumentException {
		PathSegment curSegment = pathSegments.remove(0);
		
		RestFunctionRegistryTrieNode<T> childNode = children.get(curSegment);
		if (childNode == null) {
			childNode = new RestFunctionRegistryTrieNode<T>(curSegment);
			children.put(curSegment, childNode);
		} else {
			// Check for attempts to register a wilcard segment where one already exists
			if (childNode.segment.isWildCard() && curSegment.isWildCard()) {
				String registeredWildcard = childNode.segment.getWildcardName();
				String newWildcard = curSegment.getWildcardName();
				if (!registeredWildcard.equals(newWildcard)) {
					throw new IllegalArgumentException("Cannot register [" + function + "]. Path already contains wildcard {"
							+ registeredWildcard + "} and trying to register wildcard {" + newWildcard + "}");
				}
			}
		}
		
		// base case: at the tail of the path
		if (pathSegments.isEmpty()) {
			if (childNode.function != null) {
				throw new IllegalArgumentException("Cannot register [" + function + "]. Function [" + childNode.function + "] already exists");
			}
			childNode.function = function;
		} else {
			// recurse
			childNode.addPathSegments(pathSegments, function);
		}
		
	}
	
	public T getFunction(List<PathSegment> segments, Map<String, String> wildcardMap) {
		PathSegment currentSeg = segments.remove(0);
		RestFunctionRegistryTrieNode<T> matchedSeg = children.get(currentSeg);

		// WILDCARD CHECK
		// If match doesn't exist, check for wildcard segment
		// This happens when initially going down the tree. There is a similar block 
		// where the wild card check is performed when going back up the tree (see: RECURSIVE BACKTRACK)
		if (matchedSeg == null) {
			matchedSeg = children.get(PathSegment.WILDCARD);
			if (matchedSeg == null) {
				segments.add(0, currentSeg); // for recursive backtracking
				return null;
			} else if (!segments.isEmpty() || matchedSeg.function != null) {
				// protected against adding wildcard when at tail and no function has been registered
				wildcardMap.put(matchedSeg.segment.getWildcardName(), currentSeg.getValue());
			}
		}

		if (segments.isEmpty()) {
			// PRIMARY BASE CASE
			// At the tail of the search path (last segment)
			segments.add(0, currentSeg); // for recursive backtracking
			return matchedSeg.function; // may be null
		} else {
			// RECURSIVE CALL #1
			// Have not reached the tail yet, continue down tree
			T function = matchedSeg.getFunction(segments, wildcardMap);
			
			/*
			 * RECURSIVE BACKTRACK
			 * If function wasn't found, it may be that we took a wrong turn down
			 * a non-wildcard node. Check for wildcard node as we go back up the tree.
			 * 
			 * Example:
			 * - Registered paths:
			 *     - /api/basic/auth/op1
			 *     - /api/{type}/op2
			 * - Queried path: 
			 *     - /api/basic/op2
			 * - Code will first try the non-wildcard "basic" segment looking for "op2"
			 *   before backtracking and attempting the wildcard {type} and then find "op2"
			 */
			if (function == null && !matchedSeg.segment.isWildCard()) {
				matchedSeg = children.get(PathSegment.WILDCARD);
				if (matchedSeg != null) {
					// add wildcard to wildcardMap
					wildcardMap.put(matchedSeg.segment.getWildcardName(), currentSeg.getValue());
					
					// RECURSIVE CALL #2
					function = matchedSeg.getFunction(segments, wildcardMap);
					
					// if function wasn't found, remove wildcard from wildcardMap
					if (function == null) {
						wildcardMap.remove(matchedSeg.segment.getWildcardName());
					}
				}
			}
			
			segments.add(0, currentSeg); // for recursive backtracking
			return function;
		}
	}

	@Override
	public String toString() {
		return printMap(children, 1);
	}
	
	// based off https://github.com/twitter/commons/blob/master/src/java/com/twitter/common/thrift/Util.java
	private String printMap(Map<PathSegment, RestFunctionRegistryTrieNode<T>> map, int depth) {
		List<String> entries = new ArrayList<>();
		for (Map.Entry<PathSegment, RestFunctionRegistryTrieNode<T>> entry : map.entrySet()) {
			entries.add(tabs(depth) + entry.getKey() 
			+ ((entry.getValue().function != null) ? "["+entry.getValue().function+"]" : "") 
			+ " = " 
			+ printMap(entry.getValue().children, depth+1));
		}
		return entries.isEmpty() ? "{}" : String.format("{\n%s\n%s}", join(",\n", (entries)), tabs(depth - 1));
	}
	
	private static String join(String joiner, Collection<String> c) {
		StringBuilder sb = new StringBuilder();
		for (String s : c) sb.append(s).append(joiner);
		return sb.substring(0, sb.length() - joiner.length());
	}

	private static String tabs(int n) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < n; i++) sb.append("  ");
		return sb.toString();
	}
}
