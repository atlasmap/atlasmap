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
package io.atlasmap.core;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import io.atlasmap.api.AtlasConstants;
import io.atlasmap.api.AtlasException;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.spi.AtlasFieldReader;
import io.atlasmap.spi.AtlasFieldWriter;
import io.atlasmap.spi.AtlasInternalSession;
import io.atlasmap.spi.AtlasModuleMode;
import io.atlasmap.v2.AtlasMapping;
import io.atlasmap.v2.AtlasModelFactory;
import io.atlasmap.v2.Audit;
import io.atlasmap.v2.Audits;
import io.atlasmap.v2.Constant;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldGroup;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.LookupEntry;
import io.atlasmap.v2.LookupTable;
import io.atlasmap.v2.Mapping;
import io.atlasmap.v2.Property;
import io.atlasmap.v2.SimpleField;

public abstract class BaseDefaultAtlasContextTest {
    protected DefaultAtlasContext context = null;
    protected DefaultAtlasPreviewContext previewContext = null;
    protected BaseAtlasModule sourceModule = null;
    protected BaseAtlasModule targetModule = null;
    protected AtlasMapping mapping = null;
    protected DefaultAtlasSession session = null;
    protected MockFieldReader reader = null;
    protected MockFieldWriter writer = null;

    @BeforeEach
    public void init() throws AtlasException {
        mapping = AtlasTestData.generateAtlasMapping();
        context = new DefaultAtlasContext(DefaultAtlasContextFactory.getInstance(), mapping) {
            protected void init() {
                // hijack initialization
            }
        };
        previewContext = new DefaultAtlasPreviewContext(DefaultAtlasContextFactory.getInstance());
        sourceModule = mockAtlasModule();
        sourceModule.setMode(AtlasModuleMode.SOURCE);
        targetModule = mockAtlasModule();
        targetModule.setMode(AtlasModuleMode.TARGET);
        context.getSourceModules().put(AtlasConstants.DEFAULT_SOURCE_DOCUMENT_ID, sourceModule);
        context.getTargetModules().put(AtlasConstants.DEFAULT_TARGET_DOCUMENT_ID, targetModule);
        ConstantModule constantModule = new ConstantModule();
        constantModule.setConversionService(DefaultAtlasConversionService.getInstance());
        context.getSourceModules().put(AtlasConstants.CONSTANTS_DOCUMENT_ID, constantModule);
        PropertyModule sourcePropertyModule = new PropertyModule(new DefaultAtlasPropertyStrategy());
        sourcePropertyModule.setConversionService(DefaultAtlasConversionService.getInstance());
        context.getSourceModules().put(AtlasConstants.PROPERTIES_SOURCE_DOCUMENT_ID,
                sourcePropertyModule);
        PropertyModule targetPropertyModule = new PropertyModule(new DefaultAtlasPropertyStrategy());
        targetPropertyModule.setConversionService(DefaultAtlasConversionService.getInstance());
        context.getTargetModules().put(AtlasConstants.PROPERTIES_TARGET_DOCUMENT_ID,
                targetPropertyModule);
        recreateSession();
    }

    protected void recreateSession() throws AtlasException {
        session = (DefaultAtlasSession) context.createSession();
        if (reader == null) {
            reader = new MockFieldReader();
        }
        session.setFieldReader(AtlasConstants.DEFAULT_SOURCE_DOCUMENT_ID, reader);
        if (writer == null) {
            writer = new MockFieldWriter();
        }
        session.setFieldWriter(AtlasConstants.DEFAULT_TARGET_DOCUMENT_ID, writer);
    }

