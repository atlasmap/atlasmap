package io.atlasmap.json.core;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.hamcrest.core.Is;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.atlasmap.api.AtlasException;
import io.atlasmap.json.v2.AtlasJsonModelFactory;
import io.atlasmap.json.v2.JsonComplexType;
import io.atlasmap.json.v2.JsonField;
import io.atlasmap.json.v2.JsonFields;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.FieldStatus;
import io.atlasmap.v2.FieldType;

/**
 */
public class JsonFieldWriterTest {
    private JsonFieldWriter writer = null;

    @Before
    public void setupWriter() {
        this.writer = new JsonFieldWriter();
        Assert.assertNotNull(writer.getRootNode());
    }

    @Test(expected = AtlasException.class)
    public void testWriteNullField() throws Exception {
        writer.write(null);
    }

    @Test
    public void testWriteSimpleObjectNoRoot() throws Exception {
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/brand");
        field.setValue("Mercedes");
        field.setFieldType(FieldType.STRING);

        writer.write(field);
        Assert.assertNotNull(writer.getRootNode());
        Assert.assertThat(writer.getRootNode().toString(), Is.is("{\"brand\":\"Mercedes\"}"));

        JsonField field2 = AtlasJsonModelFactory.createJsonField();
        field2.setPath("/doors");
        field2.setValue(5);
        field2.setFieldType(FieldType.INTEGER);
        writer.write(field2);
        Assert.assertThat(writer.getRootNode().toString(), Is.is("{\"brand\":\"Mercedes\",\"doors\":5}"));
    }

    @Test
    public void testWriteSimpleObjectWithRoot() throws Exception {
        JsonField field1 = AtlasJsonModelFactory.createJsonField();
        field1.setPath("/car/brand");
        field1.setValue("Mercedes");
        field1.setFieldType(FieldType.STRING);
        writer.write(field1);

        JsonField field2 = AtlasJsonModelFactory.createJsonField();
        field2.setPath("/car/doors");
        field2.setValue(5);
        field2.setFieldType(FieldType.INTEGER);
        writer.write(field2);

        Assert.assertThat(writer.getRootNode().toString(), Is.is("{\"car\":{\"brand\":\"Mercedes\",\"doors\":5}}"));
    }

    @Test
    public void testWriteFlatPrimitiveObjectUnrooted() throws Exception {

        JsonField booleanField = AtlasJsonModelFactory.createJsonField();
        booleanField.setFieldType(FieldType.BOOLEAN);
        booleanField.setValue(false);
        booleanField.setPath("/booleanField");
        booleanField.setStatus(FieldStatus.SUPPORTED);

        writer.write(booleanField);
        Assert.assertNotNull(writer.getRootNode());

        JsonField charField = AtlasJsonModelFactory.createJsonField();
        charField.setFieldType(FieldType.CHAR);
        charField.setValue('a');
        charField.setPath("/charField");
        charField.setStatus(FieldStatus.SUPPORTED);
        writer.write(charField);

        JsonField doubleField = AtlasJsonModelFactory.createJsonField();
        doubleField.setFieldType(FieldType.DOUBLE);
        doubleField.setValue(-27152745.3422);
        doubleField.setPath("/doubleField");
        doubleField.setStatus(FieldStatus.SUPPORTED);
        writer.write(doubleField);

        JsonField floatField = AtlasJsonModelFactory.createJsonField();
        floatField.setFieldType(FieldType.FLOAT);
        floatField.setValue(-63988281.00);
        floatField.setPath("/floatField");
        floatField.setStatus(FieldStatus.SUPPORTED);
        writer.write(floatField);

        JsonField intField = AtlasJsonModelFactory.createJsonField();
        intField.setFieldType(FieldType.INTEGER);
        intField.setValue(8281);
        intField.setPath("/intField");
        intField.setStatus(FieldStatus.SUPPORTED);
        writer.write(intField);

        JsonField shortField = AtlasJsonModelFactory.createJsonField();
        shortField.setFieldType(FieldType.SHORT);
        shortField.setValue(81);
        shortField.setPath("/shortField");
        shortField.setStatus(FieldStatus.SUPPORTED);
        writer.write(shortField);

        JsonField longField = AtlasJsonModelFactory.createJsonField();
        longField.setFieldType(FieldType.LONG);
        longField.setValue(3988281);
        longField.setPath("/longField");
        longField.setStatus(FieldStatus.SUPPORTED);
        writer.write(longField);

        Assert.assertThat(writer.getRootNode().toString(), Is.is(
                "{\"booleanField\":false,\"charField\":\"a\",\"doubleField\":-27152745.3422,\"floatField\":-63988281,\"intField\":8281,\"shortField\":81,\"longField\":3988281}"));
    }

