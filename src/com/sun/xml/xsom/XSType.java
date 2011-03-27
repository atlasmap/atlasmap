/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License.  You can
 * obtain a copy of the License at
 * https://glassfish.dev.java.net/public/CDDL+GPL_1_1.html
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

/**
 * Base interface for {@link XSComplexType} and {@link XSSimpleType}.
 * 
 * @author
 *  Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface XSType extends XSDeclaration {
    /**
     * Returns the base type of this type.
     *
     * Note that if this type represents <tt>xs:anyType</tt>, this method returns itself.
     * This is awkward as an API, but it follows the schema specification.
     *
     * @return  always non-null.
     */
    XSType getBaseType();

    final static int EXTENSION = 1;
    final static int RESTRICTION = 2;
    final static int SUBSTITUTION = 4;

    int getDerivationMethod();

    /** Returns true if <code>this instanceof XSSimpleType</code>. */
    boolean isSimpleType();
    /** Returns true if <code>this instanceof XSComplexType</code>. */
    boolean isComplexType();

    /**
     * Lists up types that can substitute this type by using xsi:type.
     * Includes this type itself.
     * <p>
     * This method honors the block flag.
     */
    XSType[] listSubstitutables();

    /**
     * If this {@link XSType} is redefined by another type,
     * return that component.
     *
     * @return null
     *      if this component has not been redefined.
     */
    XSType getRedefinedBy();

    /**
     * Returns the number of complex types that redefine this component.
     *
     * <p>
     * For example, if A is redefined by B and B is redefined by C,
     * A.getRedefinedCount()==2, B.getRedefinedCount()==1, and
     * C.getRedefinedCount()==0.
     */
    int getRedefinedCount();


    /** Casts this object to XSSimpleType if possible, otherwise returns null. */
    XSSimpleType asSimpleType();
    /** Casts this object to XSComplexType if possible, otherwise returns null. */
    XSComplexType asComplexType();

    /**
     * Returns true if this type is derived from the specified type.
     *
     * <p>
     * Note that <tt>t.isDerivedFrom(t)</tt> returns true.
     */
    boolean isDerivedFrom( XSType t );
}
