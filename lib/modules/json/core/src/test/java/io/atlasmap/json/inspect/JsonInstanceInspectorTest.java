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
package io.atlasmap.json.inspect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.junit.jupiter.api.Test;

import io.atlasmap.json.v2.JsonComplexType;
import io.atlasmap.json.v2.JsonDocument;
import io.atlasmap.json.v2.JsonField;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldStatus;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.Fields;

/**
 */
public class JsonInstanceInspectorTest {

    private final JsonInspectionService inspectionService = new JsonInspectionService();

    @Test
    public void inspectJsonDocumentEmpty() throws Exception {
        final String instance = "";
        assertThrows(IllegalArgumentException.class, () -> {
            inspectionService.inspectJsonDocument(instance);
        });
    }

    @Test
    public void inspectJsonDocumentWhitespaceOnly() throws Exception {
        final String instance = " ";
        assertThrows(IllegalArgumentException.class, () -> {
            inspectionService.inspectJsonDocument(instance);
        });
    }

    @Test
    public void inspectJsonDocumentNull() throws Exception {
        final String instance = null;
        assertThrows(IllegalArgumentException.class, () -> {
            inspectionService.inspectJsonDocument(instance);
        });
    }

    @Test
    public void inspectJsonDocumentUnparseableHighlyComplexNestedObject() throws Exception {
        final String instance = new String(Files
                .readAllBytes(Paths.get("src/test/resources/inspect/unparseable-highly-complex-nested-object.json")));
        assertThrows(JsonInspectionException.class, () -> {
            inspectionService.inspectJsonDocument(instance);
        });
    }

    @Test
    public void inspectJsonDocumentUnparseableMissingOpenCurly() throws Exception {
        final String instance = "\"ads\":[{\"id_ad\":\"20439\"}, {\"id_ad\":\"20449\"}]}";
        assertThrows(JsonInspectionException.class, () -> {
            inspectionService.inspectJsonDocument(instance);
        });
    }

    @Test
    public void inspectJsonDocumentUnparseableMissingClosingCurly() throws Exception {
        final String instance = "{\"ads\":[{\"id_ad\":\"20439\"}, {\"id_ad\":\"20449\"}]";
        assertThrows(JsonInspectionException.class, () -> {
            inspectionService.inspectJsonDocument(instance);
        });
    }

    @Test
    public void inspectJsonDocumentUnparseableMissingKeySeperator() throws Exception {
        final String instance = "{\"ads\"[{\"id_ad\":\"20439\"}, {\"id_ad\":\"20449\"}]";
        assertThrows(JsonInspectionException.class, () -> {
            inspectionService.inspectJsonDocument(instance);
        });
    }

    @Test
    public void inspectJsonDocumentUnparseableMissingValueSeperator() throws Exception {
        final String instance = "{\"id_ad\":\"20439\" \"id_ad\":\"20449\"}";
        assertThrows(JsonInspectionException.class, () -> {
            inspectionService.inspectJsonDocument(instance);
        });
    }

    @Test
    public void inspectJsonDocumentEmptyDocument() throws Exception {
        final String instance = "{}";
        JsonDocument document = inspectionService.inspectJsonDocument(instance);
        assertNotNull(document);
        assertEquals(0, document.getFields().getField().size());
    }

    @Test
    public void inspectJsonDocumentSimpleArray() throws Exception {
        final String instance = "[ 100, 500, 300, 200, 400 ]";
        JsonDocument document = inspectionService.inspectJsonDocument(instance);
        assertNotNull(document);
        assertEquals(1, document.getFields().getField().size());
        JsonField jsonField = (JsonField) document.getFields().getField().get(0);
        assertEquals(FieldStatus.SUPPORTED, jsonField.getStatus());
        assertEquals(CollectionType.LIST, jsonField.getCollectionType());
        assertEquals(FieldType.INTEGER, jsonField.getFieldType());
        assertEquals("", jsonField.getName());
        assertEquals("/<>", jsonField.getPath());
        // printDocument(document);
    }

    @Test
    public void inspectJsonDocumentSimpleArrayStartsWithWhiteSpace() throws Exception {
        final String instance = "\n\t\r [ 100, 500, 300, 200, 400 ]";
        JsonDocument document = inspectionService.inspectJsonDocument(instance);
        assertNotNull(document);
        assertEquals(1, document.getFields().getField().size());
        JsonField jsonField = (JsonField) document.getFields().getField().get(0);
        assertEquals(FieldStatus.SUPPORTED, jsonField.getStatus());
        assertEquals(CollectionType.LIST, jsonField.getCollectionType());
        assertEquals(FieldType.INTEGER, jsonField.getFieldType());
        assertEquals("", jsonField.getName());
        assertEquals("/<>", jsonField.getPath());
    }

    @Test
    public void inspectJsonDocumentSimpleObjectArray() throws Exception {
        final String instance = "[\n" + "\t{\n" + "\t\t\"color\": \"red\",\n" + "\t\t\"value\": \"#f00\"\n" + "\t},\n"
                + "\t{\n" + "\t\t\"color\": \"green\",\n" + "\t\t\"value\": \"#0f0\"\n" + "\t},\n" + "\t{\n"
                + "\t\t\"color\": \"blue\",\n" + "\t\t\"value\": \"#00f\"\n" + "\t}]";
        JsonDocument document = inspectionService.inspectJsonDocument(instance);
        assertNotNull(document);
        assertEquals(1, document.getFields().getField().size());
        JsonField jsonField = (JsonField) document.getFields().getField().get(0);
        assertEquals(FieldStatus.SUPPORTED, jsonField.getStatus());
        assertEquals(CollectionType.LIST, jsonField.getCollectionType());
        assertEquals(FieldType.COMPLEX, jsonField.getFieldType());
        assertEquals("", jsonField.getName());
        assertEquals("/<>", jsonField.getPath());
        JsonComplexType complexType = (JsonComplexType)jsonField;
        assertEquals(2, complexType.getJsonFields().getJsonField().size());

        JsonField color = complexType.getJsonFields().getJsonField().get(0);
        assertEquals(FieldStatus.SUPPORTED, color.getStatus());
        assertEquals(null, color.getCollectionType());
        assertEquals(FieldType.STRING, color.getFieldType());
        assertEquals("color", color.getName());
        assertEquals("/<>/color", color.getPath());

        JsonField value = complexType.getJsonFields().getJsonField().get(1);
        assertEquals(FieldStatus.SUPPORTED, value.getStatus());
        assertEquals(null, value.getCollectionType());
        assertEquals(FieldType.STRING, value.getFieldType());
        assertEquals("value", value.getName());
        assertEquals("/<>/value", value.getPath());
        // printDocument(document);
    }

