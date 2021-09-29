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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.atlasmap.api.AtlasException;
import io.atlasmap.core.DefaultAtlasConversionService;
import io.atlasmap.json.v2.AtlasJsonModelFactory;
import io.atlasmap.json.v2.JsonComplexType;
import io.atlasmap.json.v2.JsonField;
import io.atlasmap.json.v2.JsonFields;
import io.atlasmap.spi.AtlasInternalSession;
import io.atlasmap.spi.AtlasInternalSession.Head;
import io.atlasmap.v2.AuditStatus;
import io.atlasmap.v2.Audits;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.Field;
import io.atlasmap.v2.FieldStatus;
import io.atlasmap.v2.FieldType;

public class JsonFieldWriterTest {
    private static JsonFieldReader reader = new JsonFieldReader(DefaultAtlasConversionService.getInstance());
    private JsonFieldWriter writer = null;

    @BeforeEach
    public void setupWriter() {
        this.writer = new JsonFieldWriter();
    }

    @Test
    public void testWriteNullField() throws Exception {
        assertThrows(AtlasException.class, () -> {
            write(null);
        });
    }

    @Test
    public void testWriteSimpleObjectNoRoot() throws Exception {
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/brand");
        field.setValue("Mercedes");
        field.setFieldType(FieldType.STRING);

        write(field);
        assertNotNull(writer.getRootNode());
        assertEquals("{\"brand\":\"Mercedes\"}", writer.getRootNode().toString());

        JsonField field2 = AtlasJsonModelFactory.createJsonField();
        field2.setPath("/doors");
        field2.setValue(5);
        field2.setFieldType(FieldType.INTEGER);
        write(field2);
        assertEquals("{\"brand\":\"Mercedes\",\"doors\":5}", writer.getRootNode().toString());
    }

    @Test
    public void testWriteSimpleObjectWithRoot() throws Exception {
        JsonField field1 = AtlasJsonModelFactory.createJsonField();
        field1.setPath("/car/brand");
        field1.setValue("Mercedes");
        field1.setFieldType(FieldType.STRING);
        write(field1);

        JsonField field2 = AtlasJsonModelFactory.createJsonField();
        field2.setPath("/car/doors");
        field2.setValue(5);
        field2.setFieldType(FieldType.INTEGER);
        write(field2);

        assertEquals("{\"car\":{\"brand\":\"Mercedes\",\"doors\":5}}", writer.getRootNode().toString());
    }

    @Test
    public void testWriteFlatPrimitiveObjectUnrooted() throws Exception {

        JsonField booleanField = AtlasJsonModelFactory.createJsonField();
        booleanField.setFieldType(FieldType.BOOLEAN);
        booleanField.setValue(false);
        booleanField.setPath("/booleanField");
        booleanField.setStatus(FieldStatus.SUPPORTED);

        write(booleanField);
        assertNotNull(writer.getRootNode());

        JsonField charField = AtlasJsonModelFactory.createJsonField();
        charField.setFieldType(FieldType.CHAR);
        charField.setValue('a');
        charField.setPath("/charField");
        charField.setStatus(FieldStatus.SUPPORTED);
        write(charField);

        JsonField doubleField = AtlasJsonModelFactory.createJsonField();
        doubleField.setFieldType(FieldType.DOUBLE);
        doubleField.setValue(-27152745.3422);
        doubleField.setPath("/doubleField");
        doubleField.setStatus(FieldStatus.SUPPORTED);
        write(doubleField);

        JsonField floatField = AtlasJsonModelFactory.createJsonField();
        floatField.setFieldType(FieldType.FLOAT);
        floatField.setValue(-63988281.00);
        floatField.setPath("/floatField");
        floatField.setStatus(FieldStatus.SUPPORTED);
        write(floatField);

        JsonField numberField = AtlasJsonModelFactory.createJsonField();
        numberField.setFieldType(FieldType.NUMBER);
        numberField.setValue(-63988281.00);
        numberField.setPath("/numberField");
        numberField.setStatus(FieldStatus.SUPPORTED);
        write(numberField);

        JsonField intField = AtlasJsonModelFactory.createJsonField();
        intField.setFieldType(FieldType.INTEGER);
        intField.setValue(8281);
        intField.setPath("/intField");
        intField.setStatus(FieldStatus.SUPPORTED);
        write(intField);

        JsonField shortField = AtlasJsonModelFactory.createJsonField();
        shortField.setFieldType(FieldType.SHORT);
        shortField.setValue(81);
        shortField.setPath("/shortField");
        shortField.setStatus(FieldStatus.SUPPORTED);
        write(shortField);

        JsonField longField = AtlasJsonModelFactory.createJsonField();
        longField.setFieldType(FieldType.LONG);
        longField.setValue(3988281);
        longField.setPath("/longField");
        longField.setStatus(FieldStatus.SUPPORTED);
        write(longField);

        assertEquals(
                "{\"booleanField\":false,\"charField\":\"a\",\"doubleField\":-27152745.3422,\"floatField\":-63988281,\"numberField\":-63988281,\"intField\":8281,\"shortField\":81,\"longField\":3988281}",
                writer.getRootNode().toString());
    }

