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
package io.atlasmap.v2;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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

    protected LookupTable generateLookupTable() {
        LookupTable lookupTable = new LookupTable();
        lookupTable.setName("junit-lookuptable");
        lookupTable.setDescription("Sample lookup table entry for reference");

        LookupEntry entry1 = new LookupEntry();
        entry1.setSourceType(FieldType.STRING);
        entry1.setSourceValue("foo");
        entry1.setTargetType(FieldType.STRING);
        entry1.setTargetValue("bar");

        LookupEntry entry2 = new LookupEntry();
        entry2.setSourceType(FieldType.STRING);
        entry2.setSourceValue("blah");
        entry2.setTargetType(FieldType.STRING);
        entry2.setTargetValue("blur");

        lookupTable.getLookupEntry().add(entry1);
        lookupTable.getLookupEntry().add(entry2);
        return lookupTable;
    }

    protected AtlasMapping generateReferenceAtlasMapping() {
        AtlasMapping mapping = AtlasModelFactory.createAtlasMapping();
        mapping.setName("junit");

        DataSource src = new DataSource();
        src.setId("srcId");
        src.setUri("java:foo.bar");
        src.setDataSourceType(DataSourceType.SOURCE);

        DataSource tgt = new DataSource();
        src.setId("tgtId");
        tgt.setUri("xml:blah.meow");
        tgt.setDataSourceType(DataSourceType.TARGET);

        mapping.getDataSource().add(src);
        mapping.getDataSource().add(tgt);

        addMapField(mapping, "map", false);
        addCombineField(mapping, "combine");
        addMapField(mapping, "mapActions", true);
        addSeparateField(mapping, "separate");
        addProperties(mapping);
        addMapPropertyField(mapping, "prop");
        addMapLookupField(mapping, "lookup");
        addMapSimpleField(mapping, "simple");
        addMapConstantField(mapping, "constant");

        addManyToOneMapping(mapping, "manyToOne");
        addOneToManyMapping(mapping, "oneToMany");
        addConditionalMapping(mapping, "conditional");

        return mapping;
    }

    protected void validateReferenceAtlasMapping(AtlasMapping mapping) {
        assertNotNull(mapping);
        assertNotNull(mapping.getName());
        assertEquals("junit", mapping.getName());
        assertNotNull(mapping.getMappings());
        assertEquals(new Integer(11), new Integer(mapping.getMappings().getMapping().size()));
        assertNotNull(mapping.getProperties());

        Mapping f0 = (Mapping) mapping.getMappings().getMapping().get(0);
        validateMapField(f0, "map", false);

        Mapping f1 = (Mapping) mapping.getMappings().getMapping().get(1);
        validateCombineField(f1, "combine");

        Mapping f2 = (Mapping) mapping.getMappings().getMapping().get(2);
        validateMapField(f2, "mapActions", true);

        Mapping f3 = (Mapping) mapping.getMappings().getMapping().get(3);
        validateSeparateField(f3, "separate");

        Mapping f4 = (Mapping) mapping.getMappings().getMapping().get(4);
        validateMapPropertyField(f4, "prop");

        Mapping f5 = (Mapping) mapping.getMappings().getMapping().get(5);
        validateMapLookupField(f5, "lookup");

        Mapping f6 = (Mapping) mapping.getMappings().getMapping().get(6);
        validateMapSimpleField(f6, "simple");

        Mapping f7 = (Mapping) mapping.getMappings().getMapping().get(7);
        validateMapConstantField(f7, "constant");

        Mapping f8 = (Mapping) mapping.getMappings().getMapping().get(8);
        validateManyToOneMapping(f8, "manyToOne");

        Mapping f9 = (Mapping) mapping.getMappings().getMapping().get(9);
        validateOneToManyMapping(f9, "oneToMany");

        Mapping f10 = (Mapping) mapping.getMappings().getMapping().get(10);
        validateConditionalMapping(f10, "conditional");

        validateProperties(mapping.getProperties());
    }

    protected void addMapField(AtlasMapping mapping, String key, boolean outputActions) {
        MockField inputMockField = new MockField();
        populateField(key + "-input-value", generateActions(), inputMockField, 3);
        inputMockField.setName(key + "-input");
        inputMockField.setCustom(key + "-input-custom");

        MockField outputMockField = new MockField();
        populateField(key + "-output-value", generateActions(), outputMockField, 3);
        outputMockField.setName(key + "-output");
        outputMockField.setCustom(key + "-output-custom");

        Mapping fm = AtlasModelFactory.createMapping(MappingType.MAP);
        fm.setMappingType(MappingType.MAP);
        fm.getInputField().add(inputMockField);
        fm.getOutputField().add(outputMockField);
        fm.setLookupTableName("lookupTableName");
        fm.setMappingType(MappingType.MAP);

        populateMapping(fm);

        mapping.getMappings().getMapping().add(fm);

        if (outputActions) {
            outputMockField.setActions(generateActions());
        }
    }

    protected void validateMapField(Mapping fm, String key, boolean outputActions) {
        assertNotNull(fm);
        assertEquals("lookupTableName", fm.getLookupTableName());
        assertEquals(MappingType.MAP, fm.getMappingType());

        validateMapping(fm);

        assertNotNull(fm.getInputField());
        Field f1 = fm.getInputField().get(0);
        assertTrue(f1 instanceof MockField);
        assertEquals(key + "-input", ((MockField) f1).getName());
        assertEquals(key + "-input-value", f1.getValue());
        validateField(key + "-input-value", f1, 3);

        assertNotNull(fm.getOutputField());
        Field f2 = fm.getOutputField().get(0);
        assertTrue(f2 instanceof MockField);
        assertEquals(key + "-output", ((MockField) f2).getName());
        assertEquals(key + "-output-value", f2.getValue());
        validateField(key + "-output-value", f2, 3);

        if (outputActions) {
            assertNotNull(f2.getActions());

            int i = 0;
            for (Action a : f2.getActions()) {
                if (a instanceof Camelize) {
                    i++;
                }
                if (a instanceof Capitalize) {
                    i++;
                }
                if (a instanceof Lowercase) {
                    i++;
                }
                if (a instanceof SeparateByDash) {
                    i++;
                }
                if (a instanceof SeparateByUnderscore) {
                    i++;
                }
                if (a instanceof Length) {
                    i++;
                }
                if (a instanceof Trim) {
                    i++;
                }
                if (a instanceof TrimLeft) {
                    i++;
                }
                if (a instanceof TrimRight) {
                    i++;
                }
                if (a instanceof Uppercase) {
                    i++;
                }
            }
            assertEquals(new Integer(1), new Integer(i));
        } else {
            assertNotNull(f2.getActions());
        }
    }

    protected void addMapLookupField(AtlasMapping mapping, String key) {

        LookupTable table = new LookupTable();
        table.setName(key + "-lookupTable");
        table.setDescription(key + "-lookupTableDescription");
        LookupEntry l1 = new LookupEntry();
        l1.setSourceType(FieldType.STRING);
        l1.setSourceValue("Foo");
        l1.setTargetType(FieldType.STRING);
        l1.setTargetValue("Bar");

        table.getLookupEntry().add(l1);
        mapping.getLookupTables().getLookupTable().add(table);

        ArrayList<Action> actions = generateActions();
        MockField inputField = generateMockField(key + "-input-value", actions);
        inputField.setName(key + "-input");
        inputField.setCustom("custom");

        MockField outputField = generateMockField(key + "-output-value", actions);
        outputField.setName(key + "-output");
        outputField.setCustom("custom");

        Mapping fm = AtlasModelFactory.createMapping(MappingType.LOOKUP);
        fm.setMappingType(MappingType.LOOKUP);
        fm.getInputField().add(inputField);
        fm.getOutputField().add(outputField);

        fm.setLookupTableName(key + "-lookupTable");
        populateMapping(fm);

        mapping.getMappings().getMapping().add(fm);
    }

    protected void validateMapLookupField(Mapping fm, String key) {
        assertNotNull(fm);
        validateMapping(fm);
        assertEquals(MappingType.LOOKUP, fm.getMappingType());

        assertNotNull(fm.getInputField());
        Field f1 = fm.getInputField().get(0);
        validateMockField(key + "-input", f1);

        assertNotNull(fm.getOutputField());
        Field f2 = fm.getOutputField().get(0);
        validateMockField(key + "-output", f2);

        assertEquals(key + "-lookupTable", fm.getLookupTableName());
    }

    private void validateMockField(String key, Field f1) {
        assertNotNull(f1.getActions());
        assertTrue(f1.getActions().get(0) instanceof Trim);
        assertTrue(f1 instanceof MockField);
        assertEquals(key, ((MockField) f1).getName());
        assertEquals("custom", ((MockField) f1).getCustom());
        validateField(key + "-value", f1, 3);
    }

    protected void addMapPropertyField(AtlasMapping mapping, String key) {
        ArrayList<Action> actions = new ArrayList<Action>();
        Action action = new Trim();
        actions.add(action);

        MockField inputField = new MockField();
        inputField.setName(key + "-input");
        inputField.setCustom("custom");
        populateField(key + "-input-value", actions, inputField, 3);

        PropertyField outputField = new PropertyField();
        outputField.setName(key + "-output");
        populateField(key + "-output-value", actions, outputField, 3);

        Mapping fm = AtlasModelFactory.createMapping(MappingType.MAP);
        fm.setMappingType(MappingType.MAP);
        fm.getInputField().add(inputField);
        fm.getOutputField().add(outputField);
        fm.setLookupTableName("lookupTableName");

        populateMapping(fm);

        mapping.getMappings().getMapping().add(fm);
    }

    private void populateMapping(Mapping fm) {
        fm.setAlias("alias");
        fm.setDelimiter(",");
        fm.setDescription("description");
        fm.setDelimiterString(",");
        fm.setId("id");
        fm.setStrategy("strategy");
        fm.setStrategyClassName("strategyClassName");
    }

    protected void validateMapPropertyField(Mapping fm, String key) {
        assertNotNull(fm);
        assertEquals(MappingType.MAP, fm.getMappingType());
        assertEquals("lookupTableName", fm.getLookupTableName());

        validateMapping(fm);

        assertNotNull(fm.getInputField());
        Field f1 = fm.getInputField().get(0);
        assertTrue(f1 instanceof MockField);
        assertEquals(key + "-input", ((MockField) f1).getName());
        assertEquals("custom", ((MockField) f1).getCustom());
        validateField(key + "-input-value", f1, 3);

        assertNotNull(fm.getOutputField());
        Field f2 = fm.getOutputField().get(0);
        assertTrue(f2 instanceof PropertyField);
        assertEquals(key + "-output", ((PropertyField) f2).getName());
        validateField(key + "-output-value", f2, 3);
    }

    protected void addCombineField(AtlasMapping mapping, String key) {
        Mapping fm = AtlasModelFactory.createMapping(MappingType.COMBINE);

        for (int i = 0; i < 3; i++) {
            MockField inputMockField = new MockField();

            populateField(key + "-input-" + i + "-value", generateActions(), inputMockField, i);
            inputMockField.setName(key + "-input-" + i);
            inputMockField.setCustom("custom");
            inputMockField.setCollectionType(CollectionType.values()[i]);
            inputMockField.setFieldType(FieldType.values()[i]);
            inputMockField.setStatus(FieldStatus.values()[i]);

            fm.getInputField().add(inputMockField);
        }

        MockField outputMockField = new MockField();
        outputMockField.setName(key + "-output");
        outputMockField.setCustom("custom");
        populateField(key + "-output-value", generateActions(), outputMockField, 0);

        fm.getOutputField().add(outputMockField);
        fm.setLookupTableName("lookupTableName");
        fm.setStrategy("strategy");

        populateMapping(fm);

        mapping.getMappings().getMapping().add(fm);
    }

    protected void validateCombineField(Mapping fm, String key) {
        assertNotNull(fm);
        assertEquals(MappingType.COMBINE, fm.getMappingType());
        assertEquals("lookupTableName", fm.getLookupTableName());

        validateMapping(fm);

        assertEquals(new Integer(3), new Integer(fm.getInputField().size()));
        assertNotNull(fm.getInputField());

        for (int i = 0; i < 3; i++) {
            Field in = fm.getInputField().get(i);
            assertTrue(in instanceof MockField);
            assertEquals(key + "-input-" + i, ((MockField) in).getName());
            assertEquals("custom", ((MockField) in).getCustom());
            assertEquals(CollectionType.values()[i], in.getCollectionType());
            assertEquals(FieldType.values()[i], in.getFieldType());
            assertEquals(FieldStatus.values()[i], in.getStatus());
            validateCommonFields(key + "-input-" + i + "-value", in, i);
        }

        assertNotNull(fm.getOutputField());
        Field o1 = fm.getOutputField().get(0);
        assertNotNull(o1.getActions());
        assertTrue(o1.getActions().get(0) instanceof Trim);
        assertTrue(o1 instanceof MockField);
        assertEquals(key + "-output", ((MockField) o1).getName());
        validateField(key + "-output-value", o1, 0);
    }

    private void populateField(String value, ArrayList<Action> actions, Field field, int n) {
        field.setValue(value);
        field.setArrayDimensions(n);
        field.setArraySize(n);
        field.setCollectionType(CollectionType.ARRAY);
        field.setDocId("docid");
        field.setPath("/path");
        field.setRequired(false);
        field.setStatus(FieldStatus.SUPPORTED);
        field.setActions(actions);
        field.setFieldType(FieldType.INTEGER);
        field.setIndex(n);
    }

    protected void addSeparateField(AtlasMapping mapping, String key) {
        Mapping fm = AtlasModelFactory.createMapping(MappingType.SEPARATE);

        MockField inputField = new MockField();
        populateField(key + "-input-value", generateActions(), inputField, 3);
        inputField.setName(key + "-input");
        inputField.setCustom("custom");

        fm.getInputField().add(inputField);

        for (int i = 0; i < 3; i++) {
            MockField outputField = new MockField();
            populateField(key + "-output-" + i + "-value", generateActions(), outputField, i);
            outputField.setName(key + "-output-" + i);
            outputField.setCustom("custom");
            fm.getOutputField().add(outputField);
        }

        fm.setLookupTableName("lookupTableName");
        populateMapping(fm);

        mapping.getMappings().getMapping().add(fm);
    }

    protected void validateSeparateField(Mapping fm, String key) {
        assertNotNull(fm);
        assertEquals(MappingType.SEPARATE, fm.getMappingType());
        assertEquals(("lookupTableName"), fm.getLookupTableName());
        validateMapping(fm);
        assertNotNull(fm.getOutputField());
        assertEquals(new Integer(3), new Integer(fm.getOutputField().size()));

        assertNotNull(fm.getInputField());
        Field o1 = fm.getInputField().get(0);
        assertTrue(o1 instanceof MockField);
        assertEquals(key + "-input", ((MockField) o1).getName());
        validateField(key + "-input-value", o1, 3);

        for (int i = 0; i < 3; i++) {
            Field in = fm.getOutputField().get(i);
            assertTrue(in instanceof MockField);
            assertEquals(key + "-output-" + i, ((MockField) in).getName());
            validateField(key + "-output-" + i + "-value", in, i);
        }
    }

    private void validateCommonFields(String value, Field f, int n) {
        assertNotNull(f.getActions());
        assertTrue(f.getActions().get(0) instanceof Trim);
        assertEquals(value, f.getValue());
        assertEquals("docid", f.getDocId());
        assertEquals(Integer.valueOf(n), f.getIndex());
        assertEquals(Integer.valueOf(n), f.getArrayDimensions());
        assertEquals(Integer.valueOf(n), f.getArraySize());
        assertEquals("/path", f.getPath());
        assertFalse(f.isRequired());
    }

    private void validateField(String value, Field f, int n) {
        assertEquals(FieldType.INTEGER, f.getFieldType());
        assertEquals(CollectionType.ARRAY, f.getCollectionType());
        assertEquals(FieldStatus.SUPPORTED, f.getStatus());

        validateCommonFields(value, f, n);
    }

    protected void addProperties(AtlasMapping mapping) {
        Properties props = new Properties();

        for (int i = 0; i < 8; i++) {
            Property p = new Property();
            p.setName("p" + i);

            switch (i) {
            case 0:
                p.setFieldType(FieldType.BOOLEAN);
                p.setValue(Boolean.toString(true));
                break;
            case 1:
                p.setFieldType(FieldType.CHAR);
                p.setValue("a");
                break;
            case 2:
                p.setFieldType(FieldType.DOUBLE);
                p.setValue(Double.toString(Double.MAX_VALUE));
                break;
            case 3:
                p.setFieldType(FieldType.FLOAT);
                p.setValue(Float.toString(Float.MAX_VALUE));
                break;
            case 4:
                p.setFieldType(FieldType.INTEGER);
                p.setValue(Integer.toString(Integer.MAX_VALUE));
                break;
            case 5:
                p.setFieldType(FieldType.LONG);
                p.setValue(Long.toString(Long.MAX_VALUE));
                break;
            case 6:
                p.setFieldType(FieldType.SHORT);
                p.setValue(Short.toString(Short.MAX_VALUE));
                break;
            case 7:
                p.setFieldType(FieldType.STRING);
                p.setValue(Integer.toString(i));
                break;
            default:
                throw new IllegalArgumentException("Uh-oh " + i);
            }

            props.getProperty().add(p);
        }
        mapping.setProperties(props);
    }

    protected void validateProperties(Properties props) {
        for (int i = 0; i < 8; i++) {
            Property p = props.getProperty().get(i);
            assertEquals("p" + i, p.getName());

            switch (i) {
            case 0:
                assertEquals(FieldType.BOOLEAN, p.getFieldType());
                assertEquals(Boolean.toString(true), p.getValue());
                break;
            case 1:
                assertEquals(FieldType.CHAR, p.getFieldType());
                assertEquals("a", p.getValue());
                break;
            case 2:
                assertEquals(FieldType.DOUBLE, p.getFieldType());
                assertEquals(Double.toString(Double.MAX_VALUE), p.getValue());
                break;
            case 3:
                assertEquals(FieldType.FLOAT, p.getFieldType());
                assertEquals(Float.toString(Float.MAX_VALUE), p.getValue());
                break;
            case 4:
                assertEquals(FieldType.INTEGER, p.getFieldType());
                assertEquals(Integer.toString(Integer.MAX_VALUE), p.getValue());
                break;
            case 5:
                assertEquals(FieldType.LONG, p.getFieldType());
                assertEquals(Long.toString(Long.MAX_VALUE), p.getValue());
                break;
            case 6:
                assertEquals(FieldType.SHORT, p.getFieldType());
                assertEquals(Short.toString(Short.MAX_VALUE), p.getValue());
                break;
            case 7:
                assertEquals(FieldType.STRING, p.getFieldType());
                assertEquals(Integer.toString(i), p.getValue());
                break;
            }
        }
    }

    protected void addMapSimpleField(AtlasMapping mapping, String key) {
        ArrayList<Action> actions = generateActions();
        SimpleField inputField = generateSimpleFidld(key + "-input-value", actions);
        inputField.setName(key + "-input");
        SimpleField outputField = generateSimpleFidld(key + "-output-value", actions);
        outputField.setName(key + "-output");
        Mapping fm = AtlasModelFactory.createMapping(MappingType.MAP);
        fm.setMappingType(MappingType.MAP);
        fm.getInputField().add(inputField);
        fm.getOutputField().add(outputField);
        fm.setLookupTableName(key + "-lookupTable");
        populateMapping(fm);
        mapping.getMappings().getMapping().add(fm);
    }

    private ArrayList<Action> generateActions() {
        ArrayList<Action> actions = new ArrayList<Action>();
        Action action = new Trim();
        actions.add(action);
        return actions;
    }

    protected void validateMapSimpleField(Mapping fm, String key) {
        assertNotNull(fm);
        assertEquals(MappingType.MAP, fm.getMappingType());

        validateMapping(fm);

        assertNotNull(fm.getInputField());
        Field f1 = fm.getInputField().get(0);
        validateSimpleField(key + "-input", f1);
        assertNotNull(fm.getOutputField());
        Field f2 = fm.getOutputField().get(0);
        validateSimpleField(key + "-output", f2);
    }

    private void validateMapping(Mapping fm) {
        assertEquals("alias", fm.getAlias());
        assertEquals(",", fm.getDelimiter());
        assertEquals(",", fm.getDelimiterString());
        assertEquals("description", fm.getDescription());
        assertEquals("id", fm.getId());
        assertEquals("strategy", fm.getStrategy());
        assertEquals("strategyClassName", fm.getStrategyClassName());
    }

    private void validateSimpleField(String key, Field f1) {
        assertTrue(f1 instanceof SimpleField);
        assertEquals(key, ((SimpleField) f1).getName());
        validateField(key + "-value", f1, 3);
    }

    protected void addMapConstantField(AtlasMapping mapping, String key) {
        ArrayList<Action> actions = generateActions();
        ConstantField inputField = generateConstantField(key + "-input-value", actions);
        ConstantField outputField = generateConstantField(key + "-output-value", actions);
        Mapping fm = AtlasModelFactory.createMapping(MappingType.MAP);
        fm.setMappingType(MappingType.MAP);
        fm.getInputField().add(inputField);
        fm.getOutputField().add(outputField);
        fm.setLookupTableName(key + "-lookupTable");
        populateMapping(fm);
        mapping.getMappings().getMapping().add(fm);
    }

    private ConstantField generateConstantField(String value, ArrayList<Action> actions) {
        ConstantField field = new ConstantField();
        populateField(value, actions, field, 3);
        return field;
    }

    private SimpleField generateSimpleFidld(String value, ArrayList<Action> actions) {
        SimpleField inputField = new SimpleField();
        populateField(value, actions, inputField, 3);
        return inputField;
    }

    private MockField generateMockField(String value, ArrayList<Action> actions) {
        MockField inputField = new MockField();
        populateField(value, actions, inputField, 3);
        return inputField;
    }

    protected void validateMapConstantField(Mapping fm, String key) {
        assertNotNull(fm);
        assertEquals(MappingType.MAP, fm.getMappingType());

        validateMapping(fm);

        assertNotNull(fm.getInputField());
        Field f1 = fm.getInputField().get(0);
        assertTrue(f1 instanceof ConstantField);
        validateField(key + "-input-value", f1, 3);
        assertNotNull(fm.getOutputField());
        Field f2 = fm.getOutputField().get(0);
        assertTrue(f2 instanceof ConstantField);
        validateField(key + "-output-value", f2, 3);
    }

    protected void addManyToOneMapping(AtlasMapping model, String key) {
        Mapping fm = new Mapping();
        FieldGroup fg = new FieldGroup();
        fg.setActions(new ArrayList<Action>());
        Concatenate c = new Concatenate();
        c.setDelimiter(",");
        fg.getActions().add(new Concatenate());
        fm.setInputFieldGroup(fg);

        for (int i = 0; i < 3; i++) {
            MockField sourceMockField = new MockField();

            populateField(key + "-input-" + i + "-value", generateActions(), sourceMockField, i);
            sourceMockField.setName(key + "-input-" + i);
            sourceMockField.setCustom("custom");
            sourceMockField.setCollectionType(CollectionType.values()[i]);
            sourceMockField.setFieldType(FieldType.values()[i]);
            sourceMockField.setStatus(FieldStatus.values()[i]);

            fm.getInputFieldGroup().getField().add(sourceMockField);
        }

        MockField targetMockField = new MockField();
        targetMockField.setName(key + "-output");
        targetMockField.setCustom("custom");
        populateField(key + "-output-value", generateActions(), targetMockField, 0);
        fm.getOutputField().add(targetMockField);

        populateMapping(fm);

        model.getMappings().getMapping().add(fm);
    }

    protected void validateManyToOneMapping(Mapping mapping, String key) {
        assertNotNull(mapping);

        validateMapping(mapping);

        assertNotNull(mapping.getInputFieldGroup());
        FieldGroup fg = mapping.getInputFieldGroup();
        assertEquals(3, fg.getField().size());
        assertNotNull(fg.getActions());
        assertEquals(1, fg.getActions().size());
        assertEquals(Concatenate.class, fg.getActions().get(0).getClass());

        for (int i = 0; i < 3; i++) {
            Field sf = mapping.getInputFieldGroup().getField().get(i);
            assertTrue(sf instanceof MockField);
            assertEquals(key + "-input-" + i, ((MockField) sf).getName());
            assertEquals("custom", ((MockField) sf).getCustom());
            assertEquals(CollectionType.values()[i], sf.getCollectionType());
            assertEquals(FieldType.values()[i], sf.getFieldType());
            assertEquals(FieldStatus.values()[i], sf.getStatus());
            validateCommonFields(key + "-input-" + i + "-value", sf, i);
        }

        assertNotNull(mapping.getOutputField());
        Field tf = mapping.getOutputField().get(0);
        assertNotNull(tf.getActions());
        assertTrue(tf.getActions().get(0) instanceof Trim);
        assertTrue(tf instanceof MockField);
        assertEquals(key + "-output", ((MockField) tf).getName());
        validateField(key + "-output-value", tf, 0);
    }

    protected void addOneToManyMapping(AtlasMapping model, String key) {
        Mapping fm = new Mapping();

        MockField sourceField = new MockField();
        populateField(key + "-input-value", generateActions(), sourceField, 3);
        sourceField.setName(key + "-input");
        sourceField.setCustom("custom");
        Split split = new Split();
        split.setDelimiter(",");
        sourceField.getActions().add(split);
        fm.getInputField().add(sourceField);

        for (int i = 0; i < 3; i++) {
            MockField targetField = new MockField();
            populateField(key + "-output-" + i + "-value", generateActions(), targetField, i);
            targetField.setName(key + "-output-" + i);
            targetField.setCustom("custom");
            fm.getOutputField().add(targetField);
        }

        populateMapping(fm);

        model.getMappings().getMapping().add(fm);
    }

    protected void validateOneToManyMapping(Mapping mapping, String key) {
        assertNotNull(mapping);
        validateMapping(mapping);
        assertNotNull(mapping.getOutputField());
        assertEquals(new Integer(3), new Integer(mapping.getOutputField().size()));

        assertNotNull(mapping.getInputField());
        Field sf = mapping.getInputField().get(0);
        assertTrue(sf instanceof MockField);
        assertEquals(key + "-input", ((MockField) sf).getName());
        validateField(key + "-input-value", sf, 3);
        assertNotNull(sf.getActions());
        assertEquals(2, sf.getActions().size());
        assertEquals(Split.class, sf.getActions().get(1).getClass());

        for (int i = 0; i < 3; i++) {
            Field in = mapping.getOutputField().get(i);
            assertTrue(in instanceof MockField);
            assertEquals(key + "-output-" + i, ((MockField) in).getName());
            validateField(key + "-output-" + i + "-value", in, i);
        }
    }

    protected void addConditionalMapping(AtlasMapping model, String key) {
        Mapping mapping = new Mapping();
        FormulaExpression fe = new FormulaExpression();
        fe.setExpression("=if(srcId:/path != \"\", srcId:/path, \":\")");
        mapping.setFormulaExpression(fe);

        MockField targetMockField = new MockField();
        targetMockField.setName(key + "-output");
        targetMockField.setCustom("custom");
        populateField(key + "-output-value", generateActions(), targetMockField, 0);
        mapping.getOutputField().add(targetMockField);

        populateMapping(mapping);

        model.getMappings().getMapping().add(mapping);
    }

    protected void validateConditionalMapping(Mapping mapping, String key) {
        assertNotNull(mapping);

        validateMapping(mapping);

        assertNotNull(mapping.getFormulaExpression());
        FormulaExpression fe = mapping.getFormulaExpression();
        assertEquals("=if(srcId:/path != \"\", srcId:/path, \":\")", fe.getExpression());

        assertNotNull(mapping.getOutputField());
        Field tf = mapping.getOutputField().get(0);
        assertNotNull(tf.getActions());
        assertTrue(tf.getActions().get(0) instanceof Trim);
        assertTrue(tf instanceof MockField);
        assertEquals(key + "-output", ((MockField) tf).getName());
        validateField(key + "-output-value", tf, 0);
    }

}
