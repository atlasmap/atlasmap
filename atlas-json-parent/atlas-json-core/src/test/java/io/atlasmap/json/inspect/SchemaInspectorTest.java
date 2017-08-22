package io.atlasmap.json.inspect;

import static org.junit.Assert.assertEquals;
import io.atlasmap.json.inspect.JsonDocumentInspectionService;
import io.atlasmap.json.v2.JsonComplexType;
import io.atlasmap.json.v2.JsonDocument;
import io.atlasmap.json.v2.JsonField;
import io.atlasmap.v2.CollectionType;
import io.atlasmap.v2.Field;
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

    @Test
    public void inspectJsonSchema_geo() throws Exception {
        final String schema = new String(Files
                .readAllBytes(Paths.get("src/test/resources/inspect/schema/geo.json")));
        JsonDocument document = inspectionService.inspectJsonSchema(schema);
        List<Field> fields = document.getFields().getField();
        JsonComplexType f = (JsonComplexType) fields.get(0);
        assertEquals("latitude", f.getName());
        assertEquals("/latitude", f.getPath());
        assertEquals(FieldType.NUMBER, f.getFieldType());
        f = (JsonComplexType) fields.get(1);
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
        JsonComplexType f = (JsonComplexType) fields.get(0);
        assertEquals("post-office-box", f.getName());
        assertEquals("/post-office-box", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonComplexType) fields.get(1);
        assertEquals("extended-address", f.getName());
        assertEquals("/extended-address", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonComplexType) fields.get(2);
        assertEquals("street-address", f.getName());
        assertEquals("/street-address", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonComplexType) fields.get(3);
        assertEquals("locality", f.getName());
        assertEquals("/locality", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonComplexType) fields.get(4);
        assertEquals("region", f.getName());
        assertEquals("/region", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonComplexType) fields.get(5);
        assertEquals("postal-code", f.getName());
        assertEquals("/postal-code", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonComplexType) fields.get(6);
        assertEquals("country-name", f.getName());
        assertEquals("/country-name", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
    }

    @Ignore("TODO support $ref")
    @Test
    public void inspectJsonSchema_calendar() throws Exception {
        final String instance = new String(Files
                .readAllBytes(Paths.get("src/test/resources/inspect/schema/calendar.json")));
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
        JsonComplexType f = (JsonComplexType) fields.get(0);
        assertEquals("dtstart", f.getName());
        assertEquals("/dtstart", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonComplexType) fields.get(1);
        assertEquals("dtend", f.getName());
        assertEquals("/dtend", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonComplexType) fields.get(2);
        assertEquals("summary", f.getName());
        assertEquals("/summary", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonComplexType) fields.get(3);
        assertEquals("location", f.getName());
        assertEquals("/location", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonComplexType) fields.get(4);
        assertEquals("url", f.getName());
        assertEquals("/url", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonComplexType) fields.get(5);
        assertEquals("duration", f.getName());
        assertEquals("/duration", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonComplexType) fields.get(6);
        assertEquals("rdate", f.getName());
        assertEquals("/rdate", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonComplexType) fields.get(7);
        assertEquals("rrule", f.getName());
        assertEquals("/rrule", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonComplexType) fields.get(8);
        assertEquals("category", f.getName());
        assertEquals("/category", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonComplexType) fields.get(9);
        assertEquals("description", f.getName());
        assertEquals("/description", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonComplexType) fields.get(10);
        assertEquals("geo", f.getName());
        assertEquals("/geo", f.getPath());
        assertEquals(FieldType.COMPLEX, f.getFieldType());
        List<JsonField> geofields = f.getJsonFields().getJsonField();
        f = (JsonComplexType) geofields.get(0);
        assertEquals("latitude", f.getName());
        assertEquals("/geo/latitude", f.getPath());
        assertEquals(FieldType.NUMBER, f.getFieldType());
        f = (JsonComplexType) geofields.get(1);
        assertEquals("longitude", f.getName());
        assertEquals("/geo/longitude", f.getPath());
        assertEquals(FieldType.NUMBER, f.getFieldType());
    }

    @Ignore("TODO support $ref")
    @Test
    public void inspectJsonSchema_card() throws Exception {
        final String schema = new String(Files
                .readAllBytes(Paths.get("src/test/resources/inspect/schema/card.json")));
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
        JsonComplexType f = (JsonComplexType) fields.get(0);
        assertEquals("fn", f.getName());
        assertEquals("/fn", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonComplexType) fields.get(1);
        assertEquals("familyName", f.getName());
        assertEquals("/familyName", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonComplexType) fields.get(2);
        assertEquals("givenName", f.getName());
        assertEquals("/givenName", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonComplexType) fields.get(3);
        assertEquals("additionalName", f.getName());
        assertEquals("/additionalName", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        assertEquals(CollectionType.ARRAY, f.getCollectionType());
        f = (JsonComplexType) fields.get(4);
        assertEquals("honorificPrefix", f.getName());
        assertEquals("/honorificPrefix", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        assertEquals(CollectionType.ARRAY, f.getCollectionType());
        f = (JsonComplexType) fields.get(5);
        assertEquals("honorificSuffix", f.getName());
        assertEquals("/honorificSuffix", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        assertEquals(CollectionType.ARRAY, f.getCollectionType());
        f = (JsonComplexType) fields.get(6);
        assertEquals("nickname", f.getName());
        assertEquals("/nickname", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonComplexType) fields.get(7);
        assertEquals("url", f.getName());
        assertEquals("/url", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonComplexType) fields.get(8);
        assertEquals("email", f.getName());
        assertEquals("/email", f.getPath());
        assertEquals(FieldType.COMPLEX, f.getFieldType());
        List<JsonField> emailfields = f.getJsonFields().getJsonField();
        f = (JsonComplexType) emailfields.get(0);
        assertEquals("type", f.getName());
        assertEquals("/email/type", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonComplexType) emailfields.get(1);
        assertEquals("value", f.getName());
        assertEquals("/email/value", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonComplexType) fields.get(9);
        assertEquals("tel", f.getName());
        assertEquals("/tel", f.getPath());
        assertEquals(FieldType.COMPLEX, f.getFieldType());
        List<JsonField> telfields = f.getJsonFields().getJsonField();
        f = (JsonComplexType) telfields.get(0);
        assertEquals("type", f.getName());
        assertEquals("/tel/type", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonComplexType) telfields.get(1);
        assertEquals("value", f.getName());
        assertEquals("/tel/value", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonComplexType) fields.get(10);
        assertEquals("adr", f.getName());
        assertEquals("/adr", f.getPath());
        assertEquals(FieldType.COMPLEX, f.getFieldType());
        List<JsonField> addrfields = f.getJsonFields().getJsonField();
        f = (JsonComplexType) addrfields.get(0);
        assertEquals("post-office-box", f.getName());
        assertEquals("/adr/post-office-box", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonComplexType) addrfields.get(1);
        assertEquals("extended-address", f.getName());
        assertEquals("/adr/extended-address", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonComplexType) addrfields.get(2);
        assertEquals("street-address", f.getName());
        assertEquals("/adr/street-address", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonComplexType) addrfields.get(3);
        assertEquals("locality", f.getName());
        assertEquals("/adr/locality", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonComplexType) addrfields.get(4);
        assertEquals("region", f.getName());
        assertEquals("/adr/region", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonComplexType) addrfields.get(5);
        assertEquals("postal-code", f.getName());
        assertEquals("/adr/postal-code", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonComplexType) addrfields.get(6);
        assertEquals("country-name", f.getName());
        assertEquals("/adr/country-name", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonComplexType) fields.get(11);
        assertEquals("geo", f.getName());
        assertEquals("/geo", f.getPath());
        assertEquals(FieldType.COMPLEX, f.getFieldType());
        List<JsonField> geofields = f.getJsonFields().getJsonField();
        f = (JsonComplexType) geofields.get(0);
        assertEquals("latitude", f.getName());
        assertEquals("/geo/latitude", f.getPath());
        assertEquals(FieldType.NUMBER, f.getFieldType());
        f = (JsonComplexType) geofields.get(1);
        assertEquals("longitude", f.getName());
        assertEquals("/geo/longitude", f.getPath());
        assertEquals(FieldType.NUMBER, f.getFieldType());
        f = (JsonComplexType) fields.get(12);
        assertEquals("tz", f.getName());
        assertEquals("/tz", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonComplexType) fields.get(13);
        assertEquals("photo", f.getName());
        assertEquals("/photo", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonComplexType) fields.get(14);
        assertEquals("logo", f.getName());
        assertEquals("/logo", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonComplexType) fields.get(15);
        assertEquals("sound", f.getName());
        assertEquals("/sound", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonComplexType) fields.get(16);
        assertEquals("bday", f.getName());
        assertEquals("/bday", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonComplexType) fields.get(17);
        assertEquals("title", f.getName());
        assertEquals("/title", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonComplexType) fields.get(18);
        assertEquals("role", f.getName());
        assertEquals("/role", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonComplexType) fields.get(19);
        assertEquals("org", f.getName());
        assertEquals("/org", f.getPath());
        assertEquals(FieldType.COMPLEX, f.getFieldType());
        List<JsonField> orgfields = f.getJsonFields().getJsonField();
        f = (JsonComplexType) orgfields.get(0);
        assertEquals("organizationName", f.getName());
        assertEquals("/org/organizationName", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
        f = (JsonComplexType) orgfields.get(1);
        assertEquals("organizationUnit", f.getName());
        assertEquals("/org/organizationUnit", f.getPath());
        assertEquals(FieldType.STRING, f.getFieldType());
    }
}