    @Test
    public void testWriteFlatPrimitiveObjectRooted() throws Exception {
        JsonField booleanField = AtlasJsonModelFactory.createJsonField();
        booleanField.setFieldType(FieldType.BOOLEAN);
        booleanField.setValue(false);
        booleanField.setPath("/SourceFlatPrimitive/booleanField");
        booleanField.setStatus(FieldStatus.SUPPORTED);
        write(booleanField);

        JsonField charField = AtlasJsonModelFactory.createJsonField();
        charField.setFieldType(FieldType.CHAR);
        charField.setValue('a');
        charField.setPath("/SourceFlatPrimitive/charField");
        charField.setStatus(FieldStatus.SUPPORTED);
        write(charField);

        JsonField numberField = AtlasJsonModelFactory.createJsonField();
        numberField.setFieldType(FieldType.DOUBLE);
        numberField.setValue(-27152745.3422);
        numberField.setPath("/SourceFlatPrimitive/doubleField");
        numberField.setStatus(FieldStatus.SUPPORTED);
        write(numberField);

        JsonField doubleField = AtlasJsonModelFactory.createJsonField();
        doubleField.setFieldType(FieldType.DOUBLE);
        doubleField.setValue(-27152745.3422);
        doubleField.setPath("/SourceFlatPrimitive/doubleField");
        doubleField.setStatus(FieldStatus.SUPPORTED);
        write(doubleField);

        JsonField floatField = AtlasJsonModelFactory.createJsonField();
        floatField.setFieldType(FieldType.FLOAT);
        floatField.setValue(-63988281.00);
        floatField.setPath("/SourceFlatPrimitive/floatField");
        floatField.setStatus(FieldStatus.SUPPORTED);
        write(floatField);

        JsonField intField = AtlasJsonModelFactory.createJsonField();
        intField.setFieldType(FieldType.INTEGER);
        intField.setValue(8281);
        intField.setPath("/SourceFlatPrimitive/intField");
        intField.setStatus(FieldStatus.SUPPORTED);
        write(intField);

        JsonField shortField = AtlasJsonModelFactory.createJsonField();
        shortField.setFieldType(FieldType.SHORT);
        shortField.setValue(81);
        shortField.setPath("/SourceFlatPrimitive/shortField");
        shortField.setStatus(FieldStatus.SUPPORTED);
        write(shortField);

        JsonField longField = AtlasJsonModelFactory.createJsonField();
        longField.setFieldType(FieldType.LONG);
        longField.setValue(3988281);
        longField.setPath("/SourceFlatPrimitive/longField");
        longField.setStatus(FieldStatus.SUPPORTED);
        write(longField);

        assertEquals(
                "{\"SourceFlatPrimitive\":{\"booleanField\":false,\"charField\":\"a\",\"doubleField\":-27152745.3422,\"floatField\":-63988281,\"intField\":8281,\"shortField\":81,\"longField\":3988281}}",
                writer.getRootNode().toString());
    }

    @Test
    public void testSimpleRepeated() throws Exception {
        writeString("/orders[0]/orderid", "orderid1");
        writeString("/orders[1]/orderid", "orderid2");
        writeString("/orders[3]/orderid", "orderid4");
        assertEquals("{\"orders\":[{\"orderid\":\"orderid1\"},{\"orderid\":\"orderid2\"},{},{\"orderid\":\"orderid4\"}]}",
                writer.getRootNode().toString());
    }

    protected void writeString(String path, String value) throws Exception {
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setValue(value);
        field.setStatus(FieldStatus.SUPPORTED);
        field.setFieldType(FieldType.STRING);
        field.setPath(path);
        write(field);
    }

    protected void writeInteger(String path, Integer value) throws Exception {
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setValue(value);
        field.setStatus(FieldStatus.SUPPORTED);
        field.setFieldType(FieldType.INTEGER);
        field.setPath(path);
        write(field);
    }

    @Test
    public void testWriteComplexObjectUnrooted() throws Exception {
        writeComplexTestData("", "");
        assertEquals(
                "{\"address\":{\"addressLine1\":\"123 Main St\",\"addressLine2\":\"Suite 42b\",\"city\":\"Anytown\",\"state\":\"NY\",\"zipCode\":\"90210\"},\"contact\":{\"firstName\":\"Ozzie\",\"lastName\":\"Smith\",\"phoneNumber\":\"5551212\",\"zipCode\":\"81111\"},\"orderId\":9}",
                writer.getRootNode().toString());
    }

    @Test
    public void testWriteComplexObjectRooted() throws Exception {
        writeComplexTestData("/order", "");

        final String instance = new String(
                Files.readAllBytes(Paths.get("src/test/resources/complex-rooted-result.json")));
        assertNotNull(instance);

        assertEquals(prettyPrintJson(instance), prettyPrintJson(writer.getRootNode().toString()));
    }

