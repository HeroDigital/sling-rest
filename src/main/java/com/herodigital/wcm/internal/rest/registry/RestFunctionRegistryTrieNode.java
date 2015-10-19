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
					throw new IllegalArgumentException("Cannot register [" + function 
							+ "]. Path already contains wildcard {"
							+ registeredWildcard + "} and trying to register wildcard {" 
							+ newWildcard + "}");
				}
			}
		}
		
		// base case: at the tail of the path
		if (pathSegments.isEmpty()) {
			if (childNode.function != null) {
				throw new IllegalArgumentException("Cannot register [" + function 
						+ "]. Function [" + childNode.function + "] already exists");
			}
			childNode.function = function;
		} else {
			// recurse
			childNode.addPathSegments(pathSegments, function);
		}
		
	}
	
	/**
	 * Retrieve the matching function {@code <T>} from the trie data structure.
	 * 
	 * @param segments The search path (URL) consisting of individual {@link PathSegment} objects.
	 * Each path segment maps to a level of the trie.
	 * @param wildcardMap This map will be updated with wildcard key/value pairs if resolved
	 * path resulted in the use of wildcard segments.
	 * 
	 * @return resolved {@code <T>} or null
	 */
	public T getFunction(List<PathSegment> segments, Map<String, String> wildcardMap) {
		PathSegment currentSeg = segments.remove(0); // pop the next segment off the list
		
		try {
			// WALK THE TREE #1
			// Check child nodes for matching segment key
			RestFunctionRegistryTrieNode<T> matchedNode = children.get(currentSeg);
			
			// BASE CASE #1
			// At tail of search path (last segment) and found node with matching segment for key
			if (segments.isEmpty() && matchedNode != null) {
				return matchedNode.function; // may be null
			}
			
			// RECURSIVE CALL
			// Have not reached tail yet, continue down tree
			T function = (matchedNode == null) ? null : matchedNode.getFunction(segments, wildcardMap);
			
			if (function == null) {
				// This means lookup based on matching segment failed. Now try tree traversal using
				// a wildcard segment instead.
				
				// WALK THE TREE #2
				// Check child nodes for wildcard based segment
				matchedNode = children.get(PathSegment.WILDCARD);
				
				if (matchedNode != null) {
					// Add wildcard to wildcardMap
					wildcardMap.put(matchedNode.segment.getWildcardName(), currentSeg.getValue());
					
					// BASE CASE #2
					// At tail of search path (last segment) and found wildcard segment at this location
					if (segments.isEmpty()) {
						return matchedNode.function; // may be null
					}
					
					/*
					 * RECURSIVE BACKTRACK CALL
					 * If function wasn't found, it may be that we took a wrong turn down
					 * a non-wildcard node. Check for nodes with wildcard segments as we
					 * go back up the recursive stack.
					 * 
					 * Example:
					 * - Registered paths:
					 *     - /api/basic/auth/op1
					 *     - /api/{type}/op2
					 * - Queried path: 
					 *     - /api/basic/op2
					 * - Code will first try the non-wildcard "basic" segment looking for "op2"
					 *   before backtracking and attempting the wildcard {type} segment.
					 */
					function = matchedNode.getFunction(segments, wildcardMap);
					
					// If function wasn't found via this node and its children, remove this 
					// wildcard segment from wildcardMap
					if (function == null) {
						wildcardMap.remove(matchedNode.segment.getWildcardName());
					}
				}
			}
			
			return function;
			
		} finally {
			/*
			 * Guarantees segments popped will also be re-pushed.
			 * This is required for support of the recursive backtracking.
			 * Also want to be a good citizen and leave the list in the same
			 * state as it was provided.
			 */
			segments.add(0, currentSeg);
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
