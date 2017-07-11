package io.atlasmap.json.v2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.atlasmap.api.AtlasException;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.FieldStatus;
import io.atlasmap.v2.FieldType;
import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 */
public class DocumentJsonFieldWriterTest {

    private static DocumentJsonFieldWriter writer = new DocumentJsonFieldWriter();

    @Test(expected = AtlasException.class)
    public void testWriteNullField() throws Exception {
        JsonNode jsonDocument = writer.write(null);
    }

    @Test
    public void testWriteSimpleObjectNoRoot() throws Exception {
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setName("brand");
        field.setPath("/brand");
        field.setValue("Mercedes");
        field.setFieldType(FieldType.STRING);

        JsonNode rootNode = writer.write(field);
        Assert.assertNotNull(rootNode);
        Assert.assertThat(rootNode.toString(), Is.is("{\"brand\":\"Mercedes\"}"));

        JsonField field2 = AtlasJsonModelFactory.createJsonField();
        field2.setName("doors");
        field2.setPath("/doors");
        field2.setValue(5);
        field2.setFieldType(FieldType.INTEGER);
        writer.write(field2, rootNode);
        Assert.assertThat(rootNode.toString(), Is.is("{\"brand\":\"Mercedes\",\"doors\":5}"));

    }

    @Test
    public void testWriteSimpleObjectWithRoot() throws Exception {
        JsonField field1 = AtlasJsonModelFactory.createJsonField();
        field1.setName("brand");
        field1.setPath("/car/brand");
        field1.setValue("Mercedes");
        field1.setFieldType(FieldType.STRING);
        JsonNode rootNode = writer.write(field1);

        JsonField field2 = AtlasJsonModelFactory.createJsonField();
        field2.setName("doors");
        field2.setPath("/car/doors");
        field2.setValue(5);
        field2.setFieldType(FieldType.INTEGER);
        writer.write(field2, rootNode);

        Assert.assertThat(rootNode.toString(), Is.is("{\"car\":{\"brand\":\"Mercedes\",\"doors\":5}}"));
    }

    @Test
    public void testWriteFlatPrimitiveObject_Unrooted() throws Exception {

        JsonField booleanField = AtlasJsonModelFactory.createJsonField();
        booleanField.setFieldType(FieldType.BOOLEAN);
        booleanField.setName("booleanField");
        booleanField.setValue(false);
        booleanField.setPath("/booleanField");
        booleanField.setStatus(FieldStatus.SUPPORTED);

        JsonNode root = writer.write(booleanField);
        Assert.assertNotNull(root);

        JsonField charField = AtlasJsonModelFactory.createJsonField();
        charField.setFieldType(FieldType.CHAR);
        charField.setName("charField");
        charField.setValue("a");
        charField.setPath("/charField");
        charField.setStatus(FieldStatus.SUPPORTED);
        writer.write(charField, root);

        JsonField doubleField = AtlasJsonModelFactory.createJsonField();
        doubleField.setFieldType(FieldType.DOUBLE);
        doubleField.setName("doubleField");
        doubleField.setValue(-27152745.3422);
        doubleField.setPath("/doubleField");
        doubleField.setStatus(FieldStatus.SUPPORTED);
        writer.write(doubleField, root);

        JsonField floatField = AtlasJsonModelFactory.createJsonField();
        floatField.setFieldType(FieldType.FLOAT);
        floatField.setName("floatField");
        floatField.setValue(-63988281.00);
        floatField.setPath("/floatField");
        floatField.setStatus(FieldStatus.SUPPORTED);
        writer.write(floatField, root);

        JsonField intField = AtlasJsonModelFactory.createJsonField();
        intField.setFieldType(FieldType.INTEGER);
        intField.setName("intField");
        intField.setValue(8281);
        intField.setPath("/intField");
        intField.setStatus(FieldStatus.SUPPORTED);
        writer.write(intField, root);

        JsonField shortField = AtlasJsonModelFactory.createJsonField();
        shortField.setFieldType(FieldType.SHORT);
        shortField.setName("shortField");
        shortField.setValue(81);
        shortField.setPath("/shortField");
        shortField.setStatus(FieldStatus.SUPPORTED);
        writer.write(shortField, root);

        JsonField longField = AtlasJsonModelFactory.createJsonField();
        longField.setFieldType(FieldType.LONG);
        longField.setName("longField");
        longField.setValue(3988281);
        longField.setPath("/longField");
        longField.setStatus(FieldStatus.SUPPORTED);
        writer.write(longField, root);

        Assert.assertThat(root.toString(), Is.is("{\"booleanField\":false,\"charField\":\"a\",\"doubleField\":-27152745.3422,\"floatField\":-63988281,\"intField\":8281,\"shortField\":81,\"longField\":3988281}"));
    }

