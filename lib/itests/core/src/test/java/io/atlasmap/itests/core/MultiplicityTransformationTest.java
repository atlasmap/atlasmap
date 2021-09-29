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

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.DefaultAtlasContextFactory;
import io.atlasmap.itests.core.issue.Item;
import io.atlasmap.itests.core.issue.SourceClass;
import io.atlasmap.itests.core.issue.TargetClass;
import io.atlasmap.java.test.SourceFlatPrimitiveClass;
import io.atlasmap.java.test.TargetFlatPrimitiveClass;
import io.atlasmap.java.test.TargetTestClass;
import io.atlasmap.v2.AuditStatus;

public class MultiplicityTransformationTest {

    @Test
    public void testConcatenateSplit() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/atlasmapping-multiplicity-transformation-concatenate-split.json");
        AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(url.toURI());
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
        String sourceJson = new String(Files.readAllBytes(Paths.get(
                Thread.currentThread().getContextClassLoader().getResource("data/json-source-collection.json").toURI())));
        session.setSourceDocument("SourceJson", sourceJson);
        context.process(session);
        assertFalse(session.hasErrors(), TestHelper.printAudit(session));
        assertTrue(session.hasWarns(), "split(STRING) => INTEGER/DOUBLE mapping should get warnings");
        assertEquals(11, session.getAudits().getAudit().stream().filter(a -> a.getStatus() == AuditStatus.WARN).count());
        Object output = session.getTargetDocument("io.atlasmap.itests.core.issue.TargetClass");
        assertEquals(TargetClass.class, output.getClass());
        TargetClass target = TargetClass.class.cast(output);
        assertEquals("Manjiro", target.getTargetFirstName());
        assertEquals("Nakahama", target.getTargetLastName());
        assertEquals("Manjiro,Nakahama", target.getTargetName());

        // Concatenate(',', Capitalize(SourceStringList<>), SourceName) -> targetFullName
        assertEquals("One,Two,Three,Nakahama", target.getTargetFullName());

        assertEquals("one,two,three", target.getTargetString());
        assertEquals(Integer.valueOf(314), target.getTargetStreetNumber());
        assertEquals("Littleton", target.getTargetStreetName1());
        assertEquals("Rd", target.getTargetStreetName2());
        List<String> list = target.getTargetStringList();
        assertEquals(3, list.size());
        assertEquals("one", list.get(0));
        assertEquals("two", list.get(1));
        assertEquals("three", list.get(2));
        List<Integer> intList = target.getTargetIntegerList();
        assertEquals(4, intList.size());
        assertEquals(Integer.valueOf(1), intList.get(0));
        assertEquals(Integer.valueOf(20), intList.get(1));
        assertEquals(Integer.valueOf(300), intList.get(2));
        assertEquals(Integer.valueOf(4000), intList.get(3));
        assertEquals(Double.valueOf(128.965), target.getTargetWeightDouble());
        assertEquals("kg", target.getTargetWeightUnit());

