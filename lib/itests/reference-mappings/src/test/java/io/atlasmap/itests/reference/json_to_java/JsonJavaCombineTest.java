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
package io.atlasmap.itests.reference.json_to_java;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.Test;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.java.test.TargetContact;
import io.atlasmap.itests.reference.AtlasMappingBaseTest;

public class JsonJavaCombineTest extends AtlasMappingBaseTest {

    @Test
    public void testProcessCombineSimple() throws Exception {
        AtlasSession session = processCombineMapping(
                "src/test/resources/jsonToJava/atlasmapping-combine-simple.xml",
                new String(Files.readAllBytes(Paths.get("src/test/resources/jsonToJava/atlas-json-combine.json"))));
        TargetContact targetContact = (TargetContact) session.getDefaultTargetDocument();
        assertEquals("Ozzie    Smith   5551212                                                                                            81111", targetContact.getFirstName());
        assertNull(targetContact.getLastName());
        assertNull(targetContact.getPhoneNumber());
        assertNull(targetContact.getZipCode());
        assertFalse(session.hasErrors());
    }

    @Test
    public void testProcessCombineSkip() throws Exception {
        AtlasSession session = processCombineMapping(
                "src/test/resources/jsonToJava/atlasmapping-combine-skip.xml",
                new String(Files.readAllBytes(Paths.get("src/test/resources/jsonToJava/atlas-json-combine.json"))));
        TargetContact targetContact = (TargetContact) session.getDefaultTargetDocument();
        assertEquals("Ozzie Smith 5551212 81111", targetContact.getFirstName());
        assertNull(targetContact.getLastName());
        assertNull(targetContact.getPhoneNumber());
        assertNull(targetContact.getZipCode());
        assertFalse(session.hasErrors());
    }

    @Test
    public void testProcessCombineOutOfOrder() throws Exception {
        AtlasSession session = processCombineMapping(
                "src/test/resources/jsonToJava/atlasmapping-combine-outoforder.xml",
                new String(Files.readAllBytes(Paths.get("src/test/resources/jsonToJava/atlas-json-combine.json"))));
        TargetContact targetContact = (TargetContact) session.getDefaultTargetDocument();
        assertEquals("Ozzie Smith 5551212 81111", targetContact.getFirstName());
        assertNull(targetContact.getLastName());
        assertNull(targetContact.getPhoneNumber());
        assertNull(targetContact.getZipCode());
        assertFalse(session.hasErrors());
    }

    @Test
    public void testProcessCombineNullInput() throws Exception {
        AtlasSession session = processCombineMapping(
                "src/test/resources/jsonToJava/atlasmapping-combine-inputnull.xml",
                new String(Files.readAllBytes(Paths.get("src/test/resources/jsonToJava/atlas-json-combine-null-lastName.json"))));
        TargetContact targetContact = (TargetContact) session.getDefaultTargetDocument();
        assertNotNull(targetContact);
        assertEquals("Ozzie  5551212 81111", targetContact.getFirstName());
        assertFalse(session.hasErrors());
    }

    protected AtlasSession processCombineMapping(String mappingFile, String sourceContact) throws Exception {
        AtlasContext context = atlasContextFactory.createContext(new File(mappingFile).toURI());
        AtlasSession session = context.createSession();
        session.setDefaultSourceDocument(sourceContact);
        context.process(session);

        Object object = session.getDefaultTargetDocument();
        assertNotNull(object);
        assertEquals(TargetContact.class.getName(), object.getClass().getName());
        return session;
    }
}