    protected void writeComplexTestData(String prefix, String valueSuffix) throws Exception {
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
        assertNotNull(instance);
        assertEquals(prettyPrintJson(instance), prettyPrintJson(writer.getRootNode().toString()));
    }

    @Test
    public void testWriteHighlyComplexObject() throws Exception {

        JsonComplexType items = JsonComplexTypeFactory.createJsonComlexField();
        items.setPath("/items");
        items.setJsonFields(new JsonFields());
        write(items);

        JsonComplexType item = JsonComplexTypeFactory.createJsonComlexField();
        item.setJsonFields(new JsonFields());
        item.setPath("/items/item");
        item.setCollectionType(CollectionType.LIST);
        items.getJsonFields().getJsonField().add(item);
        write(item);

        JsonField itemid = new JsonField();
        itemid.setPath("/items/item/id");
        itemid.setValue("0001");
        itemid.setFieldType(FieldType.STRING);
        itemid.setStatus(FieldStatus.SUPPORTED);
        item.getJsonFields().getJsonField().add(itemid);
        write(itemid);

        JsonField type = new JsonField();
        type.setPath("/items/item/type");
        type.setValue("donut");
        type.setFieldType(FieldType.STRING);
        type.setStatus(FieldStatus.SUPPORTED);
        item.getJsonFields().getJsonField().add(type);
        write(type);

        JsonField name = new JsonField();
        name.setPath("/items/item/name");
        name.setValue("Cake");
        name.setFieldType(FieldType.STRING);
        name.setStatus(FieldStatus.SUPPORTED);
        item.getJsonFields().getJsonField().add(name);
        write(name);

        JsonField ppu = new JsonField();
        ppu.setPath("/items/item/ppu");
        ppu.setValue(0.55);
        ppu.setFieldType(FieldType.DOUBLE);
        ppu.setStatus(FieldStatus.SUPPORTED);
        item.getJsonFields().getJsonField().add(ppu);
        write(ppu);

        JsonComplexType batters = JsonComplexTypeFactory.createJsonComlexField();
        batters.setJsonFields(new JsonFields());
        batters.setPath("/items/item/batters");
        items.getJsonFields().getJsonField().add(batters);
        write(batters);

        JsonComplexType batter = JsonComplexTypeFactory.createJsonComlexField();
        batter.setPath("/items/item/batters/batter");
        batter.setJsonFields(new JsonFields());
        batter.setStatus(FieldStatus.SUPPORTED);
        batter.setCollectionType(CollectionType.LIST);
        batters.getJsonFields().getJsonField().add(batter);
        write(batter);

        JsonField batter1Id = new JsonField();
        batter1Id.setPath("/items/item/batters/batter/id");
        batter1Id.setValue("1001");
        batter1Id.setFieldType(FieldType.STRING);
        batter1Id.setStatus(FieldStatus.SUPPORTED);
        batter.getJsonFields().getJsonField().add(batter1Id);
        write(batter1Id);

        JsonField batter1Type = new JsonField();
        batter1Type.setPath("/items/item/batters/batter/type");
        batter1Type.setValue("Regular");
        batter1Type.setFieldType(FieldType.STRING);
        batter1Type.setStatus(FieldStatus.SUPPORTED);
        batter.getJsonFields().getJsonField().add(batter1Type);
        write(batter1Type);

        JsonField batter2Id = new JsonField();
        batter2Id.setPath("/items/item/batters/batter[1]/id");
        batter2Id.setValue("1002");
        batter2Id.setFieldType(FieldType.STRING);
        batter2Id.setStatus(FieldStatus.SUPPORTED);
        batter.getJsonFields().getJsonField().add(batter2Id);
        write(batter2Id);

        JsonField batter2Type = new JsonField();
        batter2Type.setPath("/items/item/batters/batter[1]/type");
        batter2Type.setValue("Chocolate");
        batter2Type.setFieldType(FieldType.STRING);
        batter2Type.setStatus(FieldStatus.SUPPORTED);
        batter.getJsonFields().getJsonField().add(batter2Type);
        write(batter2Type);

        JsonField batter3Id = new JsonField();
        batter3Id.setPath("/items/item/batters/batter[2]/id");
        batter3Id.setValue("1003");
        batter3Id.setFieldType(FieldType.STRING);
        batter3Id.setStatus(FieldStatus.SUPPORTED);
        batter.getJsonFields().getJsonField().add(batter3Id);
        write(batter3Id);

        JsonField batter3Type = new JsonField();
        batter3Type.setPath("/items/item/batters/batter[2]/type");
        batter3Type.setValue("Blueberry");
        batter3Type.setFieldType(FieldType.STRING);
        batter3Type.setStatus(FieldStatus.SUPPORTED);
        batter.getJsonFields().getJsonField().add(batter3Type);
        write(batter3Type);

        JsonField batter4Id = new JsonField();
        batter4Id.setPath("/items/item/batters/batter[3]/id");
        batter4Id.setValue("1004");
        batter4Id.setFieldType(FieldType.STRING);
        batter4Id.setStatus(FieldStatus.SUPPORTED);
        batter.getJsonFields().getJsonField().add(batter4Id);
        write(batter4Id);

        JsonField batter4Type = new JsonField();
        batter4Type.setPath("/items/item/batters/batter[3]/type");
        batter4Type.setValue("Devil's Food");
        batter4Type.setFieldType(FieldType.STRING);
        batter4Type.setStatus(FieldStatus.SUPPORTED);
        batter.getJsonFields().getJsonField().add(batter4Type);
        write(batter4Type);

        JsonComplexType topping = JsonComplexTypeFactory.createJsonComlexField();
        topping.setJsonFields(new JsonFields());
        topping.setPath("/items/item/topping");
        topping.setCollectionType(CollectionType.ARRAY);
        items.getJsonFields().getJsonField().add(topping);
        write(topping);

        JsonField topping1Id = new JsonField();
        topping1Id.setPath("/items/item/topping/id[0]");
        topping1Id.setValue("5001");
        topping1Id.setFieldType(FieldType.STRING);
        topping1Id.setStatus(FieldStatus.SUPPORTED);
        topping.getJsonFields().getJsonField().add(topping1Id);
        write(topping1Id);

        JsonField topping1Type = new JsonField();
        topping1Type.setPath("/items/item/topping/type[0]");
        topping1Type.setValue("None");
        topping1Type.setFieldType(FieldType.STRING);
        topping1Type.setStatus(FieldStatus.SUPPORTED);
        topping.getJsonFields().getJsonField().add(topping1Type);
        write(topping1Type);

        JsonField topping2Id = new JsonField();
        topping2Id.setPath("/items/item/topping/id[1]");
        topping2Id.setValue("5002");
        topping2Id.setFieldType(FieldType.STRING);
        topping2Id.setStatus(FieldStatus.SUPPORTED);
        topping.getJsonFields().getJsonField().add(topping2Id);
        write(topping2Id);

        JsonField topping2Type = new JsonField();
        topping2Type.setPath("/items/item/topping/type[1]");
        topping2Type.setValue("Glazed");
        topping2Type.setFieldType(FieldType.STRING);
        topping2Type.setStatus(FieldStatus.SUPPORTED);
        topping.getJsonFields().getJsonField().add(topping2Type);
        write(topping2Type);

        JsonField topping3Id = new JsonField();
        topping3Id.setPath("/items/item/topping/id[2]");
        topping3Id.setValue("5005");
        topping3Id.setFieldType(FieldType.STRING);
        topping3Id.setStatus(FieldStatus.SUPPORTED);
        topping.getJsonFields().getJsonField().add(topping3Id);
        write(topping3Id);

        JsonField topping3Type = new JsonField();
        topping3Type.setPath("/items/item/topping/type[2]");
        topping3Type.setValue("Sugar");
        topping3Type.setFieldType(FieldType.STRING);
        topping3Type.setStatus(FieldStatus.SUPPORTED);
        topping.getJsonFields().getJsonField().add(topping3Type);
        write(topping3Type);

        JsonField topping4Id = new JsonField();
        topping4Id.setPath("/items/item/topping/id[3]");
        topping4Id.setValue("5007");
        topping4Id.setFieldType(FieldType.STRING);
        topping4Id.setStatus(FieldStatus.SUPPORTED);
        topping.getJsonFields().getJsonField().add(topping4Id);
        write(topping4Id);

        JsonField topping4Type = new JsonField();
        topping4Type.setPath("/items/item/topping/type[3]");
        topping4Type.setValue("Powdered Sugar");
        topping4Type.setFieldType(FieldType.STRING);
        topping4Type.setStatus(FieldStatus.SUPPORTED);
        topping.getJsonFields().getJsonField().add(topping4Type);
        write(topping4Type);

        JsonField topping5Id = new JsonField();
        topping5Id.setPath("/items/item/topping/id[4]");
        topping5Id.setValue("5006");
        topping5Id.setFieldType(FieldType.STRING);
        topping5Id.setStatus(FieldStatus.SUPPORTED);
        topping.getJsonFields().getJsonField().add(topping5Id);
        write(topping5Id);

        JsonField topping5Type = new JsonField();
        topping5Type.setPath("/items/item/topping/type[4]");
        topping5Type.setValue("Chocolate with Sprinkles");
        topping5Type.setFieldType(FieldType.STRING);
        topping5Type.setStatus(FieldStatus.SUPPORTED);
        topping.getJsonFields().getJsonField().add(topping5Type);
        write(topping5Type);

        JsonField topping6Id = new JsonField();
        topping6Id.setPath("/items/item/topping/id[5]");
        topping6Id.setValue("5003");
        topping6Id.setFieldType(FieldType.STRING);
        topping6Id.setStatus(FieldStatus.SUPPORTED);
        topping.getJsonFields().getJsonField().add(topping6Id);
        write(topping6Id);

        JsonField topping6Type = new JsonField();
        topping6Type.setPath("/items/item/topping/type[5]");
        topping6Type.setValue("Chocolate");
        topping6Type.setFieldType(FieldType.STRING);
        topping6Type.setStatus(FieldStatus.SUPPORTED);
        topping.getJsonFields().getJsonField().add(topping6Type);
        write(topping6Type);

        JsonField topping7Id = new JsonField();
        topping7Id.setPath("/items/item/topping/id[6]");
        topping7Id.setValue("5004");
        topping7Id.setFieldType(FieldType.STRING);
        topping7Id.setStatus(FieldStatus.SUPPORTED);
        topping.getJsonFields().getJsonField().add(topping7Id);
        write(topping7Id);

        JsonField topping7Type = new JsonField();
        topping7Type.setPath("/items/item/topping/type[6]");
        topping7Type.setValue("Maple");
        topping7Type.setFieldType(FieldType.STRING);
        topping7Type.setStatus(FieldStatus.SUPPORTED);
        topping.getJsonFields().getJsonField().add(topping7Type);
        write(topping7Type);

        // repeat complex

        JsonComplexType item1 = JsonComplexTypeFactory.createJsonComlexField();
        item1.setJsonFields(new JsonFields());
        item1.setPath("/items/item[1]");
        item1.setCollectionType(CollectionType.LIST);
        items.getJsonFields().getJsonField().add(item1);
        write(item1);

        JsonField itemId1 = new JsonField();
        itemId1.setPath("/items/item[1]/id");
        itemId1.setValue("0002");
        itemId1.setFieldType(FieldType.STRING);
        itemId1.setStatus(FieldStatus.SUPPORTED);
        item1.getJsonFields().getJsonField().add(itemId1);
        write(itemId1);

        JsonField type1 = new JsonField();
        type1.setPath("/items/item[1]/type");
        type1.setValue("donut");
        type1.setFieldType(FieldType.STRING);
        type1.setStatus(FieldStatus.SUPPORTED);
        item.getJsonFields().getJsonField().add(type1);
        write(type1);

        JsonField name1 = new JsonField();
        name1.setPath("/items/item[1]/name");
        name1.setValue("Raised");
        name1.setFieldType(FieldType.STRING);
        name1.setStatus(FieldStatus.SUPPORTED);
        item.getJsonFields().getJsonField().add(name1);
        write(name1);

        JsonField ppu2 = new JsonField();
        ppu2.setPath("/items/item[1]/ppu");
        ppu2.setValue(0.55);
        ppu2.setFieldType(FieldType.DOUBLE);
        ppu2.setStatus(FieldStatus.SUPPORTED);
        item.getJsonFields().getJsonField().add(ppu2);
        write(ppu2);

        JsonComplexType batters1 = JsonComplexTypeFactory.createJsonComlexField();
        batters1.setJsonFields(new JsonFields());
        batters1.setPath("/items/item[1]/batters");
        items.getJsonFields().getJsonField().add(batters1);
        write(batters1);

        JsonComplexType batter1 = JsonComplexTypeFactory.createJsonComlexField();
        batter1.setPath("/items/item[1]/batters/batter");
        batter1.setJsonFields(new JsonFields());
        batter1.setStatus(FieldStatus.SUPPORTED);
        batter1.setCollectionType(CollectionType.LIST);
        batters1.getJsonFields().getJsonField().add(batter1);
        write(batter1);

        JsonField batter1_1_id = new JsonField();
        batter1_1_id.setPath("/items/item[1]/batters/batter/id");
        batter1_1_id.setValue("1001");
        batter1_1_id.setFieldType(FieldType.STRING);
        batter1_1_id.setStatus(FieldStatus.SUPPORTED);
        batter1.getJsonFields().getJsonField().add(batter1_1_id);
        write(batter1_1_id);

        JsonField batter1_1_type = new JsonField();
        batter1_1_type.setPath("/items/item[1]/batters/batter/type");
        batter1_1_type.setValue("Regular");
        batter1_1_type.setFieldType(FieldType.STRING);
        batter1_1_type.setStatus(FieldStatus.SUPPORTED);
        batter1.getJsonFields().getJsonField().add(batter1_1_type);
        write(batter1_1_type);

        System.out.println(prettyPrintJson(writer.getRootNode().toString()));
    }