    @Test
    public void inspectJsonDocumentArrayHighlyNestedObjects() throws Exception {
        final String instance = new String(
                Files.readAllBytes(Paths.get("src/test/resources/inspect/array-highly-nested-objects.json")));
        JsonDocument document = inspectionService.inspectJsonDocument(instance);
        assertNotNull(document);
        assertEquals(1, document.getFields().getField().size());
        JsonField jsonField = (JsonField) document.getFields().getField().get(0);
        assertEquals(FieldStatus.SUPPORTED, jsonField.getStatus());
        assertEquals(CollectionType.LIST, jsonField.getCollectionType());
        assertEquals(FieldType.COMPLEX, jsonField.getFieldType());
        JsonComplexType complex = (JsonComplexType)jsonField;
        assertEquals(6, complex.getJsonFields().getJsonField().size());

        JsonField id = complex.getJsonFields().getJsonField().get(0);
        assertEquals(FieldStatus.SUPPORTED, id.getStatus());
        assertEquals(null, id.getCollectionType());
        assertEquals(FieldType.STRING, id.getFieldType());
        assertEquals("id", id.getName());
        assertEquals("/<>/id", id.getPath());

        JsonField type = complex.getJsonFields().getJsonField().get(1);
        assertEquals(FieldStatus.SUPPORTED, type.getStatus());
        assertEquals(null, type.getCollectionType());
        assertEquals(FieldType.STRING, type.getFieldType());
        assertEquals("type", type.getName());
        assertEquals("/<>/type", type.getPath());

        JsonField name = complex.getJsonFields().getJsonField().get(2);
        assertEquals(FieldStatus.SUPPORTED, name.getStatus());
        assertEquals(null, name.getCollectionType());
        assertEquals(FieldType.STRING, name.getFieldType());
        assertEquals("name", name.getName());
        assertEquals("/<>/name", name.getPath());

        JsonField ppu = complex.getJsonFields().getJsonField().get(3);
        assertEquals(FieldStatus.SUPPORTED, ppu.getStatus());
        assertEquals(null, ppu.getCollectionType());
        assertEquals(FieldType.DOUBLE, ppu.getFieldType());
        assertEquals("ppu", ppu.getName());
        assertEquals("/<>/ppu", ppu.getPath());

        JsonField batters = complex.getJsonFields().getJsonField().get(4);
        assertEquals(FieldStatus.SUPPORTED, batters.getStatus());
        assertEquals(null, batters.getCollectionType());
        assertEquals(FieldType.COMPLEX, batters.getFieldType());
        assertEquals("batters", batters.getName());
        assertEquals("/<>/batters", batters.getPath());
        JsonComplexType battersComplex = (JsonComplexType)batters;
        assertEquals(1, battersComplex.getJsonFields().getJsonField().size());

        JsonField batter = battersComplex.getJsonFields().getJsonField().get(0);
        assertEquals(FieldStatus.SUPPORTED, batter.getStatus());
        assertEquals(CollectionType.LIST, batter.getCollectionType());
        assertEquals(FieldType.COMPLEX, batter.getFieldType());
        assertEquals("batter", batter.getName());
        assertEquals("/<>/batters/batter<>", batter.getPath());
        JsonComplexType batterComplex = (JsonComplexType)batter;
        assertEquals(2, batterComplex.getJsonFields().getJsonField().size());

        JsonField batterId = batterComplex.getJsonFields().getJsonField().get(0);
        assertEquals(FieldStatus.SUPPORTED, batterId.getStatus());
        assertEquals(null, batterId.getCollectionType());
        assertEquals(FieldType.STRING, batterId.getFieldType());
        assertEquals("id", batterId.getName());
        assertEquals("/<>/batters/batter<>/id", batterId.getPath());

        JsonField batterType = batterComplex.getJsonFields().getJsonField().get(1);
        assertEquals(FieldStatus.SUPPORTED, batterType.getStatus());
        assertEquals(null, batterType.getCollectionType());
        assertEquals(FieldType.STRING, batterType.getFieldType());
        assertEquals("type", batterType.getName());
        assertEquals("/<>/batters/batter<>/type", batterType.getPath());

        JsonField topping = complex.getJsonFields().getJsonField().get(5);
        assertEquals(FieldStatus.SUPPORTED, topping.getStatus());
        assertEquals(CollectionType.LIST, topping.getCollectionType());
        assertEquals(FieldType.COMPLEX, topping.getFieldType());
        assertEquals("topping", topping.getName());
        assertEquals("/<>/topping<>", topping.getPath());
        JsonComplexType toppingComplex = (JsonComplexType)topping;
        assertEquals(2, toppingComplex.getJsonFields().getJsonField().size());

        JsonField toppingId = toppingComplex.getJsonFields().getJsonField().get(0);
        assertEquals(FieldStatus.SUPPORTED, toppingId.getStatus());
        assertEquals(null, toppingId.getCollectionType());
        assertEquals(FieldType.STRING, toppingId.getFieldType());
        assertEquals("id", toppingId.getName());
        assertEquals("/<>/topping<>/id", toppingId.getPath());

        JsonField toppingType = toppingComplex.getJsonFields().getJsonField().get(1);
        assertEquals(FieldStatus.SUPPORTED, toppingType.getStatus());
        assertEquals(null, toppingType.getCollectionType());
        assertEquals(FieldType.STRING, toppingType.getFieldType());
        assertEquals("type", toppingType.getName());
        assertEquals("/<>/topping<>/type", toppingType.getPath());
    }

    @Test
    public void inspectJsonDocumentEscapedCharsInKeys() throws Exception {
        final String instance = new String(
                Files.readAllBytes(Paths.get("src/test/resources/inspect/keys-with-escaped-characters.json")));
        JsonDocument document = inspectionService.inspectJsonDocument(instance);
        assertNotNull(document);
        assertEquals(7, document.getFields().getField().size());
        for (int i = 0; i < document.getFields().getField().size(); i++) {
            JsonField field = (JsonField) document.getFields().getField().get(i);
            if (i == 0) {
                assertEquals("'booleanField'", field.getName());
                assertEquals(false, field.getValue());
                assertEquals("/".concat(field.getName()), field.getPath());
                assertEquals(FieldType.BOOLEAN, field.getFieldType());
                assertEquals(FieldStatus.SUPPORTED, field.getStatus());
            } else if (i == 1) {
                assertEquals("\"charField\"", field.getName());
                assertEquals("a", field.getValue());
                assertEquals("/".concat(field.getName()), field.getPath());
                assertEquals(FieldType.STRING, field.getFieldType());
                assertEquals(FieldStatus.SUPPORTED, field.getStatus());
            } else if (i == 2) {
                assertEquals("\\doubleField", field.getName());
                assertEquals(-27152745.3422, field.getValue());
                assertEquals("/".concat(field.getName()), field.getPath());
                assertEquals(FieldType.DOUBLE, field.getFieldType());
                assertEquals(FieldStatus.SUPPORTED, field.getStatus());
            } else if (i == 3) {
                assertEquals("floatField\t", field.getName());
                assertEquals(-63988281.00, field.getValue());
                assertEquals("/".concat(field.getName()), field.getPath());
                assertEquals(FieldType.DOUBLE, field.getFieldType());
                assertEquals(FieldStatus.SUPPORTED, field.getStatus());
            } else if (i == 4) {
                assertEquals("intField\n", field.getName());
                assertEquals(8281, field.getValue());
                assertEquals("/".concat(field.getName()), field.getPath());
                assertEquals(FieldType.INTEGER, field.getFieldType());
                assertEquals(FieldStatus.SUPPORTED, field.getStatus());
            } else if (i == 5) {
                assertEquals("shortField", field.getName());
                assertEquals(81, field.getValue());
                assertEquals("/".concat(field.getName()), field.getPath());
                assertEquals(FieldType.INTEGER, field.getFieldType());
                assertEquals(FieldStatus.SUPPORTED, field.getStatus());
            } else if (i == 6) {
                assertEquals("longField", field.getName());
                assertEquals(3988281, field.getValue());
                assertEquals("/".concat(field.getName()), field.getPath());
                assertEquals(FieldType.INTEGER, field.getFieldType());
                assertEquals(FieldStatus.SUPPORTED, field.getStatus());
            }
        }
        // printDocument(document);
    }

