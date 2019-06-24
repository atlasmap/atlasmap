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
package io.atlasmap.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import io.atlasmap.api.AtlasConstants;
import io.atlasmap.api.AtlasException;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.spi.AtlasInternalSession.Head;
import io.atlasmap.spi.AtlasModule;
import io.atlasmap.spi.StringDelimiter;
import io.atlasmap.v2.*;

public class DefaultAtlasContextTest extends BaseDefaultAtlasContextTest {

    @Test
    public void testMap() throws AtlasException {
        Mapping m = (Mapping) AtlasModelFactory.createMapping(MappingType.MAP);
        mapping.getMappings().getMapping().add(m);
        recreateSession();
        populateSourceField(m, FieldType.STRING, "foo");
        prepareTargetField(m, "/target");
        recreateSession();
        context.process(session);
        assertFalse(printAudit(session), session.hasErrors());
        assertEquals("foo", writer.targets.get("/target"));
    }

    @Test
    public void testMapNotExistingDocId() throws AtlasException {
        Mapping m = (Mapping) AtlasModelFactory.createMapping(MappingType.MAP);
        mapping.getMappings().getMapping().add(m);
        populateSourceField(m, "docId.not.existing", FieldType.STRING, "foo");
        prepareTargetField(m, "/target");
        recreateSession();
        context.process(session);
        assertTrue(printAudit(session), session.hasErrors());
        assertEquals(1,
                session.getAudits().getAudit().stream().filter(a -> a.getStatus() == AuditStatus.ERROR).count());
    }

    @Test
    public void testCombineNonStringFields() throws AtlasException {
        Mapping m = (Mapping) AtlasModelFactory.createMapping(MappingType.COMBINE);
        mapping.getMappings().getMapping().add(m);
        m.setDelimiter(StringDelimiter.SEMICOLON.getName());
        populateSourceField(m, FieldType.DATE_TIME, new Date(0), 0);
        populateSourceField(m, FieldType.INTEGER, 1, 1);
        populateSourceField(m, FieldType.DOUBLE, 2d, 2);
        populateSourceField(m, FieldType.FLOAT, 3f, 3);
        populateSourceField(m, FieldType.BOOLEAN, true, 4);
        populateSourceField(m, FieldType.NUMBER, 5, 5); // not listed as primitive type
        populateSourceField(m, FieldType.SHORT, (short) 6, 6);
        populateSourceField(m, FieldType.STRING, "string", 7);
        populateSourceField(m, FieldType.BYTE, Byte.parseByte("8"), 8);
        populateSourceField(m, FieldType.CHAR, '9', 9);
        populateSourceField(m, FieldType.UNSIGNED_INTEGER, 10, 10);// not listed as primitive type
        prepareTargetField(m, "/target");
        recreateSession();
        context.process(session);
        assertFalse(printAudit(session), session.hasErrors());
        assertEquals(new Date(0).toInstant().toString() + ";1;2.0;3.0;true;5;6;string;8;9;10",
                writer.targets.get("/target"));
    }

    @Test
    public void testTemplateCombine() throws AtlasException {
        DefaultAtlasContextFactory.getInstance().setCombineStrategy(new TemplateCombineStrategy());
        Mapping mapping = (Mapping) AtlasModelFactory.createMapping(MappingType.COMBINE);
        this.mapping.getMappings().getMapping().add(mapping);
        populateSourceField(mapping, FieldType.STRING, "string", 0);
        populateSourceField(mapping, FieldType.INTEGER, 1, 1);
        populateSourceField(mapping, FieldType.DOUBLE, 2d, 2);
        prepareTargetField(mapping, "/target");
        mapping.setDelimiterString("String: {1}, Integer: {2}, Double: {3}, String again: {1}");
        recreateSession();
        context.process(session);
        assertFalse(printAudit(session), session.hasErrors());
        assertEquals("String: string, Integer: 1, Double: 2.0, String again: string", writer.targets.get("/target"));
    }

    @Test
    public void testCombineNonSupportedObjects() throws AtlasException {
        Mapping m = (Mapping) AtlasModelFactory.createMapping(MappingType.COMBINE);
        mapping.getMappings().getMapping().add(m);
        m.setDelimiter(StringDelimiter.SEMICOLON.getName());
        populateUnsupportedSourceField(m, "foo", 0);
        populateUnsupportedSourceField(m, "bar", 1);
        prepareTargetField(m, "/target");
        recreateSession();
        context.process(session);
        assertFalse(printAudit(session), session.hasErrors());
        assertEquals("foo;bar", writer.targets.get("/target"));
    }

