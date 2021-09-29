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
package io.atlasmap.json.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.junit.jupiter.api.Test;

import io.atlasmap.api.AtlasException;
import io.atlasmap.core.DefaultAtlasConversionService;
import io.atlasmap.json.v2.AtlasJsonModelFactory;
import io.atlasmap.json.v2.JsonField;
import io.atlasmap.spi.AtlasInternalSession;
import io.atlasmap.spi.AtlasInternalSession.Head;
import io.atlasmap.v2.AuditStatus;
import io.atlasmap.v2.Audits;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldGroup;
import io.atlasmap.v2.FieldType;

public class JsonFieldReaderTest {

    private static JsonFieldReader reader = new JsonFieldReader(DefaultAtlasConversionService.getInstance());

    @Test
    public void testWithNullDocument() throws Exception {
        reader.setDocument(null);
        AtlasInternalSession session = mock(AtlasInternalSession.class);
        when(session.head()).thenReturn(mock(Head.class));
        when(session.head().getSourceField()).thenReturn(AtlasJsonModelFactory.createJsonField());
        Audits audits = new Audits();
        when(session.getAudits()).thenReturn(audits);
        reader.read(session);
        assertEquals(1, audits.getAudit().size());
        assertEquals(AuditStatus.ERROR, audits.getAudit().get(0).getStatus());
    }

    @Test
    public void testWithEmptyDocument() throws Exception {
        reader.setDocument("");
        AtlasInternalSession session = mock(AtlasInternalSession.class);
        when(session.head()).thenReturn(mock(Head.class));
        when(session.head().getSourceField()).thenReturn(AtlasJsonModelFactory.createJsonField());
        Audits audits = new Audits();
        when(session.getAudits()).thenReturn(audits);
        reader.read(session);
        assertEquals(1, audits.getAudit().size());
        assertEquals(AuditStatus.ERROR, audits.getAudit().get(0).getStatus());
    }

    @Test
    public void testWithNullJsonField() throws Exception {
        assertThrows(AtlasException.class, () -> {
            reader.setDocument("{qwerty : ytrewq}");
            reader.read(mock(AtlasInternalSession.class));
        });
    }

    @Test
    public void testSimpleJsonDocument() throws Exception {
        final String document = "   { \"brand\" : \"Mercedes\", \"doors\" : 5 }";
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/brand");
        AtlasInternalSession session = mock(AtlasInternalSession.class);
        when(session.head()).thenReturn(mock(Head.class));
        when(session.head().getSourceField()).thenReturn(field);
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("Mercedes", field.getValue());

        field.setFieldType(null);
        field.setPath("/doors");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals(5, field.getValue());

    }

    @Test
    public void testSimpleJsonDocumentWithRoot() throws Exception {
        final String document = " {\"car\" :{ \"brand\" : \"Mercedes\", \"doors\" : 5 } }";
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/car/doors");
        AtlasInternalSession session = mock(AtlasInternalSession.class);
        when(session.head()).thenReturn(mock(Head.class));
        when(session.head().getSourceField()).thenReturn(field);
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals(5, field.getValue());
        resetField(field);

        field.setPath("/car/brand");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("Mercedes", field.getValue());
    }

    @Test
    public void testComplexJsonDocumentNestedObjectArray() throws Exception {
        final String document = "{\"menu\": {\n" + "  \"id\": \"file\",\n" + "  \"value\": \"Filed\",\n"
                + "  \"popup\": {\n" + "    \"menuitem\": [\n"
                + "      {\"value\": \"New\", \"onclick\": \"CreateNewDoc()\"},\n"
                + "      {\"value\": \"Open\", \"onclick\": \"OpenDoc()\"},\n"
                + "      {\"value\": \"Close\", \"onclick\": \"CloseDoc()\"}\n" + "    ]\n" + "  }\n" + "}}";
        reader.setDocument(document);

        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/menu/id");
        AtlasInternalSession session = mock(AtlasInternalSession.class);
        when(session.head()).thenReturn(mock(Head.class));
        when(session.head().getSourceField()).thenReturn(field);
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("file", field.getValue());

        field.setPath("/menu/value");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("Filed", field.getValue());

        field.setPath("/menu/popup/menuitem[0]/value");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("New", field.getValue());

        field.setPath("/menu/popup/menuitem[0]/onclick");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("CreateNewDoc()", field.getValue());

        field.setPath("/menu/popup/menuitem[1]/value");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("Open", field.getValue());

        field.setPath("/menu/popup/menuitem[1]/onclick");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("OpenDoc()", field.getValue());

        field.setPath("/menu/popup/menuitem[2]/value");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("Close", field.getValue());

        field.setPath("/menu/popup/menuitem[2]/onclick");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("CloseDoc()", field.getValue());
    }