    @Test
    public void inspectJsonDocumentEscapedCharsInValue() throws Exception {
        final String instance = new String(
                Files.readAllBytes(Paths.get("src/test/resources/inspect/value-with-escaped-characters.json")));
        JsonDocument document = inspectionService.inspectJsonDocument(instance);
        assertNotNull(document);
        assertEquals(5, document.getFields().getField().size());
        for (int i = 0; i < document.getFields().getField().size(); i++) {
            JsonField field = (JsonField) document.getFields().getField().get(i);
            if (i == 0) {
                assertEquals("quote", field.getName());
                assertEquals("\"yadda\"", field.getValue());
                assertEquals("/".concat(field.getName()), field.getPath());
                assertEquals(FieldType.STRING, field.getFieldType());
                assertEquals(FieldStatus.SUPPORTED, field.getStatus());
            } else if (i == 1) {
                assertEquals("singlequote", field.getName());
                assertEquals("'a'", field.getValue());
                assertEquals("/".concat(field.getName()), field.getPath());
                assertEquals(FieldType.STRING, field.getFieldType());
                assertEquals(FieldStatus.SUPPORTED, field.getStatus());
            } else if (i == 2) {
                assertEquals("backslash", field.getName());
                assertEquals("\\qwerty", field.getValue());
                assertEquals("/".concat(field.getName()), field.getPath());
                assertEquals(FieldType.STRING, field.getFieldType());
                assertEquals(FieldStatus.SUPPORTED, field.getStatus());
            } else if (i == 3) {
                assertEquals("tab", field.getName());
                assertEquals("foo\t", field.getValue());
                assertEquals("/".concat(field.getName()), field.getPath());
                assertEquals(FieldType.STRING, field.getFieldType());
                assertEquals(FieldStatus.SUPPORTED, field.getStatus());
            } else if (i == 4) {
                assertEquals("linefeed", field.getName());
                assertEquals("bar\n", field.getValue());
                assertEquals("/".concat(field.getName()), field.getPath());
                assertEquals(FieldType.STRING, field.getFieldType());
                assertEquals(FieldStatus.SUPPORTED, field.getStatus());
            }
        }
        // printDocument(document);
    }

    // FlatPrimitive
    @Test
    public void inspectFlatPrimitiveNoRoot() throws Exception {
        final String instance = new String(
                Files.readAllBytes(Paths.get("src/test/resources/inspect/flatprimitive-base-unrooted.json")));
        JsonDocument document = inspectionService.inspectJsonDocument(instance);
        assertNotNull(document);
        assertEquals(7, document.getFields().getField().size());
        for (int i = 0; i < document.getFields().getField().size(); i++) {
            JsonField field = (JsonField) document.getFields().getField().get(i);
            if (i == 0) {
                assertEquals("booleanField", field.getName());
                assertEquals(false, field.getValue());
                assertEquals("/".concat(field.getName()), field.getPath());
                assertEquals(FieldType.BOOLEAN, field.getFieldType());
                assertEquals(FieldStatus.SUPPORTED, field.getStatus());
            } else if (i == 1) {
                assertEquals("charField", field.getName());
                assertEquals("a", field.getValue());
                assertEquals("/".concat(field.getName()), field.getPath());
                // FIXME DOES NOT RECOGNIZE CHAR DISTINCTLY AND RETURNS THIS AS A STRING
                // (TEXTUAL)
                assertEquals(FieldType.STRING, field.getFieldType());
                assertEquals(FieldStatus.SUPPORTED, field.getStatus());
            } else if (i == 2) {
                assertEquals("doubleField", field.getName());
                assertEquals(-27152745.3422, field.getValue());
                assertEquals("/".concat(field.getName()), field.getPath());
                assertEquals(FieldType.DOUBLE, field.getFieldType());
                assertEquals(FieldStatus.SUPPORTED, field.getStatus());
            } else if (i == 3) {
                assertEquals("floatField", field.getName());
                assertEquals(-63988281.00, field.getValue());
                assertEquals("/".concat(field.getName()), field.getPath());
                // FIXME DOES NOT RECOGNIZE FLOAT DISTINCTLY AND RETURNS THIS AS A DOUBLE
                assertEquals(FieldType.DOUBLE, field.getFieldType());
                assertEquals(FieldStatus.SUPPORTED, field.getStatus());
            } else if (i == 4) {
                assertEquals("intField", field.getName());
                assertEquals(8281, field.getValue());
                assertEquals("/".concat(field.getName()), field.getPath());
                assertEquals(FieldType.INTEGER, field.getFieldType());
                assertEquals(FieldStatus.SUPPORTED, field.getStatus());
            } else if (i == 5) {
                assertEquals("shortField", field.getName());
                assertEquals(81, field.getValue());
                assertEquals("/".concat(field.getName()), field.getPath());
                // FIXME JSON DOES NOT RECOGNIZE SHORT DISTINCTLY AND RETURNS THIS AS A INTEGER
                assertEquals(FieldType.INTEGER, field.getFieldType());
                assertEquals(FieldStatus.SUPPORTED, field.getStatus());
            } else if (i == 6) {
                assertEquals("longField", field.getName());
                assertEquals(3988281, field.getValue());
                assertEquals("/".concat(field.getName()), field.getPath());
                // FIXME JSON DOES NOT RECOGNIZE LONG DISTINCTLY AND RETURNS THIS AS A INTEGER
                assertEquals(FieldType.INTEGER, field.getFieldType());
                assertEquals(FieldStatus.SUPPORTED, field.getStatus());
            }
        }
    }

    @Test
    public void inspectFlatPrimitiveWithRoot() throws Exception {
        final String instance = new String(
                Files.readAllBytes(Paths.get("src/test/resources/inspect/flatprimitive-base-rooted.json")));
        JsonDocument document = inspectionService.inspectJsonDocument(instance);
        assertNotNull(document);
        assertEquals(1, document.getFields().getField().size());
        JsonComplexType root = (JsonComplexType) document.getFields().getField().get(0);
        assertNotNull(root);
        assertEquals("SourceFlatPrimitive", root.getName());
        assertEquals("/SourceFlatPrimitive", root.getPath());
        assertEquals(FieldType.COMPLEX, root.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, root.getStatus());

        assertEquals(8, root.getJsonFields().getJsonField().size());
        for (int i = 0; i < root.getJsonFields().getJsonField().size(); i++) {
            JsonField field = root.getJsonFields().getJsonField().get(i);
            if (i == 0) {
                assertEquals("booleanField", field.getName());
                assertEquals(false, field.getValue());
                assertEquals("/SourceFlatPrimitive/".concat(field.getName()), field.getPath());
                assertEquals(FieldType.BOOLEAN, field.getFieldType());
                assertEquals(FieldStatus.SUPPORTED, field.getStatus());
            } else if (i == 1) {
                assertEquals("charField", field.getName());
                assertEquals("a", field.getValue());
                assertEquals("/SourceFlatPrimitive/".concat(field.getName()), field.getPath());
                assertEquals(FieldType.STRING, field.getFieldType());
                assertEquals(FieldStatus.SUPPORTED, field.getStatus());
            } else if (i == 2) {
                assertEquals("doubleField", field.getName());
                assertEquals(-27152745.3422, field.getValue());
                assertEquals("/SourceFlatPrimitive/".concat(field.getName()), field.getPath());
                assertEquals(FieldType.DOUBLE, field.getFieldType());
                assertEquals(FieldStatus.SUPPORTED, field.getStatus());
            } else if (i == 3) {
                assertEquals("floatField", field.getName());
                assertEquals(-63988281.00, field.getValue());
                assertEquals("/SourceFlatPrimitive/".concat(field.getName()), field.getPath());
                assertEquals(FieldType.DOUBLE, field.getFieldType());
                assertEquals(FieldStatus.SUPPORTED, field.getStatus());
            } else if (i == 4) {
                assertEquals("intField", field.getName());
                assertEquals(8281, field.getValue());
                assertEquals("/SourceFlatPrimitive/".concat(field.getName()), field.getPath());
                assertEquals(FieldType.INTEGER, field.getFieldType());
                assertEquals(FieldStatus.SUPPORTED, field.getStatus());
            } else if (i == 5) {
                assertEquals("shortField", field.getName());
                assertEquals(81, field.getValue());
                assertEquals("/SourceFlatPrimitive/".concat(field.getName()), field.getPath());
                assertEquals(FieldType.INTEGER, field.getFieldType());
                assertEquals(FieldStatus.SUPPORTED, field.getStatus());
            } else if (i == 6) {
                assertEquals("longField", field.getName());
                assertEquals(3988281, field.getValue());
                assertEquals("/SourceFlatPrimitive/".concat(field.getName()), field.getPath());
                assertEquals(FieldType.INTEGER, field.getFieldType());
                assertEquals(FieldStatus.SUPPORTED, field.getStatus());
            }
        }
    }

