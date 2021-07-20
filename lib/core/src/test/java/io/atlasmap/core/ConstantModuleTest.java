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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import io.atlasmap.api.AtlasException;
import io.atlasmap.spi.AtlasInternalSession;
import io.atlasmap.spi.AtlasInternalSession.Head;
import io.atlasmap.v2.ConstantField;
import io.atlasmap.v2.FieldType;
import io.atlasmap.v2.MockField;

public class ConstantModuleTest {

    private static ConstantModule module = new ConstantModule();

    @Test
    public void testDestroy() {
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
        ConstantField field = mock(ConstantField.class);
        when(field.getValue()).thenReturn("fieldValue");

        Head head = mock(Head.class);
        when(head.getSourceField()).thenReturn(field);

        AtlasInternalSession session = mock(AtlasInternalSession.class);
        when(session.head()).thenReturn(head);

        DefaultAtlasConversionService atlasConversionService = mock(DefaultAtlasConversionService.class);
        when(atlasConversionService.fieldTypeFromClass(any(String.class))).thenReturn(FieldType.ANY);

        module.setConversionService(atlasConversionService);
        module.readSourceValue(session);
    }

    @Test
    public void testProcessPostSourceExecution() throws AtlasException {
        module.processPostSourceExecution(null);
    }

    @Test
    public void testProcessPreTargetExecution() throws AtlasException {
        assertThrows(UnsupportedOperationException.class, () -> {
            module.processPreTargetExecution(null);
        });
    }

    @Test
    public void testProcessTargetFieldMapping() throws AtlasException {
        assertThrows(UnsupportedOperationException.class, () -> {
            module.writeTargetValue(null);
        });
    }

    @Test
    public void testProcessPostTargetExecution() throws AtlasException {
        assertThrows(UnsupportedOperationException.class, () -> {
            module.processPostTargetExecution(null);
        });
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

}
