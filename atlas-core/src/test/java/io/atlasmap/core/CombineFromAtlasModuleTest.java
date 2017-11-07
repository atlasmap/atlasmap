package io.atlasmap.core;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
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
    private Mapping mapping;
    private AtlasSession session;
    private BaseAtlasModule module;

    @Before
    public void init() throws AtlasException {
        mapping = new Mapping();
        mapping.setDelimiter(";");
        module = mockAtlasModule();
        session = mockAtlasSession();
    }

    @Test
    public void combineNonStringFieldsTest() throws AtlasException {
        Field outputField = generateField(FieldType.STRING, "", 1);
        Assert.assertNotNull(session.getAtlasContext().getContextFactory().getCombineStrategy());
        module.processCombineField(session, mapping, generateCombineList(), outputField);
        Assert.assertEquals("1;2.0;3.0;true;5;6;string;8;9;10;" + new Date(0).toString(), outputField.getValue());
    }

    @Test public void combineNonSupportedObjects() throws AtlasException {
        Field outputField = generateField(FieldType.STRING, "", 1);
        Assert.assertNotNull(session.getAtlasContext().getContextFactory().getCombineStrategy());
        module.processCombineField(session, mapping, generateCombineListWithObjects(), outputField);
        Assert.assertEquals("foo;bar", outputField.getValue());
    }

    @After
    public void tearDown() {
        mapping = null;
        session = null;
        module = null;
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
        fields.add(generateField(FieldType.BYTE, Byte.parseByte("8"), 8));
        fields.add(generateField(FieldType.CHAR, '9', 9));
        fields.add(generateField(FieldType.UNSIGNED_INTEGER, 10, 10));// not listed as primitive type
        fields.add(generateField(FieldType.FLOAT.DATE_TIME, new Date(0), 11));

        return fields;
    }

    private List<Field> generateCombineListWithObjects() {
        ArrayList<Field> fields = new ArrayList<>();
        fields.add(generateObjectField("foo", 1));
        fields.add(generateObjectField("bar", 2));
        return fields;
    }

    private Field generateObjectField(String value, int index) {
        return generateField(FieldType.FLOAT.UNSUPPORTED, new Object() {
            @Override
            public String toString() {
                return value;
            }
        }, index);
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
