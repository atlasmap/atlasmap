package io.atlasmap.java.module;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.atlasmap.java.inspect.ClassInspectionService;
import io.atlasmap.java.inspect.JavaConstructService;
import io.atlasmap.java.v2.JavaEnumField;
import io.atlasmap.java.v2.JavaField;
import io.atlasmap.v2.ConstantField;
import io.atlasmap.v2.PropertyField;
import io.atlasmap.v2.SimpleField;

public class JavaModuleTest {

    private JavaModule module = null;

    @Before
    public void setUp() throws Exception {
        module = new JavaModule();
    }

    @After
    public void tearDown() throws Exception {
        module = null;
    }

    @Test
    public void testGetSetJavaInspectionService() {
        ClassInspectionService cis = new ClassInspectionService();
        assertNotNull(module);
        assertNull(module.getJavaInspectionService());
        module.setJavaInspectionService(cis);
        assertNotNull(module.getJavaInspectionService());
        assertTrue(module.getJavaInspectionService() instanceof ClassInspectionService);
        assertSame(cis, module.getJavaInspectionService());
    }

    @Test
    public void testGetSetJavaConstructService() {
        JavaConstructService jcs = new JavaConstructService();
        assertNotNull(module);
        assertNull(module.getJavaConstructService());
        module.setJavaConstructService(jcs);
        assertNotNull(module.getJavaConstructService());
        assertTrue(module.getJavaConstructService() instanceof JavaConstructService);
        assertSame(jcs, module.getJavaConstructService());
    }

    @Test
    public void testIsSupportedField() {
        assertTrue(module.isSupportedField(new JavaField()));
        assertTrue(module.isSupportedField(new JavaEnumField()));
        assertFalse(module.isSupportedField(new PropertyField()));
        assertFalse(module.isSupportedField(new ConstantField()));
        assertTrue(module.isSupportedField(new SimpleField()));
    }

}
