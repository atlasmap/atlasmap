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

package com.sun.xml.xsom.util;

import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSNotation;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.XSIdentityConstraint;
import com.sun.xml.xsom.XSXPath;
import com.sun.xml.xsom.visitor.XSFunction;

/**
 * Utility implementation of {@link XSFunction} that returns
 * {@link Boolean} to find something from schema objects.
 * 
 * <p>
 * This implementation returns <code>Boolean.FALSE</code> from
 * all of the methods. The derived class is expected to override
 * some of the methods to actually look for something.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class XSFinder implements XSFunction<Boolean> {
    
    /**
     * Invokes this object as a visitor with the specified component.
     */
    public final boolean find( XSComponent c ) {
        return c.apply(this);
    }
    
    /**
     * @see com.sun.xml.xsom.visitor.XSFunction#annotation(com.sun.xml.xsom.XSAnnotation)
     */
    public Boolean annotation(XSAnnotation ann) {
        return Boolean.FALSE;
    }

    /**
     * @see com.sun.xml.xsom.visitor.XSFunction#attGroupDecl(com.sun.xml.xsom.XSAttGroupDecl)
     */
    public Boolean attGroupDecl(XSAttGroupDecl decl) {
        return Boolean.FALSE;
    }

    /**
     * @see com.sun.xml.xsom.visitor.XSFunction#attributeDecl(com.sun.xml.xsom.XSAttributeDecl)
     */
    public Boolean attributeDecl(XSAttributeDecl decl) {
        return Boolean.FALSE;
    }

    /**
     * @see com.sun.xml.xsom.visitor.XSFunction#attributeUse(com.sun.xml.xsom.XSAttributeUse)
     */
    public Boolean attributeUse(XSAttributeUse use) {
        return Boolean.FALSE;
    }

    /**
     * @see com.sun.xml.xsom.visitor.XSFunction#complexType(com.sun.xml.xsom.XSComplexType)
     */
    public Boolean complexType(XSComplexType type) {
        return Boolean.FALSE;
    }

    /**
     * @see com.sun.xml.xsom.visitor.XSFunction#schema(com.sun.xml.xsom.XSSchema)
     */
    public Boolean schema(XSSchema schema) {
        return Boolean.FALSE;
    }

    /**
     * @see com.sun.xml.xsom.visitor.XSFunction#facet(com.sun.xml.xsom.XSFacet)
     */
    public Boolean facet(XSFacet facet) {
        return Boolean.FALSE;
    }

    /**
     * @see com.sun.xml.xsom.visitor.XSFunction#notation(com.sun.xml.xsom.XSNotation)
     */
    public Boolean notation(XSNotation notation) {
        return Boolean.FALSE;
    }

    /**
     * @see com.sun.xml.xsom.visitor.XSContentTypeFunction#simpleType(com.sun.xml.xsom.XSSimpleType)
     */
    public Boolean simpleType(XSSimpleType simpleType) {
        return Boolean.FALSE;
    }

    /**
     * @see com.sun.xml.xsom.visitor.XSContentTypeFunction#particle(com.sun.xml.xsom.XSParticle)
     */
    public Boolean particle(XSParticle particle) {
        return Boolean.FALSE;
    }

    /**
     * @see com.sun.xml.xsom.visitor.XSContentTypeFunction#empty(com.sun.xml.xsom.XSContentType)
     */
    public Boolean empty(XSContentType empty) {
        return Boolean.FALSE;
    }

    /**
     * @see com.sun.xml.xsom.visitor.XSTermFunction#wildcard(com.sun.xml.xsom.XSWildcard)
     */
    public Boolean wildcard(XSWildcard wc) {
        return Boolean.FALSE;
    }

    /**
     * @see com.sun.xml.xsom.visitor.XSTermFunction#modelGroupDecl(com.sun.xml.xsom.XSModelGroupDecl)
     */
    public Boolean modelGroupDecl(XSModelGroupDecl decl) {
        return Boolean.FALSE;
    }

    /**
     * @see com.sun.xml.xsom.visitor.XSTermFunction#modelGroup(com.sun.xml.xsom.XSModelGroup)
     */
    public Boolean modelGroup(XSModelGroup group) {
        return Boolean.FALSE;
    }

    /**
     * @see com.sun.xml.xsom.visitor.XSTermFunction#elementDecl(com.sun.xml.xsom.XSElementDecl)
     */
    public Boolean elementDecl(XSElementDecl decl) {
        return Boolean.FALSE;
    }

    public Boolean identityConstraint(XSIdentityConstraint decl) {
        return Boolean.FALSE;
    }

    public Boolean xpath(XSXPath xpath) {
        return Boolean.FALSE;
    }
}
