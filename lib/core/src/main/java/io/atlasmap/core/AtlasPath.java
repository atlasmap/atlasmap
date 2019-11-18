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
import java.util.Collections;
import java.util.List;

import io.atlasmap.v2.CollectionType;

public class AtlasPath {
    public static final String PATH_SEPARATOR = "/";
    public static final char PATH_SEPARATOR_CHAR = '/';
    public static final String PATH_SEPARATOR_ESCAPED = "/";
    public static final String PATH_ARRAY_START = "[";
    public static final String PATH_ARRAY_END = "]";
    public static final String PATH_LIST_START = "<";
    public static final String PATH_LIST_END = ">";
    public static final String PATH_MAP_START = "{";
    public static final String PATH_MAP_END = "}";
    public static final String PATH_ATTRIBUTE_PREFIX = "@";
    public static final String PATH_NAMESPACE_SEPARATOR = ":";

    protected List<SegmentContext> segmentContexts = new ArrayList<>();
    private String originalPath = null;

    public AtlasPath(String p) {
        String path = p;
        this.originalPath = path;
        if (path != null && !"".equals(path)) {
            if (path.startsWith(PATH_SEPARATOR)) {
                path = path.replaceFirst(PATH_SEPARATOR, "");
            }
            if (path.contains(PATH_SEPARATOR)) {
                String[] parts = path.split(PATH_SEPARATOR_ESCAPED, 512);
                for (String part : parts) {
                    this.segmentContexts.add(createSegmentContext(part));
                }
            } else {
                this.segmentContexts.add(createSegmentContext(path));
            }
        }
        if (this.segmentContexts.isEmpty() || !this.segmentContexts.get(0).isRoot()) {
            // add root segment if there's not
            this.segmentContexts.add(0, createSegmentContext(""));
        }
    }

    protected SegmentContext createSegmentContext(String expression) {
        return new SegmentContext(expression);
    }

    private AtlasPath() {
    }

    public AtlasPath appendField(String fieldExpression) {
        this.segmentContexts.add(createSegmentContext(fieldExpression));
        return this;
    }

    public List<SegmentContext> getSegments(boolean includeRoot) {
        if (includeRoot) {
            return Collections.unmodifiableList(this.segmentContexts);
        }
        if (this.segmentContexts.size() > 1) {
            return Collections.unmodifiableList(this.segmentContexts.subList(1, this.segmentContexts.size()));
        }
        return Collections.emptyList();
    }

    public Boolean isRoot() {
        return this.segmentContexts.size() == 1 && this.segmentContexts.get(0).isRoot();
    }

    public SegmentContext getRootSegment() {
        return this.segmentContexts.get(0);
    }

    public Boolean isCollectionRoot() {
        return this.segmentContexts.size() == 1 && this.segmentContexts.get(0).getCollectionType() != CollectionType.NONE;
    }

    public Boolean hasCollectionRoot() {
        return this.segmentContexts.get(0).getCollectionType() != CollectionType.NONE;
    }

    public SegmentContext getLastSegment() {
        return this.segmentContexts.get(this.segmentContexts.size()-1);
    }

    public SegmentContext getLastSegmentParent() {
        if (this.segmentContexts.isEmpty() || this.segmentContexts.size() == 1) {
            return null;
        }

        return this.segmentContexts.get(this.segmentContexts.size() - 2);
    }

    public AtlasPath getLastSegmentParentPath() {
        if (this.segmentContexts.isEmpty() || this.segmentContexts.size() == 1) {
            return null;
        }

        AtlasPath parentPath = new AtlasPath();
        for (int i = 0; i < this.segmentContexts.size() - 1; i++) {
            parentPath.appendField(this.segmentContexts.get(i).getExpression());
        }
        return parentPath;
    }

    public SegmentContext getParentSegmentOf(SegmentContext sc) {
        for (int i=0; i<this.segmentContexts.size(); i++) {
            if (this.segmentContexts.get(i) == sc) {
                if (sc.isRoot()) {
                    return null;
                }
                return this.segmentContexts.get(i-1);
            }
        }
        return null;
    }

    public boolean hasCollection() {
        for (SegmentContext sc : this.segmentContexts) {
            if (sc.getCollectionType() != CollectionType.NONE) {
                return true;
            }
        }
        return false;
    }

