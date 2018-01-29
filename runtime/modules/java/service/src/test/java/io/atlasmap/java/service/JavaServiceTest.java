package io.atlasmap.java.service;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.core.Response;

import io.atlasmap.v2.Json;
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
        assertEquals(byte[].class, entity.getClass());
        JavaClass javaClass = Json.mapper().readValue((byte[]) entity, JavaClass.class);
        assertEquals(JavaService.class.getName(), javaClass.getClassName());
    }
}
