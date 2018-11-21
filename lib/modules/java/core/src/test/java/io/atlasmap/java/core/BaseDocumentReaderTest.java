package io.atlasmap.java.core;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.LinkedList;
import java.util.List;

import org.junit.Before;

import io.atlasmap.api.AtlasException;
import io.atlasmap.core.DefaultAtlasConversionService;
import io.atlasmap.java.v2.JavaField;
import io.atlasmap.spi.AtlasInternalSession;
import io.atlasmap.spi.AtlasInternalSession.Head;
import io.atlasmap.v2.Audit;
import io.atlasmap.v2.Audits;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldType;

public abstract class BaseDocumentReaderTest {

    protected DocumentJavaFieldReader reader = null;
    protected List<Audit> audits = null;
    protected JavaField field = null;

    @Before
    public void reset() {
        audits = new LinkedList<>();
        reader = new DocumentJavaFieldReader();
        reader.setConversionService(DefaultAtlasConversionService.getInstance());
    }

    protected void read(Field field) throws AtlasException {
        AtlasInternalSession session = mock(AtlasInternalSession.class);
        when(session.head()).thenReturn(mock(Head.class));
        when(session.head().getSourceField()).thenReturn(field);
        when(session.getAudits()).thenReturn(mock(Audits.class));
        when(session.getAudits().getAudit()).thenReturn(this.audits);
        when(session.head().getAudits()).thenReturn(this.audits);
        reader.read(session);
    }

    protected Object read(String path, FieldType fieldType) throws AtlasException {
        this.field = createField(path, null, fieldType);
        read(field);
        return field.getValue();
    }

    protected JavaField createField(String path, Object value, FieldType fieldType) {
        JavaField field = new JavaField();
        field.setFieldType(fieldType);
        field.setValue(value);
        field.setPath(path);
        return field;
    }

}
