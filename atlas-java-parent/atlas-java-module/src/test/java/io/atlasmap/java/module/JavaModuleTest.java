package io.atlasmap.java.module;

import java.lang.reflect.Method;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.atlasmap.api.AtlasException;
import io.atlasmap.java.inspect.ClassInspectionService;
import io.atlasmap.java.inspect.JavaConstructService;
import io.atlasmap.java.test.SourceContact;
import io.atlasmap.java.v2.JavaEnumField;
import io.atlasmap.java.v2.JavaField;
import io.atlasmap.v2.ConstantField;
import io.atlasmap.v2.PropertyField;
import io.atlasmap.v2.SimpleField;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

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
    public void testResolveGetMethod() throws AtlasException {
        Object sourceObject = new SourceContact();
        JavaField field = new JavaField();
        field.setPath("/firstName");
        Method getter = JavaModule.resolveGetMethod(sourceObject, field, false);
        assertNotNull(getter);
        assertEquals(getter.getName(), "getFirstName");
    }

    @Test
    public void testResolveInputSetMethod() throws AtlasException {
        Object sourceObject = new SourceContact();
        JavaField field = new JavaField();
        field.setPath("/firstName");
        Method setter = module.resolveInputSetMethod(sourceObject, field, String.class);
        assertNotNull(setter);
        assertEquals(setter.getName(), "setFirstName");
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
        assertTrue(module.isSupportedField(new PropertyField()));
        assertTrue(module.isSupportedField(new ConstantField()));
        assertTrue(module.isSupportedField(new SimpleField()));
    }

}
