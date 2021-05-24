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
     * Note that if this type represents {@code xs:anyType}, this method returns itself.
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
     * Note that {@code t.isDerivedFrom(t)} returns true.
     */
    boolean isDerivedFrom( XSType t );
}
