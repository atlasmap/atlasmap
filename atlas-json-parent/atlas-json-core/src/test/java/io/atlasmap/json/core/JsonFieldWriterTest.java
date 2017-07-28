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
    public void testWriteFlatPrimitiveObject_Unrooted() throws Exception {

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

        Assert.assertThat(writer.getRootNode().toString(), Is.is("{\"booleanField\":false,\"charField\":\"a\",\"doubleField\":-27152745.3422,\"floatField\":-63988281,\"intField\":8281,\"shortField\":81,\"longField\":3988281}"));
    }

    @Test
    public void testWriteFlatPrimitiveObject_Rooted() throws Exception {
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

        Assert.assertThat(writer.getRootNode().toString(), Is.is("{\"SourceFlatPrimitive\":{\"booleanField\":false,\"charField\":\"a\",\"doubleField\":-27152745.3422,\"floatField\":-63988281,\"intField\":8281,\"shortField\":81,\"longField\":3988281}}"));
    }
    
    @Test 
    public void testSimpleRepeated() throws Exception {
    	writeString("/orders[0]/orderid", "orderid1");
    	writeString("/orders[1]/orderid", "orderid2");
    	Assert.assertThat(writer.getRootNode().toString(), Is.is("{\"orders\":[{\"orderid\":\"orderid1\"},{\"orderid\":\"orderid2\"}]}"));
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
    public void testWriteComplexObject_Unrooted() throws Exception {    	      
    	writeComplexTestData("", "");
        Assert.assertThat(writer.getRootNode().toString(), Is.is("{\"address\":{\"addressLine1\":\"123 Main St\",\"addressLine2\":\"Suite 42b\",\"city\":\"Anytown\",\"state\":\"NY\",\"zipCode\":\"90210\"},\"contact\":{\"firstName\":\"Ozzie\",\"lastName\":\"Smith\",\"phoneNumber\":\"5551212\",\"zipCode\":\"81111\"},\"orderId\":9}"));
    }

    @Test
    public void testWriteComplexObject_Rooted() throws Exception {
    	writeComplexTestData("/order", "");    	      

        final String instance = new String(Files.readAllBytes(Paths.get("src/test/resources/complex-rooted-result.json")));
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
    public void testWriteComplexObject_Repeated() throws Exception {
    	
    	for (int i = 0; i < 5; i++) {
    		String prefix = "/SourceOrderList/orders[" + i + "]";
    		String valueSuffix = " (" + (i + 1) + ")";
    		writeComplexTestData(prefix, valueSuffix);
    	}
    	
    	writeInteger("/SourceOrderList/orderBatchNumber", 4123562);
    	writeInteger("/SourceOrderList/numberOfOrders", 5);       

        final String instance = new String(Files.readAllBytes(Paths.get("src/test/resources/complex-repeated-result.json")));
        Assert.assertNotNull(instance);
        Assert.assertThat(prettyPrintJson(writer.getRootNode().toString()), Is.is(prettyPrintJson(instance)));
    }

    @Test
    @Ignore
//    TODO this needs more fleshing out. Currently we cannot handle nested objects with nested arrays.
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

        JsonField batter1_id = new JsonField();
        batter1_id.setPath("/items/item/batters/batter/id");
        batter1_id.setValue("1001");
        batter1_id.setFieldType(FieldType.STRING);
        batter1_id.setStatus(FieldStatus.SUPPORTED);
        batter.getJsonFields().getJsonField().add(batter1_id);
        writer.write(batter1_id);

        JsonField batter1_type = new JsonField();
        batter1_type.setPath("/items/item/batters/batter/type");
        batter1_type.setValue("Regular");
        batter1_type.setFieldType(FieldType.STRING);
        batter1_type.setStatus(FieldStatus.SUPPORTED);
        batter.getJsonFields().getJsonField().add(batter1_type);
        writer.write(batter1_type);

        JsonField batter2_id = new JsonField();
        batter2_id.setPath("/items/item/batters/batter[1]/id");
        batter2_id.setValue("1002");
        batter2_id.setFieldType(FieldType.STRING);
        batter2_id.setStatus(FieldStatus.SUPPORTED);
        batter.getJsonFields().getJsonField().add(batter2_id);
        writer.write(batter2_id);

        JsonField batter2_type = new JsonField();
        batter2_type.setPath("/items/item/batters/batter[1]/type");
        batter2_type.setValue("Chocolate");
        batter2_type.setFieldType(FieldType.STRING);
        batter2_type.setStatus(FieldStatus.SUPPORTED);
        batter.getJsonFields().getJsonField().add(batter2_type);
        writer.write(batter2_type);

        JsonField batter3_id = new JsonField();
        batter3_id.setPath("/items/item/batters/batter[2]/id");
        batter3_id.setValue("1003");
        batter3_id.setFieldType(FieldType.STRING);
        batter3_id.setStatus(FieldStatus.SUPPORTED);
        batter.getJsonFields().getJsonField().add(batter3_id);
        writer.write(batter3_id);

        JsonField batter3_type = new JsonField();
        batter3_type.setPath("/items/item/batters/batter[2]/type");
        batter3_type.setValue("Blueberry");
        batter3_type.setFieldType(FieldType.STRING);
        batter3_type.setStatus(FieldStatus.SUPPORTED);
        batter.getJsonFields().getJsonField().add(batter3_type);
        writer.write(batter3_type);

        JsonField batter4_id = new JsonField();
        batter4_id.setPath("/items/item/batters/batter[3]/id");
        batter4_id.setValue("1004");
        batter4_id.setFieldType(FieldType.STRING);
        batter4_id.setStatus(FieldStatus.SUPPORTED);
        batter.getJsonFields().getJsonField().add(batter4_id);
        writer.write(batter4_id);

        JsonField batter4_type = new JsonField();
        batter4_type.setPath("/items/item/batters/batter[3]/type");
        batter4_type.setValue("Devil's Food");
        batter4_type.setFieldType(FieldType.STRING);
        batter4_type.setStatus(FieldStatus.SUPPORTED);
        batter.getJsonFields().getJsonField().add(batter4_type);
        writer.write(batter4_type);

        JsonComplexType topping = new JsonComplexType();
        topping.setJsonFields(new JsonFields());
        topping.setPath("/items/item/topping");
        topping.setFieldType(FieldType.COMPLEX);
        topping.setCollectionType(CollectionType.ARRAY);
        items.getJsonFields().getJsonField().add(topping);
        writer.write(topping);

        JsonField topping1_id = new JsonField();
        topping1_id.setPath("/items/item/topping/id");
        topping1_id.setValue("5001");
        topping1_id.setFieldType(FieldType.STRING);
        topping1_id.setStatus(FieldStatus.SUPPORTED);
        topping.getJsonFields().getJsonField().add(topping1_id);
        writer.write(topping1_id);

        JsonField topping1_type = new JsonField();
        topping1_type.setPath("/items/item/topping/type");
        topping1_type.setValue("None");
        topping1_type.setFieldType(FieldType.STRING);
        topping1_type.setStatus(FieldStatus.SUPPORTED);
        topping.getJsonFields().getJsonField().add(topping1_type);
        writer.write(topping1_type);

        JsonField topping2_id = new JsonField();
        topping2_id.setPath("/items/item/topping/id[1]");
        topping2_id.setValue("5002");
        topping2_id.setFieldType(FieldType.STRING);
        topping2_id.setStatus(FieldStatus.SUPPORTED);
        topping.getJsonFields().getJsonField().add(topping2_id);
        writer.write(topping2_id);

        JsonField topping2_type = new JsonField();
        topping2_type.setPath("/items/item/topping/type[1]");
        topping2_type.setValue("Glazed");
        topping2_type.setFieldType(FieldType.STRING);
        topping2_type.setStatus(FieldStatus.SUPPORTED);
        topping.getJsonFields().getJsonField().add(topping2_type);
        writer.write(topping2_type);

        JsonField topping3_id = new JsonField();
        topping3_id.setPath("/items/item/topping/id[2]");
        topping3_id.setValue("5005");
        topping3_id.setFieldType(FieldType.STRING);
        topping3_id.setStatus(FieldStatus.SUPPORTED);
        topping.getJsonFields().getJsonField().add(topping3_id);
        writer.write(topping3_id);

        JsonField topping3_type = new JsonField();
        topping3_type.setPath("/items/item/topping/type[2]");
        topping3_type.setValue("Sugar");
        topping3_type.setFieldType(FieldType.STRING);
        topping3_type.setStatus(FieldStatus.SUPPORTED);
        topping.getJsonFields().getJsonField().add(topping3_type);
        writer.write(topping3_type);

        JsonField topping4_id = new JsonField();
        topping4_id.setPath("/items/item/topping/id[3]");
        topping4_id.setValue("5007");
        topping4_id.setFieldType(FieldType.STRING);
        topping4_id.setStatus(FieldStatus.SUPPORTED);
        topping.getJsonFields().getJsonField().add(topping4_id);
        writer.write(topping4_id);

        JsonField topping4_type = new JsonField();
        topping4_type.setPath("/items/item/topping/type[3]");
        topping4_type.setValue("Powdered Sugar");
        topping4_type.setFieldType(FieldType.STRING);
        topping4_type.setStatus(FieldStatus.SUPPORTED);
        topping.getJsonFields().getJsonField().add(topping4_type);
        writer.write(topping4_type);

        JsonField topping5_id = new JsonField();
        topping5_id.setPath("/items/item/topping/id[4]");
        topping5_id.setValue("5006");
        topping5_id.setFieldType(FieldType.STRING);
        topping5_id.setStatus(FieldStatus.SUPPORTED);
        topping.getJsonFields().getJsonField().add(topping5_id);
        writer.write(topping5_id);

        JsonField topping5_type = new JsonField();
        topping5_type.setPath("/items/item/topping/type[4]");
        topping5_type.setValue("Chocolate with Sprinkles");
        topping5_type.setFieldType(FieldType.STRING);
        topping5_type.setStatus(FieldStatus.SUPPORTED);
        topping.getJsonFields().getJsonField().add(topping5_type);
        writer.write(topping5_type);

        JsonField topping6_id = new JsonField();
        topping6_id.setPath("/items/item/topping/id[5]");
        topping6_id.setValue("5003");
        topping6_id.setFieldType(FieldType.STRING);
        topping6_id.setStatus(FieldStatus.SUPPORTED);
        topping.getJsonFields().getJsonField().add(topping6_id);
        writer.write(topping6_id);

        JsonField topping6_type = new JsonField();
        topping6_type.setPath("/items/item/topping/type[5]");
        topping6_type.setValue("Chocolate");
        topping6_type.setFieldType(FieldType.STRING);
        topping6_type.setStatus(FieldStatus.SUPPORTED);
        topping.getJsonFields().getJsonField().add(topping6_type);
        writer.write(topping6_type);

        JsonField topping7_id = new JsonField();
        topping7_id.setPath("/items/item/topping/id[6]");
        topping7_id.setValue("5004");
        topping7_id.setFieldType(FieldType.STRING);
        topping7_id.setStatus(FieldStatus.SUPPORTED);
        topping.getJsonFields().getJsonField().add(topping7_id);
        writer.write(topping7_id);

        JsonField topping7_type = new JsonField();
        topping7_type.setPath("/items/item/topping/type[6]");
        topping7_type.setValue("Maple");
        topping7_type.setFieldType(FieldType.STRING);
        topping7_type.setStatus(FieldStatus.SUPPORTED);
        topping.getJsonFields().getJsonField().add(topping7_type);
        writer.write(topping7_type);

        //repeat complex

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

//        FIXME this writes to the items/item/batters/batter and not at items/item[1]/batters/batter

//        JsonField batter1_1_id = new JsonField();
//        batter1_1_id.setPath("/items/item[1]/batters/batter/id");
//        batter1_1_id.setValue("1001");
//        batter1_1_id.setFieldType(FieldType.STRING);
//        batter1_1_id.setStatus(FieldStatus.SUPPORTED);
//        batter1.getJsonFields().getJsonField().add(batter1_1_id);
//        writer.write(batter1_1_id, root);
//
//        JsonField batter1_1_type = new JsonField();
//        batter1_1_type.setPath("/items/item[1]/batters/batter/type");
//        batter1_1_type.setValue("Regular");
//        batter1_1_type.setFieldType(FieldType.STRING);
//        batter1_1_type.setStatus(FieldStatus.SUPPORTED);
//        batter1.getJsonFields().getJsonField().add(batter1_1_type);
//        writer.write(batter1_1_type, root);

        System.out.println(prettyPrintJson(writer.getRootNode().toString()));
    }    

    private String prettyPrintJson(String json) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Object objJSON = objectMapper.readValue(json, Object.class);
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objJSON);
    }
}
