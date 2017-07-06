package io.atlasmap.xml.inspect.v2;

import io.atlasmap.xml.v2.XmlDocument;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 */
public class XmlDocumentInspectionService {


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


    private Document getDocument(InputStream is, boolean namespaced) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setNamespaceAware(namespaced); //this must be done to use namespaces
        DocumentBuilder b = dbf.newDocumentBuilder();
        return b.parse(is);
    }
}
