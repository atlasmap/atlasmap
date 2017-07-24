package io.atlasmap.core;

import java.util.ArrayList;
import java.util.List;

public class PathUtil {
	public static final String PATH_SEPARATOR = "/";
    public static final String PATH_SEPARATOR_ESCAPED = "/";
    public static final String PATH_ARRAY_START= "[";
    public static final String PATH_ARRAY_END= "]";
    public static final String PATH_LIST_START = "<";
    public static final String PATH_LIST_END = ">";
    public static final String PATH_MAP_START = "{";
    public static final String PATH_MAP_END = "}";

    private List<String> segments = new ArrayList<String>();
    private String originalPath = null;

    public PathUtil() {}

    public PathUtil(String path) {
    	this.originalPath = path;
        if (path != null) {
            if(path.startsWith(PATH_SEPARATOR)) {
            	path = path.replaceFirst(PATH_SEPARATOR, "");
            }
            if (path.contains(PATH_SEPARATOR)) {
                String[] parts = path.split(PATH_SEPARATOR_ESCAPED, 512);
                for (String part : parts) {
                    getSegments().add(part);
                }
            } else {
                getSegments().add(path);
            }
        }
    }

    public PathUtil appendField(String fieldName) {
        this.segments.add(fieldName);
        return this;
    }

    public List<String> getSegments() {
        return this.segments;
    }

    public String getLastSegment() {
        if (segments.size() > 0) {
            return segments.get(segments.size() - 1);
        } else {
            return null;
        }
    }

    public boolean hasParent() {
        return segments.size() > 1;

    }

    public boolean hasCollection() {
       for(String seg: getSegments()) {
           if(isCollectionSegment(seg)) {
               return true;
           }
       }
       return false;
    }

    public Boolean isIndexedCollection() {
        for(String seg: getSegments()) {
            if(isCollectionSegment(seg) && indexOfSegment(seg) != null) {
                return true;
            }
        }
        return false;
    }

    public Boolean isCollectionRoot() {
        return isCollectionSegment(getLastSegment());
    }

    public String getLastSegmentParent() {
        if (segments.size() == 0 || segments.size() == 1) {
            return null;
        }

        return segments.get(segments.size() - 2);
    }

    public PathUtil getLastSegmentParentPath() {
        if (segments.size() == 0 || segments.size() == 1) {
            return null;
        }

        PathUtil parentPath = new PathUtil();
        for (int i = 0; i < segments.size() - 1; i++) {
            parentPath.appendField(segments.get(i));
        }
        return parentPath;
    }

    public PathUtil deCollectionify(String collectionSegment) {
        if (segments.size() == 0 || segments.size() == 1) {
            return null;
        }

        PathUtil j = new PathUtil();
        boolean collectionFound = false;
        for (String part : segments) {
            if(collectionFound) {
                j.appendField(part);
            }
            if(cleanPathSegment(part).equals(cleanPathSegment(collectionSegment))) {
                collectionFound = true;
            }
        }
        return j;
    }

    public PathUtil deParentify() {
        if (segments.size() == 0 || segments.size() == 1) {
            return null;
        }

        PathUtil j = new PathUtil();
        for (int i = 1; i < segments.size(); i++) {
            j.appendField(segments.get(i));
        }
        return j;
    }

    public static String cleanPathSegment(String pathSegment) {
        if (pathSegment == null) {
            return null;
        }
        
        //strip namespace if there is one        
        if (pathSegment.contains(":")) {
        	pathSegment = pathSegment.substring(pathSegment.indexOf(":") + 1);
        }
        
        //strip leading @ symbol if there is one
        if (pathSegment.startsWith("@")) {
        	pathSegment = pathSegment.substring(1);
        }

        if (pathSegment.contains(PATH_ARRAY_START) && pathSegment.endsWith(PATH_ARRAY_END)) {
            return pathSegment.substring(0, pathSegment.indexOf(PATH_ARRAY_START, 0));
        }

        if (pathSegment.contains(PATH_LIST_START) && pathSegment.endsWith(PATH_LIST_END)) {
            return pathSegment.substring(0, pathSegment.indexOf(PATH_LIST_START, 0));
        }

        if (pathSegment.contains(PATH_MAP_START) && pathSegment.endsWith(PATH_MAP_END)) {
            return pathSegment.substring(0, pathSegment.indexOf(PATH_MAP_START, 0));
        }

        return pathSegment;
    }

