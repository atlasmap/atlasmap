/**
 * Copyright (C) 20 Red Hat, Inc.
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

import static io.atlasmap.xml.core.AtlasXmlConstants.NS_PREFIX_SCHEMASET;
import static io.atlasmap.xml.core.AtlasXmlConstants.NS_PREFIX_XMLSCHEMA;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.parser.XSOMParser;
import com.sun.xml.xsom.util.DomAnnotationParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import io.atlasmap.api.AtlasException;
import io.atlasmap.xml.core.AtlasXmlNamespaceContext;

public class AtlasXmlSchemaSetParser {

    private static final Logger LOG = LoggerFactory.getLogger(AtlasXmlSchemaSetParser.class);
    private ClassLoader classLoader;
    private AtlasXmlNamespaceContext namespaceContext;
    private String rootNamespace;
    private SAXParserFactory saxParserFactory;
    private Transformer transformer;
    private DocumentBuilder documentBuilder;

    public AtlasXmlSchemaSetParser(ClassLoader cl) throws AtlasException {
        this.classLoader = cl;
        this.namespaceContext = new AtlasXmlNamespaceContext();
        this.saxParserFactory = SAXParserFactory.newInstance();
        try {
            this.transformer = TransformerFactory.newInstance().newTransformer();
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            this.documentBuilder = dbf.newDocumentBuilder();
        } catch (Exception e) {
            throw new AtlasException(e);
        }
    }

    /**
     * Parse single XML Schema or SchemaSet which contains multiple XML Schema and
     * build a {@link XSSchemaSet}.
     * @param root DOM {@link Document} instance of XML Schema
     * @return parsed {@link XSSchemaSet}
     */
    public XSSchemaSet parse(Document doc) throws AtlasException {
        XSOMParser xsomParser = createXSOMParser();
        parseInternal(doc, n -> {
            try {
                xsomParser.parse(toInputStream(n));
            } catch (Exception e) {
                throw new AtlasException(e);
            }
        });
        try {
            return xsomParser.getResult();
        } catch (Exception e) {
            throw new AtlasException(e);
        }
    }

    /**
     * Parse XML Schema or SchemaSet which contains multiple XML Schema and
     * build a {@link XSSchemaSet}.
     * @param in {@link InputStream} of XML Schema document
     * @return parsed {@link XSSchemaSet}
     */
    public XSSchemaSet parse(InputStream in) throws AtlasException {
        try {
            Document doc = this.documentBuilder.parse(in);
            return parse(doc);
        } catch (Exception e) {
            throw new AtlasException(e);
        }
    }

    /**
     * Parse single XML Schema or SchemaSet which contains multiple XML Schema and
     * build a {@link Schema}.
     * @return
     * @throws AtlasException
     */
    public Schema createSchema(InputStream in) throws AtlasException {
        SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        List<Source> schemaSources = new LinkedList<>();
        Document doc;
        try {
            doc = this.documentBuilder.parse(in);
            parseInternal(doc, n -> {
                DOMSource s = new DOMSource(n);
                schemaSources.add(s);
            });
            factory.setErrorHandler(new NoopErrorHandler());
            return factory.newSchema(schemaSources.toArray(new Source[0]));
        } catch (AtlasException e) {
            throw e;
        } catch (Exception e2) {
            throw new AtlasException(e2);
        }
    }

    public void setNamespaceContext(AtlasXmlNamespaceContext nsc) {
        this.namespaceContext = nsc;
    }

    public AtlasXmlNamespaceContext getNamespaceContext() {
        return this.namespaceContext;
    }

    public void setRootNamespace(String rootns) {
        this.rootNamespace = rootns;
    }

    public String getRootNamespace() {
        return this.rootNamespace;
    }

    private String getTargetNamespace(Node n) {
        NamedNodeMap attributes = n.getAttributes();
        if (attributes == null) {
            return "";
        }
        Attr tns = (Attr) attributes.getNamedItem("targetNamespace");
        return tns != null ? tns.getValue() : "";
    }

    @FunctionalInterface
    private interface ParserCallback {
        void addSchema(Node n) throws AtlasException;
    }

    private class NoopErrorHandler implements ErrorHandler {

        @Override
        public void warning(SAXParseException e) throws SAXException {
            if (LOG.isDebugEnabled()) {
                LOG.debug("warning", e);
            }
        }

        @Override
        public void error(SAXParseException e) throws SAXException {
            if (LOG.isDebugEnabled()) {
                LOG.debug("error", e);
            }
        }

        @Override
        public void fatalError(SAXParseException e) throws SAXException {
            if (LOG.isDebugEnabled()) {
                LOG.debug("fatalError", e);
            }
        }
        
    }

    private void parseInternal(Document doc, ParserCallback callback) throws AtlasException {
        try {
            Element root = doc.getDocumentElement();
            if ("SchemaSet".equals(root.getLocalName())) {
                XPath xpath = XPathFactory.newInstance().newXPath();
                xpath.setNamespaceContext(this.namespaceContext);
                NodeList subSchemas = (NodeList) xpath
                        .evaluate(String.format("/%s:SchemaSet/%s:AdditionalSchemas/%s:schema", NS_PREFIX_SCHEMASET,
                                NS_PREFIX_SCHEMASET, NS_PREFIX_XMLSCHEMA), root, XPathConstants.NODESET);
                for (int i = 0; i < subSchemas.getLength(); i++) {
                    Element e = (Element) subSchemas.item(i);
                    inheritNamespaces(e, false);
                    callback.addSchema(e);
                }

                Element rootSchema = (Element) xpath.evaluate(
                        String.format("/%s:SchemaSet/%s:schema", NS_PREFIX_SCHEMASET, NS_PREFIX_XMLSCHEMA), root,
                        XPathConstants.NODE);
                if (rootSchema == null) {
                    throw new AtlasException(
                            "The root schema '/SchemaSet/schema' must be specified once and only once");
                }
                this.rootNamespace = getTargetNamespace(rootSchema);
                if (this.rootNamespace != null && !this.rootNamespace.isEmpty()) {
                    this.namespaceContext.add("tns", this.rootNamespace);
                }
                inheritNamespaces(rootSchema, true);
                callback.addSchema(rootSchema);
            } else if ("schema".equals(root.getLocalName())) {
                callback.addSchema(root);
                this.rootNamespace = getTargetNamespace(root);
                if (this.rootNamespace != null && !this.rootNamespace.isEmpty()) {
                    this.namespaceContext.add("tns", this.rootNamespace);
                }
            } else {
                throw new AtlasException(
                        String.format("Unsupported document element '%s': root element must be 'schema' or 'SchemaSet'",
                                root.getLocalName()));
            }
        } catch (Exception e) {
            throw new AtlasException(e);
        }
    }

    private XSOMParser createXSOMParser() {
        XSOMParser parser = new XSOMParser(this.saxParserFactory);
        parser.setEntityResolver(new XSOMClasspathEntityResolver(this.classLoader));
        parser.setAnnotationParser(new DomAnnotationParserFactory());
        parser.setErrorHandler(new XSOMErrorHandler());
        return parser;
    }

    private void inheritNamespaces(Element element, boolean updateContext) {
        Node target = element.getParentNode();
        while (target != null) {
            NamedNodeMap attributes = target.getAttributes();
            if (attributes != null) {
                for (int i = 0; i < attributes.getLength(); i++) {
                    Attr attr = (Attr) attributes.item(i);
                    if ("xmlns".equals(attr.getPrefix()) && !"xmlns".equals(attr.getLocalName())) {
                        element.setAttribute(attr.getName(), attr.getValue());
                        if (updateContext) {
                            namespaceContext.add(attr.getLocalName(), attr.getValue());
                        }
                    }
                }
            }
            target = target.getParentNode();
        }
    }

    private ByteArrayInputStream toInputStream(Node n) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        this.transformer.transform(new DOMSource(n), new StreamResult(baos));
        byte[] output = baos.toByteArray();
        if (LOG.isTraceEnabled()) {
            LOG.trace(">>> {}", new String(output));
        }
        return new ByteArrayInputStream(output);
    }

}