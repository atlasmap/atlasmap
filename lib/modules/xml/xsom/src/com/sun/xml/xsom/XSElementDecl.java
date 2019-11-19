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

import java.util.List;
import java.util.Set;

/**
 * Element declaration.
 * 
 * @author
 *  Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface XSElementDecl extends XSDeclaration, XSTerm
{
    /**
     * Gets the type of this element declaration.
     * @return
     *      always non-null.
     */
    XSType getType();

    boolean isNillable();

    /**
     * Gets the substitution head of this element, if any.
     * Otherwise null.
     */
    XSElementDecl getSubstAffiliation();

    /**
     * Returns all the {@link XSIdentityConstraint}s in this element decl.
     *
     * @return
     *      never null, but can be empty.
     */
    List<XSIdentityConstraint> getIdentityConstraints();

    /**
     * Checks the substitution excluded property of the schema component.
     * 
     * IOW, this checks the value of the <code>final</code> attribute
     * (plus <code>finalDefault</code>).
     * 
     * @param method
     *      Possible values are {@link XSType#EXTENSION} or
     *      <code>XSType.RESTRICTION</code>.
     */
    boolean isSubstitutionExcluded(int method);

    /**
     * Checks the diallowed substitution property of the schema component.
     * 
     * IOW, this checks the value of the <code>block</code> attribute
     * (plus <code>blockDefault</code>).
     * 
     * @param method
     *      Possible values are {@link XSType#EXTENSION},
     *      <code>XSType.RESTRICTION</code>, or <code>XSType.SUBSTITUTION</code>
     */
    boolean isSubstitutionDisallowed(int method);

    boolean isAbstract();

    /**
     * Returns the element declarations that can substitute
     * this element.
     * 
     * <p>
     * IOW, this set returns all the element decls that satisfies
     * <a href="http://www.w3.org/TR/xmlschema-1/#cos-equiv-derived-ok-rec">
     * the "Substitution Group OK" constraint.
     * </a>
     * 
     * @return
     *      nun-null valid array. The return value always contains this element
     *      decl itself. 
     * 
     * @deprecated
     *      this method allocates a new array every time, so it could be
     *      inefficient when working with a large schema. Use
     *      {@link #getSubstitutables()} instead.
     */
    XSElementDecl[] listSubstitutables();
    
    /**
     * Returns the element declarations that can substitute
     * this element.
     * 
     * <p>
     * IOW, this set returns all the element decls that satisfies
     * <a href="http://www.w3.org/TR/xmlschema-1/#cos-equiv-derived-ok-rec">
     * the "Substitution Group OK" constraint.
     * </a>
     * 
     * <p>
     * Note that the above clause does <em>NOT</em> check for
     * abstract elements. So abstract elements may still show up
     * in the returned set.
     * 
     * @return
     *      nun-null unmodifiable list.
     *      The returned list always contains this element decl itself. 
     */
    Set<? extends XSElementDecl> getSubstitutables();
    
    /**
     * Returns true if this element declaration can be validly substituted
     * by the given declaration.
     * 
     * <p>
     * Just a short cut of <tt>getSubstitutables().contain(e);</tt>
     */
    boolean canBeSubstitutedBy(XSElementDecl e);

    // TODO: identitiy constraints
    // TODO: scope

    XmlString getDefaultValue();
    XmlString getFixedValue();

    /**
     * Used for javadoc schema generation
     *
     * @return
     *    null if form attribute not present,
     *    true if form attribute present and set to qualified,
     *    false if form attribute present and set to unqualified.
     */

    Boolean getForm();
}
