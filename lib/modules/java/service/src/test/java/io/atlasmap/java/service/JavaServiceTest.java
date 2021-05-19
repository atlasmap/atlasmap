/*
 * Copyright (C) 2017 Red Hat, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