    @Test
    public void testJsonFieldDoubleMax() throws Exception {
        testValue("test-write-field-double-max.json", "/primitiveValue", Double.MAX_VALUE, FieldType.DOUBLE);
    }

    @Test
    public void testJsonFieldDoubleMin() throws Exception {
        testValue("test-write-field-double-min.json", "/primitiveValue", Double.MIN_VALUE, FieldType.DOUBLE);
    }

    @Test
    public void testJsonFieldFloatMax() throws Exception {
        testValue("test-write-field-float-max.json", "/primitiveValue", Float.MAX_VALUE, FieldType.FLOAT);
    }

    @Test
    public void testJsonFieldFloatMin() throws Exception {
        testValue("test-write-field-float-min.json", "/primitiveValue", Float.MIN_VALUE, FieldType.FLOAT);
    }

    @Test
    public void testJsonFieldLongMax() throws Exception {
        testValue("test-write-field-long-max.json", "/primitiveValue", Long.MAX_VALUE, FieldType.LONG);
    }

    @Test
    public void testJsonFieldLongMin() throws Exception {
        testValue("test-write-field-long-min.json", "/primitiveValue", Long.MIN_VALUE, FieldType.LONG);
    }

    @Test
    public void testJsonFieldIntegerMax() throws Exception {
        testValue("test-write-field-integer-max.json", "/primitiveValue", Integer.MAX_VALUE, FieldType.INTEGER);
    }

