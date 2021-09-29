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

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;

import org.junit.jupiter.api.Test;

import io.atlasmap.api.AtlasConstants;
import io.atlasmap.api.AtlasException;
import io.atlasmap.v2.Action;
import io.atlasmap.v2.Audits;
import io.atlasmap.v2.Capitalize;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.Concatenate;
import io.atlasmap.v2.ConstantField;
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
        target = m.getOutputField().get(0);
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
        target = m.getOutputField().get(0);
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
        target = m.getOutputField().get(0);
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
        target = m.getOutputField().get(0);
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
        target1 = m.getOutputField().get(0);
        target2 = m.getOutputField().get(1);
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
        target = m.getOutputField().get(0);
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
        source2.setPath("/three");
        source2.setIndex(3);
        source2.setValue("three");
        Field source3 = new SimpleField();
        source3.setFieldType(FieldType.STRING);
        source3.setPath("/five");
        source3.setIndex(5);
        source3.setValue("five");
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
        target = m.getOutputField().get(0);
        assertEquals("-one--three--five", target.getValue());
    }

    @Test
    public void testProcessActionConcatenateCollectionAndNonCollection() throws Exception {
        Mapping m = new Mapping();
        Field source1 = new SimpleField();
        source1.setFieldType(FieldType.STRING);
        source1.setPath("/list<1>");
        source1.setIndex(1);
        source1.setValue("one");
        Field source2 = new SimpleField();
        source2.setFieldType(FieldType.STRING);
        source2.setPath("/list<3>");
        source2.setIndex(3);
        source2.setValue("three");
        Field source3 = new SimpleField();
        source3.setFieldType(FieldType.STRING);
        source3.setPath("/list<5>");
        source3.setIndex(5);
        source3.setValue("five");
        FieldGroup list = new FieldGroup();
        list.setCollectionType(CollectionType.LIST);
        list.setIndex(0);
        list.setPath("/list<>");
        list.getField().add(source1);
        list.getField().add(source2);
        list.getField().add(source3);
        Capitalize capitalize = new Capitalize();
        list.setActions(new ArrayList<>());
        list.getActions().add(capitalize);
        Field f = new SimpleField();
        f.setFieldType(FieldType.STRING);
        f.setIndex(1);
        f.setPath("/nc");
        f.setValue("nc");
        FieldGroup group = new FieldGroup();
        group.getField().add(list);
        group.getField().add(f);
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
        target = m.getOutputField().get(0);
        assertEquals("-One--Three--Five-nc", target.getValue());

    }

    @Test
    public void testProcessPreviewSplit() throws AtlasException {
        Mapping m = new Mapping();
        Field source = new SimpleField();
        source.setFieldType(FieldType.STRING);
        source.setPath("/source");
        source.setValue("one two three four");
        source.setActions(new ArrayList<>());
        Split action = new Split();
        action.setDelimiter(" ");
        source.getActions().add(action);
        m.getInputField().add(source);
        Field target1 = new SimpleField();
        target1.setPath("/target1");
        target1.setIndex(0);
        target1.setFieldType(FieldType.STRING);
        m.getOutputField().add(target1);
        Field target2 = new SimpleField();
        target2.setPath("/target2");
        target2.setIndex(1);
        target2.setFieldType(FieldType.STRING);
        m.getOutputField().add(target2);
        Field target3 = new SimpleField();
        target3.setPath("/target3");
        target3.setIndex(3);
        target3.setFieldType(FieldType.STRING);
        m.getOutputField().add(target3);
        Audits audits = previewContext.processPreview(m);
        assertEquals(0, audits.getAudit().size(), printAudit(audits));
        target1 = m.getOutputField().get(0);
        target2 = m.getOutputField().get(1);
        target3 = m.getOutputField().get(2);
        assertEquals("one", target1.getValue());
        assertEquals("two", target2.getValue());
        assertEquals("four", target3.getValue());
    }

    @Test
    public void testProcessPreviewSplitCollection() throws AtlasException {
        Mapping m = new Mapping();
        Field source = new SimpleField();
        source.setFieldType(FieldType.STRING);
        source.setPath("/source");
        source.setValue("one two three four");
        source.setActions(new ArrayList<>());
        Split action = new Split();
        action.setDelimiter(" ");
        source.getActions().add(action);
        m.getInputField().add(source);
        Field target = new SimpleField();
        target.setCollectionType(CollectionType.LIST);
        target.setFieldType(FieldType.STRING);
        target.setPath("/results<>");
        m.getOutputField().add(target);
        Audits audits = previewContext.processPreview(m);
        assertEquals(0, audits.getAudit().size(), printAudit(audits));
        target = m.getOutputField().get(0);
        assertEquals(FieldGroup.class, target.getClass());
        FieldGroup targetGroup = (FieldGroup)target;
        Field one = targetGroup.getField().get(0);
        Field two = targetGroup.getField().get(1);
        Field three = targetGroup.getField().get(2);
        Field four = targetGroup.getField().get(3);
        assertEquals("/results<0>", one.getPath());
        assertEquals("one", one.getValue());
        assertEquals("/results<1>", two.getPath());
        assertEquals("two", two.getValue());
        assertEquals("/results<2>", three.getPath());
        assertEquals("three", three.getValue());
        assertEquals("/results<3>", four.getPath());
        assertEquals("four", four.getValue());

        target = new SimpleField();
        target.setCollectionType(CollectionType.NONE);
        target.setPath("/collection<>/result");
        target.setFieldType(FieldType.STRING);
        target.setIndex(0);
        m.getOutputField().clear();
        m.getOutputField().add(target);
        audits = previewContext.processPreview(m);
        assertEquals(0, audits.getAudit().size(), printAudit(audits));
        target = m.getOutputField().get(0);
        assertEquals(FieldGroup.class, target.getClass());
        targetGroup = (FieldGroup)target;
        one = targetGroup.getField().get(0);
        two = targetGroup.getField().get(1);
        three = targetGroup.getField().get(2);
        four = targetGroup.getField().get(3);
        assertEquals("/collection<0>/result", one.getPath());
        assertEquals("one", one.getValue());
        assertEquals("/collection<1>/result", two.getPath());
        assertEquals("two", two.getValue());
        assertEquals("/collection<2>/result", three.getPath());
        assertEquals("three", three.getValue());
        assertEquals("/collection<3>/result", four.getPath());
        assertEquals("four", four.getValue());
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
        assertEquals(0, audits.getAudit().size(), printAudit(audits));
        target = m.getOutputField().get(0);
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
        assertEquals(0, audits.getAudit().size(), printAudit(audits));
        target = m.getOutputField().get(0);
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
        assertEquals(0, audits.getAudit().size(), printAudit(audits));
        target = m.getOutputField().get(0);
        assertEquals("1.0", target.getValue());
    }

    @Test
    public void testProcessPreviewFieldTypes() throws AtlasException {
        Mapping m = new Mapping();
        Field source = new SimpleField();
        source.setFieldType(FieldType.STRING);
        source.setValue("foo");
        Field target = new SimpleField();
        target.setFieldType(FieldType.STRING);
        m.getInputField().add(source);
        m.getOutputField().add(target);
        Audits audits = previewContext.processPreview(m);
        assertEquals(0, audits.getAudit().size(), printAudit(audits));
        target = m.getOutputField().get(0);
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
        assertEquals(0, audits.getAudit().size(), printAudit(audits));
        target = m.getOutputField().get(0);
        assertEquals("MA", target.getValue());
    }

    @Test
    public void testProcessPreviewRepeatCount() throws Exception {
        Mapping m = new Mapping();
        FieldGroup fg = new FieldGroup();
        m.setInputFieldGroup(fg);
        FieldGroup fgc = new FieldGroup();
        fgc.setDocId("source");
        fgc.setFieldType(FieldType.STRING);
        fgc.setCollectionType(CollectionType.LIST);
        fgc.setName("city");
        fgc.setPath("/addressList<>/city");
        Field source = new SimpleField();
        source.setDocId("source");
        source.setFieldType(FieldType.STRING);
        source.setPath("/addressList<0>/city");
        source.setName("city");
        source.setValue("Bolton");
        fgc.getField().add(source);
        fg.getField().add(fgc);
        Field source2 = new ConstantField();
        source2.setDocId(AtlasConstants.CONSTANTS_DOCUMENT_ID);
        source2.setFieldType(FieldType.STRING);
        source2.setPath("/test");
        source2.setName("test");
        source2.setValue("testVal");
        fg.getField().add(source2);
        m.setExpression(String.format(
                "REPEAT(COUNT(${source:/addressList<>/city}), ${%s:/test})",
                AtlasConstants.CONSTANTS_DOCUMENT_ID));
        Field target = new SimpleField();
        target.setFieldType(FieldType.STRING);
        target.setDocId("target");
        target.setPath("/addressList<>/city");
        m.getOutputField().add(target);
        Audits audits = previewContext.processPreview(m);
        assertEquals(0, audits.getAudit().size(), printAudit(audits));
        FieldGroup targetGroup = (FieldGroup) m.getOutputField().get(0);
        assertEquals("/addressList<>/city", targetGroup.getPath());
        assertEquals(1, targetGroup.getField().size());
        assertEquals("testVal", targetGroup.getField().get(0).getValue());
        
    }

    @Test
    public void testProcessPreviewExpressionBoolean() throws AtlasException {
        Mapping m = new Mapping();
        Field source = new SimpleField();
        source.setDocId("source");
        source.setFieldType(FieldType.BOOLEAN);
        source.setPath("/sourceBoolean");
        source.setValue("true");
        m.getInputField().add(source);
        m.setExpression("IF(${source:/sourceBoolean}, 'YES', 'NO')");
        Field target = new SimpleField();
        target.setFieldType(FieldType.STRING);
        m.getOutputField().add(target);
        Audits audits = previewContext.processPreview(m);
        assertEquals(0, audits.getAudit().size(), printAudit(audits));
        target = m.getOutputField().get(0);
        assertEquals("YES", target.getValue());

        m = new Mapping();
        source = new SimpleField();
        source.setDocId("source");
        source.setFieldType(FieldType.BOOLEAN);
        source.setPath("/sourceBoolean");
        source.setValue("true");
        m.getInputField().add(source);
        m.setExpression("IF (${source:/sourceBoolean} == true, 'YES', 'NO')");
        target = new SimpleField();
        target.setFieldType(FieldType.STRING);
        m.getOutputField().add(target);
        audits = previewContext.processPreview(m);
        assertEquals(0, audits.getAudit().size(), printAudit(audits));
        target = m.getOutputField().get(0);
        assertEquals("YES", target.getValue());
    }

}
