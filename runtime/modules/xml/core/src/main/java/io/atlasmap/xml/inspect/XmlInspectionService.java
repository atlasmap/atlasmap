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
package io.atlasmap.xml.inspect;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import io.atlasmap.xml.v2.XmlDocument;

public class XmlInspectionService {

    public XmlDocument inspectXmlDocument(String sourceDocument) throws XmlInspectionException {
        if (sourceDocument == null || sourceDocument.isEmpty()) {
            throw new IllegalArgumentException("Source must be specified");
        }
        Document document;
        try {
            document = getDocument(new ByteArrayInputStream(sourceDocument.getBytes()), true);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new XmlInspectionException(e.getMessage(), e);
        }
        return inspectXmlDocument(document);
    }

    public XmlDocument inspectXmlDocument(File sourceDocument) throws XmlInspectionException {
        if (sourceDocument == null) {
            throw new IllegalArgumentException("Source must be specified");
        }
        Document document;
        try {
            document = getDocument(new FileInputStream(sourceDocument), true);
        } catch (Exception e) {
            throw new XmlInspectionException(e.getMessage(), e);
        }
        return inspectXmlDocument(document);
    }

    public XmlDocument inspectXmlDocument(Document sourceDocument) throws XmlInspectionException {
        if (sourceDocument == null) {
            throw new IllegalArgumentException("Source must be specified");
        }
        InstanceInspector inspector = new InstanceInspector();
        inspector.inspect(sourceDocument);
        return inspector.getXmlDocument();
    }

    public XmlDocument inspectSchema(String schemaSource) throws XmlInspectionException {
        if (schemaSource == null || schemaSource.isEmpty()) {
            throw new IllegalArgumentException("Source must be specified");
        }
        SchemaInspector inspector = new SchemaInspector();
        inspector.inspect(schemaSource);
        return inspector.getXmlDocument();
    }

    public XmlDocument inspectSchema(File schemaFile) throws XmlInspectionException {
        if (schemaFile == null || !schemaFile.exists()) {
            throw new IllegalArgumentException("Source must be specified and available");
        }
        SchemaInspector inspector = new SchemaInspector();
        inspector.inspect(schemaFile);
        return inspector.getXmlDocument();
    }

    private Document getDocument(InputStream is, boolean namespaced)
            throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(namespaced); // this must be done to use namespaces
        DocumentBuilder b = dbf.newDocumentBuilder();
        return b.parse(is);
    }
}
