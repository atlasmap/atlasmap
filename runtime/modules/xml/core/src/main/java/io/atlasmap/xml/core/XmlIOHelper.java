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

    public static String writeDocumentToString(boolean stripSpaces, Node node) throws AtlasException {
        try {
            if (node == null) {
                return "";
            }
            TransformerFactory tf = TransformerFactory.newInstance();
            Transformer transformer = tf.newTransformer();
            transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
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

}