    @Test
    public void inspectComplexObjectNoRoot() throws Exception {
        final String instance = new String(
                Files.readAllBytes(Paths.get("src/test/resources/inspect/complex-object-unrooted.json")));
        JsonDocument document = inspectionService.inspectJsonDocument(instance);
        assertNotNull(document);
        assertEquals(3, document.getFields().getField().size());

        JsonComplexType address = (JsonComplexType) document.getFields().getField().get(0);
        assertNotNull(address);
        assertEquals(5, address.getJsonFields().getJsonField().size());

        JsonField address1 = address.getJsonFields().getJsonField().get(0);
        assertNotNull(address1);
        assertEquals("addressLine1", address1.getName());
        assertEquals("123 Main St", address1.getValue());
        assertEquals("/address/addressLine1", address1.getPath());
        assertEquals(FieldType.STRING, address1.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, address1.getStatus());

        JsonField address2 = address.getJsonFields().getJsonField().get(1);
        assertNotNull(address2);
        assertEquals("addressLine2", address2.getName());
        assertEquals("Suite 42b", address2.getValue());
        assertEquals("/address/addressLine2", address2.getPath());
        assertEquals(FieldType.STRING, address2.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, address2.getStatus());

        JsonField city = address.getJsonFields().getJsonField().get(2);
        assertNotNull(city);
        assertEquals("city", city.getName());
        assertEquals("Anytown", city.getValue());
        assertEquals("/address/city", city.getPath());
        assertEquals(FieldType.STRING, city.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, city.getStatus());

        JsonField state = address.getJsonFields().getJsonField().get(3);
        assertNotNull(state);
        assertEquals("state", state.getName());
        assertEquals("NY", state.getValue());
        assertEquals("/address/state", state.getPath());
        assertEquals(FieldType.STRING, state.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, state.getStatus());

        JsonField postalCode = address.getJsonFields().getJsonField().get(4);
        assertNotNull(postalCode);
        assertEquals("zipCode", postalCode.getName());
        assertEquals("90210", postalCode.getValue());
        assertEquals("/address/zipCode", postalCode.getPath());
        assertEquals(FieldType.STRING, postalCode.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, postalCode.getStatus());

        JsonComplexType contact = (JsonComplexType) document.getFields().getField().get(1);
        assertNotNull(contact);
        assertEquals(4, contact.getJsonFields().getJsonField().size());

        JsonField firstName = contact.getJsonFields().getJsonField().get(0);
        assertNotNull(firstName);
        assertEquals("firstName", firstName.getName());
        assertEquals("Ozzie", firstName.getValue());
        assertEquals("/contact/firstName", firstName.getPath());
        assertEquals(FieldType.STRING, firstName.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, firstName.getStatus());

        JsonField lastName = contact.getJsonFields().getJsonField().get(1);
        assertNotNull(lastName);
        assertEquals("lastName", lastName.getName());
        assertEquals("Smith", lastName.getValue());
        assertEquals("/contact/lastName", lastName.getPath());
        assertEquals(FieldType.STRING, lastName.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, lastName.getStatus());

        JsonField phoneNumber = contact.getJsonFields().getJsonField().get(2);
        assertNotNull(phoneNumber);
        assertEquals("phoneNumber", phoneNumber.getName());
        assertEquals("5551212", phoneNumber.getValue());
        assertEquals("/contact/phoneNumber", phoneNumber.getPath());
        assertEquals(FieldType.STRING, phoneNumber.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, phoneNumber.getStatus());

        JsonField zipCode = contact.getJsonFields().getJsonField().get(3);
        assertNotNull(zipCode);
        assertEquals("zipCode", zipCode.getName());
        assertEquals("81111", zipCode.getValue());
        assertEquals("/contact/zipCode", zipCode.getPath());
        assertEquals(FieldType.STRING, zipCode.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, zipCode.getStatus());

        JsonField orderId = (JsonField) document.getFields().getField().get(2);
        assertNotNull(orderId);
        assertEquals("orderId", orderId.getName());
        assertEquals(0, orderId.getValue());
        assertEquals("/orderId", orderId.getPath());
        assertEquals(FieldType.INTEGER, orderId.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, orderId.getStatus());
    }

    @Test
    public void inspectComplexObjectWithRoot() throws Exception {
        final String instance = new String(
                Files.readAllBytes(Paths.get("src/test/resources/inspect/complex-object-rooted.json")));
        JsonDocument document = inspectionService.inspectJsonDocument(instance);
        assertNotNull(document);
        assertEquals(1, document.getFields().getField().size());

        JsonComplexType root = (JsonComplexType) document.getFields().getField().get(0);
        assertEquals(3, root.getJsonFields().getJsonField().size());

        JsonComplexType address = (JsonComplexType) root.getJsonFields().getJsonField().get(0);
        assertNotNull(address);
        assertEquals(5, address.getJsonFields().getJsonField().size());

        JsonField address1 = address.getJsonFields().getJsonField().get(0);
        assertNotNull(address1);
        assertEquals("addressLine1", address1.getName());
        assertEquals("123 Main St", address1.getValue());
        assertEquals("/order/address/addressLine1", address1.getPath());
        assertEquals(FieldType.STRING, address1.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, address1.getStatus());

        JsonField address2 = address.getJsonFields().getJsonField().get(1);
        assertNotNull(address2);
        assertEquals("addressLine2", address2.getName());
        assertEquals("Suite 42b", address2.getValue());
        assertEquals("/order/address/addressLine2", address2.getPath());
        assertEquals(FieldType.STRING, address2.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, address2.getStatus());

        JsonField city = address.getJsonFields().getJsonField().get(2);
        assertNotNull(city);
        assertEquals("city", city.getName());
        assertEquals("Anytown", city.getValue());
        assertEquals("/order/address/city", city.getPath());
        assertEquals(FieldType.STRING, city.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, city.getStatus());

        JsonField state = address.getJsonFields().getJsonField().get(3);
        assertNotNull(state);
        assertEquals("state", state.getName());
        assertEquals("NY", state.getValue());
        assertEquals("/order/address/state", state.getPath());
        assertEquals(FieldType.STRING, state.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, state.getStatus());

        JsonField postalCode = address.getJsonFields().getJsonField().get(4);
        assertNotNull(postalCode);
        assertEquals("zipCode", postalCode.getName());
        assertEquals("90210", postalCode.getValue());
        assertEquals("/order/address/zipCode", postalCode.getPath());
        assertEquals(FieldType.STRING, postalCode.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, postalCode.getStatus());

        JsonComplexType contact = (JsonComplexType) root.getJsonFields().getJsonField().get(1);
        assertNotNull(contact);
        assertEquals(4, contact.getJsonFields().getJsonField().size());

        JsonField firstName = contact.getJsonFields().getJsonField().get(0);
        assertNotNull(firstName);
        assertEquals("firstName", firstName.getName());
        assertEquals("Ozzie", firstName.getValue());
        assertEquals("/order/contact/firstName", firstName.getPath());
        assertEquals(FieldType.STRING, firstName.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, firstName.getStatus());

        JsonField lastName = contact.getJsonFields().getJsonField().get(1);
        assertNotNull(lastName);
        assertEquals("lastName", lastName.getName());
        assertEquals("Smith", lastName.getValue());
        assertEquals("/order/contact/lastName", lastName.getPath());
        assertEquals(FieldType.STRING, lastName.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, lastName.getStatus());

        JsonField phoneNumber = contact.getJsonFields().getJsonField().get(2);
        assertNotNull(phoneNumber);
        assertEquals("phoneNumber", phoneNumber.getName());
        assertEquals("5551212", phoneNumber.getValue());
        assertEquals("/order/contact/phoneNumber", phoneNumber.getPath());
        assertEquals(FieldType.STRING, phoneNumber.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, phoneNumber.getStatus());

        JsonField zipCode = contact.getJsonFields().getJsonField().get(3);
        assertNotNull(zipCode);
        assertEquals("zipCode", zipCode.getName());
        assertEquals("81111", zipCode.getValue());
        assertEquals("/order/contact/zipCode", zipCode.getPath());
        assertEquals(FieldType.STRING, zipCode.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, zipCode.getStatus());

        JsonField orderId = root.getJsonFields().getJsonField().get(2);
        assertNotNull(orderId);
        assertEquals("orderId", orderId.getName());
        assertEquals(0, orderId.getValue());
        assertEquals("/order/orderId", orderId.getPath());
        assertEquals(FieldType.INTEGER, orderId.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, orderId.getStatus());
    }

