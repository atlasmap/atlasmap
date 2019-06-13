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
package io.atlasmap.itests.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.AtlasMappingService;
import io.atlasmap.core.DefaultAtlasContextFactory;
import io.atlasmap.itests.core.issue.SourceClass;
import io.atlasmap.itests.core.issue.TargetClass;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.AuditStatus;

public class MultiplicityTransformationTest {

    private AtlasMappingService mappingService;

    @Before
    public void before() {
        mappingService = DefaultAtlasContextFactory.getInstance().getMappingService();
    }

    @Test
    public void testConcatenateSplit() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/atlasmapping-multiplicity-transformation-concatenate-split.json");
        AtlasMapping mapping = mappingService.loadMapping(url);
        AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(mapping);
        AtlasSession session = context.createSession();
        SourceClass source = new SourceClass()
                .setSourceFirstName("Manjiro")
                .setSourceLastName("Nakahama")
                .setSourceName("Manjiro,Nakahama")
                .setSourceString("one,two,three")
                .setSourceStringList(Arrays.asList(new String[] {"one", "two", "three"}))
                .setSourceHiphenatedInteger("1-20-300-4000");
        session.setSourceDocument("io.atlasmap.itests.core.issue.SourceClass", source);
        context.process(session);
        assertFalse(TestHelper.printAudit(session), session.hasErrors());
        assertTrue("split(STRING) => INTEGER mapping should get 3 warnings", session.hasWarns());
        assertEquals(3, session.getAudits().getAudit().stream().filter(a -> a.getStatus() == AuditStatus.WARN).count());
        Object output = session.getTargetDocument("io.atlasmap.itests.core.issue.TargetClass");
        assertEquals(TargetClass.class, output.getClass());
        TargetClass target = TargetClass.class.cast(output);
        assertEquals("Manjiro", target.getTargetFirstName());
        assertEquals("Nakahama", target.getTargetLastName());
        assertEquals("Manjiro,Nakahama", target.getTargetName());
        assertEquals("one,two,three", target.getTargetString());
        List<String> list = target.getTargetStringList();
        assertEquals(3, list.size());
        assertEquals("one", list.get(0));
        assertEquals("two", list.get(1));
        assertEquals("three", list.get(2));
        List<Integer> intList = target.getTargetIntegerList();  
        assertEquals(4, intList.size());
        assertEquals(new Integer(1), intList.get(0));
        assertEquals(new Integer(20), intList.get(1));
        assertEquals(new Integer(300), intList.get(2));
        assertEquals(new Integer(4000), intList.get(3));
    }

    @Test
    public void testItemAt() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/atlasmapping-multiplicity-transformation-itemAt.json");
        AtlasMapping mapping = mappingService.loadMapping(url);
        AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(mapping);
        AtlasSession session = context.createSession();
        SourceClass source = new SourceClass().setSourceStringList(Arrays.asList(new String[] {"one", "two", "three"}));
        session.setSourceDocument("io.atlasmap.itests.core.issue.SourceClass", source);
        context.process(session);
        assertFalse(TestHelper.printAudit(session), session.hasErrors());
        assertFalse(TestHelper.printAudit(session), session.hasWarns());
        Object output = session.getTargetDocument("io.atlasmap.itests.core.issue.TargetClass");
        assertEquals(TargetClass.class, output.getClass());
        TargetClass target = TargetClass.class.cast(output);
        assertEquals("two", target.getTargetString());
    }

    @Test
    public void testExpression() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/atlasmapping-multiplicity-transformation-expression.json");
        AtlasMapping mapping = mappingService.loadMapping(url);
        AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(mapping);
        AtlasSession session = context.createSession();
        SourceClass source = new SourceClass()
                                .setSourceString("")
                                .setSourceInteger(123)
                                .setSourceFirstName(null)
                                .setSourceLastName("")
                                .setSourceInteger2(-123);
        session.setSourceDocument("io.atlasmap.itests.core.issue.SourceClass", source);
        context.process(session);
        assertFalse(TestHelper.printAudit(session), session.hasErrors());
        assertFalse(TestHelper.printAudit(session), session.hasWarns());
        Object output = session.getTargetDocument("io.atlasmap.itests.core.issue.TargetClass");
        assertEquals(TargetClass.class, output.getClass());
        TargetClass target = TargetClass.class.cast(output);
        assertEquals("one-two-three", target.getTargetString());
        assertEquals(123, target.getTargetInteger());
        assertEquals("last name is empty", target.getTargetName());
        assertEquals(1, target.getTargetInteger2());
        session = context.createSession();
        source = new SourceClass()
                    .setSourceString("not empty")
                    .setSourceInteger(789)
                    .setSourceFirstName(null)
                    .setSourceLastName("lastname")
                    .setSourceInteger2(790);
        session.setSourceDocument("io.atlasmap.itests.core.issue.SourceClass", source);
        context.process(session);
        assertFalse(TestHelper.printAudit(session), session.hasErrors());
        assertFalse(TestHelper.printAudit(session), session.hasWarns());
        output = session.getTargetDocument("io.atlasmap.itests.core.issue.TargetClass");
        assertEquals(TargetClass.class, output.getClass());
        target = TargetClass.class.cast(output);
        assertEquals("not one-two-three", target.getTargetString());
        assertEquals(456, target.getTargetInteger());
        assertEquals("last name is not empty", target.getTargetName());
        assertEquals(0, target.getTargetInteger2());
    }
}