    @Test
    public void testWriteFlatPrimitiveObjectRooted() throws Exception {
        JsonField booleanField = AtlasJsonModelFactory.createJsonField();
        booleanField.setFieldType(FieldType.BOOLEAN);
        booleanField.setValue(false);
        booleanField.setPath("/SourceFlatPrimitive/booleanField");
        booleanField.setStatus(FieldStatus.SUPPORTED);
        writer.write(booleanField);

        JsonField charField = AtlasJsonModelFactory.createJsonField();
        charField.setFieldType(FieldType.CHAR);
        charField.setValue('a');
        charField.setPath("/SourceFlatPrimitive/charField");
        charField.setStatus(FieldStatus.SUPPORTED);
        writer.write(charField);

        JsonField doubleField = AtlasJsonModelFactory.createJsonField();
        doubleField.setFieldType(FieldType.DOUBLE);
        doubleField.setValue(-27152745.3422);
        doubleField.setPath("/SourceFlatPrimitive/doubleField");
        doubleField.setStatus(FieldStatus.SUPPORTED);
        writer.write(doubleField);

        JsonField floatField = AtlasJsonModelFactory.createJsonField();
        floatField.setFieldType(FieldType.FLOAT);
        floatField.setValue(-63988281.00);
        floatField.setPath("/SourceFlatPrimitive/floatField");
        floatField.setStatus(FieldStatus.SUPPORTED);
        writer.write(floatField);

        JsonField intField = AtlasJsonModelFactory.createJsonField();
        intField.setFieldType(FieldType.INTEGER);
        intField.setValue(8281);
        intField.setPath("/SourceFlatPrimitive/intField");
        intField.setStatus(FieldStatus.SUPPORTED);
        writer.write(intField);

        JsonField shortField = AtlasJsonModelFactory.createJsonField();
        shortField.setFieldType(FieldType.SHORT);
        shortField.setValue(81);
        shortField.setPath("/SourceFlatPrimitive/shortField");
        shortField.setStatus(FieldStatus.SUPPORTED);
        writer.write(shortField);

        JsonField longField = AtlasJsonModelFactory.createJsonField();
        longField.setFieldType(FieldType.LONG);
        longField.setValue(3988281);
        longField.setPath("/SourceFlatPrimitive/longField");
        longField.setStatus(FieldStatus.SUPPORTED);
        writer.write(longField);

        Assert.assertThat(writer.getRootNode().toString(), Is.is(
                "{\"SourceFlatPrimitive\":{\"booleanField\":false,\"charField\":\"a\",\"doubleField\":-27152745.3422,\"floatField\":-63988281,\"intField\":8281,\"shortField\":81,\"longField\":3988281}}"));
    }

    @Test
    public void testSimpleRepeated() throws Exception {
        writeString("/orders[0]/orderid", "orderid1");
        writeString("/orders[1]/orderid", "orderid2");
        Assert.assertThat(writer.getRootNode().toString(),
                Is.is("{\"orders\":[{\"orderid\":\"orderid1\"},{\"orderid\":\"orderid2\"}]}"));
    }

    public void writeString(String path, String value) throws Exception {
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setValue(value);
        field.setStatus(FieldStatus.SUPPORTED);
        field.setFieldType(FieldType.STRING);
        field.setPath(path);
        writer.write(field);
    }

    public void writeInteger(String path, Integer value) throws Exception {
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setValue(value);
        field.setStatus(FieldStatus.SUPPORTED);
        field.setFieldType(FieldType.INTEGER);
        field.setPath(path);
        writer.write(field);
    }

    @Test
    public void testWriteComplexObjectUnrooted() throws Exception {
        writeComplexTestData("", "");
        Assert.assertThat(writer.getRootNode().toString(), Is.is(
                "{\"address\":{\"addressLine1\":\"123 Main St\",\"addressLine2\":\"Suite 42b\",\"city\":\"Anytown\",\"state\":\"NY\",\"zipCode\":\"90210\"},\"contact\":{\"firstName\":\"Ozzie\",\"lastName\":\"Smith\",\"phoneNumber\":\"5551212\",\"zipCode\":\"81111\"},\"orderId\":9}"));
    }

