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
package io.atlasmap.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import io.atlasmap.api.AtlasException;
import io.atlasmap.spi.AtlasInternalSession;
import io.atlasmap.spi.AtlasInternalSession.Head;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.MockField;
import io.atlasmap.v2.PropertyField;

public class PropertyModuleTest {
    private static PropertyModule module = new PropertyModule(new DefaultAtlasPropertyStrategy());

    @Test
    public void testDestroy() throws Exception {
        module.destroy();
    }

    @Test
    public void testProcessPreValidation() throws AtlasException {
        module.processPreValidation(null);
    }

    @Test
    public void testProcessPreSourceExecution() throws AtlasException {
        module.processPreSourceExecution(null);
    }

    @Test
    public void testProcessSourceFieldMapping() throws AtlasException {
        PropertyField field = mock(PropertyField.class);
        when(field.getName()).thenReturn("testProp");
        Head head = mock(Head.class);
        when(head.getSourceField()).thenReturn(field);

        AtlasInternalSession session = mock(AtlasInternalSession.class);
        when(session.head()).thenReturn(head);
        Map<String, Object> sourceProps = new HashMap<>();
        sourceProps.put("testProp", "testValue");
        when(session.getSourceProperties()).thenReturn(sourceProps);

        DefaultAtlasConversionService atlasConversionService = mock(DefaultAtlasConversionService.class);
        when(atlasConversionService.fieldTypeFromClass(any(String.class))).thenReturn(FieldType.ANY);

        module.setConversionService(atlasConversionService);
        module.readSourceValue(session);

        ArgumentCaptor<Object> arg = ArgumentCaptor.forClass(Object.class);
        verify(field).setValue(arg.capture());
        assertEquals("testValue", arg.getValue());
    }

    @Test
    public void testProcessPostSourceExecution() throws AtlasException {
        module.processPostSourceExecution(null);
    }

    @Test
    public void testProcessPreTargetExecution() throws AtlasException {
        module.processPreTargetExecution(null);
    }

    @Test
    public void testProcessTargetFieldMapping() throws Exception {
        PropertyField field = mock(PropertyField.class);
        when(field.getName()).thenReturn("testProp");
        when(field.getValue()).thenReturn("testValue");
        Head head = mock(Head.class);
        when(head.getTargetField()).thenReturn(field);

        AtlasInternalSession session = mock(AtlasInternalSession.class);
        when(session.head()).thenReturn(head);
        Map<String, Object> targetProps = new HashMap<>();
        when(session.getTargetProperties()).thenReturn(targetProps);

        DefaultAtlasConversionService atlasConversionService = mock(DefaultAtlasConversionService.class);
        when(atlasConversionService.fieldTypeFromClass(any(String.class))).thenReturn(FieldType.ANY);

        module.setConversionService(atlasConversionService);
        module.writeTargetValue(session);

        assertEquals("testValue", targetProps.get("testProp"));
    }

    @Test
    public void testProcessPostTargetExecution() throws AtlasException {
        module.processPostTargetExecution(null);
    }

    @Test
    public void testProcessPostValidation() throws AtlasException {
        module.processPostValidation(null);
    }

    @Test
    public void testGetMode() {
        assertNotNull(module.getMode());
    }

    @Test
    public void testGetConversionService() {
        assertNotNull(module.getConversionService());
    }

    @Test
    public void testListSupportedModes() {
        assertNotNull(module.listSupportedModes());
    }

    @Test
    public void testGetDocId() {
        assertNull(module.getDocId());
    }

    @Test
    public void testGetUri() {
        assertNull(module.getUri());
    }

    @Test
    public void testIsStatisticsSupported() {
        assertFalse(module.isStatisticsSupported());
    }

    @Test
    public void testIsStatisticsEnabled() {
        assertFalse(module.isStatisticsEnabled());
    }

    @Test
    public void testIsSupportedField() {
        assertFalse(module.isSupportedField(new MockField()));
    }

    @Test
    public void testCloneField() throws AtlasException {
        assertNull(module.cloneField(new MockField()));
    }

    @Test
    public void testGetFieldActionService() {
        assertNull(module.getFieldActionService());
    }

    @Test
    public void testInit() throws Exception {
        module.init();
    }

    @Test
    public void testSetDocId() {
        module.setDocId(null);
    }

    @Test
    public void testSetUri() {
        module.setUri(null);
    }

}
