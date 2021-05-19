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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

import io.atlasmap.api.AtlasConstants;
import io.atlasmap.api.AtlasException;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldGroup;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.PropertyField;
import io.atlasmap.v2.SimpleField;

public class DefaultAtlasExpressionProcessorTest extends BaseDefaultAtlasContextTest {

    @Test
    public void testSingle() throws AtlasException {
        Field source = populateSourceField(null, AtlasConstants.DEFAULT_SOURCE_DOCUMENT_ID, FieldType.STRING, "foo");
        String expression = String.format("${%s:/testPathfoo}", AtlasConstants.DEFAULT_SOURCE_DOCUMENT_ID);
        recreateSession();
        session.head().setSourceField(source);
        DefaultAtlasExpressionProcessor.processExpression(session, expression);
        assertFalse(printAudit(session), session.hasErrors());
        assertEquals("foo", session.head().getSourceField().getValue());
    }

    @Test
    public void testCollection() throws Exception {
        FieldGroup source = populateCollectionSourceField(null, AtlasConstants.DEFAULT_SOURCE_DOCUMENT_ID, "foo");
        String expression = String.format(
            "IF(ISEMPTY(${%s:/testPathfoo<0>}), null, ${%s:/testPathfoo<>})",
            AtlasConstants.DEFAULT_SOURCE_DOCUMENT_ID, AtlasConstants.DEFAULT_SOURCE_DOCUMENT_ID);
        recreateSession();
        FieldGroup wrapper = new FieldGroup();
        wrapper.getField().add(source);
        wrapper.getField().add(source.getField().get(0));
        session.head().setSourceField(wrapper);
        DefaultAtlasExpressionProcessor.processExpression(session, expression);
        assertFalse(printAudit(session), session.hasErrors());
        assertEquals(FieldGroup.class, session.head().getSourceField().getClass());
        FieldGroup fieldGroup = (FieldGroup) session.head().getSourceField();
        assertEquals("/testPathfoo<>", fieldGroup.getPath());
        assertEquals(10, fieldGroup.getField().size());
        assertEquals("foo0", fieldGroup.getField().get(0).getValue());
    }

    @Test
    public void testComplexCollection() throws Exception {
        populateComplexCollectionSourceField(null, AtlasConstants.DEFAULT_SOURCE_DOCUMENT_ID, "foo");
        String expression = String.format(
            "IF(ISEMPTY(${%s:/testPathfoo<0>/value}), null, ${%s:/testPathfoo<>/value})",
            AtlasConstants.DEFAULT_SOURCE_DOCUMENT_ID, AtlasConstants.DEFAULT_SOURCE_DOCUMENT_ID);
        recreateSession();
        FieldGroup wrapper = new FieldGroup();
        wrapper.getField().add((Field)reader.sources.get("/testPathfoo<0>/value"));
        wrapper.getField().add((Field)reader.sources.get("/testPathfoo<>/value"));
        session.head().setSourceField(wrapper);
        DefaultAtlasExpressionProcessor.processExpression(session, expression);
        assertFalse(printAudit(session), session.hasErrors());
        assertEquals(FieldGroup.class, session.head().getSourceField().getClass());
        FieldGroup fieldGroup = (FieldGroup) session.head().getSourceField();
        assertEquals("/testPathfoo<>/value", fieldGroup.getPath());
        assertEquals(10, fieldGroup.getField().size());
        Field childField = fieldGroup.getField().get(0);
        assertEquals("/testPathfoo<0>/value", childField.getPath());
        assertEquals("foo0", childField.getValue());
        childField = fieldGroup.getField().get(1);
        assertEquals("/testPathfoo<1>/value", childField.getPath());
        assertEquals("foo1", childField.getValue());
    }

    @Test
    public void testFilter() throws Exception {
        FieldGroup source = populateComplexCollectionSourceField(null, AtlasConstants.DEFAULT_SOURCE_DOCUMENT_ID, "foo");
        String expression = String.format(
            "FILTER(${%s:/testPathfoo<>}, ${/value} != 'foo1')",
            AtlasConstants.DEFAULT_SOURCE_DOCUMENT_ID, AtlasConstants.DEFAULT_SOURCE_DOCUMENT_ID);
        recreateSession();
        session.head().setSourceField(source);
        DefaultAtlasExpressionProcessor.processExpression(session, expression);
        assertFalse(printAudit(session), session.hasErrors());
        assertEquals(FieldGroup.class, session.head().getSourceField().getClass());
        FieldGroup fieldGroup = (FieldGroup) session.head().getSourceField();
        assertEquals("/testPathfoo<>", fieldGroup.getPath());
        assertEquals(9, fieldGroup.getField().size());
        FieldGroup childFieldGroup = (FieldGroup) fieldGroup.getField().get(0);
        assertEquals("/testPathfoo<0>", childFieldGroup.getPath());
        assertEquals(1, childFieldGroup.getField().size());
        assertEquals("foo0", childFieldGroup.getField().get(0).getValue());
        childFieldGroup = (FieldGroup) fieldGroup.getField().get(1);
        assertEquals("/testPathfoo<1>", childFieldGroup.getPath());
        assertEquals(1, childFieldGroup.getField().size());
        assertEquals("foo2", childFieldGroup.getField().get(0).getValue());
    }

