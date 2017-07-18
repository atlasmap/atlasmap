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
package io.atlasmap.json.core;

import java.util.ArrayList;
import java.util.List;

public class JsonPath {

    public static final String JSONPATH_SEPARATOR = "/";
    public static final String JSONPATH_SEPARATOR_ESCAPTED = "/";
    public static final String JSONPATH_ARRAY_START= "[";
    public static final String JSONPATH_ARRAY_END= "]";
    public static final String JSONPATH_LIST_START = "<";
    public static final String JSONPATH_LIST_END = ">";
    public static final String JSONPATH_MAP_START = "{";
    public static final String JSONPATH_MAP_END = "}";

    private List<String> segments = new ArrayList<String>();
    private String originalPath = null;

    public JsonPath() {}

    public JsonPath(String jsonPath) {
    	this.originalPath = jsonPath;
        if (jsonPath != null) {
            if(jsonPath.startsWith(JSONPATH_SEPARATOR)) {
                jsonPath = jsonPath.replaceFirst(JSONPATH_SEPARATOR, "");
            }
            if (jsonPath.contains(JSONPATH_SEPARATOR)) {
                String[] parts = jsonPath.split(JSONPATH_SEPARATOR_ESCAPTED, 512);
                for (String part : parts) {
                    getSegments().add(part);
                }
            } else {
                getSegments().add(jsonPath);
            }
        }
    }

    public JsonPath appendField(String fieldName) {
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

    public JsonPath getLastSegmentParentPath() {
        if (segments.size() == 0 || segments.size() == 1) {
            return null;
        }

        JsonPath parentPath = new JsonPath();
        for (int i = 0; i < segments.size() - 1; i++) {
            parentPath.appendField(segments.get(i));
        }
        return parentPath;
    }

    public JsonPath deCollectionify(String collectionSegment) {
        if (segments.size() == 0 || segments.size() == 1) {
            return null;
        }

        JsonPath j = new JsonPath();
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

    public JsonPath deParentify() {
        if (segments.size() == 0 || segments.size() == 1) {
            return null;
        }

        JsonPath j = new JsonPath();
        for (int i = 1; i < segments.size(); i++) {
            j.appendField(segments.get(i));
        }
        return j;
    }

    public static String cleanPathSegment(String pathSegment) {
        if (pathSegment == null) {
            return null;
        }

        if (pathSegment.contains(JSONPATH_ARRAY_START) && pathSegment.endsWith(JSONPATH_ARRAY_END)) {
            return pathSegment.substring(0, pathSegment.indexOf(JSONPATH_ARRAY_START, 0));
        }

        if (pathSegment.contains(JSONPATH_LIST_START) && pathSegment.endsWith(JSONPATH_LIST_END)) {
            return pathSegment.substring(0, pathSegment.indexOf(JSONPATH_LIST_START, 0));
        }

        if (pathSegment.contains(JSONPATH_MAP_START) && pathSegment.endsWith(JSONPATH_MAP_END)) {
            return pathSegment.substring(0, pathSegment.indexOf(JSONPATH_MAP_START, 0));
        }

        return pathSegment;
    }

    public static Boolean isCollectionSegment(String pathSegment) {
        if (pathSegment == null) {
            return false;
        }

        if (pathSegment.contains(JSONPATH_ARRAY_START) && pathSegment.endsWith(JSONPATH_ARRAY_END)) {
            return true;
        }

        if (pathSegment.contains(JSONPATH_LIST_START) && pathSegment.endsWith(JSONPATH_LIST_END)) {
            return true;
        }

        return pathSegment.contains(JSONPATH_MAP_START) && pathSegment.endsWith(JSONPATH_MAP_END);

    }

    public static Integer indexOfSegment(String pathSegment) {
        if (pathSegment == null) {
            return null;
        }

        if (pathSegment.contains(JSONPATH_ARRAY_START) && pathSegment.endsWith(JSONPATH_ARRAY_END)) {
            int start = pathSegment.indexOf(JSONPATH_ARRAY_START, 0) + 1;
            String index = pathSegment.substring(start, pathSegment.indexOf(JSONPATH_ARRAY_END, start));
            if(index != null && index.length() > 0) {
                return Integer.valueOf(index);
            }
            return null;
        }

        if (pathSegment.contains(JSONPATH_LIST_START) && pathSegment.endsWith(JSONPATH_LIST_END)) {
            int start = pathSegment.indexOf(JSONPATH_LIST_START, 0) + 1;
            String index = pathSegment.substring(start, pathSegment.indexOf(JSONPATH_LIST_END, start));
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
                if((part.contains(JSONPATH_ARRAY_START) && part.contains(JSONPATH_ARRAY_END))||
                   (part.contains(JSONPATH_LIST_START) && (part.contains(JSONPATH_LIST_END)))) {
                    return indexOfSegment(part);
                }
            }
        }

        return null;
    }

    public String getCollectionSegment() {
        for(String part : getSegments()) {
            if(JsonPath.isCollectionSegment(part)) {
                return part;
            }
        }
        return null;
    }

    public void setCollectionIndex(String segment, Integer index) {
        if(segment == null) {
            throw new IllegalArgumentException("JSONPATH segment cannot be null");
        }

        if(index < 0) {
            throw new IllegalArgumentException("JSONPATH index must be a positive integer");
        }

        if(segment.contains(JSONPATH_ARRAY_START) && segment.contains(JSONPATH_ARRAY_END)) {
            for(int i=0; i < getSegments().size(); i++) {
                String part = getSegments().get(i);
                if(cleanPathSegment(part).equals(cleanPathSegment(segment))) {
                    getSegments().set(i, cleanPathSegment(segment) + JSONPATH_ARRAY_START + index + JSONPATH_ARRAY_END);
                }
            }
        } else if(segment.contains(JSONPATH_LIST_START) && segment.contains(JSONPATH_LIST_END)) {
            for(int i=0; i < getSegments().size(); i++) {
                String part = getSegments().get(i);
                if(cleanPathSegment(part).equals(cleanPathSegment(segment))) {
                    getSegments().set(i,cleanPathSegment(segment) + JSONPATH_LIST_START + index + JSONPATH_LIST_END);
                }
            }
        } else {
            throw new IllegalArgumentException("JSONPATH segment is not a List or Array segment");
        }
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();

        int i = 0;

        if(getSegments().size() > 1) {
            buffer.append(JSONPATH_SEPARATOR);
        }

        for (String part : getSegments()) {
            buffer.append(part);
            if (i < (getSegments().size() - 1)) {
                buffer.append(JSONPATH_SEPARATOR);
            }
            i++;
        }
        return buffer.toString();
    }       
}