    @Test
    public void testJsonFieldBigInteger() throws Exception {
        testValue("test-write-field-biginteger.json", "/primitiveValue", new BigInteger("1234567890123456789012345678901234567890"), FieldType.BIG_INTEGER);
    }

    @Test
    public void testJsonFieldIntegerMin() throws Exception {
        testValue("test-write-field-integer-min.json", "/primitiveValue", Integer.MIN_VALUE, FieldType.INTEGER);
    }

    @Test
    public void testJsonFieldShortMax() throws Exception {
        testValue("test-write-field-short-max.json", "/primitiveValue", Short.MAX_VALUE, FieldType.SHORT);
    }

    @Test
    public void testJsonFieldShortMin() throws Exception {
        testValue("test-write-field-short-min.json", "/primitiveValue", Short.MIN_VALUE, FieldType.SHORT);
    }

    @Test
    public void testJsonFieldChar() throws Exception {
        testValue("test-write-field-char.json", "/primitiveValue", Character.valueOf((char) 127), FieldType.CHAR);
    }

    @Test
    public void testJsonFieldCharMin() throws Exception {
        testValue("test-write-field-char-min.json", "/primitiveValue", Character.MIN_VALUE, FieldType.CHAR);
    }

    @Test
    public void testJsonFieldByteMax() throws Exception {
        testValue("test-write-field-byte-max.json", "/primitiveValue", Byte.MAX_VALUE, FieldType.BYTE);
    }

