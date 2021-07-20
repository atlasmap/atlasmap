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
package io.atlasmap.itests.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.LinkedList;

import org.junit.jupiter.api.Test;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.DefaultAtlasContextFactory;
import io.atlasmap.java.test.TargetContact;
import io.atlasmap.java.test.TargetTestClass;

public class DfdlCsvTest {

    @Test
    public void test() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/atlasmapping-dfdl-csv.json");
        AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(url.toURI());
        AtlasSession session = context.createSession();
        TargetTestClass javaSource = new TargetTestClass();
        javaSource.setContactList(new LinkedList<>());
        TargetContact tc = new TargetContact();
        tc.setFirstName("firstName1");
        tc.setLastName("lastName1");
        tc.setPhoneNumber("111-111-1111");
        javaSource.getContactList().add(tc);
        tc = new TargetContact();
        tc.setFirstName("firstName2");
        tc.setLastName("lastName2");
        tc.setPhoneNumber("222-222-2222");
        javaSource.getContactList().add(tc);
        tc = new TargetContact();
        tc.setFirstName("firstName3");
        tc.setLastName("lastName3");
        tc.setPhoneNumber("333-333-3333");
        javaSource.getContactList().add(tc);
        session.setSourceDocument("java-source", javaSource);
        String csvSource = TestHelper.readStringFromFile("atlas-dfdl-csv-simple.csv");
        session.setSourceDocument("dfdl-csv-source", csvSource);

        context.process(session);
        assertFalse(session.hasErrors(), TestHelper.printAudit(session));
        assertTrue(session.hasWarns(), TestHelper.printAudit(session));
        Object jt = session.getTargetDocument("java-target");
        assertEquals(TargetTestClass.class, jt.getClass());
        TargetTestClass javaTarget = TargetTestClass.class.cast(jt);
        assertEquals(3, javaTarget.getContactList().size());
        assertEquals("l1r1", javaTarget.getContactList().get(0).getFirstName());
        assertEquals("l3r3", javaTarget.getContactList().get(2).getPhoneNumber());
        Object dct = session.getTargetDocument("dfdl-csv-target");
        String dfdlCsvTarget = String.class.cast(dct);
        assertEquals(TestHelper.readStringFromFile("data/dfdl-csv-target.csv"), dfdlCsvTarget);
    }

}
