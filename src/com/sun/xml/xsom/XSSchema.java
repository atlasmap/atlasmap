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

import com.sun.xml.xsom.parser.SchemaDocument;

import java.util.Iterator;
import java.util.Map;

/**
 * Schema.
 * 
 * Container of declarations that belong to the same target namespace.
 * 
 * @author
 *  Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface XSSchema extends XSComponent
{
    /**
     * Gets the target namespace of the schema.
     *
     * @return
     *      can be empty, but never be null.
     */
    String getTargetNamespace();

    /**
     * Gets all the {@link XSAttributeDecl}s in this schema
     * keyed by their local names.
     */
    Map<String,XSAttributeDecl> getAttributeDecls();
    Iterator<XSAttributeDecl> iterateAttributeDecls();
    XSAttributeDecl getAttributeDecl(String localName);

    /**
     * Gets all the {@link XSElementDecl}s in this schema.
     */
    Map<String,XSElementDecl> getElementDecls();
    Iterator<XSElementDecl> iterateElementDecls();
    XSElementDecl getElementDecl(String localName);

    /**
     * Gets all the {@link XSAttGroupDecl}s in this schema.
     */
    Map<String,XSAttGroupDecl> getAttGroupDecls();
    Iterator<XSAttGroupDecl> iterateAttGroupDecls();
    XSAttGroupDecl getAttGroupDecl(String localName);

    /**
     * Gets all the {@link XSModelGroupDecl}s in this schema.
     */
    Map<String,XSModelGroupDecl> getModelGroupDecls();
    Iterator<XSModelGroupDecl> iterateModelGroupDecls();
    XSModelGroupDecl getModelGroupDecl(String localName);

    /**
     * Gets all the {@link XSType}s in this schema (union of
     * {@link #getSimpleTypes()} and {@link #getComplexTypes()}
     */
    Map<String,XSType> getTypes();
    Iterator<XSType> iterateTypes();
    XSType getType(String localName);

    /**
     * Gets all the {@link XSSimpleType}s in this schema.
     */
    Map<String,XSSimpleType> getSimpleTypes();
    Iterator<XSSimpleType> iterateSimpleTypes();
    XSSimpleType getSimpleType(String localName);

    /**
     * Gets all the {@link XSComplexType}s in this schema.
     */
    Map<String,XSComplexType> getComplexTypes();
    Iterator<XSComplexType> iterateComplexTypes();
    XSComplexType getComplexType(String localName);

    /**
     * Gets all the {@link XSNotation}s in this schema.
     */
    Map<String,XSNotation> getNotations();
    Iterator<XSNotation> iterateNotations();
    XSNotation getNotation(String localName);

    /**
     * Gets all the {@link XSIdentityConstraint}s in this schema,
     * keyed by their names.
     */
    Map<String,XSIdentityConstraint> getIdentityConstraints();

    /**
     * Gets the identity constraint of the given name, or null if not found.
     */
    XSIdentityConstraint getIdentityConstraint(String localName);

    /**
     * Sine an {@link XSSchema} is not necessarily defined in
     * one schema document (for example one schema can span across
     * many documents through &lt;xs:include>s.),
     * so this method always returns null.
     *
     * @deprecated
     *      Since this method always returns null, if you are calling
     *      this method from {@link XSSchema} and not from {@link XSComponent},
     *      there's something wrong with your code.
     */
    SchemaDocument getSourceDocument();

    /**
     * Gets the root schema set that includes this schema.
     *
     * @return never null.
     */
    XSSchemaSet getRoot();
}
