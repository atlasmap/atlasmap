/**
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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.hamcrest.core.Is;
import org.junit.Test;

import io.atlasmap.api.AtlasException;
import io.atlasmap.core.DefaultAtlasConversionService;
import io.atlasmap.json.v2.AtlasJsonModelFactory;
import io.atlasmap.json.v2.JsonField;
import io.atlasmap.spi.AtlasInternalSession;
import io.atlasmap.spi.AtlasInternalSession.Head;
import io.atlasmap.v2.AuditStatus;
import io.atlasmap.v2.Audits;
import io.atlasmap.v2.FieldType;

public class JsonFieldReaderTest {

    private static JsonFieldReader reader = new JsonFieldReader(DefaultAtlasConversionService.getInstance());

    @Test(expected = AtlasException.class)
    public void testWithNullDocument() throws Exception {
        reader.setDocument(null);
        AtlasInternalSession session = mock(AtlasInternalSession.class);
        when(session.head().getSourceField()).thenReturn(AtlasJsonModelFactory.createJsonField());
        reader.read(session);
    }

    @Test(expected = AtlasException.class)
    public void testWithEmptyDocument() throws Exception {
        reader.setDocument("");
        AtlasInternalSession session = mock(AtlasInternalSession.class);
        when(session.head().getSourceField()).thenReturn(AtlasJsonModelFactory.createJsonField());
        reader.read(session);
    }

    @Test(expected = AtlasException.class)
    public void testWithNullJsonField() throws Exception {
        reader.setDocument("{qwerty : ytrewq}");
        reader.read(mock(AtlasInternalSession.class));
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
        assertThat(field.getValue(), Is.is("Mercedes"));

        field.setFieldType(null);
        field.setPath("/doors");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is(5));

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
        assertThat(field.getValue(), Is.is(5));
        resetField(field);

        field.setPath("/car/brand");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Mercedes"));
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
        assertThat(field.getValue(), Is.is("file"));

        field.setPath("/menu/value");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Filed"));

        field.setPath("/menu/popup/menuitem[0]/value");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("New"));

        field.setPath("/menu/popup/menuitem[0]/onclick");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("CreateNewDoc()"));

        field.setPath("/menu/popup/menuitem[1]/value");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Open"));

        field.setPath("/menu/popup/menuitem[1]/onclick");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("OpenDoc()"));

        field.setPath("/menu/popup/menuitem[2]/value");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Close"));

        field.setPath("/menu/popup/menuitem[2]/onclick");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("CloseDoc()"));
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
        assertThat(field.getValue(), Is.is("0001"));
        resetField(field);

        field.setPath("/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("donut"));
        resetField(field);

        field.setPath("/name");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Cake"));
        resetField(field);

        field.setPath("/ppu");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is(0.55));
        resetField(field);

        field.setPath("/batters/batter[0]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("1001"));
        resetField(field);

        field.setPath("/batters/batter[0]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Regular"));
        resetField(field);

        field.setPath("/batters/batter[1]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("1002"));
        resetField(field);

        field.setPath("/batters/batter[1]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Chocolate"));
        resetField(field);

        field.setPath("/batters/batter[2]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("1003"));
        resetField(field);

        field.setPath("/batters/batter[2]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Blueberry"));
        resetField(field);

        field.setPath("/batters/batter[3]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("1004"));
        resetField(field);

        field.setPath("/batters/batter[3]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Devil's Food"));
        resetField(field);

        field.setPath("/topping[0]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("5001"));
        resetField(field);

        field.setPath("/topping[0]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("None"));
        resetField(field);

        field.setPath("/topping[1]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("5002"));
        resetField(field);

        field.setPath("/topping[1]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Glazed"));
        resetField(field);

        field.setPath("/topping[2]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("5005"));
        resetField(field);

        field.setPath("/topping[2]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Sugar"));
        resetField(field);

        field.setPath("/topping[3]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("5007"));
        resetField(field);

        field.setPath("/topping[3]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Powdered Sugar"));
        resetField(field);

        field.setPath("/topping[4]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("5006"));
        resetField(field);

        field.setPath("/topping[4]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Chocolate with Sprinkles"));
        resetField(field);

        field.setPath("/topping[5]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("5003"));
        resetField(field);

        field.setPath("/topping[5]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Chocolate"));
        resetField(field);

        field.setPath("/topping[6]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("5004"));
        resetField(field);

        field.setPath("/topping[6]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Maple"));
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
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("0001"));
        resetField(field);

        field.setPath("/items/item[0]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("donut"));
        resetField(field);

        field.setPath("/items/item[0]/name");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Cake"));
        resetField(field);

        field.setPath("/items/item[0]/ppu");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is(0.55));
        resetField(field);

        // array of objects
        field.setPath("/items/item[0]/batters/batter[0]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("1001"));
        resetField(field);

        field.setPath("/items/item[0]/batters/batter[0]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Regular"));
        resetField(field);

        field.setPath("/items/item[0]/batters/batter[1]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("1002"));
        resetField(field);

        field.setPath("/items/item[0]/batters/batter[1]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Chocolate"));
        resetField(field);

        field.setPath("/items/item[0]/batters/batter[2]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("1003"));
        resetField(field);

        field.setPath("/items/item[0]/batters/batter[2]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Blueberry"));
        resetField(field);

        field.setPath("/items/item[0]/batters/batter[3]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("1004"));
        resetField(field);

        field.setPath("/items/item[0]/batters/batter[3]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Devil's Food"));
        resetField(field);

        // simple array
        field.setPath("/items/item[0]/topping[0]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("5001"));
        resetField(field);

        field.setPath("/items/item[0]/topping[0]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("None"));
        resetField(field);

        field.setPath("/items/item[0]/topping[1]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("5002"));
        resetField(field);

        field.setPath("/items/item[0]/topping[1]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Glazed"));
        resetField(field);

        field.setPath("/items/item[0]/topping[2]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("5005"));
        resetField(field);

        field.setPath("/items/item[0]/topping[2]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Sugar"));
        resetField(field);

        field.setPath("/items/item[0]/topping[3]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("5007"));
        resetField(field);

        field.setPath("/items/item[0]/topping[3]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Powdered Sugar"));
        resetField(field);

        field.setPath("/items/item[0]/topping[4]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("5006"));
        resetField(field);

        field.setPath("/items/item[0]/topping[4]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Chocolate with Sprinkles"));
        resetField(field);

        field.setPath("/items/item[0]/topping[5]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("5003"));
        resetField(field);

        field.setPath("/items/item[0]/topping[5]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Chocolate"));
        resetField(field);

        field.setPath("/items/item[0]/topping[6]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("5004"));
        resetField(field);

        field.setPath("/items/item[0]/topping[6]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Maple"));
        resetField(field);

        field.setPath("/items/item[1]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("0002"));
        resetField(field);

        field.setPath("/items/item[1]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("donut"));
        resetField(field);

        field.setPath("/items/item[1]/name");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Raised"));
        resetField(field);

        field.setPath("/items/item[1]/ppu");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is(0.55));
        resetField(field);

        // array of objects
        field.setPath("/items/item[1]/batters/batter[0]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("1001"));
        resetField(field);

        field.setPath("/items/item[1]/batters/batter[0]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Regular"));
        resetField(field);

        field.setPath("/items/item[1]/topping[0]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("5001"));
        resetField(field);

        field.setPath("/items/item[1]/topping[0]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("None"));
        resetField(field);

        field.setPath("/items/item[1]/topping[1]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("5002"));
        resetField(field);

        field.setPath("/items/item[1]/topping[1]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Glazed"));
        resetField(field);

        field.setPath("/items/item[1]/topping[2]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("5005"));
        resetField(field);

        field.setPath("/items/item[1]/topping[2]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Sugar"));
        resetField(field);

        field.setPath("/items/item[1]/topping[3]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("5003"));
        resetField(field);

        field.setPath("/items/item[1]/topping[3]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Chocolate"));
        resetField(field);

        field.setPath("/items/item[1]/topping[4]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("5004"));
        resetField(field);

        field.setPath("/items/item[1]/topping[4]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Maple"));
        resetField(field);

        field.setPath("/items/item[1]/topping[5]/id");
        reader.read(session);
        assertNull(field.getValue());
        resetField(field);

        field.setPath("/items/item[2]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("0003"));
        resetField(field);

        field.setPath("/items/item[2]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("donut"));
        resetField(field);

        field.setPath("/items/item[2]/name");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Old Fashioned"));
        resetField(field);

        field.setPath("/items/item[2]/ppu");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is(0.55));
        resetField(field);

        field.setPath("/items/item[3]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("0004"));
        resetField(field);

        field.setPath("/items/item[3]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("bar"));
        resetField(field);

        field.setPath("/items/item[3]/name");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Bar"));
        resetField(field);

        field.setPath("/items/item[3]/ppu");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is(0.75));
        resetField(field);

        field.setPath("/items/item[4]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("0005"));
        resetField(field);

        field.setPath("/items/item[4]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("twist"));
        resetField(field);

        field.setPath("/items/item[4]/name");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Twist"));
        resetField(field);

        field.setPath("/items/item[4]/ppu");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is(0.65));
        resetField(field);

        field.setPath("/items/item[5]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("0006"));
        resetField(field);

        field.setPath("/items/item[5]/type");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("filled"));
        resetField(field);

        field.setPath("/items/item[5]/name");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Filled"));
        resetField(field);

        field.setPath("/items/item[5]/ppu");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is(0.75));
        resetField(field);

        field.setPath("/items/item[5]/fillings/filling[2]/id");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("7004"));
        resetField(field);

        field.setPath("/items/item[5]/fillings/filling[3]/addcost");
        reader.read(session);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is(0));
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

    @Test
    public void testJsonFieldDoubleMax() throws Exception {
        String filePath = "src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "jsonFields" + File.separator + "field-double-max.json";
        String document = new String(Files.readAllBytes(Paths.get(filePath)));
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/doubleValue");
        field.setFieldType(FieldType.DOUBLE);
        read(field);
        assertEquals(Double.MAX_VALUE, field.getValue());
    }

    @Test
    public void testJsonFieldDoubleMin() throws Exception {
        String filePath = "src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "jsonFields" + File.separator + "field-double-min.json";
        String document = new String(Files.readAllBytes(Paths.get(filePath)));
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/doubleValue");
        field.setFieldType(FieldType.DOUBLE);
        read(field);
        assertEquals(Double.MIN_VALUE, field.getValue());
    }

    @Test
    public void testJsonFieldFloatMax() throws Exception {
        String filePath = "src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "jsonFields" + File.separator + "field-float-max.json";
        String document = new String(Files.readAllBytes(Paths.get(filePath)));
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/floatValue");
        field.setFieldType(FieldType.FLOAT);
        read(field);
        assertEquals(Float.MAX_VALUE, field.getValue());
    }

    @Test
    public void testJsonFieldFloatMin() throws Exception {
        String filePath = "src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "jsonFields" + File.separator + "field-float-min.json";
        String document = new String(Files.readAllBytes(Paths.get(filePath)));
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/floatValue");
        field.setFieldType(FieldType.FLOAT);
        read(field);
        assertEquals(Float.MIN_VALUE, field.getValue());
    }

    @Test
    public void testJsonFieldLongMax() throws Exception {
        String filePath = "src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "jsonFields" + File.separator + "field-long-max.json";
        String document = new String(Files.readAllBytes(Paths.get(filePath)));
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/longValue");
        field.setFieldType(FieldType.LONG);
        read(field);
        assertEquals(Long.MAX_VALUE, field.getValue());
    }

    @Test
    public void testJsonFieldLongMin() throws Exception {
        String filePath = "src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "jsonFields" + File.separator + "field-long-min.json";
        String document = new String(Files.readAllBytes(Paths.get(filePath)));
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/longValue");
        field.setFieldType(FieldType.LONG);
        read(field);
        assertEquals(Long.MIN_VALUE, field.getValue());
    }

    @Test
    public void testJsonFieldIntegerMax() throws Exception {
        String filePath = "src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "jsonFields" + File.separator + "field-integer-max.json";
        String document = new String(Files.readAllBytes(Paths.get(filePath)));
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/integerValue");
        field.setFieldType(FieldType.INTEGER);
        read(field);
        assertEquals(Integer.MAX_VALUE, field.getValue());
    }

    @Test
    public void testJsonFieldIntegerMin() throws Exception {
        String filePath = "src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "jsonFields" + File.separator + "field-integer-min.json";
        String document = new String(Files.readAllBytes(Paths.get(filePath)));
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/integerValue");
        field.setFieldType(FieldType.INTEGER);
        read(field);
        assertEquals(Integer.MIN_VALUE, field.getValue());
    }

    @Test
    public void testJsonFieldShortMax() throws Exception {
        String filePath = "src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "jsonFields" + File.separator + "field-short-max.json";
        String document = new String(Files.readAllBytes(Paths.get(filePath)));
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/shortValue");
        field.setFieldType(FieldType.SHORT);
        read(field);
        assertEquals(Short.MAX_VALUE, field.getValue());
    }

    @Test
    public void testJsonFieldShortMin() throws Exception {
        String filePath = "src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "jsonFields" + File.separator + "field-short-min.json";
        String document = new String(Files.readAllBytes(Paths.get(filePath)));
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/shortValue");
        field.setFieldType(FieldType.SHORT);
        read(field);
        assertEquals(Short.MIN_VALUE, field.getValue());
    }

    @Test
    public void testJsonFieldCharMax() throws Exception {
        String filePath = "src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "jsonFields" + File.separator + "field-char-max.json";
        String document = new String(Files.readAllBytes(Paths.get(filePath)));
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/charValue");
        field.setFieldType(FieldType.CHAR);
        read(field);
        assertEquals(Character.MAX_VALUE, field.getValue());
    }

    @Test
    public void testJsonFieldCharMin() throws Exception {
        String filePath = "src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "jsonFields" + File.separator + "field-char-min.json";
        String document = new String(Files.readAllBytes(Paths.get(filePath)));
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/charValue");
        field.setFieldType(FieldType.CHAR);
        read(field);
        assertEquals(Character.MIN_VALUE, field.getValue());
    }

    @Test
    public void testJsonFieldByteMax() throws Exception {
        String filePath = "src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "jsonFields" + File.separator + "field-byte-max.json";
        String document = new String(Files.readAllBytes(Paths.get(filePath)));
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/byteValue");
        field.setFieldType(FieldType.BYTE);
        read(field);
        assertEquals(Byte.MAX_VALUE, field.getValue());
    }

    @Test
    public void testJsonFieldByteMin() throws Exception {
        String filePath = "src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "jsonFields" + File.separator + "field-byte-min.json";
        String document = new String(Files.readAllBytes(Paths.get(filePath)));
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/byteValue");
        field.setFieldType(FieldType.BYTE);
        read(field);
        assertEquals(Byte.MIN_VALUE, field.getValue());
    }

    @Test
    public void testJsonFieldBooleanTrue() throws Exception {
        String filePath = "src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "jsonFields" + File.separator + "field-boolean-true.json";
        String document = new String(Files.readAllBytes(Paths.get(filePath)));
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/booleanValue");
        field.setFieldType(FieldType.BOOLEAN);
        read(field);
        assertEquals(Boolean.TRUE, field.getValue());
    }

    @Test
    public void testJsonFieldBooleanFalse() throws Exception {
        String filePath = "src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "jsonFields" + File.separator + "field-boolean-false.json";
        String document = new String(Files.readAllBytes(Paths.get(filePath)));
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/booleanValue");
        field.setFieldType(FieldType.BOOLEAN);
        read(field);
        assertEquals(Boolean.FALSE, field.getValue());
    }

    @Test
    public void testJsonFieldStringNonEmpty() throws Exception {
        String filePath = "src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "jsonFields" + File.separator + "field-string-nonempty.json";
        String document = new String(Files.readAllBytes(Paths.get(filePath)));
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/stringValue");
        field.setFieldType(FieldType.STRING);
        read(field);
        assertEquals("testString", field.getValue());
    }

    @Test
    public void testJsonFieldStringNull() throws Exception {
        String filePath = "src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "jsonFields" + File.separator + "field-string-null.json";
        String document = new String(Files.readAllBytes(Paths.get(filePath)));
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/stringValue");
        field.setFieldType(FieldType.STRING);
        read(field);
        assertEquals(null, field.getValue());
    }

    @Test
    public void testJsonFieldStringEmpty() throws Exception {
        String filePath = "src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "jsonFields" + File.separator + "field-string-empty.json";
        String document = new String(Files.readAllBytes(Paths.get(filePath)));
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/stringValue");
        field.setFieldType(FieldType.STRING);
        read(field);
        assertEquals("", field.getValue());
    }

    @Test
    public void testJsonFieldStringNonExist() throws Exception {
        String filePath = "src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "jsonFields" + File.separator + "field-string-nonexist.json";
        String document = new String(Files.readAllBytes(Paths.get(filePath)));
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/stringValue");
        field.setFieldType(FieldType.STRING);
        read(field);
        assertEquals(null, field.getValue());
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

    @Test
    public void testJsonFieldDoubleMaxRangeOut() throws Exception {
        String filePath = "src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "jsonFields" + File.separator + "field-double-max-range-out.json";
        String document = new String(Files.readAllBytes(Paths.get(filePath)));
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/doubleValue");
        field.setFieldType(FieldType.DOUBLE);
        AtlasInternalSession session = read(field);

        assertEquals(null, field.getValue());
        assertEquals(1, session.getAudits().getAudit().size());
        assertEquals("Failed to convert field value 'Infinity' into type 'DOUBLE'", session.getAudits().getAudit().get(0).getMessage());
        assertEquals("Infinity", session.getAudits().getAudit().get(0).getValue());
        assertEquals(AuditStatus.ERROR, session.getAudits().getAudit().get(0).getStatus());
    }

    @Test
    public void testJsonFieldDoubleMinRangeOut() throws Exception {
        String filePath = "src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "jsonFields" + File.separator + "field-double-min-range-out.json";
        String document = new String(Files.readAllBytes(Paths.get(filePath)));
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/doubleValue");
        field.setFieldType(FieldType.DOUBLE);
        AtlasInternalSession session = read(field);

        assertEquals(0.0, field.getValue());
        assertEquals(0, session.getAudits().getAudit().size());
    }

    @Test
    public void testJsonFieldFloatMaxRangOut() throws Exception {
        String filePath = "src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "jsonFields" + File.separator + "field-float-max-range-out.json";
        String document = new String(Files.readAllBytes(Paths.get(filePath)));
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/floatValue");
        field.setFieldType(FieldType.FLOAT);
        AtlasInternalSession session = read(field);

        assertEquals(null, field.getValue());
        assertEquals(1, session.getAudits().getAudit().size());
        assertEquals("Failed to convert field value '3.4028235E39' into type 'FLOAT'", session.getAudits().getAudit().get(0).getMessage());
        assertEquals("3.4028235E39", session.getAudits().getAudit().get(0).getValue());
        assertEquals(AuditStatus.ERROR, session.getAudits().getAudit().get(0).getStatus());
    }

    @Test
    public void testJsonFieldFloatMinRangOut() throws Exception {
        String filePath = "src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "jsonFields" + File.separator + "field-float-min-range-out.json";
        String document = new String(Files.readAllBytes(Paths.get(filePath)));
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/floatValue");
        field.setFieldType(FieldType.FLOAT);
        AtlasInternalSession session = read(field);

        assertEquals(0.0f, field.getValue());
        assertEquals(0, session.getAudits().getAudit().size());
    }

    @Test
    public void testJsonFieldLongMaxRangeOut() throws Exception {
        String filePath = "src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "jsonFields" + File.separator + "field-long-max-range-out.json";
        String document = new String(Files.readAllBytes(Paths.get(filePath)));
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/longValue");
        field.setFieldType(FieldType.LONG);
        AtlasInternalSession session = read(field);

        assertEquals(null, field.getValue());
        assertEquals(1, session.getAudits().getAudit().size());
        assertEquals("Failed to convert field value '9223372036854775808' into type 'LONG'", session.getAudits().getAudit().get(0).getMessage());
        assertEquals("9223372036854775808", session.getAudits().getAudit().get(0).getValue());
        assertEquals(AuditStatus.ERROR, session.getAudits().getAudit().get(0).getStatus());
    }

    @Test
    public void testJsonFieldLongMinRangeOut() throws Exception {
        String filePath = "src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "jsonFields" + File.separator + "field-long-min-range-out.json";
        String document = new String(Files.readAllBytes(Paths.get(filePath)));
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/longValue");
        field.setFieldType(FieldType.LONG);
        AtlasInternalSession session = read(field);

        assertEquals(null, field.getValue());
        assertEquals(1, session.getAudits().getAudit().size());
        assertEquals("Failed to convert field value '-9223372036854775809' into type 'LONG'", session.getAudits().getAudit().get(0).getMessage());
        assertEquals("-9223372036854775809", session.getAudits().getAudit().get(0).getValue());
        assertEquals(AuditStatus.ERROR, session.getAudits().getAudit().get(0).getStatus());
    }

    @Test
    public void testJsonFieldIntegerMaxRangeOut() throws Exception {
        String filePath = "src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "jsonFields" + File.separator + "field-integer-max-range-out.json";
        String document = new String(Files.readAllBytes(Paths.get(filePath)));
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/integerValue");
        field.setFieldType(FieldType.INTEGER);
        AtlasInternalSession session = read(field);

        assertEquals(null, field.getValue());
        assertEquals(1, session.getAudits().getAudit().size());
        assertEquals("Failed to convert field value '2147483648' into type 'INTEGER'", session.getAudits().getAudit().get(0).getMessage());
        assertEquals("2147483648", session.getAudits().getAudit().get(0).getValue());
        assertEquals(AuditStatus.ERROR, session.getAudits().getAudit().get(0).getStatus());
    }

    @Test
    public void testJsonFieldIntegerMinRangeOut() throws Exception {
        String filePath = "src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "jsonFields" + File.separator + "field-integer-min-range-out.json";
        String document = new String(Files.readAllBytes(Paths.get(filePath)));
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/integerValue");
        field.setFieldType(FieldType.INTEGER);
        AtlasInternalSession session = read(field);

        assertEquals(null, field.getValue());
        assertEquals(1, session.getAudits().getAudit().size());
        assertEquals("Failed to convert field value '-2147483649' into type 'INTEGER'", session.getAudits().getAudit().get(0).getMessage());
        assertEquals("-2147483649", session.getAudits().getAudit().get(0).getValue());
        assertEquals(AuditStatus.ERROR, session.getAudits().getAudit().get(0).getStatus());
    }

    @Test
    public void testJsonFieldShortMaxRangeOut() throws Exception {
        String filePath = "src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "jsonFields" + File.separator + "field-short-max-range-out.json";
        String document = new String(Files.readAllBytes(Paths.get(filePath)));
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/shortValue");
        field.setFieldType(FieldType.SHORT);
        AtlasInternalSession session = read(field);

        assertEquals(null, field.getValue());
        assertEquals(1, session.getAudits().getAudit().size());
        assertEquals("Failed to convert field value '32768' into type 'SHORT'", session.getAudits().getAudit().get(0).getMessage());
        assertEquals("32768", session.getAudits().getAudit().get(0).getValue());
        assertEquals(AuditStatus.ERROR, session.getAudits().getAudit().get(0).getStatus());
    }

    @Test
    public void testJsonFieldShortMinRangeOut() throws Exception {
        String filePath = "src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "jsonFields" + File.separator + "field-short-min-range-out.json";
        String document = new String(Files.readAllBytes(Paths.get(filePath)));
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/shortValue");
        field.setFieldType(FieldType.SHORT);
        AtlasInternalSession session = read(field);

        assertEquals(null, field.getValue());
        assertEquals(1, session.getAudits().getAudit().size());
        assertEquals("Failed to convert field value '-32769' into type 'SHORT'", session.getAudits().getAudit().get(0).getMessage());
        assertEquals("-32769", session.getAudits().getAudit().get(0).getValue());
        assertEquals(AuditStatus.ERROR, session.getAudits().getAudit().get(0).getStatus());
    }

    @Test
    public void testJsonFieldCharMaxRangeOut() throws Exception {
        String filePath = "src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "jsonFields" + File.separator + "field-char-max-range-out.json";
        String document = new String(Files.readAllBytes(Paths.get(filePath)));
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/charValue");
        field.setFieldType(FieldType.CHAR);
        AtlasInternalSession session = read(field);

        assertEquals(null, field.getValue());
        assertEquals(1, session.getAudits().getAudit().size());
        assertEquals("Failed to convert field value '65536' into type 'CHAR'", session.getAudits().getAudit().get(0).getMessage());
        assertEquals("65536", session.getAudits().getAudit().get(0).getValue());
        assertEquals(AuditStatus.ERROR, session.getAudits().getAudit().get(0).getStatus());
    }

    @Test
    public void testJsonFieldCharMinRangeOut() throws Exception {
        String filePath = "src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "jsonFields" + File.separator + "field-char-min-range-out.json";
        String document = new String(Files.readAllBytes(Paths.get(filePath)));
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/charValue");
        field.setFieldType(FieldType.CHAR);
        AtlasInternalSession session = read(field);

        assertEquals(null, field.getValue());
        assertEquals(1, session.getAudits().getAudit().size());
        assertEquals("Failed to convert field value '-1' into type 'CHAR'", session.getAudits().getAudit().get(0).getMessage());
        assertEquals("-1", session.getAudits().getAudit().get(0).getValue());
        assertEquals(AuditStatus.ERROR, session.getAudits().getAudit().get(0).getStatus());
    }

    @Test
    public void testJsonFieldByteMaxRangeOut() throws Exception {
        String filePath = "src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "jsonFields" + File.separator + "field-byte-max-range-out.json";
        String document = new String(Files.readAllBytes(Paths.get(filePath)));
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/byteValue");
        field.setFieldType(FieldType.BYTE);
        AtlasInternalSession session = read(field);

        assertEquals(null, field.getValue());
        assertEquals(1, session.getAudits().getAudit().size());
        assertEquals("Failed to convert field value '128' into type 'BYTE'", session.getAudits().getAudit().get(0).getMessage());
        assertEquals("128", session.getAudits().getAudit().get(0).getValue());
        assertEquals(AuditStatus.ERROR, session.getAudits().getAudit().get(0).getStatus());
    }

    @Test
    public void testJsonFieldByteMinRangeOut() throws Exception {
        String filePath = "src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "jsonFields" + File.separator + "field-byte-min-range-out.json";
        String document = new String(Files.readAllBytes(Paths.get(filePath)));
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/byteValue");
        field.setFieldType(FieldType.BYTE);
        AtlasInternalSession session = read(field);

        assertEquals(null, field.getValue());
        assertEquals(1, session.getAudits().getAudit().size());
        assertEquals("Failed to convert field value '-129' into type 'BYTE'", session.getAudits().getAudit().get(0).getMessage());
        assertEquals("-129", session.getAudits().getAudit().get(0).getValue());
        assertEquals(AuditStatus.ERROR, session.getAudits().getAudit().get(0).getStatus());
    }

    @Test
    public void testJsonFieldBooleanRangeOut() throws Exception {
        String filePath = "src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "jsonFields" + File.separator + "field-boolean-range-out.json";
        String document = new String(Files.readAllBytes(Paths.get(filePath)));
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/booleanValue");
        field.setFieldType(FieldType.BOOLEAN);
        read(field);

        assertEquals(Boolean.TRUE, field.getValue());
    }

    @Test
    public void testJsonFieldBooleanWithLetterF() throws Exception {
        String filePath = "src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "jsonFields" + File.separator + "field-boolean-with-letterF.json";
        String document = new String(Files.readAllBytes(Paths.get(filePath)));
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/booleanValue");
        field.setFieldType(FieldType.BOOLEAN);
        read(field);

        assertEquals(Boolean.FALSE, field.getValue());
    }

    @Test
    public void testJsonFieldBooleanWithBlankString() throws Exception {
        String filePath = "src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "jsonFields" + File.separator + "field-boolean-with-blank-string.json";
        String document = new String(Files.readAllBytes(Paths.get(filePath)));
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/booleanValue");
        field.setFieldType(FieldType.BOOLEAN);
        read(field);

        assertEquals(Boolean.FALSE, field.getValue());
    }

    @Test
    public void testJsonFieldBooleanWithNull() throws Exception {
        String filePath = "src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "jsonFields" + File.separator + "field-boolean-with-null.json";
        String document = new String(Files.readAllBytes(Paths.get(filePath)));
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/booleanValue");
        field.setFieldType(FieldType.BOOLEAN);
        read(field);

        assertEquals(null, field.getValue());
    }

    @Test
    public void testJsonFieldBooleanWithNumber0() throws Exception {
        String filePath = "src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "jsonFields" + File.separator + "field-boolean-with-number0.json";
        String document = new String(Files.readAllBytes(Paths.get(filePath)));
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/booleanValue");
        field.setFieldType(FieldType.BOOLEAN);
        read(field);

        assertEquals(Boolean.FALSE, field.getValue());
    }

    @Test
    public void testJsonFieldBooleanWithNumber1() throws Exception {
        String filePath = "src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "jsonFields" + File.separator + "field-boolean-with-number1.json";
        String document = new String(Files.readAllBytes(Paths.get(filePath)));
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/booleanValue");
        field.setFieldType(FieldType.BOOLEAN);
        read(field);

        assertEquals(Boolean.TRUE, field.getValue());
    }

    @Test
    public void testJsonFieldLongDecimal() throws Exception {
        String filePath = "src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "jsonFields" + File.separator + "field-long-decimal.json";
        String document = new String(Files.readAllBytes(Paths.get(filePath)));
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/longValue");
        field.setFieldType(FieldType.LONG);
        AtlasInternalSession session = read(field);

        assertEquals(null, field.getValue());
        assertEquals(1, session.getAudits().getAudit().size());
        assertEquals("Failed to convert field value '9.223372036854776E18' into type 'LONG'", session.getAudits().getAudit().get(0).getMessage());
        assertEquals("9.223372036854776E18", session.getAudits().getAudit().get(0).getValue());
        assertEquals(AuditStatus.ERROR, session.getAudits().getAudit().get(0).getStatus());
    }

    @Test
    public void testJsonFieldIntegerDecimal() throws Exception {
        String filePath = "src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "jsonFields" + File.separator + "field-integer-decimal.json";
        String document = new String(Files.readAllBytes(Paths.get(filePath)));
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/integerValue");
        field.setFieldType(FieldType.INTEGER);
        AtlasInternalSession session = read(field);

        assertEquals(null, field.getValue());
        assertEquals(1, session.getAudits().getAudit().size());
        assertEquals("Failed to convert field value '2.1474836471234E9' into type 'INTEGER'", session.getAudits().getAudit().get(0).getMessage());
        assertEquals("2.1474836471234E9", session.getAudits().getAudit().get(0).getValue());
        assertEquals(AuditStatus.ERROR, session.getAudits().getAudit().get(0).getStatus());
    }

    @Test
    public void testJsonFieldShortDecimal() throws Exception {
        String filePath = "src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "jsonFields" + File.separator + "field-short-decimal.json";
        String document = new String(Files.readAllBytes(Paths.get(filePath)));
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/shortValue");
        field.setFieldType(FieldType.SHORT);
        AtlasInternalSession session = read(field);

        assertEquals(null, field.getValue());
        assertEquals(1, session.getAudits().getAudit().size());
        assertEquals("Failed to convert field value '32767.1234' into type 'SHORT'", session.getAudits().getAudit().get(0).getMessage());
        assertEquals("32767.1234", session.getAudits().getAudit().get(0).getValue());
        assertEquals(AuditStatus.ERROR, session.getAudits().getAudit().get(0).getStatus());
    }

    @Test
    public void testJsonFieldCharDecimal() throws Exception {
        String filePath = "src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "jsonFields" + File.separator + "field-char-decimal.json";
        String document = new String(Files.readAllBytes(Paths.get(filePath)));
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/charValue");
        field.setFieldType(FieldType.CHAR);
        AtlasInternalSession session = read(field);

        assertEquals(null, field.getValue());
        assertEquals(1, session.getAudits().getAudit().size());
        assertEquals("Failed to convert field value '65535.1234' into type 'CHAR'", session.getAudits().getAudit().get(0).getMessage());
        assertEquals("65535.1234", session.getAudits().getAudit().get(0).getValue());
        assertEquals(AuditStatus.ERROR, session.getAudits().getAudit().get(0).getStatus());
    }

    @Test
    public void testJsonFieldByteDecimal() throws Exception {
        String filePath = "src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "jsonFields" + File.separator + "field-byte-decimal.json";
        String document = new String(Files.readAllBytes(Paths.get(filePath)));
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/byteValue");
        field.setFieldType(FieldType.BYTE);
        AtlasInternalSession session = read(field);

        assertEquals(null, field.getValue());
        assertEquals(1, session.getAudits().getAudit().size());
        assertEquals("Failed to convert field value '127.1234' into type 'BYTE'", session.getAudits().getAudit().get(0).getMessage());
        assertEquals("127.1234", session.getAudits().getAudit().get(0).getValue());
        assertEquals(AuditStatus.ERROR, session.getAudits().getAudit().get(0).getStatus());
    }

    @Test
    public void testJsonFieldDoubleString() throws Exception {
        String filePath = "src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "jsonFields" + File.separator + "field-double-string.json";
        String document = new String(Files.readAllBytes(Paths.get(filePath)));
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/doubleValue");
        field.setFieldType(FieldType.DOUBLE);
        AtlasInternalSession session = read(field);

        assertEquals(null, field.getValue());
        assertEquals(1, session.getAudits().getAudit().size());
        assertEquals("Failed to convert field value 'abcd' into type 'DOUBLE'", session.getAudits().getAudit().get(0).getMessage());
        assertEquals("abcd", session.getAudits().getAudit().get(0).getValue());
        assertEquals(AuditStatus.ERROR, session.getAudits().getAudit().get(0).getStatus());
    }

    @Test
    public void testJsonFieldFloatString() throws Exception {
        String filePath = "src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "jsonFields" + File.separator + "field-float-string.json";
        String document = new String(Files.readAllBytes(Paths.get(filePath)));
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/floatValue");
        field.setFieldType(FieldType.FLOAT);
        AtlasInternalSession session = read(field);

        assertEquals(null, field.getValue());
        assertEquals(1, session.getAudits().getAudit().size());
        assertEquals("Failed to convert field value 'abcd' into type 'FLOAT'", session.getAudits().getAudit().get(0).getMessage());
        assertEquals("abcd", session.getAudits().getAudit().get(0).getValue());
        assertEquals(AuditStatus.ERROR, session.getAudits().getAudit().get(0).getStatus());
    }

    @Test
    public void testJsonFieldLongString() throws Exception {
        String filePath = "src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "jsonFields" + File.separator + "field-long-string.json";
        String document = new String(Files.readAllBytes(Paths.get(filePath)));
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/longValue");
        field.setFieldType(FieldType.LONG);
        AtlasInternalSession session = read(field);

        assertEquals(null, field.getValue());
        assertEquals(1, session.getAudits().getAudit().size());
        assertEquals("Failed to convert field value 'abcd' into type 'LONG'", session.getAudits().getAudit().get(0).getMessage());
        assertEquals("abcd", session.getAudits().getAudit().get(0).getValue());
        assertEquals(AuditStatus.ERROR, session.getAudits().getAudit().get(0).getStatus());
    }

    @Test
    public void testJsonFieldIntegerString() throws Exception {
        String filePath = "src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "jsonFields" + File.separator + "field-integer-string.json";
        String document = new String(Files.readAllBytes(Paths.get(filePath)));
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/integerValue");
        field.setFieldType(FieldType.INTEGER);
        AtlasInternalSession session = read(field);

        assertEquals(null, field.getValue());
        assertEquals(1, session.getAudits().getAudit().size());
        assertEquals("Failed to convert field value 'abcd' into type 'INTEGER'", session.getAudits().getAudit().get(0).getMessage());
        assertEquals("abcd", session.getAudits().getAudit().get(0).getValue());
        assertEquals(AuditStatus.ERROR, session.getAudits().getAudit().get(0).getStatus());
    }

    @Test
    public void testJsonFieldShortString() throws Exception {
        String filePath = "src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "jsonFields" + File.separator + "field-short-string.json";
        String document = new String(Files.readAllBytes(Paths.get(filePath)));
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/shortValue");
        field.setFieldType(FieldType.SHORT);
        AtlasInternalSession session = read(field);

        assertEquals(null, field.getValue());
        assertEquals(1, session.getAudits().getAudit().size());
        assertEquals("Failed to convert field value 'abcd' into type 'SHORT'", session.getAudits().getAudit().get(0).getMessage());
        assertEquals("abcd", session.getAudits().getAudit().get(0).getValue());
        assertEquals(AuditStatus.ERROR, session.getAudits().getAudit().get(0).getStatus());
    }

    @Test
    public void testJsonFieldCharString() throws Exception {
        String filePath = "src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "jsonFields" + File.separator + "field-char-string.json";
        String document = new String(Files.readAllBytes(Paths.get(filePath)));
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/charValue");
        field.setFieldType(FieldType.CHAR);
        AtlasInternalSession session = read(field);

        assertEquals(null, field.getValue());
        assertEquals(1, session.getAudits().getAudit().size());
        assertEquals("Failed to convert field value 'abcd' into type 'CHAR'", session.getAudits().getAudit().get(0).getMessage());
        assertEquals("abcd", session.getAudits().getAudit().get(0).getValue());
        assertEquals(AuditStatus.ERROR, session.getAudits().getAudit().get(0).getStatus());
    }

    @Test
    public void testJsonFieldByteString() throws Exception {
        String filePath = "src" + File.separator + "test" + File.separator + "resources"
                + File.separator + "jsonFields" + File.separator + "field-byte-string.json";
        String document = new String(Files.readAllBytes(Paths.get(filePath)));
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/byteValue");
        field.setFieldType(FieldType.BYTE);
        AtlasInternalSession session = read(field);

        assertEquals(null, field.getValue());
        assertEquals(1, session.getAudits().getAudit().size());
        assertEquals("Failed to convert field value 'abcd' into type 'BYTE'", session.getAudits().getAudit().get(0).getMessage());
        assertEquals("abcd", session.getAudits().getAudit().get(0).getValue());
        assertEquals(AuditStatus.ERROR, session.getAudits().getAudit().get(0).getStatus());
    }
}
