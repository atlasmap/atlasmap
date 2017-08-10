package io.atlasmap.java.inspect;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.atlasmap.core.DefaultAtlasConversionService;
import io.atlasmap.java.test.TestListOrders;
import io.atlasmap.java.v2.AtlasJavaModelFactory;
import io.atlasmap.java.v2.JavaClass;
import io.atlasmap.java.v2.JavaField;
import io.atlasmap.v2.CollectionType;

public class TestListOrdersTest {

    private ClassInspectionService classInspectionService = null;

    @Before
    public void setUp() {
        classInspectionService = new ClassInspectionService();
        classInspectionService.setConversionService(DefaultAtlasConversionService.getInstance());
    }

    @After
    public void tearDown() {
        classInspectionService = null;
    }

    @Test
    public void testInspectJavaList() {
        JavaClass c = classInspectionService.inspectClass(TestListOrders.class);
        assertNotNull(c);
        assertNull(c.getAnnotations());
        assertNull(c.getArrayDimensions());
        assertEquals("io.atlasmap.java.test.TestListOrders", c.getClassName());
        assertNull(c.getCollectionClassName());
        assertNull(c.getCollectionType());
        assertNull(c.getGetMethod());
        assertNotNull(c.getJavaEnumFields());
        assertNotNull(c.getJavaEnumFields().getJavaEnumField());
        assertEquals(Integer.valueOf(0), Integer.valueOf(c.getJavaEnumFields().getJavaEnumField().size()));
        assertNotNull(c.getJavaFields());
        assertNotNull(c.getJavaFields().getJavaField());
        assertNull(c.getName());
        assertEquals("io.atlasmap.java.test", c.getPackageName());
        assertNull(c.getSetMethod());
        assertNull(c.getFieldType());
        assertNotNull(c.getUri());
        assertEquals(String.format(AtlasJavaModelFactory.URI_FORMAT, "io.atlasmap.java.test.TestListOrders"),
                c.getUri());
        assertNull(c.getValue());
        assertEquals(Integer.valueOf(2), new Integer(c.getJavaFields().getJavaField().size()));

        JavaField f1 = c.getJavaFields().getJavaField().get(0);
        assertNotNull(f1);
        assertTrue(f1 instanceof JavaClass);
        JavaClass c2 = (JavaClass) f1;

        assertNotNull(c2.getCollectionClassName());
        assertEquals("java.util.List", c2.getCollectionClassName());
        assertEquals(CollectionType.LIST, c2.getCollectionType());

        boolean foundAddress = false;
        boolean foundContact = false;

        for (JavaField c2f : c2.getJavaFields().getJavaField()) {
            if (c2f instanceof JavaClass) {
                if ("io.atlasmap.java.test.BaseAddress".equals(((JavaClass) c2f).getClassName())) {
                    ClassValidationUtil.validateSimpleTestAddress(((JavaClass) c2f));
                    foundAddress = true;
                } else if ("io.atlasmap.java.test.BaseContact".equals(((JavaClass) c2f).getClassName())) {
                    ClassValidationUtil.validateSimpleTestContact(((JavaClass) c2f));
                    foundContact = true;
                } else {
                    fail("Unexpected class: " + ((JavaClass) c2f).getClassName());
                }
            }
        }

        assertTrue(foundAddress);
        assertTrue(foundContact);
    }

}
