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
package io.atlasmap.xml.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import io.atlasmap.core.AtlasPath;
import io.atlasmap.v2.CollectionType;

public class XmlPath extends AtlasPath {

    public static final String PATH_NAMESPACE_SEPARATOR = ":";

    public XmlPath(String path) {
        super(path);
    }

    public XmlPath(String path, Map<String, String> namespacesToReplace) {
        super(updatedPath(path, namespacesToReplace));
    }

    protected XmlSegmentContext createSegmentContext(String expression) {
        return new XmlSegmentContext(expression);
    }

    public List<XmlSegmentContext> getXmlSegments(boolean includeRoot) {
        List<XmlSegmentContext> answer = new ArrayList<>();
        int start = includeRoot ? 0 : 1;
        for (int i=start; i<segmentContexts.size(); i++) {
            answer.add((XmlSegmentContext)segmentContexts.get(i));
        }
        return Collections.unmodifiableList(answer);
    }

    public XmlSegmentContext getLastSegment() {
        return (XmlSegmentContext) super.getLastSegment();
    }

    public XmlPath getLastSegmentParentPath() {
        if (this.segmentContexts.isEmpty() || this.segmentContexts.size() == 1) {
            return null;
        }

        XmlPath parentPath = new XmlPath(null);
        for (int i = 0; i < this.segmentContexts.size() - 1; i++) {
            parentPath.appendField(this.segmentContexts.get(i).getExpression());
        }
        return parentPath;
    }

    private static String updatedPath(String fieldPath, Map<String, String> namespacesToReplace) {

        boolean isStartedWtSlash = false;
        String path = fieldPath;
        String updatedPath = "";
        if (namespacesToReplace != null && namespacesToReplace.size() > 0) {

            if (path != null && !"".equals(path)) {
                if (path.startsWith(PATH_SEPARATOR)) {
                    path = path.replaceFirst(PATH_SEPARATOR, "");
                    isStartedWtSlash = true;
                }
                if (path.contains(PATH_SEPARATOR)) {
                    String[] parts = path.split(PATH_SEPARATOR_ESCAPED, 512);
                    for (String part : parts) {
                        int index = part.indexOf(PATH_NAMESPACE_SEPARATOR);
                        if (index >= 0) {
                            if (part.startsWith(PATH_ATTRIBUTE_PREFIX)) {
                                String namespace = part.substring(1, index);
                                if (namespacesToReplace.containsKey(namespace)) {
                                    updatedPath = updatedPath + PATH_SEPARATOR + PATH_ATTRIBUTE_PREFIX + namespacesToReplace.get(namespace)
                                            + part.substring(index);
                                } else {
                                    updatedPath = updatedPath + PATH_SEPARATOR + part;
                                }
                            } else {
                                String namespace = part.substring(0, index);
                                if (namespacesToReplace.containsKey(namespace)) {
                                    updatedPath = updatedPath + PATH_SEPARATOR + namespacesToReplace.get(namespace)
                                            + part.substring(index);
                                } else {
                                    updatedPath = updatedPath + PATH_SEPARATOR + part;
                                }
                            }
                        } else {
                            updatedPath = updatedPath + PATH_SEPARATOR + part;
                        }
                    }
                } else {
                    updatedPath = path;
                }
                if (!isStartedWtSlash) {
                    updatedPath = updatedPath.substring(1);
                }
            } else {
                updatedPath = path;
            }

        }
        return updatedPath;

    }

    public static class XmlSegmentContext extends SegmentContext {
        private String namespace;
        private String qname;

        public XmlSegmentContext(String expression) {
            super(expression);
            if (getExpression().contains(PATH_NAMESPACE_SEPARATOR)) {
                String[] splitted = getExpression().split(PATH_NAMESPACE_SEPARATOR);
                namespace = isAttribute() ? splitted[0].replaceFirst(PATH_ATTRIBUTE_PREFIX, "") : splitted[0];
                qname = namespace + PATH_NAMESPACE_SEPARATOR + getName();
            } else {
                qname = getName();
            }
        }

        public String getNamespace() {
            return namespace;
        }

        public String getQName() {
            return qname;
        }

        protected XmlSegmentContext rebuild() {
            StringBuilder buf = new StringBuilder();
            if (isAttribute()) {
                buf.append(PATH_ATTRIBUTE_PREFIX);
            }
            buf.append(qname);
            String index = getCollectionIndex() != null ? getCollectionIndex().toString() : "";
            if (getCollectionType() == CollectionType.ARRAY) {
                buf.append(PATH_ARRAY_START).append(index).append(PATH_ARRAY_END);
            } else if (getCollectionType() == CollectionType.LIST) {
                buf.append(PATH_LIST_START).append(index).append(PATH_LIST_END);
            } else if (getCollectionType() == CollectionType.MAP) {
                buf.append(PATH_LIST_START).append(getMapKey()).append(PATH_LIST_END);
            }
            return new XmlSegmentContext(buf.toString());
        }

        @Override
        public String toString() {
            return getCollectionType() == CollectionType.MAP
                ? String.format("XmlSegmentContext [namespace=%s, name=%s, expression=%s, collectionType=%s, mapKey=%s]",
                    namespace, getName(), getExpression(), getCollectionType(), getMapKey())
                : String.format(
                    "XmlSegmentContext [namespace=%s, name=%s, expression=%s, collectionType=%s, collectionIndex=%s]",
                    namespace, getName(), getExpression(), getCollectionType(), getCollectionIndex());
        }

    }

}
