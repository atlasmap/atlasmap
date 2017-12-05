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

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public abstract class XmlFieldTransformer {

    protected Map<String, String> namespaces = new HashMap<>();

    public XmlFieldTransformer() {
    }

    public XmlFieldTransformer(Map<String, String> namespaces) {
        this.namespaces = namespaces;
    }

    public void setNamespaces(Map<String, String> namespaces) {
        this.namespaces = namespaces;
    }

    protected void seedDocumentNamespaces(Document document) {
        NodeList nodeList = document.getChildNodes();
        if (namespaces == null) {
            namespaces = new LinkedHashMap<>();
        }
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                NamedNodeMap namedNodeMap = node.getAttributes();
                for (int x = 0; x < namedNodeMap.getLength(); x++) {
                    Node attribute = namedNodeMap.item(x);
                    if (attribute.getNamespaceURI() != null) {
                        if (attribute.getLocalName().equals("xmlns")) {
                            namespaces.put(attribute.getNodeValue(), "");
                        } else {
                            namespaces.put(attribute.getNodeValue(), attribute.getLocalName());
                        }
                    }
                }
            }
        }
    }

}