    private BaseAtlasModule mockAtlasModule() throws AtlasException {
        BaseAtlasModule module = spy(BaseAtlasModule.class);
        when(module.getConversionService()).thenReturn(DefaultAtlasConversionService.getInstance());
        when(module.createField()).thenReturn(new SimpleField());
        when(module.isSupportedField(any(Field.class))).thenReturn(true);
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                AtlasInternalSession session = (AtlasInternalSession) invocation.getArguments()[0];
                reader.read(session);
                return null;
            }
        }).when(module).readSourceValue(any());
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                AtlasInternalSession session = (AtlasInternalSession) invocation.getArguments()[0];
                LookupTable table = session.head().getLookupTable();
                Field source = session.head().getSourceField();
                Field target = session.head().getTargetField();
                Object value = source.getValue();
                if (table != null) {
                    for (LookupEntry e : table.getLookupEntry()) {
                        if (value.equals(e.getSourceValue())) {
                            value = e.getTargetValue();
                        }
                    }
                }
                target.setValue(value);
                return null;
            }
        }).when(module).populateTargetField(any());
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                AtlasInternalSession session = (AtlasInternalSession) invocation.getArguments()[0];
                writer.write(session);
                return null;
            }
        }).when(module).writeTargetValue(any());
        return module;
    }

    protected Field populateSourceField(Mapping mapping, String docId, FieldType type, Object value, int index) {
        Field field = populateSourceField(mapping, type, value);
        field.setDocId(docId);
        field.setIndex(index);
        return field;
    }

    protected Field populateSourceField(Mapping mapping, FieldType type, Object value, int index) {
        Field field = populateSourceField(mapping, type, value);
        field.setIndex(index);
        return field;
    }

    protected Field populateSourceField(Mapping mapping, FieldType type, Object value) {
        Field field = new SimpleField();
        field.setFieldType(type);
        field.setPath("/testPath" + value);
        if (mapping != null) {
            mapping.getInputField().add(field);
        }
        reader.sources.put(field.getPath(), value);
        return field;
    }

    protected Field populateSourceField(Mapping mapping, String docId, FieldType type, Object value) {
        Field field = populateSourceField(mapping, type, value);
        field.setDocId(docId);
        return field;
    }

    protected Field populateSourceField(String docId, FieldType type, String name, Object value) {
        Field field = new SimpleField();
        field.setFieldType(type);
        field.setDocId(docId);
        field.setName(name);
        field.setPath("/" + name);
        reader.sources.put(field.getPath(), value);
        return field;
    }

    protected Field populateUnsupportedSourceField(Mapping mapping, String docId, String value, int index) {
        return populateSourceField(mapping, docId, FieldType.UNSUPPORTED, new Object() {
            @Override
            public String toString() {
                return value;
            }
        }, index);
    }

    protected FieldGroup populateCollectionSourceField(Mapping mapping, String docId, String seed) {
        String basePath = "/testPath" + seed;
        FieldGroup fieldGroup = new FieldGroup();
        fieldGroup.setFieldType(FieldType.STRING);
        fieldGroup.setDocId(docId);
        fieldGroup.setPath(basePath + "<>");
        if (mapping != null) {
            mapping.setInputFieldGroup(fieldGroup);
        }
        for (int i=0; i<10; i++) {
            Field child = new SimpleField();
            child.setFieldType(FieldType.STRING);
            child.setDocId(docId);
            child.setPath(basePath + "<" + i + ">");
            child.setValue(seed + i);
            child.setIndex(i);
            fieldGroup.getField().add(child);
            reader.sources.put(child.getPath(), child.getValue());
        }
        reader.sources.put(fieldGroup.getPath(), fieldGroup);
        return fieldGroup;
    }

    protected FieldGroup populateComplexCollectionSourceField(Mapping mapping, String docId, String seed) {
        String basePath = "/testPath" + seed;
        FieldGroup fieldGroup = new FieldGroup();
        fieldGroup.setFieldType(FieldType.COMPLEX);
        fieldGroup.setDocId(docId);
        fieldGroup.setPath(basePath + "<>");
        if (mapping != null) {
            mapping.setInputFieldGroup(fieldGroup);
        }
        List<Field> terminals = new ArrayList<>();
        for (int i=0; i<10; i++) {
            FieldGroup child = new FieldGroup();
            child.setFieldType(FieldType.COMPLEX);
            child.setDocId(docId);
            child.setPath(basePath + "<" + i + ">");
            child.setIndex(i);
            Field terminal = new SimpleField();
            terminal.setFieldType(FieldType.STRING);
            terminal.setDocId(docId);
            terminal.setPath(basePath + "<" + i + ">/value");
            terminal.setValue(seed + i);
            child.getField().add(terminal);
            reader.sources.put(terminal.getPath(), terminal.getValue());
            fieldGroup.getField().add(child);
            reader.sources.put(child.getPath(), child);
            reader.sources.put(terminal.getPath(), terminal);
            terminals.add(terminal);
        }
        reader.sources.put(fieldGroup.getPath(), fieldGroup);
        FieldGroup valueGroup = AtlasModelFactory.copyFieldGroup(fieldGroup);
        valueGroup.getField().addAll(terminals);
        valueGroup.setPath(fieldGroup.getPath() + "/value");
        reader.sources.put(valueGroup.getPath(), valueGroup);
        return fieldGroup;
    }

    protected Field populateUnsupportedSourceField(Mapping mapping, String value, int index) {
        return populateUnsupportedSourceField(mapping, null, value, index);
    }

    protected void populateConstant(String name, String value) {
        Constant c = new Constant();
        c.setName(name);
        c.setValue(value);
        session.getMapping().getConstants().getConstant().add(c);
    }

    protected void populateProperty(String scope, String name, String value) {
        Property p = new Property();
        p.setScope(scope);
        p.setName(name);
        p.setValue(value);
        session.getMapping().getProperties().getProperty().add(p);
    }
    protected Field prepareTargetField(Mapping mapping, String path) {
        Field field = new SimpleField();
        field.setPath(path);
        mapping.getOutputField().add(field);
        return field;
    }

    protected Field prepareTargetField(Mapping mapping, String path, int index) {
        Field field = prepareTargetField(mapping, path);
        field.setIndex(index);
        return field;
    }

    protected Field prepareTargetField(Mapping mapping, FieldType type, String path, int index) {
        Field field = prepareTargetField(mapping, path);
        field.setFieldType(type);
        field.setPath(path);
        field.setIndex(index);
        return field;
    }

    protected Object getTargetFieldValue(String path) {
        return writer.targets.get(path);
    }

    protected class MockFieldReader implements AtlasFieldReader {
        protected Map<String, Object> sources = new HashMap<>();
        @Override
        public Field read(AtlasInternalSession session) throws AtlasException {
            Field field = session.head().getSourceField();
            Object value = sources.get(field.getPath());
            if (value instanceof Field) {
                session.head().setSourceField((Field)value);
                return (Field)value;
            }
            field.setValue(value);
            return field;
        }
    }

    protected class MockFieldWriter implements AtlasFieldWriter {
        protected Map<String, Object> targets = new HashMap<>();
        @Override
        public void write(AtlasInternalSession session) throws AtlasException {
            targets.put(session.head().getTargetField().getPath(),
                    session.head().getTargetField().getValue());
        }
    }

    protected String printAudit(Audits audits) {
        StringBuilder buf = new StringBuilder("Audits: ");
        for (Audit a : audits.getAudit()) {
            buf.append('[');
            buf.append(a.getStatus());
            buf.append(", message=");
            buf.append(a.getMessage());
            buf.append("], ");
        }
        return buf.toString();
    }

    protected String printAudit(AtlasSession session) {
        return printAudit(session.getAudits());
    }
}
