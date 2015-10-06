package com.herodigital.wcm.internal.rest.registry;

/**
 * Segment of a URI path. 
 * <p>
 * A segment string surrounded by curly braces (example: {string})
 * represents a wildcard segment which will match any string. 
 * 
 * @author joel.epps
 *
 */
public class PathSegment {

	public static final PathSegment WILDCARD = new PathSegment("{wildcard}");

	private String value;
	private String wildcardName;

	public PathSegment(String value) {
		if (value.startsWith("{") && value.endsWith("}")) {
			this.value = "*";
			this.wildcardName = value.substring(1, value.length() - 1);
		} else {
			this.value = value;
			this.wildcardName = null;
		}
	}

	/**
	 * Value of the segment. "*" if this segment is a wildcard. 
	 * 
	 * @return
	 */
	public String getValue() {
		return value;
	}

	/**
	 * The name of this wildcard segment. Denoted by the text between curly braces.
	 * 
	 * @return Name or null if this segment is not a wildcard
	 */
	public String getWildcardName() {
		return wildcardName;
	}
	
	public boolean isWildCard() {
		return getWildcardName() != null;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		PathSegment other = (PathSegment) obj;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return ((wildcardName != null) ? "{" + wildcardName + "}" : value);
	}

}
