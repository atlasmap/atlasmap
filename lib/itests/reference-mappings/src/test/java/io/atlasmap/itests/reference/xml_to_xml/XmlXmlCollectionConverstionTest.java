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
package io.atlasmap.itests.reference.xml_to_xml;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.itests.reference.AtlasMappingBaseTest;

public class XmlXmlCollectionConverstionTest extends AtlasMappingBaseTest {

    @Test
    public void testProcessCollectionListSimple() throws Exception {
        AtlasContext context = atlasContextFactory
                .createContext(new File("src/test/resources/xmlToXml/atlasmapping-collection-list-simple.xml").toURI());

        // contact<>.firstName -> contact<>.name

        String input = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        input += "<XmlOA>";
        for (int i = 0; i < 3; i++) {
            input += "<contact><firstName>name" + i + "</firstName></contact>";
        }
        input += "</XmlOA>";

        AtlasSession session = context.createSession();
        session.setDefaultSourceDocument(input);
        context.process(session);

        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof String);
        String output = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>";
        output += "<XmlOA>";
        for (int i = 0; i < 3; i++) {
            output += "<contact><name>name" + i + "</name></contact>";
        }
        output += "</XmlOA>";
        assertEquals(output, object);
    }

    @Test
    public void testProcessCollectionArraySimple() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
                new File("src/test/resources/xmlToXml/atlasmapping-collection-array-simple.xml").toURI());

        // contact[].firstName -> contact[].name

        String input = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        input += "<XmlOA>";
        for (int i = 0; i < 3; i++) {
            input += "<contact><firstName>name" + i + "</firstName></contact>";
        }
        input += "</XmlOA>";

        AtlasSession session = context.createSession();
        session.setDefaultSourceDocument(input);
        context.process(session);

        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof String);

        String output = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>";
        output += "<XmlOA>";
        for (int i = 0; i < 3; i++) {
            output += "<contact><name>name" + i + "</name></contact>";
        }
        output += "</XmlOA>";

        assertEquals(output, object);
    }

    @Test
    public void testProcessCollectionToNonCollection() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
                new File("src/test/resources/xmlToXml/atlasmapping-collection-to-noncollection.xml").toURI());

        // contact<>.firstName -> contact.name

        String input = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        input += "<XmlOA>";
        for (int i = 0; i < 3; i++) {
            input += "<contact><firstName>name" + i + "</firstName></contact>";
        }
        input += "</XmlOA>";

        AtlasSession session = context.createSession();
        session.setDefaultSourceDocument(input);
        context.process(session);

        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof String);
        String output = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>";
        output += "<XmlOA>";
        output += "<contact><name>name2</name></contact>";
        output += "</XmlOA>";
        assertEquals(output, object);
    }

    @Test
    public void testProcessCollectionFromNonCollection() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
                new File("src/test/resources/xmlToXml/atlasmapping-collection-from-noncollection.xml").toURI());

        // contact.firstName -> contact<>.name

        String input = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
        input += "<XmlOA>";
        input += "<contact><firstName>name76</firstName></contact>";
        input += "</XmlOA>";

        AtlasSession session = context.createSession();
        session.setDefaultSourceDocument(input);
        context.process(session);

        String output = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>";
        output += "<XmlOA>";
        output += "<contact><name>name76</name></contact>";
        output += "</XmlOA>";

        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof String);
        assertEquals(output, object);
    }
}
