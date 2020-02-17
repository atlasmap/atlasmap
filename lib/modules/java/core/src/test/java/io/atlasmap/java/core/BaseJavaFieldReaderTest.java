package io.atlasmap.java.core;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import io.atlasmap.api.AtlasException;
import io.atlasmap.core.DefaultAtlasConversionService;
import io.atlasmap.java.v2.JavaField;
import io.atlasmap.spi.AtlasInternalSession;
import io.atlasmap.spi.AtlasInternalSession.Head;
import io.atlasmap.v2.Audit;
import io.atlasmap.v2.Audits;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldGroup;
import io.atlasmap.v2.FieldType;

public abstract class BaseJavaFieldReaderTest {

    protected JavaFieldReader reader = null;
    protected List<Audit> audits = null;
    protected JavaField field = null;
    protected FieldGroup fieldGroup = null;
    private Field sourceField = null;

    @Before
    public void reset() {
        audits = new LinkedList<>();
        reader = new JavaFieldReader();
        reader.setConversionService(DefaultAtlasConversionService.getInstance());
        field = null;
        fieldGroup = null;
        sourceField = null;
    }

    protected Field read(Field field) throws AtlasException {
        this.sourceField = field;
        AtlasInternalSession session = mock(AtlasInternalSession.class);
        Head head = mock(Head.class);
        when(session.head()).thenReturn(head);
        doAnswer(new Answer<Field>() {
            public Field answer(InvocationOnMock invocation) throws Throwable {
                return BaseJavaFieldReaderTest.this.sourceField;
            }
        }).when(head).getSourceField();
        doAnswer(new Answer<Head>() {
            public Head answer(InvocationOnMock invocation) throws Throwable {
                BaseJavaFieldReaderTest.this.sourceField = (Field)invocation.getArguments()[0];
                return (Head)invocation.getMock();
            }
        }).when(head).setSourceField(any());
        when(session.getAudits()).thenReturn(mock(Audits.class));
        when(session.getAudits().getAudit()).thenReturn(this.audits);
        when(head.getAudits()).thenReturn(this.audits);
        return reader.read(session);
    }

    protected Object read(String path, FieldType fieldType) throws AtlasException {
        this.field = createJavaField(path, null, fieldType);
        this.field = (JavaField) read(field);
        return field.getValue();
    }

    protected void readGroup(String path, FieldType fieldType) throws AtlasException {
        if (fieldType == FieldType.COMPLEX) {
            this.fieldGroup = createFieldGroup(path, fieldType);
        } else {
            this.field = createJavaField(path, null, fieldType);
        }
        this.fieldGroup = (FieldGroup) read(this.fieldGroup != null ? this.fieldGroup : this.field);
    }

    protected JavaField createJavaField(String path, Object value, FieldType fieldType) {
        JavaField field = new JavaField();
        field.setFieldType(fieldType);
        field.setValue(value);
        field.setPath(path);
        return field;
    }

    protected FieldGroup createFieldGroup(String path, FieldType fieldType) {
        FieldGroup group = new FieldGroup();
        group.setFieldType(fieldType);
        group.setPath(path);
        return group;
    }
}
