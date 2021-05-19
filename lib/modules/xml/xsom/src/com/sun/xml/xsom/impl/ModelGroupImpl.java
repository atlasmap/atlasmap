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
package com.sun.xml.xsom.impl;

import java.util.Arrays;
import java.util.Iterator;

import org.xml.sax.Locator;

import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.impl.parser.SchemaDocumentImpl;
import com.sun.xml.xsom.visitor.XSFunction;
import com.sun.xml.xsom.visitor.XSTermFunction;
import com.sun.xml.xsom.visitor.XSTermFunctionWithParam;
import com.sun.xml.xsom.visitor.XSTermVisitor;
import com.sun.xml.xsom.visitor.XSVisitor;

public class ModelGroupImpl extends ComponentImpl implements XSModelGroup, Ref.Term
{
    public ModelGroupImpl( SchemaDocumentImpl owner, AnnotationImpl _annon, Locator _loc, ForeignAttributesImpl _fa,
                           Compositor _compositor, ParticleImpl[] _children ) {

        super(owner,_annon,_loc,_fa);
        this.compositor = _compositor;
        this.children = _children;

        if(compositor==null)
            throw new IllegalArgumentException();
        for( int i=children.length-1; i>=0; i-- )
            if(children[i]==null)
                throw new IllegalArgumentException();
    }

    private final ParticleImpl[] children;
    public ParticleImpl getChild( int idx ) { return children[idx]; }
    public int getSize() { return children.length; }

    public ParticleImpl[] getChildren() { return children; }


    private final Compositor compositor;
    public Compositor getCompositor() { return compositor; }


    public void redefine(ModelGroupDeclImpl oldMG) {
        for (ParticleImpl p : children)
            p.redefine(oldMG);
    }

    public Iterator<XSParticle> iterator() {
        return Arrays.asList((XSParticle[])children).iterator();
    }


    public boolean isWildcard()                 { return false; }
    public boolean isModelGroupDecl()           { return false; }
    public boolean isModelGroup()               { return true; }
    public boolean isElementDecl()              { return false; }

    public XSWildcard asWildcard()              { return null; }
    public XSModelGroupDecl asModelGroupDecl()  { return null; }
    public XSModelGroup asModelGroup()          { return this; }
    public XSElementDecl asElementDecl()        { return null; }



    public void visit( XSVisitor visitor ) {
        visitor.modelGroup(this);
    }
    public void visit( XSTermVisitor visitor ) {
        visitor.modelGroup(this);
    }
    public Object apply( XSTermFunction function ) {
        return function.modelGroup(this);
    }

    public <T,P> T apply(XSTermFunctionWithParam<T, P> function, P param) {
        return function.modelGroup(this,param);
    }

    public Object apply( XSFunction function ) {
        return function.modelGroup(this);
    }

    // Ref.Term implementation
    public XSTerm getTerm() { return this; }
}
