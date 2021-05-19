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
package com.sun.xml.xsom;

import org.relaxng.datatype.ValidationContext;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;

/**
 * Foreign attributes on schema elements.
 *
 * <p>
 * This is not a schema component as defined in the spec,
 * but this is often useful for a schema processing application.
 *
 * @author Kohsuke Kawaguchi
 */
public interface ForeignAttributes extends Attributes {
    /**
     * Returns context information of the element to which foreign attributes
     * are attached.
     *
     * <p>
     * For example, this can be used to resolve relative references to other resources
     * (by using {@link ValidationContext#getBaseUri()}) or to resolve
     * namespace prefixes in the attribute values (by using {@link ValidationContext#resolveNamespacePrefix(String)}.
     *
     * @return
     *      always non-null.
     */
    ValidationContext getContext();

    /**
     * Returns the location of the element to which foreign attributes
     * are attached.
     */
    Locator getLocator();
}