    @Test
    public void testSeparate() throws Exception {
        Mapping m = (Mapping) AtlasModelFactory.createMapping(MappingType.SEPARATE);
        mapping.getMappings().getMapping().add(m);
        m.setDelimiter(StringDelimiter.SEMICOLON.getName());
        populateSourceField(m, FieldType.STRING, new Date(0).toString() + ";1;2.0;3.0;true;5;6;string;8;9;10");
        prepareTargetField(m, "/target1", 1);
        prepareTargetField(m, "/target0", 0);
        prepareTargetField(m, "/target10", 10);
        prepareTargetField(m, "/target9", 9);
        prepareTargetField(m, "/target8", 8);
        prepareTargetField(m, "/target7", 7);
        prepareTargetField(m, "/target6", 6);
        prepareTargetField(m, "/target5", 5);
        prepareTargetField(m, "/target4", 4);
        prepareTargetField(m, "/target3", 3);
        prepareTargetField(m, "/target2", 2);
        recreateSession();
        context.process(session);

        assertFalse(printAudit(session), session.hasErrors());
        assertEquals(new Date(0).toString(), writer.targets.get("/target0"));
        assertEquals("1", writer.targets.get("/target1"));
        assertEquals("2.0", writer.targets.get("/target2"));
        assertEquals("3.0", writer.targets.get("/target3"));
        assertEquals("true", writer.targets.get("/target4"));
        assertEquals("5", writer.targets.get("/target5"));
        assertEquals("6", writer.targets.get("/target6"));
        assertEquals("string", writer.targets.get("/target7"));
        assertEquals("8", writer.targets.get("/target8"));
        assertEquals("9", writer.targets.get("/target9"));
        assertEquals("10", writer.targets.get("/target10"));
    }

    @Test
    public void testLookupTable() throws Exception {
        LookupTable table = new LookupTable();
        table.setName("table");
        LookupEntry e = new LookupEntry();
        e.setSourceValue("foo");
        e.setTargetValue("bar");
        table.getLookupEntry().add(e);
        context.getLookupTables().put(table.getName(), table);
        Mapping m = (Mapping) AtlasModelFactory.createMapping(MappingType.LOOKUP);
        mapping.getMappings().getMapping().add(m);
        m.setLookupTableName("table");
        populateSourceField(m, FieldType.STRING, "foo");
        prepareTargetField(m, "/target");
        recreateSession();
        context.process(session);
        assertFalse(printAudit(session), session.hasErrors());
        assertEquals("bar", writer.targets.get("/target"));
    }

    @Test
    public void testDefaultAtlasContext() throws AtlasException {
        File file = Paths.get(
                "src" + File.separator + "test" + File.separator + "resources" + File.separator + "atlasmapping.json")
                .toFile();
        DefaultAtlasContextFactory factory = DefaultAtlasContextFactory.getInstance();
        factory.init();

        DefaultAtlasContext context = new DefaultAtlasContext(factory, file.toURI());
        context.init();
        assertNotNull(context);
        assertNotNull(context.getClassName());
        assertNotNull(context.getMapping());
        assertNotNull(context.getMappingName());
        assertNotNull(context.getMappingUri());
        assertNotNull(context.getThreadName());
        assertNull(context.getVersion());
        assertNotNull(context.toString());
        context.setLookupTables(null);
        context.setSourceModules(null);
        context.setTargetModules(null);
        context.setMappingUri(file.toURI());

        assertNotNull(new DefaultAtlasContext(file.toURI()));
    }

    @Test(expected = AtlasException.class)
    public void testProcessValidationAtlasException() throws AtlasException {
        File file = Paths.get(
                "src" + File.separator + "test" + File.separator + "resources" + File.separator + "atlasmapping.json")
                .toFile();
        DefaultAtlasContextFactory factory = DefaultAtlasContextFactory.getInstance();
        factory.init();

        DefaultAtlasContext context = new DefaultAtlasContext(factory, file.toURI());
        context.init();

        AtlasSession mockAtlasSession = mock(AtlasSession.class);

        context.processValidation(mockAtlasSession);
    }

    @Test(expected = AtlasException.class)
    public void testProcessValidationAtlasExceptionOtherContext() throws AtlasException {
        File file = Paths.get(
                "src" + File.separator + "test" + File.separator + "resources" + File.separator + "atlasmapping.json")
                .toFile();
        DefaultAtlasContextFactory factory = DefaultAtlasContextFactory.getInstance();
        factory.init();

        DefaultAtlasContext context = new DefaultAtlasContext(factory, file.toURI());
        context.init();
        context.processValidation(new DefaultAtlasSession(new DefaultAtlasContext(factory, file.toURI())));
    }

