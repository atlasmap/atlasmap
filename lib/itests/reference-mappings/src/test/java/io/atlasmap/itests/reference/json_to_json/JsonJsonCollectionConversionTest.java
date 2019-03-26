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
package io.atlasmap.itests.reference.json_to_json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.itests.reference.AtlasMappingBaseTest;

public class JsonJsonCollectionConversionTest extends AtlasMappingBaseTest {

    @Test
    public void testProcessCollectionListSimple() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
                new File("src/test/resources/jsonToJson/atlasmapping-collection-list-simple.json").toURI());

        // contact<>.firstName -> contact<>.name

        String input = "{ \"contact\": [";
        for (int i = 0; i < 3; i++) {
            input += "{ \"firstName\": \"name" + i + "\"}";
            input += (i == 2) ? "" : ",";
        }
        input += "] }";

        AtlasSession session = context.createSession();
        session.setDefaultSourceDocument(input);
        context.process(session);

        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof String);

        String output = "{\"contact\":[";
        for (int i = 0; i < 3; i++) {
            output += "{\"name\":\"name" + i + "\"}";
            output += (i == 2) ? "" : ",";
        }
        output += "]}";
        assertEquals(output, object);
    }

    @Test
    public void testProcessCollectionArraySimple() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
                new File("src/test/resources/jsonToJson/atlasmapping-collection-array-simple.json").toURI());

        // contact[].firstName -> contact[].name

        String input = "{ \"contact\": [";
        for (int i = 0; i < 3; i++) {
            input += "{ \"firstName\": \"name" + i + "\"}";
            input += (i == 2) ? "" : ",";
        }
        input += "] }";

        AtlasSession session = context.createSession();
        session.setDefaultSourceDocument(input);
        context.process(session);

        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof String);

        String output = "{\"contact\":[";
        for (int i = 0; i < 3; i++) {
            output += "{\"name\":\"name" + i + "\"}";
            output += (i == 2) ? "" : ",";
        }
        output += "]}";
        assertEquals(output, object);
    }

    @Test
    public void testProcessCollectionToNonCollection() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
                new File("src/test/resources/jsonToJson/atlasmapping-collection-to-noncollection.json").toURI());

        // contact<>.firstName -> contact.name

        String input = "{ \"contact\": [";
        for (int i = 0; i < 3; i++) {
            input += "{ \"firstName\": \"name" + i + "\"}";
            input += (i == 2) ? "" : ",";
        }
        input += "] }";

        AtlasSession session = context.createSession();
        session.setDefaultSourceDocument(input);
        context.process(session);

        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof String);

        String output = "{\"contact\":{\"name\":\"name2\"}}";
        assertEquals(output, object);
    }

    @Test
    public void testProcessCollectionFromNonCollection() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
                new File("src/test/resources/jsonToJson/atlasmapping-collection-from-noncollection.json").toURI());

        // contact.firstName -> contact<>.name

        String input = "{ \"contact\": { \"firstName\": \"name9\" } }";
        AtlasSession session = context.createSession();
        session.setDefaultSourceDocument(input);
        context.process(session);

        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof String);

        String output = "{\"contact\":[{\"name\":\"name9\"}]}";
        assertEquals(output, object);
    }
}
