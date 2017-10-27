package io.atlasmap.json.inspect;

import static org.junit.Assert.*;
import io.atlasmap.json.inspect.JsonDocumentInspectionService;
import io.atlasmap.json.v2.JsonComplexType;
import io.atlasmap.json.v2.JsonDocument;
import io.atlasmap.json.v2.JsonField;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldStatus;
import io.atlasmap.v2.FieldType;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;

/**
 */
public class SchemaInspectorTest {

    private final JsonDocumentInspectionService inspectionService = new JsonDocumentInspectionService();

    @Test(expected = IllegalArgumentException.class)
    public void inspectJsonSchema_Empty() throws Exception {
        final String schema = "";
        inspectionService.inspectJsonSchema(schema);
    }

    @Test(expected = IllegalArgumentException.class)
    public void inspectJsonSchema_WhitespaceOnly() throws Exception {
        final String schema = " ";
        inspectionService.inspectJsonSchema(schema);
    }

    @Test(expected = IllegalArgumentException.class)
    public void inspectJsonSchema_Null() throws Exception {
        final String schema = null;
        inspectionService.inspectJsonSchema(schema);
    }

    @Test(expected = JsonInspectionException.class)
    public void inspectJsonSchema_UnparseableMissingOpenCurly() throws Exception {
        final String schema = "\"$schema\": \"http://json-schema.org/\"}";
        inspectionService.inspectJsonSchema(schema);
    }

    @Test(expected = JsonInspectionException.class)
    public void inspectJsonSchema_UnparseableMissingClosingCurly() throws Exception {
        final String schema = "{\"$schema\": \"http://json-schema.org/\"";
        inspectionService.inspectJsonSchema(schema);
    }

    @Test
    public void inspectJsonSchema_emptyDocument() throws Exception {
        final String schema = "{\"$schema\": \"http://json-schema.org/\"}";
        JsonDocument document = inspectionService.inspectJsonSchema(schema);
        assertNotNull(document);
        assertEquals(0, document.getFields().getField().size());
    }

    @Test
    public void inspectJsonSchema_SimpleArray() throws Exception {
        final String schema =
                "{\"$schema\": \"http://json-schema.org/\","
                        + " \"type\": \"array\","
                        + " \"items\": { \"type\": \"integer\"}}";
        JsonDocument document = inspectionService.inspectJsonSchema(schema);
        assertNotNull(document);
        assertEquals(1, document.getFields().getField().size());
        JsonField jsonField = (JsonField) document.getFields().getField().get(0);
        assertEquals(FieldStatus.UNSUPPORTED, jsonField.getStatus());
        assertEquals(CollectionType.LIST, jsonField.getCollectionType());
        assertEquals(FieldType.INTEGER, jsonField.getFieldType());
        assertNull(jsonField.getName());
        assertNull(jsonField.getPath());
    }

    @Test
    public void inspectJsonSchema_SimpleString() throws Exception {
        final String schema =
                "{\"$schema\": \"http://json-schema.org/\", \"type\": \"string\"}";
        JsonDocument document = inspectionService.inspectJsonSchema(schema);
        assertNotNull(document);
        assertEquals(1, document.getFields().getField().size());
        JsonField jsonField = (JsonField) document.getFields().getField().get(0);
        assertTrue(jsonField.getStatus().compareTo(FieldStatus.SUPPORTED) == 0);
        assertEquals(FieldType.STRING, jsonField.getFieldType());
        assertNull(jsonField.getName());
        assertNull(jsonField.getPath());
    }

