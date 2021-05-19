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

import org.xml.sax.Locator;

import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSModelGroup;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.impl.parser.SchemaDocumentImpl;
import com.sun.xml.xsom.visitor.XSFunction;
import com.sun.xml.xsom.visitor.XSTermFunction;
import com.sun.xml.xsom.visitor.XSTermFunctionWithParam;
import com.sun.xml.xsom.visitor.XSTermVisitor;
import com.sun.xml.xsom.visitor.XSVisitor;

public class ModelGroupDeclImpl extends DeclarationImpl implements XSModelGroupDecl, Ref.Term
{
    public ModelGroupDeclImpl( SchemaDocumentImpl owner,
        AnnotationImpl _annon, Locator _loc, ForeignAttributesImpl _fa,
        String _targetNamespace, String _name,
        ModelGroupImpl _modelGroup ) {
        
        super(owner,_annon,_loc,_fa,_targetNamespace,_name,false);
        this.modelGroup = _modelGroup;
        
        if(modelGroup==null)
            throw new IllegalArgumentException();
    }
    
    private final ModelGroupImpl modelGroup;
    public XSModelGroup getModelGroup() { return modelGroup; }
    
    /**
     * This component is a redefinition of "oldMG". Fix up the internal state
     * as such. 
     */
    public void redefine( ModelGroupDeclImpl oldMG ) {
        modelGroup.redefine(oldMG);
    }
    
    
    public void visit( XSVisitor visitor ) {
        visitor.modelGroupDecl(this);
    }
    public void visit( XSTermVisitor visitor ) {
        visitor.modelGroupDecl(this);
    }
    public Object apply( XSTermFunction function ) {
        return function.modelGroupDecl(this);
    }

    public <T,P> T apply(XSTermFunctionWithParam<T, P> function, P param) {
        return function.modelGroupDecl(this,param);
    }

    public Object apply( XSFunction function ) {
        return function.modelGroupDecl(this);
    }


    public boolean isWildcard()                 { return false; }
    public boolean isModelGroupDecl()           { return true; }
    public boolean isModelGroup()               { return false; }
    public boolean isElementDecl()              { return false; }

    public XSWildcard asWildcard()              { return null; }
    public XSModelGroupDecl asModelGroupDecl()  { return this; }
    public XSModelGroup asModelGroup()          { return null; }
    public XSElementDecl asElementDecl()        { return null; }


    // Ref.Term implementation
    public XSTerm getTerm() { return this; }
}