        Object obj = session.getTargetDocument("TargetJson");
        JsonNode targetJson = new ObjectMapper().readTree((String) obj);
        JsonNode targetJsonString = targetJson.get("targetJsonString");
        assertEquals("2 2 3", targetJsonString.asText());

    }

    @Test
    public void testConcatenateTypes() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/atlasmapping-multiplicity-transformation-concatenate-types.json");
        AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(url.toURI());
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
        primitives.setBoxedCharField(Character.valueOf('c'));
        primitives.setBoxedIntField(Integer.valueOf(1));
        primitives.setBoxedFloatField(Float.valueOf(1.3f));
        primitives.setBoxedLongField(Long.valueOf(2L));
        primitives.setBoxedShortField(Short.valueOf((short)2));
        primitives.setBoxedDoubleField(Double.valueOf(3.1d));
        source.setPrimitives(primitives);
        session.setSourceDocument("io.atlasmap.java.test.TargetTestClass", source);
        context.process(session);
        assertFalse(session.hasErrors(), TestHelper.printAudit(session));
        Object output = session.getTargetDocument("io.atlasmap.java.test.TargetTestClass");
        assertEquals(TargetTestClass.class, output.getClass());
        TargetTestClass target = TargetTestClass.class.cast(output);
        assertEquals(14, target.getFullAddress().split(" ").length, "[" + target.getFullAddress() + "]");
    }

    @Test
    public void testItemAt() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/atlasmapping-multiplicity-transformation-itemAt.json");
        AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(url.toURI());
        AtlasSession session = context.createSession();
        SourceClass source = new SourceClass().setSourceStringList(Arrays.asList(new String[] {"one", "two", "three"}));
        session.setSourceDocument("io.atlasmap.itests.core.issue.SourceClass", source);
        context.process(session);
        assertFalse(session.hasErrors(), TestHelper.printAudit(session));
        assertFalse(session.hasWarns(), TestHelper.printAudit(session));
        Object output = session.getTargetDocument("io.atlasmap.itests.core.issue.TargetClass");
        assertEquals(TargetClass.class, output.getClass());
        TargetClass target = TargetClass.class.cast(output);
        assertEquals("two", target.getTargetString());
    }

    @Test
    public void testExpression() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/atlasmapping-multiplicity-transformation-expression.json");
        AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(url.toURI());
        AtlasSession session = context.createSession();
        SourceClass source = new SourceClass()
                                .setSourceString("")
                                .setSourceInteger(123)
                                .setSourceFirstName(null)
                                .setSourceLastName("")
                                .setSourceInteger2(-123);
        session.setSourceDocument("io.atlasmap.itests.core.issue.SourceClass", source);
        context.process(session);
        assertFalse(session.hasErrors(), TestHelper.printAudit(session));
        assertFalse(session.hasWarns(), TestHelper.printAudit(session));
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
        assertFalse(session.hasErrors(), TestHelper.printAudit(session));
        assertFalse(session.hasWarns(), TestHelper.printAudit(session));
        output = session.getTargetDocument("io.atlasmap.itests.core.issue.TargetClass");
        assertEquals(TargetClass.class, output.getClass());
        target = TargetClass.class.cast(output);
        assertEquals("not one-two-three", target.getTargetString());
        assertEquals(456, target.getTargetInteger());
        assertEquals("last name is not empty", target.getTargetName());
        assertEquals("false", target.getTargetFirstName());
    }

    @Test
    public void testCapitalizeExpressionWithCollection() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/atlasmapping-multiplicity-transformation-capitalize-expression.json");
        AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(url.toURI());
        AtlasSession session = context.createSession();
        SourceClass source = new SourceClass()
            .setSourceStringList(Arrays.asList("bob", "john", "andrea"));
        session.setSourceDocument("io.atlasmap.itests.core.issue.SourceClass", source);
        context.process(session);
        assertFalse(session.hasErrors(), TestHelper.printAudit(session));
        assertFalse(session.hasWarns(), TestHelper.printAudit(session));
        Object output = session.getTargetDocument("io.atlasmap.itests.core.issue.TargetClass");
        assertEquals(TargetClass.class, output.getClass());
        TargetClass target = TargetClass.class.cast(output);
        assertEquals(Arrays.asList("Bob", "John", "Andrea"), target.getTargetStringList());
    }

    @Test
    public void testConcatenateExpressionWithCollection() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/atlasmapping-multiplicity-transformation-concatenate-expression.json");
        AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(url.toURI());
        AtlasSession session = context.createSession();
        SourceClass source = new SourceClass()
            .setSourceStringList(Arrays.asList("bob", "john", "andrea"));
        source.setSourceString(",");
        session.setSourceDocument("io.atlasmap.itests.core.issue.SourceClass", source);
        context.process(session);
        assertFalse(session.hasErrors(), TestHelper.printAudit(session));
        assertFalse(session.hasWarns(), TestHelper.printAudit(session));
        Object output = session.getTargetDocument("io.atlasmap.itests.core.issue.TargetClass");
        assertEquals(TargetClass.class, output.getClass());
        TargetClass target = TargetClass.class.cast(output);
        assertEquals("Bob,John,Andrea", target.getTargetString());
    }

    @Test
    public void testConcatenateExpressionWithTwoCollections() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/atlasmapping-multiplicity-transformation-concatenate-expression.json");
        AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(url.toURI());
        AtlasSession session = context.createSession();
        SourceClass source = new SourceClass()
            .setSourceStringList(Arrays.asList("bob", "john", "andrea"));
        source.setSourceList(Arrays.asList(new Item("thomas", null), new Item("arnold", null)));
        source.setSourceString(",");
        session.setSourceDocument("io.atlasmap.itests.core.issue.SourceClass", source);
        context.process(session);
        assertFalse(session.hasErrors(), TestHelper.printAudit(session));
        assertFalse(session.hasWarns(), TestHelper.printAudit(session));
        Object output = session.getTargetDocument("io.atlasmap.itests.core.issue.TargetClass");
        assertEquals(TargetClass.class, output.getClass());
        TargetClass target = TargetClass.class.cast(output);
        assertEquals("bob,john,andrea,thomas,arnold", target.getTargetFirstName());
    }

    @Test
    public void testConcatenateExpressionWithCsv() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/atlasmapping-multiplicity-transformation-concatenate-csv.json");
        AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(url.toURI());
        AtlasSession session = context.createSession();
        session.setSourceDocument("io.atlasmap.itests.core.SourceCsv", "givenName,familyName\n" +
            "Bob,Smith\nAnthony,Hopkins\nTimothy,Anders");
        context.process(session);
        assertFalse(session.hasErrors(), TestHelper.printAudit(session));
        assertFalse(session.hasWarns(), TestHelper.printAudit(session));
        Object output = session.getTargetDocument("io.atlasmap.itests.core.TargetCsv");
        assertEquals("allGivenNames,name\r\n" +
            "\"Bob,Anthony,Timothy\",\"Bob,Anthony,Timothy,Smith,Hopkins,Anders\"\r\n", output);
    }


    @Test
    public void testAdd() throws Exception {
        URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/atlasmapping-multiplicity-transformation-add.json");
        AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(url.toURI());
        AtlasSession session = context.createSession();
        String sourceJson = new String(Files.readAllBytes(Paths.get(
            Thread.currentThread().getContextClassLoader().getResource("data/json-source.json").toURI())));
        session.setSourceDocument("json-source", sourceJson);
        String sourceXml = new String(Files.readAllBytes(Paths.get(
            Thread.currentThread().getContextClassLoader().getResource("data/xml-source.xml").toURI())));
        session.setSourceDocument("xml-source", sourceXml);
        SourceFlatPrimitiveClass sourceJava = new SourceFlatPrimitiveClass();
        sourceJava.setIntArrayField(new int[] {1, 3, 5, 7});
        sourceJava.setBoxedIntListField(Arrays.asList(new Integer[] {2, 4, 6, 8}));
        session.setSourceDocument("java-source", sourceJava);
        context.process(session);
        assertFalse(session.hasErrors(), TestHelper.printAudit(session));
        Object output = session.getTargetDocument("io.atlasmap.java.test.TargetFlatPrimitiveClass");
        assertEquals(TargetFlatPrimitiveClass.class, output.getClass());
        TargetFlatPrimitiveClass target = TargetFlatPrimitiveClass.class.cast(output);
        assertEquals(Float.valueOf((1+3+5+7)).floatValue(), target.getFloatField(), 1e-15);
        assertEquals(Double.valueOf((1+3+5+7)).doubleValue(), target.getDoubleField(), 1e-15);
        assertEquals(1+3+5+7, target.getLongField());
        assertEquals(2+4+6+8, target.getIntField());
    }

    @Test
    public void testActionRepeat_1() throws Exception {
    	 URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/atlasmapping-multiplicity-transformation-action_repeat.json");
         AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(url.toURI());
         AtlasSession session = context.createSession();
         String sourceJson = new String(Files.readAllBytes(Paths.get(
             Thread.currentThread().getContextClassLoader().getResource("data/json-source-repeat.json").toURI())));
         session.setSourceDocument("json-source-repeat", sourceJson);

         context.process(session);
         assertFalse(session.hasErrors(), TestHelper.printAudit(session));
         Object output = session.getTargetDocument("json-target");
         assertEquals("[{\"targetField\":\"simpleFieldValue\"}]", output);
    }

    @Test
    public void testActionRepeatCount3() throws Exception {
    	 URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/atlasmapping-multiplicity-transformation-action_repeat.json");
         AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(url.toURI());
         AtlasSession session = context.createSession();
         String sourceJson = new String(Files.readAllBytes(Paths.get(
             Thread.currentThread().getContextClassLoader().getResource("data/json-source-repeat_count_3.json").toURI())));
         session.setSourceDocument("json-source-repeat", sourceJson);

         context.process(session);
         assertFalse(session.hasErrors(), TestHelper.printAudit(session));
         Object output = session.getTargetDocument("json-target");
         assertEquals("[{\"targetField\":\"simpleFieldValue\"},{\"targetField\":\"simpleFieldValue\"},{\"targetField\":\"simpleFieldValue\"}]", output);
    }

    @Test
    public void testActionRepeatForNoSourceField() throws Exception {
    	 URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/atlasmapping-multiplicity-transformation-action_repeat.json");
         AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(url.toURI());
         AtlasSession session = context.createSession();
         String sourceJson = new String(Files.readAllBytes(Paths.get(
             Thread.currentThread().getContextClassLoader().getResource("data/json-source-repeat_no_field.json").toURI())));
         session.setSourceDocument("json-source-repeat", sourceJson);

         context.process(session);
         assertFalse(session.hasErrors(), TestHelper.printAudit(session));
         Object output = session.getTargetDocument("json-target");
         assertEquals("[]", output);
    }

    @Test
    public void testActionRepeatForNestedCollectionField() throws Exception {
    	 URL url = Thread.currentThread().getContextClassLoader().getResource("mappings/atlasmapping-multiplicity-transformation-action_repeat_nested_collection.json");
         AtlasContext context = DefaultAtlasContextFactory.getInstance().createContext(url.toURI());
         AtlasSession session = context.createSession();
         String sourceJson = new String(Files.readAllBytes(Paths.get(
             Thread.currentThread().getContextClassLoader().getResource("data/json-source-repeat_for_nested_collection_field.json").toURI())));
         session.setSourceDocument("json-source-repeat", sourceJson);

         context.process(session);
         assertFalse(session.hasErrors(), TestHelper.printAudit(session));
         Object output = session.getTargetDocument("json-target");
         assertEquals("[{\"targetField\":\"simpleFieldValue\"},{\"targetField\":\"simpleFieldValue\"},{\"targetField\":\"simpleFieldValue\"}]", output);
    }

}
