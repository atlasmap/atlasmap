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
package io.atlasmap.java.v2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        ArrayList<Action> actions = generateActions();

        StringList annotations = generateAnnotations();

        ModifierList modifierList = generateModifierList();

        StringList parameterizedTypes = generateParameterizedTypes();

        JavaField inputField = generateJavaField(actions, annotations, modifierList, parameterizedTypes);

        JavaField outputField = generateJavaField(actions, annotations, modifierList, parameterizedTypes);


        Mapping fm = AtlasModelFactory.createMapping(MappingType.MAP);
        fm.getInputField().add(inputField);
        fm.getOutputField().add(outputField);
        populateMapping(fm, MappingType.MAP, "MapPropertyFieldAlias", ",", ",");
        populateMappingString(fm, "description", "id", "lookupTableName", "strategy", "strategyClassName");

        atlasMapping.getMappings().getMapping().add(fm);

        generateProperties(atlasMapping);

        return atlasMapping;
    }

    private void populateMapping(Mapping mapping, MappingType mappingType, String alias, String delimiter, String delimiterString) {
        if (mappingType != null) {
            mapping.setMappingType(mappingType);
        }
        mapping.setAlias(alias);
        mapping.setDelimiter(delimiter);
        mapping.setDelimiterString(delimiterString);
    }

    private void populateMappingString(Mapping mapping, String description, String id, String lookupTableName, String strategy, String strategyClassName) {
        mapping.setDescription(description);
        mapping.setId(id);
        mapping.setLookupTableName(lookupTableName);
        mapping.setStrategy(strategy);
        mapping.setStrategyClassName(strategyClassName);
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
        DataSource src = generateDataSource("srcId", "srcUri", DataSourceType.SOURCE);
        DataSource tgt = generateDataSource("tgtId", "tgtUri", DataSourceType.TARGET);

        atlasMapping.getDataSource().add(src);
        atlasMapping.getDataSource().add(tgt);
    }

    private DataSource generateDataSource(String id, String uri, DataSourceType type) {
        DataSource dataSource = new DataSource();
        dataSource.setId(id);
        dataSource.setUri(uri);
        dataSource.setDataSourceType(type);
        return dataSource;
    }

    protected AtlasMapping generateActionMapping() {
        AtlasMapping mapping = generateAtlasMapping();
        JavaField outputField = (JavaField) ((Mapping) mapping.getMappings().getMapping().get(0)).getOutputField().get(0);

        populateJavaField(outputField, generateAnnotations(), generateModifierList(), generateParameterizedTypes(), Boolean.FALSE, Boolean.TRUE);
        populateJavaFieldString(outputField, "JavaField", "ArrayList", "getMethod", "setMethod", "foo");
        populateFieldComplexObject(outputField, generateActions(),  CollectionType.ARRAY,  FieldStatus.SUPPORTED, FieldType.INTEGER);
        populateFieldSimpleObject(outputField, 3, "docid", "/path", false, "bar");

        return mapping;
    }

    protected AtlasMapping generatePropertyReferenceMapping() {
        AtlasMapping mapping = generateAtlasMapping();

        PropertyField inputField = new PropertyField();
        inputField.setName("foo");
        ArrayList<Action> actions = new ArrayList<Action>();
        actions.add(new Trim());
        populateFieldComplexObject(inputField, actions, CollectionType.ARRAY, FieldStatus.SUPPORTED, FieldType.INTEGER);
        populateFieldSimpleObject(inputField, 3, "docid", "/path", false, "bar");

        Mapping fm = (Mapping) mapping.getMappings().getMapping().get(0);
        fm.getInputField().add(inputField);
        fm.getOutputField().add(inputField);
        populateMapping(fm, MappingType.MAP, "MapPropertyFieldAlias", ",", ",");
        populateMappingString(fm, "description", "id", "lookupTableName", "strategy", "strategyClassName");

        generateProperties(mapping);
        return mapping;
    }

    protected AtlasMapping generateConstantMapping() {
        AtlasMapping mapping = generateAtlasMapping();

        ConstantField inputField = new ConstantField();
        ArrayList<Action> actions = new ArrayList<Action>();
        actions.add(new Trim());
        populateFieldComplexObject(inputField, actions, CollectionType.ARRAY, FieldStatus.SUPPORTED, FieldType.INTEGER);
        populateFieldSimpleObject(inputField, 3, "docid", "/path", false, "bar");

        Mapping fm = (Mapping) mapping.getMappings().getMapping().get(0);
        fm.getInputField().add(inputField);
        fm.getOutputField().add(inputField);
        populateMapping(fm, MappingType.MAP, "MapPropertyFieldAlias", ",", ",");
        populateMappingString(fm, "description", "id", "lookupTableName", "strategy", "strategyClassName");
        return mapping;
    }

    protected AtlasMapping generateCollectionMapping() {
        AtlasMapping innerMapping1 = generateAtlasMapping();
        AtlasMapping innerMapping2 = generateAtlasMapping();

        Collection cMapping = (Collection) AtlasModelFactory.createMapping(MappingType.COLLECTION);
        cMapping.setMappings(new Mappings());

        cMapping.getMappings().getMapping().addAll(innerMapping1.getMappings().getMapping());
        cMapping.getMappings().getMapping().addAll(innerMapping2.getMappings().getMapping());
        cMapping.setCollectionType(CollectionType.LIST);
        cMapping.setCollectionSize(new BigInteger("2"));
        cMapping.setAlias("alias");
        cMapping.setDescription("description");

        AtlasMapping mapping = generateAtlasMapping();
        mapping.getMappings().getMapping().clear();
        mapping.getMappings().getMapping().add(cMapping);
        return mapping;
    }

    protected AtlasMapping generateCombineMapping() {
        ArrayList<Action> actions = generateActions();

        StringList annotations = generateAnnotations();

        ModifierList modifierList = generateModifierList();

        StringList parameterizedTypes = generateParameterizedTypes();

        JavaField inputJavaField = generateJavaField(actions, annotations, modifierList, parameterizedTypes);

        JavaField inputJavaFieldB = generateJavaField(actions, annotations, modifierList, parameterizedTypes);

        JavaField outputJavaFieldA = generateJavaField(actions, annotations, modifierList, parameterizedTypes);

        Mapping fm = (Mapping) AtlasModelFactory.createMapping(MappingType.COMBINE);

        fm.getInputField().add(inputJavaField);
        fm.getInputField().add(inputJavaFieldB);
        fm.getOutputField().add(outputJavaFieldA);

        populateMapping(fm, MappingType.COMBINE, "MapPropertyFieldAlias", ",", ",");
        populateMappingString(fm, "description", "id", "lookupTableName", "strategy", "strategyClassName");

        AtlasMapping mapping = generateAtlasMapping();
        mapping.getMappings().getMapping().clear();
        mapping.getMappings().getMapping().add(fm);

        generateProperties(mapping);

        return mapping;
    }

    private void generateProperties(AtlasMapping mapping) {
        Property p = new Property();
        p.setName("foo");
        p.setValue("bar");
        p.setFieldType(FieldType.INTEGER);
        mapping.setProperties(new Properties());
        mapping.getProperties().getProperty().add(p);
    }

    protected AtlasMapping generateMultiSourceMapping() {
        AtlasMapping mapping = generateSeparateAtlasMapping();

        DataSource sourceA = generateDataSource("srcId", "srcUri", DataSourceType.SOURCE);
        mapping.getDataSource().add(sourceA);

        DataSource targetA = generateDataSource("tgtId", "tgtUri", DataSourceType.TARGET);
        mapping.getDataSource().add(targetA);

        DataSource targetB = generateDataSource("tgtId", "tgtUri", DataSourceType.TARGET);
        mapping.getDataSource().add(targetB);

        Mapping fm = (Mapping) mapping.getMappings().getMapping().get(0);
        fm.getInputField().get(0).setDocId("docid");
        fm.getOutputField().get(0).setDocId("docid");
        fm.getOutputField().get(1).setDocId("docid");
        populateMapping(fm, MappingType.MAP, "MapPropertyFieldAlias", ",", ",");
        populateMappingString(fm, "description", "id", "lookupTableName", "strategy", "strategyClassName");
        return mapping;
    }

    protected AtlasMapping generateSeparateAtlasMapping() {

        ArrayList<Action> actions = generateActions();

        StringList annotations = generateAnnotations();

        ModifierList modifierList = generateModifierList();

        StringList parameterizedTypes = generateParameterizedTypes();

        JavaField inputJavaField = generateJavaField(actions, annotations, modifierList, parameterizedTypes);

        JavaField outputJavaFieldA = generateJavaField(actions, annotations, modifierList, parameterizedTypes);

        JavaField outputJavaFieldB = generateJavaField(actions, annotations, modifierList, parameterizedTypes);

        Mapping fm = (Mapping) AtlasModelFactory.createMapping(MappingType.SEPARATE);

        fm.getInputField().add(inputJavaField);
        fm.getOutputField().add(outputJavaFieldA);
        fm.getOutputField().add(outputJavaFieldB);
        populateMapping(fm, MappingType.SEPARATE, "MapPropertyFieldAlias", ",", ",");
        populateMappingString(fm, "description", "id", "lookupTableName", "strategy", "strategyClassName");

        AtlasMapping mapping = generateAtlasMapping();
        mapping.getMappings().getMapping().clear();
        mapping.getMappings().getMapping().add(fm);
        return mapping;
    }

    private StringList generateAnnotations() {
        StringList annotations = new StringList();
        annotations.getString().add("XmlAccessorType");
        annotations.getString().add("XmlType");
        return annotations;
    }

    private ModifierList generateModifierList() {
        ModifierList modifierList = new ModifierList();
        modifierList.getModifier().add(Modifier.PUBLIC);
        modifierList.getModifier().add(Modifier.STATIC);
        return modifierList;
    }

    private StringList generateParameterizedTypes() {
        StringList parameterizedTypes = new StringList();
        parameterizedTypes.getString().add("String");
        parameterizedTypes.getString().add("Integer");
        return parameterizedTypes;
    }

    private JavaField generateJavaField(ArrayList<Action> actions, StringList annotations, ModifierList modifierList, StringList parameterizedTypes) {
        JavaField outputJavaFieldB = new JavaField();
        populateJavaField(outputJavaFieldB, annotations, modifierList, parameterizedTypes, Boolean.FALSE, Boolean.TRUE);
        populateJavaFieldString(outputJavaFieldB, "JavaField", "ArrayList", "getMethod", "setMethod", "foo");
        populateFieldComplexObject(outputJavaFieldB, actions, CollectionType.ARRAY, FieldStatus.SUPPORTED, FieldType.INTEGER);
        populateFieldSimpleObject(outputJavaFieldB, 3, "docid", "/path", false, "bar");
        return outputJavaFieldB;
    }

    private void populateJavaField(JavaField javaField, StringList annotations, ModifierList modifierList, StringList parameterizedTypes, boolean isPrimitive, boolean isSynthetic) {
        javaField.setAnnotations(annotations);
        javaField.setModifiers(modifierList);
        javaField.setParameterizedTypes(parameterizedTypes);
        javaField.setPrimitive(isPrimitive);
        javaField.setSynthetic(isSynthetic);
    }

    private void populateJavaFieldString(JavaField javaField, String className, String collectionClassName, String getMethod, String setMethod, String name) {
        javaField.setClassName(className);
        javaField.setCollectionClassName(collectionClassName);
        javaField.setGetMethod(getMethod);
        javaField.setSetMethod(setMethod);
        javaField.setName(name);

    }
    private void populateFieldComplexObject(Field field, ArrayList<Action> actions, CollectionType collectionType, FieldStatus status, FieldType type) {
        field.setActions(actions);
        field.setCollectionType(collectionType);
        field.setStatus(status);
        field.setFieldType(type);
    }

    private void populateFieldSimpleObject(Field field, int n, String docid, String path, boolean isRequired, String value) {
        field.setValue(value);
        field.setIndex(n);
        field.setArrayDimensions(n);
        field.setArraySize(n);
        field.setDocId(docid);
        field.setPath(path);
        field.setRequired(isRequired);
    }

    private ArrayList<Action> generateActions() {
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
        return actions;
    }

    public MavenClasspathRequest generateMavenClasspathRequest() {
        MavenClasspathRequest mavenClasspathRequest = new MavenClasspathRequest();
        mavenClasspathRequest.setExecuteTimeout(30000L);
        mavenClasspathRequest.setPomXmlData(generatePomXmlAsString());
        return mavenClasspathRequest;
    }

    public ClassInspectionRequest generateClassInspectionRequest() {
        ClassInspectionRequest classInspectionRequest = new ClassInspectionRequest();
        classInspectionRequest.setClasspath(
                "/Users/mattrpav/.m2/repository/org/twitter4j/twitter4j-core/4.0.5/twitter4j-core-4.0.5.jar");
        classInspectionRequest.setClassName("twitter4j.StatusJSONImpl");
        classInspectionRequest.setFieldNameBlacklist(new StringList());
        classInspectionRequest.getFieldNameBlacklist().getString().add("createdAt");
        return classInspectionRequest;
    }

    public String generatePomXmlAsString() {
        return new String(
                "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">"
                        + "\t<modelVersion>4.0.0</modelVersion>" + "\t<groupId>foo.bar</groupId>"
                        + "\t<artifactId>test.model</artifactId>" + "\t<version>1.10.0</version>"
                        + "\t<packaging>jar</packaging>" + "\t<name>Test :: Model</name>" + "\t<dependencies>"
                        + "\t\t<dependency>" + "\t\t\t<groupId>com.fasterxml.jackson.core</groupId>"
                        + "\t\t\t<artifactId>jackson-annotations</artifactId>"
                        + "\t\t\t<version>2.8.5</version>" + "\t\t</dependency>" + "\t\t<dependency>"
                        + "]t]t]t<groupId>com.fasterxml.jackson.core</groupId>"
                        + "\t\t\t<artifactId>jackson-databind</artifactId>" + "\t\t\t<version>2.8.5</version>"
                        + "\t\t</dependency>" + "\t\t<dependency>"
                        + "\t\t\t<groupId>com.fasterxml.jackson.core</groupId>"
                        + "\t\t\t<artifactId>jackson-core</artifactId>" + "\t\t\t<version>2.8.5</version>"
                        + "\t\t</dependency>" + "\t</dependencies>" + "</project>");
    }

    protected void validateAtlasMapping(AtlasMapping atlasMapping) {
        assertNotNull(atlasMapping);
        assertEquals("junit", atlasMapping.getName());

        assertEquals(2, atlasMapping.getDataSource().size());
        valiateDataSource(atlasMapping.getDataSource().get(0), DataSourceType.SOURCE, "srcId", "srcUri");
        valiateDataSource(atlasMapping.getDataSource().get(1), DataSourceType.TARGET, "tgtId", "tgtUri");

        assertNotNull(atlasMapping.getLookupTables());
        assertEquals(1, atlasMapping.getLookupTables().getLookupTable().size());
        validateLookupTable(atlasMapping.getLookupTables().getLookupTable().get(0));

        assertNotNull(atlasMapping.getMappings());
        assertEquals(1, atlasMapping.getMappings().getMapping().size());
        Mapping mapping = (Mapping) atlasMapping.getMappings().getMapping().get(0);
        assertEquals(1, mapping.getInputField().size());
        validateJavaField((JavaField) mapping.getInputField().get(0));
        assertEquals(1, mapping.getOutputField().size());
        validateJavaField((JavaField) mapping.getOutputField().get(0));

        validateMapping(mapping, MappingType.MAP, generateMappingParams());

        assertNotNull(atlasMapping.getProperties());
        assertEquals(1, atlasMapping.getProperties().getProperty().size());
        validateProperty(atlasMapping.getProperties().getProperty().get(0));

    }

    private Map<String, String> generateMappingParams() {
        Map<String, String> params = new HashMap<>();
        params.put("alias", "MapPropertyFieldAlias");
        params.put("delimiter", ",");
        params.put("delimiterString", ",");
        params.put("description", "description");
        params.put("id", "id");
        params.put("lookupTableName", "lookupTableName");
        params.put("strategy", "strategy");
        params.put("strategyClassName", "strategyClassName");
        return params;
    }

    private void validateLookupTable(LookupTable lookupTable) {
        assertEquals("lookupTableDescription", lookupTable.getDescription());
        assertEquals("lookupTable", lookupTable.getName());
        assertEquals(FieldType.STRING, lookupTable.getLookupEntry().get(0).getSourceType());
        assertEquals("Foo", lookupTable.getLookupEntry().get(0).getSourceValue());
        assertEquals(FieldType.STRING, lookupTable.getLookupEntry().get(0).getTargetType());
        assertEquals("Bar", lookupTable.getLookupEntry().get(0).getTargetValue());
    }

    private void validateMapping(Mapping mapping, MappingType mappingType, Map<String, String> params) {
        assertEquals(params.get("alias"), mapping.getAlias());
        assertEquals(mappingType, mapping.getMappingType());
        assertEquals(params.get("delimiter"), mapping.getDelimiter());
        assertEquals(params.get("delimiterString"), mapping.getDelimiterString());
        assertEquals(params.get("description"), mapping.getDescription());
        assertEquals(params.get("id"), mapping.getId());
        assertEquals(params.get("lookupTableName"), mapping.getLookupTableName());
        assertEquals(params.get("strategy"), mapping.getStrategy());
        assertEquals(params.get("strategyClassName"), mapping.getStrategyClassName());
    }

    private void validateJavaField(JavaField field) {
        assertEquals("XmlAccessorType", field.getAnnotations().getString().get(0));
        assertEquals("XmlType", field.getAnnotations().getString().get(1));
        assertEquals("JavaField", field.getClassName());
        assertEquals("ArrayList", field.getCollectionClassName());
        assertEquals("getMethod", field.getGetMethod());
        assertEquals(2, field.getModifiers().getModifier().size());
        assertEquals(Modifier.PUBLIC, field.getModifiers().getModifier().get(0));
        assertEquals(Modifier.STATIC, field.getModifiers().getModifier().get(1));
        assertEquals("foo", field.getName());
        assertEquals("String", field.getParameterizedTypes().getString().get(0));
        assertEquals("Integer", field.getParameterizedTypes().getString().get(1));
        assertEquals(Boolean.FALSE, field.isPrimitive());
        assertEquals("setMethod", field.getSetMethod());
        assertEquals(Boolean.TRUE, field.isSynthetic());
        validateField(field, 10);
    }

    private void validateField(Field field, int actionSize) {
        assertEquals(actionSize, field.getActions().size());
        assertEquals(Integer.valueOf(3), field.getArrayDimensions());
        assertEquals(Integer.valueOf(3), field.getArraySize());
        assertEquals(CollectionType.ARRAY, field.getCollectionType());
        assertEquals("docid", field.getDocId());
        assertEquals(FieldType.INTEGER, field.getFieldType());
        assertEquals(Integer.valueOf(3), field.getIndex());
        assertEquals("/path", field.getPath());
        assertEquals(Boolean.FALSE, field.isRequired());
        assertEquals(FieldStatus.SUPPORTED, field.getStatus());
        assertEquals("bar", field.getValue());
    }

    private void validateProperty(Property p) {
        assertEquals(FieldType.INTEGER, p.getFieldType());
        assertEquals("foo", p.getName());
        assertEquals("bar", p.getValue());
    }

    protected void validateSeparateAtlasMapping(AtlasMapping atlasMapping) {
        assertNotNull(atlasMapping);
        assertEquals("junit", atlasMapping.getName());

        assertEquals(2, atlasMapping.getDataSource().size());
        valiateDataSource(atlasMapping.getDataSource().get(0), DataSourceType.SOURCE, "srcId", "srcUri");
        valiateDataSource(atlasMapping.getDataSource().get(1), DataSourceType.TARGET, "tgtId", "tgtUri");

        assertNotNull(atlasMapping.getLookupTables());
        assertEquals(1, atlasMapping.getLookupTables().getLookupTable().size());
        validateLookupTable(atlasMapping.getLookupTables().getLookupTable().get(0));

        assertNotNull(atlasMapping.getMappings());
        assertEquals(1, atlasMapping.getMappings().getMapping().size());
        validateSeparateMapping((Mapping) atlasMapping.getMappings().getMapping().get(0));

        assertNotNull(atlasMapping.getProperties());
        assertEquals(1, atlasMapping.getProperties().getProperty().size());
        validateProperty(atlasMapping.getProperties().getProperty().get(0));
    }

    private void validateSeparateMapping(Mapping mapping) {
        assertEquals(1, mapping.getInputField().size());
        validateJavaField((JavaField) mapping.getInputField().get(0));
        assertEquals(2, mapping.getOutputField().size());
        validateJavaField((JavaField) mapping.getOutputField().get(0));
        if (mapping.getOutputField().size() > 1) {
            validateJavaField((JavaField) mapping.getOutputField().get(1));
        }

        validateMapping(mapping, MappingType.SEPARATE, generateMappingParams());
    }

    protected void validateCombineAtlasMapping(AtlasMapping atlasMapping) {
        assertNotNull(atlasMapping);
        assertEquals("junit", atlasMapping.getName());

        assertEquals(2, atlasMapping.getDataSource().size());
        valiateDataSource(atlasMapping.getDataSource().get(0), DataSourceType.SOURCE, "srcId", "srcUri");
        valiateDataSource(atlasMapping.getDataSource().get(1), DataSourceType.TARGET, "tgtId", "tgtUri");

        assertNotNull(atlasMapping.getLookupTables());
        assertEquals(1, atlasMapping.getLookupTables().getLookupTable().size());
        validateLookupTable(atlasMapping.getLookupTables().getLookupTable().get(0));

        assertNotNull(atlasMapping.getMappings());
        assertEquals(1, atlasMapping.getMappings().getMapping().size());
        validateCombineMapping((Mapping) atlasMapping.getMappings().getMapping().get(0));

        assertNotNull(atlasMapping.getProperties());
        assertEquals(1, atlasMapping.getProperties().getProperty().size());
        validateProperty(atlasMapping.getProperties().getProperty().get(0));
    }

    private void validateCombineMapping(Mapping mapping) {
        assertEquals(2, mapping.getInputField().size());
        validateJavaField((JavaField) mapping.getInputField().get(0));
        validateJavaField((JavaField) mapping.getInputField().get(1));
        assertEquals(1, mapping.getOutputField().size());
        validateJavaField((JavaField) mapping.getOutputField().get(0));

        validateMapping(mapping, MappingType.COMBINE, generateMappingParams());
    }

    protected void validatePropertyAtlasMapping(AtlasMapping atlasMapping) {
        assertNotNull(atlasMapping);
        assertEquals("junit", atlasMapping.getName());

        assertEquals(2, atlasMapping.getDataSource().size());
        valiateDataSource(atlasMapping.getDataSource().get(0), DataSourceType.SOURCE, "srcId", "srcUri");
        valiateDataSource(atlasMapping.getDataSource().get(1), DataSourceType.TARGET, "tgtId", "tgtUri");

        assertNotNull(atlasMapping.getLookupTables());
        assertEquals(1, atlasMapping.getLookupTables().getLookupTable().size());
        validateLookupTable(atlasMapping.getLookupTables().getLookupTable().get(0));

        assertNotNull(atlasMapping.getMappings());
        assertEquals(1, atlasMapping.getMappings().getMapping().size());
        validatePropertyMapping((Mapping) atlasMapping.getMappings().getMapping().get(0));

        assertNotNull(atlasMapping.getProperties());
        assertEquals(1, atlasMapping.getProperties().getProperty().size());
        validateProperty(atlasMapping.getProperties().getProperty().get(0));
    }

    private void validatePropertyMapping(Mapping mapping) {
        assertEquals(2, mapping.getInputField().size());
        validateJavaField((JavaField) mapping.getInputField().get(0));
        validatePropertyField((PropertyField) mapping.getInputField().get(1));
        assertEquals(2, mapping.getOutputField().size());
        validateJavaField((JavaField) mapping.getOutputField().get(0));
        validatePropertyField((PropertyField) mapping.getOutputField().get(1));

        validateMapping(mapping, MappingType.MAP, generateMappingParams());
    }

    private void validatePropertyField(PropertyField field) {
        assertEquals("foo", field.getName());
        validateField(field, 1);
    }

    protected void validateConstantAtlasMapping(AtlasMapping atlasMapping) {
        assertNotNull(atlasMapping);
        assertEquals("junit", atlasMapping.getName());

        assertEquals(2, atlasMapping.getDataSource().size());
        valiateDataSource(atlasMapping.getDataSource().get(0), DataSourceType.SOURCE, "srcId", "srcUri");
        valiateDataSource(atlasMapping.getDataSource().get(1), DataSourceType.TARGET, "tgtId", "tgtUri");

        assertNotNull(atlasMapping.getLookupTables());
        assertEquals(1, atlasMapping.getLookupTables().getLookupTable().size());
        validateLookupTable(atlasMapping.getLookupTables().getLookupTable().get(0));

        assertNotNull(atlasMapping.getMappings());
        assertEquals(1, atlasMapping.getMappings().getMapping().size());
        validateConstantMapping((Mapping) atlasMapping.getMappings().getMapping().get(0));

        assertNotNull(atlasMapping.getProperties());
        assertEquals(1, atlasMapping.getProperties().getProperty().size());
        validateProperty(atlasMapping.getProperties().getProperty().get(0));
    }

    private void validateConstantMapping(Mapping mapping) {
        assertEquals(2, mapping.getInputField().size());
        validateJavaField((JavaField) mapping.getInputField().get(0));
        validateField(mapping.getInputField().get(1), 1);
        assertEquals(2, mapping.getOutputField().size());
        validateJavaField((JavaField) mapping.getOutputField().get(0));
        validateField(mapping.getOutputField().get(1), 1);

        validateMapping(mapping, MappingType.MAP, generateMappingParams());
    }

    protected void validateMultisourceAtlasMapping(AtlasMapping atlasMapping) {
        assertNotNull(atlasMapping);
        assertEquals("junit", atlasMapping.getName());

        assertEquals(5, atlasMapping.getDataSource().size());
        valiateDataSource(atlasMapping.getDataSource().get(0), DataSourceType.SOURCE, "srcId", "srcUri");
        valiateDataSource(atlasMapping.getDataSource().get(1), DataSourceType.TARGET, "tgtId", "tgtUri");
        valiateDataSource(atlasMapping.getDataSource().get(2), DataSourceType.SOURCE, "srcId", "srcUri");
        valiateDataSource(atlasMapping.getDataSource().get(3), DataSourceType.TARGET, "tgtId", "tgtUri");
        valiateDataSource(atlasMapping.getDataSource().get(4), DataSourceType.TARGET, "tgtId", "tgtUri");

        assertNotNull(atlasMapping.getLookupTables());
        assertEquals(1, atlasMapping.getLookupTables().getLookupTable().size());
        validateLookupTable(atlasMapping.getLookupTables().getLookupTable().get(0));

        assertNotNull(atlasMapping.getMappings());
        assertEquals(1, atlasMapping.getMappings().getMapping().size());
        validateMultisourceMapping((Mapping) atlasMapping.getMappings().getMapping().get(0));

        assertNotNull(atlasMapping.getProperties());
        assertEquals(1, atlasMapping.getProperties().getProperty().size());
        validateProperty(atlasMapping.getProperties().getProperty().get(0));
    }

    private void valiateDataSource(DataSource dataSource, DataSourceType dataSourceType, String id, String uri) {
        assertEquals(dataSourceType, dataSource.getDataSourceType());
        assertEquals(id, dataSource.getId());
        assertEquals(uri, dataSource.getUri());
    }

    private void validateMultisourceMapping(Mapping mapping) {
        assertEquals(1, mapping.getInputField().size());
        validateJavaField((JavaField) mapping.getInputField().get(0));

        assertEquals(2, mapping.getOutputField().size());
        validateJavaField((JavaField) mapping.getOutputField().get(0));
        validateJavaField((JavaField) mapping.getOutputField().get(1));

        validateMapping(mapping, MappingType.MAP, generateMappingParams());
    }

    protected void validateCollectionAtlasMapping(AtlasMapping atlasMapping) {
        assertNotNull(atlasMapping);
        assertEquals("junit", atlasMapping.getName());

        assertEquals(2, atlasMapping.getDataSource().size());
        valiateDataSource(atlasMapping.getDataSource().get(0), DataSourceType.SOURCE, "srcId", "srcUri");
        valiateDataSource(atlasMapping.getDataSource().get(1), DataSourceType.TARGET, "tgtId", "tgtUri");

        assertNotNull(atlasMapping.getLookupTables());
        assertEquals(1, atlasMapping.getLookupTables().getLookupTable().size());
        validateLookupTable(atlasMapping.getLookupTables().getLookupTable().get(0));

        assertNotNull(atlasMapping.getMappings());
        assertEquals(1, atlasMapping.getMappings().getMapping().size());
        validateCollectionMapping((Collection) atlasMapping.getMappings().getMapping().get(0));

        assertNotNull(atlasMapping.getProperties());
        assertEquals(1, atlasMapping.getProperties().getProperty().size());
        validateProperty(atlasMapping.getProperties().getProperty().get(0));
    }

    private void validateCollectionMapping(Collection collection) {
        assertEquals(new BigInteger("2"), collection.getCollectionSize());
        assertEquals(CollectionType.LIST, collection.getCollectionType());
        Mapping mapping = (Mapping) collection.getMappings().getMapping().get(0);

        assertEquals(1, mapping.getInputField().size());
        validateJavaField((JavaField) mapping.getInputField().get(0));
        assertEquals(1, mapping.getOutputField().size());
        validateJavaField((JavaField) mapping.getOutputField().get(0));
        validateMapping(mapping, MappingType.MAP, generateMappingParams());

        mapping = (Mapping) collection.getMappings().getMapping().get(1);
        assertEquals(1, mapping.getInputField().size());
        validateJavaField((JavaField) mapping.getInputField().get(0));
        assertEquals(1, mapping.getOutputField().size());
        validateJavaField((JavaField) mapping.getOutputField().get(0));
        validateMapping(mapping, MappingType.MAP, generateMappingParams());
    }

}