    @Test
    public void inspectFlatPrimitive_NoRoot() throws Exception {
        final String instance = new String(
                Files.readAllBytes(Paths.get("src/test/resources/inspect/schema/flatprimitive-base-unrooted.json")));
        JsonDocument document = inspectionService.inspectJsonSchema(instance);
        assertNotNull(document);
        assertEquals(5, document.getFields().getField().size());
        List<Field> fields = document.getFields().getField();
        JsonField field = (JsonField) fields.get(0);
        assertEquals("booleanField", field.getName());
        assertEquals("/booleanField", field.getPath());
        assertEquals(FieldType.BOOLEAN, field.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, field.getStatus());
        field = (JsonField) fields.get(1);
        assertEquals("stringField", field.getName());
        assertEquals("/stringField", field.getPath());
        assertEquals(FieldType.STRING, field.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, field.getStatus());
        field = (JsonField) fields.get(2);
        assertEquals("numberField", field.getName());
        assertEquals("/numberField", field.getPath());
        assertEquals(FieldType.NUMBER, field.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, field.getStatus());
        field = (JsonField) fields.get(3);
        assertEquals("intField", field.getName());
        assertEquals("/intField", field.getPath());
        assertEquals(FieldType.INTEGER, field.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, field.getStatus());
        field = (JsonField) fields.get(4);
        assertEquals("nullField", field.getName());
        assertEquals("/nullField", field.getPath());
        assertEquals(FieldType.NONE, field.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, field.getStatus());
    }

    @Test
    public void inspectFlatPrimitive_WithRoot() throws Exception {
        final String instance = new String(
                Files.readAllBytes(Paths.get("src/test/resources/inspect/schema/flatprimitive-base-rooted.json")));
        JsonDocument document = inspectionService.inspectJsonSchema(instance);
        assertNotNull(document);
        assertEquals(1, document.getFields().getField().size());
        JsonComplexType root = (JsonComplexType) document.getFields().getField().get(0);
        assertNotNull(root);
        assertEquals("SourceFlatPrimitive", root.getName());
        assertEquals("/SourceFlatPrimitive", root.getPath());
        assertEquals(FieldType.COMPLEX, root.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, root.getStatus());
        
        assertEquals(5, root.getJsonFields().getJsonField().size());
        List<JsonField> fields = root.getJsonFields().getJsonField();
        JsonField field = (JsonField) fields.get(0);
        assertEquals("booleanField", field.getName());
        assertEquals("/SourceFlatPrimitive/booleanField", field.getPath());
        assertEquals(FieldType.BOOLEAN, field.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, field.getStatus());
        field = (JsonField) fields.get(1);
        assertEquals("stringField", field.getName());
        assertEquals("/SourceFlatPrimitive/stringField", field.getPath());
        assertEquals(FieldType.STRING, field.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, field.getStatus());
        field = (JsonField) fields.get(2);
        assertEquals("numberField", field.getName());
        assertEquals("/SourceFlatPrimitive/numberField", field.getPath());
        assertEquals(FieldType.NUMBER, field.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, field.getStatus());
        field = (JsonField) fields.get(3);
        assertEquals("intField", field.getName());
        assertEquals("/SourceFlatPrimitive/intField", field.getPath());
        assertEquals(FieldType.INTEGER, field.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, field.getStatus());
        field = (JsonField) fields.get(4);
        assertEquals("nullField", field.getName());
        assertEquals("/SourceFlatPrimitive/nullField", field.getPath());
        assertEquals(FieldType.NONE, field.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, field.getStatus());
    }

