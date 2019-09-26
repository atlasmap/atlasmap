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
import java.nio.file.Files;
import java.nio.file.Paths;
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
import io.atlasmap.java.test.SourceFlatPrimitiveClass;
import io.atlasmap.java.test.TargetFlatPrimitiveClass;
import io.atlasmap.java.test.TargetTestClass;
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
                .setSourceHiphenatedInteger("1-20-300-4000")
                .setSourceStreet("314 Littleton Rd")
                .setSourceWeight("128.965 kg");
        session.setSourceDocument("io.atlasmap.itests.core.issue.SourceClass", source);
        context.process(session);
        assertFalse(TestHelper.printAudit(session), session.hasErrors());
        assertTrue("split(STRING) => INTEGER/DOUBLE mapping should get warnings", session.hasWarns());
        assertEquals(8, session.getAudits().getAudit().stream().filter(a -> a.getStatus() == AuditStatus.WARN).count());
        Object output = session.getTargetDocument("io.atlasmap.itests.core.issue.TargetClass");
        assertEquals(TargetClass.class, output.getClass());
        TargetClass target = TargetClass.class.cast(output);
        assertEquals("Manjiro", target.getTargetFirstName());
        assertEquals("Nakahama", target.getTargetLastName());
        assertEquals("Manjiro,Nakahama", target.getTargetName());
        assertEquals("one,two,three", target.getTargetString());
        assertEquals(new Integer(314), target.getTargetStreetNumber());
        assertEquals("Littleton", target.getTargetStreetName1());
        assertEquals("Rd", target.getTargetStreetName2());
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
        assertEquals(Double.valueOf(128.965), target.getTargetWeightDouble());
        assertEquals("kg", target.getTargetWeightUnit());
    }

    @Test
    public void testConcatenateTypes() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/atlasmapping-multiplicity-transformation-concatenate-types.json");
        AtlasMapping mapping = mappingService.loadMapping(url);
        AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(mapping);
        AtlasSession session = context.createSession();
        TargetTestClass source = new TargetTestClass();
        source.setCreated(new java.util.Date());
        TargetFlatPrimitiveClass primitives = new TargetFlatPrimitiveClass();
        primitives.setBoxedStringField("boxedString");
        primitives.setCharField('c');
        primitives.setIntField(1);
        primitives.setFloatField(1.3f);
        primitives.setLongField(2L);
        primitives.setShortField((short)2);
        primitives.setDoubleField(3.1d);
        primitives.setBoxedCharField(new Character('c'));
        primitives.setBoxedIntField(new Integer(1));
        primitives.setBoxedFloatField(new Float(1.3f));
        primitives.setBoxedLongField(new Long(2L));
        primitives.setBoxedShortField(new Short((short)2));
        primitives.setBoxedDoubleField(new Double(3.1d));
        source.setPrimitives(primitives);
        session.setSourceDocument("io.atlasmap.java.test.TargetTestClass", source);
        context.process(session);
        assertFalse(TestHelper.printAudit(session), session.hasErrors());
        Object output = session.getTargetDocument("io.atlasmap.java.test.TargetTestClass");
        assertEquals(TargetTestClass.class, output.getClass());
        TargetTestClass target = TargetTestClass.class.cast(output);
        assertEquals("[" + target.getFullAddress() + "]", 14, target.getFullAddress().split(" ").length);
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
        assertEquals("true", target.getTargetFirstName());
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
        assertEquals("false", target.getTargetFirstName());
    }


    @Test
    public void testAdd() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/atlasmapping-multiplicity-transformation-add.json");
        AtlasMapping mapping = mappingService.loadMapping(url);
        AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(mapping);
        AtlasSession session = context.createSession();
        String sourceJson = new String(Files.readAllBytes(Paths.get(
            Thread.currentThread().getContextClassLoader().getResource("data/json-source.json").toURI())));
        session.setSourceDocument("json-source", sourceJson);
        String sourceXml = new String(Files.readAllBytes(Paths.get(
            Thread.currentThread().getContextClassLoader().getResource("data/xml-source.xml").toURI())));
        session.setSourceDocument("xml-source", sourceXml);
        SourceFlatPrimitiveClass sourceJava = new SourceFlatPrimitiveClass();
        sourceJava.setIntArrayField(new int[] {1, 3, 5, 7});
        session.setSourceDocument("java-source", sourceJava);
        context.process(session);
        assertFalse(TestHelper.printAudit(session), session.hasErrors());
        assertFalse(TestHelper.printAudit(session), session.hasWarns());
        Object output = session.getTargetDocument("io.atlasmap.java.test.TargetFlatPrimitiveClass");
        assertEquals(TargetFlatPrimitiveClass.class, output.getClass());
        TargetFlatPrimitiveClass target = TargetFlatPrimitiveClass.class.cast(output);
        assertEquals(new Float((1+3+5+7)).floatValue(), target.getFloatField(), 1e-15);
        assertEquals(new Double((1+3+5+7)).doubleValue(), target.getDoubleField(), 1e-15);
        assertEquals(1+3+5+7, target.getLongField());
    }

}