    @Test
    public void inspectRepeatingComplexObjectWithRoot() throws Exception {
        final String instance = new String(
                Files.readAllBytes(Paths.get("src/test/resources/inspect/complex-repeated-rooted.json")));
        JsonDocument document = inspectionService.inspectJsonDocument(instance);
        assertNotNull(document);
        assertEquals(1, document.getFields().getField().size());
        JsonComplexType root = (JsonComplexType) document.getFields().getField().get(0);
        assertNotNull(root);
        assertEquals(3, root.getJsonFields().getJsonField().size());
        assertEquals("SourceOrderList", root.getName());
        assertEquals("/SourceOrderList", root.getPath());
        assertEquals(FieldType.COMPLEX, root.getFieldType());
        assertEquals(null, root.getCollectionType());

        JsonComplexType orders = (JsonComplexType) root.getJsonFields().getJsonField().get(0);
        assertNotNull(orders);
        assertEquals(3, orders.getJsonFields().getJsonField().size());
        assertEquals("orders", orders.getName());
        assertEquals("/SourceOrderList/orders<>", orders.getPath());
        assertEquals(FieldType.COMPLEX, orders.getFieldType());
        assertEquals(CollectionType.LIST, orders.getCollectionType());

        JsonField orderBatchNumber = root.getJsonFields().getJsonField().get(1);
        assertNotNull(orderBatchNumber);
        assertEquals("orderBatchNumber", orderBatchNumber.getName());
        assertEquals(4123562, orderBatchNumber.getValue());
        assertEquals("/SourceOrderList/orderBatchNumber", orderBatchNumber.getPath());
        assertEquals(FieldType.INTEGER, orderBatchNumber.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, orderBatchNumber.getStatus());

        JsonField numberOrders = root.getJsonFields().getJsonField().get(2);
        assertNotNull(numberOrders);
        assertEquals("numberOrders", numberOrders.getName());
        assertEquals(5, numberOrders.getValue());
        assertEquals("/SourceOrderList/numberOrders", numberOrders.getPath());
        assertEquals(FieldType.INTEGER, numberOrders.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, numberOrders.getStatus());

        JsonComplexType address = (JsonComplexType) orders.getJsonFields().getJsonField().get(0);
        assertNotNull(address);
        assertEquals(5, address.getJsonFields().getJsonField().size());
        assertEquals(null, address.getCollectionType());
        assertEquals(FieldType.COMPLEX, address.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, address.getStatus());
        assertEquals("address", address.getName());
        assertEquals("/SourceOrderList/orders<>/address", address.getPath());

        JsonField addressLine1 = address.getJsonFields().getJsonField().get(0);
        assertNotNull(addressLine1);
        assertEquals("addressLine1", addressLine1.getName());
        assertEquals("/SourceOrderList/orders<>/address/addressLine1", addressLine1.getPath());
        assertEquals(FieldType.STRING, addressLine1.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, addressLine1.getStatus());

        JsonField addressLine2 = address.getJsonFields().getJsonField().get(1);
        assertNotNull(addressLine2);
        assertEquals("addressLine2", addressLine2.getName());
        assertEquals("/SourceOrderList/orders<>/address/addressLine2", addressLine2.getPath());
        assertEquals(FieldType.STRING, addressLine2.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, addressLine2.getStatus());

        JsonField city = address.getJsonFields().getJsonField().get(2);
        assertNotNull(city);
        assertEquals("city", city.getName());
        assertEquals("/SourceOrderList/orders<>/address/city", city.getPath());
        assertEquals(FieldType.STRING, city.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, city.getStatus());

        JsonField state = address.getJsonFields().getJsonField().get(3);
        assertNotNull(state);
        assertEquals("state", state.getName());
        assertEquals("/SourceOrderList/orders<>/address/state", state.getPath());
        assertEquals(FieldType.STRING, state.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, state.getStatus());

        JsonField postalCode = address.getJsonFields().getJsonField().get(4);
        assertNotNull(postalCode);
        assertEquals("zipCode", postalCode.getName());
        assertEquals("/SourceOrderList/orders<>/address/zipCode", postalCode.getPath());
        assertEquals(FieldType.STRING, postalCode.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, postalCode.getStatus());

        JsonComplexType contact = (JsonComplexType) orders.getJsonFields().getJsonField().get(1);
        assertNotNull(contact);
        assertEquals(4, contact.getJsonFields().getJsonField().size());
        assertEquals(FieldType.COMPLEX, contact.getFieldType());
        assertEquals(null, contact.getCollectionType());
        assertEquals(FieldStatus.SUPPORTED, contact.getStatus());
        assertEquals("contact", contact.getName());
        assertEquals("/SourceOrderList/orders<>/contact", contact.getPath());

        JsonField firstName = contact.getJsonFields().getJsonField().get(0);
        assertNotNull(firstName);
        assertEquals("firstName", firstName.getName());
        assertEquals("/SourceOrderList/orders<>/contact/firstName", firstName.getPath());
        assertEquals(FieldType.STRING, firstName.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, firstName.getStatus());

        JsonField lastName = contact.getJsonFields().getJsonField().get(1);
        assertNotNull(lastName);
        assertEquals("lastName", lastName.getName());
        assertEquals("/SourceOrderList/orders<>/contact/lastName", lastName.getPath());
        assertEquals(FieldType.STRING, lastName.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, lastName.getStatus());

        JsonField phoneNumber = contact.getJsonFields().getJsonField().get(2);
        assertNotNull(phoneNumber);
        assertEquals("phoneNumber", phoneNumber.getName());
        assertEquals("/SourceOrderList/orders<>/contact/phoneNumber", phoneNumber.getPath());
        assertEquals(FieldType.STRING, phoneNumber.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, phoneNumber.getStatus());

        JsonField zipCode = contact.getJsonFields().getJsonField().get(3);
        assertNotNull(zipCode);
        assertEquals("zipCode", zipCode.getName());
        assertEquals("/SourceOrderList/orders<>/contact/zipCode", zipCode.getPath());
        assertEquals(FieldType.STRING, zipCode.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, zipCode.getStatus());
    }

