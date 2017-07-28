package io.atlasmap.json.core;

import io.atlasmap.api.AtlasException;
import io.atlasmap.json.v2.AtlasJsonModelFactory;
import io.atlasmap.json.v2.JsonField;

import org.hamcrest.core.Is;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Paths;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public class DocumentJsonFieldReaderTest {

    private static DocumentJsonFieldReader reader = new DocumentJsonFieldReader();

    @Test(expected = AtlasException.class)
    public void testWithNullDocument() throws Exception {
        reader.read(null, AtlasJsonModelFactory.createJsonField());
    }

    @Test(expected = AtlasException.class)
    public void testWithEmptyDocument() throws Exception {
        reader.read("", AtlasJsonModelFactory.createJsonField());
    }

    @Test(expected = AtlasException.class)
    public void testWithNullJsonField() throws Exception {
        reader.read("{qwerty : ytrewq}", null);
    }


    @Test
    public void testSimpleJsonDocument() throws Exception {
        final String document = "   { \"brand\" : \"Mercedes\", \"doors\" : 5 }";
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/brand");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Mercedes"));

        field.setPath("/doors");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is(5));

    }

    @Test
    public void testSimpleJsonDocument_WithRoot() throws Exception {
        final String document = " {\"car\" :{ \"brand\" : \"Mercedes\", \"doors\" : 5 } }";
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/car/doors");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is(5));
        resetField(field);
        
        field.setPath("/car/brand");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Mercedes"));
    }

    @Test
    public void testComplexJsonDocument_NestedObjectArray() throws Exception {
        final String document = "{\"menu\": {\n" +
            "  \"id\": \"file\",\n" +
            "  \"value\": \"Filed\",\n" +
            "  \"popup\": {\n" +
            "    \"menuitem\": [\n" +
            "      {\"value\": \"New\", \"onclick\": \"CreateNewDoc()\"},\n" +
            "      {\"value\": \"Open\", \"onclick\": \"OpenDoc()\"},\n" +
            "      {\"value\": \"Close\", \"onclick\": \"CloseDoc()\"}\n" +
            "    ]\n" +
            "  }\n" +
            "}}";

        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/menu/id");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("file"));

        field.setPath("/menu/value");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Filed"));

        field.setPath("/menu/popup/menuitem/value");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("New"));

        field.setPath("/menu/popup/menuitem/onclick");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("CreateNewDoc()"));

        field.setPath("/menu/popup/menuitem[1]/value");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Open"));

        field.setPath("/menu/popup/menuitem[1]/onclick");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("OpenDoc()"));

        field.setPath("/menu/popup/menuitem[2]/value");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Close"));

        field.setPath("/menu/popup/menuitem[2]/onclick");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("CloseDoc()"));
    }

    @Test
    public void testComplexJsonDocument_HighlyNested() throws Exception {
        final String document = new String(Files.readAllBytes(Paths.get("src/test/resources/highly-nested-object.json")));
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/id");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("0001"));
        resetField(field);

        field.setPath("/type");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("donut"));
        resetField(field);

        field.setPath("/name");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Cake"));
        resetField(field);

        field.setPath("/ppu");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is(0.55));
        resetField(field);

        field.setPath("/batters/batter/id");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("1001"));
        resetField(field);

        field.setPath("/batters/batter/type");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Regular"));
        resetField(field);

        field.setPath("/batters/batter[1]/id");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("1002"));
        resetField(field);

        field.setPath("/batters/batter[1]/type");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Chocolate"));
        resetField(field);

        field.setPath("/batters/batter[2]/id");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("1003"));
        resetField(field);

        field.setPath("/batters/batter[2]/type");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Blueberry"));
        resetField(field);

        field.setPath("/batters/batter[3]/id");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("1004"));
        resetField(field);

        field.setPath("/batters/batter[3]/type");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Devil's Food"));
        resetField(field);

        field.setPath("/topping/id");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("5001"));
        resetField(field);

        field.setPath("/topping/type");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("None"));
        resetField(field);

        field.setPath("/topping/id[1]");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("5002"));
        resetField(field);

        field.setPath("/topping/type[1]");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Glazed"));
        resetField(field);

        field.setPath("/topping/id[2]");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("5005"));
        resetField(field);

        field.setPath("/topping/type[2]");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Sugar"));
        resetField(field);

        field.setPath("/topping/id[3]");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("5007"));
        resetField(field);

        field.setPath("/topping/type[3]");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Powdered Sugar"));
        resetField(field);

        field.setPath("/topping/id[4]");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("5006"));
        resetField(field);

        field.setPath("/topping/type[4]");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Chocolate with Sprinkles"));
        resetField(field);

        field.setPath("/topping/id[5]");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("5003"));
        resetField(field);

        field.setPath("/topping/type[5]");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Chocolate"));
        resetField(field);

        field.setPath("/topping/id[6]");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("5004"));
        resetField(field);

        field.setPath("/topping/type[6]");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Maple"));
        resetField(field);
    }
    
    @Test
    public void testCollectionCount_HighlyNested() throws Exception {
        final String document = new String(Files.readAllBytes(Paths.get("src/test/resources/highly-nested-object.json")));
        JsonField field = AtlasJsonModelFactory.createJsonField();
        Integer count = reader.getCollectionCount(document, field, "batter");
        assertNotNull(count);
        assertEquals(Integer.valueOf(4), count);
    }
    
    @Test
    public void testCollectionCount_HighlyNestedSegmentDoesNotExist() throws Exception {
        final String document = new String(Files.readAllBytes(Paths.get("src/test/resources/highly-nested-object.json")));
        JsonField field = AtlasJsonModelFactory.createJsonField();
        Integer count = reader.getCollectionCount(document, field, "battery");
        assertNull(count);
    }

    @Test
    public void testComplexJsonDocument_HighlyComplexNested() throws Exception {
        final String document = new String(Files.readAllBytes(Paths.get("src/test/resources/highly-complex-nested-object.json")));
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/items/item/id");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("0001"));
        resetField(field);

        field.setPath("/items/item/type");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("donut"));
        resetField(field);

        field.setPath("/items/item/name");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Cake"));
        resetField(field);

        field.setPath("/items/item/ppu");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is(0.55));
        resetField(field);

        //array of objects
        field.setPath("/items/item/batters/batter/id");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("1001"));
        resetField(field);

        field.setPath("/items/item/batters/batter/type");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Regular"));
        resetField(field);

        field.setPath("/items/item/batters/batter[1]/id");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("1002"));
        resetField(field);

        field.setPath("/items/item/batters/batter[1]/type");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Chocolate"));
        resetField(field);

        field.setPath("/items/item/batters/batter[2]/id");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("1003"));
        resetField(field);

        field.setPath("/items/item/batters/batter[2]/type");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Blueberry"));
        resetField(field);

        field.setPath("/items/item/batters/batter[3]/id");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("1004"));
        resetField(field);

        field.setPath("/items/item/batters/batter[3]/type");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Devil's Food"));
        resetField(field);

        //simple array
        field.setPath("/items/item/topping/id");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("5001"));
        resetField(field);

        field.setPath("/items/item/topping/type");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("None"));
        resetField(field);

        field.setPath("/items/item/topping/id[1]");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("5002"));
        resetField(field);

        field.setPath("/items/item/topping/type[1]");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Glazed"));
        resetField(field);

        field.setPath("/items/item/topping/id[2]");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("5005"));
        resetField(field);

        field.setPath("/items/item/topping/type[2]");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Sugar"));
        resetField(field);

        field.setPath("/items/item/topping/id[3]");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("5007"));
        resetField(field);

        field.setPath("/items/item/topping/type[3]");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Powdered Sugar"));
        resetField(field);

        field.setPath("/items/item/topping/id[4]");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("5006"));
        resetField(field);

        field.setPath("/items/item/topping/type[4]");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Chocolate with Sprinkles"));
        resetField(field);

        field.setPath("/items/item/topping/id[5]");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("5003"));
        resetField(field);

        field.setPath("/items/item/topping/type[5]");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Chocolate"));
        resetField(field);

        field.setPath("/items/item/topping/id[6]");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("5004"));
        resetField(field);

        field.setPath("/items/item/topping/type[6]");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Maple"));
        resetField(field);

        field.setPath("/items/item[1]/id");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("0002"));
        resetField(field);

        field.setPath("/items/item[1]/type");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("donut"));
        resetField(field);

        field.setPath("/items/item[1]/name");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Raised"));
        resetField(field);

        field.setPath("/items/item[1]/ppu");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is(0.55));
        resetField(field);

        //array of objects
        field.setPath("/items/item[1]/batters/batter/id");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("1001"));
        resetField(field);

        field.setPath("/items/item[1]/batters/batter/type");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Regular"));
        resetField(field);

        field.setPath("/items/item[1]/topping/id");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("5001"));
        resetField(field);

        field.setPath("/items/item[1]/topping/type");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("None"));
        resetField(field);

        field.setPath("/items/item[1]/topping/id[1]");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("5002"));
        resetField(field);

        field.setPath("/items/item[1]/topping/type[1]");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Glazed"));
        resetField(field);

        field.setPath("/items/item[1]/topping/id[2]");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("5005"));
        resetField(field);

        field.setPath("/items/item[1]/topping/type[2]");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Sugar"));
        resetField(field);

        field.setPath("/items/item[1]/topping/id[3]");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("5003"));
        resetField(field);

        field.setPath("/items/item[1]/topping/type[3]");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Chocolate"));
        resetField(field);

        field.setPath("/items/item[1]/topping/id[4]");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("5004"));
        resetField(field);

        field.setPath("/items/item[1]/topping/type[4]");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Maple"));
        resetField(field);

        field.setPath("/items/item[1]/topping/id[5]");
        reader.read(document, field);
        assertNull(field.getValue());
        resetField(field);

        field.setPath("/items/item[2]/id");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("0003"));
        resetField(field);

        field.setPath("/items/item[2]/type");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("donut"));
        resetField(field);

        field.setPath("/items/item[2]/name");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Old Fashioned"));
        resetField(field);

        field.setPath("/items/item[2]/ppu");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is(0.55));
        resetField(field);

        field.setPath("/items/item[3]/id");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("0004"));
        resetField(field);

        field.setPath("/items/item[3]/type");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("bar"));
        resetField(field);

        field.setPath("/items/item[3]/name");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Bar"));
        resetField(field);

        field.setPath("/items/item[3]/ppu");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is(0.75));
        resetField(field);

        field.setPath("/items/item[4]/id");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("0005"));
        resetField(field);

        field.setPath("/items/item[4]/type");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("twist"));
        resetField(field);

        field.setPath("/items/item[4]/name");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Twist"));
        resetField(field);

        field.setPath("/items/item[4]/ppu");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is(0.65));
        resetField(field);

        field.setPath("/items/item[5]/id");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("0006"));
        resetField(field);

        field.setPath("/items/item[5]/type");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("filled"));
        resetField(field);

        field.setPath("/items/item[5]/name");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Filled"));
        resetField(field);

        field.setPath("/items/item[5]/ppu");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is(0.75));
        resetField(field);

        field.setPath("/items/item[5]/fillings/filling[2]/id");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("7004"));
        resetField(field);

        field.setPath("/items/item[5]/fillings/filling[3]/addcost");
        reader.read(document, field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is(0));
        resetField(field);
    }

    private void resetField(JsonField field) {
        field.setPath(null);
        field.setValue(null);
        field.setFieldType(null);
    }
}
