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

import com.sun.xml.xsom.XSNotation;
import com.sun.xml.xsom.impl.parser.SchemaDocumentImpl;
import com.sun.xml.xsom.visitor.XSFunction;
import com.sun.xml.xsom.visitor.XSVisitor;

/**
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public class NotationImpl extends DeclarationImpl implements XSNotation {
    
    public NotationImpl( SchemaDocumentImpl owner, AnnotationImpl _annon,
        Locator _loc, ForeignAttributesImpl _fa, String _name,
        String _publicId, String _systemId ) {
        super(owner,_annon,_loc,_fa,owner.getTargetNamespace(),_name,false);
        
        this.publicId = _publicId;
        this.systemId = _systemId;
    }
    
    private final String publicId;
    private final String systemId;
    
    public String getPublicId() { return publicId; }
    public String getSystemId() { return systemId; }

    public void visit(XSVisitor visitor) {
        visitor.notation(this);
    }

    public Object apply(XSFunction function) {
        return function.notation(this);
    }

}
