package io.atlasmap.json.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JsonServiceTest {

    private JsonService jsonService = null;

    @Before
    public void setUp() throws Exception {
        jsonService = new JsonService();
    }

    @After
    public void tearDown() throws Exception {
        jsonService = null;
    }

    @Test
    public void testValidJsonData() {
        assertTrue(jsonService.validJsonData("{ \"foo\":\"bar\" }"));
        assertTrue(jsonService.validJsonData("[ { \"foo\":\"bar\" }, { \"meow\":\"blah\" } ]"));
        assertFalse(jsonService.validJsonData("  [ { \"foo\":\"bar\" }, { \"meow\":\"blah\" } ]"));
        assertFalse(jsonService.validJsonData("  \"foo\":\"bar\" }, { \"meow\":\"blah\" } ]"));

        assertTrue(jsonService.validJsonData(jsonService.cleanJsonData("  { \"foo\":\"bar\" }")));
        assertTrue(jsonService.validJsonData(jsonService.cleanJsonData("{ \"foo\":\"bar\" }   ")));
        assertTrue(jsonService
                .validJsonData(jsonService.cleanJsonData("  [ { \"foo\":\"bar\" }, { \"meow\":\"blah\" } ]")));
        assertTrue(jsonService.validJsonData(jsonService.cleanJsonData("\b\t\n\f\r   { \"foo\":\"bar\" }")));

    }
}
