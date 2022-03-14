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
package io.atlasmap.xml.core.schema;

import java.util.LinkedList;
import java.util.List;

import javax.xml.XMLConstants;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSIdentityConstraint;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSNotation;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.XSType;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.XSXPath;
import com.sun.xml.xsom.visitor.XSVisitor;

/**
 * Rewrite XML document instance to conform XML schema as a XSOM {@link XSVisitor}.
 */
public class AtlasRewritingXSVisitor implements XSVisitor {
    private final Node source;
    private final Node target;

    public AtlasRewritingXSVisitor(Node source, Node target) {
        this.source = source;
        this.target = target;
    }

    @Override
    public void attributeUse(XSAttributeUse use) {
        attributeDecl(use.getDecl());
    }

    @Override
    public void modelGroupDecl(XSModelGroupDecl decl) {
        modelGroup(decl.getModelGroup());
    }

    @Override
    public void modelGroup(XSModelGroup model) {
        for (XSParticle term : model.getChildren()) {
            term.visit(this);
        }
    }

    @Override
    public void particle(XSParticle particle) {
        XSTerm term = particle.getTerm();
        term.visit(this);
    }

    @Override
    public void complexType(XSComplexType complex) {
        for (XSAttributeUse use : complex.getAttributeUses()) {
            attributeUse(use);
        }

        XSContentType contentType = complex.getContentType();
        contentType.visit(this);
    }

    @Override
    public void elementDecl(XSElementDecl decl) {
        String namespaceUri = decl.getTargetNamespace();
        String localName = decl.getName();

        try {
            for (Element child : getChildElements(source, namespaceUri, localName)) {
                Document targetDoc = (target instanceof Document ? (Document)target : target.getOwnerDocument());
                Element targetChild;
                if (namespaceUri == null) {
                    targetChild = targetDoc.createElement(localName);
                } else {
                    targetChild = targetDoc.createElementNS(namespaceUri, localName);
                }
                target.appendChild(targetChild);

                XSType type = decl.getType();
                type.visit(new AtlasRewritingXSVisitor(child, targetChild));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void attributeDecl(XSAttributeDecl decl) {
        String namespaceUri = decl.getTargetNamespace();
        String localName = decl.getName();

        String attribute = getAttribute(source, namespaceUri, localName);
        if (attribute != null) {
            try {
                if (namespaceUri == null || XMLConstants.NULL_NS_URI.equals(namespaceUri)) {
                    ((Element)target).setAttribute(localName, attribute);
                } else {
                    ((Element)target).setAttributeNS(namespaceUri, localName, attribute);
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void simpleType(XSSimpleType simpleType) {
        String value = source.getTextContent();
        if (value != null) {
            try {
                target.setTextContent(value);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public void empty(XSContentType empty) {
    }

    @Override
    public void facet(XSFacet facet) {
    }

    @Override
    public void annotation(XSAnnotation ann) {
    }

    @Override
    public void schema(XSSchema schema) {
    }

    @Override
    public void notation(XSNotation notation) {
    }

    @Override
    public void identityConstraint(XSIdentityConstraint decl) {
    }

    @Override
    public void xpath(XSXPath xp) {
    }

    @Override
    public void wildcard(XSWildcard wc) {
    }

    @Override
    public void attGroupDecl(XSAttGroupDecl decl) {
    }

    private List<Element> getChildElements(Node node, String namespaceUri, String localName) {
        List<Element> answer = new LinkedList<>();
        NodeList nodeList = node.getChildNodes();
        for (int i = 0; i < nodeList.getLength(); i++) {
            Node n = nodeList.item(i);
            if (n.getNodeType() != Node.ELEMENT_NODE) {
                continue;
            }
            Element e = (Element)n;
            if ((namespaceUri == null || XMLConstants.NULL_NS_URI.equals(namespaceUri))
              && (e.getNamespaceURI() == null || XMLConstants.NULL_NS_URI.equals(e.getNamespaceURI()))) {
                  if (localName != null && localName.equals((e.getTagName()))) {
                      answer.add(e);
                  }
            } else if (namespaceUri != null && namespaceUri.equals(e.getNamespaceURI())
             && localName != null && localName.equals(e.getLocalName())) {
                 answer.add(e);
             }
        }
        return answer;
    }

    private String getAttribute(Node node, String namespaceUri, String localName) {
        if (namespaceUri == null) {
            namespaceUri = XMLConstants.NULL_NS_URI;
        }
        NamedNodeMap attrMap = node.getAttributes();
        if (attrMap == null) {
            return null;
        }
        if (XMLConstants.NULL_NS_URI.equals(namespaceUri)) {
            Node attr = attrMap.getNamedItem(localName);
            if (attr != null) {
                return attr.getNodeValue();
            }
        }
        Node attr = attrMap.getNamedItemNS(namespaceUri, localName);
        return attr != null ? attr.getNodeValue() : null;
    }

}