    @Test
    public void inspectComplexObject_NoRoot() throws Exception {
        final String schema = new String(
                Files.readAllBytes(Paths.get("src/test/resources/inspect/schema/complex-object-unrooted.json")));
        JsonDocument document = inspectionService.inspectJsonSchema(schema);
        assertNotNull(document);
        assertEquals(3, document.getFields().getField().size());

        JsonComplexType address = (JsonComplexType) document.getFields().getField().get(0);
        assertNotNull(address);
        assertEquals(5, address.getJsonFields().getJsonField().size());

        JsonField address1 = address.getJsonFields().getJsonField().get(0);
        assertNotNull(address1);
        assertEquals("addressLine1", address1.getName());
        assertEquals("/address/addressLine1", address1.getPath());
        assertEquals(FieldType.STRING, address1.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, address1.getStatus());

        JsonField address2 = address.getJsonFields().getJsonField().get(1);
        assertNotNull(address2);
        assertEquals("addressLine2", address2.getName());
        assertEquals("/address/addressLine2", address2.getPath());
        assertEquals(FieldType.STRING, address2.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, address2.getStatus());

        JsonField city = address.getJsonFields().getJsonField().get(2);
        assertNotNull(city);
        assertEquals("city", city.getName());
        assertEquals("/address/city", city.getPath());
        assertEquals(FieldType.STRING, city.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, city.getStatus());

        JsonField state = address.getJsonFields().getJsonField().get(3);
        assertNotNull(state);
        assertEquals("state", state.getName());
        assertEquals("/address/state", state.getPath());
        assertEquals(FieldType.STRING, state.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, state.getStatus());

        JsonField postalCode = address.getJsonFields().getJsonField().get(4);
        assertNotNull(postalCode);
        assertEquals("zipCode", postalCode.getName());
        assertEquals("/address/zipCode", postalCode.getPath());
        assertEquals(FieldType.STRING, postalCode.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, postalCode.getStatus());

        JsonComplexType contact = (JsonComplexType) document.getFields().getField().get(1);
        assertNotNull(contact);
        assertEquals(4, contact.getJsonFields().getJsonField().size());

        JsonField firstName = contact.getJsonFields().getJsonField().get(0);
        assertNotNull(firstName);
        assertEquals("firstName", firstName.getName());
        assertEquals("/contact/firstName", firstName.getPath());
        assertEquals(FieldType.STRING, firstName.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, firstName.getStatus());

        JsonField lastName = contact.getJsonFields().getJsonField().get(1);
        assertNotNull(lastName);
        assertEquals("lastName", lastName.getName());
        assertEquals("/contact/lastName", lastName.getPath());
        assertEquals(FieldType.STRING, lastName.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, lastName.getStatus());

        JsonField phoneNumber = contact.getJsonFields().getJsonField().get(2);
        assertNotNull(phoneNumber);
        assertEquals("phoneNumber", phoneNumber.getName());
        assertEquals("/contact/phoneNumber", phoneNumber.getPath());
        assertEquals(FieldType.STRING, phoneNumber.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, phoneNumber.getStatus());

        JsonField zipCode = contact.getJsonFields().getJsonField().get(3);
        assertNotNull(zipCode);
        assertEquals("zipCode", zipCode.getName());
        assertEquals("/contact/zipCode", zipCode.getPath());
        assertEquals(FieldType.STRING, zipCode.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, zipCode.getStatus());

        JsonField orderId = (JsonField) document.getFields().getField().get(2);
        assertNotNull(orderId);
        assertEquals("orderId", orderId.getName());
        assertEquals("/orderId", orderId.getPath());
        assertEquals(FieldType.INTEGER, orderId.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, orderId.getStatus());
    }

    @Test
    public void inspectComplexObject_WithRoot() throws Exception {
        final String schema = new String(
                Files.readAllBytes(Paths.get("src/test/resources/inspect/schema/complex-object-rooted.json")));
        JsonDocument document = inspectionService.inspectJsonSchema(schema);
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
        assertEquals("/order/address/addressLine1", address1.getPath());
        assertEquals(FieldType.STRING, address1.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, address1.getStatus());

        JsonField address2 = address.getJsonFields().getJsonField().get(1);
        assertNotNull(address2);
        assertEquals("addressLine2", address2.getName());
        assertEquals("/order/address/addressLine2", address2.getPath());
        assertEquals(FieldType.STRING, address2.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, address2.getStatus());

        JsonField city = address.getJsonFields().getJsonField().get(2);
        assertNotNull(city);
        assertEquals("city", city.getName());
        assertEquals("/order/address/city", city.getPath());
        assertEquals(FieldType.STRING, city.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, city.getStatus());

        JsonField state = address.getJsonFields().getJsonField().get(3);
        assertNotNull(state);
        assertEquals("state", state.getName());
        assertEquals("/order/address/state", state.getPath());
        assertEquals(FieldType.STRING, state.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, state.getStatus());

        JsonField postalCode = address.getJsonFields().getJsonField().get(4);
        assertNotNull(postalCode);
        assertEquals("zipCode", postalCode.getName());
        assertEquals("/order/address/zipCode", postalCode.getPath());
        assertEquals(FieldType.STRING, postalCode.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, postalCode.getStatus());

        JsonComplexType contact = (JsonComplexType) root.getJsonFields().getJsonField().get(1);
        assertNotNull(contact);
        assertEquals(4, contact.getJsonFields().getJsonField().size());

        JsonField firstName = contact.getJsonFields().getJsonField().get(0);
        assertNotNull(firstName);
        assertEquals("firstName", firstName.getName());
        assertEquals("/order/contact/firstName", firstName.getPath());
        assertEquals(FieldType.STRING, firstName.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, firstName.getStatus());

        JsonField lastName = contact.getJsonFields().getJsonField().get(1);
        assertNotNull(lastName);
        assertEquals("lastName", lastName.getName());
        assertEquals("/order/contact/lastName", lastName.getPath());
        assertEquals(FieldType.STRING, lastName.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, lastName.getStatus());

        JsonField phoneNumber = contact.getJsonFields().getJsonField().get(2);
        assertNotNull(phoneNumber);
        assertEquals("phoneNumber", phoneNumber.getName());
        assertEquals("/order/contact/phoneNumber", phoneNumber.getPath());
        assertEquals(FieldType.STRING, phoneNumber.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, phoneNumber.getStatus());

        JsonField zipCode = contact.getJsonFields().getJsonField().get(3);
        assertNotNull(zipCode);
        assertEquals("zipCode", zipCode.getName());
        assertEquals("/order/contact/zipCode", zipCode.getPath());
        assertEquals(FieldType.STRING, zipCode.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, zipCode.getStatus());

        JsonField orderId = root.getJsonFields().getJsonField().get(2);
        assertNotNull(orderId);
        assertEquals("orderId", orderId.getName());
        assertEquals("/order/orderId", orderId.getPath());
        assertEquals(FieldType.INTEGER, orderId.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, orderId.getStatus());
    }

