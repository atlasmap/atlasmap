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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;

import javax.ws.rs.container.ResourceContext;
import javax.ws.rs.core.Response;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.atlasmap.api.AtlasSession;
import io.atlasmap.java.v2.ClassInspectionRequest;
import io.atlasmap.java.v2.ClassInspectionResponse;
import io.atlasmap.service.AtlasService;
import io.atlasmap.v2.Json;

@ExtendWith(MockitoExtension.class)
public class JavaServiceTest {

    @Mock
    private ResourceContext mockResourceContext;
    @InjectMocks
    private JavaService javaService;

    @Test
    public void testGetClass() throws Exception {
        when(mockResourceContext.getResource(AtlasService.class)).thenReturn(new AtlasService());
        ClassInspectionRequest request = new ClassInspectionRequest();
        request.setClassName(ClassInspectionRequest.class.getName());
        byte[] bytes = Json.mapper().writeValueAsBytes(request);
        Response res = javaService.inspectClass(new ByteArrayInputStream(bytes));
        Object entity = res.getEntity();
        assertEquals(byte[].class, entity.getClass());
        ClassInspectionResponse inspectionResponse = Json.mapper().readValue((byte[]) entity, ClassInspectionResponse.class);
        String error = inspectionResponse.getErrorMessage();
        assertTrue(error == null || error.isEmpty(), error);
        assertEquals(ClassInspectionRequest.class.getName(), inspectionResponse.getJavaClass().getClassName());
        request.setClassName(AtlasSession.class.getName());
        bytes = Json.mapper().writeValueAsBytes(request);
        res = javaService.inspectClass(new ByteArrayInputStream(bytes));
        entity = res.getEntity();
        assertEquals(byte[].class, entity.getClass());
        inspectionResponse = Json.mapper().readValue((byte[]) entity, ClassInspectionResponse.class);
        error = inspectionResponse.getErrorMessage();
        assertTrue(error == null || error.isEmpty(), error);
        assertEquals(AtlasSession.class.getName(), inspectionResponse.getJavaClass().getClassName());
        inspectionResponse.getJavaClass().getJavaFields().getJavaField()
                .stream().filter(f -> "properties".equals(f.getName()))
                .forEach(f -> assertEquals("/properties", f.getPath(), "Invalid path: " + f.getPath()));
                
    }
}
