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
package com.sun.xml.xsom.util;

import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSIdentityConstraint;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSNotation;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.XSXPath;
import com.sun.xml.xsom.visitor.XSFunction;

/**
 * Filter implementation of XSFilter.
 * This class forwards all the method calls to another XSFunction.
 * 
 * <p>
 * This class is intended to be derived by client application
 * to add some meaningful behavior.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class XSFunctionFilter<T> implements XSFunction<T> {
    
    /** This object will receive all forwarded calls. */
    protected XSFunction<T> core;
    
    public XSFunctionFilter( XSFunction<T> _core ) {
        this.core = _core;
    }
    
    public XSFunctionFilter() {}
    
    public T annotation(XSAnnotation ann) {
        return core.annotation(ann);
    }

    public T attGroupDecl(XSAttGroupDecl decl) {
        return core.attGroupDecl(decl);
    }

    public T attributeDecl(XSAttributeDecl decl) {
        return core.attributeDecl(decl);
    }
    
    public T attributeUse(XSAttributeUse use) {
        return core.attributeUse(use);
    }

    public T complexType(XSComplexType type) {
        return core.complexType(type);
    }

    public T schema(XSSchema schema) {
        return core.schema(schema);
    }

    public T facet(XSFacet facet) {
        return core.facet(facet);
    }

    public T notation(XSNotation notation) {
        return core.notation(notation);
    }

    public T simpleType(XSSimpleType simpleType) {
        return core.simpleType(simpleType);
    }

    public T particle(XSParticle particle) {
        return core.particle(particle);
    }

    public T empty(XSContentType empty) {
        return core.empty(empty);
    }

    public T wildcard(XSWildcard wc) {
        return core.wildcard(wc);
    }

    public T modelGroupDecl(XSModelGroupDecl decl) {
        return core.modelGroupDecl(decl);
    }

    public T modelGroup(XSModelGroup group) {
        return core.modelGroup(group);
    }

    public T elementDecl(XSElementDecl decl) {
        return core.elementDecl(decl);
    }

    public T identityConstraint(XSIdentityConstraint decl) {
        return core.identityConstraint(decl);
    }

    public T xpath(XSXPath xpath) {
        return core.xpath(xpath);
    }
}
