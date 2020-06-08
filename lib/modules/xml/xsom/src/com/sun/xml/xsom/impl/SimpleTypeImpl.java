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

package com.sun.xml.xsom.impl;

import java.util.Set;

import org.xml.sax.Locator;

import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSListSimpleType;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSRestrictionSimpleType;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSType;
import com.sun.xml.xsom.XSUnionSimpleType;
import com.sun.xml.xsom.XSVariety;
import com.sun.xml.xsom.impl.parser.SchemaDocumentImpl;
import com.sun.xml.xsom.visitor.XSContentTypeFunction;
import com.sun.xml.xsom.visitor.XSContentTypeVisitor;
import com.sun.xml.xsom.visitor.XSFunction;
import com.sun.xml.xsom.visitor.XSVisitor;

public abstract class SimpleTypeImpl extends DeclarationImpl
    implements XSSimpleType, ContentTypeImpl, Ref.SimpleType
{
    SimpleTypeImpl(
        SchemaDocumentImpl _parent,
        AnnotationImpl _annon,
        Locator _loc,
        ForeignAttributesImpl _fa,
        String _name,
        boolean _anonymous,
        Set<XSVariety> finalSet,
        Ref.SimpleType _baseType) {

        super(_parent, _annon, _loc, _fa, _parent.getTargetNamespace(), _name, _anonymous);

        this.baseType = _baseType;
        this.finalSet = finalSet;
    }

    private Ref.SimpleType baseType;

    public XSType[] listSubstitutables() {
        return Util.listSubstitutables(this);
    }

    public void redefine( SimpleTypeImpl st ) {
        baseType = st;
        st.redefinedBy = this;
        redefiningCount = (short)(st.redefiningCount+1);
    }

    /**
     * Number of times this component redefines other components.
     */
    private short redefiningCount = 0;

    private SimpleTypeImpl redefinedBy = null;

    public XSSimpleType getRedefinedBy() {
        return redefinedBy;
    }

    public int getRedefinedCount() {
        int i=0;
        for( SimpleTypeImpl st =this.redefinedBy; st !=null; st =st.redefinedBy)
            i++;
        return i;
    }

    public XSType getBaseType() { return baseType.getType(); }
    public XSSimpleType getSimpleBaseType() { return baseType.getType(); }
    public boolean isPrimitive() { return false; }

    public XSListSimpleType getBaseListType() {
        return getSimpleBaseType().getBaseListType();
    }

    public XSUnionSimpleType getBaseUnionType() {
        return getSimpleBaseType().getBaseUnionType();
    }

    private final Set<XSVariety> finalSet;

    public boolean isFinal(XSVariety v) {
        return finalSet.contains(v);
    }


    public final int getDerivationMethod() { return XSType.RESTRICTION; }


    public final XSSimpleType asSimpleType()  { return this; }
    public final XSComplexType asComplexType(){ return null; }

    public boolean isDerivedFrom(XSType t) {
        XSType x = this;
        while(true) {
            if(t==x)
                return true;
            XSType s = x.getBaseType();
            if(s==x)
                return false;
            x = s;
        }
    }

    public final boolean isSimpleType()       { return true; }
    public final boolean isComplexType()      { return false; }
    public final XSParticle asParticle()      { return null; }
    public final XSContentType asEmpty()      { return null; }


    public boolean isRestriction() { return false; }
    public boolean isList() { return false; }
    public boolean isUnion() { return false; }
    public XSRestrictionSimpleType asRestriction() { return null; }
    public XSListSimpleType asList() { return null; }
    public XSUnionSimpleType asUnion() { return null; }




    public final void visit( XSVisitor visitor ) {
        visitor.simpleType(this);
    }
    public final void visit( XSContentTypeVisitor visitor ) {
        visitor.simpleType(this);
    }
    public final Object apply( XSFunction function ) {
        return function.simpleType(this);
    }
    public final Object apply( XSContentTypeFunction function ) {
        return function.simpleType(this);
    }

    // Ref.ContentType implementation
    public XSContentType getContentType() { return this; }
    // Ref.SimpleType implementation
    public XSSimpleType getType() { return this; }
}
