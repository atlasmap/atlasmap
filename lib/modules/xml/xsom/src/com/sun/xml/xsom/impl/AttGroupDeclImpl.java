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

import java.util.Iterator;

import org.xml.sax.Locator;

import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSWildcard;
import com.sun.xml.xsom.impl.parser.DelayedRef;
import com.sun.xml.xsom.impl.parser.SchemaDocumentImpl;
import com.sun.xml.xsom.visitor.XSFunction;
import com.sun.xml.xsom.visitor.XSVisitor;

public class AttGroupDeclImpl extends AttributesHolder implements XSAttGroupDecl
{
    public AttGroupDeclImpl( SchemaDocumentImpl _parent, AnnotationImpl _annon,
        Locator _loc, ForeignAttributesImpl _fa, String _name, WildcardImpl _wildcard ) {
        
        this(_parent,_annon,_loc,_fa,_name);
        setWildcard(_wildcard);
    }
        
    public AttGroupDeclImpl( SchemaDocumentImpl _parent, AnnotationImpl _annon,
        Locator _loc, ForeignAttributesImpl _fa, String _name ) {
            
        super(_parent,_annon,_loc,_fa,_name,false);
    }
    
    
    private WildcardImpl wildcard;
    public void setWildcard( WildcardImpl wc ) { wildcard=wc; }
    public XSWildcard getAttributeWildcard() { return wildcard; }

    public XSAttributeUse getAttributeUse( String nsURI, String localName ) {
        UName name = new UName(nsURI,localName);
        XSAttributeUse o=null;
        
        Iterator itr = iterateAttGroups();
        while(itr.hasNext() && o==null)
            o = ((XSAttGroupDecl)itr.next()).getAttributeUse(nsURI,localName);
        
        if(o==null)     o = attributes.get(name);
        
        return o;
    }
    
    public void redefine( AttGroupDeclImpl ag ) {
        for (Iterator itr = attGroups.iterator(); itr.hasNext();) {
            DelayedRef.AttGroup r = (DelayedRef.AttGroup) itr.next();
            r.redefine(ag);
        }
    }
    
    public void visit( XSVisitor visitor ) {
        visitor.attGroupDecl(this);
    }
    public Object apply( XSFunction function ) {
        return function.attGroupDecl(this);
    }
}