    @Test
    public void testCreateSession() throws Exception {

        assertNotNull(context.createSession(mapping));

        File file = Paths.get(
                "src" + File.separator + "test" + File.separator + "resources" + File.separator + "atlasmapping.json")
                .toFile();
        DefaultAtlasContext ctx = new DefaultAtlasContext(file.toURI());
        assertNotNull(ctx.createSession());
    }

    @Test // (expected = AtlasException.class)
    public void testInit() throws AtlasException {
        File file = Paths.get(
                "src" + File.separator + "test" + File.separator + "resources" + File.separator + "atlasmapping.json")
                .toFile();
        DefaultAtlasContext ctx = new DefaultAtlasContext(DefaultAtlasContextFactory.getInstance(), file.toURI());
        ctx.init();

        DataSource dataSource = new DataSource();
        dataSource.setUri("URI");
        mapping.getDataSource().add(dataSource);

        dataSource = new DataSource();
        dataSource.setUri(null);
        mapping.getDataSource().add(dataSource);

        dataSource = new DataSource();
        dataSource.setUri("java:source");
        dataSource.setDataSourceType(DataSourceType.SOURCE);
        dataSource.setId("io.atlasmap.core.DefaultAtlasContext.constants.docId");
        mapping.getDataSource().add(dataSource);

        dataSource = new DataSource();
        dataSource.setUri("java:target");
        dataSource.setDataSourceType(DataSourceType.TARGET);
        dataSource.setId("io.atlasmap.core.DefaultAtlasContext.constants.docId");
        mapping.getDataSource().add(dataSource);

        dataSource = new DataSource();
        dataSource.setUri("java:target");
        dataSource.setDataSourceType(DataSourceType.TARGET);
        dataSource.setId("io.atlasmap.core.DefaultAtlasContext.constants.docId");
        mapping.getDataSource().add(dataSource);

        ctx = new DefaultAtlasContext(DefaultAtlasContextFactory.getInstance(), mapping);
        ctx.getTargetModules().put("io.atlasmap.core.DefaultAtlasContext.constants.docId", new ConstantModule());
        ctx.init();

        @SuppressWarnings("unchecked")
        Map<String, AtlasModule> targetModules = spy(Map.class);
        when(targetModules.put(any(String.class), any(AtlasModule.class)))
                .thenThrow(new RuntimeException("mockException"));
        ctx.setTargetModules(targetModules);
        ctx.init();
    }

    @Test(expected = AtlasException.class)
    public void testProcessAtlasExceptionUnspported() throws AtlasException {
        AtlasSession session = spy(AtlasSession.class);
        context.process(session);
    }

    @Test(expected = AtlasException.class)
    public void testProcessAtlasExceptionOtherContext() throws AtlasException {
        DefaultAtlasContext context = new DefaultAtlasContext(DefaultAtlasContextFactory.getInstance(), mapping);
        AtlasSession session = new DefaultAtlasSession(context);
        new DefaultAtlasContext(DefaultAtlasContextFactory.getInstance(), mapping).process(session);
    }

