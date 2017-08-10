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

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import io.atlasmap.v2.Validation;
import io.atlasmap.v2.ValidationStatus;
import io.atlasmap.validators.AtlasValidationTestHelper;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.*;

public class AtlasValidationTestHelperTest {

    private AtlasValidationTestHelper validations = null;
    private Validation error = null;
    private Validation warning = null;
    private Validation info = null;

    @Before
    public void setUp() {
        validations = new AtlasValidationTestHelper();
        error = new Validation();
        error.setField("test.field");
        error.setMessage("Error message");
        error.setStatus(ValidationStatus.ERROR);
        validations.addValidation(error);

        warning = new Validation();
        warning.setField("test.field.one");
        warning.setMessage("Warning message");
        warning.setStatus(ValidationStatus.WARN);
        warning.setValue("");
        validations.addValidation(warning);

        info = new Validation();
        info.setField("test.field.two");
        info.setMessage("Information message");
        info.setStatus(ValidationStatus.INFO);
        info.setValue("qwerty");
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
    public void testGetField() throws Exception {
        assertTrue("test.field".equals(error.getField()));
        assertTrue("test.field.one".equals(warning.getField()));
        assertTrue("test.field.two".equals(info.getField()));
    }

    @Test
    public void testGetRejectedValue() throws Exception {
        assertNull(error.getValue());
        assertTrue(((String) warning.getValue()).isEmpty());
        assertTrue(info.getValue().equals("qwerty"));
    }

    @Test
    public void testGetDefaultMessage() throws Exception {
        assertTrue(error.getMessage().equals("Error message"));
        assertTrue(warning.getMessage().equals("Warning message"));
        assertTrue(info.getMessage().equals("Information message"));
    }

    @Test
    public void testGetLevel() throws Exception {
        assertTrue(error.getStatus().compareTo(ValidationStatus.ERROR) == 0);
        assertTrue(warning.getStatus().compareTo(ValidationStatus.WARN) == 0);
        assertTrue(info.getStatus().compareTo(ValidationStatus.INFO) == 0);
    }

    @Test
    public void testToString() throws Exception {
        assertThat(error.getField(), is("test.field"));
        assertThat(error.getValue(), nullValue());
        assertThat(error.getMessage(), is("Error message"));
        assertThat(error.getStatus(), is(ValidationStatus.ERROR));

        assertThat(warning.getField(), is("test.field.one"));
        assertThat(warning.getValue(), CoreMatchers.is(""));
        assertThat(warning.getMessage(), is("Warning message"));
        assertThat(warning.getStatus(), is(ValidationStatus.WARN));

        assertThat(info.getField(), is("test.field.two"));
        assertThat(info.getValue(), CoreMatchers.is("qwerty"));
        assertThat(info.getMessage(), is("Information message"));
        assertThat(info.getStatus(), is(ValidationStatus.INFO));
    }

    @Test
    public void testEquals() throws Exception {
        assertFalse(error.equals(info));
    }

    @Test
    @Ignore // Hashcode not consistent across instances
    public void testHashCode() throws Exception {
        assertEquals(1000142829, error.hashCode());
        assertEquals(warning.hashCode(), -187767976);
        assertEquals(info.hashCode(), -1746235594);
    }

}