    @Test
    public void testWriteFlatPrimitiveObject_Rooted() throws Exception {

        JsonComplexType parent = new JsonComplexType();
        parent.setName("SourceFlatPrimitive");
        parent.setPath("/SourceFlatPrimitive");
        parent.setFieldType(FieldType.COMPLEX);
        parent.setJsonFields(new JsonFields());

        JsonField booleanField = AtlasJsonModelFactory.createJsonField();
        booleanField.setFieldType(FieldType.BOOLEAN);
        booleanField.setName("booleanField");
        booleanField.setValue(false);
        booleanField.setPath("/SourceFlatPrimitive/booleanField");
        booleanField.setStatus(FieldStatus.SUPPORTED);
        parent.getJsonFields().getJsonField().add(booleanField);


        JsonField charField = AtlasJsonModelFactory.createJsonField();
        charField.setFieldType(FieldType.CHAR);
        charField.setName("charField");
        charField.setValue("a");
        charField.setPath("/SourceFlatPrimitive/charField");
        charField.setStatus(FieldStatus.SUPPORTED);
        parent.getJsonFields().getJsonField().add(charField);

        JsonNode root = writer.write(parent);
        Assert.assertNotNull(root);

        JsonField doubleField = AtlasJsonModelFactory.createJsonField();
        doubleField.setFieldType(FieldType.DOUBLE);
        doubleField.setName("doubleField");
        doubleField.setValue(-27152745.3422);
        doubleField.setPath("/SourceFlatPrimitive/doubleField");
        doubleField.setStatus(FieldStatus.SUPPORTED);
        writer.write(doubleField, root);

        JsonField floatField = AtlasJsonModelFactory.createJsonField();
        floatField.setFieldType(FieldType.FLOAT);
        floatField.setName("floatField");
        floatField.setValue(-63988281.00);
        floatField.setPath("/SourceFlatPrimitive/floatField");
        floatField.setStatus(FieldStatus.SUPPORTED);
        writer.write(floatField, root);

        JsonField intField = AtlasJsonModelFactory.createJsonField();
        intField.setFieldType(FieldType.INTEGER);
        intField.setName("intField");
        intField.setValue(8281);
        intField.setPath("/SourceFlatPrimitive/intField");
        intField.setStatus(FieldStatus.SUPPORTED);
        writer.write(intField, root);

        JsonField shortField = AtlasJsonModelFactory.createJsonField();
        shortField.setFieldType(FieldType.SHORT);
        shortField.setName("shortField");
        shortField.setValue(81);
        shortField.setPath("/SourceFlatPrimitive/shortField");
        shortField.setStatus(FieldStatus.SUPPORTED);
        writer.write(shortField, root);

        JsonField longField = AtlasJsonModelFactory.createJsonField();
        longField.setFieldType(FieldType.LONG);
        longField.setName("longField");
        longField.setValue(3988281);
        longField.setPath("/SourceFlatPrimitive/longField");
        longField.setStatus(FieldStatus.SUPPORTED);
        writer.write(longField, root);

        Assert.assertThat(root.toString(), Is.is("{\"SourceFlatPrimitive\":{\"booleanField\":false,\"charField\":\"a\",\"doubleField\":-27152745.3422,\"floatField\":-63988281,\"intField\":8281,\"shortField\":81,\"longField\":3988281}}"));
    }

    @Test
    public void testWriteComplexObject_Unrooted() throws Exception {

        JsonComplexType address = new JsonComplexType();
        address.setName("address");
        address.setPath("/address");
        address.setFieldType(FieldType.COMPLEX);
        address.setJsonFields(new JsonFields());
        setAddress(address, "", 0);
        JsonNode root = writer.write(address);

        JsonComplexType contact = new JsonComplexType();
        contact.setName("contact");
        contact.setPath("/contact");
        contact.setFieldType(FieldType.COMPLEX);
        contact.setJsonFields(new JsonFields());
        setContact(contact, "", 0);
        writer.write(contact, root);

        JsonField orderId = AtlasJsonModelFactory.createJsonField();
        orderId.setFieldType(FieldType.INTEGER);
        orderId.setName("orderId");
        orderId.setValue(0);
        orderId.setPath("/orderId");
        orderId.setStatus(FieldStatus.SUPPORTED);
        writer.write(orderId, root);

        Assert.assertThat(root.toString(), Is.is("{\"address\":{\"addressLine1\":\"123 Main St\",\"addressLine2\":\"Suite 42b\",\"city\":\"Anytown\",\"state\":\"NY\",\"zipCode\":\"90210\"},\"contact\":{\"firstName\":\"Ozzie\",\"lastName\":\"Smith\",\"phoneNumber\":\"5551212\",\"zipCode\":\"81111\"},\"orderId\":0}"));
    }