    @Test
    public void testProcess() throws AtlasException {
        DefaultAtlasSession session = mock(DefaultAtlasSession.class);
        when(session.getAtlasContext()).thenReturn(context);

        Head head = mock(Head.class);
        when(session.head()).thenReturn(head);
        when(head.setMapping(any(Mapping.class))).thenReturn(head);
        when(head.setLookupTable(any(LookupTable.class))).thenReturn(head);
        Field headField = mock(ConstantField.class);
        when(head.getSourceField()).thenReturn(headField);
        Audits audits = mock(Audits.class);
        when(session.getAudits()).thenReturn(audits);
        Validations validations = mock(Validations.class);
        when(session.getValidations()).thenReturn(validations);
        AtlasMapping mapping = mock(AtlasMapping.class);
        when(session.getMapping()).thenReturn(mapping);

        when(session.hasErrors()).thenReturn(true);
        context.process(session);

        when(session.hasErrors()).thenReturn(false);
        Mappings mappings = mock(Mappings.class);
        when(mapping.getMappings()).thenReturn(mappings);

        List<BaseMapping> baseMappings = new ArrayList<>();
        Collection baseMapping = mock(Collection.class);
        when(baseMapping.getMappingType()).thenReturn(MappingType.COLLECTION);
        baseMappings.add(baseMapping);
        when(mappings.getMapping()).thenReturn(baseMappings);

        Mappings subMappings = mock(Mappings.class);
        when(baseMapping.getMappings()).thenReturn(subMappings);
        List<BaseMapping> baseMappingList = new ArrayList<>();
        Mapping mappingElement1 = mock(Mapping.class);

        List<Field> sourceFieldList = new ArrayList<>();
        ConstantField sourceField = mock(ConstantField.class);
        sourceFieldList.add(sourceField);
        when(sourceField.getPath()).thenReturn("contact.firstName");
        when(mappingElement1.getInputField()).thenReturn(sourceFieldList);

        List<Field> outputFieldList = new ArrayList<>();
        Field outputField = mock(Field.class);
        outputFieldList.add(outputField);
        when(outputField.getPath()).thenReturn("contact.firstName");
        when(mappingElement1.getOutputField()).thenReturn(outputFieldList);
        when(mappingElement1.getMappingType()).thenReturn(MappingType.ALL);

        baseMappingList.add(mappingElement1);
        when(subMappings.getMapping()).thenReturn(baseMappingList);

        Mapping mappingElement2 = mock(Mapping.class);
        when(mappingElement2.getMappingType()).thenReturn(MappingType.ALL);
        baseMappingList.add(mappingElement2);

        List<Field> sourceFieldList2 = new ArrayList<>();
        ConstantField sourceField2 = mock(ConstantField.class);
        sourceFieldList2.add(sourceField2);
        when(sourceField2.getPath()).thenReturn("contact[1]");

        when(mappingElement2.getInputField()).thenReturn(sourceFieldList2);
        ConstantModule mockConstantModule = mock(ConstantModule.class);
        ConstantField clonedField = mock(ConstantField.class);
        when(clonedField.getPath()).thenReturn("cloned[1]");
        when(mockConstantModule.cloneField(any(Field.class))).thenReturn(clonedField);

        List<Field> mockSourceFieldList = new ArrayList<>();
        ConstantField mockSourceField = mock(ConstantField.class);
        mockSourceFieldList.add(mockSourceField);
        when(mockSourceField.getPath()).thenReturn("source[1]");
        when(mappingElement2.getInputField()).thenReturn(mockSourceFieldList);

        List<Field> mockOutputFieldList = new ArrayList<>();
        ConstantField mockOutputField = mock(ConstantField.class);
        mockOutputFieldList.add(mockOutputField);
        when(mockOutputField.getPath()).thenReturn("output[1]");
        when(mappingElement2.getOutputField()).thenReturn(mockOutputFieldList);

        context.getSourceModules().put(DefaultAtlasContext.CONSTANTS_DOCUMENT_ID, mockConstantModule);
        context.getTargetModules().put(AtlasConstants.DEFAULT_TARGET_DOCUMENT_ID, mockConstantModule);
        context.process(session);
    }

    @Test
    public void testProcessPreviewConverter() throws AtlasException {
        Mapping m = new Mapping();
        m.setMappingType(MappingType.MAP);
        Field source = new SimpleField();
        source.setFieldType(FieldType.STRING);
        source.setValue("404");
        Field target = new SimpleField();
        target.setFieldType(FieldType.INTEGER);
        m.getInputField().add(source);
        m.getOutputField().add(target);
        context.processPreview(m);
        assertEquals(Integer.class, target.getValue().getClass());
        assertEquals(404, target.getValue());
    }

    @Test
    public void testProcessPreviewSourceFieldAction() throws AtlasException {
        Mapping m = new Mapping();
        m.setMappingType(MappingType.MAP);
        Field source = new SimpleField();
        source.setFieldType(FieldType.STRING);
        source.setValue("abc");
        ArrayList<Action> actions = new ArrayList<Action>();
        actions.add(new Uppercase());
        source.setActions(actions);
        Field target = new SimpleField();
        target.setFieldType(FieldType.STRING);
        m.getInputField().add(source);
        m.getOutputField().add(target);
        context.processPreview(m);
        assertEquals("ABC", target.getValue());
    }

    @Test
    public void testProcessPreviewTargetFieldAction() throws AtlasException {
        Mapping m = new Mapping();
        m.setMappingType(MappingType.MAP);
        Field source = new SimpleField();
        source.setFieldType(FieldType.STRING);
        source.setValue("abc");
        Field target = new SimpleField();
        target.setFieldType(FieldType.STRING);
        ArrayList<Action> actions = new ArrayList<Action>();
        actions.add(new Uppercase());
        target.setActions(actions);
        m.getInputField().add(source);
        m.getOutputField().add(target);
        context.processPreview(m);
        assertEquals("ABC", target.getValue());
    }