    @Test
    public void testComplexJsonDocumentHighlyNested() throws Exception {
        final String document = new String(
                Files.readAllBytes(Paths.get("src/test/resources/highly-nested-object.json")));
        reader.setDocument(document);

        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/id");
        AtlasInternalSession session = mock(AtlasInternalSession.class);
        when(session.head()).thenReturn(mock(Head.class));
        when(session.head().getSourceField()).thenReturn(field);
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("0001", field.getValue());
        resetField(field);

        field.setPath("/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("donut", field.getValue());
        resetField(field);

        field.setPath("/name");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("Cake", field.getValue());
        resetField(field);

        field.setPath("/ppu");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals(0.55, field.getValue());
        resetField(field);

        field.setPath("/batters/batter[0]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("1001", field.getValue());
        resetField(field);

        field.setPath("/batters/batter[0]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("Regular", field.getValue());
        resetField(field);

        field.setPath("/batters/batter[1]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("1002", field.getValue());
        resetField(field);

        field.setPath("/batters/batter[1]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("Chocolate", field.getValue());
        resetField(field);

        field.setPath("/batters/batter[2]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("1003", field.getValue());
        resetField(field);

        field.setPath("/batters/batter[2]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("Blueberry", field.getValue());
        resetField(field);

        field.setPath("/batters/batter[3]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("1004", field.getValue());
        resetField(field);

        field.setPath("/batters/batter[3]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("Devil's Food", field.getValue());
        resetField(field);

        field.setPath("/topping[0]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("5001", field.getValue());
        resetField(field);

        field.setPath("/topping[0]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("None", field.getValue());
        resetField(field);

        field.setPath("/topping[1]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("5002", field.getValue());
        resetField(field);

        field.setPath("/topping[1]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("Glazed", field.getValue());
        resetField(field);

        field.setPath("/topping[2]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("5005", field.getValue());
        resetField(field);

        field.setPath("/topping[2]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("Sugar", field.getValue());
        resetField(field);

        field.setPath("/topping[3]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("5007", field.getValue());
        resetField(field);

        field.setPath("/topping[3]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("Powdered Sugar", field.getValue());
        resetField(field);

        field.setPath("/topping[4]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("5006", field.getValue());
        resetField(field);

        field.setPath("/topping[4]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("Chocolate with Sprinkles", field.getValue());
        resetField(field);

        field.setPath("/topping[5]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("5003", field.getValue());
        resetField(field);

        field.setPath("/topping[5]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("Chocolate", field.getValue());
        resetField(field);

        field.setPath("/topping[6]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("5004", field.getValue());
        resetField(field);

        field.setPath("/topping[6]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("Maple", field.getValue());
        resetField(field);
    }

    @Test
    public void testComplexJsonDocumentHighlyComplexNested() throws Exception {
        final String document = new String(
                Files.readAllBytes(Paths.get("src/test/resources/highly-complex-nested-object.json")));
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/items/item[0]/id");
        AtlasInternalSession session = mock(AtlasInternalSession.class);
        when(session.head()).thenReturn(mock(Head.class));
        when(session.head().getSourceField()).thenReturn(field);
        when(session.getAudits()).thenReturn(new Audits());
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("0001", field.getValue());
        resetField(field);

        field.setPath("/items/item[0]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("donut", field.getValue());
        resetField(field);

        field.setPath("/items/item[0]/name");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("Cake", field.getValue());
        resetField(field);

        field.setPath("/items/item[0]/ppu");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals(0.55, field.getValue());
        resetField(field);

        // array of objects
        field.setPath("/items/item[0]/batters/batter[0]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("1001", field.getValue());
        resetField(field);

        field.setPath("/items/item[0]/batters/batter[0]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("Regular", field.getValue());
        resetField(field);

        field.setPath("/items/item[0]/batters/batter[1]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("1002", field.getValue());
        resetField(field);

        field.setPath("/items/item[0]/batters/batter[1]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("Chocolate", field.getValue());
        resetField(field);

        field.setPath("/items/item[0]/batters/batter[2]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("1003", field.getValue());
        resetField(field);

        field.setPath("/items/item[0]/batters/batter[2]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("Blueberry", field.getValue());
        resetField(field);

        field.setPath("/items/item[0]/batters/batter[3]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("1004", field.getValue());
        resetField(field);

        field.setPath("/items/item[0]/batters/batter[3]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("Devil's Food", field.getValue());
        resetField(field);

        // simple array
        field.setPath("/items/item[0]/topping[0]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("5001", field.getValue());
        resetField(field);

        field.setPath("/items/item[0]/topping[0]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("None", field.getValue());
        resetField(field);

        field.setPath("/items/item[0]/topping[1]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("5002", field.getValue());
        resetField(field);

        field.setPath("/items/item[0]/topping[1]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("Glazed", field.getValue());
        resetField(field);

        field.setPath("/items/item[0]/topping[2]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("5005", field.getValue());
        resetField(field);

        field.setPath("/items/item[0]/topping[2]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("Sugar", field.getValue());
        resetField(field);

        field.setPath("/items/item[0]/topping[3]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("5007", field.getValue());
        resetField(field);

        field.setPath("/items/item[0]/topping[3]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("Powdered Sugar", field.getValue());
        resetField(field);

        field.setPath("/items/item[0]/topping[4]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("5006", field.getValue());
        resetField(field);

        field.setPath("/items/item[0]/topping[4]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("Chocolate with Sprinkles", field.getValue());
        resetField(field);

        field.setPath("/items/item[0]/topping[5]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("5003", field.getValue());
        resetField(field);

        field.setPath("/items/item[0]/topping[5]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("Chocolate", field.getValue());
        resetField(field);

        field.setPath("/items/item[0]/topping[6]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("5004", field.getValue());
        resetField(field);

        field.setPath("/items/item[0]/topping[6]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("Maple", field.getValue());
        resetField(field);

        field.setPath("/items/item[1]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("0002", field.getValue());
        resetField(field);

        field.setPath("/items/item[1]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("donut", field.getValue());
        resetField(field);

        field.setPath("/items/item[1]/name");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("Raised", field.getValue());
        resetField(field);

        field.setPath("/items/item[1]/ppu");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals(0.55, field.getValue());
        resetField(field);

        // array of objects
        field.setPath("/items/item[1]/batters/batter[0]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("1001", field.getValue());
        resetField(field);

        field.setPath("/items/item[1]/batters/batter[0]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("Regular", field.getValue());
        resetField(field);

        field.setPath("/items/item[1]/topping[0]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("5001", field.getValue());
        resetField(field);

        field.setPath("/items/item[1]/topping[0]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("None", field.getValue());
        resetField(field);

        field.setPath("/items/item[1]/topping[1]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("5002", field.getValue());
        resetField(field);

        field.setPath("/items/item[1]/topping[1]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("Glazed", field.getValue());
        resetField(field);

        field.setPath("/items/item[1]/topping[2]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("5005", field.getValue());
        resetField(field);

        field.setPath("/items/item[1]/topping[2]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("Sugar", field.getValue());
        resetField(field);

        field.setPath("/items/item[1]/topping[3]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("5003", field.getValue());
        resetField(field);

        field.setPath("/items/item[1]/topping[3]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("Chocolate", field.getValue());
        resetField(field);

        field.setPath("/items/item[1]/topping[4]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("5004", field.getValue());
        resetField(field);

        field.setPath("/items/item[1]/topping[4]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("Maple", field.getValue());
        resetField(field);

        field.setPath("/items/item[1]/topping[5]/id");
        reader.read(session);
        assertNull(field.getValue());
        resetField(field);

        field.setPath("/items/item[2]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("0003", field.getValue());
        resetField(field);

        field.setPath("/items/item[2]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("donut", field.getValue());
        resetField(field);

        field.setPath("/items/item[2]/name");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("Old Fashioned", field.getValue());
        resetField(field);

        field.setPath("/items/item[2]/ppu");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals(0.55, field.getValue());
        resetField(field);

        field.setPath("/items/item[3]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("0004", field.getValue());
        resetField(field);

        field.setPath("/items/item[3]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("bar", field.getValue());
        resetField(field);

        field.setPath("/items/item[3]/name");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("Bar", field.getValue());
        resetField(field);

        field.setPath("/items/item[3]/ppu");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals(0.75, field.getValue());
        resetField(field);

        field.setPath("/items/item[4]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("0005", field.getValue());
        resetField(field);

        field.setPath("/items/item[4]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("twist", field.getValue());
        resetField(field);

        field.setPath("/items/item[4]/name");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("Twist", field.getValue());
        resetField(field);

        field.setPath("/items/item[4]/ppu");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals(0.65, field.getValue());
        resetField(field);

        field.setPath("/items/item[5]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("0006", field.getValue());
        resetField(field);

        field.setPath("/items/item[5]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("filled", field.getValue());
        resetField(field);

        field.setPath("/items/item[5]/name");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("Filled", field.getValue());
        resetField(field);

        field.setPath("/items/item[5]/ppu");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals(0.75, field.getValue());
        resetField(field);

        field.setPath("/items/item[5]/fillings/filling[2]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("7004", field.getValue());
        resetField(field);

        field.setPath("/items/item[5]/fillings/filling[3]/addcost");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals(new BigInteger("100000000000000000000000000000000000"), field.getValue());
        resetField(field);
    }

    @Test
    public void testSameFieldNameInDifferentPath() throws Exception {
        final String document = new String(
                Files.readAllBytes(Paths.get("src/test/resources/same-field-name-in-different-path.json")));
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/name");
        AtlasInternalSession session = mock(AtlasInternalSession.class);
        when(session.head()).thenReturn(mock(Head.class));
        when(session.head().getSourceField()).thenReturn(field);
        reader.read(session);
        assertEquals("name", field.getValue());
        field.setPath("/object1/name");
        reader.read(session);
        assertEquals("object1-name", field.getValue());
        field.setPath("/object2/name");
        reader.read(session);
        assertEquals("object2-name", field.getValue());
        field.setPath("/object1/object2/name");
        reader.read(session);
        assertEquals("object1-object2-name", field.getValue());
    }

    @Test
    public void testArrayUnderRoot() throws Exception {
        final String document = new String(Files.readAllBytes(Paths.get("src/test/resources/array-under-root.json")));
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/array[0]");
        AtlasInternalSession session = mock(AtlasInternalSession.class);
        when(session.head()).thenReturn(mock(Head.class));
        when(session.head().getSourceField()).thenReturn(field);
        reader.read(session);
        assertEquals("array-zero", field.getValue());
        field.setPath("/array[1]");
        reader.read(session);
        assertEquals("array-one", field.getValue());
        field.setPath("/array[2]");
        reader.read(session);
        assertEquals("array-two", field.getValue());
    }

    private void resetField(JsonField field) {
        field.setPath(null);
        field.setValue(null);
        field.setFieldType(null);
    }

    private AtlasInternalSession testBoundaryValue(String fileName, String fieldPath, FieldType fieldType,
            Object expectedObject) throws Exception {
        String filePath = "src" + File.separator + "test" + File.separator + "resources" + File.separator + "jsonFields"
                + File.separator + fileName;
        String document = new String(Files.readAllBytes(Paths.get(filePath)));
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath(fieldPath);
        field.setFieldType(fieldType);
        AtlasInternalSession session = read(field);
        assertEquals(expectedObject, field.getValue());
        return session;
    }

    @Test
    public void testJsonFieldDoubleMax() throws Exception {
        testBoundaryValue("field-double-max.json", "/doubleValue", FieldType.DOUBLE, Double.MAX_VALUE);
    }

    @Test
    public void testJsonFieldDoubleMin() throws Exception {
        testBoundaryValue("field-double-min.json", "/doubleValue", FieldType.DOUBLE, Double.MIN_VALUE);
    }

    @Test
    public void testJsonFieldFloatMax() throws Exception {
        testBoundaryValue("field-float-max.json", "/floatValue", FieldType.FLOAT, Float.MAX_VALUE);
    }

    @Test
    public void testJsonFieldFloatMin() throws Exception {
        testBoundaryValue("field-float-min.json", "/floatValue", FieldType.FLOAT, Float.MIN_VALUE);
    }

    @Test
    public void testJsonFieldLongMax() throws Exception {
        testBoundaryValue("field-long-max.json", "/longValue", FieldType.LONG, Long.MAX_VALUE);
    }

    @Test
    public void testJsonFieldLongMin() throws Exception {
        testBoundaryValue("field-long-min.json", "/longValue", FieldType.LONG, Long.MIN_VALUE);
    }

    @Test
    public void testJsonFieldIntegerMax() throws Exception {
        testBoundaryValue("field-integer-max.json", "/integerValue", FieldType.INTEGER, Integer.MAX_VALUE);
    }

    @Test
    public void testJsonFieldIntegerMin() throws Exception {
        testBoundaryValue("field-integer-min.json", "/integerValue", FieldType.INTEGER, Integer.MIN_VALUE);
    }

    @Test
    public void testJsonFieldShortMax() throws Exception {
        testBoundaryValue("field-short-max.json", "/shortValue", FieldType.SHORT, Short.MAX_VALUE);
    }

    @Test
    public void testJsonFieldShortMin() throws Exception {
        testBoundaryValue("field-short-min.json", "/shortValue", FieldType.SHORT, Short.MIN_VALUE);
    }

    @Test
    public void testJsonFieldCharMax() throws Exception {
        testBoundaryValue("field-char-max.json", "/charValue", FieldType.CHAR, Character.MAX_VALUE);
    }

    @Test
    public void testJsonFieldCharMin() throws Exception {
        testBoundaryValue("field-char-min.json", "/charValue", FieldType.CHAR, Character.MIN_VALUE);
    }

    @Test
    public void testJsonFieldByteMax() throws Exception {
        testBoundaryValue("field-byte-max.json", "/byteValue", FieldType.BYTE, Byte.MAX_VALUE);
    }

    @Test
    public void testJsonFieldByteMin() throws Exception {
        testBoundaryValue("field-byte-min.json", "/byteValue", FieldType.BYTE, Byte.MIN_VALUE);
    }

    @Test
    public void testJsonFieldBooleanTrue() throws Exception {
        testBoundaryValue("field-boolean-true.json", "/booleanValue", FieldType.BOOLEAN, Boolean.TRUE);
    }

    @Test
    public void testJsonFieldBooleanFalse() throws Exception {
        testBoundaryValue("field-boolean-false.json", "/booleanValue", FieldType.BOOLEAN, Boolean.FALSE);
    }

    @Test
    public void testJsonFieldStringNonEmpty() throws Exception {
        testBoundaryValue("field-string-nonempty.json", "/stringValue", FieldType.STRING, "testString");
    }

    @Test
    public void testJsonFieldStringNull() throws Exception {
        testBoundaryValue("field-string-null.json", "/stringValue", FieldType.STRING, null);
    }

    @Test
    public void testJsonFieldStringEmpty() throws Exception {
        testBoundaryValue("field-string-empty.json", "/stringValue", FieldType.STRING, "");
    }

    @Test
    public void testJsonFieldStringNonExist() throws Exception {
        testBoundaryValue("field-string-nonexist.json", "/stringValue", FieldType.STRING, null);
    }

    private AtlasInternalSession read(JsonField field) throws AtlasException {
        AtlasInternalSession session = mock(AtlasInternalSession.class);
        when(session.head()).thenReturn(mock(Head.class));
        when(session.head().getSourceField()).thenReturn(field);

        Audits audits = new Audits();
        when(session.getAudits()).thenReturn(audits);
        reader.read(session);
        return session;
    }

    private void testRangeOutValue(String fileName, String fieldPath, FieldType fieldType, String errorMessage,
            String errorValue) throws Exception {
        String filePath = "src" + File.separator + "test" + File.separator + "resources" + File.separator + "jsonFields"
                + File.separator + fileName;
        String document = new String(Files.readAllBytes(Paths.get(filePath)));
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath(fieldPath);
        field.setFieldType(fieldType);
        AtlasInternalSession session = read(field);

        assertEquals(null, field.getValue());
        assertEquals(1, session.getAudits().getAudit().size());
        assertEquals(errorMessage, session.getAudits().getAudit().get(0).getMessage());
        assertEquals(errorValue, session.getAudits().getAudit().get(0).getValue());
        assertEquals(AuditStatus.ERROR, session.getAudits().getAudit().get(0).getStatus());
    }

    @Test
    public void testJsonFieldDoubleMaxRangeOut() throws Exception {
        testRangeOutValue("field-double-max-range-out.json", "/doubleValue", FieldType.DOUBLE,
                "Failed to convert field value 'Infinity' into type 'DOUBLE'", "Infinity");
    }

    @Test
    public void testJsonFieldDoubleMinRangeOut() throws Exception {
        AtlasInternalSession session = testBoundaryValue("field-double-min-range-out.json", "/doubleValue",
                FieldType.DOUBLE, 0.0);
        assertEquals(0, session.getAudits().getAudit().size());
    }

    @Test
    public void testJsonFieldFloatMaxRangOut() throws Exception {
        testRangeOutValue("field-float-max-range-out.json", "/floatValue", FieldType.FLOAT,
                "Failed to convert field value '3.4028235E39' into type 'FLOAT'", "3.4028235E39");
    }

    @Test
    public void testJsonFieldFloatMinRangOut() throws Exception {
        AtlasInternalSession session = testBoundaryValue("field-float-min-range-out.json", "/floatValue",
                FieldType.FLOAT, 0.0f);
        assertEquals(0, session.getAudits().getAudit().size());
    }

    @Test
    public void testJsonFieldLongMaxRangeOut() throws Exception {
        testRangeOutValue("field-long-max-range-out.json", "/longValue", FieldType.LONG,
                "Failed to convert field value '9223372036854775808' into type 'LONG'", "9223372036854775808");
    }

    @Test
    public void testJsonFieldLongMinRangeOut() throws Exception {
        testRangeOutValue("field-long-min-range-out.json", "/longValue", FieldType.LONG,
                "Failed to convert field value '-9223372036854775809' into type 'LONG'", "-9223372036854775809");
    }

    @Test
    public void testJsonFieldIntegerMaxRangeOut() throws Exception {
        testRangeOutValue("field-integer-max-range-out.json", "/integerValue", FieldType.INTEGER,
                "Failed to convert field value '2147483648' into type 'INTEGER'", "2147483648");
    }

    @Test
    public void testJsonFieldIntegerMinRangeOut() throws Exception {
        testRangeOutValue("field-integer-min-range-out.json", "/integerValue", FieldType.INTEGER,
                "Failed to convert field value '-2147483649' into type 'INTEGER'", "-2147483649");
    }

    @Test
    public void testJsonFieldShortMaxRangeOut() throws Exception {
        testRangeOutValue("field-short-max-range-out.json", "/shortValue", FieldType.SHORT,
                "Failed to convert field value '32768' into type 'SHORT'", "32768");
    }

    @Test
    public void testJsonFieldShortMinRangeOut() throws Exception {
        testRangeOutValue("field-short-min-range-out.json", "/shortValue", FieldType.SHORT,
                "Failed to convert field value '-32769' into type 'SHORT'", "-32769");
    }

    @Test
    public void testJsonFieldCharMaxRangeOut() throws Exception {
        testRangeOutValue("field-char-max-range-out.json", "/charValue", FieldType.CHAR,
                "Failed to convert field value '65536' into type 'CHAR'", "65536");
    }

    @Test
    public void testJsonFieldCharMinRangeOut() throws Exception {
        testRangeOutValue("field-char-min-range-out.json", "/charValue", FieldType.CHAR,
                "Failed to convert field value '-1' into type 'CHAR'", "-1");
    }

    @Test
    public void testJsonFieldByteMaxRangeOut() throws Exception {
        testRangeOutValue("field-byte-max-range-out.json", "/byteValue", FieldType.BYTE,
                "Failed to convert field value '128' into type 'BYTE'", "128");
    }

    @Test
    public void testJsonFieldByteMinRangeOut() throws Exception {
        testRangeOutValue("field-byte-min-range-out.json", "/byteValue", FieldType.BYTE,
                "Failed to convert field value '-129' into type 'BYTE'", "-129");
    }

    @Test
    public void testJsonFieldBooleanRangeOut() throws Exception {
        testBoundaryValue("field-boolean-range-out.json", "/booleanValue", FieldType.BOOLEAN, Boolean.FALSE);
    }

    @Test
    public void testJsonFieldBooleanWithLetterF() throws Exception {
        testBoundaryValue("field-boolean-with-letterF.json", "/booleanValue", FieldType.BOOLEAN, Boolean.FALSE);
    }

    @Test
    public void testJsonFieldBooleanWithBlankString() throws Exception {
        testBoundaryValue("field-boolean-with-blank-string.json", "/booleanValue", FieldType.BOOLEAN, Boolean.FALSE);
    }

    @Test
    public void testJsonFieldBooleanWithNull() throws Exception {
        testBoundaryValue("field-boolean-with-null.json", "/booleanValue", FieldType.BOOLEAN, null);
    }

    @Test
    public void testJsonFieldBooleanWithNumber0() throws Exception {
        testBoundaryValue("field-boolean-with-number0.json", "/booleanValue", FieldType.BOOLEAN, Boolean.FALSE);
    }

    @Test
    public void testJsonFieldBooleanWithNumber1() throws Exception {
        testBoundaryValue("field-boolean-with-number1.json", "/booleanValue", FieldType.BOOLEAN, Boolean.TRUE);
    }

    @Test
    public void testJsonFieldLongDecimal() throws Exception {
        testRangeOutValue("field-long-decimal.json", "/longValue", FieldType.LONG,
                "Failed to convert field value '9.223372036854776E18' into type 'LONG'", "9.223372036854776E18");
    }

    @Test
    public void testJsonFieldIntegerDecimal() throws Exception {
        testRangeOutValue("field-integer-decimal.json", "/integerValue", FieldType.INTEGER,
                "Failed to convert field value '2.1474836471234E9' into type 'INTEGER'", "2.1474836471234E9");
    }

    @Test
    public void testJsonFieldShortDecimal() throws Exception {
        testRangeOutValue("field-short-decimal.json", "/shortValue", FieldType.SHORT,
                "Failed to convert field value '32767.1234' into type 'SHORT'", "32767.1234");
    }

    @Test
    public void testJsonFieldCharDecimal() throws Exception {
        testRangeOutValue("field-char-decimal.json", "/charValue", FieldType.CHAR,
                "Failed to convert field value '65535.1234' into type 'CHAR'", "65535.1234");
    }

    @Test
    public void testJsonFieldByteDecimal() throws Exception {
        testRangeOutValue("field-byte-decimal.json", "/byteValue", FieldType.BYTE,
                "Failed to convert field value '127.1234' into type 'BYTE'", "127.1234");
    }

    @Test
    public void testJsonFieldDoubleString() throws Exception {
        testRangeOutValue("field-double-string.json", "/doubleValue", FieldType.DOUBLE,
                "Failed to convert field value 'abcd' into type 'DOUBLE'", "abcd");
    }

    @Test
    public void testJsonFieldFloatString() throws Exception {
        testRangeOutValue("field-float-string.json", "/floatValue", FieldType.FLOAT,
                "Failed to convert field value 'abcd' into type 'FLOAT'", "abcd");
    }

    @Test
    public void testJsonFieldLongString() throws Exception {
        testRangeOutValue("field-long-string.json", "/longValue", FieldType.LONG,
                "Failed to convert field value 'abcd' into type 'LONG'", "abcd");
    }

    @Test
    public void testJsonFieldIntegerString() throws Exception {
        testRangeOutValue("field-integer-string.json", "/integerValue", FieldType.INTEGER,
                "Failed to convert field value 'abcd' into type 'INTEGER'", "abcd");
    }

    @Test
    public void testJsonFieldShortString() throws Exception {
        testRangeOutValue("field-short-string.json", "/shortValue", FieldType.SHORT,
                "Failed to convert field value 'abcd' into type 'SHORT'", "abcd");
    }

    @Test
    public void testJsonFieldCharString() throws Exception {
        testRangeOutValue("field-char-string.json", "/charValue", FieldType.CHAR,
                "Failed to convert field value 'abcd' into type 'CHAR'", "abcd");
    }

    @Test
    public void testJsonFieldByteString() throws Exception {
        testRangeOutValue("field-byte-string.json", "/byteValue", FieldType.BYTE,
                "Failed to convert field value 'abcd' into type 'BYTE'", "abcd");
    }

    @Test
    public void testJsonFieldTopmostArraySimple() throws Exception {
        final String document = "[ 100, 500, 300, 200, 400 ]";
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/<1>");
        AtlasInternalSession session = mock(AtlasInternalSession.class);
        when(session.head()).thenReturn(mock(Head.class));
        when(session.head().getSourceField()).thenReturn(field);
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals(500, field.getValue());

        field.setFieldType(null);
        field.setPath("/<>");
        Field readField = reader.read(session);
        assertEquals(FieldGroup.class, readField.getClass());
        FieldGroup readFieldGroup = (FieldGroup)readField;
        assertEquals(5, readFieldGroup.getField().size());
        assertEquals(100, readFieldGroup.getField().get(0).getValue());
        assertEquals(500, readFieldGroup.getField().get(1).getValue());
        assertEquals(300, readFieldGroup.getField().get(2).getValue());
        assertEquals(200, readFieldGroup.getField().get(3).getValue());
        assertEquals(400, readFieldGroup.getField().get(4).getValue());
    }

    @Test
    public void testJsonFieldTopmostArrayObject() throws Exception {
        final String document = "[\n" + "\t{\n" + "\t\t\"color\": \"red\",\n" + "\t\t\"value\": \"#f00\"\n" + "\t},\n"
                + "\t{\n" + "\t\t\"color\": \"green\",\n" + "\t\t\"value\": \"#0f0\"\n" + "\t},\n" + "\t{\n"
                + "\t\t\"color\": \"blue\",\n" + "\t\t\"value\": \"#00f\"\n" + "\t}]";
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/<0>/color");
        AtlasInternalSession session = mock(AtlasInternalSession.class);
        when(session.head()).thenReturn(mock(Head.class));
        when(session.head().getSourceField()).thenReturn(field);
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("red", field.getValue());
        field.setPath("/<1>/value");
        reader.read(session);
        assertNotNull(field.getValue());
        assertEquals("#0f0", field.getValue());

        field.setFieldType(null);
        field.setPath("/<>/color");
        Field readField = reader.read(session);
        assertEquals(FieldGroup.class, readField.getClass());
        FieldGroup readFieldGroup = (FieldGroup)readField;
        assertEquals(3, readFieldGroup.getField().size());
        assertEquals("red", readFieldGroup.getField().get(0).getValue());
        assertEquals("green", readFieldGroup.getField().get(1).getValue());
        assertEquals("blue", readFieldGroup.getField().get(2).getValue());
    }

    @Test
    public void testReadParentCollection() throws Exception {
        final String document = new String(Files.readAllBytes(Paths.get(
            getClass().getClassLoader().getResource("complex-repeated-result.json").toURI())));
        reader.setDocument(document);
        FieldGroup address = new FieldGroup();
        address.setFieldType(FieldType.COMPLEX);
        address.setDocId("json");
        address.setPath("/orders[]/address");
        JsonField addressLine1 = AtlasJsonModelFactory.createJsonField();
        addressLine1.setFieldType(FieldType.STRING);
        addressLine1.setDocId("json");
        addressLine1.setPath("/orders[]/address/addressLine1");
        address.getField().add(addressLine1);
        AtlasInternalSession session = mock(AtlasInternalSession.class);
        when(session.head()).thenReturn(mock(Head.class));
        when(session.head().getSourceField()).thenReturn(address);
        Field readField = reader.read(session);
        assertNotNull(readField);
        assertEquals(FieldGroup.class, readField.getClass());
        FieldGroup readGroup = FieldGroup.class.cast(readField);
        assertEquals(5, readGroup.getField().size());
        for (int i=0; i<5; i++) {
            FieldGroup readAddress = (FieldGroup) readGroup.getField().get(i);
            assertEquals("/orders[" + i + "]/address", readAddress.getPath());
            assertEquals(1, readAddress.getField().size());
            Field readAddressLine = (Field) readAddress.getField().get(0);
            assertEquals("/orders[" + i + "]/address/addressLine1", readAddressLine.getPath());
            assertEquals("123 Main St (" + (i+1) + ")", readAddressLine.getValue());
        }
    }

    @Test
    public void testReadParentCollectionEmpty() throws Exception {
        final String document = new String(Files.readAllBytes(Paths.get(
            getClass().getClassLoader().getResource("complex-repeated-result-empty.json").toURI())));
        reader.setDocument(document);
        FieldGroup address = new FieldGroup();
        address.setFieldType(FieldType.COMPLEX);
        address.setDocId("json");
        address.setPath("/orders[]/address");
        JsonField addressLine1 = AtlasJsonModelFactory.createJsonField();
        addressLine1.setFieldType(FieldType.STRING);
        addressLine1.setDocId("json");
        addressLine1.setPath("/orders[]/address/addressLine1");
        address.getField().add(addressLine1);
        AtlasInternalSession session = mock(AtlasInternalSession.class);
        when(session.head()).thenReturn(mock(Head.class));
        when(session.head().getSourceField()).thenReturn(address);
        Field readField = reader.read(session);
        assertNotNull(readField);
        assertEquals(FieldGroup.class, readField.getClass());
        FieldGroup readGroup = FieldGroup.class.cast(readField);
        assertEquals(0, readGroup.getField().size());
    }

    @Test
    public void testReadParentCollectionEmptyItem() throws Exception {
        final String document = new String(Files.readAllBytes(Paths.get(
            getClass().getClassLoader().getResource("complex-repeated-result-empty-item.json").toURI())));
        reader.setDocument(document);
        FieldGroup address = new FieldGroup();
        address.setFieldType(FieldType.COMPLEX);
        address.setDocId("json");
        address.setPath("/orders[]/address");
        JsonField addressLine1 = AtlasJsonModelFactory.createJsonField();
        addressLine1.setFieldType(FieldType.STRING);
        addressLine1.setDocId("json");
        addressLine1.setPath("/orders[]/address/addressLine1");
        address.getField().add(addressLine1);
        AtlasInternalSession session = mock(AtlasInternalSession.class);
        when(session.head()).thenReturn(mock(Head.class));
        when(session.head().getSourceField()).thenReturn(address);
        Field readField = reader.read(session);
        assertNotNull(readField);
        assertEquals(FieldGroup.class, readField.getClass());
        FieldGroup readGroup = FieldGroup.class.cast(readField);
        assertEquals(2, readGroup.getField().size());
        FieldGroup readAddress = (FieldGroup) readGroup.getField().get(0);
        assertEquals("/orders[0]/address", readAddress.getPath());
        assertEquals(1, readAddress.getField().size());
        Field readAddressLine = (Field) readAddress.getField().get(0);
        assertEquals("/orders[0]/address/addressLine1", readAddressLine.getPath());
        assertEquals("123 Main St (1)", readAddressLine.getValue());
        readAddress = (FieldGroup) readGroup.getField().get(1);
        assertEquals("/orders[2]/address", readAddress.getPath());
        assertEquals(1, readAddress.getField().size());
        readAddressLine = (Field) readAddress.getField().get(0);
        assertEquals("/orders[2]/address/addressLine1", readAddressLine.getPath());
        assertEquals("123 Main St (3)", readAddressLine.getValue());
    }
}