    public boolean isIndexedCollection() {
        boolean hasIndexedCollection = false;
        for (SegmentContext sc : this.segmentContexts) {
            if (sc.getCollectionType() != CollectionType.NONE) {
                if (sc.getCollectionIndex() != null) {
                    hasIndexedCollection = true;
                }

            }
        }
        return hasIndexedCollection;
    }

    public SegmentContext setCollectionIndex(int segmentIndex, Integer collectionIndex) {
        if (collectionIndex != null && collectionIndex < 0) {
            throw new IllegalArgumentException(String.format(
                    "Cannnot set negative collection index %s for the path %s",
                    collectionIndex, this.toString()));
        }
        SegmentContext sc = this.segmentContexts.get(segmentIndex);
        sc.collectionIndex = collectionIndex;
        return this.segmentContexts.set(segmentIndex, sc.rebuild());
    }

    public void copyCollectionIndexes(AtlasPath sourcePath) {
        int targetCollectionCount = getCollectionSegmentCount();
        int sourceCollectionCount = sourcePath.getCollectionSegmentCount();
        if (targetCollectionCount != sourceCollectionCount) {
            throw new IllegalArgumentException(String.format("Source has %d collections, whereas target has %d" +
                    " collections on the path. Target must have the same collection count as source or equal to 1.", sourceCollectionCount,
                targetCollectionCount));
        }

        List<SegmentContext> targetSegments = getSegments(true);
        int targetIndex = 0;
        for (SegmentContext sourceSegment : sourcePath.getSegments(true)) {
            if (sourceSegment.getCollectionType() != CollectionType.NONE && sourceSegment.getCollectionIndex() != null) {
                while (targetSegments.size() > targetIndex) {
                    SegmentContext targetSegment = targetSegments.get(targetIndex);
                    if (targetSegment.getCollectionType() != CollectionType.NONE) {
                        setCollectionIndex(targetIndex, sourceSegment.getCollectionIndex());
                        targetIndex++;
                        break;
                    } else {
                        targetIndex++;
                    }
                }
            }
        }
    }

    public SegmentContext setVacantCollectionIndex(Integer collectionIndex) {
        for (int i = 0; i < this.segmentContexts.size(); i++) {
            SegmentContext sc = segmentContexts.get(i);
            if (sc.getCollectionType() != CollectionType.NONE && sc.getCollectionIndex() == null) {
                return setCollectionIndex(i, collectionIndex);
            }
        }
        throw new IllegalArgumentException("No Vacant index on collection segments in the path " + this.toString());
    }

    public String getSegmentPath(SegmentContext sc) {
        int toIndex = this.segmentContexts.indexOf(sc);
        if (toIndex == -1) {
            return null;
        }
        StringBuilder builder = new StringBuilder().append(PATH_SEPARATOR_CHAR);
        if (!this.segmentContexts.get(0).getExpression().isEmpty()) {
            builder.append(this.segmentContexts.get(0).getExpression());
        }
        for (int i=1; i<=toIndex; i++) {
            if (!(builder.charAt(builder.length()-1) == PATH_SEPARATOR_CHAR)) {
                builder.append(PATH_SEPARATOR_CHAR);
            }
            builder.append(this.segmentContexts.get(i).getExpression());
        }
        return builder.toString();
    }

    @Override
    public String toString() {
        return getSegmentPath(getLastSegment());
    }

    public String getOriginalPath() {
        return originalPath;
    }

    public int getCollectionSegmentCount() {
        int answer = 0;
        for (SegmentContext sc : getSegments(true)) {
            if (sc.collectionType != null && sc.collectionType != CollectionType.NONE) {
                answer++;
            }
        }
        return answer;
    }

    public static class SegmentContext {

        private String name;
        private String expression;
        private CollectionType collectionType;
        private Integer collectionIndex;
        private String mapKey;
        private boolean isAttribute;
        private boolean isRoot;

        public SegmentContext(String expression) {
            this.expression = expression;
            if (this.expression.startsWith(PATH_SEPARATOR)) {
                this.expression = this.expression.replaceFirst(PATH_SEPARATOR, "");
            }
            this.name = cleanPathSegment(expression);
            if (expression.contains(PATH_MAP_START)) {
                this.collectionType = CollectionType.MAP;
            } else if (expression.contains(PATH_ARRAY_START)) {
                this.collectionType = CollectionType.ARRAY;
            } else if (expression.contains(PATH_LIST_START)) {
                this.collectionType = CollectionType.LIST;
            } else {
                this.collectionType = CollectionType.NONE;
            }
            if (this.collectionType == CollectionType.MAP) {
                this.mapKey = getMapKey(expression);
            } else {
                this.collectionIndex = getCollectionIndex(expression);
            }
            this.isAttribute = expression.startsWith(PATH_ATTRIBUTE_PREFIX);
            this.isRoot = this.name.isEmpty();
        }

