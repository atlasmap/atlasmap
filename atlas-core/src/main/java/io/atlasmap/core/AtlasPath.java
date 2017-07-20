/**
 * Copyright (C) 2017 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.atlasmap.core;

import java.util.ArrayList;
import java.util.List;

public class AtlasPath {
    
    public static final String JAVAPATH_SEPARATOR = ".";
    public static final String JAVAPATH_SEPARATOR_ESCAPTED = "\\.";
    public static final String JAVAPATH_ARRAY_START= "[";
    public static final String JAVAPATH_ARRAY_END= "]";
    public static final String JAVAPATH_LIST_START = "<";
    public static final String JAVAPATH_LIST_END = ">";
    public static final String JAVAPATH_MAP_START = "{";
    public static final String JAVAPATH_MAP_END = "}";
    
    private List<String> segments = new ArrayList<String>();

    public AtlasPath() {}

    public AtlasPath(String javaPath) {
        if (javaPath != null) {
            if (javaPath.contains(JAVAPATH_SEPARATOR)) {
                String[] parts = javaPath.split(JAVAPATH_SEPARATOR_ESCAPTED, 512);
                for (String part : parts) {
                    getSegments().add(part);
                }
            } else {
                getSegments().add(javaPath);
            }
        }
    }

    public AtlasPath appendField(String fieldName) {
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
        if (segments.size() > 1) {
            return true;
        }

        return false;
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

    public AtlasPath getLastSegmentParentPath() {
        if (segments.size() == 0 || segments.size() == 1) {
            return null;
        }

        AtlasPath parentPath = new AtlasPath();
        for (int i = 0; i < segments.size() - 1; i++) {
            parentPath.appendField(segments.get(i));
        }
        return parentPath;
    }
    
    public AtlasPath deCollectionify(String collectionSegment) {
        if (segments.size() == 0 || segments.size() == 1) {
            return null;
        }

        AtlasPath j = new AtlasPath();
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

    public AtlasPath deParentify() {        
        if (segments.size() == 0 || segments.size() == 1) {
            return null;
        }

        AtlasPath j = new AtlasPath();
        for (int i = 1; i < segments.size(); i++) {
            j.appendField(segments.get(i));
        }
        return j;
    }
    
    public static String cleanPathSegment(String pathSegment) {
        if (pathSegment == null) {
            return null;
        }

        if (pathSegment.contains(JAVAPATH_ARRAY_START) && pathSegment.endsWith(JAVAPATH_ARRAY_END)) {
            return pathSegment.substring(0, pathSegment.indexOf(JAVAPATH_ARRAY_START, 0));
        }

        if (pathSegment.contains(JAVAPATH_LIST_START) && pathSegment.endsWith(JAVAPATH_LIST_END)) {
            return pathSegment.substring(0, pathSegment.indexOf(JAVAPATH_LIST_START, 0));
        }

        if (pathSegment.contains(JAVAPATH_MAP_START) && pathSegment.endsWith(JAVAPATH_MAP_END)) {
            return pathSegment.substring(0, pathSegment.indexOf(JAVAPATH_MAP_START, 0));
        }

        return pathSegment;
    }
    
    public static Boolean isCollectionSegment(String pathSegment) {
        if (pathSegment == null) {
            return false;
        }

        if (pathSegment.contains(JAVAPATH_ARRAY_START) && pathSegment.endsWith(JAVAPATH_ARRAY_END)) {
            return true;
        }

        if (pathSegment.contains(JAVAPATH_LIST_START) && pathSegment.endsWith(JAVAPATH_LIST_END)) {
            return true;
        }

        if (pathSegment.contains(JAVAPATH_MAP_START) && pathSegment.endsWith(JAVAPATH_MAP_END)) {
            return true;
        }

        return false;
    }
        
    public static Integer indexOfSegment(String pathSegment) {
        if (pathSegment == null) {
            return null;
        }

        if (pathSegment.contains(JAVAPATH_ARRAY_START) && pathSegment.endsWith(JAVAPATH_ARRAY_END)) {
            int start = pathSegment.indexOf(JAVAPATH_ARRAY_START, 0) + 1;
            String index = pathSegment.substring(start, pathSegment.indexOf(JAVAPATH_ARRAY_END, start));
            if(index != null && index.length() > 0) {
                return Integer.valueOf(index);
            }
            return null;
        }
        
        if (pathSegment.contains(JAVAPATH_LIST_START) && pathSegment.endsWith(JAVAPATH_LIST_END)) {
            int start = pathSegment.indexOf(JAVAPATH_LIST_START, 0) + 1;
            String index = pathSegment.substring(start, pathSegment.indexOf(JAVAPATH_LIST_END, start));
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
                if((part.contains(JAVAPATH_ARRAY_START) && part.contains(JAVAPATH_ARRAY_END))|| 
                   (part.contains(JAVAPATH_LIST_START) && (part.contains(JAVAPATH_LIST_END)))) {
                    return indexOfSegment(part);
                }
            }
        }
        
        return null;
    }
    
    public String getCollectionSegment() {
        for(String part : getSegments()) {
            if(AtlasPath.isCollectionSegment(part)) {
                return part;
            }
        }
        return null;
    }
    
    public void setCollectionIndex(String segment, Integer index) {
        if(segment == null) {
            throw new IllegalArgumentException("JavaPath segment cannot be null");
        }
        
        if(index < 0) {
            throw new IllegalArgumentException("JavaPath index must be a positive integer");
        }
        
        if(segment.contains(JAVAPATH_ARRAY_START) && segment.contains(JAVAPATH_ARRAY_END)) { 
            for(int i=0; i < getSegments().size(); i++) {
                String part = getSegments().get(i);
                if(cleanPathSegment(part).equals(cleanPathSegment(segment))) {
                    getSegments().set(i, cleanPathSegment(segment) + JAVAPATH_ARRAY_START + index + JAVAPATH_ARRAY_END); 
                }
            }
        } else if(segment.contains(JAVAPATH_LIST_START) && segment.contains(JAVAPATH_LIST_END)) {
            for(int i=0; i < getSegments().size(); i++) {
                String part = getSegments().get(i);
                if(cleanPathSegment(part).equals(cleanPathSegment(segment))) {
                    getSegments().set(i,cleanPathSegment(segment) + JAVAPATH_LIST_START + index + JAVAPATH_LIST_END); 
                }
            }
        } else {
            throw new IllegalArgumentException("JavaPath segment is not a List or Array segment");
        }
    }
    
    public String toString() {
        StringBuffer buffer = new StringBuffer();

        int i = 0;
        for (String part : getSegments()) {
            buffer.append(part);
            if (i < (getSegments().size() - 1)) {
                buffer.append(JAVAPATH_SEPARATOR);
            }
            i++;
        }
        return buffer.toString();
    }
}