    @Test
    public void testSelect() throws Exception {
        FieldGroup source = populateComplexCollectionSourceField(null, AtlasConstants.DEFAULT_SOURCE_DOCUMENT_ID, "foo");
        String expression = String.format(
            "SELECT(${%s:/testPathfoo<>}, ${/value})",
            AtlasConstants.DEFAULT_SOURCE_DOCUMENT_ID, AtlasConstants.DEFAULT_SOURCE_DOCUMENT_ID);
        recreateSession();
        session.head().setSourceField(source);
        DefaultAtlasExpressionProcessor.processExpression(session, expression);
        assertFalse(printAudit(session), session.hasErrors());
        assertEquals(FieldGroup.class, session.head().getSourceField().getClass());
        FieldGroup fieldGroup = (FieldGroup) session.head().getSourceField();
        assertEquals("/testPathfoo<>/value", fieldGroup.getPath());
        assertEquals(10, fieldGroup.getField().size());
        Field child = fieldGroup.getField().get(0);
        assertEquals("/testPathfoo<0>/value", child.getPath());
        assertEquals("foo0", child.getValue());
        child = fieldGroup.getField().get(1);
        assertEquals("/testPathfoo<1>/value", child.getPath());
        assertEquals("foo1", child.getValue());
    }

    @Test
    public void testFilterSelect() throws Exception {
        FieldGroup source = populateComplexCollectionSourceField(null, AtlasConstants.DEFAULT_SOURCE_DOCUMENT_ID, "foo");
        String expression = String.format(
            "SELECT(FILTER(${%s:/testPathfoo<>}, ${/value} != 'foo1'), ${/value})",
            AtlasConstants.DEFAULT_SOURCE_DOCUMENT_ID, AtlasConstants.DEFAULT_SOURCE_DOCUMENT_ID);
        recreateSession();
        session.head().setSourceField(source);
        DefaultAtlasExpressionProcessor.processExpression(session, expression);
        assertFalse(printAudit(session), session.hasErrors());
        assertEquals(FieldGroup.class, session.head().getSourceField().getClass());
        FieldGroup fieldGroup = (FieldGroup) session.head().getSourceField();
        assertEquals("/testPathfoo<>/value", fieldGroup.getPath());
        assertEquals(9, fieldGroup.getField().size());
        Field child = fieldGroup.getField().get(0);
        assertEquals("/testPathfoo<0>/value", child.getPath());
        assertEquals("foo0", child.getValue());
        child = fieldGroup.getField().get(1);
        assertEquals("/testPathfoo<1>/value", child.getPath());
        assertEquals("foo2", child.getValue());
    }

    @Test
    public void testScopedProperty() throws Exception {
        FieldGroup source = new FieldGroup();
        PropertyField doc1Prop = new PropertyField();
        doc1Prop.setDocId("DOC.Properties.85731");
        doc1Prop.setScope("Doc1");
        doc1Prop.setPath("/Doc1/testprop");
        doc1Prop.setName("testprop");
        doc1Prop.setValue("doc1prop");
        source.getField().add(doc1Prop);
        PropertyField doc2Prop = new PropertyField();
        doc2Prop.setDocId("DOC.Properties.85731");
        doc2Prop.setScope("Doc2");
        doc2Prop.setPath("/Doc2/testprop");
        doc2Prop.setName("testprop");
        doc2Prop.setValue("doc2prop");
        source.getField().add(doc2Prop);
        PropertyField currentProp = new PropertyField();
        currentProp.setDocId("DOC.Properties.85731");
        currentProp.setPath("/testprop");
        currentProp.setName("testprop");
        currentProp.setValue("currentprop");
        source.getField().add(currentProp);
        String expression = "${DOC.Properties.85731:/Doc1/testprop} + ${DOC.Properties.85731:/Doc2/testprop}"
                + " + ${DOC.Properties.85731:/testprop}";
        recreateSession();
        context.getSourceModules().put(AtlasConstants.PROPERTIES_SOURCE_DOCUMENT_ID, new PropertyModule(new DefaultAtlasPropertyStrategy()));
        session.head().setSourceField(source);
        DefaultAtlasExpressionProcessor.processExpression(session, expression);
        assertFalse(printAudit(session), session.hasErrors());
        assertEquals(SimpleField.class, session.head().getSourceField().getClass());
        SimpleField field = (SimpleField) session.head().getSourceField();
        assertEquals("$ATLASMAP", field.getPath());
        assertEquals("doc1propdoc2propcurrentprop", field.getValue());
    }

}

