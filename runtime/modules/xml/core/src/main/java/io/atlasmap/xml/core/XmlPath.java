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

import java.util.Map;

import io.atlasmap.core.AtlasPath;

public class XmlPath extends AtlasPath {

    public static final String PATH_SEPARATOR = "/";
    public static final String AT = "@";
    public static final String COLON = ":";

    public XmlPath(String path) {
        super(path);
    }

    public XmlPath(String path, Map<String, String> namespacesToReplace) {
        super(updatedPath(path, namespacesToReplace));
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
                        int index = part.indexOf(COLON);
                        if (index >= 0) {
                            if (part.startsWith(AT)) {
                                String namespace = part.substring(1, index);
                                if (namespacesToReplace.containsKey(namespace)) {
                                    updatedPath = updatedPath + PATH_SEPARATOR + AT + namespacesToReplace.get(namespace)
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

    public static Boolean isNamespaceSegment(String pathSegment) {
        return pathSegment != null && pathSegment.contains(":");
    }

    public static String getNamespace(String pathSeg) {
        String pathSegment = pathSeg;
        if (!isNamespaceSegment(pathSegment)) {
            return null;
        }
        pathSegment = pathSegment.substring(0, pathSegment.indexOf(':'));
        if (pathSegment.startsWith("@")) {
            pathSegment = pathSegment.substring(1);
        }
        return pathSegment;
    }

}