    @Test
    public void testWriteComplexObject_Rooted() throws Exception {

        JsonComplexType order = new JsonComplexType();
        order.setName("order");
        order.setPath("/order");
        order.setFieldType(FieldType.COMPLEX);
        order.setJsonFields(new JsonFields());

        JsonComplexType address = new JsonComplexType();
        address.setName("address");
        address.setPath("/order/address");
        address.setFieldType(FieldType.COMPLEX);
        address.setJsonFields(new JsonFields());
        order.getJsonFields().getJsonField().add(address);

        setAddress(address, "order", 0);

        JsonComplexType contact = new JsonComplexType();
        contact.setName("contact");
        contact.setPath("/order/contact");
        contact.setFieldType(FieldType.COMPLEX);
        contact.setJsonFields(new JsonFields());
        order.getJsonFields().getJsonField().add(contact);

        setContact(contact, "order", 0);

        JsonField orderId = AtlasJsonModelFactory.createJsonField();
        orderId.setFieldType(FieldType.INTEGER);
        orderId.setName("orderId");
        orderId.setValue(0);
        orderId.setPath("/order/orderId");
        orderId.setStatus(FieldStatus.SUPPORTED);
        order.getJsonFields().getJsonField().add(orderId);
        JsonNode root = writer.write(order);

        final String instance = new String(Files.readAllBytes(Paths.get("src/test/resources/complex-rooted-result.json")));
        Assert.assertNotNull(instance);

        Assert.assertThat(prettyPrintJson(root.toString()), Is.is(prettyPrintJson(instance)));
    }

    @Test
    public void testWriteComplexObject_Repeated() throws Exception {

        JsonComplexType jsonComplexType = new JsonComplexType();
        jsonComplexType.setName("SourceOrderList");
        jsonComplexType.setPath("/SourceOrderList");
        jsonComplexType.setFieldType(FieldType.COMPLEX);
        jsonComplexType.setJsonFields(new JsonFields());

        JsonComplexType orders = new JsonComplexType();
        orders.setName("orders");
        orders.setPath("/SourceOrderList/orders");
        orders.setFieldType(FieldType.COMPLEX);
        orders.setCollectionType(CollectionType.LIST);
        orders.setJsonFields(new JsonFields());
        jsonComplexType.getJsonFields().getJsonField().add(orders);

        JsonComplexType address = new JsonComplexType();
        address.setName("address");
        address.setPath("/SourceOrderList/orders/address");
        address.setFieldType(FieldType.COMPLEX);
        address.setJsonFields(new JsonFields());
        orders.getJsonFields().getJsonField().add(address);

        setAddress(address, "SourceOrderList", 0);

        JsonComplexType contact = new JsonComplexType();
        contact.setName("contact");
        contact.setPath("/SourceOrderList/orders/contact");
        contact.setFieldType(FieldType.COMPLEX);
        contact.setJsonFields(new JsonFields());
        orders.getJsonFields().getJsonField().add(contact);

        setContact(contact, "SourceOrderList", 0);

        JsonField orderId0 = AtlasJsonModelFactory.createJsonField();
        orderId0.setFieldType(FieldType.INTEGER);
        orderId0.setName("orderId");
        orderId0.setValue(0);
        orderId0.setPath("/SourceOrderList/orders/orderId");
        orderId0.setStatus(FieldStatus.SUPPORTED);
        orders.getJsonFields().getJsonField().add(orderId0);

        //repeat
        JsonComplexType address1_1 = new JsonComplexType();
        address1_1.setName("address");
        address1_1.setPath("/SourceOrderList/orders[1]/address");
        address1_1.setFieldType(FieldType.COMPLEX);
        address1_1.setJsonFields(new JsonFields());
        orders.getJsonFields().getJsonField().add(address1_1);

        setAddress(address1_1, "SourceOrderList", 1);

        JsonComplexType contact1_1 = new JsonComplexType();
        contact1_1.setName("contact");
        contact1_1.setPath("/SourceOrderList/orders[1]/contact");
        contact1_1.setFieldType(FieldType.COMPLEX);
        contact1_1.setJsonFields(new JsonFields());
        orders.getJsonFields().getJsonField().add(contact1_1);

        setContact(contact1_1, "SourceOrderList", 1);

        JsonField orderId1 = AtlasJsonModelFactory.createJsonField();
        orderId1.setFieldType(FieldType.INTEGER);
        orderId1.setName("orderId");
        orderId1.setValue(1);
        orderId1.setPath("/SourceOrderList/orders[1]/orderId");
        orderId1.setStatus(FieldStatus.SUPPORTED);
        orders.getJsonFields().getJsonField().add(orderId1);

        JsonComplexType address1_2 = new JsonComplexType();
        address1_2.setName("address");
        address1_2.setPath("/SourceOrderList/orders[2]/address");
        address1_2.setFieldType(FieldType.COMPLEX);
        address1_2.setJsonFields(new JsonFields());
        orders.getJsonFields().getJsonField().add(address1_2);

        setAddress(address1_2, "SourceOrderList", 2);

        JsonComplexType contact1_2 = new JsonComplexType();
        contact1_2.setName("contact");
        contact1_2.setPath("/SourceOrderList/orders[2]/contact");
        contact1_2.setFieldType(FieldType.COMPLEX);
        contact1_2.setJsonFields(new JsonFields());
        orders.getJsonFields().getJsonField().add(contact1_2);

        setContact(contact1_2, "SourceOrderList", 2);

        JsonField orderId2 = AtlasJsonModelFactory.createJsonField();
        orderId2.setFieldType(FieldType.INTEGER);
        orderId2.setName("orderId");
        orderId2.setValue(2);
        orderId2.setPath("/SourceOrderList/orders[2]/orderId");
        orderId2.setStatus(FieldStatus.SUPPORTED);
        orders.getJsonFields().getJsonField().add(orderId2);

        JsonComplexType address1_3 = new JsonComplexType();
        address1_3.setName("address");
        address1_3.setPath("/SourceOrderList/orders[3]/address");
        address1_3.setFieldType(FieldType.COMPLEX);
        address1_3.setJsonFields(new JsonFields());
        orders.getJsonFields().getJsonField().add(address1_3);

        setAddress(address1_3, "SourceOrderList", 3);

        JsonComplexType contact1_3 = new JsonComplexType();
        contact1_3.setName("contact");
        contact1_3.setPath("/SourceOrderList/orders[3]/contact");
        contact1_3.setFieldType(FieldType.COMPLEX);
        contact1_3.setJsonFields(new JsonFields());
        orders.getJsonFields().getJsonField().add(contact1_3);

        setContact(contact1_3, "SourceOrderList", 3);

        JsonField orderId3 = AtlasJsonModelFactory.createJsonField();
        orderId3.setFieldType(FieldType.INTEGER);
        orderId3.setName("orderId");
        orderId3.setValue(3);
        orderId3.setPath("/SourceOrderList/orders[3]/orderId");
        orderId3.setStatus(FieldStatus.SUPPORTED);
        orders.getJsonFields().getJsonField().add(orderId3);

        JsonComplexType address1_4 = new JsonComplexType();
        address1_4.setName("address");
        address1_4.setPath("/SourceOrderList/orders[4]/address");
        address1_4.setFieldType(FieldType.COMPLEX);
        address1_4.setJsonFields(new JsonFields());
        orders.getJsonFields().getJsonField().add(address1_4);

        setAddress(address1_4, "SourceOrderList", 4);

        JsonComplexType contact1_4 = new JsonComplexType();
        contact1_4.setName("contact");
        contact1_4.setPath("/SourceOrderList/orders[4]/contact");
        contact1_4.setFieldType(FieldType.COMPLEX);
        contact1_4.setJsonFields(new JsonFields());
        orders.getJsonFields().getJsonField().add(contact1_4);

        setContact(contact1_4, "SourceOrderList", 4);

        JsonField orderId4 = AtlasJsonModelFactory.createJsonField();
        orderId4.setFieldType(FieldType.INTEGER);
        orderId4.setName("orderId");
        orderId4.setValue(4);
        orderId4.setPath("/SourceOrderList/orders[4]/orderId");
        orderId4.setStatus(FieldStatus.SUPPORTED);
        orders.getJsonFields().getJsonField().add(orderId4);

        JsonField orderBatchNumber = AtlasJsonModelFactory.createJsonField();
        orderBatchNumber.setFieldType(FieldType.INTEGER);
        orderBatchNumber.setName("orderBatchNumber");
        orderBatchNumber.setValue(4123562);
        orderBatchNumber.setPath("/SourceOrderList/orderBatchNumber");
        orderBatchNumber.setStatus(FieldStatus.SUPPORTED);
        jsonComplexType.getJsonFields().getJsonField().add(orderBatchNumber);

        JsonField numberOfOrders = AtlasJsonModelFactory.createJsonField();
        numberOfOrders.setFieldType(FieldType.INTEGER);
        numberOfOrders.setName("numberOfOrders");
        numberOfOrders.setValue(5);
        numberOfOrders.setPath("/SourceOrderList/numberOfOrders");
        numberOfOrders.setStatus(FieldStatus.SUPPORTED);
        jsonComplexType.getJsonFields().getJsonField().add(numberOfOrders);

        JsonNode root = writer.write(jsonComplexType);

        final String instance = new String(Files.readAllBytes(Paths.get("src/test/resources/complex-repeated-result.json")));
        Assert.assertNotNull(instance);
        Assert.assertThat(prettyPrintJson(root.toString()), Is.is(prettyPrintJson(instance)));

    }


