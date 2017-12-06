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
import java.util.Arrays;
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
        src.setUri("java:foo.bar");
        src.setDataSourceType(DataSourceType.SOURCE);

        DataSource tgt = new DataSource();
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
        return mapping;
    }

    protected void validateReferenceAtlasMapping(AtlasMapping mapping) {
        assertNotNull(mapping);
        assertNotNull(mapping.getName());
        assertEquals("junit", mapping.getName());
        assertNotNull(mapping.getMappings());
        assertEquals(new Integer(6), new Integer(mapping.getMappings().getMapping().size()));
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

        validateProperties(mapping.getProperties());
    }

    protected void addMapField(AtlasMapping mapping, String key, boolean outputActions) {
        MockField inputMockField = new MockField();
        inputMockField.setName(key + "-input");
        inputMockField.setValue(key + "-input-value");
        inputMockField.setFieldType(FieldType.STRING);

        MockField outputMockField = new MockField();
        outputMockField.setName(key + "-output");
        outputMockField.setValue(key + "-output-value");
        outputMockField.setFieldType(FieldType.STRING);

        Mapping fm = AtlasModelFactory.createMapping(MappingType.MAP);
        fm.setMappingType(MappingType.MAP);
        fm.getInputField().add(inputMockField);
        fm.getOutputField().add(outputMockField);

        mapping.getMappings().getMapping().add(fm);

        if (outputActions) {
            outputMockField.setActions(new Actions());
            outputMockField.getActions().getActions().add(new Uppercase());
            outputMockField.getActions().getActions().add(new Lowercase());
        }
    }

    protected void validateMapField(Mapping fm, String key, boolean outputActions) {
        assertNotNull(fm);
        assertNull(fm.getAlias());

        // assertTrue(m1.getFieldActions().getFieldAction().isEmpty());
        assertNotNull(fm.getInputField());
        Field f1 = fm.getInputField().get(0);
        assertNull(f1.getActions());

        assertTrue(f1 instanceof MockField);
        assertEquals(key + "-input", ((MockField) f1).getName());
        assertEquals(key + "-input-value", f1.getValue());
        assertEquals(FieldType.STRING, ((MockField) f1).getFieldType());

        // assertTrue(m2.getFieldActions().getFieldAction().isEmpty());
        assertNotNull(fm.getOutputField());
        Field f2 = fm.getOutputField().get(0);
        assertTrue(f2 instanceof MockField);
        assertEquals(key + "-output", ((MockField) f2).getName());
        assertEquals(key + "-output-value", f2.getValue());
        assertEquals(FieldType.STRING, ((MockField) f2).getFieldType());

        if (outputActions) {
            assertNotNull(f2.getActions());

            int i = 0;
            for (Action a : f2.getActions().getActions()) {
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
            assertEquals(new Integer(2), new Integer(i));
        } else {
            assertNull(f2.getActions());
        }
    }

    protected void addMapLookupField(AtlasMapping mapping, String key) {

        LookupTable table = new LookupTable();
        table.setName(key + "-lookupTable");

        LookupEntry l1 = new LookupEntry();
        l1.setSourceType(FieldType.STRING);
        l1.setSourceValue("Foo");
        l1.setTargetType(FieldType.STRING);
        l1.setTargetValue("Bar");

        table.getLookupEntry().add(l1);
        mapping.getLookupTables().getLookupTable().add(table);

        MockField inputField = new MockField();
        inputField.setName(key + "-input");
        inputField.setValue(key + "-input-value");

        MockField outputField = new MockField();
        outputField.setName(key + "-output");
        outputField.setValue(key + "-output-value");

        Mapping fm = AtlasModelFactory.createMapping(MappingType.LOOKUP);
        fm.setMappingType(MappingType.LOOKUP);
        fm.getInputField().add(inputField);
        fm.getOutputField().add(outputField);

        fm.setLookupTableName(key + "-lookupTable");
        mapping.getMappings().getMapping().add(fm);
    }

    protected void validateMapLookupField(Mapping fm, String key) {
        assertNotNull(fm);
        assertNull(fm.getAlias());
        assertEquals(MappingType.LOOKUP, fm.getMappingType());

        assertNotNull(fm.getInputField());
        Field f1 = fm.getInputField().get(0);
        assertNull(f1.getActions());

        assertTrue(f1 instanceof MockField);
        assertEquals(key + "-input", ((MockField) f1).getName());
        assertEquals(key + "-input-value", f1.getValue());
        assertNull(((MockField) f1).getFieldType());

        assertNotNull(fm.getOutputField());
        Field f2 = fm.getOutputField().get(0);
        assertTrue(f2 instanceof MockField);
        assertEquals(key + "-output", ((MockField) f2).getName());
        assertEquals(key + "-output-value", f2.getValue());
        assertNull(((MockField) f2).getFieldType());
        assertNull(f2.getActions());

        assertEquals(key + "-lookupTable", fm.getLookupTableName());
    }

    protected void addMapPropertyField(AtlasMapping mapping, String key) {
        MockField inputField = new MockField();
        inputField.setName(key + "-input");
        inputField.setValue(key + "-input-value");

        PropertyField outputField = new PropertyField();
        outputField.setName("p7");

        Mapping fm = AtlasModelFactory.createMapping(MappingType.MAP);
        fm.setMappingType(MappingType.MAP);
        fm.getInputField().add(inputField);
        fm.getOutputField().add(outputField);

        mapping.getMappings().getMapping().add(fm);

    }

    protected void validateMapPropertyField(Mapping fm, String key) {
        assertNotNull(fm);
        assertNull(fm.getAlias());

        // assertTrue(m1.getFieldActions().getFieldAction().isEmpty());
        assertNotNull(fm.getInputField());
        Field f1 = fm.getInputField().get(0);
        assertNull(f1.getActions());

        assertTrue(f1 instanceof MockField);
        assertEquals(key + "-input", ((MockField) f1).getName());
        assertEquals(key + "-input-value", f1.getValue());
        assertNull(((MockField) f1).getFieldType());

        // assertTrue(m2.getFieldActions().getFieldAction().isEmpty());
        assertNotNull(fm.getOutputField());
        Field f2 = fm.getOutputField().get(0);
        assertTrue(f2 instanceof PropertyField);
        assertEquals("p7", ((PropertyField) f2).getName());
        assertNull(f2.getValue());
        assertNull(((PropertyField) f2).getFieldType());
        assertNull(f2.getActions());
    }

    protected void addCombineField(AtlasMapping mapping, String key) {
        Mapping fm = AtlasModelFactory.createMapping(MappingType.COMBINE);

        for (int i = 0; i < 3; i++) {
            MockField inputMockField = new MockField();
            inputMockField.setName(key + "-input-" + i);
            inputMockField.setValue(key + "-input-" + i + "-value");
            inputMockField.setIndex(i);
            fm.getInputField().add(inputMockField);
        }

        MockField outputMockField = new MockField();
        outputMockField.setName(key + "-output");
        outputMockField.setValue(key + "-output-value");
        fm.getOutputField().add(outputMockField);

        fm.setDelimiterString(",");
        mapping.getMappings().getMapping().add(fm);
    }

    protected void validateCombineField(Mapping fm, String key) {
        assertNotNull(fm);
        assertNull(fm.getAlias());
        assertEquals(MappingType.COMBINE, fm.getMappingType());
        assertEquals(",", fm.getDelimiterString());

        assertEquals(new Integer(3), new Integer(fm.getInputField().size()));
        assertNotNull(fm.getInputField());

        for (int i = 0; i < 3; i++) {
            Field in = fm.getInputField().get(i);
            assertNull(in.getActions());
            assertTrue(in instanceof MockField);
            assertEquals(key + "-input-" + i, ((MockField) in).getName());
            assertEquals(key + "-input-" + i + "-value", in.getValue());
            assertEquals(new Integer(i), new Integer(in.getIndex()));
            assertNull(((MockField) in).getFieldType());
        }

        // assertTrue(m2.getFieldActions().getFieldAction().isEmpty());
        assertNotNull(fm.getOutputField());
        Field o1 = fm.getOutputField().get(0);
        assertNull(o1.getActions());
        assertTrue(o1 instanceof MockField);
        assertEquals(key + "-output", ((MockField) o1).getName());
        assertEquals(key + "-output-value", o1.getValue());
        assertNull(((MockField) o1).getFieldType());
    }

    protected void addSeparateField(AtlasMapping mapping, String key) {
        Mapping fm = AtlasModelFactory.createMapping(MappingType.SEPARATE);

        MockField inputField = new MockField();
        inputField.setName(key + "-input");
        inputField.setValue(key + "-input-value");
        fm.getInputField().add(inputField);

        for (int i = 0; i < 3; i++) {
            MockField outputField = new MockField();
            outputField.setName(key + "-output-" + i);
            outputField.setValue(key + "-output-" + i + "-value");
            outputField.setIndex(i);
            fm.getOutputField().add(outputField);
        }

        fm.setDelimiterString(",");
        mapping.getMappings().getMapping().add(fm);
    }

    protected void validateSeparateField(Mapping fm, String key) {
        assertNotNull(fm);
        assertNull(fm.getAlias());
        assertEquals(MappingType.SEPARATE, fm.getMappingType());
        assertEquals((","), fm.getDelimiterString());

        assertNotNull(fm.getOutputField());
        assertEquals(new Integer(3), new Integer(fm.getOutputField().size()));

        // assertTrue(m2.getFieldActions().getFieldAction().isEmpty());
        assertNotNull(fm.getInputField());
        Field o1 = fm.getInputField().get(0);
        assertNull(o1.getActions());
        assertTrue(o1 instanceof MockField);
        assertEquals(key + "-input", ((MockField) o1).getName());
        assertEquals(key + "-input-value", o1.getValue());
        assertNull(((MockField) o1).getFieldType());

        for (int i = 0; i < 3; i++) {
            Field in = fm.getOutputField().get(i);
            assertNull(in.getActions());
            assertTrue(in instanceof MockField);
            assertEquals(key + "-output-" + i, ((MockField) in).getName());
            assertEquals(key + "-output-" + i + "-value", in.getValue());
            assertEquals(new Integer(i), new Integer(in.getIndex()));
            assertNull(((MockField) in).getFieldType());
        }
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
            p.setName("p" + i);

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

    protected List<Action> generateReferenceFieldActions() {
        List<Action> actions = Arrays.asList(
                new AbsoluteValue(),
                new Add(),
                new Average(),
                new Camelize(),
                new Capitalize(),
                new Ceiling(),
                new Concatenate(),
                new ConvertAreaUnit(),
                new ConvertDistanceUnit(),
                new ConvertMassUnit(),
                new ConvertVolumeUnit(),
                new CurrentDate(),
                new CurrentDateTime(),
                new CurrentTime(),
                new CustomAction(),
                new Divide(),
                new EndsWith(),
                new FileExtension(),
                new Floor(),
                new Format(),
                new GenerateUUID(),
                new IndexOf(),
                new LastIndexOf(),
                new Length(),
                new Lowercase(),
                new Maximum(),
                new Minimum(),
                new Multiply(),
                new Normalize(),
                new PadStringLeft(),
                new PadStringRight(),
                new RemoveFileExtension(),
                new ReplaceAll(),
                new ReplaceFirst(),
                new Round(),
                new SeparateByDash(),
                new SeparateByUnderscore(),
                new StartsWith(),
                new SubString(),
                new SubStringAfter(),
                new SubStringBefore(),
                new Subtract(),
                new Trim(),
                new TrimLeft(),
                new TrimRight(),
                new Uppercase());
        return actions;
    }
}
