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

import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XmlString;
import com.sun.xml.xsom.impl.parser.SchemaDocumentImpl;
import com.sun.xml.xsom.visitor.XSFunction;
import com.sun.xml.xsom.visitor.XSVisitor;

public class AttributeDeclImpl extends DeclarationImpl implements XSAttributeDecl, Ref.Attribute
{
    public AttributeDeclImpl( SchemaDocumentImpl owner,
        String _targetNamespace, String _name,
        AnnotationImpl _annon, Locator _loc, ForeignAttributesImpl _fa, boolean _anonymous,
        XmlString _defValue, XmlString _fixedValue,
        Ref.SimpleType _type ) {
        
        super(owner,_annon,_loc,_fa,_targetNamespace,_name,_anonymous);
        
        if(_name==null) // assertion failed.
            throw new IllegalArgumentException();
        
        this.defaultValue = _defValue;
        this.fixedValue = _fixedValue;
        this.type = _type;
    }
    
    private final Ref.SimpleType type;
    public XSSimpleType getType() { return type.getType(); }

    private final XmlString defaultValue;
    public XmlString getDefaultValue() { return defaultValue; }
    
    private final XmlString fixedValue;
    public XmlString getFixedValue() { return fixedValue; }
    
    public void visit( XSVisitor visitor ) {
        visitor.attributeDecl(this);
    }
    public Object apply( XSFunction function ) {
        return function.attributeDecl(this);
    }


    // Ref.Attribute implementation
    public XSAttributeDecl getAttribute() { return this; }
 }
