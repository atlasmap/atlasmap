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

import javax.xml.namespace.NamespaceContext;
import java.util.Iterator;
import java.util.Collection;

/**
 * Set of {@link XSSchema} objects.
 * 
 * @author
 *  Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface XSSchemaSet
{
    XSSchema getSchema(String targetNamespace);
    XSSchema getSchema(int idx);
    int getSchemaSize();
    Iterator<XSSchema> iterateSchema();

    /**
     * Gets all {@link XSSchema}s in a single collection.
     */
    Collection<XSSchema> getSchemas();

    XSType getType(String namespaceURI, String localName);
    XSSimpleType getSimpleType(String namespaceURI, String localName);
    XSAttributeDecl getAttributeDecl(String namespaceURI, String localName);
    XSElementDecl getElementDecl(String namespaceURI, String localName);
    XSModelGroupDecl getModelGroupDecl(String namespaceURI, String localName);
    XSAttGroupDecl getAttGroupDecl(String namespaceURI, String localName);
    XSComplexType getComplexType(String namespaceURI, String localName);
    XSIdentityConstraint getIdentityConstraint(String namespaceURI, String localName);

    /** Iterates all element declarations in all the schemas. */
    Iterator<XSElementDecl> iterateElementDecls();
    /** Iterates all type definitions in all the schemas. */
    Iterator<XSType> iterateTypes();
    /** Iterates all atribute declarations in all the schemas. */
    Iterator<XSAttributeDecl> iterateAttributeDecls();
    /** Iterates all attribute group declarations in all the schemas. */
    Iterator<XSAttGroupDecl> iterateAttGroupDecls();
    /** Iterates all model group declarations in all the schemas. */
    Iterator<XSModelGroupDecl> iterateModelGroupDecls();
    /** Iterates all simple type definitions in all the schemas. */
    Iterator<XSSimpleType> iterateSimpleTypes();
    /** Iterates all complex type definitions in all the schemas. */
    Iterator<XSComplexType> iterateComplexTypes();
    /** Iterates all notation declarations in all the schemas. */
    Iterator<XSNotation> iterateNotations();
    /**
     * Iterates all identity constraints in all the schemas.
     */
    Iterator<XSIdentityConstraint> iterateIdentityConstraints();

    // conceptually static methods
    XSComplexType getAnyType();
    XSSimpleType getAnySimpleType();
    XSContentType getEmpty();

    /**
     * Evaluates a schema component designator against this schema component
     * and returns the resulting schema components.
     *
     * @throws IllegalArgumentException
     *      if SCD is syntactically incorrect.
     * @param scd
     *      Schema component designator. See {@link SCD} for more details.
     * @param nsContext
     *      The namespace context in which SCD is evaluated. Cannot be null.
     * @return
     *      Can be empty but never null.
     */
    Collection<XSComponent> select(String scd, NamespaceContext nsContext);

    /**
     * Evaluates a schema component designator against this schema component
     * and returns the first resulting schema component.
     *
     * @throws IllegalArgumentException
     *      if SCD is syntactically incorrect.
     * @param scd
     *      Schema component designator. See {@link SCD} for more details.
     * @param nsContext
     *      The namespace context in which SCD is evaluated. Cannot be null.
     * @return
     *      null if the SCD didn't match anything. If the SCD matched more than one node,
     *      the first one will be returned.
     */
    XSComponent selectSingle(String scd, NamespaceContext nsContext);
}
