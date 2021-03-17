package io.atlasmap.core;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Test;

import io.atlasmap.api.AtlasException;
import io.atlasmap.v2.Action;
import io.atlasmap.v2.Audits;
import io.atlasmap.v2.Concatenate;
import io.atlasmap.v2.Expression;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldGroup;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.Mapping;
import io.atlasmap.v2.MappingType;
import io.atlasmap.v2.SimpleField;
import io.atlasmap.v2.Split;
import io.atlasmap.v2.Uppercase;

public class DefaultAtlasPreviewContextTest extends BaseDefaultAtlasContextTest {

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
        previewContext.processPreview(m);
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
        previewContext.processPreview(m);
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
        previewContext.processPreview(m);
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
        previewContext.processPreview(m);
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
        previewContext.processPreview(m);
        assertEquals("1", target1.getValue());
        assertEquals("2", target2.getValue());
    }

    @Test
    public void testProcessPreviewSourceFieldGroup() throws AtlasException {
        Mapping m = new Mapping();
        Field source1 = new SimpleField();
        source1.setFieldType(FieldType.STRING);
        source1.setPath("/one");
        source1.setIndex(0);
        source1.setValue("one");
        Field source2 = new SimpleField();
        source2.setFieldType(FieldType.STRING);
        source2.setPath("/two");
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
        previewContext.processPreview(m);
        assertEquals("one and two", target.getValue());
    }

    @Test
    public void testProcessPreviewConcatenate() throws AtlasException {
        Mapping m = new Mapping();
        Field source1 = new SimpleField();
        source1.setFieldType(FieldType.STRING);
        source1.setPath("/one");
        source1.setIndex(1);
        source1.setValue("one");
        Field source2 = new SimpleField();
        source2.setFieldType(FieldType.STRING);
        source2.setPath("/two");
        source2.setIndex(3);
        source2.setValue("two");
        Field source3 = new SimpleField();
        source3.setFieldType(FieldType.STRING);
        source3.setPath("/six");
        source3.setIndex(5);
        source3.setValue("six");
        FieldGroup group = new FieldGroup();
        group.getField().add(source1);
        group.getField().add(source2);
        group.getField().add(source3);
        Concatenate action = new Concatenate();
        action.setDelimiter("-");
        action.setDelimitingEmptyValues(true);
        group.setActions(new ArrayList<>());
        group.getActions().add(action);
        m.setInputFieldGroup(group);
        Field target = new SimpleField();
        target.setFieldType(FieldType.STRING);
        m.getOutputField().add(target);
        previewContext.processPreview(m);
        assertEquals("-one--two--six", target.getValue());
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
        Audits audits = previewContext.processPreview(m);
        assertEquals(printAudit(audits), 0, audits.getAudit().size());
        assertEquals("one", target1.getValue());
        assertEquals("two", target2.getValue());
        assertEquals("four", target3.getValue());
    }

    @Test
    public void testProcessPreviewSourceCollection() throws AtlasException {
        Mapping m = new Mapping();
        Field source = new SimpleField();
        source.setFieldType(FieldType.STRING);
        source.setIndex(0);
        source.setPath("/this<>/is/collection");
        source.setValue("one");
        source.setActions(new ArrayList<>());
        Concatenate action = new Concatenate();
        action.setDelimiter(" ");
        source.getActions().add(action);
        m.getInputField().add(source);
        Field target = new SimpleField();
        target.setIndex(0);
        target.setFieldType(FieldType.STRING);
        m.getOutputField().add(target);
        Audits audits = previewContext.processPreview(m);
        assertEquals(printAudit(audits), 0, audits.getAudit().size());
        assertEquals("one", target.getValue());
    }

    @Test
    public void testProcessPreviewExpression() throws AtlasException {
        Mapping m = new Mapping();
        Field source = new SimpleField();
        source.setDocId("source");
        source.setFieldType(FieldType.DOUBLE);
        source.setPath("/sourceDouble");
        source.setValue(99.0);
        m.getInputField().add(source);
        Field source2 = new SimpleField();
        source2.setDocId("source2");
        source2.setFieldType(FieldType.DOUBLE);
        source2.setPath("/sourceDouble");
        source2.setValue(1.0);
        m.setExpression("${source:/sourceDouble} + ${source2:/sourceDouble}");
        m.getInputField().add(source2);
        Field target = new SimpleField();
        target.setFieldType(FieldType.STRING);
        m.getOutputField().add(target);
        Audits audits = previewContext.processPreview(m);
        assertEquals(printAudit(audits), 0, audits.getAudit().size());
        assertEquals("100.0", target.getValue());
    }

    @Test
    public void testProcessPreviewExpressionNullValue() throws AtlasException {
        Mapping m = new Mapping();
        FieldGroup fg = new FieldGroup();
        m.setInputFieldGroup(fg);
        Field source = new SimpleField();
        source.setDocId("source");
        source.setFieldType(FieldType.DOUBLE);
        source.setPath("/sourceDouble");
        fg.getField().add(source);
        Field source2 = new SimpleField();
        source2.setDocId("source2");
        source2.setFieldType(FieldType.DOUBLE);
        source2.setPath("/sourceDouble");
        source2.setValue(1.0);
        fg.getField().add(source2);
        m.setExpression("IF(ISEMPTY(${source:/sourceDouble}), ${source2:/sourceDouble}, ${source:/sourceDouble})");
        Field target = new SimpleField();
        target.setFieldType(FieldType.STRING);
        m.getOutputField().add(target);
        Audits audits = previewContext.processPreview(m);
        assertEquals(printAudit(audits), 0, audits.getAudit().size());
        assertEquals("1.0", target.getValue());
    }

    @Test
    public void testProcessPreviewFieldTypes() throws AtlasException {
        Mapping m = new Mapping();
        Field source = new Field() {private static final long serialVersionUID = 1L;};
        source.setFieldType(FieldType.STRING);
        source.setValue("foo");
        Field target = new Field() {private static final long serialVersionUID = 1L;};
        target.setFieldType(FieldType.STRING);
        m.getInputField().add(source);
        m.getOutputField().add(target);
        Audits audits = previewContext.processPreview(m);
        assertEquals(printAudit(audits), 0, audits.getAudit().size());
        assertEquals("foo", target.getValue());
    }

    @Test
    public void testProcessPreviewFilterSelect() throws Exception {
        Mapping m = new Mapping();
        FieldGroup fg = new FieldGroup();
        fg.setDocId("source");
        fg.setPath("/addressList<>");
        m.setInputFieldGroup(fg);
        FieldGroup fgc = new FieldGroup();
        fgc.setDocId("source");
        fgc.setPath("/addressList<0>");
        fg.getField().add(fgc);
        Field source = new SimpleField();
        source.setDocId("source");
        source.setFieldType(FieldType.STRING);
        source.setPath("/addressList<0>/city");
        source.setValue("Bolton");
        fgc.getField().add(source);
        Field source2 = new SimpleField();
        source2.setDocId("source");
        source2.setFieldType(FieldType.STRING);
        source2.setPath("/addressList<0>/state");
        source2.setValue("MA");
        fgc.getField().add(source2);
        m.setExpression("SELECT(FILTER(${source:/addressList<>}, ${/city} != 'Boston'), ${state})");
        Field target = new SimpleField();
        target.setFieldType(FieldType.STRING);
        m.getOutputField().add(target);
        Audits audits = previewContext.processPreview(m);
        assertEquals(printAudit(audits), 0, audits.getAudit().size());
        assertEquals("MA", target.getValue());
    }
}
