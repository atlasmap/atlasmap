package io.atlasmap.java.service;

import static org.junit.Assert.*;

import javax.ws.rs.core.Response;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.atlasmap.java.v2.JavaClass;

public class JavaServiceTest {

    private JavaService javaService = null;

    @Before
    public void setUp() throws Exception {
        javaService = new JavaService();
    }

    @After
    public void tearDown() throws Exception {
        javaService = null;
    }

    @Test
    public void testGetClass() throws Exception {
        Response res = javaService.getClass(JavaService.class.getName());
        Object entity = res.getEntity();
        assertEquals(JavaClass.class, entity.getClass());
        JavaClass javaClass = (JavaClass) entity;
        assertEquals(JavaService.class.getName(), javaClass.getClassName());
    }
}
