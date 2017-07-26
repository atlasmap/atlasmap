package io.atlasmap.java.inspect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import io.atlasmap.core.PathUtil;
import io.atlasmap.java.test.BaseOrder;
import io.atlasmap.java.test.SourceAddress;
import io.atlasmap.java.test.SourceContact;
import io.atlasmap.java.test.SourceOrder;
import io.atlasmap.java.test.SourceOrderList;
import io.atlasmap.java.test.SourceParentOrder;

public class ClassHelperTest {
  
    @Test
    public void testDetectGetter() throws Exception {
        Method getter = ClassHelper.detectGetterMethod(JavaGetterSetterModel.class, "getParam");
        assertNotNull(getter);
        assertEquals("getParam", getter.getName());
        assertEquals(String.class, getter.getReturnType());    
    }
    
    @Test
    public void testDetectGetterNotFound() throws Exception {
        try {
            Method setter = ClassHelper.detectGetterMethod(JavaGetterSetterModel.class, "getParam2");
            fail("NoSuchMethodException expected instead found=" + setter.getName());
        } catch (NoSuchMethodException e) {
            assertEquals(String.format("No matching getter method for class=%s method=%s", JavaGetterSetterModel.class.getName(), "getParam2"), e.getMessage());
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
    public void testDetectSetterOverloadedNotPresentParamType() throws Exception {
        try {
            Method setter = ClassHelper.detectSetterMethod(JavaGetterSetterModel.class, "setOverloadParam", Short.class);
            fail("NoSuchMethodException expected instead found=" + setter.getName());
        } catch (NoSuchMethodException e) {
        	assertEquals("No matching setter found for class=io.atlasmap.java.inspect.JavaGetterSetterModel method=setOverloadParam paramType=java.lang.Short", e.getMessage());
        }
    }
    
    @Test
    public void testDetectSetterOverloadedNoGetter() throws Exception {
        try {
            Method setter = ClassHelper.detectSetterMethod(JavaGetterSetterModel.class, "setOverloadParamNoGetter", null);
            fail("NoSuchMethodException expected instead found=" + setter.getName());
        } catch (NoSuchMethodException e) {
            assertTrue(e.getMessage().startsWith(String.format("Unable to auto-detect setter class=%s method=%s", JavaGetterSetterModel.class.getName(), "setOverloadParamNoGetter")));
        }
    }
    
    @Test
    public void testDetectSetterOverloadedNoMatch() throws Exception {
        try {
            Method setter = ClassHelper.detectSetterMethod(JavaGetterSetterModel.class, "setOverloadParamNoMatch", null);
            fail("NoSuchMethodException expected instead found=" + setter.getName());
        } catch (NoSuchMethodException e) {
            assertTrue(e.getMessage().startsWith(String.format("No matching setter found for class=%s method=%s", JavaGetterSetterModel.class.getName(), "setOverloadParamNoMatch")));
        }
    }
    
    @Test
    public void testParentObjectForPathParamChecking() throws Exception {
        assertNull(ClassHelper.parentObjectForPath(null, null));
        assertNull(ClassHelper.parentObjectForPath(null, new PathUtil("foo.bar")));
        
        SourceContact targetObject = new SourceContact();
        Object parentObject = ClassHelper.parentObjectForPath(targetObject, null);
        assertNotNull(parentObject);
        assertTrue(parentObject instanceof SourceContact);
        assertEquals(targetObject, parentObject);
    }
    
    @Test
    public void testParentObjectForPath() throws Exception {
        
        SourceAddress sourceAddress = new SourceAddress();
        SourceOrder sourceOrder = new SourceOrder();
        sourceOrder.setAddress(sourceAddress);
        
        Object parentObject = ClassHelper.parentObjectForPath(sourceOrder, new PathUtil("/address/city"));
        assertNotNull(parentObject);
        assertTrue(parentObject instanceof SourceAddress);
        assertEquals(sourceAddress, parentObject);
    }
    
    @Test
    public void testParentObjectForPathGrandParent() throws Exception {
        
        SourceAddress sourceAddress = new SourceAddress();
        SourceOrder sourceOrder = new SourceOrder();
        sourceOrder.setAddress(sourceAddress);

        SourceParentOrder sourceParentOrder = new SourceParentOrder();
        sourceParentOrder.setOrder(sourceOrder);
        
        Object parentObject = ClassHelper.parentObjectForPath(sourceParentOrder, new PathUtil("/order/address/city"));
        assertNotNull(parentObject);
        assertTrue(parentObject instanceof SourceAddress);
        assertEquals(sourceAddress, parentObject);
    }

    @Test
    public void testParentObjectForPathList() throws Exception {
        
        SourceOrderList sourceOrderList = new SourceOrderList();
        List<BaseOrder> sourceOrders = new ArrayList<BaseOrder>();
        sourceOrderList.setOrders(sourceOrders);
        SourceAddress sourceAddress = new SourceAddress();
        SourceOrder sourceOrder = new SourceOrder();
        sourceOrder.setAddress(sourceAddress);

        sourceOrderList.getOrders().add(sourceOrder);
        
        Object parentObject = ClassHelper.parentObjectForPath(sourceOrderList, new PathUtil("orders<>"));
        assertNotNull(parentObject);
        assertTrue(parentObject instanceof List<?>);
        assertEquals(sourceOrders, parentObject);
    }
}