    @Test
    public void testWriteComplexObjectRooted() throws Exception {
        writeComplexTestData("/order", "");

        final String instance = new String(
                Files.readAllBytes(Paths.get("src/test/resources/complex-rooted-result.json")));
        Assert.assertNotNull(instance);

        Assert.assertThat(prettyPrintJson(writer.getRootNode().toString()), Is.is(prettyPrintJson(instance)));
    }

    public void writeComplexTestData(String prefix, String valueSuffix) throws Exception {
        System.out.println("\nNow writing with prefix: " + prefix + ", suffix: " + valueSuffix);
        writeString(prefix + "/address/addressLine1", "123 Main St" + valueSuffix);
        writeString(prefix + "/address/addressLine2", "Suite 42b" + valueSuffix);
        writeString(prefix + "/address/city", "Anytown" + valueSuffix);
        writeString(prefix + "/address/state", "NY" + valueSuffix);
        writeString(prefix + "/address/zipCode", "90210" + valueSuffix);
        writeString(prefix + "/contact/firstName", "Ozzie" + valueSuffix);
        writeString(prefix + "/contact/lastName", "Smith" + valueSuffix);
        writeString(prefix + "/contact/phoneNumber", "5551212" + valueSuffix);
        writeString(prefix + "/contact/zipCode", "81111" + valueSuffix);
        writeInteger(prefix + "/orderId", 9);
    }

    @Test
    public void testWriteComplexObjectRepeated() throws Exception {

        for (int i = 0; i < 5; i++) {
            String prefix = "/SourceOrderList/orders[" + i + "]";
            String valueSuffix = " (" + (i + 1) + ")";
            writeComplexTestData(prefix, valueSuffix);
        }

        writeInteger("/SourceOrderList/orderBatchNumber", 4123562);
        writeInteger("/SourceOrderList/numberOfOrders", 5);

        final String instance = new String(
                Files.readAllBytes(Paths.get("src/test/resources/complex-repeated-result.json")));
        Assert.assertNotNull(instance);
        Assert.assertThat(prettyPrintJson(writer.getRootNode().toString()), Is.is(prettyPrintJson(instance)));
    }

