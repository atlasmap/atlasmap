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

}
