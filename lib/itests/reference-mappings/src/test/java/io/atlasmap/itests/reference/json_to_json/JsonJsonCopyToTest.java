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
package io.atlasmap.itests.reference.json_to_json;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.itests.reference.AtlasMappingBaseTest;
import io.atlasmap.v2.CopyTo;


import static org.junit.Assert.*;

public class JsonJsonCopyToTest extends AtlasMappingBaseTest {

    private AtlasSession session;
    private AtlasContext context;
    private String input = "{ \"contact\": { \"firstName\": \"name9\" } }";

    @Before
    public void setup() throws Exception {
        context = atlasContextFactory.createContext(
            new File("src/test/resources/jsonToJson/atlasmapping-empty-mapping.json").toURI());
        session = context.createSession();
    }

    @Test
    // contact.firstName -> contact<1>.name
    public void testCopyToSingleOutput() throws Exception {

        JsonTestHelper.addInputStringField(session,"/contact/firstName");
        JsonTestHelper.addOutputStringField(session,"/contact<>/name");
        JsonTestHelper.addInputMappings(session,new CopyTo("2"));

        session.setDefaultSourceDocument(input);
        context.process(session);

        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof String);

        String output = "{\"contact\":[{},{\"name\":\"name9\"}]}";
        assertEquals(output, object);
    }

    @Test
    // contact.firstName -> contact<0>/foreigner<1>.name
    public void testCopyToNestedOutput() throws Exception {

        JsonTestHelper.addInputStringField(session,"/contact/firstName");
        JsonTestHelper.addOutputStringField(session,"/contact<>/foreigner<>/name");
        JsonTestHelper.addInputMappings(session, new CopyTo("1,2"));

        session.setDefaultSourceDocument(input);
        context.process(session);

        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof String);

        String output = "{\"contact\":[{\"foreigner\":[{},{\"name\":\"name9\"}]}]}";
        assertEquals(output, object);
    }

    @Test
    // contact.firstName -> contact<0>/foreigner<1>.name
    public void testCopyToMultipleOutputs() throws Exception {

        JsonTestHelper.addInputStringField(session,"/contact/firstName");
        JsonTestHelper.addOutputStringField(session,"/contact<>/foreigner<>/name");
        JsonTestHelper.addOutputStringField(session,"/contact<>/name");
        JsonTestHelper.addInputMappings(session, new CopyTo("1,2"));

        session.setDefaultSourceDocument(input);
        context.process(session);

        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertTrue(object instanceof String);

        String output = "{\"contact\":[{\"foreigner\":[{},{\"name\":\"name9\"}],\"name\":\"name9\"}]}";
        assertEquals(output, object);
    }
}