    @Test
    @Ignore
    // TODO this needs more fleshing out. Currently we cannot handle nested objects
    // with nested arrays.
    public void testWriteHighlyComplexObject() throws Exception {

        JsonComplexType items = new JsonComplexType();
        items.setPath("/items");
        items.setFieldType(FieldType.COMPLEX);
        items.setJsonFields(new JsonFields());
        writer.write(items);

        JsonComplexType item = new JsonComplexType();
        item.setJsonFields(new JsonFields());
        item.setPath("/items/item");
        item.setFieldType(FieldType.COMPLEX);
        item.setCollectionType(CollectionType.LIST);
        items.getJsonFields().getJsonField().add(item);
        writer.write(item);

        JsonField itemid = new JsonField();
        itemid.setPath("/items/item/id");
        itemid.setValue("0001");
        itemid.setFieldType(FieldType.STRING);
        itemid.setStatus(FieldStatus.SUPPORTED);
        item.getJsonFields().getJsonField().add(itemid);
        writer.write(itemid);

        JsonField type = new JsonField();
        type.setPath("/items/item/type");
        type.setValue("donut");
        type.setFieldType(FieldType.STRING);
        type.setStatus(FieldStatus.SUPPORTED);
        item.getJsonFields().getJsonField().add(type);
        writer.write(type);

        JsonField name = new JsonField();
        name.setPath("/items/item/name");
        name.setValue("Cake");
        name.setFieldType(FieldType.STRING);
        name.setStatus(FieldStatus.SUPPORTED);
        item.getJsonFields().getJsonField().add(name);
        writer.write(name);

        JsonField ppu = new JsonField();
        ppu.setPath("/items/item/ppu");
        ppu.setValue(0.55);
        ppu.setFieldType(FieldType.DOUBLE);
        ppu.setStatus(FieldStatus.SUPPORTED);
        item.getJsonFields().getJsonField().add(ppu);
        writer.write(ppu);

        JsonComplexType batters = new JsonComplexType();
        batters.setJsonFields(new JsonFields());
        batters.setPath("/items/item/batters");
        batters.setFieldType(FieldType.COMPLEX);
        items.getJsonFields().getJsonField().add(batters);
        writer.write(batters);

        JsonComplexType batter = new JsonComplexType();
        batter.setPath("/items/item/batters/batter");
        batter.setJsonFields(new JsonFields());
        batter.setFieldType(FieldType.COMPLEX);
        batter.setStatus(FieldStatus.SUPPORTED);
        batter.setCollectionType(CollectionType.LIST);
        batters.getJsonFields().getJsonField().add(batter);
        writer.write(batter);

        JsonField batter1Id = new JsonField();
        batter1Id.setPath("/items/item/batters/batter/id");
        batter1Id.setValue("1001");
        batter1Id.setFieldType(FieldType.STRING);
        batter1Id.setStatus(FieldStatus.SUPPORTED);
        batter.getJsonFields().getJsonField().add(batter1Id);
        writer.write(batter1Id);

        JsonField batter1Type = new JsonField();
        batter1Type.setPath("/items/item/batters/batter/type");
        batter1Type.setValue("Regular");
        batter1Type.setFieldType(FieldType.STRING);
        batter1Type.setStatus(FieldStatus.SUPPORTED);
        batter.getJsonFields().getJsonField().add(batter1Type);
        writer.write(batter1Type);

        JsonField batter2Id = new JsonField();
        batter2Id.setPath("/items/item/batters/batter[1]/id");
        batter2Id.setValue("1002");
        batter2Id.setFieldType(FieldType.STRING);
        batter2Id.setStatus(FieldStatus.SUPPORTED);
        batter.getJsonFields().getJsonField().add(batter2Id);
        writer.write(batter2Id);

        JsonField batter2Type = new JsonField();
        batter2Type.setPath("/items/item/batters/batter[1]/type");
        batter2Type.setValue("Chocolate");
        batter2Type.setFieldType(FieldType.STRING);
        batter2Type.setStatus(FieldStatus.SUPPORTED);
        batter.getJsonFields().getJsonField().add(batter2Type);
        writer.write(batter2Type);

        JsonField batter3Id = new JsonField();
        batter3Id.setPath("/items/item/batters/batter[2]/id");
        batter3Id.setValue("1003");
        batter3Id.setFieldType(FieldType.STRING);
        batter3Id.setStatus(FieldStatus.SUPPORTED);
        batter.getJsonFields().getJsonField().add(batter3Id);
        writer.write(batter3Id);

        JsonField batter3Type = new JsonField();
        batter3Type.setPath("/items/item/batters/batter[2]/type");
        batter3Type.setValue("Blueberry");
        batter3Type.setFieldType(FieldType.STRING);
        batter3Type.setStatus(FieldStatus.SUPPORTED);
        batter.getJsonFields().getJsonField().add(batter3Type);
        writer.write(batter3Type);

        JsonField batter4Id = new JsonField();
        batter4Id.setPath("/items/item/batters/batter[3]/id");
        batter4Id.setValue("1004");
        batter4Id.setFieldType(FieldType.STRING);
        batter4Id.setStatus(FieldStatus.SUPPORTED);
        batter.getJsonFields().getJsonField().add(batter4Id);
        writer.write(batter4Id);

        JsonField batter4Type = new JsonField();
        batter4Type.setPath("/items/item/batters/batter[3]/type");
        batter4Type.setValue("Devil's Food");
        batter4Type.setFieldType(FieldType.STRING);
        batter4Type.setStatus(FieldStatus.SUPPORTED);
        batter.getJsonFields().getJsonField().add(batter4Type);
        writer.write(batter4Type);

        JsonComplexType topping = new JsonComplexType();
        topping.setJsonFields(new JsonFields());
        topping.setPath("/items/item/topping");
        topping.setFieldType(FieldType.COMPLEX);
        topping.setCollectionType(CollectionType.ARRAY);
        items.getJsonFields().getJsonField().add(topping);
        writer.write(topping);

        JsonField topping1Id = new JsonField();
        topping1Id.setPath("/items/item/topping/id");
        topping1Id.setValue("5001");
        topping1Id.setFieldType(FieldType.STRING);
        topping1Id.setStatus(FieldStatus.SUPPORTED);
        topping.getJsonFields().getJsonField().add(topping1Id);
        writer.write(topping1Id);

        JsonField topping1Type = new JsonField();
        topping1Type.setPath("/items/item/topping/type");
        topping1Type.setValue("None");
        topping1Type.setFieldType(FieldType.STRING);
        topping1Type.setStatus(FieldStatus.SUPPORTED);
        topping.getJsonFields().getJsonField().add(topping1Type);
        writer.write(topping1Type);

        JsonField topping2Id = new JsonField();
        topping2Id.setPath("/items/item/topping/id[1]");
        topping2Id.setValue("5002");
        topping2Id.setFieldType(FieldType.STRING);
        topping2Id.setStatus(FieldStatus.SUPPORTED);
        topping.getJsonFields().getJsonField().add(topping2Id);
        writer.write(topping2Id);

        JsonField topping2Type = new JsonField();
        topping2Type.setPath("/items/item/topping/type[1]");
        topping2Type.setValue("Glazed");
        topping2Type.setFieldType(FieldType.STRING);
        topping2Type.setStatus(FieldStatus.SUPPORTED);
        topping.getJsonFields().getJsonField().add(topping2Type);
        writer.write(topping2Type);

        JsonField topping3Id = new JsonField();
        topping3Id.setPath("/items/item/topping/id[2]");
        topping3Id.setValue("5005");
        topping3Id.setFieldType(FieldType.STRING);
        topping3Id.setStatus(FieldStatus.SUPPORTED);
        topping.getJsonFields().getJsonField().add(topping3Id);
        writer.write(topping3Id);

        JsonField topping3Type = new JsonField();
        topping3Type.setPath("/items/item/topping/type[2]");
        topping3Type.setValue("Sugar");
        topping3Type.setFieldType(FieldType.STRING);
        topping3Type.setStatus(FieldStatus.SUPPORTED);
        topping.getJsonFields().getJsonField().add(topping3Type);
        writer.write(topping3Type);

        JsonField topping4Id = new JsonField();
        topping4Id.setPath("/items/item/topping/id[3]");
        topping4Id.setValue("5007");
        topping4Id.setFieldType(FieldType.STRING);
        topping4Id.setStatus(FieldStatus.SUPPORTED);
        topping.getJsonFields().getJsonField().add(topping4Id);
        writer.write(topping4Id);

        JsonField topping4Type = new JsonField();
        topping4Type.setPath("/items/item/topping/type[3]");
        topping4Type.setValue("Powdered Sugar");
        topping4Type.setFieldType(FieldType.STRING);
        topping4Type.setStatus(FieldStatus.SUPPORTED);
        topping.getJsonFields().getJsonField().add(topping4Type);
        writer.write(topping4Type);

        JsonField topping5Id = new JsonField();
        topping5Id.setPath("/items/item/topping/id[4]");
        topping5Id.setValue("5006");
        topping5Id.setFieldType(FieldType.STRING);
        topping5Id.setStatus(FieldStatus.SUPPORTED);
        topping.getJsonFields().getJsonField().add(topping5Id);
        writer.write(topping5Id);

        JsonField topping5Type = new JsonField();
        topping5Type.setPath("/items/item/topping/type[4]");
        topping5Type.setValue("Chocolate with Sprinkles");
        topping5Type.setFieldType(FieldType.STRING);
        topping5Type.setStatus(FieldStatus.SUPPORTED);
        topping.getJsonFields().getJsonField().add(topping5Type);
        writer.write(topping5Type);

        JsonField topping6Id = new JsonField();
        topping6Id.setPath("/items/item/topping/id[5]");
        topping6Id.setValue("5003");
        topping6Id.setFieldType(FieldType.STRING);
        topping6Id.setStatus(FieldStatus.SUPPORTED);
        topping.getJsonFields().getJsonField().add(topping6Id);
        writer.write(topping6Id);

        JsonField topping6Type = new JsonField();
        topping6Type.setPath("/items/item/topping/type[5]");
        topping6Type.setValue("Chocolate");
        topping6Type.setFieldType(FieldType.STRING);
        topping6Type.setStatus(FieldStatus.SUPPORTED);
        topping.getJsonFields().getJsonField().add(topping6Type);
        writer.write(topping6Type);

        JsonField topping7Id = new JsonField();
        topping7Id.setPath("/items/item/topping/id[6]");
        topping7Id.setValue("5004");
        topping7Id.setFieldType(FieldType.STRING);
        topping7Id.setStatus(FieldStatus.SUPPORTED);
        topping.getJsonFields().getJsonField().add(topping7Id);
        writer.write(topping7Id);

        JsonField topping7Type = new JsonField();
        topping7Type.setPath("/items/item/topping/type[6]");
        topping7Type.setValue("Maple");
        topping7Type.setFieldType(FieldType.STRING);
        topping7Type.setStatus(FieldStatus.SUPPORTED);
        topping.getJsonFields().getJsonField().add(topping7Type);
        writer.write(topping7Type);

        // repeat complex

        JsonComplexType item1 = new JsonComplexType();
        item1.setJsonFields(new JsonFields());
        item1.setPath("/items/item[1]");
        item1.setFieldType(FieldType.COMPLEX);
        item1.setCollectionType(CollectionType.LIST);
        items.getJsonFields().getJsonField().add(item1);
        writer.write(item1);

        JsonField itemId1 = new JsonField();
        itemId1.setPath("/items/item[1]/id");
        itemId1.setValue("0002");
        itemId1.setFieldType(FieldType.STRING);
        itemId1.setStatus(FieldStatus.SUPPORTED);
        item1.getJsonFields().getJsonField().add(itemId1);
        writer.write(itemId1);

        JsonField type1 = new JsonField();
        type1.setPath("/items/item[1]/type");
        type1.setValue("donut");
        type1.setFieldType(FieldType.STRING);
        type1.setStatus(FieldStatus.SUPPORTED);
        item.getJsonFields().getJsonField().add(type1);
        writer.write(type1);

        JsonField name1 = new JsonField();
        name1.setPath("/items/item[1]/name");
        name1.setValue("Raised");
        name1.setFieldType(FieldType.STRING);
        name1.setStatus(FieldStatus.SUPPORTED);
        item.getJsonFields().getJsonField().add(name1);
        writer.write(name1);

        JsonField ppu2 = new JsonField();
        ppu2.setPath("/items/item[1]/ppu");
        ppu2.setValue(0.55);
        ppu2.setFieldType(FieldType.DOUBLE);
        ppu2.setStatus(FieldStatus.SUPPORTED);
        item.getJsonFields().getJsonField().add(ppu2);
        writer.write(ppu2);

        JsonComplexType batters1 = new JsonComplexType();
        batters1.setJsonFields(new JsonFields());
        batters1.setPath("/items/item[1]/batters");
        batters1.setFieldType(FieldType.COMPLEX);
        items.getJsonFields().getJsonField().add(batters1);
        writer.write(batters1);

        JsonComplexType batter1 = new JsonComplexType();
        batter1.setPath("/items/item[1]/batters/batter");
        batter1.setJsonFields(new JsonFields());
        batter1.setFieldType(FieldType.COMPLEX);
        batter1.setStatus(FieldStatus.SUPPORTED);
        batter1.setCollectionType(CollectionType.LIST);
        batters1.getJsonFields().getJsonField().add(batter1);
        writer.write(batter1);

        // FIXME this writes to the items/item/batters/batter and not at
        // items/item[1]/batters/batter

        // JsonField batter1_1_id = new JsonField();
        // batter1_1_id.setPath("/items/item[1]/batters/batter/id");
        // batter1_1_id.setValue("1001");
        // batter1_1_id.setFieldType(FieldType.STRING);
        // batter1_1_id.setStatus(FieldStatus.SUPPORTED);
        // batter1.getJsonFields().getJsonField().add(batter1_1_id);
        // writer.write(batter1_1_id, root);
        //
        // JsonField batter1_1_type = new JsonField();
        // batter1_1_type.setPath("/items/item[1]/batters/batter/type");
        // batter1_1_type.setValue("Regular");
        // batter1_1_type.setFieldType(FieldType.STRING);
        // batter1_1_type.setStatus(FieldStatus.SUPPORTED);
        // batter1.getJsonFields().getJsonField().add(batter1_1_type);
        // writer.write(batter1_1_type, root);

        System.out.println(prettyPrintJson(writer.getRootNode().toString()));
    }

    private String prettyPrintJson(String json) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Object objJSON = objectMapper.readValue(json, Object.class);
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objJSON);
    }
}