    @Test
    public void testJsonFieldByteMin() throws Exception {
        testValue("test-write-field-byte-min.json", "/primitiveValue", Byte.MIN_VALUE, FieldType.BYTE);
    }

    @Test
    public void testJsonFieldBooleanTrue() throws Exception {
        testValue("test-write-field-boolean-true.json", "/primitiveValue", Boolean.TRUE, FieldType.BOOLEAN);
    }

    @Test
    public void testJsonFieldBooleanFalse() throws Exception {
        testValue("test-write-field-boolean-false.json", "/primitiveValue", Boolean.FALSE, FieldType.BOOLEAN);
    }

    @Test
    public void testJsonFieldStringEmpty() throws Exception {
        testValue("test-write-field-string-empty.json", "/stringValue", "", FieldType.STRING);
    }

    @Test
    public void testJsonFieldStringNonEmpty() throws Exception {
        testValue("test-write-field-string-nonempty.json", "/stringValue", "testString", FieldType.STRING);
    }

    @Test
    public void testJsonFieldStringNull() throws Exception {
        testValue("test-write-field-string-null.json", "/stringValue", null, FieldType.STRING);
    }

    @Test
    public void testJsonFieldDoubleMaxRangeOut() throws Exception {
        testRangeOutValue("test-write-field-double-max-range-out.json", "/primitiveValue", "1.7976931348623157E309",
                FieldType.STRING, FieldType.DOUBLE);
    }

    @Test
    public void testJsonFieldDoubleMinRangeOut() throws Exception {
        testValue("test-write-field-double-min-range-out.json", "/primitiveValue", "4.9E-325",
                FieldType.STRING, FieldType.DOUBLE, 0.0);
    }

    @Test
    public void testJsonFieldFloatMaxRangeOut() throws Exception {
        testRangeOutValue("test-write-field-float-max-range-out.json", "/primitiveValue", "3.4028235E39",
                FieldType.STRING, FieldType.FLOAT);
    }

    @Test
    public void testJsonFieldFloatMinRangeOut() throws Exception {
        testValue("test-write-field-float-min-range-out.json", "/primitiveValue", "1.4E-46",
                FieldType.STRING, FieldType.FLOAT, 0.0f);
    }

    @Test
    public void testJsonFieldLongMaxRangeOut() throws Exception {
        testRangeOutValue("test-write-field-long-max-range-out.json", "/primitiveValue", "9223372036854775808",
                FieldType.STRING, FieldType.LONG);
    }

    @Test
    public void testJsonFieldLongMinRangeOut() throws Exception {
        testRangeOutValue("test-write-field-long-min-range-out.json", "/primitiveValue", "-9223372036854775809",
                FieldType.STRING, FieldType.LONG);
    }

    @Test
    public void testJsonFieldIntegerMaxRangeOut() throws Exception {
        testRangeOutValue("test-write-field-integer-max-range-out.json", "/primitiveValue", Long.MAX_VALUE,
                FieldType.LONG, FieldType.INTEGER);
    }

