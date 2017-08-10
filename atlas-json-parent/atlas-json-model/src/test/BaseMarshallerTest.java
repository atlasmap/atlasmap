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
package io.atlasmap.json.v2;

import io.atlasmap.v2.*;
import io.atlasmap.json.v2.InspectionType;
import io.atlasmap.json.v2.JsonField;
import io.atlasmap.json.v2.JsonInspectionRequest;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import static org.junit.Assert.*;

public abstract class BaseMarshallerTest {

    public boolean deleteTestFolders = true;

    @Rule
    public TestName testName = new TestName();

    @Before
    public void setUp() throws Exception {
        Files.createDirectories(Paths.get("target/junit/" + testName.getMethodName()));
    }

    @After
    public void tearDown() throws Exception {
        if (deleteTestFolders) {
            Path directory = Paths.get("target/junit/" + testName.getMethodName());
            Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
                @Override
                public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
                    Files.delete(file);
                    return FileVisitResult.CONTINUE;
                }

                @Override
                public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                    if (exc == null) {
                        Files.delete(dir);
                        return FileVisitResult.CONTINUE;
                    } else {
                        throw exc;
                    }
                }
            });
        }
    }

    protected AtlasMapping generateAtlasMapping() {
        AtlasMapping mapping = AtlasModelFactory.createAtlasMapping();
        mapping.setName("junit");

        JsonField inputJsonField = new JsonField();
        inputJsonField.setName("foo");
        inputJsonField.setValue("bar");

        JsonField outputJsonField = new JsonField();
        outputJsonField.setName("woot");
        outputJsonField.setValue("blerg");

        Mapping fm = AtlasModelFactory.createMapping(MappingType.MAP);
        fm.getInputField().add(inputJsonField);
        fm.getOutputField().add(outputJsonField);

        mapping.getMappings().getMapping().add(fm);
        return mapping;
    }

    protected AtlasMapping generateCollectionMapping() {
        AtlasMapping innerMapping1 = generateAtlasMapping();
        AtlasMapping innerMapping2 = generateAtlasMapping();

        Collection cMapping = AtlasModelFactory.createMapping(MappingType.COLLECTION);
        cMapping.getMappings().getMapping().addAll(innerMapping1.getMappings().getMapping());
        cMapping.getMappings().getMapping().addAll(innerMapping2.getMappings().getMapping());
        cMapping.setCollectionType(CollectionType.LIST);

        AtlasMapping mapping = generateAtlasMapping();
        mapping.getMappings().getMapping().clear();
        mapping.getMappings().getMapping().add(cMapping);
        return mapping;
    }

    protected AtlasMapping generateCombineMapping() {

        JsonField inputJsonField = new JsonField();
        inputJsonField.setName("foo");
        inputJsonField.setValue("bar");
        inputJsonField.setIndex(0);

        JsonField inputJsonFieldB = new JsonField();
        inputJsonFieldB.setName("foo3");
        inputJsonFieldB.setValue("bar3");
        inputJsonFieldB.setIndex(1);

        JsonField outputJsonFieldA = new JsonField();
        outputJsonFieldA.setName("woot");
        outputJsonFieldA.setValue("blerg");

        Mapping fm = AtlasModelFactory.createMapping(MappingType.COMBINE);
        fm.getInputField().add(inputJsonField);
        fm.getInputField().add(inputJsonFieldB);
        fm.getOutputField().add(outputJsonFieldA);

        AtlasMapping mapping = generateAtlasMapping();
        mapping.getMappings().getMapping().clear();
        mapping.getMappings().getMapping().add(fm);
        return mapping;
    }

    protected AtlasMapping generatePropertyReferenceMapping() {
        AtlasMapping mapping = generateAtlasMapping();

        MappedField inputField = new MappedField();
        PropertyField inputPropertyField = new PropertyField();
        inputPropertyField.setName("foo");
        inputField.setField(inputPropertyField);

        MapFieldMapping fm = (MapFieldMapping) mapping.getFieldMappings().getFieldMapping().get(0);
        fm.setInputField(inputField);

        Property p = new Property();
        p.setName("foo");
        p.setValue("bar");
        mapping.setProperties(new Properties());
        mapping.getProperties().getProperty().add(p);
        return mapping;
    }

    protected AtlasMapping generateConstantMapping() {
        AtlasMapping mapping = generateAtlasMapping();

        MappedField inputField = new MappedField();
        ConstantField inputPropertyField = new ConstantField();
        inputPropertyField.setValue("foo");
        inputField.setField(inputPropertyField);

        MapFieldMapping fm = (MapFieldMapping) mapping.getFieldMappings().getFieldMapping().get(0);
        fm.setInputField(inputField);

        return mapping;
    }

    protected AtlasMapping generateMultiSourceMapping() {
        AtlasMapping mapping = generateSeparateAtlasMapping();
        mapping.setSourceUri(null);
        mapping.setTargetUri(null);

        mapping.setSources(new DataSources());
        mapping.setTargets(new DataSources());

        DataSource sourceA = new DataSource();
        sourceA.setUri("someSourceURI:A");
        sourceA.setDocId("sourceA");
        mapping.getSources().getDataSource().add(sourceA);

        DataSource targetA = new DataSource();
        targetA.setUri("someTargetURI:A");
        targetA.setDocId("targetA");
        mapping.getTargets().getDataSource().add(targetA);

        DataSource targetB = new DataSource();
        targetB.setUri("someTargetURI:B");
        targetB.setDocId("targetB");
        mapping.getTargets().getDataSource().add(targetB);

        SeparateFieldMapping fm = (SeparateFieldMapping) mapping.getFieldMappings().getFieldMapping().get(0);
        fm.getInputField().getField().setDocId("sourceA");
        fm.getOutputFields().getMappedField().get(0).getField().setDocId("targetA");
        fm.getOutputFields().getMappedField().get(1).getField().setDocId("targetB");

        return mapping;
    }

    protected void validateAtlasMapping(AtlasMapping mapping) {
        assertNotNull(mapping);
        assertNotNull(mapping.getName());
        assertEquals("junit", mapping.getName());
        assertNotNull(mapping.getFieldMappings());
        assertEquals(new Integer(1), new Integer(mapping.getFieldMappings().getFieldMapping().size()));
        assertNull(mapping.getProperties());

        MapFieldMapping fm = (MapFieldMapping) mapping.getFieldMappings().getFieldMapping().get(0);
        assertNotNull(fm);
        assertNull(fm.getAlias());

        MappedField m1 = fm.getInputField();
        assertNotNull(m1);
        assertNull(m1.getFieldActions());
        // assertTrue(m1.getFieldActions().isEmpty());
        assertNotNull(m1.getField());
        Field f1 = m1.getField();
        assertTrue(f1 instanceof JsonField);
        assertEquals("foo", ((JsonField) f1).getName());
        assertEquals("bar", f1.getValue());
        assertNull(((JsonField) f1).getType());

        MappedField m2 = fm.getOutputField();
        assertNotNull(m2);
        assertNull(m2.getFieldActions());
        // assertTrue(m2.getFieldActions().isEmpty());
        assertNotNull(m2.getField());
        Field f2 = m2.getField();
        assertTrue(f2 instanceof JsonField);
        assertEquals("woot", ((JsonField) f2).getName());
        assertEquals("blerg", f2.getValue());
        assertNull(((JsonField) f2).getType());

    }

    protected AtlasMapping generateSeparateAtlasMapping() {
        AtlasMapping mapping = new AtlasMapping();
        mapping.setName("junit");
        mapping.setFieldMappings(new FieldMappings());

        MappedField inputField = new MappedField();
        JsonField inputJsonField = new JsonField();
        inputJsonField.setName("foo");
        inputJsonField.setValue("bar");
        inputField.setField(inputJsonField);

        MappedField outputFieldA = new MappedField();
        JsonField outputJsonFieldA = new JsonField();
        outputJsonFieldA.setName("woot");
        outputJsonFieldA.setValue("blerg");
        outputFieldA.setField(outputJsonFieldA);

        MapAction outputActionA = new MapAction();
        outputActionA.setIndex(new Integer(1));
        outputFieldA.setFieldActions(new FieldActions());
        // outputFieldA.getFieldActions().getFieldAction().add(outputActionA);

        MappedField outputFieldB = new MappedField();
        JsonField outputJsonFieldB = new JsonField();
        outputJsonFieldB.setName("meow");
        outputJsonFieldB.setValue("ruff");
        outputFieldB.setField(outputJsonFieldB);

        MapAction outputActionB = new MapAction();
        outputActionB.setIndex(new Integer(2));
        outputFieldB.setFieldActions(new FieldActions());
        // outputFieldB.getFieldActions().getFieldAction().add(outputActionB);

        SeparateFieldMapping fm = AtlasModelFactory.createFieldMapping(SeparateFieldMapping.class);
        fm.setInputField(inputField);
        fm.getOutputFields().getMappedField().add(outputFieldA);
        fm.getOutputFields().getMappedField().add(outputFieldB);

        mapping.getFieldMappings().getFieldMapping().add(fm);
        return mapping;
    }

    protected void validateSeparateAtlasMapping(AtlasMapping mapping) {
        assertNotNull(mapping);
        assertNotNull(mapping.getName());
        assertEquals("junit", mapping.getName());
        assertNotNull(mapping.getFieldMappings());
        assertEquals(new Integer(1), new Integer(mapping.getFieldMappings().getFieldMapping().size()));
        assertNull(mapping.getProperties());

        FieldMapping fm = mapping.getFieldMappings().getFieldMapping().get(0);
        assertNotNull(fm);
        assertTrue(fm instanceof SeparateFieldMapping);
        assertNull(fm.getAlias());

        SeparateFieldMapping sfm = (SeparateFieldMapping) fm;
        MappedField m1 = sfm.getInputField();
        assertNotNull(m1);
        assertNull(m1.getFieldActions());
        // assertEquals(new Integer(0), new
        // Integer(m1.getFieldActions().getFieldAction().size()));
        assertNotNull(m1.getField());
        Field f1 = m1.getField();
        assertTrue(f1 instanceof JsonField);
        assertEquals("foo", ((JsonField) f1).getName());
        assertEquals("bar", f1.getValue());
        assertNull(((JsonField) f1).getType());

        MappedFields mFields = sfm.getOutputFields();
        MappedField m2 = mFields.getMappedField().get(0);
        assertNotNull(m2);
        assertNotNull(m2.getFieldActions());
        // assertEquals(new Integer(1), new
        // Integer(m2.getFieldActions().getFieldAction().size()));
        assertNotNull(m2.getField());
        Field f2 = m2.getField();
        assertTrue(f2 instanceof JsonField);
        assertEquals("woot", ((JsonField) f2).getName());
        assertEquals("blerg", f2.getValue());
        assertNull(((JsonField) f2).getType());

        MappedField m3 = mFields.getMappedField().get(1);
        assertNotNull(m3);
        assertNotNull(m3.getFieldActions());
        // assertEquals(new Integer(1), new
        // Integer(m3.getFieldActions().getFieldAction().size()));
        assertNotNull(m3.getField());
        Field f3 = m3.getField();
        assertTrue(f3 instanceof JsonField);
        assertEquals("meow", ((JsonField) f3).getName());
        assertEquals("ruff", f3.getValue());
        assertNull(((JsonField) f3).getType());

    }

    public JsonInspectionRequest generateInspectionRequest() {
        JsonInspectionRequest jsonInspectionRequest = new JsonInspectionRequest();
        jsonInspectionRequest.setType(InspectionType.INSTANCE);

        final String jsonData = "<data>\n" + "     <intField a='1'>32000</intField>\n"
                + "     <longField>12421</longField>\n" + "     <stringField>abc</stringField>\n"
                + "     <booleanField>true</booleanField>\n" + "     <doubleField b='2'>12.0</doubleField>\n"
                + "     <shortField>1000</shortField>\n" + "     <floatField>234.5f</floatField>\n"
                + "     <charField>A</charField>\n" + "</data>";
        jsonInspectionRequest.setJsonData(jsonData);
        return jsonInspectionRequest;
    }
}
