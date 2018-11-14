package io.atlasmap.java.service;

import static org.junit.Assert.assertEquals;

import javax.ws.rs.core.Response;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.atlasmap.java.v2.JavaClass;
import io.atlasmap.v2.Json;

public class JavaServiceTest {

    private JavaService javaService = null;

    @Before
    public void setUp() {
        javaService = new JavaService();
    }

    @After
    public void tearDown() {
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