    @Test
    public void inspectISO8601DatesNoRoot() throws Exception {
        final String instance = new String(
                Files.readAllBytes(Paths.get("src/test/resources/inspect/iso8601dates-unrooted.json")));
        JsonDocument document = inspectionService.inspectJsonDocument(instance);
        assertNotNull(document);
        assertEquals(7, document.getFields().getField().size());

        JsonField yyyy = (JsonField) document.getFields().getField().get(0);
        assertNotNull(yyyy);
        assertEquals("YYYY", yyyy.getName());
        assertEquals("1997", yyyy.getValue());
        assertEquals("/YYYY", yyyy.getPath());
        assertEquals(FieldType.STRING, yyyy.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, yyyy.getStatus());

        JsonField yyyymm = (JsonField) document.getFields().getField().get(1);
        assertNotNull(yyyymm);
        assertEquals("YYYY-MM", yyyymm.getName());
        assertEquals("1997-07", yyyymm.getValue());
        assertEquals("/YYYY-MM", yyyymm.getPath());
        assertEquals(FieldType.STRING, yyyymm.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, yyyymm.getStatus());

        JsonField yyyymmdd = (JsonField) document.getFields().getField().get(2);
        assertNotNull(yyyymmdd);
        assertEquals("YYYY-MM-DD", yyyymmdd.getName());
        assertEquals("1997-07-16", yyyymmdd.getValue());
        assertEquals("/YYYY-MM-DD", yyyymmdd.getPath());
        assertEquals(FieldType.STRING, yyyymmdd.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, yyyymmdd.getStatus());

        JsonField yyyymmddthhmmtzd = (JsonField) document.getFields().getField().get(3);
        assertNotNull(yyyymmddthhmmtzd);
        assertEquals("YYYY-MM-DDThh:mmTZD", yyyymmddthhmmtzd.getName());
        assertEquals("1997-07-16T19:20+01:00", yyyymmddthhmmtzd.getValue());
        assertEquals("/YYYY-MM-DDThh:mmTZD", yyyymmddthhmmtzd.getPath());
        assertEquals(FieldType.STRING, yyyymmddthhmmtzd.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, yyyymmddthhmmtzd.getStatus());

        JsonField yyyymmddthhmmsstzd = (JsonField) document.getFields().getField().get(4);
        assertNotNull(yyyymmddthhmmsstzd);
        assertEquals("YYYY-MM-DDThh:mm:ssTZD", yyyymmddthhmmsstzd.getName());
        assertEquals("1997-07-16T19:20:30+01:00", yyyymmddthhmmsstzd.getValue());
        assertEquals("/YYYY-MM-DDThh:mm:ssTZD", yyyymmddthhmmsstzd.getPath());
        assertEquals(FieldType.STRING, yyyymmddthhmmsstzd.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, yyyymmddthhmmsstzd.getStatus());

        JsonField yyyymmddthhmmssstzd = (JsonField) document.getFields().getField().get(5);
        assertNotNull(yyyymmddthhmmssstzd);
        assertEquals("YYYY-MM-DDThh:mm:ss.sTZD", yyyymmddthhmmssstzd.getName());
        assertEquals("1997-07-16T19:20:30.45+01:00", yyyymmddthhmmssstzd.getValue());
        assertEquals("/YYYY-MM-DDThh:mm:ss.sTZD", yyyymmddthhmmssstzd.getPath());
        assertEquals(FieldType.STRING, yyyymmddthhmmssstzd.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, yyyymmddthhmmssstzd.getStatus());

        JsonField yyyymmddthhmmssutz = (JsonField) document.getFields().getField().get(6);
        assertNotNull(yyyymmddthhmmssutz);
        assertEquals("YYYY-MM-DDThh:mm:ssUTZ", yyyymmddthhmmssutz.getName());
        assertEquals("1994-11-05T13:15:30Z", yyyymmddthhmmssutz.getValue());
        assertEquals("/YYYY-MM-DDThh:mm:ssUTZ", yyyymmddthhmmssutz.getPath());
        assertEquals(FieldType.STRING, yyyymmddthhmmssutz.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, yyyymmddthhmmssutz.getStatus());

    }

    @Test
    public void inspectJsonDocumentNoRoot() throws Exception {
        final String instance = "{ \"brand\" : \"Mercedes\", \"doors\" : 5 }";
        JsonDocument document = inspectionService.inspectJsonDocument(instance);
        assertNotNull(document);
        assertEquals(2, document.getFields().getField().size());

        JsonField field1 = (JsonField) document.getFields().getField().get(0);
        assertNotNull(field1);
        assertEquals("brand", field1.getName());
        assertEquals("Mercedes", field1.getValue());
        assertEquals("/brand", field1.getPath());
        assertEquals(FieldType.STRING, field1.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, field1.getStatus());

        JsonField field2 = (JsonField) document.getFields().getField().get(1);
        assertNotNull(field2);
        assertEquals("doors", field2.getName());
        assertEquals(5, field2.getValue());
        assertEquals("/doors", field2.getPath());
        assertEquals(FieldType.INTEGER, field2.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, field2.getStatus());
        // printDocument(document);
    }

    @Test
    public void inspectJsonDocumentWithRoot() throws Exception {
        final String instance = "{\"car\" :{ \"brand\" : \"Mercedes\", \"doors\" : 5 } }";
        JsonDocument document = inspectionService.inspectJsonDocument(instance);
        assertNotNull(document);
        assertEquals(1, document.getFields().getField().size());
        JsonComplexType car = (JsonComplexType) document.getFields().getField().get(0);
        assertNotNull(car);
        assertEquals("car", car.getName());
        assertEquals(FieldType.COMPLEX, car.getFieldType());
        assertEquals("/car", car.getPath());
        assertEquals(FieldStatus.SUPPORTED, car.getStatus());
        assertEquals(2, car.getJsonFields().getJsonField().size());

        JsonField field1 = car.getJsonFields().getJsonField().get(0);
        assertNotNull(field1);
        assertEquals("brand", field1.getName());
        assertEquals("Mercedes", field1.getValue());
        assertEquals("/car/brand", field1.getPath());
        assertEquals(FieldType.STRING, field1.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, field1.getStatus());

        JsonField field2 = car.getJsonFields().getJsonField().get(1);
        assertNotNull(field2);
        assertEquals("doors", field2.getName());
        assertEquals(5, field2.getValue());
        assertEquals("/car/doors", field2.getPath());
        assertEquals(FieldType.INTEGER, field2.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, field2.getStatus());
        // printDocument(document);
    }

