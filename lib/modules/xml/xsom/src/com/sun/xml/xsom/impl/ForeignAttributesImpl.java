/*
 * Copyright (C) 2017 Oracle
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
package com.sun.xml.xsom.impl;

import org.relaxng.datatype.ValidationContext;
import org.xml.sax.Locator;
import org.xml.sax.helpers.AttributesImpl;

import com.sun.xml.xsom.ForeignAttributes;

/**
 * Remembers foreign attributes.
 *
 * @author Kohsuke Kawaguchi
 */
public final class ForeignAttributesImpl extends AttributesImpl implements ForeignAttributes {
    private final ValidationContext context;
    private final Locator locator;
    /**
     * {@link ForeignAttributes} forms a linked list.
     */
    final ForeignAttributesImpl next;

    public ForeignAttributesImpl(ValidationContext context, Locator locator, ForeignAttributesImpl next) {
        this.context = context;
        this.locator = locator;
        this.next = next;
    }

    public ValidationContext getContext() {
        return context;
    }

    public Locator getLocator() {
        return locator;
    }
}
