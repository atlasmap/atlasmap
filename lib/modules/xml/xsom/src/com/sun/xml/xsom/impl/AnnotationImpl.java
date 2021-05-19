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

import org.xml.sax.Locator;
import org.xml.sax.helpers.LocatorImpl;

import com.sun.xml.xsom.XSAnnotation;

public class AnnotationImpl implements XSAnnotation
{
    private Object annotation;
    public Object getAnnotation() { return annotation; }

    public Object setAnnotation(Object o) {
        Object r = this.annotation;
        this.annotation = o;
        return r;
    }

    private final Locator locator;
    public Locator getLocator() { return locator; }

    public AnnotationImpl( Object o, Locator _loc ) {
        this.annotation = o;
        this.locator = _loc;
    }

    public AnnotationImpl() {
        locator = NULL_LOCATION;
    }

    private static class LocatorImplUnmodifiable extends LocatorImpl {

        @Override
        public void setColumnNumber(int columnNumber) {
            return;
        }

        @Override
        public void setPublicId(String publicId) {
            return;
        }

        @Override
        public void setSystemId(String systemId) {
            return;
        }

        @Override
        public void setLineNumber(int lineNumber) {
            return;
        }
    };
    
    private static final LocatorImplUnmodifiable NULL_LOCATION = new LocatorImplUnmodifiable();
}