    @Test
    public void testProcessPreviewCombine() throws AtlasException {
        Mapping m = new Mapping();
        m.setMappingType(MappingType.COMBINE);
        Field source1 = new SimpleField();
        source1.setFieldType(FieldType.STRING);
        source1.setIndex(0);
        source1.setValue("1");
        Field source2 = new SimpleField();
        source2.setFieldType(FieldType.STRING);
        source2.setIndex(1);
        source2.setValue("2");
        Field target = new SimpleField();
        target.setFieldType(FieldType.STRING);
        m.getInputField().add(source1);
        m.getInputField().add(source2);
        m.getOutputField().add(target);
        context.processPreview(m);
        assertEquals("1 2", target.getValue());
    }

    @Test
    public void testProcessPreviewSeparate() throws AtlasException {
        Mapping m = new Mapping();
        m.setMappingType(MappingType.SEPARATE);
        Field source = new SimpleField();
        source.setFieldType(FieldType.STRING);
        source.setValue("1 2");
        Field target1 = new SimpleField();
        target1.setFieldType(FieldType.STRING);
        target1.setIndex(0);
        Field target2 = new SimpleField();
        target2.setFieldType(FieldType.STRING);
        target2.setIndex(1);
        m.getInputField().add(source);
        m.getOutputField().add(target1);
        m.getOutputField().add(target2);
        context.processPreview(m);
        assertEquals("1", target1.getValue());
        assertEquals("2", target2.getValue());
    }

    @Test
    public void testProcessPreviewSourceFieldGroup() throws AtlasException {
        Mapping m = new Mapping();
        Field source1 = new SimpleField();
        source1.setFieldType(FieldType.STRING);
        source1.setIndex(0);
        source1.setValue("one");
        Field source2 = new SimpleField();
        source2.setFieldType(FieldType.STRING);
        source2.setIndex(1);
        source2.setValue("two");
        FieldGroup group = new FieldGroup();
        group.getField().add(source1);
        group.getField().add(source2);
        Expression action = new Expression();
        action.setExpression("${0} + ' and ' + ${1}");
        group.setActions(new ArrayList<>());
        group.getActions().add(action);
        m.setInputFieldGroup(group);
        Field target = new SimpleField();
        target.setFieldType(FieldType.STRING);
        m.getOutputField().add(target);
        context.processPreview(m);
        assertEquals("one and two", target.getValue());
    }

    @Test
    public void testProcessPreviewConcatenate() throws AtlasException {
        Mapping m = new Mapping();
        Field source1 = new SimpleField();
        source1.setFieldType(FieldType.STRING);
        source1.setIndex(0);
        source1.setValue("one");
        Field source2 = new SimpleField();
        source2.setFieldType(FieldType.STRING);
        source2.setIndex(1);
        source2.setValue("two");
        Field source3 = new SimpleField();
        source3.setFieldType(FieldType.STRING);
        source3.setIndex(5);
        source3.setValue("six");
        FieldGroup group = new FieldGroup();
        group.getField().add(source1);
        group.getField().add(source2);
        group.getField().add(source3);
        Concatenate action = new Concatenate();
        action.setDelimiter("-");
        group.setActions(new ArrayList<>());
        group.getActions().add(action);
        m.setInputFieldGroup(group);
        Field target = new SimpleField();
        target.setFieldType(FieldType.STRING);
        m.getOutputField().add(target);
        context.processPreview(m);
        assertEquals("one-two----six", target.getValue());
    }

    @Test
    public void testProcessPreviewSplit() throws AtlasException {
        Mapping m = new Mapping();
        Field source = new SimpleField();
        source.setFieldType(FieldType.STRING);
        source.setValue("one two three four");
        source.setActions(new ArrayList<>());
        Split action = new Split();
        action.setDelimiter(" ");
        source.getActions().add(action);
        m.getInputField().add(source);
        Field target1 = new SimpleField();
        target1.setIndex(0);
        target1.setFieldType(FieldType.STRING);
        m.getOutputField().add(target1);
        Field target2 = new SimpleField();
        target2.setIndex(1);
        target2.setFieldType(FieldType.STRING);
        m.getOutputField().add(target2);
        Field target3 = new SimpleField();
        target3.setIndex(3);
        target3.setFieldType(FieldType.STRING);
        m.getOutputField().add(target3);
        Audits audits = context.processPreview(m);
        assertEquals(audits.toString(), 0, audits.getAudit().size());
        assertEquals("one", target1.getValue());
        assertEquals("two", target2.getValue());
        assertEquals("four", target3.getValue());
    }
}
