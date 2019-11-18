package io.atlasmap.java.inspect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import io.atlasmap.core.AtlasPath;
import io.atlasmap.java.core.ClassHelper;
import io.atlasmap.java.test.BaseOrder;
import io.atlasmap.java.test.SourceAddress;
import io.atlasmap.java.test.SourceContact;
import io.atlasmap.java.test.SourceOrder;
import io.atlasmap.java.test.SourceOrderList;
import io.atlasmap.java.test.SourceParentOrder;
import io.atlasmap.spi.AtlasInternalSession;

public class ClassHelperTest {

    @Test
    public void testDetectGetter() throws Exception {
        Method getter = ClassHelper.detectGetterMethod(JavaGetterSetterModel.class, "getParam");
        assertNotNull(getter);
        assertEquals("getParam", getter.getName());
        assertEquals(String.class, getter.getReturnType());
    }

    @Test
    public void testDetectGetterNotFound() {
        try {
            Method setter = ClassHelper.detectGetterMethod(JavaGetterSetterModel.class, "getParam2");
            fail("NoSuchMethodException expected instead found=" + setter.getName());
        } catch (NoSuchMethodException e) {
            assertEquals(String.format("No matching getter method for class=%s method=%s",
                    JavaGetterSetterModel.class.getName(), "getParam2"), e.getMessage());
        }
    }

    @Test
    public void testDetectSetter() throws Exception {
        Method setter = ClassHelper.detectSetterMethod(JavaGetterSetterModel.class, "setParam", String.class);
        assertNotNull(setter);
        assertEquals("setParam", setter.getName());
        assertNotNull(setter.getParameters());
        assertEquals(new Integer(1), new Integer(setter.getParameterCount()));
        assertEquals(String.class, setter.getParameterTypes()[0]);
    }

    @Test
    public void testDetectSetterOverloaded() throws Exception {
        Method setter = ClassHelper.detectSetterMethod(JavaGetterSetterModel.class, "setOverloadParam", String.class);
        assertNotNull(setter);
        assertEquals("setOverloadParam", setter.getName());
        assertNotNull(setter.getParameters());
        assertEquals(new Integer(1), new Integer(setter.getParameterCount()));
        assertEquals(String.class, setter.getParameterTypes()[0]);
    }

    @Test
    public void testDetectSetterOverloadedNullParam() throws Exception {
        Method setter = ClassHelper.detectSetterMethod(JavaGetterSetterModel.class, "setOverloadParam", null);
        assertNotNull(setter);
        assertEquals("setOverloadParam", setter.getName());
        assertNotNull(setter.getParameters());
        assertEquals(new Integer(1), new Integer(setter.getParameterCount()));
        assertEquals(String.class, setter.getParameterTypes()[0]);
    }

    @Test
    public void testDetectSetterOverloadedNotPresentParamType() {
        try {
            Method setter = ClassHelper.detectSetterMethod(JavaGetterSetterModel.class, "setOverloadParam",
                    Short.class);
            fail("NoSuchMethodException expected instead found=" + setter.getName());
        } catch (NoSuchMethodException e) {
            assertEquals(
                    "No matching setter found for class=io.atlasmap.java.inspect.JavaGetterSetterModel method=setOverloadParam paramType=java.lang.Short",
                    e.getMessage());
        }
    }

    @Test
    public void testDetectSetterOverloadedNoGetter() {
        try {
            Method setter = ClassHelper.detectSetterMethod(JavaGetterSetterModel.class, "setOverloadParamNoGetter",
                    null);
            fail("NoSuchMethodException expected instead found=" + setter.getName());
        } catch (NoSuchMethodException e) {
            assertTrue(e.getMessage().startsWith(String.format("Unable to auto-detect setter class=%s method=%s",
                    JavaGetterSetterModel.class.getName(), "setOverloadParamNoGetter")));
        }
    }

    @Test
    public void testDetectSetterOverloadedNoMatch() {
        try {
            Method setter = ClassHelper.detectSetterMethod(JavaGetterSetterModel.class, "setOverloadParamNoMatch",
                    null);
            fail("NoSuchMethodException expected instead found=" + setter.getName());
        } catch (NoSuchMethodException e) {
            assertTrue(e.getMessage().startsWith(String.format("No matching setter found for class=%s method=%s",
                    JavaGetterSetterModel.class.getName(), "setOverloadParamNoMatch")));
        }
    }
}