    @Test
    public void inspectObjectArray_WithRoot() throws Exception {
        final String schema = new String(
                Files.readAllBytes(Paths.get("src/test/resources/inspect/schema/complex-array-rooted.json")));
        JsonDocument document = inspectionService.inspectJsonSchema(schema);
        assertNotNull(document);
        assertEquals(1, document.getFields().getField().size());
        JsonComplexType root = (JsonComplexType) document.getFields().getField().get(0);
        assertNotNull(root);
        assertEquals(3, root.getJsonFields().getJsonField().size());
        assertEquals("SourceOrderList", root.getName());

        JsonComplexType orders = (JsonComplexType) root.getJsonFields().getJsonField().get(0);
        assertNotNull(orders);
        assertEquals(3, orders.getJsonFields().getJsonField().size());
        assertEquals("orders", orders.getName());
        assertEquals(CollectionType.LIST, orders.getCollectionType());
        assertEquals(FieldType.COMPLEX, orders.getFieldType());

        JsonField orderBatchNumber = root.getJsonFields().getJsonField().get(1);
        assertNotNull(orderBatchNumber);
        assertEquals("orderBatchNumber", orderBatchNumber.getName());
        assertEquals("/SourceOrderList/orderBatchNumber", orderBatchNumber.getPath());
        assertEquals(FieldType.INTEGER, orderBatchNumber.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, orderBatchNumber.getStatus());

        JsonField numberOrders = root.getJsonFields().getJsonField().get(2);
        assertNotNull(numberOrders);
        assertEquals("numberOrders", numberOrders.getName());
        assertEquals("/SourceOrderList/numberOrders", numberOrders.getPath());
        assertEquals(FieldType.INTEGER, numberOrders.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, numberOrders.getStatus());

        JsonComplexType address = (JsonComplexType) orders.getJsonFields().getJsonField().get(0);
        assertNotNull(address);
        assertEquals(5, address.getJsonFields().getJsonField().size());
        assertEquals("address", address.getName());
        assertEquals("/SourceOrderList/orders/address", address.getPath());

        JsonField addressLine1 = address.getJsonFields().getJsonField().get(0);
        assertNotNull(addressLine1);
        assertEquals("addressLine1", addressLine1.getName());
        assertEquals("/SourceOrderList/orders/address/addressLine1", addressLine1.getPath());
        assertEquals(FieldType.STRING, addressLine1.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, addressLine1.getStatus());

        JsonField addressLine2 = address.getJsonFields().getJsonField().get(1);
        assertNotNull(addressLine2);
        assertEquals("addressLine2", addressLine2.getName());
        assertEquals("/SourceOrderList/orders/address/addressLine2", addressLine2.getPath());
        assertEquals(FieldType.STRING, addressLine2.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, addressLine2.getStatus());

        JsonField city = address.getJsonFields().getJsonField().get(2);
        assertNotNull(city);
        assertEquals("city", city.getName());
        assertEquals("/SourceOrderList/orders/address/city", city.getPath());
        assertEquals(FieldType.STRING, city.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, city.getStatus());

        JsonField state = address.getJsonFields().getJsonField().get(3);
        assertNotNull(state);
        assertEquals("state", state.getName());
        assertEquals("/SourceOrderList/orders/address/state", state.getPath());
        assertEquals(FieldType.STRING, state.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, state.getStatus());

        JsonField postalCode = address.getJsonFields().getJsonField().get(4);
        assertNotNull(postalCode);
        assertEquals("zipCode", postalCode.getName());
        assertEquals("/SourceOrderList/orders/address/zipCode", postalCode.getPath());
        assertEquals(FieldType.STRING, postalCode.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, postalCode.getStatus());

        JsonComplexType contact = (JsonComplexType) orders.getJsonFields().getJsonField().get(1);
        assertNotNull(contact);
        assertEquals(4, contact.getJsonFields().getJsonField().size());
        assertEquals("contact", contact.getName());
        assertEquals("/SourceOrderList/orders/contact", contact.getPath());

        JsonField firstName = contact.getJsonFields().getJsonField().get(0);
        assertNotNull(firstName);
        assertEquals("firstName", firstName.getName());
        assertEquals("/SourceOrderList/orders/contact/firstName", firstName.getPath());
        assertEquals(FieldType.STRING, firstName.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, firstName.getStatus());

        JsonField lastName = contact.getJsonFields().getJsonField().get(1);
        assertNotNull(lastName);
        assertEquals("lastName", lastName.getName());
        assertEquals("/SourceOrderList/orders/contact/lastName", lastName.getPath());
        assertEquals(FieldType.STRING, lastName.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, lastName.getStatus());

        JsonField phoneNumber = contact.getJsonFields().getJsonField().get(2);
        assertNotNull(phoneNumber);
        assertEquals("phoneNumber", phoneNumber.getName());
        assertEquals("/SourceOrderList/orders/contact/phoneNumber", phoneNumber.getPath());
        assertEquals(FieldType.STRING, phoneNumber.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, phoneNumber.getStatus());

        JsonField zipCode = contact.getJsonFields().getJsonField().get(3);
        assertNotNull(zipCode);
        assertEquals("zipCode", zipCode.getName());
        assertEquals("/SourceOrderList/orders/contact/zipCode", zipCode.getPath());
        assertEquals(FieldType.STRING, zipCode.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, zipCode.getStatus());

        JsonField orderId = orders.getJsonFields().getJsonField().get(2);
        assertNotNull(orderId);
        assertEquals("orderId", orderId.getName());
        assertEquals("/SourceOrderList/orders/orderId", orderId.getPath());
        assertEquals(FieldType.INTEGER, orderId.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, orderId.getStatus());
    }

