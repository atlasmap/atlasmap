/**
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
package io.atlasmap.validators;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import io.atlasmap.v2.Validation;
import io.atlasmap.v2.ValidationScope;
import io.atlasmap.v2.ValidationStatus;

public class AtlasValidationTestHelperTest {

    private AtlasValidationTestHelper validations = null;
    private Validation error = null;
    private Validation warning = null;
    private Validation info = null;

    @Before
    public void setUp() {
        validations = new AtlasValidationTestHelper();
        error = new Validation();
        error.setScope(ValidationScope.ALL);
        error.setMessage("Error message");
        error.setStatus(ValidationStatus.ERROR);
        validations.addValidation(error);

        warning = new Validation();
        warning.setScope(ValidationScope.DATA_SOURCE);
        warning.setId("atlas:testDataSource");
        warning.setMessage("Warning message");
        warning.setStatus(ValidationStatus.WARN);
        validations.addValidation(warning);

        info = new Validation();
        info.setScope(ValidationScope.MAPPING);
        info.setId("0001");
        info.setMessage("Information message");
        info.setStatus(ValidationStatus.INFO);
        validations.addValidation(info);
    }

    @After
    public void tearDown() {
        validations = null;
        error = null;
        warning = null;
        info = null;
    }

    @Test
    public void testGetScope() {
        assertEquals(ValidationScope.ALL, error.getScope());
        assertEquals(ValidationScope.DATA_SOURCE, warning.getScope());
        assertEquals(ValidationScope.MAPPING, info.getScope());
    }

    @Test
    public void testGetRejectedValue() {
        assertNull(error.getId());
        assertEquals("atlas:testDataSource", warning.getId());
        assertEquals("0001", info.getId());
    }

    @Test
    public void testGetDefaultMessage() {
        assertTrue(error.getMessage().equals("Error message"));
        assertTrue(warning.getMessage().equals("Warning message"));
        assertTrue(info.getMessage().equals("Information message"));
    }

    @Test
    public void testGetLevel() {
        assertTrue(error.getStatus().compareTo(ValidationStatus.ERROR) == 0);
        assertTrue(warning.getStatus().compareTo(ValidationStatus.WARN) == 0);
        assertTrue(info.getStatus().compareTo(ValidationStatus.INFO) == 0);
    }

    @Test
    public void testToString() {
        assertThat(error.getScope(), is(ValidationScope.ALL));
        assertThat(error.getId(), nullValue());
        assertThat(error.getMessage(), is("Error message"));
        assertThat(error.getStatus(), is(ValidationStatus.ERROR));

        assertThat(warning.getScope(), is(ValidationScope.DATA_SOURCE));
        assertThat(warning.getId(), is("atlas:testDataSource"));
        assertThat(warning.getMessage(), is("Warning message"));
        assertThat(warning.getStatus(), is(ValidationStatus.WARN));

        assertThat(info.getScope(), is(ValidationScope.MAPPING));
        assertThat(info.getId(), is("0001"));
        assertThat(info.getMessage(), is("Information message"));
        assertThat(info.getStatus(), is(ValidationStatus.INFO));
    }

    @Test
    public void testEquals() {
        assertFalse(error.equals(info));
    }

    @Test
    @Ignore // Hashcode not consistent across instances
    public void testHashCode() {
        assertEquals(1000142829, error.hashCode());
        assertEquals(warning.hashCode(), -187767976);
        assertEquals(info.hashCode(), -1746235594);
    }

}
