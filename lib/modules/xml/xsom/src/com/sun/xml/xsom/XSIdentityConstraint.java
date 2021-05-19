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

import java.util.List;

/**
 * Identity constraint.
 *
 * @author Kohsuke Kawaguchi
 */
public interface XSIdentityConstraint extends XSComponent {

    /**
     * Gets the {@link XSElementDecl} that owns this identity constraint.
     *
     * @return
     *      never null.
     */
    XSElementDecl getParent();

    /**
     * Name of the identity constraint.
     *
     * A name uniquely identifies this {@link XSIdentityConstraint} within
     * the namespace.
     *
     * @return
     *      never null.
     */
    String getName();

    /**
     * Target namespace of the identity constraint.
     *
     * Just short for <code>getParent().getTargetNamespace()</code>.
     */
    String getTargetNamespace();

    /**
     * Returns the type of the identity constraint.
     *
     * @return
     *      either {@link #KEY},{@link #KEYREF}, or {@link #UNIQUE}.
     */
    short getCategory();

    final short KEY = 0;
    final short KEYREF = 1;
    final short UNIQUE = 2;

    /**
     * Returns the selector XPath expression as string.
     *
     * @return
     *      never null.
     */
    XSXPath getSelector();

    /**
     * Returns the list of field XPaths.
     *
     * @return
     *      a non-empty read-only list of {@link String}s,
     *      each representing the XPath.
     */
    List<XSXPath> getFields();

    /**
     * If this is {@link #KEYREF}, returns the key {@link XSIdentityConstraint}
     * being referenced.
     *
     * @return
     *      always non-null (when {@link #getCategory()}=={@link #KEYREF}).
     * @throws IllegalStateException
     *      if {@link #getCategory()}!={@link #KEYREF}
     */
    XSIdentityConstraint getReferencedKey();
}
