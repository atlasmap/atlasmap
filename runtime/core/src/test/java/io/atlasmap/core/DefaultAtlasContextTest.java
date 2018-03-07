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

import org.junit.Assert;
import org.junit.Test;

import io.atlasmap.api.AtlasException;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.core.AtlasMappingService.AtlasMappingFormat;
import io.atlasmap.spi.AtlasInternalSession;
import io.atlasmap.spi.AtlasInternalSession.Head;
import io.atlasmap.spi.AtlasModule;
import io.atlasmap.spi.StringDelimiter;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.AtlasModelFactory;
import io.atlasmap.v2.AuditStatus;
import io.atlasmap.v2.Audits;
import io.atlasmap.v2.BaseMapping;
import io.atlasmap.v2.Collection;
import io.atlasmap.v2.ConstantField;
import io.atlasmap.v2.DataSource;
import io.atlasmap.v2.DataSourceType;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.LookupEntry;
import io.atlasmap.v2.LookupTable;
import io.atlasmap.v2.Mapping;
import io.atlasmap.v2.MappingType;
import io.atlasmap.v2.Mappings;
import io.atlasmap.v2.Validations;

public class DefaultAtlasContextTest extends BaseDefaultAtlasContextTest {

    @Test
    public void testMap() throws AtlasException {
        Mapping m = (Mapping) AtlasModelFactory.createMapping(MappingType.MAP);
        mapping.getMappings().getMapping().add(m);
        populateSourceField(m, FieldType.STRING, "foo");
        prepareTargetField(m, "/target");
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
        context.process(session);
        assertFalse(printAudit(session), session.hasErrors());
        assertEquals(new Date(0).toInstant().toString() + ";1;2.0;3.0;true;5;6;string;8;9;10",
                writer.targets.get("/target"));
    }

    @Test
    public void testCombineNonSupportedObjects() throws AtlasException {
        Mapping m = (Mapping) AtlasModelFactory.createMapping(MappingType.COMBINE);
        mapping.getMappings().getMapping().add(m);
        m.setDelimiter(StringDelimiter.SEMICOLON.getName());
        populateUnsupportedSourceField(m, "foo", 0);
        populateUnsupportedSourceField(m, "bar", 1);
        prepareTargetField(m, "/target");
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
        context.process(session);

        Assert.assertFalse(printAudit(session), session.hasErrors());
        Assert.assertEquals(new Date(0).toString(), writer.targets.get("/target0"));
        Assert.assertEquals("1", writer.targets.get("/target1"));
        Assert.assertEquals("2.0", writer.targets.get("/target2"));
        Assert.assertEquals("3.0", writer.targets.get("/target3"));
        Assert.assertEquals("true", writer.targets.get("/target4"));
        Assert.assertEquals("5", writer.targets.get("/target5"));
        Assert.assertEquals("6", writer.targets.get("/target6"));
        Assert.assertEquals("string", writer.targets.get("/target7"));
        Assert.assertEquals("8", writer.targets.get("/target8"));
        Assert.assertEquals("9", writer.targets.get("/target9"));
        Assert.assertEquals("10", writer.targets.get("/target10"));
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
        context.process(session);
        Assert.assertFalse(printAudit(session), session.hasErrors());
        Assert.assertEquals("bar", writer.targets.get("/target"));
    }

    @Test
    public void testDefaultAtlasContext() throws AtlasException {
        File file = Paths.get(
                "src" + File.separator + "test" + File.separator + "resources" + File.separator + "atlasmapping.xml")
                .toFile();
        DefaultAtlasContextFactory factory = new DefaultAtlasContextFactory();
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
                "src" + File.separator + "test" + File.separator + "resources" + File.separator + "atlasmapping.xml")
                .toFile();
        DefaultAtlasContextFactory factory = new DefaultAtlasContextFactory();
        factory.init();

        DefaultAtlasContext context = new DefaultAtlasContext(factory, file.toURI());
        context.init();

        AtlasSession mockAtlasSession = mock(AtlasSession.class);

        context.processValidation(mockAtlasSession);
    }

    @Test(expected = AtlasException.class)
    public void testProcessValidationAtlasExceptionOtherContext() throws AtlasException {
        File file = Paths.get(
                "src" + File.separator + "test" + File.separator + "resources" + File.separator + "atlasmapping.xml")
                .toFile();
        DefaultAtlasContextFactory factory = new DefaultAtlasContextFactory();
        factory.init();

        DefaultAtlasContext context = new DefaultAtlasContext(factory, file.toURI());
        context.init();
        context.processValidation(session);
    }

    @Test
    public void testCreateSession() throws Exception {

        assertNotNull(context.createSession(mapping));

        File file = Paths.get(
                "src" + File.separator + "test" + File.separator + "resources" + File.separator + "atlasmapping.xml")
                .toFile();
        DefaultAtlasContext ctx = new DefaultAtlasContext(file.toURI());
        assertNotNull(ctx.createSession());
    }

    @Test // (expected = AtlasException.class)
    public void testInit() throws AtlasException {
        File file = Paths.get(
                "src" + File.separator + "test" + File.separator + "resources" + File.separator + "atlasmapping.json")
                .toFile();
        DefaultAtlasContext ctx = new DefaultAtlasContext(DefaultAtlasContextFactory.getInstance(), file.toURI(),
                AtlasMappingFormat.JSON);
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
        AtlasSession session = new DefaultAtlasSession(mapping);
        session.setAtlasContext(new DefaultAtlasContext(DefaultAtlasContextFactory.getInstance(), mapping));
        context.process(session);
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
        when(mockConstantModule.getCollectionSize(any(AtlasInternalSession.class), any(Field.class))).thenReturn(1);
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
        context.process(session);
    }

}
