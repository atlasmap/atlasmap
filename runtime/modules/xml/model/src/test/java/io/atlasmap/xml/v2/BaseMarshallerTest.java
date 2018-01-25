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

import io.atlasmap.v2.Actions;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.AtlasModelFactory;
import io.atlasmap.v2.Camelize;
import io.atlasmap.v2.Capitalize;
import io.atlasmap.v2.Collection;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.ConstantField;
import io.atlasmap.v2.DataSource;
import io.atlasmap.v2.DataSourceType;
import io.atlasmap.v2.FieldStatus;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.Length;
import io.atlasmap.v2.LookupEntry;
import io.atlasmap.v2.LookupTable;
import io.atlasmap.v2.Lowercase;
import io.atlasmap.v2.Mapping;
import io.atlasmap.v2.MappingType;
import io.atlasmap.v2.Mappings;
import io.atlasmap.v2.Properties;
import io.atlasmap.v2.Property;
import io.atlasmap.v2.PropertyField;
import io.atlasmap.v2.SeparateByDash;
import io.atlasmap.v2.SeparateByUnderscore;
import io.atlasmap.v2.StringList;
import io.atlasmap.v2.Trim;
import io.atlasmap.v2.TrimLeft;
import io.atlasmap.v2.TrimRight;
import io.atlasmap.v2.Uppercase;

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

        generateXmlDataSource(atlasMapping);

        generateLookupTables(atlasMapping);

        Actions actions = generateActions();

        StringList stringList = new StringList();
        stringList.getString().add("XmlAccessorType");
        stringList.getString().add("XmlType");

        Restrictions restrictions = new Restrictions();
        Restriction restriction = new Restriction();
        restriction.setType(RestrictionType.LENGTH);
        restriction.setValue("100");
        restrictions.getRestriction().add(restriction);

        Mapping mapping = AtlasModelFactory.createMapping(MappingType.MAP);

        XmlField inputField = generateXmlField(actions, stringList, restrictions);

        mapping.getInputField().add(inputField);

        XmlField outputField = generateXmlField(actions, stringList, restrictions);

        mapping.getOutputField().add(outputField);

        mapping.setMappingType(MappingType.MAP);
        mapping.setDelimiterString(",");
        mapping.setAlias("MapPropertyFieldAlias");
        mapping.setDelimiter(",");
        mapping.setDescription("description");
        mapping.setId("id");
        mapping.setLookupTableName("lookupTableName");
        mapping.setStrategy("strategy");
        mapping.setStrategyClassName("strategyClassName");

        atlasMapping.getMappings().getMapping().add(mapping);

        generateProperties(atlasMapping);

        return atlasMapping;
    }

    private void generateProperties(AtlasMapping atlasMapping) {
        Property p = new Property();
        p.setName("foo");
        p.setValue("bar");
        p.setFieldType(FieldType.INTEGER);
        atlasMapping.setProperties(new Properties());
        atlasMapping.getProperties().getProperty().add(p);
    }

    private XmlField generateXmlField(Actions actions, StringList stringList, Restrictions restrictions) {
        XmlField inputField = new XmlField();
        inputField.setName("foo");
        inputField.setValue("bar");
        inputField.setActions(actions);
        inputField.setArrayDimensions(3);
        inputField.setArraySize(3);
        inputField.setCollectionType(CollectionType.ARRAY);
        inputField.setDocId("docid");
        inputField.setPath("/path");
        inputField.setRequired(false);
        inputField.setStatus(FieldStatus.SUPPORTED);
        inputField.setFieldType(FieldType.INTEGER);
        inputField.setIndex(3);
        inputField.setAnnotations(stringList);
        inputField.setPrimitive(Boolean.FALSE);
        inputField.setNodeType(NodeType.ELEMENT);
        inputField.setRestrictions(restrictions);
        inputField.setTypeName("typeName");
        inputField.setUserCreated(Boolean.TRUE);
        return inputField;
    }

    private Actions generateActions() {
        Actions actions = new Actions();
        actions.getActions().add(new Camelize());
        actions.getActions().add(new Capitalize());
        actions.getActions().add(new Length());
        actions.getActions().add(new Lowercase());
        actions.getActions().add(new SeparateByDash());
        actions.getActions().add(new SeparateByUnderscore());
        actions.getActions().add(new Trim());
        actions.getActions().add(new TrimLeft());
        actions.getActions().add(new TrimRight());
        actions.getActions().add(new Uppercase());
        return actions;
    }

    private void generateLookupTables(AtlasMapping atlasMapping) {
        LookupTable table = new LookupTable();
        table.setName("lookupTable");
        table.setDescription("lookupTableDescription");
        LookupEntry l1 = new LookupEntry();
        l1.setSourceType(FieldType.STRING);
        l1.setSourceValue("Foo");
        l1.setTargetType(FieldType.STRING);
        l1.setTargetValue("Bar");

        table.getLookupEntry().add(l1);
        atlasMapping.getLookupTables().getLookupTable().add(table);
    }

    private void generateXmlDataSource(AtlasMapping atlasMapping) {
        XmlNamespace xmlNs = generateXmlNamespace("alias", "http://atlasmap.io/xml/test/v2", "http://atlasmap.io/xml/test/v2", Boolean.FALSE);
        XmlDataSource src = generateXmlDataSource("srcId", "srcUri", DataSourceType.SOURCE, "template", xmlNs);
        xmlNs = generateXmlNamespace("alias", "http://atlasmap.io/xml/test/v2", "http://atlasmap.io/xml/test/v2", Boolean.TRUE);
        XmlDataSource tgt = generateXmlDataSource("tgtId", "tgtUri", DataSourceType.TARGET, "template", xmlNs);

        atlasMapping.getDataSource().add(src);
        atlasMapping.getDataSource().add(tgt);
    }

    private XmlNamespace generateXmlNamespace(String alias, String uri, String location, boolean isTarget) {
        XmlNamespace xmlNs = new XmlNamespace();
        xmlNs.setAlias(alias);
        xmlNs.setUri(uri);
        xmlNs.setLocationUri(location);
        xmlNs.setTargetNamespace(isTarget);
        return xmlNs;
    }

    private XmlDataSource generateXmlDataSource(String id, String uri, DataSourceType dataSourceType, String template, XmlNamespace xmlNs) {
        XmlDataSource src = new XmlDataSource();
        src.setId(id);
        src.setUri(uri);
        src.setDataSourceType(dataSourceType);
        src.setTemplate(template);
        src.setXmlNamespaces(new XmlNamespaces());
        src.getXmlNamespaces().getXmlNamespace().add(xmlNs);
        return src;
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

        assertEquals(2, mapping.getDataSource().size());
        assertEquals(DataSourceType.SOURCE, mapping.getDataSource().get(0).getDataSourceType());
        assertEquals("srcId", mapping.getDataSource().get(0).getId());
        assertEquals("srcUri", mapping.getDataSource().get(0).getUri());
        assertEquals("template", ((XmlDataSource) mapping.getDataSource().get(0)).getTemplate());
        assertEquals("alias", ((XmlDataSource) mapping.getDataSource().get(0)).getXmlNamespaces().getXmlNamespace().get(0).getAlias());
        assertEquals("http://atlasmap.io/xml/test/v2", ((XmlDataSource) mapping.getDataSource().get(0)).getXmlNamespaces().getXmlNamespace().get(0).getUri());
        assertEquals("http://atlasmap.io/xml/test/v2", ((XmlDataSource) mapping.getDataSource().get(0)).getXmlNamespaces().getXmlNamespace().get(0).getLocationUri());
        assertEquals(Boolean.FALSE, ((XmlDataSource) mapping.getDataSource().get(0)).getXmlNamespaces().getXmlNamespace().get(0).isTargetNamespace());

        assertEquals(DataSourceType.TARGET, mapping.getDataSource().get(1).getDataSourceType());
        assertEquals("tgtId", mapping.getDataSource().get(1).getId());
        assertEquals("tgtUri", mapping.getDataSource().get(1).getUri());
        assertEquals("template", ((XmlDataSource) mapping.getDataSource().get(1)).getTemplate());
        assertEquals("alias", ((XmlDataSource) mapping.getDataSource().get(1)).getXmlNamespaces().getXmlNamespace().get(0).getAlias());
        assertEquals("http://atlasmap.io/xml/test/v2", ((XmlDataSource) mapping.getDataSource().get(1)).getXmlNamespaces().getXmlNamespace().get(0).getUri());
        assertEquals("http://atlasmap.io/xml/test/v2", ((XmlDataSource) mapping.getDataSource().get(1)).getXmlNamespaces().getXmlNamespace().get(0).getLocationUri());
        assertEquals(Boolean.TRUE, ((XmlDataSource) mapping.getDataSource().get(1)).getXmlNamespaces().getXmlNamespace().get(0).isTargetNamespace());

        assertNotNull(mapping.getLookupTables());
        assertEquals(1, mapping.getLookupTables().getLookupTable().size());
        validateLookupTable(mapping.getLookupTables().getLookupTable().get(0));

        assertNotNull(mapping.getMappings());
        assertEquals(new Integer(1), new Integer(mapping.getMappings().getMapping().size()));
        validateMapping((Mapping) mapping.getMappings().getMapping().get(0));

        assertNotNull(mapping.getProperties());
        assertEquals(1, mapping.getProperties().getProperty().size());
        validateProperty(mapping.getProperties().getProperty().get(0));

    }

    private void validateMapping(Mapping mapping) {
        assertEquals("MapPropertyFieldAlias", mapping.getAlias());
        assertEquals(MappingType.MAP, mapping.getMappingType());
        assertEquals(",", mapping.getDelimiter());
        assertEquals(",", mapping.getDelimiterString());
        assertEquals("description", mapping.getDescription());
        assertEquals("id", mapping.getId());
        assertEquals(1, mapping.getInputField().size());
        validateXmlField((XmlField) mapping.getInputField().get(0));
        assertEquals("lookupTableName", mapping.getLookupTableName());
        assertEquals(1, mapping.getOutputField().size());
        validateXmlField((XmlField) mapping.getOutputField().get(0));
        assertEquals("strategy", mapping.getStrategy());
        assertEquals("strategyClassName", mapping.getStrategyClassName());

    }

    private void validateXmlField(XmlField field) {
        assertEquals(10, field.getActions().getActions().size());
        assertEquals("XmlAccessorType", field.getAnnotations().getString().get(0));
        assertEquals("XmlType", field.getAnnotations().getString().get(1));
        assertEquals(Integer.valueOf(3), field.getArrayDimensions());
        assertEquals(Integer.valueOf(3), field.getArraySize());
        assertEquals(CollectionType.ARRAY, field.getCollectionType());
        assertEquals("docid", field.getDocId());
        assertEquals(FieldType.INTEGER, field.getFieldType());
        assertEquals(Integer.valueOf(3), field.getIndex());
        assertEquals("foo", field.getName());
        assertEquals("/path", field.getPath());
        assertEquals(Boolean.FALSE, field.isPrimitive());
        assertEquals(Boolean.FALSE, field.isRequired());
        assertEquals(FieldStatus.SUPPORTED, field.getStatus());
        assertEquals("bar", field.getValue());
        assertEquals(NodeType.ELEMENT, field.getNodeType());
        assertEquals("typeName", field.getTypeName());
        assertEquals(Boolean.TRUE, field.isUserCreated());
        assertEquals(RestrictionType.LENGTH, field.getRestrictions().getRestriction().get(0).getType());
        assertEquals("100", field.getRestrictions().getRestriction().get(0).getValue());
    }

    private void validateLookupTable(LookupTable lookupTable) {
        assertEquals("lookupTableDescription", lookupTable.getDescription());
        assertEquals("lookupTable", lookupTable.getName());
        assertEquals(FieldType.STRING, lookupTable.getLookupEntry().get(0).getSourceType());
        assertEquals("Foo", lookupTable.getLookupEntry().get(0).getSourceValue());
        assertEquals(FieldType.STRING, lookupTable.getLookupEntry().get(0).getTargetType());
        assertEquals("Bar", lookupTable.getLookupEntry().get(0).getTargetValue());
    }

    private void validateProperty(Property p) {
        assertEquals(FieldType.INTEGER, p.getFieldType());
        assertEquals("foo", p.getName());
        assertEquals("bar", p.getValue());
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
