package io.atlasmap.json.core;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

import java.nio.file.Files;
import java.nio.file.Paths;

import org.hamcrest.core.Is;
import org.junit.Test;

import io.atlasmap.api.AtlasException;
import io.atlasmap.json.v2.AtlasJsonModelFactory;
import io.atlasmap.json.v2.JsonField;

public class JsonFieldReaderTest {

    private static JsonFieldReader reader = new JsonFieldReader();

    @Test(expected = AtlasException.class)
    public void testWithNullDocument() throws Exception {
        reader.setDocument(null);
        reader.read(AtlasJsonModelFactory.createJsonField());
    }

    @Test(expected = AtlasException.class)
    public void testWithEmptyDocument() throws Exception {
        reader.setDocument("");
        reader.read(AtlasJsonModelFactory.createJsonField());
    }

    @Test(expected = AtlasException.class)
    public void testWithNullJsonField() throws Exception {
        reader.setDocument("{qwerty : ytrewq}");
        reader.read(null);
    }

    @Test
    public void testSimpleJsonDocument() throws Exception {
        final String document = "   { \"brand\" : \"Mercedes\", \"doors\" : 5 }";
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/brand");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Mercedes"));

        field.setPath("/doors");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is(5));

    }

    @Test
    public void testSimpleJsonDocumentWithRoot() throws Exception {
        final String document = " {\"car\" :{ \"brand\" : \"Mercedes\", \"doors\" : 5 } }";
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/car/doors");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is(5));
        resetField(field);

        field.setPath("/car/brand");
        reader.read(field);
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
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("file"));

        field.setPath("/menu/value");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Filed"));

        field.setPath("/menu/popup/menuitem[0]/value");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("New"));

        field.setPath("/menu/popup/menuitem[0]/onclick");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("CreateNewDoc()"));

        field.setPath("/menu/popup/menuitem[1]/value");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Open"));

        field.setPath("/menu/popup/menuitem[1]/onclick");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("OpenDoc()"));

        field.setPath("/menu/popup/menuitem[2]/value");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Close"));

        field.setPath("/menu/popup/menuitem[2]/onclick");
        reader.read(field);
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
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("0001"));
        resetField(field);

        field.setPath("/type");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("donut"));
        resetField(field);

        field.setPath("/name");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Cake"));
        resetField(field);

        field.setPath("/ppu");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is(0.55));
        resetField(field);

        field.setPath("/batters/batter[0]/id");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("1001"));
        resetField(field);

        field.setPath("/batters/batter[0]/type");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Regular"));
        resetField(field);

        field.setPath("/batters/batter[1]/id");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("1002"));
        resetField(field);

        field.setPath("/batters/batter[1]/type");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Chocolate"));
        resetField(field);

        field.setPath("/batters/batter[2]/id");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("1003"));
        resetField(field);

        field.setPath("/batters/batter[2]/type");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Blueberry"));
        resetField(field);

        field.setPath("/batters/batter[3]/id");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("1004"));
        resetField(field);

        field.setPath("/batters/batter[3]/type");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Devil's Food"));
        resetField(field);

        field.setPath("/topping[0]/id");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("5001"));
        resetField(field);

        field.setPath("/topping[0]/type");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("None"));
        resetField(field);

        field.setPath("/topping[1]/id");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("5002"));
        resetField(field);

        field.setPath("/topping[1]/type");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Glazed"));
        resetField(field);

        field.setPath("/topping[2]/id");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("5005"));
        resetField(field);

        field.setPath("/topping[2]/type");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Sugar"));
        resetField(field);

        field.setPath("/topping[3]/id");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("5007"));
        resetField(field);

        field.setPath("/topping[3]/type");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Powdered Sugar"));
        resetField(field);

        field.setPath("/topping[4]/id");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("5006"));
        resetField(field);

        field.setPath("/topping[4]/type");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Chocolate with Sprinkles"));
        resetField(field);

        field.setPath("/topping[5]/id");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("5003"));
        resetField(field);

        field.setPath("/topping[5]/type");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Chocolate"));
        resetField(field);

        field.setPath("/topping[6]/id");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("5004"));
        resetField(field);

        field.setPath("/topping[6]/type");
        reader.read(field);
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
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("0001"));
        resetField(field);

        field.setPath("/items/item[0]/type");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("donut"));
        resetField(field);

        field.setPath("/items/item[0]/name");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Cake"));
        resetField(field);

        field.setPath("/items/item[0]/ppu");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is(0.55));
        resetField(field);

        // array of objects
        field.setPath("/items/item[0]/batters/batter[0]/id");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("1001"));
        resetField(field);

        field.setPath("/items/item[0]/batters/batter[0]/type");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Regular"));
        resetField(field);

        field.setPath("/items/item[0]/batters/batter[1]/id");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("1002"));
        resetField(field);

        field.setPath("/items/item[0]/batters/batter[1]/type");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Chocolate"));
        resetField(field);

        field.setPath("/items/item[0]/batters/batter[2]/id");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("1003"));
        resetField(field);

        field.setPath("/items/item[0]/batters/batter[2]/type");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Blueberry"));
        resetField(field);

        field.setPath("/items/item[0]/batters/batter[3]/id");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("1004"));
        resetField(field);

        field.setPath("/items/item[0]/batters/batter[3]/type");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Devil's Food"));
        resetField(field);

        // simple array
        field.setPath("/items/item[0]/topping[0]/id");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("5001"));
        resetField(field);

        field.setPath("/items/item[0]/topping[0]/type");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("None"));
        resetField(field);

        field.setPath("/items/item[0]/topping[1]/id");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("5002"));
        resetField(field);

        field.setPath("/items/item[0]/topping[1]/type");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Glazed"));
        resetField(field);

        field.setPath("/items/item[0]/topping[2]/id");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("5005"));
        resetField(field);

        field.setPath("/items/item[0]/topping[2]/type");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Sugar"));
        resetField(field);

        field.setPath("/items/item[0]/topping[3]/id");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("5007"));
        resetField(field);

        field.setPath("/items/item[0]/topping[3]/type");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Powdered Sugar"));
        resetField(field);

        field.setPath("/items/item[0]/topping[4]/id");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("5006"));
        resetField(field);

        field.setPath("/items/item[0]/topping[4]/type");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Chocolate with Sprinkles"));
        resetField(field);

        field.setPath("/items/item[0]/topping[5]/id");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("5003"));
        resetField(field);

        field.setPath("/items/item[0]/topping[5]/type");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Chocolate"));
        resetField(field);

        field.setPath("/items/item[0]/topping[6]/id");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("5004"));
        resetField(field);

        field.setPath("/items/item[0]/topping[6]/type");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Maple"));
        resetField(field);

        field.setPath("/items/item[1]/id");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("0002"));
        resetField(field);

        field.setPath("/items/item[1]/type");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("donut"));
        resetField(field);

        field.setPath("/items/item[1]/name");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Raised"));
        resetField(field);

        field.setPath("/items/item[1]/ppu");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is(0.55));
        resetField(field);

        // array of objects
        field.setPath("/items/item[1]/batters/batter[0]/id");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("1001"));
        resetField(field);

        field.setPath("/items/item[1]/batters/batter[0]/type");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Regular"));
        resetField(field);

        field.setPath("/items/item[1]/topping[0]/id");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("5001"));
        resetField(field);

        field.setPath("/items/item[1]/topping[0]/type");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("None"));
        resetField(field);

        field.setPath("/items/item[1]/topping[1]/id");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("5002"));
        resetField(field);

        field.setPath("/items/item[1]/topping[1]/type");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Glazed"));
        resetField(field);

        field.setPath("/items/item[1]/topping[2]/id");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("5005"));
        resetField(field);

        field.setPath("/items/item[1]/topping[2]/type");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Sugar"));
        resetField(field);

        field.setPath("/items/item[1]/topping[3]/id");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("5003"));
        resetField(field);

        field.setPath("/items/item[1]/topping[3]/type");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Chocolate"));
        resetField(field);

        field.setPath("/items/item[1]/topping[4]/id");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("5004"));
        resetField(field);

        field.setPath("/items/item[1]/topping[4]/type");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Maple"));
        resetField(field);

        field.setPath("/items/item[1]/topping[5]/id");
        reader.read(field);
        assertNull(field.getValue());
        resetField(field);

        field.setPath("/items/item[2]/id");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("0003"));
        resetField(field);

        field.setPath("/items/item[2]/type");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("donut"));
        resetField(field);

        field.setPath("/items/item[2]/name");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Old Fashioned"));
        resetField(field);

        field.setPath("/items/item[2]/ppu");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is(0.55));
        resetField(field);

        field.setPath("/items/item[3]/id");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("0004"));
        resetField(field);

        field.setPath("/items/item[3]/type");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("bar"));
        resetField(field);

        field.setPath("/items/item[3]/name");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Bar"));
        resetField(field);

        field.setPath("/items/item[3]/ppu");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is(0.75));
        resetField(field);

        field.setPath("/items/item[4]/id");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("0005"));
        resetField(field);

        field.setPath("/items/item[4]/type");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("twist"));
        resetField(field);

        field.setPath("/items/item[4]/name");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Twist"));
        resetField(field);

        field.setPath("/items/item[4]/ppu");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is(0.65));
        resetField(field);

        field.setPath("/items/item[5]/id");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("0006"));
        resetField(field);

        field.setPath("/items/item[5]/type");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("filled"));
        resetField(field);

        field.setPath("/items/item[5]/name");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("Filled"));
        resetField(field);

        field.setPath("/items/item[5]/ppu");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is(0.75));
        resetField(field);

        field.setPath("/items/item[5]/fillings/filling[2]/id");
        reader.read(field);
        assertNotNull(field.getValue());
        assertThat(field.getValue(), Is.is("7004"));
        resetField(field);

        field.setPath("/items/item[5]/fillings/filling[3]/addcost");
        reader.read(field);
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
        reader.read(field);
        assertEquals("name", field.getValue());
        field.setPath("/object1/name");
        reader.read(field);
        assertEquals("object1-name", field.getValue());
        field.setPath("/object2/name");
        reader.read(field);
        assertEquals("object2-name", field.getValue());
        field.setPath("/object1/object2/name");
        reader.read(field);
        assertEquals("object1-object2-name", field.getValue());
    }

    @Test
    public void testArrayUnderRoot() throws Exception {
        final String document = new String(
                Files.readAllBytes(Paths.get("src/test/resources/array-under-root.json")));
        reader.setDocument(document);
        JsonField field = AtlasJsonModelFactory.createJsonField();
        field.setPath("/array[0]");
        reader.read(field);
        assertEquals("array-zero", field.getValue());
        field.setPath("/array[1]");
        reader.read(field);
        assertEquals("array-one", field.getValue());
        field.setPath("/array[2]");
        reader.read(field);
        assertEquals("array-two", field.getValue());
    }

    private void resetField(JsonField field) {
        field.setPath(null);
        field.setValue(null);
        field.setFieldType(null);
    }
}