    @Test
    public void inspectJsonSchema_ref() throws Exception {
        final String schema = new String(Files
                .readAllBytes(Paths.get("src/test/resources/inspect/schema/ref.json")));
        JsonDocument document = inspectionService.inspectJsonSchema(schema);
        assertNotNull(document);
        assertEquals(3, document.getFields().getField().size());
        
        JsonField ref_a = (JsonField) document.getFields().getField().get(0);
        assertNotNull(ref_a);
        assertEquals("ref-a", ref_a.getName());
        assertEquals("/ref-a", ref_a.getPath());
        assertEquals(FieldType.STRING, ref_a.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, ref_a.getStatus());
        
        JsonComplexType ref_b = (JsonComplexType) document.getFields().getField().get(1);
        assertNotNull(ref_b);
        assertEquals("ref-b", ref_b.getName());
        assertEquals("/ref-b", ref_b.getPath());
        assertEquals(FieldType.COMPLEX, ref_b.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, ref_b.getStatus());
        
        assertEquals(2, ref_b.getJsonFields().getJsonField().size());
        JsonComplexType ref_c_from_b = (JsonComplexType) ref_b.getJsonFields().getJsonField().get(0);
        assertNotNull(ref_c_from_b);
        assertEquals("ref-c-from-b", ref_c_from_b.getName());
        assertEquals("/ref-b/ref-c-from-b", ref_c_from_b.getPath());
        assertEquals(FieldType.COMPLEX, ref_c_from_b.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, ref_c_from_b.getStatus());
        
        assertEquals(1, ref_c_from_b.getJsonFields().getJsonField().size());
        JsonField str_c_from_b = ref_c_from_b.getJsonFields().getJsonField().get(0);
        assertNotNull(str_c_from_b);
        assertEquals("str-c", str_c_from_b.getName());
        assertEquals("/ref-b/ref-c-from-b/str-c", str_c_from_b.getPath());
        assertEquals(FieldType.STRING, str_c_from_b.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, str_c_from_b.getStatus());
        
        JsonField str_b = ref_b.getJsonFields().getJsonField().get(1);
        assertNotNull(str_b);
        assertEquals("str-b", str_b.getName());
        assertEquals("/ref-b/str-b", str_b.getPath());
        assertEquals(FieldType.STRING, str_b.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, str_b.getStatus());
        
        
        JsonComplexType ref_c = (JsonComplexType) document.getFields().getField().get(2);
        assertNotNull(ref_c);
        assertEquals("ref-c", ref_c.getName());
        assertEquals("/ref-c", ref_c.getPath());
        assertEquals(FieldType.COMPLEX, ref_c.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, ref_c.getStatus());
        assertEquals(1, ref_c.getJsonFields().getJsonField().size());
        JsonField str_c = ref_c.getJsonFields().getJsonField().get(0);
        assertNotNull(str_c);
        assertEquals("str-c", str_c.getName());
        assertEquals("/ref-c/str-c", str_c.getPath());
        assertEquals(FieldType.STRING, str_c.getFieldType());
        assertEquals(FieldStatus.SUPPORTED, str_c.getStatus());
    }

