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
package io.atlasmap.dfdl.core.schema;

import java.io.InputStream;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import io.atlasmap.dfdl.core.DfdlSchemaGenerator;
import io.atlasmap.api.AtlasException;
import io.atlasmap.dfdl.core.DfdlConstants;
import io.atlasmap.xml.core.XmlIOHelper;

/**
 * An implementation of {@code DfdlSchemaGenerator} for CSV document.
 * This class uses "csv-template.dfdl.xsd" as a template. By consuming CSV header line
 * as an option, it fills corresponding element into schema.
 */
public class CsvDfdlSchemaGenerator implements DfdlSchemaGenerator {
    public static final String NAME = "csv";
    public static final String DEFAULT_DELIMITER = ",";

    private static final Logger LOG = LoggerFactory.getLogger(CsvDfdlSchemaGenerator.class);
    private static final String TEMPLATE_FILE = "csv-template.dfdl.xsd";
    private static final String NS_XS = "http://www.w3.org/2001/XMLSchema";
    private static final String NS_DFDL = "http://www.ogf.org/dfdl/dfdl-1.0/";
    private static final String NS_ATLAS = "http://atlasmap.io/dfdl/csv";

    private XmlIOHelper helper = new XmlIOHelper(CsvDfdlSchemaGenerator.class.getClassLoader());

    public enum Options {
        HEADER(DfdlConstants.OPTION_PREFIX + ".csv.header"),
        EXAMPLE(DfdlConstants.OPTION_EXAMPLE_DATA),
        DELIMITER(DfdlConstants.OPTION_PREFIX + ".csv.delimiter");

        private final String value;

        Options(String value) {
            this.value = value;
        }

        public String value() {
            return this.value;
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String[] getOptions() {
        return EnumSet.allOf(Options.class).stream().map(e -> e.value).toArray(String[]::new);
    }

    @Override
    public Document generate(ClassLoader classLoader, Map<String, String> options) throws Exception {
        String header = (String) options.get(Options.HEADER.value());
        String example = (String) options.get(Options.EXAMPLE.value());
        String delimiter = (String) options.get(Options.DELIMITER.value());
        if ((header == null || header.isEmpty()) && (example == null || example.isEmpty())) {
            throw new AtlasException(String.format("'%s' or '%s' must be specified to generate CSV DFDL schema",
                Options.HEADER.value(), Options.EXAMPLE.value()));
        }
        header = header!= null && !header.isEmpty() ? header.split("\\R")[0] : example.split("\\R")[0];
        if (delimiter == null) {
            delimiter = DEFAULT_DELIMITER;
        }
        String[] fieldNames = header.split(delimiter);

        InputStream is = classLoader.getResourceAsStream(TEMPLATE_FILE);
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        Document xsd = factory.newDocumentBuilder().parse(is);
        XPath xpath = XPathFactory.newInstance().newXPath();
        xpath.setNamespaceContext(new NamespaceResolver());
        String targetPath = "//xs:schema/xs:element[@name='file']/xs:complexType/xs:sequence"
                        + "/xs:element[@name='record']/xs:complexType/xs:sequence";
        Node parentNode = (Node) xpath.compile(targetPath).evaluate(xsd, XPathConstants.NODE);
        if (parentNode == null) {
            throw new AtlasException(String.format("Invalid DFDL template for CSV format: path '%s' could not be found", targetPath));
        }
        parentNode.getAttributes().getNamedItemNS(NS_DFDL, "separator").setNodeValue(delimiter);
        for (String fieldName : fieldNames) {
            Element e = xsd.createElementNS(NS_XS, "element");
            e.setAttribute("name", fieldName);
            e.setAttribute("type", "xs:string");
            e.setAttribute("minOccurs", "1");
            e.setAttribute("maxOccurs", "1");
            parentNode.appendChild(e);
        }
        if (LOG.isDebugEnabled()) {
            LOG.debug("Generated CSV DFDL Schema:");
            LOG.debug(helper.writeDocumentToString(false, xsd));
        }
        return xsd;
    }

    class NamespaceResolver implements NamespaceContext {
        private Map<String, String> nsmap = new HashMap<>();

        public NamespaceResolver() {
            nsmap.put("xs", NS_XS);
            nsmap.put("dfdl", NS_DFDL);
            nsmap.put("atlas", NS_ATLAS);
        }

        @Override
        public String getNamespaceURI(String prefix) {
            return nsmap.get(prefix);
        }

        @Override
        public String getPrefix(String namespaceURI) {
            for (Entry<String, String> set : nsmap.entrySet()) {
                if (set.getValue().equals(namespaceURI)) {
                    return set.getKey();
                }
            }
            return null;
        }

        @Override
        public Iterator<String> getPrefixes(String namespaceURI) {
            List<String> prefixes = new LinkedList<>();
            for (Entry<String, String> set : nsmap.entrySet()) {
                if (set.getValue().equals(namespaceURI)) {
                    prefixes.add(set.getKey());
                }
            }
            return prefixes.iterator();
        }

    }

}