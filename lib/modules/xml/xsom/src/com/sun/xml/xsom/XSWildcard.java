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

import java.util.Collection;
import java.util.Iterator;

import com.sun.xml.xsom.visitor.XSWildcardFunction;
import com.sun.xml.xsom.visitor.XSWildcardVisitor;

/**
 * Wildcard schema component (used for both attribute wildcard
 * and element wildcard.)
 * 
 * XSWildcard interface can always be downcasted to either
 * Any, Other, or Union.
 */
public interface XSWildcard extends XSComponent, XSTerm
{
    static final int LAX = 1;
    static final int STRTICT = 2;
    static final int SKIP = 3;
    /**
     * Gets the processing mode.
     * 
     * @return
     *      Either LAX, STRICT, or SKIP.
     */
    int getMode();

    /**
     * Returns true if the specified namespace URI is valid
     * wrt this wildcard.
     * 
     * @param namespaceURI
     *      Use the empty string to test the default no-namespace.
     */
    boolean acceptsNamespace(String namespaceURI);

    /** Visitor support. */
    void visit(XSWildcardVisitor visitor);
    <T> T apply(XSWildcardFunction<T> function);

    /**
     * <code>##any</code> wildcard.
     */
    interface Any extends XSWildcard {
    }
    /**
     * <code>##other</code> wildcard.
     */
    interface Other extends XSWildcard {
        /**
         * Gets the namespace URI excluded from this wildcard.
         */
        String getOtherNamespace();
    }
    /**
     * Wildcard of a set of namespace URIs.
     */
    interface Union extends XSWildcard {
        /**
         * Short for <code>getNamespaces().iterator()</code>
         */
        Iterator<String> iterateNamespaces();

        /**
         * Read-only list of namespace URIs.
         */
        Collection<String> getNamespaces();
    }
}
