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
package io.atlasmap.itests.core.issue;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;

import java.net.URL;

import org.junit.Before;
import org.junit.Test;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.AtlasMappingService;
import io.atlasmap.core.DefaultAtlasContextFactory;
import io.atlasmap.itests.core.TestHelper;
import io.atlasmap.v2.AtlasMapping;

public class ConstantPropertyTest {

    private AtlasMappingService mappingService;

    @Before
    public void before() {
        mappingService = DefaultAtlasContextFactory.getInstance().getMappingService();
    }

    @Test
    public void test() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/issue/constant-property-mapping.json");
        AtlasMapping mapping = mappingService.loadMapping(url);
        AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(mapping);
        AtlasSession session = context.createSession();

        context.process(session);
        assertFalse(TestHelper.printAudit(session), session.hasErrors());
        TargetClass output = TargetClass.class.cast(session.getTargetDocument("io.atlasmap.itests.core.issue.TargetClass"));
        assertEquals("testValue", output.getTargetName());
        assertNotEquals("testPath", output.getTargetFirstName());
        assertEquals(777, output.getTargetInteger());

        System.setProperty("testProp", "testProp-sysProp");
        System.setProperty("PATH", "PATH-sysProp");
        context.process(session);
        assertFalse(TestHelper.printAudit(session), session.hasErrors());
        output = TargetClass.class.cast(session.getTargetDocument("io.atlasmap.itests.core.issue.TargetClass"));
        assertEquals("testProp-sysProp", output.getTargetName());
        assertEquals("PATH-sysProp", output.getTargetFirstName());
        assertEquals(777, output.getTargetInteger());

        session.getProperties().put("testProp", "testProp-runtimeProp");
        session.getProperties().put("PATH", "PATH-runtimeProp");
        context.process(session);
        assertFalse(TestHelper.printAudit(session), session.hasErrors());
        output = TargetClass.class.cast(session.getTargetDocument("io.atlasmap.itests.core.issue.TargetClass"));
        assertEquals("testProp-runtimeProp", output.getTargetName());
        assertEquals("PATH-runtimeProp", output.getTargetFirstName());
        assertEquals(777, output.getTargetInteger());
    }

}