    @Test
    @Ignore
//    TODO this needs more fleshing out. Currently we cannot handle nested objects with nested arrays.
    public void testWriteHighlyComplexObject() throws Exception {

        JsonComplexType items = new JsonComplexType();
        items.setName("items");
        items.setPath("/items");
        items.setFieldType(FieldType.COMPLEX);
        items.setJsonFields(new JsonFields());
        JsonNode root = writer.write(items);

        JsonComplexType item = new JsonComplexType();
        item.setJsonFields(new JsonFields());
        item.setName("item");
        item.setPath("/items/item");
        item.setFieldType(FieldType.COMPLEX);
        item.setCollectionType(CollectionType.LIST);
        items.getJsonFields().getJsonField().add(item);
        writer.write(item, root);

        JsonField itemid = new JsonField();
        itemid.setName("id");
        itemid.setPath("/items/item/id");
        itemid.setValue("0001");
        itemid.setFieldType(FieldType.STRING);
        itemid.setStatus(FieldStatus.SUPPORTED);
        item.getJsonFields().getJsonField().add(itemid);
        writer.write(itemid, root);

        JsonField type = new JsonField();
        type.setName("type");
        type.setPath("/items/item/type");
        type.setValue("donut");
        type.setFieldType(FieldType.STRING);
        type.setStatus(FieldStatus.SUPPORTED);
        item.getJsonFields().getJsonField().add(type);
        writer.write(type, root);

        JsonField name = new JsonField();
        name.setName("name");
        name.setPath("/items/item/name");
        name.setValue("Cake");
        name.setFieldType(FieldType.STRING);
        name.setStatus(FieldStatus.SUPPORTED);
        item.getJsonFields().getJsonField().add(name);
        writer.write(name, root);

        JsonField ppu = new JsonField();
        ppu.setName("ppu");
        ppu.setPath("/items/item/ppu");
        ppu.setValue(0.55);
        ppu.setFieldType(FieldType.DOUBLE);
        ppu.setStatus(FieldStatus.SUPPORTED);
        item.getJsonFields().getJsonField().add(ppu);
        writer.write(ppu, root);

        JsonComplexType batters = new JsonComplexType();
        batters.setJsonFields(new JsonFields());
        batters.setName("batters");
        batters.setPath("/items/item/batters");
        batters.setFieldType(FieldType.COMPLEX);
        items.getJsonFields().getJsonField().add(batters);
        writer.write(batters, root);

        JsonComplexType batter = new JsonComplexType();
        batter.setName("batter");
        batter.setPath("/items/item/batters/batter");
        batter.setJsonFields(new JsonFields());
        batter.setFieldType(FieldType.COMPLEX);
        batter.setStatus(FieldStatus.SUPPORTED);
        batter.setCollectionType(CollectionType.LIST);
        batters.getJsonFields().getJsonField().add(batter);
        writer.write(batter, root);

        JsonField batter1_id = new JsonField();
        batter1_id.setName("id");
        batter1_id.setPath("/items/item/batters/batter/id");
        batter1_id.setValue("1001");
        batter1_id.setFieldType(FieldType.STRING);
        batter1_id.setStatus(FieldStatus.SUPPORTED);
        batter.getJsonFields().getJsonField().add(batter1_id);
        writer.write(batter1_id, root);

        JsonField batter1_type = new JsonField();
        batter1_type.setName("type");
        batter1_type.setPath("/items/item/batters/batter/type");
        batter1_type.setValue("Regular");
        batter1_type.setFieldType(FieldType.STRING);
        batter1_type.setStatus(FieldStatus.SUPPORTED);
        batter.getJsonFields().getJsonField().add(batter1_type);
        writer.write(batter1_type, root);

        JsonField batter2_id = new JsonField();
        batter2_id.setName("id");
        batter2_id.setPath("/items/item/batters/batter[1]/id");
        batter2_id.setValue("1002");
        batter2_id.setFieldType(FieldType.STRING);
        batter2_id.setStatus(FieldStatus.SUPPORTED);
        batter.getJsonFields().getJsonField().add(batter2_id);
        writer.write(batter2_id, root);

        JsonField batter2_type = new JsonField();
        batter2_type.setName("type");
        batter2_type.setPath("/items/item/batters/batter[1]/type");
        batter2_type.setValue("Chocolate");
        batter2_type.setFieldType(FieldType.STRING);
        batter2_type.setStatus(FieldStatus.SUPPORTED);
        batter.getJsonFields().getJsonField().add(batter2_type);
        writer.write(batter2_type, root);

        JsonField batter3_id = new JsonField();
        batter3_id.setName("id");
        batter3_id.setPath("/items/item/batters/batter[2]/id");
        batter3_id.setValue("1003");
        batter3_id.setFieldType(FieldType.STRING);
        batter3_id.setStatus(FieldStatus.SUPPORTED);
        batter.getJsonFields().getJsonField().add(batter3_id);
        writer.write(batter3_id, root);

        JsonField batter3_type = new JsonField();
        batter3_type.setName("type");
        batter3_type.setPath("/items/item/batters/batter[2]/type");
        batter3_type.setValue("Blueberry");
        batter3_type.setFieldType(FieldType.STRING);
        batter3_type.setStatus(FieldStatus.SUPPORTED);
        batter.getJsonFields().getJsonField().add(batter3_type);
        writer.write(batter3_type, root);

        JsonField batter4_id = new JsonField();
        batter4_id.setName("id");
        batter4_id.setPath("/items/item/batters/batter[3]/id");
        batter4_id.setValue("1004");
        batter4_id.setFieldType(FieldType.STRING);
        batter4_id.setStatus(FieldStatus.SUPPORTED);
        batter.getJsonFields().getJsonField().add(batter4_id);
        writer.write(batter4_id, root);

        JsonField batter4_type = new JsonField();
        batter4_type.setName("type");
        batter4_type.setPath("/items/item/batters/batter[3]/type");
        batter4_type.setValue("Devil's Food");
        batter4_type.setFieldType(FieldType.STRING);
        batter4_type.setStatus(FieldStatus.SUPPORTED);
        batter.getJsonFields().getJsonField().add(batter4_type);
        writer.write(batter4_type, root);

        JsonComplexType topping = new JsonComplexType();
        topping.setJsonFields(new JsonFields());
        topping.setName("topping");
        topping.setPath("/items/item/topping");
        topping.setFieldType(FieldType.COMPLEX);
        topping.setCollectionType(CollectionType.ARRAY);
        items.getJsonFields().getJsonField().add(topping);
        writer.write(topping, root);

        JsonField topping1_id = new JsonField();
        topping1_id.setName("id");
        topping1_id.setPath("/items/item/topping/id");
        topping1_id.setValue("5001");
        topping1_id.setFieldType(FieldType.STRING);
        topping1_id.setStatus(FieldStatus.SUPPORTED);
        topping.getJsonFields().getJsonField().add(topping1_id);
        writer.write(topping1_id, root);

        JsonField topping1_type = new JsonField();
        topping1_type.setName("type");
        topping1_type.setPath("/items/item/topping/type");
        topping1_type.setValue("None");
        topping1_type.setFieldType(FieldType.STRING);
        topping1_type.setStatus(FieldStatus.SUPPORTED);
        topping.getJsonFields().getJsonField().add(topping1_type);
        writer.write(topping1_type, root);

        JsonField topping2_id = new JsonField();
        topping2_id.setName("id");
        topping2_id.setPath("/items/item/topping/id[1]");
        topping2_id.setValue("5002");
        topping2_id.setFieldType(FieldType.STRING);
        topping2_id.setStatus(FieldStatus.SUPPORTED);
        topping.getJsonFields().getJsonField().add(topping2_id);
        writer.write(topping2_id, root);

        JsonField topping2_type = new JsonField();
        topping2_type.setName("type");
        topping2_type.setPath("/items/item/topping/type[1]");
        topping2_type.setValue("Glazed");
        topping2_type.setFieldType(FieldType.STRING);
        topping2_type.setStatus(FieldStatus.SUPPORTED);
        topping.getJsonFields().getJsonField().add(topping2_type);
        writer.write(topping2_type, root);

        JsonField topping3_id = new JsonField();
        topping3_id.setName("id");
        topping3_id.setPath("/items/item/topping/id[2]");
        topping3_id.setValue("5005");
        topping3_id.setFieldType(FieldType.STRING);
        topping3_id.setStatus(FieldStatus.SUPPORTED);
        topping.getJsonFields().getJsonField().add(topping3_id);
        writer.write(topping3_id, root);

        JsonField topping3_type = new JsonField();
        topping3_type.setName("type");
        topping3_type.setPath("/items/item/topping/type[2]");
        topping3_type.setValue("Sugar");
        topping3_type.setFieldType(FieldType.STRING);
        topping3_type.setStatus(FieldStatus.SUPPORTED);
        topping.getJsonFields().getJsonField().add(topping3_type);
        writer.write(topping3_type, root);

        JsonField topping4_id = new JsonField();
        topping4_id.setName("id");
        topping4_id.setPath("/items/item/topping/id[3]");
        topping4_id.setValue("5007");
        topping4_id.setFieldType(FieldType.STRING);
        topping4_id.setStatus(FieldStatus.SUPPORTED);
        topping.getJsonFields().getJsonField().add(topping4_id);
        writer.write(topping4_id, root);

        JsonField topping4_type = new JsonField();
        topping4_type.setName("type");
        topping4_type.setPath("/items/item/topping/type[3]");
        topping4_type.setValue("Powdered Sugar");
        topping4_type.setFieldType(FieldType.STRING);
        topping4_type.setStatus(FieldStatus.SUPPORTED);
        topping.getJsonFields().getJsonField().add(topping4_type);
        writer.write(topping4_type, root);

        JsonField topping5_id = new JsonField();
        topping5_id.setName("id");
        topping5_id.setPath("/items/item/topping/id[4]");
        topping5_id.setValue("5006");
        topping5_id.setFieldType(FieldType.STRING);
        topping5_id.setStatus(FieldStatus.SUPPORTED);
        topping.getJsonFields().getJsonField().add(topping5_id);
        writer.write(topping5_id, root);

        JsonField topping5_type = new JsonField();
        topping5_type.setName("type");
        topping5_type.setPath("/items/item/topping/type[4]");
        topping5_type.setValue("Chocolate with Sprinkles");
        topping5_type.setFieldType(FieldType.STRING);
        topping5_type.setStatus(FieldStatus.SUPPORTED);
        topping.getJsonFields().getJsonField().add(topping5_type);
        writer.write(topping5_type, root);

        JsonField topping6_id = new JsonField();
        topping6_id.setName("id");
        topping6_id.setPath("/items/item/topping/id[5]");
        topping6_id.setValue("5003");
        topping6_id.setFieldType(FieldType.STRING);
        topping6_id.setStatus(FieldStatus.SUPPORTED);
        topping.getJsonFields().getJsonField().add(topping6_id);
        writer.write(topping6_id, root);

        JsonField topping6_type = new JsonField();
        topping6_type.setName("type");
        topping6_type.setPath("/items/item/topping/type[5]");
        topping6_type.setValue("Chocolate");
        topping6_type.setFieldType(FieldType.STRING);
        topping6_type.setStatus(FieldStatus.SUPPORTED);
        topping.getJsonFields().getJsonField().add(topping6_type);
        writer.write(topping6_type, root);

        JsonField topping7_id = new JsonField();
        topping7_id.setName("id");
        topping7_id.setPath("/items/item/topping/id[6]");
        topping7_id.setValue("5004");
        topping7_id.setFieldType(FieldType.STRING);
        topping7_id.setStatus(FieldStatus.SUPPORTED);
        topping.getJsonFields().getJsonField().add(topping7_id);
        writer.write(topping7_id, root);

        JsonField topping7_type = new JsonField();
        topping7_type.setName("type");
        topping7_type.setPath("/items/item/topping/type[6]");
        topping7_type.setValue("Maple");
        topping7_type.setFieldType(FieldType.STRING);
        topping7_type.setStatus(FieldStatus.SUPPORTED);
        topping.getJsonFields().getJsonField().add(topping7_type);
        writer.write(topping7_type, root);

        //repeat complex

        JsonComplexType item1 = new JsonComplexType();
        item1.setJsonFields(new JsonFields());
        item1.setName("item");
        item1.setPath("/items/item[1]");
        item1.setFieldType(FieldType.COMPLEX);
        item1.setCollectionType(CollectionType.LIST);
        items.getJsonFields().getJsonField().add(item1);
        writer.write(item1, root);

        JsonField itemId1 = new JsonField();
        itemId1.setName("id");
        itemId1.setPath("/items/item[1]/id");
        itemId1.setValue("0002");
        itemId1.setFieldType(FieldType.STRING);
        itemId1.setStatus(FieldStatus.SUPPORTED);
        item1.getJsonFields().getJsonField().add(itemId1);
        writer.write(itemId1, root);

        JsonField type1 = new JsonField();
        type1.setName("type");
        type1.setPath("/items/item[1]/type");
        type1.setValue("donut");
        type1.setFieldType(FieldType.STRING);
        type1.setStatus(FieldStatus.SUPPORTED);
        item.getJsonFields().getJsonField().add(type1);
        writer.write(type1, root);

        JsonField name1 = new JsonField();
        name1.setName("name");
        name1.setPath("/items/item[1]/name");
        name1.setValue("Raised");
        name1.setFieldType(FieldType.STRING);
        name1.setStatus(FieldStatus.SUPPORTED);
        item.getJsonFields().getJsonField().add(name1);
        writer.write(name1, root);

        JsonField ppu2 = new JsonField();
        ppu2.setName("ppu");
        ppu2.setPath("/items/item[1]/ppu");
        ppu2.setValue(0.55);
        ppu2.setFieldType(FieldType.DOUBLE);
        ppu2.setStatus(FieldStatus.SUPPORTED);
        item.getJsonFields().getJsonField().add(ppu2);
        writer.write(ppu2, root);

        JsonComplexType batters1 = new JsonComplexType();
        batters1.setJsonFields(new JsonFields());
        batters1.setName("batters");
        batters1.setPath("/items/item[1]/batters");
        batters1.setFieldType(FieldType.COMPLEX);
        items.getJsonFields().getJsonField().add(batters1);
        writer.write(batters1, root);

        JsonComplexType batter1 = new JsonComplexType();
        batter1.setName("batter");
        batter1.setPath("/items/item[1]/batters/batter");
        batter1.setJsonFields(new JsonFields());
        batter1.setFieldType(FieldType.COMPLEX);
        batter1.setStatus(FieldStatus.SUPPORTED);
        batter1.setCollectionType(CollectionType.LIST);
        batters1.getJsonFields().getJsonField().add(batter1);
        writer.write(batter1, root);

//        FIXME this writes to the items/item/batters/batter and not at items/item[1]/batters/batter

//        JsonField batter1_1_id = new JsonField();
//        batter1_1_id.setName("id");
//        batter1_1_id.setPath("/items/item[1]/batters/batter/id");
//        batter1_1_id.setValue("1001");
//        batter1_1_id.setFieldType(FieldType.STRING);
//        batter1_1_id.setStatus(FieldStatus.SUPPORTED);
//        batter1.getJsonFields().getJsonField().add(batter1_1_id);
//        writer.write(batter1_1_id, root);
//
//        JsonField batter1_1_type = new JsonField();
//        batter1_1_type.setName("type");
//        batter1_1_type.setPath("/items/item[1]/batters/batter/type");
//        batter1_1_type.setValue("Regular");
//        batter1_1_type.setFieldType(FieldType.STRING);
//        batter1_1_type.setStatus(FieldStatus.SUPPORTED);
//        batter1.getJsonFields().getJsonField().add(batter1_1_type);
//        writer.write(batter1_1_type, root);

        System.out.println(prettyPrintJson(root.toString()));
    }

