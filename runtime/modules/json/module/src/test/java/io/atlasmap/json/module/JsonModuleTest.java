package io.atlasmap.json.module;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.atlasmap.json.v2.JsonField;
import io.atlasmap.v2.ConstantField;
import io.atlasmap.v2.PropertyField;
import io.atlasmap.v2.SimpleField;

public class JsonModuleTest {

    private JsonModule module = null;

    @Before
    public void setUp() throws Exception {
        module = new JsonModule();
    }

    @After
    public void tearDown() throws Exception {
        module = null;
    }

    @Test
    public void testIsSupportedField() {
        assertTrue(module.isSupportedField(new JsonField()));
        assertFalse(module.isSupportedField(new PropertyField()));
        assertFalse(module.isSupportedField(new ConstantField()));
        assertTrue(module.isSupportedField(new SimpleField()));
    }

}
