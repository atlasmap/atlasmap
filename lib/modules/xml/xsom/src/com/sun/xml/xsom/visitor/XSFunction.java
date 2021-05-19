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
package com.sun.xml.xsom.visitor;

import com.sun.xml.xsom.XSAnnotation;
import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSAttributeUse;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSIdentityConstraint;
import com.sun.xml.xsom.XSNotation;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.XSXPath;

/**
 * Function object that works on the entire XML Schema components.
 * 
 * @author
 *     Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface XSFunction<T> extends XSContentTypeFunction<T>, XSTermFunction<T> {
        
    T annotation( XSAnnotation ann );
    T attGroupDecl( XSAttGroupDecl decl );
    T attributeDecl( XSAttributeDecl decl );
    T attributeUse( XSAttributeUse use );
    T complexType( XSComplexType type );
    T schema( XSSchema schema );
//    T schemaSet( XSSchemaSet schema );
    T facet( XSFacet facet );
    T notation( XSNotation notation );
    T identityConstraint(XSIdentityConstraint decl);
    T xpath(XSXPath xpath);
}