    public static Boolean isCollectionSegment(String pathSegment) {
        if (pathSegment == null) {
            return false;
        }

        if (pathSegment.contains(PATH_ARRAY_START) && pathSegment.endsWith(PATH_ARRAY_END)) {
            return true;
        }

        if (pathSegment.contains(PATH_LIST_START) && pathSegment.endsWith(PATH_LIST_END)) {
            return true;
        }

        return pathSegment.contains(PATH_MAP_START) && pathSegment.endsWith(PATH_MAP_END);
    }
    
    public static Boolean isAttributeSegment(String pathSegment) {
    	return pathSegment != null && pathSegment.startsWith("@");
    }
    
    public static Boolean isNamespaceSegment(String pathSegment) {
    	return pathSegment != null && pathSegment.contains(":");
    }
    
    public static String getNamespace(String pathSegment) {
    	if (!isNamespaceSegment(pathSegment)) {
    		return null;
    	}
    	pathSegment = pathSegment.substring(0, pathSegment.indexOf(":"));
    	if (pathSegment.startsWith("@")) {
    		pathSegment = pathSegment.substring(1);
    	}
    	return pathSegment;
    }

    public static Integer indexOfSegment(String pathSegment) {
        if (pathSegment == null) {
            return null;
        }

        if (pathSegment.contains(PATH_ARRAY_START) && pathSegment.endsWith(PATH_ARRAY_END)) {
            int start = pathSegment.indexOf(PATH_ARRAY_START, 0) + 1;
            String index = pathSegment.substring(start, pathSegment.indexOf(PATH_ARRAY_END, start));
            if(index != null && index.length() > 0) {
                return Integer.valueOf(index);
            }
            return null;
        }

        if (pathSegment.contains(PATH_LIST_START) && pathSegment.endsWith(PATH_LIST_END)) {
            int start = pathSegment.indexOf(PATH_LIST_START, 0) + 1;
            String index = pathSegment.substring(start, pathSegment.indexOf(PATH_LIST_END, start));
            if(index != null && index.length() > 0) {
                return Integer.valueOf(index);
            }
            return null;
        }

        return null;
    }

    public Integer getCollectionIndex(String segment) {
        for(String part : getSegments()) {
            if(cleanPathSegment(part).equals(cleanPathSegment(segment))) {
                if((part.contains(PATH_ARRAY_START) && part.contains(PATH_ARRAY_END))||
                   (part.contains(PATH_LIST_START) && (part.contains(PATH_LIST_END)))) {
                    return indexOfSegment(part);
                }
            }
        }

        return null;
    }

    public String getCollectionSegment() {
        for(String part : getSegments()) {
            if(PathUtil.isCollectionSegment(part)) {
                return part;
            }
        }
        return null;
    }

    public void setCollectionIndex(String segment, Integer index) {
        if(segment == null) {
            throw new IllegalArgumentException("PathUtil segment cannot be null");
        }

        if(index < 0) {
            throw new IllegalArgumentException("PathUtil index must be a positive integer");
        }

        if(segment.contains(PATH_ARRAY_START) && segment.contains(PATH_ARRAY_END)) {
            for(int i=0; i < getSegments().size(); i++) {
                String part = getSegments().get(i);
                if(cleanPathSegment(part).equals(cleanPathSegment(segment))) {
                    getSegments().set(i, cleanPathSegment(segment) + PATH_ARRAY_START + index + PATH_ARRAY_END);
                }
            }
        } else if(segment.contains(PATH_LIST_START) && segment.contains(PATH_LIST_END)) {
            for(int i=0; i < getSegments().size(); i++) {
                String part = getSegments().get(i);
                if(cleanPathSegment(part).equals(cleanPathSegment(segment))) {
                    getSegments().set(i,cleanPathSegment(segment) + PATH_LIST_START + index + PATH_LIST_END);
                }
            }
        } else {
            throw new IllegalArgumentException("PathUtil segment is not a List or Array segment");
        }
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();

        int i = 0;

        if(getSegments().size() > 1) {
            buffer.append(PATH_SEPARATOR);
        }

        for (String part : getSegments()) {
            buffer.append(part);
            if (i < (getSegments().size() - 1)) {
                buffer.append(PATH_SEPARATOR);
            }
            i++;
        }
        return buffer.toString();
    }       
    
    public String getOriginalPath() {
		return originalPath;
	}
}
