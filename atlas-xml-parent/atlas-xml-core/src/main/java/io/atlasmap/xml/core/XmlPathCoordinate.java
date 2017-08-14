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

import java.util.Collections;
import java.util.Map;

public class XmlPathCoordinate {

    private Integer index;
    private String elementName;
    private Map<String, String> namespace;

    XmlPathCoordinate(Integer index, String elementName) {
        this.index = index;
        this.elementName = elementName;
    }

    Integer getIndex() {
        return index;
    }

    String getElementName() {
        return elementName;
    }

    public Map<String, String> getNamespace() {
        return namespace;
    }

    void setNamespace(String uri, String prefix) {
        namespace = Collections.singletonMap(uri, prefix);
    }

    @Override
    public String toString() {
        return "XmlPathCoordinate{" + "index=" + index + ", elementName='" + elementName + '\'' + ", namespace="
                + namespace + '}';
    }
}
