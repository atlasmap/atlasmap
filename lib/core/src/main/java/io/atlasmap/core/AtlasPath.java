/*
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.atlasmap.v2.AtlasModelFactory;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldGroup;

public class AtlasPath implements Cloneable {

    public static final String PATH_SEPARATOR = "/";
    public static final char PATH_SEPARATOR_CHAR = '/';
    public static final String PATH_SEPARATOR_ESCAPED = "/";
    public static final String PATH_ARRAY_START = "[";
    public static final String PATH_ARRAY_END = "]";
    public static final String PATH_ARRAY_SUFFIX = PATH_ARRAY_START + PATH_ARRAY_END;
    public static final String PATH_LIST_START = "<";
    public static final String PATH_LIST_END = ">";
    public static final String PATH_LIST_SUFFIX = PATH_LIST_START + PATH_LIST_END;
    public static final String PATH_MAP_START = "{";
    public static final String PATH_MAP_END = "}";
    public static final String PATH_MAP_SUFFIX = PATH_MAP_START + PATH_MAP_END;
    public static final String PATH_ATTRIBUTE_PREFIX = "@";
    public static final String PATH_NAMESPACE_SEPARATOR = ":";

    private static final Logger LOG = LoggerFactory.getLogger(AtlasPath.class);

    protected List<SegmentContext> segmentContexts;
    private String originalPath = null;

    public AtlasPath(String p) {
        String path = p;
        this.originalPath = path;
        this.segmentContexts = parse(path);
    }

    protected AtlasPath(List<SegmentContext> segments) {
        this.segmentContexts = segments;
        this.originalPath = getSegmentPath(segments.get(segments.size() - 1));
    }

    private AtlasPath() {
        this.segmentContexts = new ArrayList<>();
    }

    /**
     * Extract child fields by feeding relative path.
     *
     * @param f Parent field to extract from
     * @param path Relative path string
     * @return extracted field(s)
     */
    public static Field extractChildren(Field f, String path) {
        if (f == null || path == null || path.isEmpty()) {
            return null;
        }

        if (path.equals(PATH_SEPARATOR)) {
            return f;
        }
        if (!(f instanceof FieldGroup)) {
            return null;
        }

        List<Field> extracted = new ArrayList<>();
        FieldGroup entryField = (FieldGroup)f;
        extracted.add(entryField);
        List<SegmentContext> entrySegments = new AtlasPath(entryField.getPath()).getSegments(true);
        SegmentContext entrySegment = entrySegments.get(entrySegments.size() - 1);
        List<SegmentContext> extractedSegments = new ArrayList<>(entrySegments);
        List<SegmentContext> relativeSegments = new AtlasPath(path).getSegments(true);
        SegmentContext relativeRootSegment = relativeSegments.get(0);

        List<Field> selected = new ArrayList<>();
        if (relativeRootSegment.getCollectionType() == null || relativeRootSegment.getCollectionType() == CollectionType.NONE) {
            if (entrySegment.getCollectionType() != null
                && entrySegment.getCollectionType() != CollectionType.NONE
                && entrySegment.getCollectionIndex() == null) {
                selected.addAll(entryField.getField());
            } else {
                selected.add(entryField);
            }
        } else if (relativeRootSegment.getCollectionIndex() != null) {
            if (entrySegment.getCollectionIndex() != null) {
                if (entrySegment.getCollectionIndex() == relativeRootSegment.getCollectionIndex()) {
                    selected.add(entryField);
                }
            } else {
                selected.add(entryField.getField().get(relativeRootSegment.getCollectionIndex()));
                entrySegment.collectionIndex = relativeRootSegment.getCollectionIndex();
                extractedSegments.set(entrySegments.size() - 1, entrySegment.rebuild());
            }
        } else {
            selected.addAll(entryField.getField());
        }
        extracted = selected;

        for (int i=1; i<relativeSegments.size(); i++) {
            SegmentContext segment = relativeSegments.get(i);
            extractedSegments.add(segment);
            selected = new ArrayList<>();

            for (Field f1 : extracted) {
                FieldGroup f1Group = (FieldGroup)f1;
                for (Field f2 : f1Group.getField()) {
                    AtlasPath f2Path = new AtlasPath(f2.getPath());
                    if (!segment.getName().equals(f2Path.getLastSegment().getName())) {
                        continue;
                    }
                    if (segment.getCollectionType() == CollectionType.NONE) {
                        selected.add(f2);
                    } else {
                        FieldGroup f2Group = (FieldGroup)f2;
                        if (segment.getCollectionIndex() != null) {
                            selected.add((f2Group.getField().get(segment.getCollectionIndex())));
                        } else {
                            selected.addAll(f2Group.getField());
                        }
                    }
                    break;
                }
            }
            extracted = selected;
        }

        if (extracted.size() == 1) {
            return extracted.get(0);
        }
        FieldGroup answer = AtlasModelFactory.createFieldGroupFrom(f, true);
        answer.setPath(new AtlasPath(extractedSegments).toString());
        answer.getField().addAll(extracted);
        return answer;
    }

    public static void setCollectionIndexRecursively(FieldGroup group, int segmentIndex, int index) {
        AtlasPath path = new AtlasPath(group.getPath());
        path.setCollectionIndex(segmentIndex, index);
        group.setPath(path.toString());
        for (Field f : group.getField()) {
            if (f instanceof FieldGroup) {
                setCollectionIndexRecursively((FieldGroup)f, segmentIndex, index);
            } else {
                AtlasPath fpath = new AtlasPath(f.getPath());
                fpath.setCollectionIndex(segmentIndex, index);
                f.setPath(fpath.toString());
            }
        }
    }

    public AtlasPath appendField(String fieldExpression) {
        this.segmentContexts.add(createSegmentContext(fieldExpression));
        return this;
    }

    @Override
    public AtlasPath clone() {
        return new AtlasPath(this.toString());
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

    public SegmentContext getLastCollectionSegment() {
        List<SegmentContext> collectionSegments = getCollectionSegments(true);
        return collectionSegments.get(collectionSegments.size() - 1);
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

    public List<SegmentContext> getCollectionSegments(boolean includeRoot) {
        List<SegmentContext> segments = getSegments(includeRoot);
        List<SegmentContext> collectionSegments = new ArrayList<>();
        for (SegmentContext segment: segments) {
            if (segment.getCollectionType() != CollectionType.NONE) {
                collectionSegments.add(segment);
            }
        }
        return collectionSegments;
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

    protected List<SegmentContext> parse(String path) {
        path = sanitize(path);
        List<SegmentContext> segmentContexts = new ArrayList<>();
        if (path != null && !"".equals(path)) {
            if (path.startsWith(PATH_SEPARATOR)) {
                path = path.replaceFirst(PATH_SEPARATOR, "");
            }
            if (path.contains(PATH_SEPARATOR)) {
                String[] parts = path.split(PATH_SEPARATOR_ESCAPED, 512);
                for (String part : parts) {
                    segmentContexts.add(createSegmentContext(part));
                }
            } else {
                segmentContexts.add(createSegmentContext(path));
            }
        }
        if (segmentContexts.isEmpty() || !segmentContexts.get(0).isRoot()) {
            // add root segment if there's not
            segmentContexts.add(0, createSegmentContext(""));
        }
        return segmentContexts;
    }

    protected String sanitize(String path) {
        String answer = path;
        if (answer == null || answer.isEmpty()) {
            return answer;
        }
        if (answer.indexOf("//") != -1) {
            LOG.warn("Sanitizing double slash (//) in the path '{}'", answer);
            answer = answer.replaceAll("//", "/");
        }
        if (answer.endsWith("/")) {
            LOG.warn("Sanitizing trailing slash (/) in the path '{}'", answer);
            answer = answer.substring(0, answer.length()-1);

        }
        return answer;
    }

    protected SegmentContext createSegmentContext(String expression) {
        return new SegmentContext(expression);
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

        protected String cleanPathSegment(String expression) {
            String answer = expression;
            if (answer == null) {
                return null;
            }

            // strip namespace if there is one
            if (answer.contains(PATH_NAMESPACE_SEPARATOR)) {
                answer = answer.substring(answer.indexOf(PATH_NAMESPACE_SEPARATOR) + 1);
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
