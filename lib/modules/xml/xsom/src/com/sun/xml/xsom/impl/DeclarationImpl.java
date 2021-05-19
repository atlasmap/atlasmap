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

import com.sun.xml.xsom.XSDeclaration;
import com.sun.xml.xsom.impl.parser.SchemaDocumentImpl;

abstract class DeclarationImpl extends ComponentImpl implements XSDeclaration
{
    DeclarationImpl( SchemaDocumentImpl owner,
        AnnotationImpl _annon, Locator loc, ForeignAttributesImpl fa,
        String _targetNamespace, String _name,    boolean _anonymous ) {
        
        super(owner,_annon,loc,fa);
        this.targetNamespace = _targetNamespace;
        this.name = _name;
        this.anonymous = _anonymous;
    }
    
    private final String name;
    public String getName() { return name; }
    
    private final String targetNamespace;
    public String getTargetNamespace() { return targetNamespace; }
    
    private final boolean anonymous;
    /** @deprecated */
    public boolean isAnonymous() { return anonymous; }
    
    public final boolean isGlobal() { return !isAnonymous(); }
    public final boolean isLocal() { return isAnonymous(); }
}
