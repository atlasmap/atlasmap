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
package io.atlasmap.xml.v2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.AtlasModelFactory;
import io.atlasmap.v2.Collection;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.ConstantField;
import io.atlasmap.v2.DataSource;
import io.atlasmap.v2.DataSourceType;
import io.atlasmap.v2.Mapping;
import io.atlasmap.v2.MappingType;
import io.atlasmap.v2.Mappings;
import io.atlasmap.v2.Properties;
import io.atlasmap.v2.Property;
import io.atlasmap.v2.PropertyField;

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
        AtlasMapping atlasMapping = AtlasModelFactory.createAtlasMapping();
        atlasMapping.setName("junit");

        Mapping mapping = AtlasModelFactory.createMapping(MappingType.MAP);
        XmlField inputField = new XmlField();
        inputField.setName("foo");
        inputField.setValue("bar");
        mapping.getInputField().add(inputField);

        XmlField outputField = new XmlField();
        outputField.setName("woot");
        outputField.setValue("blerg");
        outputField.setUserCreated(true);
        mapping.getOutputField().add(outputField);

        atlasMapping.getMappings().getMapping().add(mapping);
        return atlasMapping;
    }

    protected AtlasMapping generateCollectionMapping() {
        AtlasMapping innerMapping1 = generateAtlasMapping();
        AtlasMapping innerMapping2 = generateAtlasMapping();

        Collection cMapping = new Collection();
        cMapping.getMappings().getMapping().addAll(innerMapping1.getMappings().getMapping());
        cMapping.getMappings().getMapping().addAll(innerMapping2.getMappings().getMapping());
        cMapping.setCollectionType(CollectionType.LIST);

        AtlasMapping mapping = generateAtlasMapping();
        mapping.getMappings().getMapping().clear();
        mapping.getMappings().getMapping().add(cMapping);
        return mapping;
    }

    protected AtlasMapping generateCombineMapping() {

        XmlField inputFieldA = new XmlField();
        inputFieldA.setName("foo");
        inputFieldA.setValue("bar");

        XmlField inputFieldB = new XmlField();
        inputFieldB.setName("foo3");
        inputFieldB.setValue("bar3");

        XmlField outputFieldA = new XmlField();
        outputFieldA.setName("woot");
        outputFieldA.setValue("blerg");

        Mapping fm = AtlasModelFactory.createMapping(MappingType.COMBINE);
        fm.getInputField().add(inputFieldA);
        fm.getInputField().add(inputFieldB);
        fm.getOutputField().add(outputFieldA);

        AtlasMapping mapping = generateAtlasMapping();
        mapping.getMappings().getMapping().clear();
        mapping.getMappings().getMapping().add(fm);
        return mapping;
    }

    protected AtlasMapping generatePropertyReferenceMapping() {
        AtlasMapping mapping = generateAtlasMapping();

        PropertyField inputField = new PropertyField();
        inputField.setName("foo");

        Mapping fm = (Mapping) mapping.getMappings().getMapping().get(0);
        fm.getInputField().add(inputField);

        Property p = new Property();
        p.setName("foo");
        p.setValue("bar");
        mapping.setProperties(new Properties());
        mapping.getProperties().getProperty().add(p);
        return mapping;
    }

    protected AtlasMapping generateConstantMapping() {
        AtlasMapping mapping = generateAtlasMapping();

        ConstantField inputField = new ConstantField();
        inputField.setValue("foo");

        Mapping fm = (Mapping) mapping.getMappings().getMapping().get(0);
        fm.getInputField().add(inputField);

        return mapping;
    }

    protected AtlasMapping generateMultiSourceMapping() {
        AtlasMapping mapping = generateSeparateAtlasMapping();

        DataSource source1 = new DataSource();
        source1.setUri("xml:foo1");
        source1.setDataSourceType(DataSourceType.SOURCE);
        source1.setId("xml1");

        DataSource source2 = new DataSource();
        source2.setUri("xml:foo2");
        source2.setDataSourceType(DataSourceType.SOURCE);
        source2.setId("xml2");

        DataSource target = new DataSource();
        target.setUri("xml:bar");
        target.setDataSourceType(DataSourceType.TARGET);
        target.setId("target1");

        mapping.getDataSource().add(source1);
        mapping.getDataSource().add(source2);
        mapping.getDataSource().add(target);

        Mapping fm = (Mapping) mapping.getMappings().getMapping().get(0);
        fm.getInputField().get(0).setDocId("xml1");
        fm.getOutputField().get(0).setDocId("target1");
        fm.getOutputField().get(1).setDocId("target1");

        return mapping;
    }

    protected void validateAtlasMapping(AtlasMapping mapping) {
        assertNotNull(mapping);
        assertNotNull(mapping.getName());
        assertEquals("junit", mapping.getName());
        assertNotNull(mapping.getMappings());
        assertEquals(new Integer(1), new Integer(mapping.getMappings().getMapping().size()));
        assertNotNull(mapping.getProperties());

        Mapping fm = (Mapping) mapping.getMappings().getMapping().get(0);
        assertNotNull(fm);
        assertNull(fm.getAlias());

        assertTrue(fm.getInputField().get(0) instanceof XmlField);
        XmlField m1 = (XmlField) fm.getInputField().get(0);
        assertNotNull(m1);
        assertNull(m1.getActions());
        assertEquals("foo", ((XmlField) m1).getName());
        assertEquals("bar", m1.getValue());
        assertNull(((XmlField) m1).getFieldType());

        assertTrue(fm.getOutputField().get(0) instanceof XmlField);
        XmlField m2 = (XmlField) fm.getOutputField().get(0);
        assertNotNull(m2);
        assertNull(m2.getActions());
        assertEquals("woot", ((XmlField) m2).getName());
        assertEquals("blerg", m2.getValue());
        assertNull(((XmlField) m2).getFieldType());

    }

    protected AtlasMapping generateSeparateAtlasMapping() {
        AtlasMapping atlasMapping = new AtlasMapping();
        atlasMapping.setName("junit");
        atlasMapping.setMappings(new Mappings());

        Mapping mapping = AtlasModelFactory.createMapping(MappingType.SEPARATE);

        XmlField inputField = new XmlField();
        inputField.setName("foo");
        inputField.setValue("bar");

        XmlField outputFieldA = new XmlField();
        outputFieldA.setName("woot");
        outputFieldA.setValue("blerg");
        outputFieldA.setIndex(1);

        XmlField outputFieldB = new XmlField();
        outputFieldB.setName("meow");
        outputFieldB.setValue("ruff");
        outputFieldB.setIndex(2);

        mapping.getInputField().add(inputField);
        mapping.getOutputField().add(outputFieldA);
        mapping.getOutputField().add(outputFieldB);

        atlasMapping.getMappings().getMapping().add(mapping);
        return atlasMapping;
    }

    protected void validateSeparateAtlasMapping(AtlasMapping mapping) {
        assertNotNull(mapping);
        assertNotNull(mapping.getName());
        assertEquals("junit", mapping.getName());
        assertNotNull(mapping.getMappings());
        assertEquals(new Integer(1), new Integer(mapping.getMappings().getMapping().size()));
        assertNull(mapping.getProperties());

        Mapping fm = (Mapping) mapping.getMappings().getMapping().get(0);
        assertNotNull(fm);
        assertEquals(MappingType.SEPARATE, fm.getMappingType());
        assertNull(fm.getAlias());

        XmlField m1 = (XmlField) fm.getInputField().get(0);
        assertNotNull(m1);
        assertNull(m1.getActions());
        assertEquals("foo", ((XmlField) m1).getName());
        assertEquals("bar", m1.getValue());
        assertNull(((XmlField) m1).getFieldType());

        XmlField m2 = (XmlField) fm.getOutputField().get(0);
        assertNotNull(m2);
        assertNull(m2.getActions());
        assertEquals("woot", ((XmlField) m2).getName());
        assertEquals("blerg", m2.getValue());
        assertNull(((XmlField) m2).getFieldType());
        assertEquals(new Integer(1), m2.getIndex());

        XmlField m3 = (XmlField) fm.getOutputField().get(0);
        assertNotNull(m3);
        assertNull(m3.getActions());
        assertEquals("meow", ((XmlField) m3).getName());
        assertEquals("ruff", m3.getValue());
        assertNull(((XmlField) m3).getFieldType());
        assertEquals(new Integer(2), m3.getIndex());

    }

    public XmlInspectionRequest generateInspectionRequest() {
        XmlInspectionRequest xmlInspectionRequest = new XmlInspectionRequest();
        xmlInspectionRequest.setType(InspectionType.INSTANCE);

        final String xmlData = "<data>\n" + "     <intField a='1'>32000</intField>\n"
                + "     <longField>12421</longField>\n" + "     <stringField>abc</stringField>\n"
                + "     <booleanField>true</booleanField>\n" + "     <doubleField b='2'>12.0</doubleField>\n"
                + "     <shortField>1000</shortField>\n" + "     <floatField>234.5f</floatField>\n"
                + "     <charField>A</charField>\n" + "</data>";
        xmlInspectionRequest.setXmlData(xmlData);
        return xmlInspectionRequest;
    }
}