        public String getName() {
            return name;
        }

        public String getExpression() {
            return expression;
        }

        public CollectionType getCollectionType() {
            return this.collectionType;
        }

        public Integer getCollectionIndex() {
            return this.collectionIndex;
        }

        public String getMapKey() {
            return this.mapKey;
        }

        public boolean isAttribute() {
            return isAttribute;
        }

        public boolean isRoot() {
            return isRoot;
        }

        protected SegmentContext rebuild() {
            StringBuilder buf = new StringBuilder();
            if (this.isAttribute) {
                buf.append(PATH_ATTRIBUTE_PREFIX);
            }
            buf.append(name);
            String index = collectionIndex != null ? collectionIndex.toString() : "";
            if (this.collectionType == CollectionType.ARRAY) {
                buf.append(PATH_ARRAY_START).append(index).append(PATH_ARRAY_END);
            } else if (this.collectionType == CollectionType.LIST) {
                buf.append(PATH_LIST_START).append(index).append(PATH_LIST_END);
            } else if (this.collectionType == CollectionType.MAP) {
                buf.append(PATH_LIST_START).append(mapKey).append(PATH_LIST_END);
            }
            return new SegmentContext(buf.toString());
        }

        @Override
        public String toString() {
            return collectionType == CollectionType.MAP
                ? String.format("SegmentContext [name=%s, expression=%s, collectionType=%s, mapKey=%s]",
                    name, expression, collectionType, mapKey)
                : String.format(
                    "SegmentContext [name=%s, expression=%s, collectionType=%s, collectionIndex=%s]",
                    name, expression, collectionType, collectionIndex);
        }

        private String cleanPathSegment(String expression) {
            String answer = expression;
            if (answer == null) {
                return null;
            }

            // strip namespace if there is one
            if (answer.contains(PATH_NAMESPACE_SEPARATOR)) {
                answer = answer.substring(answer.indexOf(PATH_NAMESPACE_SEPARATOR) + 1);
            }

            // strip leading @ symbol if there is one
            if (answer.startsWith(PATH_ATTRIBUTE_PREFIX)) {
                answer = answer.substring(1);
            }

            if (answer.contains(PATH_ARRAY_START) && answer.endsWith(PATH_ARRAY_END)) {
                return answer.substring(0, answer.indexOf(PATH_ARRAY_START, 0));
            }

            if (answer.contains(PATH_LIST_START) && answer.endsWith(PATH_LIST_END)) {
                return answer.substring(0, answer.indexOf(PATH_LIST_START, 0));
            }

            if (answer.contains(PATH_MAP_START) && answer.endsWith(PATH_MAP_END)) {
                return answer.substring(0, answer.indexOf(PATH_MAP_START, 0));
            }

            return answer;
        }

        private Integer getCollectionIndex(String expression) {
            if (expression == null) {
                return null;
            }

            if (expression.contains(PATH_ARRAY_START) && expression.endsWith(PATH_ARRAY_END)) {
                int start = expression.indexOf(PATH_ARRAY_START, 0) + 1;
                String index = expression.substring(start, expression.indexOf(PATH_ARRAY_END, start));
                if (index != null && index.length() > 0) {
                    return Integer.valueOf(index);
                }
                return null;
            }

            if (expression.contains(PATH_LIST_START) && expression.endsWith(PATH_LIST_END)) {
                int start = expression.indexOf(PATH_LIST_START, 0) + 1;
                String index = expression.substring(start, expression.indexOf(PATH_LIST_END, start));
                if (index != null && index.length() > 0) {
                    return Integer.valueOf(index);
                }
                return null;
            }

            return null;
        }

        private String getMapKey(String expression) {
            int start = expression.indexOf(PATH_MAP_START, 0) + 1;
            String key = expression.substring(start, expression.indexOf(PATH_MAP_END, start));
            if (key != null && key.length() > 0) {
                return key;
            }
            return null;
        }
    }

}