    private void setAddress(JsonComplexType address, String parentName, int index) {

        JsonField streetField1 = AtlasJsonModelFactory.createJsonField();
        streetField1.setName("addressLine1");
        if (index > 0) {
            streetField1.setPath("/".concat(parentName).concat("/orders[" + index + "]/address/addressLine1"));
        } else {
            streetField1.setPath("/".concat(parentName).concat("/orders/address/addressLine1"));
        }
        streetField1.setValue("123 Main St");
        streetField1.setStatus(FieldStatus.SUPPORTED);
        streetField1.setFieldType(FieldType.STRING);
        address.getJsonFields().getJsonField().add(streetField1);

        JsonField streetField2 = AtlasJsonModelFactory.createJsonField();
        streetField2.setName("addressLine2");
        if (index > 0) {
            streetField1.setPath("/".concat(parentName).concat("/orders[" + index + "]/address/addressLine2"));
        } else {
            streetField1.setPath("/".concat(parentName).concat("/orders/address/addressLine2"));
        }
        streetField2.setValue("Suite 42b");
        streetField2.setStatus(FieldStatus.SUPPORTED);
        streetField2.setFieldType(FieldType.STRING);
        address.getJsonFields().getJsonField().add(streetField2);

        JsonField cityField = AtlasJsonModelFactory.createJsonField();
        cityField.setName("city");
        if (index > 0) {
            streetField1.setPath("/".concat(parentName).concat("/orders[" + index + "]/address/city"));
        } else {
            streetField1.setPath("/".concat(parentName).concat("/orders/address/city"));
        }
        cityField.setValue("Anytown");
        cityField.setStatus(FieldStatus.SUPPORTED);
        cityField.setFieldType(FieldType.STRING);
        address.getJsonFields().getJsonField().add(cityField);

        JsonField stateField = AtlasJsonModelFactory.createJsonField();
        stateField.setName("state");
        if (index > 0) {
            streetField1.setPath("/".concat(parentName).concat("/orders[" + index + "]/address/state"));
        } else {
            streetField1.setPath("/".concat(parentName).concat("/orders/address/state"));
        }
        stateField.setValue("NY");
        stateField.setStatus(FieldStatus.SUPPORTED);
        stateField.setFieldType(FieldType.STRING);
        address.getJsonFields().getJsonField().add(stateField);

        JsonField zipCodeField = AtlasJsonModelFactory.createJsonField();
        zipCodeField.setName("zipCode");
        if (index > 0) {
            streetField1.setPath("/".concat(parentName).concat("/orders[" + index + "]/address/zipCode"));
        } else {
            streetField1.setPath("/".concat(parentName).concat("/orders/address/zipCode"));
        }
        zipCodeField.setValue("90210");
        zipCodeField.setStatus(FieldStatus.SUPPORTED);
        zipCodeField.setFieldType(FieldType.STRING);
        address.getJsonFields().getJsonField().add(zipCodeField);
    }