    // examples from json-schema.org
    @Test
    public void inspectJsonSchema_geo() throws Exception {
        final String schema = new String(Files
                .readAllBytes(Paths.get("src/test/resources/inspect/schema/geo.json")));
        JsonDocument document = inspectionService.inspectJsonSchema(schema);
        List<Field> fields = document.getFields().getField();
        JsonField f = (JsonField) fields.get(0);
        assertEquals("latitude", f.getName());
        assertEquals("/latitude", f.getPath());
        assertEquals(FieldType.NUMBER, f.getFieldType());
        f = (JsonField) fields.get(1);
        assertEquals("longitude", f.getName());
        assertEquals("/longitude", f.getPath());
        assertEquals(FieldType.NUMBER, f.getFieldType());
    }

    @Test
    public void inspectJsonSchema_address() throws Exception {
        final String schema = new String(Files
                .readAllBytes(Paths.get("src/test/resources/inspect/schema/address.json")));
        JsonDocument document = inspectionService.inspectJsonSchema(schema);
        List<Field> fields = document.getFields().getField();
        JsonField f = (JsonField)fields.get(0);
        assertEquals("post-office-box", f.getName());
        assertEquals("/post-office-box", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonField) fields.get(1);
        assertEquals("extended-address", f.getName());
        assertEquals("/extended-address", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonField) fields.get(2);
        assertEquals("street-address", f.getName());
        assertEquals("/street-address", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonField) fields.get(3);
        assertEquals("locality", f.getName());
        assertEquals("/locality", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonField) fields.get(4);
        assertEquals("region", f.getName());
        assertEquals("/region", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonField) fields.get(5);
        assertEquals("postal-code", f.getName());
        assertEquals("/postal-code", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonField) fields.get(6);
        assertEquals("country-name", f.getName());
        assertEquals("/country-name", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
    }

    @Ignore("internet access")
    @Test
    public void inspectJsonSchema_calendar_external() throws Exception {
        final String instance = new String(Files
                .readAllBytes(Paths.get("src/test/resources/inspect/schema/calendar.json")));
        doInspectJsonSchema_calendar(instance);
    }

    @Test
    public void inspectJsonSchema_calendar_internal() throws Exception {
        final String instance = new String(Files
                .readAllBytes(Paths.get("src/test/resources/inspect/schema/calendar-internal.json")));
        doInspectJsonSchema_calendar(instance);
    }

    @Test
    public void inspectJsonSchema_calendar_inline() throws Exception {
        final String instance = new String(Files
                .readAllBytes(Paths.get("src/test/resources/inspect/schema/calendar-inline.json")));
        doInspectJsonSchema_calendar(instance);
    }

    private void doInspectJsonSchema_calendar(String instance) throws Exception {
        JsonDocument document = inspectionService.inspectJsonSchema(instance);
        List<Field> fields = document.getFields().getField();
        JsonField f = (JsonField) fields.get(0);
        assertEquals("dtstart", f.getName());
        assertEquals("/dtstart", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonField) fields.get(1);
        assertEquals("dtend", f.getName());
        assertEquals("/dtend", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonField) fields.get(2);
        assertEquals("summary", f.getName());
        assertEquals("/summary", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonField) fields.get(3);
        assertEquals("location", f.getName());
        assertEquals("/location", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonField) fields.get(4);
        assertEquals("url", f.getName());
        assertEquals("/url", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonField) fields.get(5);
        assertEquals("duration", f.getName());
        assertEquals("/duration", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonField) fields.get(6);
        assertEquals("rdate", f.getName());
        assertEquals("/rdate", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonField) fields.get(7);
        assertEquals("rrule", f.getName());
        assertEquals("/rrule", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonField) fields.get(8);
        assertEquals("category", f.getName());
        assertEquals("/category", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonField) fields.get(9);
        assertEquals("description", f.getName());
        assertEquals("/description", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonComplexType) fields.get(10);
        assertEquals("geo", f.getName());
        assertEquals("/geo", f.getPath());
        assertEquals(FieldType.COMPLEX, f.getFieldType());
        List<JsonField> geofields = ((JsonComplexType)f).getJsonFields().getJsonField();
        f = (JsonField) geofields.get(0);
        assertEquals("latitude", f.getName());
        assertEquals("/geo/latitude", f.getPath());
        assertEquals(FieldType.NUMBER, f.getFieldType());
        f = (JsonField) geofields.get(1);
        assertEquals("longitude", f.getName());
        assertEquals("/geo/longitude", f.getPath());
        assertEquals(FieldType.NUMBER, f.getFieldType());
    }

    @Ignore("internet access")
    @Test
    public void inspectJsonSchema_card_external() throws Exception {
        final String schema = new String(Files
                .readAllBytes(Paths.get("src/test/resources/inspect/schema/card.json")));
        doInspectJsonSchema_card(schema);
    }

    @Test
    public void inspectJsonSchema_card_internal() throws Exception {
        final String schema = new String(Files
                .readAllBytes(Paths.get("src/test/resources/inspect/schema/card-internal.json")));
        doInspectJsonSchema_card(schema);
    }

    @Test
    public void inspectJsonSchema_card_inline() throws Exception {
        final String schema = new String(Files
                .readAllBytes(Paths.get("src/test/resources/inspect/schema/card-inline.json")));
        doInspectJsonSchema_card(schema);
    }

    private void doInspectJsonSchema_card(String schema) throws Exception {
        JsonDocument document = inspectionService.inspectJsonSchema(schema);
        List<Field> fields = document.getFields().getField();
        JsonField f = (JsonField) fields.get(0);
        assertEquals("fn", f.getName());
        assertEquals("/fn", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonField) fields.get(1);
        assertEquals("familyName", f.getName());
        assertEquals("/familyName", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonField) fields.get(2);
        assertEquals("givenName", f.getName());
        assertEquals("/givenName", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonField) fields.get(3);
        assertEquals("additionalName", f.getName());
        assertEquals("/additionalName", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        assertEquals(CollectionType.LIST, f.getCollectionType());
        f = (JsonField) fields.get(4);
        assertEquals("honorificPrefix", f.getName());
        assertEquals("/honorificPrefix", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        assertEquals(CollectionType.LIST, f.getCollectionType());
        f = (JsonField) fields.get(5);
        assertEquals("honorificSuffix", f.getName());
        assertEquals("/honorificSuffix", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        assertEquals(CollectionType.LIST, f.getCollectionType());
        f = (JsonField) fields.get(6);
        assertEquals("nickname", f.getName());
        assertEquals("/nickname", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonField) fields.get(7);
        assertEquals("url", f.getName());
        assertEquals("/url", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonComplexType) fields.get(8);
        assertEquals("email", f.getName());
        assertEquals("/email", f.getPath());
        assertEquals(FieldType.COMPLEX, f.getFieldType());
        List<JsonField> emailfields = ((JsonComplexType)f).getJsonFields().getJsonField();
        f = (JsonField) emailfields.get(0);
        assertEquals("type", f.getName());
        assertEquals("/email/type", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonField) emailfields.get(1);
        assertEquals("value", f.getName());
        assertEquals("/email/value", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonComplexType) fields.get(9);
        assertEquals("tel", f.getName());
        assertEquals("/tel", f.getPath());
        assertEquals(FieldType.COMPLEX, f.getFieldType());
        List<JsonField> telfields = ((JsonComplexType)f).getJsonFields().getJsonField();
        f = (JsonField) telfields.get(0);
        assertEquals("type", f.getName());
        assertEquals("/tel/type", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonField) telfields.get(1);
        assertEquals("value", f.getName());
        assertEquals("/tel/value", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonComplexType) fields.get(10);
        assertEquals("adr", f.getName());
        assertEquals("/adr", f.getPath());
        assertEquals(FieldType.COMPLEX, f.getFieldType());
        List<JsonField> addrfields = ((JsonComplexType)f).getJsonFields().getJsonField();
        f = (JsonField) addrfields.get(0);
        assertEquals("post-office-box", f.getName());
        assertEquals("/adr/post-office-box", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonField) addrfields.get(1);
        assertEquals("extended-address", f.getName());
        assertEquals("/adr/extended-address", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonField) addrfields.get(2);
        assertEquals("street-address", f.getName());
        assertEquals("/adr/street-address", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonField) addrfields.get(3);
        assertEquals("locality", f.getName());
        assertEquals("/adr/locality", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonField) addrfields.get(4);
        assertEquals("region", f.getName());
        assertEquals("/adr/region", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonField) addrfields.get(5);
        assertEquals("postal-code", f.getName());
        assertEquals("/adr/postal-code", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonField) addrfields.get(6);
        assertEquals("country-name", f.getName());
        assertEquals("/adr/country-name", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonComplexType) fields.get(11);
        assertEquals("geo", f.getName());
        assertEquals("/geo", f.getPath());
        assertEquals(FieldType.COMPLEX, f.getFieldType());
        List<JsonField> geofields = ((JsonComplexType)f).getJsonFields().getJsonField();
        f = (JsonField) geofields.get(0);
        assertEquals("latitude", f.getName());
        assertEquals("/geo/latitude", f.getPath());
        assertEquals(FieldType.NUMBER, f.getFieldType());
        f = (JsonField) geofields.get(1);
        assertEquals("longitude", f.getName());
        assertEquals("/geo/longitude", f.getPath());
        assertEquals(FieldType.NUMBER, f.getFieldType());
        f = (JsonField) fields.get(12);
        assertEquals("tz", f.getName());
        assertEquals("/tz", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonField) fields.get(13);
        assertEquals("photo", f.getName());
        assertEquals("/photo", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonField) fields.get(14);
        assertEquals("logo", f.getName());
        assertEquals("/logo", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonField) fields.get(15);
        assertEquals("sound", f.getName());
        assertEquals("/sound", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonField) fields.get(16);
        assertEquals("bday", f.getName());
        assertEquals("/bday", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonField) fields.get(17);
        assertEquals("title", f.getName());
        assertEquals("/title", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonField) fields.get(18);
        assertEquals("role", f.getName());
        assertEquals("/role", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonComplexType) fields.get(19);
        assertEquals("org", f.getName());
        assertEquals("/org", f.getPath());
        assertEquals(FieldType.COMPLEX, f.getFieldType());
        List<JsonField> orgfields = ((JsonComplexType)f).getJsonFields().getJsonField();
        f = (JsonField) orgfields.get(0);
        assertEquals("organizationName", f.getName());
        assertEquals("/org/organizationName", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonField) orgfields.get(1);
        assertEquals("organizationUnit", f.getName());
        assertEquals("/org/organizationUnit", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
    }
}
