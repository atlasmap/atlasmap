package io.atlasmap.reference.json_to_json;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.File;
import org.junit.Test;
import io.atlasmap.api.AtlasContext;
import io.atlasmap.api.AtlasSession;
import io.atlasmap.reference.AtlasMappingBaseTest;

public class JsonJsonCollectionConversionTest extends AtlasMappingBaseTest {

    @Test
    public void testProcessCollectionListSimple() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
                new File("src/test/resources/jsonToJson/atlasmapping-collection-list-simple.xml").toURI());

        // contact<>.firstName -> contact<>.name

        String input = "{ \"contact\": [";
        for (int i = 0; i < 3; i++) {
            input += "{ \"firstName\": \"name" + i + "\"}";
            input += (i == 2) ? "" : ",";
        }
        input += "] }";

        AtlasSession session = context.createSession();
        session.setInput(input);
        context.process(session);

        Object object = session.getOutput();
        assertNotNull(object);
        assertTrue(object instanceof String);

        String output = "{\"contact\":[";
        for (int i = 0; i < 3; i++) {
            output += "{\"name\":\"name" + i + "\"}";
            output += (i == 2) ? "" : ",";
        }
        output += "]}";
        assertEquals(output, (String) object);
    }

    @Test
    public void testProcessCollectionArraySimple() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
                new File("src/test/resources/jsonToJson/atlasmapping-collection-array-simple.xml").toURI());

        // contact[].firstName -> contact[].name

        String input = "{ \"contact\": [";
        for (int i = 0; i < 3; i++) {
            input += "{ \"firstName\": \"name" + i + "\"}";
            input += (i == 2) ? "" : ",";
        }
        input += "] }";

        AtlasSession session = context.createSession();
        session.setInput(input);
        context.process(session);

        Object object = session.getOutput();
        assertNotNull(object);
        assertTrue(object instanceof String);

        String output = "{\"contact\":[";
        for (int i = 0; i < 3; i++) {
            output += "{\"name\":\"name" + i + "\"}";
            output += (i == 2) ? "" : ",";
        }
        output += "]}";
        assertEquals(output, (String) object);
    }

    @Test
    public void testProcessCollectionToNonCollection() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
                new File("src/test/resources/jsonToJson/atlasmapping-collection-to-noncollection.xml").toURI());

        // contact<>.firstName -> contact.name

        String input = "{ \"contact\": [";
        for (int i = 0; i < 3; i++) {
            input += "{ \"firstName\": \"name" + i + "\"}";
            input += (i == 2) ? "" : ",";
        }
        input += "] }";

        AtlasSession session = context.createSession();
        session.setInput(input);
        context.process(session);

        Object object = session.getOutput();
        assertNotNull(object);
        assertTrue(object instanceof String);

        String output = "{\"contact\":{\"name\":\"name2\"}}";
        assertEquals(output, (String) object);
    }

    @Test
    public void testProcessCollectionFromNonCollection() throws Exception {
        AtlasContext context = atlasContextFactory.createContext(
                new File("src/test/resources/jsonToJson/atlasmapping-collection-from-noncollection.xml").toURI());

        // contact.firstName -> contact<>.name

        String input = "{ \"contact\": [ { \"firstName\": \"name9\" } ] }";
        AtlasSession session = context.createSession();
        session.setInput(input);
        context.process(session);

        Object object = session.getOutput();
        assertNotNull(object);
        assertTrue(object instanceof String);

        String output = "{\"contact\":[{\"name\":\"name9\"}]}";
        assertEquals(output, (String) object);
    }
}
