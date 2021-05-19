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

import com.sun.xml.xsom.XSContentType;
import com.sun.xml.xsom.XSParticle;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.visitor.XSContentTypeFunction;
import com.sun.xml.xsom.visitor.XSContentTypeVisitor;
import com.sun.xml.xsom.visitor.XSFunction;
import com.sun.xml.xsom.visitor.XSVisitor;

/**
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class EmptyImpl extends ComponentImpl implements ContentTypeImpl {
    public EmptyImpl() { super(null,null,null,null); }
    
    public XSSimpleType asSimpleType()  { return null; }
    public XSParticle asParticle()      { return null; }
    public XSContentType asEmpty()      { return this; }
    
    public Object apply( XSContentTypeFunction function ) {
        return function.empty(this);
    }
    public Object apply( XSFunction function ) {
        return function.empty(this);
    }
    public void visit( XSVisitor visitor ) {
        visitor.empty(this);
    }
    public void visit( XSContentTypeVisitor visitor ) {
        visitor.empty(this);
    }
    
    public XSContentType getContentType() { return this; }
}
