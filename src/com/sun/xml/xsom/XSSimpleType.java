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

import com.sun.xml.xsom.visitor.XSSimpleTypeFunction;
import com.sun.xml.xsom.visitor.XSSimpleTypeVisitor;

import java.util.List;

/**
 * Simple type.
 * 
 * @author
 *  Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface XSSimpleType extends XSType, XSContentType
{
    /**
     * Gets the base type as XSSimpleType.
     * 
     * Equivalent to
     * <code>
     * (XSSimpleType)getBaseType()
     * </code>
     * Since this is a simple type, we know that the base type
     * is also a simple type.
     *
     * The only exception is xs:anySimpleType, which has xs:anyType
     * as the base type.
     *
     * @return
     *      null if this is xs:anySimpleType. Otherwise non-null.
     */
    XSSimpleType getSimpleBaseType();
    
    /**
     * Gets the variety of this simple type.
     */
    XSVariety getVariety();

    /**
     * Gets the ancestor primitive {@link XSSimpleType} if
     * this type is {@link XSVariety#ATOMIC atomic}.
     *
     * @return
     *      null otherwise.
     */
    XSSimpleType getPrimitiveType();

    /**
     * Returns true if this is a primitive built-in simple type
     * (that directly derives from xs:anySimpleType, by definition.)
     */
    boolean isPrimitive();

    /**
     * Gets the nearest ancestor {@link XSListSimpleType} (including itself)
     * if the variety of this type is {@link XSVariety#LIST list}.
     *
     * @return otherwise return null
     */
    XSListSimpleType getBaseListType();

    /**
     * Gets the nearest ancestor {@link XSUnionSimpleType} (including itself)
     * if the variety of this type is {@link XSVariety#UNION union}.
     *
     * @return otherwise return null
     */
    XSUnionSimpleType getBaseUnionType();

    /**
     * Returns true if this type definition is marked as 'final'
     * with respect to the given {@link XSVariety}.
     *
     * @return
     *      true if the type is marked final.
     */
    boolean isFinal(XSVariety v);
    
    /**
     * If this {@link XSSimpleType} is redefined by another simple type,
     * return that component.
     *
     * @return null
     *      if this component has not been redefined.
     */
    public XSSimpleType getRedefinedBy();

    /**
     * Gets the effective facet object of the given name.
     * 
     * <p>
     * For example, if a simple type "foo" is derived from
     * xs:string by restriction with the "maxLength" facet and
     * another simple type "bar" is derived from "foo" by
     * restriction with another "maxLength" facet, this method
     * will return the latter one, because that is the most
     * restrictive, effective facet.
     * 
     * <p>
     * For those facets that can have multiple values
     * (pattern facets and enumeration facets), this method
     * will return only the first one.
     * TODO: allow clients to access all of them by some means.
     * 
     * @return
     *      If this datatype has a facet of the given name,
     *      return that object. If the facet is not specified
     *      anywhere in its derivation chain, null will be returned.
     */
    XSFacet getFacet( String name );

    /**
     * For multi-valued facets (enumeration and pattern), obtain all values.
     *
     * @see #getFacet(String)
     *
     * @return
     *      can be empty but never null.
     */
    List<XSFacet> getFacets( String name );

    
    
    void visit( XSSimpleTypeVisitor visitor );
    <T> T apply( XSSimpleTypeFunction<T> function );
    
    /** Returns true if <code>this instanceof XSRestrictionSimpleType</code>. */
    boolean isRestriction();
    /** Returns true if <code>this instanceof XSListSimpleType</code>. */
    boolean isList();
    /** Returns true if <code>this instanceof XSUnionSimpleType</code>. */
    boolean isUnion();
    
    XSRestrictionSimpleType asRestriction();
    XSListSimpleType asList();
    XSUnionSimpleType asUnion();
}
