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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;

import io.atlasmap.v2.*;

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
                    }
                    throw exc;
                }
            });
        }
    }

    protected AtlasMapping generateAtlasMapping() {
        AtlasMapping atlasMapping = AtlasModelFactory.createAtlasMapping();
        atlasMapping.setName("junit");

        generateDataSource(atlasMapping);

        generateLookupTables(atlasMapping);

        generateMapping(atlasMapping);

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

    private void generateMapping(AtlasMapping atlasMapping) {
        Mapping mapping = AtlasModelFactory.createMapping(MappingType.MAP);
        generateInputField(mapping);

        generateOutputField(mapping);

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
    }

    private void generateOutputField(Mapping mapping) {
        JsonField outputField = new JsonField();
        generateActions(outputField);

        populateJsonField(outputField);

        mapping.getOutputField().add(outputField);
    }

    private void generateInputField(Mapping mapping) {
        JsonField inputField = new JsonField();
        generateActions(inputField);

        populateJsonField(inputField);

        mapping.getInputField().add(inputField);
    }

    private void populateJsonField(JsonField inputField) {
        inputField.setName("foo");
        inputField.setValue("bar");
        inputField.setArrayDimensions(3);
        inputField.setArraySize(3);
        inputField.setCollectionType(CollectionType.ARRAY);
        inputField.setDocId("docid");
        inputField.setPath("/path");
        inputField.setRequired(false);
        inputField.setStatus(FieldStatus.SUPPORTED);
        inputField.setFieldType(FieldType.INTEGER);
        inputField.setIndex(3);
        inputField.setPrimitive(Boolean.FALSE);
        inputField.setTypeName("typeName");
        inputField.setUserCreated(Boolean.TRUE);
    }

    private void generateActions(JsonField inputField) {
        ArrayList<Action> actions = new ArrayList<Action>();
        actions.add(new Camelize());
        actions.add(new Capitalize());
        actions.add(new Length());
        actions.add(new Lowercase());
        actions.add(new SeparateByDash());
        actions.add(new SeparateByUnderscore());
        actions.add(new Trim());
        actions.add(new TrimLeft());
        actions.add(new TrimRight());
        actions.add(new Uppercase());
        inputField.setActions(actions);
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

    private void generateDataSource(AtlasMapping atlasMapping) {
        JsonDataSource src = generateJsonDataSource("srcId", "srcUri", DataSourceType.SOURCE, "template");
        JsonDataSource tgt = generateJsonDataSource("tgtId", "tgtUri", DataSourceType.TARGET, "template");

        atlasMapping.getDataSource().add(src);
        atlasMapping.getDataSource().add(tgt);
    }

    private JsonDataSource generateJsonDataSource(String id, String uri, DataSourceType dataSourceType, String template) {
        JsonDataSource src = new JsonDataSource();
        src.setId(id);
        src.setUri(uri);
        src.setDataSourceType(dataSourceType);
        src.setTemplate(template);
        return src;
    }

    protected void validateAtlasMapping(AtlasMapping mapping) {
        assertNotNull(mapping);
        assertNotNull(mapping.getName());
        assertEquals("junit", mapping.getName());

        assertEquals(2, mapping.getDataSource().size());
        validateJsonDataSource(mapping.getDataSource().get(0), DataSourceType.SOURCE, "srcId", "srcUri", "template");
        validateJsonDataSource(mapping.getDataSource().get(1), DataSourceType.TARGET, "tgtId", "tgtUri", "template");

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

    private void validateJsonDataSource(DataSource ds, DataSourceType dataSourceType,
            String id, String uri, String template) {
        assertEquals(dataSourceType, ds.getDataSourceType());
        assertEquals(id, ds.getId());
        assertEquals(uri, ds.getUri());
        assertEquals(template, ((JsonDataSource) ds).getTemplate());
    }

    private void validateMapping(Mapping mapping) {
        assertEquals("MapPropertyFieldAlias", mapping.getAlias());
        assertEquals(MappingType.MAP, mapping.getMappingType());
        assertEquals(",", mapping.getDelimiter());
        assertEquals(",", mapping.getDelimiterString());
        assertEquals("description", mapping.getDescription());
        assertEquals("id", mapping.getId());
        assertEquals(1, mapping.getInputField().size());
        validateJsonField((JsonField) mapping.getInputField().get(0));
        assertEquals("lookupTableName", mapping.getLookupTableName());
        assertEquals(1, mapping.getOutputField().size());
        validateJsonField((JsonField) mapping.getOutputField().get(0));
        assertEquals("strategy", mapping.getStrategy());
        assertEquals("strategyClassName", mapping.getStrategyClassName());

    }

    private void validateJsonField(JsonField field) {
        assertEquals(10, field.getActions().size());
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
        assertEquals("typeName", field.getTypeName());
        assertEquals(Boolean.TRUE, field.isUserCreated());
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

}
