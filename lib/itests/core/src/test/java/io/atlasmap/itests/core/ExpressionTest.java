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
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.AtlasMappingService;
import io.atlasmap.core.DefaultAtlasContextFactory;
import io.atlasmap.itests.core.BaseClass.SomeNestedClass;
import io.atlasmap.v2.AtlasMapping;

public class ExpressionTest {

    private AtlasMappingService mappingService;

    @Before
    public void before() {
        mappingService = DefaultAtlasContextFactory.getInstance().getMappingService();
    }

    @Test
    public void testFilterSelectJava() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/atlasmapping-expression-filter-select-java.json");
        AtlasMapping mapping = mappingService.loadMapping(url);
        AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(mapping);
        AtlasSession session = context.createSession();
        List<SomeNestedClass>  sourceList = new ArrayList<>();
        for (int i=0; i<5; i++) {
            SomeNestedClass s = new SomeNestedClass();
            s.setSomeField("v" + i);
            sourceList.add(s);
        }
        SourceClass source = new SourceClass();
        source.setSomeArray(sourceList.toArray(new SomeNestedClass[0]));
        session.setSourceDocument("SourceClass", source);

        context.process(session);
        assertFalse(TestHelper.printAudit(session), session.hasErrors());
        Object output = session.getTargetDocument("TargetClass");
        assertEquals(TargetClass.class, output.getClass());
        TargetClass target = TargetClass.class.cast(output);
        SomeNestedClass[] array = target.getSomeArray();
        assertEquals(4, array.length);
        assertEquals("v0", array[0].getSomeField());
        assertEquals("v2", array[1].getSomeField());
    }

    @Test
    public void testFilterSelectJson() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/atlasmapping-expression-filter-select-json.json");
        AtlasMapping mapping = mappingService.loadMapping(url);
        AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(mapping);
        AtlasSession session = context.createSession();
        String sourceJson = new String(Files.readAllBytes(Paths.get(
            Thread.currentThread().getContextClassLoader().getResource("data/json-source-expression-filter-select.json").toURI())));
        session.setSourceDocument("json-source", sourceJson);

        context.process(session);
        assertFalse(TestHelper.printAudit(session), session.hasErrors());
        Object output = session.getTargetDocument("TargetClass");
        assertEquals(TargetClass.class, output.getClass());
        TargetClass target = TargetClass.class.cast(output);
        SomeNestedClass[] array = target.getSomeArray();
        assertEquals(4, array.length);
        assertEquals("v0", array[0].getSomeField());
        assertEquals("v2", array[1].getSomeField());
    }

    @Test
    public void testFilterSelectXml() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/atlasmapping-expression-filter-select-xml.json");
        AtlasMapping mapping = mappingService.loadMapping(url);
        AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(mapping);
        AtlasSession session = context.createSession();
        String sourceXml = new String(Files.readAllBytes(Paths.get(
            Thread.currentThread().getContextClassLoader().getResource("data/xml-source-expression-filter-select.xml").toURI())));
        session.setSourceDocument("xml-source", sourceXml);

        context.process(session);
        assertFalse(TestHelper.printAudit(session), session.hasErrors());
        Object output = session.getTargetDocument("TargetClass");
        assertEquals(TargetClass.class, output.getClass());
        TargetClass target = TargetClass.class.cast(output);
        SomeNestedClass[] array = target.getSomeArray();
        assertEquals(4, array.length);
        assertEquals("v0", array[0].getSomeField());
        assertEquals("v2", array[1].getSomeField());
    }

    @Test
    public void testAction() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/atlasmapping-expression2-action.json");
        AtlasMapping mapping = mappingService.loadMapping(url);
        AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(mapping);
        AtlasSession session = context.createSession();
        SourceClass source = new SourceClass();
        source.setSomeIntArray(new int[]{1, 2, 3, 4, 5});
        source.setSomeStringArray(new String[] {"one", "two", "three", "four"});
        session.setSourceDocument("SourceClass", source);

        context.process(session);
        assertFalse(TestHelper.printAudit(session), session.hasErrors());
        Object output = session.getTargetDocument("TargetClass");
        assertEquals(TargetClass.class, output.getClass());
        TargetClass target = TargetClass.class.cast(output);
        assertEquals((double)3.0, target.getSomeDouble(), 0.01);
        assertArrayEquals(new String[] {"ONE", "TWO", "THREE", "FOUR"}, target.getSomeStringArray());
    }

    @Test
    public void testCompare() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/atlasmapping-expression2-compare.json");
        AtlasMapping mapping = mappingService.loadMapping(url);
        AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(mapping);
        AtlasSession session = context.createSession();
        SourceClass source = new SourceClass();
        source.setSomeBigDecimal(new BigDecimal("1"));
        source.setSomeBigInteger(new BigInteger("2"));
        source.setSomeLong(3L);
        source.setSomeDouble(4D);
        session.setSourceDocument("SourceClass", source);

        context.process(session);
        assertFalse(TestHelper.printAudit(session), session.hasErrors());
        Object output = session.getTargetDocument("TargetClass");
        assertEquals(TargetClass.class, output.getClass());
        TargetClass target = TargetClass.class.cast(output);
        assertEquals("3", target.getSomeField());
        assertEquals("3", target.getSomeString());
        assertEquals(10, target.getSomeInt());
    }

    @Test
    public void testPlus() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/atlasmapping-expression2-oper.json");
        AtlasMapping mapping = mappingService.loadMapping(url);
        AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(mapping);
        AtlasSession session = context.createSession();
        SourceClass source = new SourceClass();
        source.setSomeField("first");
        source.setSomeString("second");
        session.setSourceDocument("SourceClass", source);

        context.process(session);
        assertFalse(TestHelper.printAudit(session), session.hasErrors());
        Object output = session.getTargetDocument("TargetClass");
        assertEquals(TargetClass.class, output.getClass());
        TargetClass target = TargetClass.class.cast(output);
        assertEquals("firstsecond", target.getSomeField());
    }
}
