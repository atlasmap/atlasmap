package io.atlasmap.core;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasContextFactory;
import io.atlasmap.api.AtlasException;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.v2.Audits;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.Mapping;

/**
 * Created by mmelko on 01/11/2017.
 */
public class CombineFromAtlasModuleTest {

    @Test
    public void combineNonStringFieldsTest() throws AtlasException {
        Mapping mapping = new Mapping();
        mapping.setDelimiter(";");
        Field outputField = generateField(FieldType.STRING, "", 1);
        BaseAtlasModule module = mockAtlasModule();
        AtlasSession session = mockAtlasSession();

        Assert.assertNotNull(session.getAtlasContext().getContextFactory().getCombineStrategy());
        module.processCombineField(session, mapping, generateCombineList(), outputField);
        Assert.assertEquals("1;2.0;3.0;true;6;string;9", outputField.getValue());
    }

    private List<Field> generateCombineList() {
        ArrayList<Field> fields = new ArrayList<>();

        fields.add(generateField(FieldType.INTEGER, 1, 1));
        fields.add(generateField(FieldType.DOUBLE, 2d, 2));
        fields.add(generateField(FieldType.FLOAT, 3f, 3));
        fields.add(generateField(FieldType.BOOLEAN, true, 4));
        fields.add(generateField(FieldType.NUMBER, 5, 5)); //  not listed as primitive type
        fields.add(generateField(FieldType.SHORT, (short) 6, 6));
        fields.add(generateField(FieldType.STRING, "string", 7));
        fields.add(generateField(FieldType.BYTE, Byte.MAX_VALUE, 8));
        fields.add(generateField(FieldType.CHAR,'9', 9));
        fields.add(generateField(FieldType.UNSIGNED_INTEGER, 5, 10));// not listed as primitive type

        return fields;
    }

    private Field generateField(FieldType type, Object value, int index) {
        Field field = new Field() {
            //epmty field
        };

        field.setFieldType(type);
        field.setValue(value);
        field.setDocId("io.atlasmap.testField");
        field.setIndex(index);
        field.setPath("/testPath");
        return field;
    }

    private BaseAtlasModule mockAtlasModule() {
        BaseAtlasModule module = spy(BaseAtlasModule.class);
        when(module.getConversionService()).thenReturn(DefaultAtlasConversionService.getInstance());
        return module;
    }

    private AtlasSession mockAtlasSession() throws AtlasException {
        DefaultAtlasSession session = mock(DefaultAtlasSession.class);
        AtlasContext context = mockAtlasContext();
        when(session.getAtlasContext()).thenReturn(context);
        when(session.getAtlasContext().getContextFactory().getCombineStrategy()).thenReturn(new DefaultAtlasCombineStrategy());
        when(session.getAudits()).thenReturn(new Audits());
        return session;
    }

    private AtlasContext mockAtlasContext() throws AtlasException {
        AtlasContext context = mock(AtlasContext.class);
        AtlasContextFactory atlasContextFactory = mockAtlasContextFactory();
        when(context.getContextFactory()).thenReturn(atlasContextFactory);
        return context;
    }

    private AtlasContextFactory mockAtlasContextFactory() throws AtlasException {
        AtlasContextFactory atlasContextFactory = mock(DefaultAtlasContextFactory.class);
        when(atlasContextFactory.getCombineStrategy()).thenReturn(new DefaultAtlasCombineStrategy());
        return atlasContextFactory;
    }
}