    @Test
    public void inspectJsonDocumentNestedObjectArray() throws Exception {
        final String instance = "{\"menu\": {\n" + "  \"id\": \"file\",\n" + "  \"value\": \"Filed\",\n"
                + "  \"popup\": {\n" + "    \"menuitem\": [\n"
                + "      {\"value\": \"New\", \"onclick\": \"CreateNewDoc()\"},\n"
                + "      {\"value\": \"Open\", \"onclick\": \"OpenDoc()\"},\n"
                + "      {\"value\": \"Close\", \"onclick\": \"CloseDoc()\"}\n" + "    ]\n" + "  }\n" + "}}";
        JsonDocument document = inspectionService.inspectJsonDocument(instance);
        assertNotNull(document);
        assertEquals(1, document.getFields().getField().size());
        assertNotNull(document.getFields().getField());

        JsonComplexType jsonComplexType = (JsonComplexType) document.getFields().getField().get(0);
        assertNotNull(jsonComplexType);
        assertNotNull(jsonComplexType.getJsonFields().getJsonField());
        assertEquals(3, jsonComplexType.getJsonFields().getJsonField().size());
        assertEquals("menu", jsonComplexType.getName());

        JsonField jsonField1 = jsonComplexType.getJsonFields().getJsonField().get(0);
        assertNotNull(jsonField1);
        assertEquals("id", jsonField1.getName());
        assertEquals("file", jsonField1.getValue());
        assertEquals("/menu/id", jsonField1.getPath());
        assertEquals(FieldType.STRING, jsonField1.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, jsonField1.getStatus());

        JsonField jsonField2 = jsonComplexType.getJsonFields().getJsonField().get(1);
        assertNotNull(jsonField2);
        assertEquals("value", jsonField2.getName());
        assertEquals("Filed", jsonField2.getValue());
        assertEquals("/menu/value", jsonField2.getPath());
        assertEquals(FieldType.STRING, jsonField2.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, jsonField2.getStatus());

        JsonComplexType popup = (JsonComplexType) jsonComplexType.getJsonFields().getJsonField().get(2);
        assertNotNull(popup);
        assertNotNull(popup.getJsonFields().getJsonField());
        assertEquals(1, popup.getJsonFields().getJsonField().size());
        assertEquals("popup", popup.getName());
        assertEquals("/menu/popup", popup.getPath());
        assertEquals(FieldType.COMPLEX, popup.getFieldType());

        JsonComplexType menuitem = (JsonComplexType) popup.getJsonFields().getJsonField().get(0);
        assertNotNull(menuitem);
        assertNotNull(menuitem.getJsonFields().getJsonField());
        assertEquals("menuitem", menuitem.getName());
        assertEquals("/menu/popup/menuitem<>", menuitem.getPath());
        assertEquals(CollectionType.LIST, menuitem.getCollectionType());
        assertEquals(FieldType.COMPLEX, menuitem.getFieldType());
        assertEquals(2, menuitem.getJsonFields().getJsonField().size());

        JsonField menuitemValue = menuitem.getJsonFields().getJsonField().get(0);
        assertNotNull(menuitemValue);
        assertEquals("value", menuitemValue.getName());
        assertEquals("/menu/popup/menuitem<>/value", menuitemValue.getPath());
        assertEquals(FieldType.STRING, menuitemValue.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, menuitemValue.getStatus());

        JsonField menuitemOnclick = menuitem.getJsonFields().getJsonField().get(1);
        assertNotNull(menuitemOnclick);
        assertEquals("onclick", menuitemOnclick.getName());
        assertEquals("/menu/popup/menuitem<>/onclick", menuitemOnclick.getPath());
        assertEquals(FieldType.STRING, menuitemOnclick.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, menuitemOnclick.getStatus());
    }

    @Test
    public void inspectJsonDocumentHighlyNestedObject() throws Exception {
        final String instance = new String(
                Files.readAllBytes(Paths.get("src/test/resources/inspect/highly-nested-object.json")));
        JsonDocument document = inspectionService.inspectJsonDocument(instance);
        assertNotNull(document);
        assertEquals(6, document.getFields().getField().size());

        JsonField id = (JsonField) document.getFields().getField().get(0);
        assertNotNull(id);
        assertEquals("id", id.getName());
        assertEquals("0001", id.getValue());
        assertEquals("/id", id.getPath());
        assertEquals(FieldType.STRING, id.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, id.getStatus());

        JsonField value = (JsonField) document.getFields().getField().get(1);
        assertNotNull(value);
        assertEquals("type", value.getName());
        assertEquals("donut", value.getValue());
        assertEquals("/type", value.getPath());
        assertEquals(FieldType.STRING, value.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, value.getStatus());

        JsonField name = (JsonField) document.getFields().getField().get(2);
        assertNotNull(name);
        assertEquals("name", name.getName());
        assertEquals("Cake", name.getValue());
        assertEquals("/name", name.getPath());
        assertEquals(FieldType.STRING, name.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, name.getStatus());

        JsonField itemPPU = (JsonField) document.getFields().getField().get(3);
        assertNotNull(itemPPU);
        assertEquals("ppu", itemPPU.getName());
        assertEquals("/ppu", itemPPU.getPath());
        assertEquals(0.55, itemPPU.getValue());
        assertEquals(FieldType.DOUBLE, itemPPU.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, itemPPU.getStatus());

        JsonComplexType batters = (JsonComplexType) document.getFields().getField().get(4);
        assertNotNull(batters);
        assertEquals(1, batters.getJsonFields().getJsonField().size());

        JsonComplexType batterParent = (JsonComplexType) batters.getJsonFields().getJsonField().get(0);
        assertNotNull(batterParent);
        assertEquals("batter", batterParent.getName());
        assertEquals("/batters/batter<>", batterParent.getPath());
        assertEquals(CollectionType.LIST, batterParent.getCollectionType());
        assertEquals(2, batterParent.getJsonFields().getJsonField().size());

        JsonField batterId = batterParent.getJsonFields().getJsonField().get(0);
        assertNotNull(batterId);
        assertEquals("id", batterId.getName());
        assertEquals("/batters/batter<>/id", batterId.getPath());
        assertEquals(FieldType.STRING, batterId.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, batterId.getStatus());

        JsonField batterType = batterParent.getJsonFields().getJsonField().get(1);
        assertNotNull(batterType);
        assertEquals("type", batterType.getName());
        assertEquals("/batters/batter<>/type", batterType.getPath());
        assertEquals(FieldType.STRING, batterType.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, batterType.getStatus());

        JsonComplexType topping = (JsonComplexType) document.getFields().getField().get(5);
        assertNotNull(topping);
        assertEquals("topping", topping.getName());
        assertEquals("/topping<>", topping.getPath());
        assertEquals(CollectionType.LIST, topping.getCollectionType());
        assertEquals(2, topping.getJsonFields().getJsonField().size());

        JsonField toppingId = topping.getJsonFields().getJsonField().get(0);
        assertNotNull(toppingId);
        assertEquals("id", toppingId.getName());
        assertEquals("/topping<>/id", toppingId.getPath());
        assertEquals(FieldType.STRING, toppingId.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, toppingId.getStatus());

        JsonField toppingType = topping.getJsonFields().getJsonField().get(1);
        assertNotNull(toppingType);
        assertEquals("type", toppingType.getName());
        assertEquals("/topping<>/type", toppingType.getPath());
        assertEquals(FieldType.STRING, toppingType.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, toppingType.getStatus());
    }