    private void setContact(JsonComplexType contact, String parentName, int index) {

        JsonField firstName = AtlasJsonModelFactory.createJsonField();
        firstName.setName("firstName");
        if (index > 0) {
            firstName.setPath("/".concat(parentName).concat("/orders[" + index + "]/contact/firstName"));
        } else {
            firstName.setPath("/".concat(parentName).concat("/orders/contact/firstName"));
        }
        firstName.setValue("Ozzie");
        firstName.setStatus(FieldStatus.SUPPORTED);
        firstName.setFieldType(FieldType.STRING);
        contact.getJsonFields().getJsonField().add(firstName);

        JsonField lastName = AtlasJsonModelFactory.createJsonField();
        lastName.setName("lastName");
        if (index > 0) {
            lastName.setPath("/".concat(parentName).concat("/orders[" + index + "]/contact/lastName"));
        } else {
            lastName.setPath("/".concat(parentName).concat("/orders/contact/lastName"));
        }
        lastName.setValue("Smith");
        lastName.setStatus(FieldStatus.SUPPORTED);
        lastName.setFieldType(FieldType.STRING);
        contact.getJsonFields().getJsonField().add(lastName);

        JsonField phoneNumber = AtlasJsonModelFactory.createJsonField();
        phoneNumber.setName("phoneNumber");
        if (index > 0) {
            phoneNumber.setPath("/".concat(parentName).concat("/orders[" + index + "]/contact/phoneNumber"));
        } else {
            phoneNumber.setPath("/".concat(parentName).concat("/orders/contact/phoneNumber"));
        }
        phoneNumber.setValue("5551212");
        phoneNumber.setStatus(FieldStatus.SUPPORTED);
        phoneNumber.setFieldType(FieldType.STRING);
        contact.getJsonFields().getJsonField().add(phoneNumber);

        JsonField contactZipCode = AtlasJsonModelFactory.createJsonField();
        contactZipCode.setName("zipCode");
        if (index > 0) {
            contactZipCode.setPath("/".concat(parentName).concat("/orders[" + index + "]/contact/zipCode"));
        } else {
            contactZipCode.setPath("/".concat(parentName).concat("/orders/contact/zipCode"));
        }
        contactZipCode.setValue("81111");
        contactZipCode.setStatus(FieldStatus.SUPPORTED);
        contactZipCode.setFieldType(FieldType.STRING);
        contact.getJsonFields().getJsonField().add(contactZipCode);
    }

    private String prettyPrintJson(String json) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Object objJSON = objectMapper.readValue(json, Object.class);
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objJSON).trim();
    }
}
