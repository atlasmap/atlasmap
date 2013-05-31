/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2013 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * http://glassfish.java.net/public/CDDL+GPL_1_1.html
 * or packager/legal/LICENSE.txt.  See the License for the specific
 * language governing permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at packager/legal/LICENSE.txt.
 *
 * GPL Classpath Exception:
 * Oracle designates this particular file as subject to the "Classpath"
 * exception as provided by Oracle in the GPL Version 2 section of the License
 * file that accompanied this code.
 *
 * Modifications:
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyright [year] [name of copyright owner]"
 *
 * Contributor(s):
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.xml.xsom;

import java.util.Iterator;
import java.util.Collection;

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