    @Test
    public void testJsonFieldIntegerMinRangeOut() throws Exception {
        testRangeOutValue("test-write-field-integer-min-range-out.json", "/primitiveValue", Long.MIN_VALUE,
                FieldType.LONG, FieldType.INTEGER);
    }

    @Test
    public void testJsonFieldShortMaxRangeOut() throws Exception {
        testRangeOutValue("test-write-field-short-max-range-out.json", "/primitiveValue", Long.MAX_VALUE,
                FieldType.LONG, FieldType.SHORT);
    }

    @Test
    public void testJsonFieldShortMinRangeOut() throws Exception {
        testRangeOutValue("test-write-field-short-min-range-out.json", "/primitiveValue", Long.MIN_VALUE,
                FieldType.LONG, FieldType.SHORT);
    }

    @Test
    public void testJsonFieldCharMaxRangeOut() throws Exception {
        testRangeOutValue("test-write-field-char-max-range-out.json", "/primitiveValue", Long.MAX_VALUE, FieldType.LONG,
                FieldType.CHAR);
    }

    @Test
    public void testJsonFieldCharMinRangeOut() throws Exception {
        testRangeOutValue("test-write-field-char-min-range-out.json", "/primitiveValue", Long.MIN_VALUE, FieldType.LONG,
                FieldType.CHAR);
    }

    @Test
    public void testJsonFieldByteMaxRangeOut() throws Exception {
        testRangeOutValue("test-write-field-byte-max-range-out.json", "/primitiveValue", Long.MAX_VALUE, FieldType.LONG,
                FieldType.BYTE);
    }

    @Test
    public void testJsonFieldByteMinRangeOut() throws Exception {
        testRangeOutValue("test-write-field-byte-min-range-out.json", "/primitiveValue", Long.MIN_VALUE, FieldType.LONG,
                FieldType.BYTE);
    }

    @Test
    public void testJsonFieldBooleanRangeOut() throws Exception {
        testValue("test-write-field-boolean-range-out.json", "/primitiveValue", null, FieldType.NONE,
                FieldType.BOOLEAN, null);
    }

    @Test
    public void testJsonFieldLongDecimal() throws Exception {
        testValue("test-write-field-long-decimal.json", "/primitiveValue", Double.valueOf("126.1234"),
                FieldType.DOUBLE, FieldType.LONG, 126L);
    }

    @Test
    public void testJsonFieldIntegerDecimal() throws Exception {
        testValue("test-write-field-integer-decimal.json", "/primitiveValue", Double.valueOf("126.1234"),
                FieldType.DOUBLE, FieldType.INTEGER, 126);
    }

    @Test
    public void testJsonFieldShortDecimal() throws Exception {
        testValue("test-write-field-short-decimal.json", "/primitiveValue", Double.valueOf("126.1234"),
                FieldType.DOUBLE, FieldType.SHORT, (short)126);
    }

    @Test
    public void testJsonFieldCharDecimal() throws Exception {
        testRangeOutValue("test-write-field-char-decimal.json", "/primitiveValue", Double.valueOf("126.1234"),
                FieldType.DOUBLE, FieldType.CHAR);
    }

    @Test
    public void testJsonFieldByteDecimal() throws Exception {
        testValue("test-write-field-byte-decimal.json", "/primitiveValue", Double.valueOf("126.1234"),
                FieldType.DOUBLE, FieldType.BYTE, (byte)126);
    }

    @Test
    public void testJsonFieldDoubleString() throws Exception {
        testRangeOutValue("test-write-field-double-string.json", "/primitiveValue", "abcd", FieldType.STRING,
                FieldType.DOUBLE);
    }

    @Test
    public void testJsonFieldFloatString() throws Exception {
        testRangeOutValue("test-write-field-float-string.json", "/primitiveValue", "abcd", FieldType.STRING,
                FieldType.FLOAT);
    }

    @Test
    public void testJsonFieldLongString() throws Exception {
        testRangeOutValue("test-write-field-long-string.json", "/primitiveValue", "abcd", FieldType.STRING,
                FieldType.LONG);
    }

    @Test
    public void testJsonFieldIntegerString() throws Exception {
        testRangeOutValue("test-write-field-integer-string.json", "/primitiveValue", "abcd", FieldType.STRING,
                FieldType.INTEGER);
    }

    @Test
    public void testJsonFieldShortString() throws Exception {
        testRangeOutValue("test-write-field-short-string.json", "/primitiveValue", "abcd", FieldType.STRING,
                FieldType.SHORT);
    }

    @Test
    public void testJsonFieldCharString() throws Exception {
        testRangeOutValue("test-write-field-char-string.json", "/primitiveValue", "abcd", FieldType.STRING,
                FieldType.CHAR);
    }

    @Test
    public void testJsonFieldByteString() throws Exception {
        testRangeOutValue("test-write-field-byte-string.json", "/primitiveValue", "abcd", FieldType.STRING,
                FieldType.BYTE);
    }

