package io.atlasmap.json.inspect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.hamcrest.core.Is;
import org.junit.Test;

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

    @Test(expected = IllegalArgumentException.class)
    public void inspectJsonDocumentEmpty() throws Exception {
        final String instance = "";
        inspectionService.inspectJsonDocument(instance);
    }

    @Test(expected = IllegalArgumentException.class)
    public void inspectJsonDocumentWhitespaceOnly() throws Exception {
        final String instance = " ";
        inspectionService.inspectJsonDocument(instance);
    }

    @Test(expected = IllegalArgumentException.class)
    public void inspectJsonDocumentNull() throws Exception {
        final String instance = null;
        inspectionService.inspectJsonDocument(instance);
    }

    @Test(expected = JsonInspectionException.class)
    public void inspectJsonDocumentUnparseableHighlyComplexNestedObject() throws Exception {
        final String instance = new String(Files
                .readAllBytes(Paths.get("src/test/resources/inspect/unparseable-highly-complex-nested-object.json")));
        inspectionService.inspectJsonDocument(instance);
    }

    @Test(expected = JsonInspectionException.class)
    public void inspectJsonDocumentUnparseableMissingOpenCurly() throws Exception {
        final String instance = "\"ads\":[{\"id_ad\":\"20439\"}, {\"id_ad\":\"20449\"}]}";
        inspectionService.inspectJsonDocument(instance);
    }

    @Test(expected = JsonInspectionException.class)
    public void inspectJsonDocumentUnparseableMissingClosingCurly() throws Exception {
        final String instance = "{\"ads\":[{\"id_ad\":\"20439\"}, {\"id_ad\":\"20449\"}]";
        inspectionService.inspectJsonDocument(instance);
    }

    @Test(expected = JsonInspectionException.class)
    public void inspectJsonDocumentUnparseableMissingKeySeperator() throws Exception {
        final String instance = "{\"ads\"[{\"id_ad\":\"20439\"}, {\"id_ad\":\"20449\"}]";
        inspectionService.inspectJsonDocument(instance);
    }

    @Test(expected = JsonInspectionException.class)
    public void inspectJsonDocumentUnparseableMissingValueSeperator() throws Exception {
        final String instance = "{\"id_ad\":\"20439\" \"id_ad\":\"20449\"}";
        inspectionService.inspectJsonDocument(instance);
    }

    @Test()
    public void inspectJsonDocumentEmptyDocument() throws Exception {
        final String instance = "{}";
        JsonDocument document = inspectionService.inspectJsonDocument(instance);
        assertNotNull(document);
        assertThat(document.getFields().getField().size(), Is.is(0));
    }

    @Test
    public void inspectJsonDocumentSimpleArray() throws Exception {
        final String instance = "[ 100, 500, 300, 200, 400 ]";
        JsonDocument document = inspectionService.inspectJsonDocument(instance);
        assertNotNull(document);
        assertThat(1, Is.is(document.getFields().getField().size()));
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
        assertThat(1, Is.is(document.getFields().getField().size()));
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
        assertThat(document.getFields().getField().size(), Is.is(7));
        for (int i = 0; i < document.getFields().getField().size(); i++) {
            JsonField field = (JsonField) document.getFields().getField().get(i);
            if (i == 0) {
                assertThat(field.getName(), Is.is("'booleanField'"));
                assertThat(field.getValue(), Is.is(false));
                assertThat(field.getPath(), Is.is("/".concat(field.getName())));
                assertThat(field.getFieldType(), Is.is(FieldType.BOOLEAN));
                assertThat(field.getStatus(), Is.is(FieldStatus.SUPPORTED));
            } else if (i == 1) {
                assertThat(field.getName(), Is.is("\"charField\""));
                assertThat(field.getValue(), Is.is("a"));
                assertThat(field.getPath(), Is.is("/".concat(field.getName())));
                assertThat(field.getFieldType(), Is.is(FieldType.STRING));
                assertThat(field.getStatus(), Is.is(FieldStatus.SUPPORTED));
            } else if (i == 2) {
                assertThat(field.getName(), Is.is("\\doubleField"));
                assertThat(field.getValue(), Is.is(-27152745.3422));
                assertThat(field.getPath(), Is.is("/".concat(field.getName())));
                assertThat(field.getFieldType(), Is.is(FieldType.DOUBLE));
                assertThat(field.getStatus(), Is.is(FieldStatus.SUPPORTED));
            } else if (i == 3) {
                assertThat(field.getName(), Is.is("floatField\t"));
                assertThat(field.getValue(), Is.is(-63988281.00));
                assertThat(field.getPath(), Is.is("/".concat(field.getName())));
                assertThat(field.getFieldType(), Is.is(FieldType.DOUBLE));
                assertThat(field.getStatus(), Is.is(FieldStatus.SUPPORTED));
            } else if (i == 4) {
                assertThat(field.getName(), Is.is("intField\n"));
                assertThat(field.getValue(), Is.is(8281));
                assertThat(field.getPath(), Is.is("/".concat(field.getName())));
                assertThat(field.getFieldType(), Is.is(FieldType.INTEGER));
                assertThat(field.getStatus(), Is.is(FieldStatus.SUPPORTED));
            } else if (i == 5) {
                assertThat(field.getName(), Is.is("shortField"));
                assertThat(field.getValue(), Is.is(81));
                assertThat(field.getPath(), Is.is("/".concat(field.getName())));
                assertThat(field.getFieldType(), Is.is(FieldType.INTEGER));
                assertThat(field.getStatus(), Is.is(FieldStatus.SUPPORTED));
            } else if (i == 6) {
                assertThat(field.getName(), Is.is("longField"));
                assertThat(field.getValue(), Is.is(3988281));
                assertThat(field.getPath(), Is.is("/".concat(field.getName())));
                assertThat(field.getFieldType(), Is.is(FieldType.INTEGER));
                assertThat(field.getStatus(), Is.is(FieldStatus.SUPPORTED));
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
        assertThat(document.getFields().getField().size(), Is.is(5));
        for (int i = 0; i < document.getFields().getField().size(); i++) {
            JsonField field = (JsonField) document.getFields().getField().get(i);
            if (i == 0) {
                assertThat(field.getName(), Is.is("quote"));
                assertThat(field.getValue(), Is.is("\"yadda\""));
                assertThat(field.getPath(), Is.is("/".concat(field.getName())));
                assertThat(field.getFieldType(), Is.is(FieldType.STRING));
                assertThat(field.getStatus(), Is.is(FieldStatus.SUPPORTED));
            } else if (i == 1) {
                assertThat(field.getName(), Is.is("singlequote"));
                assertThat(field.getValue(), Is.is("'a'"));
                assertThat(field.getPath(), Is.is("/".concat(field.getName())));
                assertThat(field.getFieldType(), Is.is(FieldType.STRING));
                assertThat(field.getStatus(), Is.is(FieldStatus.SUPPORTED));
            } else if (i == 2) {
                assertThat(field.getName(), Is.is("backslash"));
                assertThat(field.getValue(), Is.is("\\qwerty"));
                assertThat(field.getPath(), Is.is("/".concat(field.getName())));
                assertThat(field.getFieldType(), Is.is(FieldType.STRING));
                assertThat(field.getStatus(), Is.is(FieldStatus.SUPPORTED));
            } else if (i == 3) {
                assertThat(field.getName(), Is.is("tab"));
                assertThat(field.getValue(), Is.is("foo\t"));
                assertThat(field.getPath(), Is.is("/".concat(field.getName())));
                assertThat(field.getFieldType(), Is.is(FieldType.STRING));
                assertThat(field.getStatus(), Is.is(FieldStatus.SUPPORTED));
            } else if (i == 4) {
                assertThat(field.getName(), Is.is("linefeed"));
                assertThat(field.getValue(), Is.is("bar\n"));
                assertThat(field.getPath(), Is.is("/".concat(field.getName())));
                assertThat(field.getFieldType(), Is.is(FieldType.STRING));
                assertThat(field.getStatus(), Is.is(FieldStatus.SUPPORTED));
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
        assertThat(document.getFields().getField().size(), Is.is(7));
        for (int i = 0; i < document.getFields().getField().size(); i++) {
            JsonField field = (JsonField) document.getFields().getField().get(i);
            if (i == 0) {
                assertThat(field.getName(), Is.is("booleanField"));
                assertThat(field.getValue(), Is.is(false));
                assertThat(field.getPath(), Is.is("/".concat(field.getName())));
                assertThat(field.getFieldType(), Is.is(FieldType.BOOLEAN));
                assertThat(field.getStatus(), Is.is(FieldStatus.SUPPORTED));
            } else if (i == 1) {
                assertThat(field.getName(), Is.is("charField"));
                assertThat(field.getValue(), Is.is("a"));
                assertThat(field.getPath(), Is.is("/".concat(field.getName())));
                // FIXME DOES NOT RECOGNIZE CHAR DISTINCTLY AND RETURNS THIS AS A STRING
                // (TEXTUAL)
                assertThat(field.getFieldType(), Is.is(FieldType.STRING));
                assertThat(field.getStatus(), Is.is(FieldStatus.SUPPORTED));
            } else if (i == 2) {
                assertThat(field.getName(), Is.is("doubleField"));
                assertThat(field.getValue(), Is.is(-27152745.3422));
                assertThat(field.getPath(), Is.is("/".concat(field.getName())));
                assertThat(field.getFieldType(), Is.is(FieldType.DOUBLE));
                assertThat(field.getStatus(), Is.is(FieldStatus.SUPPORTED));
            } else if (i == 3) {
                assertThat(field.getName(), Is.is("floatField"));
                assertThat(field.getValue(), Is.is(-63988281.00));
                assertThat(field.getPath(), Is.is("/".concat(field.getName())));
                // FIXME DOES NOT RECOGNIZE FLOAT DISTINCTLY AND RETURNS THIS AS A DOUBLE
                assertThat(field.getFieldType(), Is.is(FieldType.DOUBLE));
                assertThat(field.getStatus(), Is.is(FieldStatus.SUPPORTED));
            } else if (i == 4) {
                assertThat(field.getName(), Is.is("intField"));
                assertThat(field.getValue(), Is.is(8281));
                assertThat(field.getPath(), Is.is("/".concat(field.getName())));
                assertThat(field.getFieldType(), Is.is(FieldType.INTEGER));
                assertThat(field.getStatus(), Is.is(FieldStatus.SUPPORTED));
            } else if (i == 5) {
                assertThat(field.getName(), Is.is("shortField"));
                assertThat(field.getValue(), Is.is(81));
                assertThat(field.getPath(), Is.is("/".concat(field.getName())));
                // FIXME JSON DOES NOT RECOGNIZE SHORT DISTINCTLY AND RETURNS THIS AS A INTEGER
                assertThat(field.getFieldType(), Is.is(FieldType.INTEGER));
                assertThat(field.getStatus(), Is.is(FieldStatus.SUPPORTED));
            } else if (i == 6) {
                assertThat(field.getName(), Is.is("longField"));
                assertThat(field.getValue(), Is.is(3988281));
                assertThat(field.getPath(), Is.is("/".concat(field.getName())));
                // FIXME JSON DOES NOT RECOGNIZE LONG DISTINCTLY AND RETURNS THIS AS A INTEGER
                assertThat(field.getFieldType(), Is.is(FieldType.INTEGER));
                assertThat(field.getStatus(), Is.is(FieldStatus.SUPPORTED));
            }
        }
    }

    @Test
    public void inspectFlatPrimitiveWithRoot() throws Exception {
        final String instance = new String(
                Files.readAllBytes(Paths.get("src/test/resources/inspect/flatprimitive-base-rooted.json")));
        JsonDocument document = inspectionService.inspectJsonDocument(instance);
        assertNotNull(document);
        assertThat(document.getFields().getField().size(), Is.is(1));
        JsonComplexType root = (JsonComplexType) document.getFields().getField().get(0);
        assertNotNull(root);
        assertThat(root.getName(), Is.is("SourceFlatPrimitive"));
        assertThat(root.getPath(), Is.is("/SourceFlatPrimitive"));
        assertThat(root.getFieldType(), Is.is(FieldType.COMPLEX));
        assertThat(root.getStatus(), Is.is(FieldStatus.SUPPORTED));

        assertThat(root.getJsonFields().getJsonField().size(), Is.is(8));
        for (int i = 0; i < root.getJsonFields().getJsonField().size(); i++) {
            JsonField field = root.getJsonFields().getJsonField().get(i);
            if (i == 0) {
                assertThat(field.getName(), Is.is("booleanField"));
                assertThat(field.getValue(), Is.is(false));
                assertThat(field.getPath(), Is.is("/SourceFlatPrimitive/".concat(field.getName())));
                assertThat(field.getFieldType(), Is.is(FieldType.BOOLEAN));
                assertThat(field.getStatus(), Is.is(FieldStatus.SUPPORTED));
            } else if (i == 1) {
                assertThat(field.getName(), Is.is("charField"));
                assertThat(field.getValue(), Is.is("a"));
                assertThat(field.getPath(), Is.is("/SourceFlatPrimitive/".concat(field.getName())));
                assertThat(field.getFieldType(), Is.is(FieldType.STRING));
                assertThat(field.getStatus(), Is.is(FieldStatus.SUPPORTED));
            } else if (i == 2) {
                assertThat(field.getName(), Is.is("doubleField"));
                assertThat(field.getValue(), Is.is(-27152745.3422));
                assertThat(field.getPath(), Is.is("/SourceFlatPrimitive/".concat(field.getName())));
                assertThat(field.getFieldType(), Is.is(FieldType.DOUBLE));
                assertThat(field.getStatus(), Is.is(FieldStatus.SUPPORTED));
            } else if (i == 3) {
                assertThat(field.getName(), Is.is("floatField"));
                assertThat(field.getValue(), Is.is(-63988281.00));
                assertThat(field.getPath(), Is.is("/SourceFlatPrimitive/".concat(field.getName())));
                assertThat(field.getFieldType(), Is.is(FieldType.DOUBLE));
                assertThat(field.getStatus(), Is.is(FieldStatus.SUPPORTED));
            } else if (i == 4) {
                assertThat(field.getName(), Is.is("intField"));
                assertThat(field.getValue(), Is.is(8281));
                assertThat(field.getPath(), Is.is("/SourceFlatPrimitive/".concat(field.getName())));
                assertThat(field.getFieldType(), Is.is(FieldType.INTEGER));
                assertThat(field.getStatus(), Is.is(FieldStatus.SUPPORTED));
            } else if (i == 5) {
                assertThat(field.getName(), Is.is("shortField"));
                assertThat(field.getValue(), Is.is(81));
                assertThat(field.getPath(), Is.is("/SourceFlatPrimitive/".concat(field.getName())));
                assertThat(field.getFieldType(), Is.is(FieldType.INTEGER));
                assertThat(field.getStatus(), Is.is(FieldStatus.SUPPORTED));
            } else if (i == 6) {
                assertThat(field.getName(), Is.is("longField"));
                assertThat(field.getValue(), Is.is(3988281));
                assertThat(field.getPath(), Is.is("/SourceFlatPrimitive/".concat(field.getName())));
                assertThat(field.getFieldType(), Is.is(FieldType.INTEGER));
                assertThat(field.getStatus(), Is.is(FieldStatus.SUPPORTED));
            }
        }
    }

    @Test
    public void inspectComplexObjectNoRoot() throws Exception {
        final String instance = new String(
                Files.readAllBytes(Paths.get("src/test/resources/inspect/complex-object-unrooted.json")));
        JsonDocument document = inspectionService.inspectJsonDocument(instance);
        assertNotNull(document);
        assertThat(document.getFields().getField().size(), Is.is(3));

        JsonComplexType address = (JsonComplexType) document.getFields().getField().get(0);
        assertNotNull(address);
        assertThat(address.getJsonFields().getJsonField().size(), Is.is(5));

        JsonField address1 = address.getJsonFields().getJsonField().get(0);
        assertNotNull(address1);
        assertThat(address1.getName(), Is.is("addressLine1"));
        assertThat(address1.getValue(), Is.is("123 Main St"));
        assertThat(address1.getPath(), Is.is("/address/addressLine1"));
        assertThat(address1.getFieldType(), Is.is(FieldType.STRING));
        assertThat(address1.getStatus(), Is.is(FieldStatus.SUPPORTED));

        JsonField address2 = address.getJsonFields().getJsonField().get(1);
        assertNotNull(address2);
        assertThat(address2.getName(), Is.is("addressLine2"));
        assertThat(address2.getValue(), Is.is("Suite 42b"));
        assertThat(address2.getPath(), Is.is("/address/addressLine2"));
        assertThat(address2.getFieldType(), Is.is(FieldType.STRING));
        assertThat(address2.getStatus(), Is.is(FieldStatus.SUPPORTED));

        JsonField city = address.getJsonFields().getJsonField().get(2);
        assertNotNull(city);
        assertThat(city.getName(), Is.is("city"));
        assertThat(city.getValue(), Is.is("Anytown"));
        assertThat(city.getPath(), Is.is("/address/city"));
        assertThat(city.getFieldType(), Is.is(FieldType.STRING));
        assertThat(city.getStatus(), Is.is(FieldStatus.SUPPORTED));

        JsonField state = address.getJsonFields().getJsonField().get(3);
        assertNotNull(state);
        assertThat(state.getName(), Is.is("state"));
        assertThat(state.getValue(), Is.is("NY"));
        assertThat(state.getPath(), Is.is("/address/state"));
        assertThat(state.getFieldType(), Is.is(FieldType.STRING));
        assertThat(state.getStatus(), Is.is(FieldStatus.SUPPORTED));

        JsonField postalCode = address.getJsonFields().getJsonField().get(4);
        assertNotNull(postalCode);
        assertThat(postalCode.getName(), Is.is("zipCode"));
        assertThat(postalCode.getValue(), Is.is("90210"));
        assertThat(postalCode.getPath(), Is.is("/address/zipCode"));
        assertThat(postalCode.getFieldType(), Is.is(FieldType.STRING));
        assertThat(postalCode.getStatus(), Is.is(FieldStatus.SUPPORTED));

        JsonComplexType contact = (JsonComplexType) document.getFields().getField().get(1);
        assertNotNull(contact);
        assertThat(contact.getJsonFields().getJsonField().size(), Is.is(4));

        JsonField firstName = contact.getJsonFields().getJsonField().get(0);
        assertNotNull(firstName);
        assertThat(firstName.getName(), Is.is("firstName"));
        assertThat(firstName.getValue(), Is.is("Ozzie"));
        assertThat(firstName.getPath(), Is.is("/contact/firstName"));
        assertThat(firstName.getFieldType(), Is.is(FieldType.STRING));
        assertThat(firstName.getStatus(), Is.is(FieldStatus.SUPPORTED));

        JsonField lastName = contact.getJsonFields().getJsonField().get(1);
        assertNotNull(lastName);
        assertThat(lastName.getName(), Is.is("lastName"));
        assertThat(lastName.getValue(), Is.is("Smith"));
        assertThat(lastName.getPath(), Is.is("/contact/lastName"));
        assertThat(lastName.getFieldType(), Is.is(FieldType.STRING));
        assertThat(lastName.getStatus(), Is.is(FieldStatus.SUPPORTED));

        JsonField phoneNumber = contact.getJsonFields().getJsonField().get(2);
        assertNotNull(phoneNumber);
        assertThat(phoneNumber.getName(), Is.is("phoneNumber"));
        assertThat(phoneNumber.getValue(), Is.is("5551212"));
        assertThat(phoneNumber.getPath(), Is.is("/contact/phoneNumber"));
        assertThat(phoneNumber.getFieldType(), Is.is(FieldType.STRING));
        assertThat(phoneNumber.getStatus(), Is.is(FieldStatus.SUPPORTED));

        JsonField zipCode = contact.getJsonFields().getJsonField().get(3);
        assertNotNull(zipCode);
        assertThat(zipCode.getName(), Is.is("zipCode"));
        assertThat(zipCode.getValue(), Is.is("81111"));
        assertThat(zipCode.getPath(), Is.is("/contact/zipCode"));
        assertThat(zipCode.getFieldType(), Is.is(FieldType.STRING));
        assertThat(zipCode.getStatus(), Is.is(FieldStatus.SUPPORTED));

        JsonField orderId = (JsonField) document.getFields().getField().get(2);
        assertNotNull(orderId);
        assertThat(orderId.getName(), Is.is("orderId"));
        assertThat(orderId.getValue(), Is.is(0));
        assertThat(orderId.getPath(), Is.is("/orderId"));
        assertThat(orderId.getFieldType(), Is.is(FieldType.INTEGER));
        assertThat(orderId.getStatus(), Is.is(FieldStatus.SUPPORTED));
    }

    @Test
    public void inspectComplexObjectWithRoot() throws Exception {
        final String instance = new String(
                Files.readAllBytes(Paths.get("src/test/resources/inspect/complex-object-rooted.json")));
        JsonDocument document = inspectionService.inspectJsonDocument(instance);
        assertNotNull(document);
        assertThat(document.getFields().getField().size(), Is.is(1));

        JsonComplexType root = (JsonComplexType) document.getFields().getField().get(0);
        assertThat(root.getJsonFields().getJsonField().size(), Is.is(3));

        JsonComplexType address = (JsonComplexType) root.getJsonFields().getJsonField().get(0);
        assertNotNull(address);
        assertThat(address.getJsonFields().getJsonField().size(), Is.is(5));

        JsonField address1 = address.getJsonFields().getJsonField().get(0);
        assertNotNull(address1);
        assertThat(address1.getName(), Is.is("addressLine1"));
        assertThat(address1.getValue(), Is.is("123 Main St"));
        assertThat(address1.getPath(), Is.is("/order/address/addressLine1"));
        assertThat(address1.getFieldType(), Is.is(FieldType.STRING));
        assertThat(address1.getStatus(), Is.is(FieldStatus.SUPPORTED));

        JsonField address2 = address.getJsonFields().getJsonField().get(1);
        assertNotNull(address2);
        assertThat(address2.getName(), Is.is("addressLine2"));
        assertThat(address2.getValue(), Is.is("Suite 42b"));
        assertThat(address2.getPath(), Is.is("/order/address/addressLine2"));
        assertThat(address2.getFieldType(), Is.is(FieldType.STRING));
        assertThat(address2.getStatus(), Is.is(FieldStatus.SUPPORTED));

        JsonField city = address.getJsonFields().getJsonField().get(2);
        assertNotNull(city);
        assertThat(city.getName(), Is.is("city"));
        assertThat(city.getValue(), Is.is("Anytown"));
        assertThat(city.getPath(), Is.is("/order/address/city"));
        assertThat(city.getFieldType(), Is.is(FieldType.STRING));
        assertThat(city.getStatus(), Is.is(FieldStatus.SUPPORTED));

        JsonField state = address.getJsonFields().getJsonField().get(3);
        assertNotNull(state);
        assertThat(state.getName(), Is.is("state"));
        assertThat(state.getValue(), Is.is("NY"));
        assertThat(state.getPath(), Is.is("/order/address/state"));
        assertThat(state.getFieldType(), Is.is(FieldType.STRING));
        assertThat(state.getStatus(), Is.is(FieldStatus.SUPPORTED));

        JsonField postalCode = address.getJsonFields().getJsonField().get(4);
        assertNotNull(postalCode);
        assertThat(postalCode.getName(), Is.is("zipCode"));
        assertThat(postalCode.getValue(), Is.is("90210"));
        assertThat(postalCode.getPath(), Is.is("/order/address/zipCode"));
        assertThat(postalCode.getFieldType(), Is.is(FieldType.STRING));
        assertThat(postalCode.getStatus(), Is.is(FieldStatus.SUPPORTED));

        JsonComplexType contact = (JsonComplexType) root.getJsonFields().getJsonField().get(1);
        assertNotNull(contact);
        assertThat(contact.getJsonFields().getJsonField().size(), Is.is(4));

        JsonField firstName = contact.getJsonFields().getJsonField().get(0);
        assertNotNull(firstName);
        assertThat(firstName.getName(), Is.is("firstName"));
        assertThat(firstName.getValue(), Is.is("Ozzie"));
        assertThat(firstName.getPath(), Is.is("/order/contact/firstName"));
        assertThat(firstName.getFieldType(), Is.is(FieldType.STRING));
        assertThat(firstName.getStatus(), Is.is(FieldStatus.SUPPORTED));

        JsonField lastName = contact.getJsonFields().getJsonField().get(1);
        assertNotNull(lastName);
        assertThat(lastName.getName(), Is.is("lastName"));
        assertThat(lastName.getValue(), Is.is("Smith"));
        assertThat(lastName.getPath(), Is.is("/order/contact/lastName"));
        assertThat(lastName.getFieldType(), Is.is(FieldType.STRING));
        assertThat(lastName.getStatus(), Is.is(FieldStatus.SUPPORTED));

        JsonField phoneNumber = contact.getJsonFields().getJsonField().get(2);
        assertNotNull(phoneNumber);
        assertThat(phoneNumber.getName(), Is.is("phoneNumber"));
        assertThat(phoneNumber.getValue(), Is.is("5551212"));
        assertThat(phoneNumber.getPath(), Is.is("/order/contact/phoneNumber"));
        assertThat(phoneNumber.getFieldType(), Is.is(FieldType.STRING));
        assertThat(phoneNumber.getStatus(), Is.is(FieldStatus.SUPPORTED));

        JsonField zipCode = contact.getJsonFields().getJsonField().get(3);
        assertNotNull(zipCode);
        assertThat(zipCode.getName(), Is.is("zipCode"));
        assertThat(zipCode.getValue(), Is.is("81111"));
        assertThat(zipCode.getPath(), Is.is("/order/contact/zipCode"));
        assertThat(zipCode.getFieldType(), Is.is(FieldType.STRING));
        assertThat(zipCode.getStatus(), Is.is(FieldStatus.SUPPORTED));

        JsonField orderId = root.getJsonFields().getJsonField().get(2);
        assertNotNull(orderId);
        assertThat(orderId.getName(), Is.is("orderId"));
        assertThat(orderId.getValue(), Is.is(0));
        assertThat(orderId.getPath(), Is.is("/order/orderId"));
        assertThat(orderId.getFieldType(), Is.is(FieldType.INTEGER));
        assertThat(orderId.getStatus(), Is.is(FieldStatus.SUPPORTED));
    }

    @Test
    public void inspectRepeatingComplexObjectWithRoot() throws Exception {
        final String instance = new String(
                Files.readAllBytes(Paths.get("src/test/resources/inspect/complex-repeated-rooted.json")));
        JsonDocument document = inspectionService.inspectJsonDocument(instance);
        assertNotNull(document);
        assertThat(document.getFields().getField().size(), Is.is(1));
        JsonComplexType root = (JsonComplexType) document.getFields().getField().get(0);
        assertNotNull(root);
        assertThat(root.getJsonFields().getJsonField().size(), Is.is(3));
        assertThat(root.getName(), Is.is("SourceOrderList"));
        assertEquals("/SourceOrderList", root.getPath());
        assertEquals(FieldType.COMPLEX, root.getFieldType());
        assertEquals(null, root.getCollectionType());

        JsonComplexType orders = (JsonComplexType) root.getJsonFields().getJsonField().get(0);
        assertNotNull(orders);
        assertThat(orders.getJsonFields().getJsonField().size(), Is.is(3));
        assertThat(orders.getName(), Is.is("orders"));
        assertEquals("/SourceOrderList/orders<>", orders.getPath());
        assertEquals(FieldType.COMPLEX, orders.getFieldType());
        assertEquals(CollectionType.LIST, orders.getCollectionType());

        JsonField orderBatchNumber = root.getJsonFields().getJsonField().get(1);
        assertNotNull(orderBatchNumber);
        assertThat(orderBatchNumber.getName(), Is.is("orderBatchNumber"));
        assertThat(orderBatchNumber.getValue(), Is.is(4123562));
        assertThat(orderBatchNumber.getPath(), Is.is("/SourceOrderList/orderBatchNumber"));
        assertThat(orderBatchNumber.getFieldType(), Is.is(FieldType.INTEGER));
        assertThat(orderBatchNumber.getStatus(), Is.is(FieldStatus.SUPPORTED));

        JsonField numberOrders = root.getJsonFields().getJsonField().get(2);
        assertNotNull(numberOrders);
        assertThat(numberOrders.getName(), Is.is("numberOrders"));
        assertThat(numberOrders.getValue(), Is.is(5));
        assertThat(numberOrders.getPath(), Is.is("/SourceOrderList/numberOrders"));
        assertThat(numberOrders.getFieldType(), Is.is(FieldType.INTEGER));
        assertThat(numberOrders.getStatus(), Is.is(FieldStatus.SUPPORTED));

        JsonComplexType address = (JsonComplexType) orders.getJsonFields().getJsonField().get(0);
        assertNotNull(address);
        assertThat(address.getJsonFields().getJsonField().size(), Is.is(5));
        assertEquals(null, address.getCollectionType());
        assertEquals(FieldType.COMPLEX, address.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, address.getStatus());
        assertThat(address.getName(), Is.is("address"));
        assertThat(address.getPath(), Is.is("/SourceOrderList/orders<>/address"));

        JsonField addressLine1 = address.getJsonFields().getJsonField().get(0);
        assertNotNull(addressLine1);
        assertThat(addressLine1.getName(), Is.is("addressLine1"));
        assertThat(addressLine1.getPath(), Is.is("/SourceOrderList/orders<>/address/addressLine1"));
        assertThat(addressLine1.getFieldType(), Is.is(FieldType.STRING));
        assertThat(addressLine1.getStatus(), Is.is(FieldStatus.SUPPORTED));

        JsonField addressLine2 = address.getJsonFields().getJsonField().get(1);
        assertNotNull(addressLine2);
        assertThat(addressLine2.getName(), Is.is("addressLine2"));
        assertThat(addressLine2.getPath(), Is.is("/SourceOrderList/orders<>/address/addressLine2"));
        assertThat(addressLine2.getFieldType(), Is.is(FieldType.STRING));
        assertThat(addressLine2.getStatus(), Is.is(FieldStatus.SUPPORTED));

        JsonField city = address.getJsonFields().getJsonField().get(2);
        assertNotNull(city);
        assertThat(city.getName(), Is.is("city"));
        assertThat(city.getPath(), Is.is("/SourceOrderList/orders<>/address/city"));
        assertThat(city.getFieldType(), Is.is(FieldType.STRING));
        assertThat(city.getStatus(), Is.is(FieldStatus.SUPPORTED));

        JsonField state = address.getJsonFields().getJsonField().get(3);
        assertNotNull(state);
        assertThat(state.getName(), Is.is("state"));
        assertThat(state.getPath(), Is.is("/SourceOrderList/orders<>/address/state"));
        assertThat(state.getFieldType(), Is.is(FieldType.STRING));
        assertThat(state.getStatus(), Is.is(FieldStatus.SUPPORTED));

        JsonField postalCode = address.getJsonFields().getJsonField().get(4);
        assertNotNull(postalCode);
        assertThat(postalCode.getName(), Is.is("zipCode"));
        assertThat(postalCode.getPath(), Is.is("/SourceOrderList/orders<>/address/zipCode"));
        assertThat(postalCode.getFieldType(), Is.is(FieldType.STRING));
        assertThat(postalCode.getStatus(), Is.is(FieldStatus.SUPPORTED));

        JsonComplexType contact = (JsonComplexType) orders.getJsonFields().getJsonField().get(1);
        assertNotNull(contact);
        assertThat(contact.getJsonFields().getJsonField().size(), Is.is(4));
        assertEquals(FieldType.COMPLEX, contact.getFieldType());
        assertEquals(null, contact.getCollectionType());
        assertEquals(FieldStatus.SUPPORTED, contact.getStatus());
        assertThat(contact.getName(), Is.is("contact"));
        assertThat(contact.getPath(), Is.is("/SourceOrderList/orders<>/contact"));

        JsonField firstName = contact.getJsonFields().getJsonField().get(0);
        assertNotNull(firstName);
        assertThat(firstName.getName(), Is.is("firstName"));
        assertThat(firstName.getPath(), Is.is("/SourceOrderList/orders<>/contact/firstName"));
        assertThat(firstName.getFieldType(), Is.is(FieldType.STRING));
        assertThat(firstName.getStatus(), Is.is(FieldStatus.SUPPORTED));

        JsonField lastName = contact.getJsonFields().getJsonField().get(1);
        assertNotNull(lastName);
        assertThat(lastName.getName(), Is.is("lastName"));
        assertThat(lastName.getPath(), Is.is("/SourceOrderList/orders<>/contact/lastName"));
        assertThat(lastName.getFieldType(), Is.is(FieldType.STRING));
        assertThat(lastName.getStatus(), Is.is(FieldStatus.SUPPORTED));

        JsonField phoneNumber = contact.getJsonFields().getJsonField().get(2);
        assertNotNull(phoneNumber);
        assertThat(phoneNumber.getName(), Is.is("phoneNumber"));
        assertThat(phoneNumber.getPath(), Is.is("/SourceOrderList/orders<>/contact/phoneNumber"));
        assertThat(phoneNumber.getFieldType(), Is.is(FieldType.STRING));
        assertThat(phoneNumber.getStatus(), Is.is(FieldStatus.SUPPORTED));

        JsonField zipCode = contact.getJsonFields().getJsonField().get(3);
        assertNotNull(zipCode);
        assertThat(zipCode.getName(), Is.is("zipCode"));
        assertThat(zipCode.getPath(), Is.is("/SourceOrderList/orders<>/contact/zipCode"));
        assertThat(zipCode.getFieldType(), Is.is(FieldType.STRING));
        assertThat(zipCode.getStatus(), Is.is(FieldStatus.SUPPORTED));
    }

    @Test
    public void inspectISO8601DatesNoRoot() throws Exception {
        final String instance = new String(
                Files.readAllBytes(Paths.get("src/test/resources/inspect/iso8601dates-unrooted.json")));
        JsonDocument document = inspectionService.inspectJsonDocument(instance);
        assertNotNull(document);
        assertThat(document.getFields().getField().size(), Is.is(7));

        JsonField yyyy = (JsonField) document.getFields().getField().get(0);
        assertNotNull(yyyy);
        assertThat(yyyy.getName(), Is.is("YYYY"));
        assertThat(yyyy.getValue(), Is.is("1997"));
        assertThat(yyyy.getPath(), Is.is("/YYYY"));
        assertThat(yyyy.getFieldType(), Is.is(FieldType.STRING));
        assertThat(yyyy.getStatus(), Is.is(FieldStatus.SUPPORTED));

        JsonField yyyymm = (JsonField) document.getFields().getField().get(1);
        assertNotNull(yyyymm);
        assertThat(yyyymm.getName(), Is.is("YYYY-MM"));
        assertThat(yyyymm.getValue(), Is.is("1997-07"));
        assertThat(yyyymm.getPath(), Is.is("/YYYY-MM"));
        assertThat(yyyymm.getFieldType(), Is.is(FieldType.STRING));
        assertThat(yyyymm.getStatus(), Is.is(FieldStatus.SUPPORTED));

        JsonField yyyymmdd = (JsonField) document.getFields().getField().get(2);
        assertNotNull(yyyymmdd);
        assertThat(yyyymmdd.getName(), Is.is("YYYY-MM-DD"));
        assertThat(yyyymmdd.getValue(), Is.is("1997-07-16"));
        assertThat(yyyymmdd.getPath(), Is.is("/YYYY-MM-DD"));
        assertThat(yyyymmdd.getFieldType(), Is.is(FieldType.STRING));
        assertThat(yyyymmdd.getStatus(), Is.is(FieldStatus.SUPPORTED));

        JsonField yyyymmddthhmmtzd = (JsonField) document.getFields().getField().get(3);
        assertNotNull(yyyymmddthhmmtzd);
        assertThat(yyyymmddthhmmtzd.getName(), Is.is("YYYY-MM-DDThh:mmTZD"));
        assertThat(yyyymmddthhmmtzd.getValue(), Is.is("1997-07-16T19:20+01:00"));
        assertThat(yyyymmddthhmmtzd.getPath(), Is.is("/YYYY-MM-DDThh:mmTZD"));
        assertThat(yyyymmddthhmmtzd.getFieldType(), Is.is(FieldType.STRING));
        assertThat(yyyymmddthhmmtzd.getStatus(), Is.is(FieldStatus.SUPPORTED));

        JsonField yyyymmddthhmmsstzd = (JsonField) document.getFields().getField().get(4);
        assertNotNull(yyyymmddthhmmsstzd);
        assertThat(yyyymmddthhmmsstzd.getName(), Is.is("YYYY-MM-DDThh:mm:ssTZD"));
        assertThat(yyyymmddthhmmsstzd.getValue(), Is.is("1997-07-16T19:20:30+01:00"));
        assertThat(yyyymmddthhmmsstzd.getPath(), Is.is("/YYYY-MM-DDThh:mm:ssTZD"));
        assertThat(yyyymmddthhmmsstzd.getFieldType(), Is.is(FieldType.STRING));
        assertThat(yyyymmddthhmmsstzd.getStatus(), Is.is(FieldStatus.SUPPORTED));

        JsonField yyyymmddthhmmssstzd = (JsonField) document.getFields().getField().get(5);
        assertNotNull(yyyymmddthhmmssstzd);
        assertThat(yyyymmddthhmmssstzd.getName(), Is.is("YYYY-MM-DDThh:mm:ss.sTZD"));
        assertThat(yyyymmddthhmmssstzd.getValue(), Is.is("1997-07-16T19:20:30.45+01:00"));
        assertThat(yyyymmddthhmmssstzd.getPath(), Is.is("/YYYY-MM-DDThh:mm:ss.sTZD"));
        assertThat(yyyymmddthhmmssstzd.getFieldType(), Is.is(FieldType.STRING));
        assertThat(yyyymmddthhmmssstzd.getStatus(), Is.is(FieldStatus.SUPPORTED));

        JsonField yyyymmddthhmmssutz = (JsonField) document.getFields().getField().get(6);
        assertNotNull(yyyymmddthhmmssutz);
        assertThat(yyyymmddthhmmssutz.getName(), Is.is("YYYY-MM-DDThh:mm:ssUTZ"));
        assertThat(yyyymmddthhmmssutz.getValue(), Is.is("1994-11-05T13:15:30Z"));
        assertThat(yyyymmddthhmmssutz.getPath(), Is.is("/YYYY-MM-DDThh:mm:ssUTZ"));
        assertThat(yyyymmddthhmmssutz.getFieldType(), Is.is(FieldType.STRING));
        assertThat(yyyymmddthhmmssutz.getStatus(), Is.is(FieldStatus.SUPPORTED));

    }

    @Test
    public void inspectJsonDocumentNoRoot() throws Exception {
        final String instance = "{ \"brand\" : \"Mercedes\", \"doors\" : 5 }";
        JsonDocument document = inspectionService.inspectJsonDocument(instance);
        assertNotNull(document);
        assertThat(document.getFields().getField().size(), Is.is(2));

        JsonField field1 = (JsonField) document.getFields().getField().get(0);
        assertNotNull(field1);
        assertThat(field1.getName(), Is.is("brand"));
        assertThat(field1.getValue(), Is.is("Mercedes"));
        assertThat(field1.getPath(), Is.is("/brand"));
        assertThat(field1.getFieldType(), Is.is(FieldType.STRING));
        assertThat(field1.getStatus(), Is.is(FieldStatus.SUPPORTED));

        JsonField field2 = (JsonField) document.getFields().getField().get(1);
        assertNotNull(field2);
        assertThat(field2.getName(), Is.is("doors"));
        assertThat(field2.getValue(), Is.is(5));
        assertThat(field2.getPath(), Is.is("/doors"));
        assertThat(field2.getFieldType(), Is.is(FieldType.INTEGER));
        assertThat(field2.getStatus(), Is.is(FieldStatus.SUPPORTED));
        // printDocument(document);
    }

    @Test
    public void inspectJsonDocumentWithRoot() throws Exception {
        final String instance = "{\"car\" :{ \"brand\" : \"Mercedes\", \"doors\" : 5 } }";
        JsonDocument document = inspectionService.inspectJsonDocument(instance);
        assertNotNull(document);
        assertThat(document.getFields().getField().size(), Is.is(1));
        JsonComplexType car = (JsonComplexType) document.getFields().getField().get(0);
        assertNotNull(car);
        assertThat(car.getName(), Is.is("car"));
        assertThat(car.getFieldType(), Is.is(FieldType.COMPLEX));
        assertThat(car.getPath(), Is.is("/car"));
        assertThat(car.getStatus(), Is.is(FieldStatus.SUPPORTED));
        assertThat(car.getJsonFields().getJsonField().size(), Is.is(2));

        JsonField field1 = car.getJsonFields().getJsonField().get(0);
        assertNotNull(field1);
        assertThat(field1.getName(), Is.is("brand"));
        assertThat(field1.getValue(), Is.is("Mercedes"));
        assertThat(field1.getPath(), Is.is("/car/brand"));
        assertThat(field1.getFieldType(), Is.is(FieldType.STRING));
        assertThat(field1.getStatus(), Is.is(FieldStatus.SUPPORTED));

        JsonField field2 = car.getJsonFields().getJsonField().get(1);
        assertNotNull(field2);
        assertThat(field2.getName(), Is.is("doors"));
        assertThat(field2.getValue(), Is.is(5));
        assertThat(field2.getPath(), Is.is("/car/doors"));
        assertThat(field2.getFieldType(), Is.is(FieldType.INTEGER));
        assertThat(field2.getStatus(), Is.is(FieldStatus.SUPPORTED));
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
        assertThat(1, Is.is(document.getFields().getField().size()));
        assertNotNull(document.getFields().getField());

        JsonComplexType jsonComplexType = (JsonComplexType) document.getFields().getField().get(0);
        assertNotNull(jsonComplexType);
        assertNotNull(jsonComplexType.getJsonFields().getJsonField());
        assertThat(jsonComplexType.getJsonFields().getJsonField().size(), Is.is(3));
        assertThat(jsonComplexType.getName(), Is.is("menu"));

        JsonField jsonField1 = jsonComplexType.getJsonFields().getJsonField().get(0);
        assertNotNull(jsonField1);
        assertThat(jsonField1.getName(), Is.is("id"));
        assertThat(jsonField1.getValue(), Is.is("file"));
        assertThat(jsonField1.getPath(), Is.is("/menu/id"));
        assertThat(jsonField1.getFieldType(), Is.is(FieldType.STRING));
        assertThat(jsonField1.getStatus(), Is.is(FieldStatus.SUPPORTED));

        JsonField jsonField2 = jsonComplexType.getJsonFields().getJsonField().get(1);
        assertNotNull(jsonField2);
        assertThat(jsonField2.getName(), Is.is("value"));
        assertThat(jsonField2.getValue(), Is.is("Filed"));
        assertThat(jsonField2.getPath(), Is.is("/menu/value"));
        assertThat(jsonField2.getFieldType(), Is.is(FieldType.STRING));
        assertThat(jsonField2.getStatus(), Is.is(FieldStatus.SUPPORTED));

        JsonComplexType popup = (JsonComplexType) jsonComplexType.getJsonFields().getJsonField().get(2);
        assertNotNull(popup);
        assertNotNull(popup.getJsonFields().getJsonField());
        assertThat(popup.getJsonFields().getJsonField().size(), Is.is(1));
        assertThat(popup.getName(), Is.is("popup"));
        assertThat(popup.getPath(), Is.is("/menu/popup"));
        assertThat(popup.getFieldType(), Is.is(FieldType.COMPLEX));

        JsonComplexType menuitem = (JsonComplexType) popup.getJsonFields().getJsonField().get(0);
        assertNotNull(menuitem);
        assertNotNull(menuitem.getJsonFields().getJsonField());
        assertEquals("menuitem", menuitem.getName());
        assertEquals("/menu/popup/menuitem<>", menuitem.getPath());
        assertEquals(CollectionType.LIST, menuitem.getCollectionType());
        assertEquals(FieldType.COMPLEX, menuitem.getFieldType());
        assertThat(menuitem.getJsonFields().getJsonField().size(), Is.is(2));

        JsonField menuitemValue = menuitem.getJsonFields().getJsonField().get(0);
        assertNotNull(menuitemValue);
        assertThat(menuitemValue.getName(), Is.is("value"));
        assertThat(menuitemValue.getPath(), Is.is("/menu/popup/menuitem<>/value"));
        assertThat(menuitemValue.getFieldType(), Is.is(FieldType.STRING));
        assertThat(menuitemValue.getStatus(), Is.is(FieldStatus.SUPPORTED));

        JsonField menuitemOnclick = menuitem.getJsonFields().getJsonField().get(1);
        assertNotNull(menuitemOnclick);
        assertThat(menuitemOnclick.getName(), Is.is("onclick"));
        assertThat(menuitemOnclick.getPath(), Is.is("/menu/popup/menuitem<>/onclick"));
        assertThat(menuitemOnclick.getFieldType(), Is.is(FieldType.STRING));
        assertThat(menuitemOnclick.getStatus(), Is.is(FieldStatus.SUPPORTED));
    }

    @Test
    public void inspectJsonDocumentHighlyNestedObject() throws Exception {
        final String instance = new String(
                Files.readAllBytes(Paths.get("src/test/resources/inspect/highly-nested-object.json")));
        JsonDocument document = inspectionService.inspectJsonDocument(instance);
        assertNotNull(document);
        assertThat(document.getFields().getField().size(), Is.is(6));

        JsonField id = (JsonField) document.getFields().getField().get(0);
        assertNotNull(id);
        assertThat(id.getName(), Is.is("id"));
        assertThat(id.getValue(), Is.is("0001"));
        assertThat(id.getPath(), Is.is("/id"));
        assertThat(id.getFieldType(), Is.is(FieldType.STRING));
        assertThat(id.getStatus(), Is.is(FieldStatus.SUPPORTED));

        JsonField value = (JsonField) document.getFields().getField().get(1);
        assertNotNull(value);
        assertThat(value.getName(), Is.is("type"));
        assertThat(value.getValue(), Is.is("donut"));
        assertThat(value.getPath(), Is.is("/type"));
        assertThat(value.getFieldType(), Is.is(FieldType.STRING));
        assertThat(value.getStatus(), Is.is(FieldStatus.SUPPORTED));

        JsonField name = (JsonField) document.getFields().getField().get(2);
        assertNotNull(name);
        assertThat(name.getName(), Is.is("name"));
        assertThat(name.getValue(), Is.is("Cake"));
        assertThat(name.getPath(), Is.is("/name"));
        assertThat(name.getFieldType(), Is.is(FieldType.STRING));
        assertThat(name.getStatus(), Is.is(FieldStatus.SUPPORTED));

        JsonField itemPPU = (JsonField) document.getFields().getField().get(3);
        assertNotNull(itemPPU);
        assertThat(itemPPU.getName(), Is.is("ppu"));
        assertThat(itemPPU.getPath(), Is.is("/ppu"));
        assertThat(itemPPU.getValue(), Is.is(0.55));
        assertThat(itemPPU.getFieldType(), Is.is(FieldType.DOUBLE));
        assertThat(itemPPU.getStatus(), Is.is(FieldStatus.SUPPORTED));

        JsonComplexType batters = (JsonComplexType) document.getFields().getField().get(4);
        assertNotNull(batters);
        assertThat(batters.getJsonFields().getJsonField().size(), Is.is(1));

        JsonComplexType batterParent = (JsonComplexType) batters.getJsonFields().getJsonField().get(0);
        assertNotNull(batterParent);
        assertEquals("batter", batterParent.getName());
        assertEquals("/batters/batter<>", batterParent.getPath());
        assertEquals(CollectionType.LIST, batterParent.getCollectionType());
        assertThat(batterParent.getJsonFields().getJsonField().size(), Is.is(2));

        JsonField batterId = batterParent.getJsonFields().getJsonField().get(0);
        assertNotNull(batterId);
        assertThat(batterId.getName(), Is.is("id"));
        assertThat(batterId.getPath(), Is.is("/batters/batter<>/id"));
        assertThat(batterId.getFieldType(), Is.is(FieldType.STRING));
        assertThat(batterId.getStatus(), Is.is(FieldStatus.SUPPORTED));

        JsonField batterType = batterParent.getJsonFields().getJsonField().get(1);
        assertNotNull(batterType);
        assertThat(batterType.getName(), Is.is("type"));
        assertThat(batterType.getPath(), Is.is("/batters/batter<>/type"));
        assertThat(batterType.getFieldType(), Is.is(FieldType.STRING));
        assertThat(batterType.getStatus(), Is.is(FieldStatus.SUPPORTED));

        JsonComplexType topping = (JsonComplexType) document.getFields().getField().get(5);
        assertNotNull(topping);
        assertEquals("topping", topping.getName());
        assertEquals("/topping<>", topping.getPath());
        assertEquals(CollectionType.LIST, topping.getCollectionType());
        assertThat(topping.getJsonFields().getJsonField().size(), Is.is(2));

        JsonField toppingId = topping.getJsonFields().getJsonField().get(0);
        assertNotNull(toppingId);
        assertThat(toppingId.getName(), Is.is("id"));
        assertThat(toppingId.getPath(), Is.is("/topping<>/id"));
        assertThat(toppingId.getFieldType(), Is.is(FieldType.STRING));
        assertThat(toppingId.getStatus(), Is.is(FieldStatus.SUPPORTED));

        JsonField toppingType = topping.getJsonFields().getJsonField().get(1);
        assertNotNull(toppingType);
        assertThat(toppingType.getName(), Is.is("type"));
        assertThat(toppingType.getPath(), Is.is("/topping<>/type"));
        assertThat(toppingType.getFieldType(), Is.is(FieldType.STRING));
        assertThat(toppingType.getStatus(), Is.is(FieldStatus.SUPPORTED));
    }

    @Test
    public void inspectJsonDocumentHighlyComplexNestedObject() throws Exception {
        final String instance = new String(
                Files.readAllBytes(Paths.get("src/test/resources/inspect/highly-complex-nested-object.json")));
        JsonDocument document = inspectionService.inspectJsonDocument(instance);
        assertNotNull(document);
        assertThat(document.getFields().getField().size(), Is.is(1));

        JsonComplexType items = (JsonComplexType) document.getFields().getField().get(0);
        assertNotNull(items);
        assertThat(items.getFieldType(), Is.is(FieldType.COMPLEX));
        assertThat(items.getStatus(), Is.is(FieldStatus.SUPPORTED));
        assertThat(items.getName(), Is.is("items"));
        assertThat(items.getJsonFields().getJsonField().size(), Is.is(1));

        JsonComplexType item = (JsonComplexType) items.getJsonFields().getJsonField().get(0);
        assertNotNull(item);
        assertThat(item.getFieldType(), Is.is(FieldType.COMPLEX));
        assertEquals(CollectionType.LIST, item.getCollectionType());
        assertEquals("item", item.getName());
        assertEquals("/items/item<>", item.getPath());
        assertThat(item.getJsonFields().getJsonField().size(), Is.is(6));

        JsonField itemId = item.getJsonFields().getJsonField().get(0);
        assertNotNull(itemId);
        assertThat(itemId.getName(), Is.is("id"));
        assertThat(itemId.getValue(), Is.is("0001"));
        assertThat(itemId.getPath(), Is.is("/items/item<>/id"));
        assertThat(itemId.getFieldType(), Is.is(FieldType.STRING));
        assertThat(itemId.getStatus(), Is.is(FieldStatus.SUPPORTED));

        JsonField itemValue = item.getJsonFields().getJsonField().get(1);
        assertNotNull(itemValue);
        assertThat(itemValue.getName(), Is.is("type"));
        assertThat(itemValue.getValue(), Is.is("donut"));
        assertThat(itemValue.getPath(), Is.is("/items/item<>/type"));
        assertThat(itemValue.getFieldType(), Is.is(FieldType.STRING));
        assertThat(itemValue.getStatus(), Is.is(FieldStatus.SUPPORTED));

        JsonField itemName = item.getJsonFields().getJsonField().get(2);
        assertNotNull(itemName);
        assertThat(itemName.getName(), Is.is("name"));
        assertThat(itemName.getValue(), Is.is("Cake"));
        assertThat(itemName.getPath(), Is.is("/items/item<>/name"));
        assertThat(itemName.getFieldType(), Is.is(FieldType.STRING));
        assertThat(itemName.getStatus(), Is.is(FieldStatus.SUPPORTED));

        JsonField itemPPU = item.getJsonFields().getJsonField().get(3);
        assertNotNull(itemPPU);
        assertThat(itemPPU.getName(), Is.is("ppu"));
        assertThat(itemPPU.getPath(), Is.is("/items/item<>/ppu"));
        assertThat(itemPPU.getValue(), Is.is(0.55));
        assertThat(itemPPU.getFieldType(), Is.is(FieldType.DOUBLE));
        assertThat(itemPPU.getStatus(), Is.is(FieldStatus.SUPPORTED));

        JsonComplexType itemBattersComplexType = (JsonComplexType) item.getJsonFields().getJsonField().get(4);
        assertNotNull(itemBattersComplexType);
        assertThat(itemBattersComplexType.getFieldType(), Is.is(FieldType.COMPLEX));
        assertThat(itemBattersComplexType.getName(), Is.is("batters"));
        assertThat(itemBattersComplexType.getJsonFields().getJsonField().size(), Is.is(1));

        JsonComplexType itemBatterComplexType = (JsonComplexType) itemBattersComplexType.getJsonFields().getJsonField()
                .get(0);
        assertNotNull(itemBatterComplexType);
        assertThat(itemBatterComplexType.getFieldType(), Is.is(FieldType.COMPLEX));
        assertThat(itemBatterComplexType.getName(), Is.is("batter"));
        assertEquals(CollectionType.LIST, itemBatterComplexType.getCollectionType());
        assertEquals("/items/item<>/batters/batter<>", itemBatterComplexType.getPath());
        assertThat(itemBatterComplexType.getJsonFields().getJsonField().size(), Is.is(2));

        JsonField batterId = itemBatterComplexType.getJsonFields().getJsonField().get(0);
        assertNotNull(batterId);
        assertThat(batterId.getName(), Is.is("id"));
        assertThat(batterId.getValue(), Is.is("1001"));
        assertThat(batterId.getPath(), Is.is("/items/item<>/batters/batter<>/id"));
        assertThat(batterId.getFieldType(), Is.is(FieldType.STRING));
        assertThat(batterId.getStatus(), Is.is(FieldStatus.SUPPORTED));

        JsonField batterType = itemBatterComplexType.getJsonFields().getJsonField().get(1);
        assertNotNull(batterType);
        assertThat(batterType.getName(), Is.is("type"));
        assertThat(batterType.getValue(), Is.is("Regular"));
        assertThat(batterType.getPath(), Is.is("/items/item<>/batters/batter<>/type"));
        assertThat(batterType.getFieldType(), Is.is(FieldType.STRING));
        assertThat(batterType.getStatus(), Is.is(FieldStatus.SUPPORTED));

        JsonComplexType itemToppingComplexType = (JsonComplexType) item.getJsonFields().getJsonField().get(5);
        assertNotNull(itemToppingComplexType);
        assertThat(itemToppingComplexType.getFieldType(), Is.is(FieldType.COMPLEX));
        assertThat(itemToppingComplexType.getName(), Is.is("topping"));
        assertEquals(CollectionType.LIST, itemToppingComplexType.getCollectionType());
        assertEquals("/items/item<>/topping<>", itemToppingComplexType.getPath());
        assertThat(itemToppingComplexType.getJsonFields().getJsonField().size(), Is.is(2));

        JsonField toppingID = itemToppingComplexType.getJsonFields().getJsonField().get(0);
        assertNotNull(toppingID);
        assertThat(toppingID.getName(), Is.is("id"));
        assertThat(toppingID.getPath(), Is.is("/items/item<>/topping<>/id"));
        assertThat(toppingID.getFieldType(), Is.is(FieldType.STRING));
        assertThat(toppingID.getStatus(), Is.is(FieldStatus.SUPPORTED));

        JsonField toppingType = itemToppingComplexType.getJsonFields().getJsonField().get(1);
        assertNotNull(toppingType);
        assertThat(toppingType.getName(), Is.is("type"));
        assertThat(toppingType.getPath(), Is.is("/items/item<>/topping<>/type"));
        assertThat(toppingType.getFieldType(), Is.is(FieldType.STRING));
        assertThat(toppingType.getStatus(), Is.is(FieldStatus.SUPPORTED));
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
