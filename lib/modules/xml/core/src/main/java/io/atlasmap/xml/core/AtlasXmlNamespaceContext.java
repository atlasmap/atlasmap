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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

public class AtlasXmlNamespaceContext implements NamespaceContext {
    protected Map<String, String> nsMap = new HashMap<>();
    private int nsIndex = 1;

    public AtlasXmlNamespaceContext() {
        nsMap.put(AtlasXmlConstants.NS_PREFIX_XMLSCHEMA, XMLConstants.W3C_XML_SCHEMA_NS_URI);
        nsMap.put(AtlasXmlConstants.NS_PREFIX_SCHEMASET, AtlasXmlConstants.ATLAS_XML_SCHEMASET_NAMESPACE);
    }

    public void add(String prefix, String uri) {
        nsMap.put(prefix, uri);
    }

    public String addWithIndex(String uri) {
        String prefix = "ns" + nsIndex++;
        while (nsMap.containsKey(prefix)) {
            prefix = "ns" + nsIndex++;
        }
        add(prefix, uri);
        return prefix;
    }

    public Map<String, String> getNamespaceMap() {
        return Collections.unmodifiableMap(nsMap);
    }

    @Override
    public String getNamespaceURI(String prefix) {
        return nsMap.get(prefix);
    }

    @Override
    public String getPrefix(String namespaceURI) {
        if (namespaceURI == null || namespaceURI.isEmpty()) {
            return null;
        }

        Optional<Entry<String, String>> entry = nsMap.entrySet().stream()
                .filter(e -> namespaceURI.equals(e.getValue())).findFirst();
        return entry.isPresent() ? entry.get().getKey() : null;
    }

    @Override
    public Iterator<?> getPrefixes(String namespaceURI) {
        if (namespaceURI == null || namespaceURI.isEmpty()) {
            return null;
        }

        return nsMap.entrySet().stream().filter(e -> namespaceURI.equals(e.getValue())).map(Entry::getKey)
                .collect(Collectors.toList()).iterator();
    }

}