    @Test
    public void inspectJsonDocumentHighlyComplexNestedObject() throws Exception {
        final String instance = new String(
                Files.readAllBytes(Paths.get("src/test/resources/inspect/highly-complex-nested-object.json")));
        JsonDocument document = inspectionService.inspectJsonDocument(instance);
        assertNotNull(document);
        assertEquals(1, document.getFields().getField().size());

        JsonComplexType items = (JsonComplexType) document.getFields().getField().get(0);
        assertNotNull(items);
        assertEquals(FieldType.COMPLEX, items.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, items.getStatus());
        assertEquals("items", items.getName());
        assertEquals(1, items.getJsonFields().getJsonField().size());

        JsonComplexType item = (JsonComplexType) items.getJsonFields().getJsonField().get(0);
        assertNotNull(item);
        assertEquals(FieldType.COMPLEX, item.getFieldType());
        assertEquals(CollectionType.LIST, item.getCollectionType());
        assertEquals("item", item.getName());
        assertEquals("/items/item<>", item.getPath());
        assertEquals(6, item.getJsonFields().getJsonField().size());

        JsonField itemId = item.getJsonFields().getJsonField().get(0);
        assertNotNull(itemId);
        assertEquals("id", itemId.getName());
        assertEquals("0001", itemId.getValue());
        assertEquals("/items/item<>/id", itemId.getPath());
        assertEquals(FieldType.STRING, itemId.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, itemId.getStatus());

        JsonField itemValue = item.getJsonFields().getJsonField().get(1);
        assertNotNull(itemValue);
        assertEquals("type", itemValue.getName());
        assertEquals("donut", itemValue.getValue());
        assertEquals("/items/item<>/type", itemValue.getPath());
        assertEquals(FieldType.STRING, itemValue.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, itemValue.getStatus());

        JsonField itemName = item.getJsonFields().getJsonField().get(2);
        assertNotNull(itemName);
        assertEquals("name", itemName.getName());
        assertEquals("Cake", itemName.getValue());
        assertEquals("/items/item<>/name", itemName.getPath());
        assertEquals(FieldType.STRING, itemName.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, itemName.getStatus());

        JsonField itemPPU = item.getJsonFields().getJsonField().get(3);
        assertNotNull(itemPPU);
        assertEquals("ppu", itemPPU.getName());
        assertEquals("/items/item<>/ppu", itemPPU.getPath());
        assertEquals(0.55, itemPPU.getValue());
        assertEquals(FieldType.DOUBLE, itemPPU.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, itemPPU.getStatus());

        JsonComplexType itemBattersComplexType = (JsonComplexType) item.getJsonFields().getJsonField().get(4);
        assertNotNull(itemBattersComplexType);
        assertEquals(FieldType.COMPLEX, itemBattersComplexType.getFieldType());
        assertEquals("batters", itemBattersComplexType.getName());
        assertEquals(1, itemBattersComplexType.getJsonFields().getJsonField().size());

        JsonComplexType itemBatterComplexType = (JsonComplexType) itemBattersComplexType.getJsonFields().getJsonField()
                .get(0);
        assertNotNull(itemBatterComplexType);
        assertEquals(FieldType.COMPLEX, itemBatterComplexType.getFieldType());
        assertEquals("batter", itemBatterComplexType.getName());
        assertEquals(CollectionType.LIST, itemBatterComplexType.getCollectionType());
        assertEquals("/items/item<>/batters/batter<>", itemBatterComplexType.getPath());
        assertEquals(2, itemBatterComplexType.getJsonFields().getJsonField().size());

        JsonField batterId = itemBatterComplexType.getJsonFields().getJsonField().get(0);
        assertNotNull(batterId);
        assertEquals("id", batterId.getName());
        assertEquals("1001", batterId.getValue());
        assertEquals("/items/item<>/batters/batter<>/id", batterId.getPath());
        assertEquals(FieldType.STRING, batterId.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, batterId.getStatus());

        JsonField batterType = itemBatterComplexType.getJsonFields().getJsonField().get(1);
        assertNotNull(batterType);
        assertEquals("type", batterType.getName());
        assertEquals("Regular", batterType.getValue());
        assertEquals("/items/item<>/batters/batter<>/type", batterType.getPath());
        assertEquals(FieldType.STRING, batterType.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, batterType.getStatus());

        JsonComplexType itemToppingComplexType = (JsonComplexType) item.getJsonFields().getJsonField().get(5);
        assertNotNull(itemToppingComplexType);
        assertEquals(FieldType.COMPLEX, itemToppingComplexType.getFieldType());
        assertEquals("topping", itemToppingComplexType.getName());
        assertEquals(CollectionType.LIST, itemToppingComplexType.getCollectionType());
        assertEquals("/items/item<>/topping<>", itemToppingComplexType.getPath());
        assertEquals(2, itemToppingComplexType.getJsonFields().getJsonField().size());

        JsonField toppingID = itemToppingComplexType.getJsonFields().getJsonField().get(0);
        assertNotNull(toppingID);
        assertEquals("id", toppingID.getName());
        assertEquals("/items/item<>/topping<>/id", toppingID.getPath());
        assertEquals(FieldType.STRING, toppingID.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, toppingID.getStatus());

        JsonField toppingType = itemToppingComplexType.getJsonFields().getJsonField().get(1);
        assertNotNull(toppingType);
        assertEquals("type", toppingType.getName());
        assertEquals("/items/item<>/topping<>/type", toppingType.getPath());
        assertEquals(FieldType.STRING, toppingType.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, toppingType.getStatus());
    }

    @Test
    public void testAddress() throws Exception {
        final String instance = new String(
                Files.readAllBytes(Paths.get("src/test/resources/inspect/address.json")));
        JsonDocument document = inspectionService.inspectJsonDocument(instance);
        assertNotNull(document);
        assertEquals(1, document.getFields().getField().size());

        JsonComplexType address = (JsonComplexType) document.getFields().getField().get(0);
        assertNotNull(address);
        assertEquals(FieldType.COMPLEX, address.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, address.getStatus());
        assertEquals(CollectionType.LIST, address.getCollectionType());
        assertEquals("address", address.getName());
        assertEquals("/address<>", address.getPath());
        assertEquals(6, address.getJsonFields().getJsonField().size());

        JsonField use = address.getJsonFields().getJsonField().get(0);
        assertNotNull(use);
        assertEquals(FieldType.STRING, use.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, use.getStatus());
        assertEquals(null, use.getCollectionType());
        assertEquals("use", use.getName());
        assertEquals("/address<>/use", use.getPath());

        JsonField type = address.getJsonFields().getJsonField().get(1);
        assertNotNull(type);
        assertEquals(FieldType.STRING, type.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, type.getStatus());
        assertEquals(null, type.getCollectionType());
        assertEquals("type", type.getName());
        assertEquals("/address<>/type", type.getPath());

        JsonField line = address.getJsonFields().getJsonField().get(2);
        assertNotNull(line);
        assertEquals(FieldType.STRING, line.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, line.getStatus());
        assertEquals(CollectionType.LIST, line.getCollectionType());
        assertEquals("line", line.getName());
        assertEquals("/address<>/line<>", line.getPath());

        JsonField city = address.getJsonFields().getJsonField().get(3);
        assertNotNull(city);
        assertEquals(FieldType.STRING, city.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, city.getStatus());
        assertEquals(null, city.getCollectionType());
        assertEquals("city", city.getName());
        assertEquals("/address<>/city", city.getPath());

        JsonField postalCode = address.getJsonFields().getJsonField().get(4);
        assertNotNull(postalCode);
        assertEquals(FieldType.STRING, postalCode.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, postalCode.getStatus());
        assertEquals(null, postalCode.getCollectionType());
        assertEquals("postalCode", postalCode.getName());
        assertEquals("/address<>/postalCode", postalCode.getPath());

        JsonField country = address.getJsonFields().getJsonField().get(5);
        assertNotNull(country);
        assertEquals(FieldType.STRING, country.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, country.getStatus());
        assertEquals(null, country.getCollectionType());
        assertEquals("country", country.getName());
        assertEquals("/address<>/country", country.getPath());
}

    @SuppressWarnings("unused")
    private void printDocument(JsonDocument document) {
        assertNotNull(document.getFields());
        printFields(document.getFields());
    }

    private void printFields(Fields fields) {
        assertNotNull(fields.getField());
        for (Field field : fields.getField()) {
            if (field instanceof JsonComplexType) {
                printJsonComplexType((JsonComplexType) field);
            } else {
                printJsonField((JsonField) field);
            }
        }
    }

    private void printFields(List<JsonField> jsonField) {
        for (JsonField field : jsonField) {
            if (field instanceof JsonComplexType) {
                printJsonComplexType((JsonComplexType) field);
            } else {
                printJsonField(field);
            }

        }
    }

    private void printJsonComplexType(JsonComplexType field) {
        assertNotNull(field.getJsonFields());
        printJsonField(field);
        printFields(field.getJsonFields().getJsonField());
    }

    private void printJsonField(JsonField jsonField) {
        System.out.println("Name --> " + jsonField.getName());
        System.out.println("Path --> " + jsonField.getPath());
        System.out.println("Value --> " + jsonField.getValue());
        if (jsonField.getFieldType() != null) {
            System.out.println("Type --> " + jsonField.getFieldType().name());
        }
        if (jsonField.getTypeName() != null) {
            System.out.println("Type Name --> " + jsonField.getTypeName());
        }
        if (jsonField.getCollectionType() != null) {
            System.out.println("Collection Type --> " + jsonField.getCollectionType().name());
        }
        if (jsonField.getStatus() != null) {
            System.out.println("Status  --> " + jsonField.getStatus().name());
        }
        System.out.println();
    }
}
