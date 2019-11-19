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

import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import io.atlasmap.api.AtlasException;

public final class XmlIOHelper {

    private TransformerFactory transformerFactory;

    public XmlIOHelper(ClassLoader cl) {
        ClassLoader origTccl = Thread.currentThread().getContextClassLoader();
        try {
            Thread.currentThread().setContextClassLoader(cl);
            this.transformerFactory = TransformerFactory.newInstance();
        } finally {
            Thread.currentThread().setContextClassLoader(origTccl);
        }
    }

    public static List<Element> getChildrenWithName(String name, Element parentNode) {
        List<Element> children = new LinkedList<>();
        if (parentNode == null) {
            return children;
        }
        NodeList nodeChildren = parentNode.getChildNodes();
        for (int i = 0; i < nodeChildren.getLength(); i++) {
            Node child = nodeChildren.item(i);
            if ((child instanceof Element) && child.getNodeName().equals(name)) {
                children.add((Element) child);
            }
        }
        return children;
    }

    public static List<Element> getChildrenWithNameStripAlias(String name, Optional<String> namespace, Element parentNode) {
        List<Element> children = new LinkedList<>();
        if (parentNode == null) {
            return children;
        }
        NodeList nodeChildren = parentNode.getChildNodes();
        for (int i = 0; i < nodeChildren.getLength(); i++) {
            Node child = nodeChildren.item(i);
            String nodeName = getNodeNameWithoutNamespaceAlias(child);
            if ((child instanceof Element) && nodeName.equals(name)) {
                if (!namespace.isPresent()) {
                    children.add((Element) child);
                } else if (namespace.get().equals(child.getNamespaceURI())) {
                    children.add((Element) child);
                }
            }
        }
        return children;
    }

    public String writeDocumentToString(boolean stripSpaces, Node node) throws AtlasException {
        try {
            if (node == null) {
                return "";
            }
            Transformer transformer = transformerFactory.newTransformer();
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(node), new StreamResult(writer));

            String result = writer.getBuffer().toString();
            if (stripSpaces) {
                result = result.replaceAll("\n|\r", "");
                result = result.replaceAll("> *?<", "><");
            }
            return result;
        } catch (Exception e) {
            throw new AtlasException(e);
        }
    }

    public static String getNodeNameWithoutNamespaceAlias(Node child) {
        String nodeName = child.getNodeName();
        int index = nodeName.indexOf(":");
        if (index >= 0) {
            nodeName = nodeName.substring(index + 1);
        }
        return nodeName;
    }

}
