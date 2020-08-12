package io.atlasmap.core;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
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
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldGroup;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.LookupEntry;
import io.atlasmap.v2.LookupTable;
import io.atlasmap.v2.Mapping;
import io.atlasmap.v2.SimpleField;

public abstract class BaseDefaultAtlasContextTest {
    protected DefaultAtlasContext context = null;
    protected BaseAtlasModule sourceModule = null;
    protected BaseAtlasModule targetModule = null;
    protected AtlasMapping mapping = null;
    protected DefaultAtlasSession session = null;
    protected MockFieldReader reader = null;
    protected MockFieldWriter writer = null;

    @Before
    public void init() throws AtlasException {
        mapping = AtlasTestData.generateAtlasMapping();
        context = new DefaultAtlasContext(DefaultAtlasContextFactory.getInstance(), mapping) {
            protected void init() {
                // hijack initialization
            }
        };
        sourceModule = mockAtlasModule();
        sourceModule.setMode(AtlasModuleMode.SOURCE);
        targetModule = mockAtlasModule();
        targetModule.setMode(AtlasModuleMode.TARGET);
        context.getSourceModules().put(AtlasConstants.DEFAULT_SOURCE_DOCUMENT_ID, sourceModule);
        context.getTargetModules().put(AtlasConstants.DEFAULT_TARGET_DOCUMENT_ID, targetModule);
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
        module.setConversionService(DefaultAtlasConversionService.getInstance());
        when(module.isSupportedField(any(Field.class))).thenReturn(true);
        doAnswer(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                AtlasInternalSession session = (AtlasInternalSession) invocation.getArguments()[0];
                Field field = session.head().getSourceField();
                field.setValue(reader.sources.get(field.getPath()));
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
		@Override
		public Field readField(AtlasInternalSession session, String fieldPath) throws AtlasException {
			// TODO Auto-generated method stub
			return null;
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