    @Test
    public void testJsonFieldBooleanString() throws Exception {
        Path path = Paths.get("target" + File.separator + "test-write-field-byte-string.json");
        String fieldPath = "/primitiveValue";
        Object testObject = "abcd";
        FieldType inputFieldType = FieldType.STRING;
        FieldType outputFieldType = FieldType.BOOLEAN;

        write(path, fieldPath, testObject, inputFieldType);

        AtlasInternalSession session = read(path, outputFieldType, fieldPath);

        assertEquals(false, session.head().getSourceField().getValue());
    }

    @Test
    public void testJsonFieldTopmostArraySimple() throws Exception {
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/<1>");
        field.setValue(300);
        field.setFieldType(FieldType.INTEGER);
        write(field);
        field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/<3>");
        field.setValue(500);
        field.setFieldType(FieldType.INTEGER);
        write(field);

        assertNotNull(writer.getRootNode());
        assertEquals("[null,300,null,500]", writer.getRootNode().toString());
    }

    @Test
    public void testJsonFieldTopmostArrayObject() throws Exception {
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/<1>/color");
        field.setValue("red");
        field.setFieldType(FieldType.STRING);
        write(field);
        field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/<2>/value");
        field.setValue("foobar");
        field.setFieldType(FieldType.STRING);
        write(field);
        field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/<5>/color");
        field.setValue("black");
        field.setFieldType(FieldType.STRING);
        write(field);
        field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/<5>/value");
        field.setValue("123");
        field.setFieldType(FieldType.STRING);
        write(field);

        assertNotNull(writer.getRootNode());
        assertEquals(
                "[{},{\"color\":\"red\"},{\"value\":\"foobar\"},{},{},{\"color\":\"black\",\"value\":\"123\"}]",
                writer.getRootNode().toString());
        
    }

    private void write(Path path, String fieldPath, Object testObject, FieldType fieldType)
            throws Exception, IOException, JsonGenerationException, JsonMappingException {
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath(fieldPath);
        field.setValue(testObject);
        field.setFieldType(fieldType);
        write(field);
        writer.getObjectMapper().writeValue(path.toFile(), writer.getRootNode());
    }

    private AtlasInternalSession read(Path path, FieldType outputFieldType, String fieldPath)
            throws IOException, AtlasException {
        String document = new String(Files.readAllBytes(path));
        reader.setDocument(document);
        JsonField jsonField = AtlasJsonModelFactory.createJsonField();
        jsonField.setPath(fieldPath);
        jsonField.setFieldType(outputFieldType);
        AtlasInternalSession session = mock(AtlasInternalSession.class);
        when(session.head()).thenReturn(mock(Head.class));
        when(session.head().getSourceField()).thenReturn(jsonField);

        Audits audits = new Audits();
        when(session.getAudits()).thenReturn(audits);
        reader.read(session);
        return session;
    }

    private void testRangeOutValue(String fileName, String fieldPath, Object testObject, FieldType inputFieldType,
            FieldType outputFieldType) throws Exception {
        Path path = Paths.get("target" + File.separator + fileName);
        write(path, fieldPath, testObject, inputFieldType);

        AtlasInternalSession session = read(path, outputFieldType, fieldPath);

        assertEquals(null, session.head().getSourceField().getValue());
        assertEquals(1, session.getAudits().getAudit().size());
        assertEquals(
                "Failed to convert field value '" + testObject.toString() + "' into type '"
                        + outputFieldType.value().toUpperCase() + "'",
                session.getAudits().getAudit().get(0).getMessage());
        assertEquals(testObject.toString(), session.getAudits().getAudit().get(0).getValue());
        assertEquals(AuditStatus.ERROR, session.getAudits().getAudit().get(0).getStatus());
    }

    private void write(Field field) throws Exception {
        AtlasInternalSession session = mock(AtlasInternalSession.class);
        when(session.head()).thenReturn(mock(Head.class));
        when(session.head().getSourceField()).thenReturn(mock(Field.class));
        when(session.head().getTargetField()).thenReturn(field);
        writer.write(session);
    }

    private String prettyPrintJson(String json) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        Object objJSON = objectMapper.readValue(json, Object.class);
        return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(objJSON);
    }

    private void testValue(String fileName, String fieldPath, Object testObject, FieldType fieldType)
            throws Exception {
        Path path = Paths.get("target" + File.separator + fileName);
        write(path, fieldPath, testObject, fieldType);

        AtlasInternalSession session = read(path, fieldType, fieldPath);
        assertEquals(testObject, session.head().getSourceField().getValue());
    }

    private void testValue(String fileName, String fieldPath, Object testObject, FieldType inputFieldType,
            FieldType outputFieldType, Object expectedValue) throws Exception {
        Path path = Paths.get("target" + File.separator + fileName);
        write(path, fieldPath, testObject, inputFieldType);

        AtlasInternalSession session = read(path, outputFieldType, fieldPath);

        assertEquals(expectedValue, session.head().getSourceField().getValue());
        assertEquals(0, session.getAudits().getAudit().size());
    }

}
