package io.atlasmap.xml.test.v2;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import com.sun.xml.bind.marshaller.NamespacePrefixMapper;
import com.sun.xml.bind.v2.WellKnownNamespace;

public class AtlasXmlTestHelper {

    private static JAXBContext jaxbContext = null;
    private static Unmarshaller unmarshaller = null;
    private static Marshaller marshaller = null;

    public static void init() throws Exception {
        jaxbContext = JAXBContext.newInstance("io.atlasmap.xml.test.v2");
        unmarshaller = jaxbContext.createUnmarshaller();
        marshaller = jaxbContext.createMarshaller();
        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
        marshaller.setProperty("com.sun.xml.bind.namespacePrefixMapper", new NamespacePrefixMapper() {
            @Override
            public String[] getPreDeclaredNamespaceUris() {
                return new String[] { XMLConstants.W3C_XML_SCHEMA_NS_URI, XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI };
            }

            @Override
            public String getPreferredPrefix(String namespaceUri, String suggestion, boolean requirePrefix) {
                if (namespaceUri.equals(XMLConstants.W3C_XML_SCHEMA_INSTANCE_NS_URI))
                    return "xsi";
                if (namespaceUri.equals(XMLConstants.W3C_XML_SCHEMA_NS_URI))
                    return "xs";
                if (namespaceUri.equals(WellKnownNamespace.XML_MIME_URI))
                    return "xmime";
                return suggestion;

            }
        });
    }

    public static Object unmarshal(String xmlData, Class<?> clazz) throws Exception {
        StreamSource data = new StreamSource(new StringReader(xmlData));

        if (unmarshaller == null) {
            init();
        }
        return unmarshaller.unmarshal(data, clazz);
    }

    public static String marshal(Object object) throws Exception {
        StringWriter writer = new StringWriter();
        if (marshaller == null) {
            init();
        }
        marshaller.marshal(object, writer);
        return writer.toString();
    }
}
