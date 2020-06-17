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

package com.sun.xml.xsom.impl.parser;

import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import com.sun.xml.xsom.XSAttGroupDecl;
import com.sun.xml.xsom.XSAttributeDecl;
import com.sun.xml.xsom.XSComplexType;
import com.sun.xml.xsom.XSDeclaration;
import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSIdentityConstraint;
import com.sun.xml.xsom.XSModelGroupDecl;
import com.sun.xml.xsom.XSSchemaSet;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSTerm;
import com.sun.xml.xsom.XSType;
import com.sun.xml.xsom.impl.Ref;
import com.sun.xml.xsom.impl.SchemaImpl;
import com.sun.xml.xsom.impl.UName;

/**
 * Reference by name.
 * 
 * UName will be later resolved to a target object,
 * after all the schemas are parsed.
 */
public abstract class DelayedRef implements Patch {
    DelayedRef( PatcherManager _manager, Locator _source, SchemaImpl _schema, UName _name ) {

        this.schema = _schema.getRoot();
        this.manager = _manager;
        this.name = _name;
        this.source = _source;
        
        if(name==null)  throw new InternalError();
        
        manager.addPatcher(this);
    }
    
    /**
     * Patch implementation. Makes sure that the name resolves
     * to a schema component.
     */
    public void run() throws SAXException {
        if(ref==null)    // redefinition can set ref without actually resolving the reference
            resolve();
        manager = null;     // avoid keeping the reference too long
        name = null;
        source = null;
    }


    protected final XSSchemaSet schema;
    private PatcherManager manager;
    private UName name;
    /** location in the source file where this reference was made. */
    private Locator source;

    protected abstract Object resolveReference( UName name );
    protected abstract String getErrorProperty();

    private Object ref=null;
    protected final Object _get() {
        if(ref==null)   throw new InternalError("unresolved reference");
        return ref;
    }
    
    private void resolve() throws SAXException {
        ref = resolveReference(name);
        if(ref==null)
            manager.reportError(
                Messages.format(getErrorProperty(),name.getQualifiedName()),
                source );
    }

    /**
     * If this reference refers to the given declaration,
     * resolve the reference now. This is used to implement redefinition. 
     */
    public void redefine(XSDeclaration d) {
        if( !d.getTargetNamespace().equals(name.getNamespaceURI())
        ||  !d.getName().equals(name.getName()) )
            return;
        
        ref = d;
        manager = null;
        name = null;
        source = null;
    }


    public static class Type extends DelayedRef implements Ref.Type {
        public Type( PatcherManager manager, Locator loc, SchemaImpl schema, UName name ) {
            super(manager,loc,schema,name);
        }
        protected Object resolveReference( UName name ) {
            Object o = super.schema.getSimpleType(
                name.getNamespaceURI(), name.getName() );
            if(o!=null)     return o;
            
            return super.schema.getComplexType(
                name.getNamespaceURI(),
                name.getName());
        }
        protected String getErrorProperty() {
            return Messages.ERR_UNDEFINED_TYPE;
        }
    
        public XSType getType() { return (XSType)super._get(); }
    }

    public static class SimpleType extends DelayedRef implements Ref.SimpleType {
        public SimpleType( PatcherManager manager, Locator loc, SchemaImpl schema, UName name ) {
            super(manager,loc,schema,name);
        }
        public XSSimpleType getType() { return (XSSimpleType)_get(); }

        protected Object resolveReference( UName name ) {
            return super.schema.getSimpleType(
                name.getNamespaceURI(),
                name.getName());
        }
        
        protected String getErrorProperty() {
            return Messages.ERR_UNDEFINED_SIMPLETYPE;
        }
    }

    public static class ComplexType extends DelayedRef implements Ref.ComplexType {
        public ComplexType( PatcherManager manager, Locator loc, SchemaImpl schema, UName name ) {
            super(manager,loc,schema,name);
        }
        protected Object resolveReference( UName name ) {
            return super.schema.getComplexType(
                name.getNamespaceURI(),
                name.getName());
        }
        
        protected String getErrorProperty() {
            return Messages.ERR_UNDEFINED_COMPLEXTYPE;
        }
    
        public XSComplexType getType() { return (XSComplexType)super._get(); }
    }

    public static class Element extends DelayedRef implements Ref.Element {
        public Element( PatcherManager manager, Locator loc, SchemaImpl schema, UName name ) {
            super(manager,loc,schema,name);
        }
        protected Object resolveReference( UName name ) {
            return super.schema.getElementDecl(
                name.getNamespaceURI(),
                name.getName());
        }
        
        protected String getErrorProperty() {
            return Messages.ERR_UNDEFINED_ELEMENT;
        }
    
        public XSElementDecl get() { return (XSElementDecl)super._get(); }
        public XSTerm getTerm() { return get(); }
    }
    
    public static class ModelGroup extends DelayedRef implements Ref.Term {
        public ModelGroup( PatcherManager manager, Locator loc, SchemaImpl schema, UName name ) {
            super(manager,loc,schema,name);
        }
        protected Object resolveReference( UName name ) {
            return super.schema.getModelGroupDecl(
                name.getNamespaceURI(),
                name.getName());
        }
        
        protected String getErrorProperty() {
            return Messages.ERR_UNDEFINED_MODELGROUP;
        }
    
        public XSModelGroupDecl get() { return (XSModelGroupDecl)super._get(); }
        public XSTerm getTerm() { return get(); }
    }
    
    public static class AttGroup extends DelayedRef implements Ref.AttGroup {
        public AttGroup( PatcherManager manager, Locator loc, SchemaImpl schema, UName name ) {
            super(manager,loc,schema,name);
        }
        protected Object resolveReference( UName name ) {
            return super.schema.getAttGroupDecl(
                name.getNamespaceURI(),
                name.getName());
        }
        
        protected String getErrorProperty() {
            return Messages.ERR_UNDEFINED_ATTRIBUTEGROUP;
        }
    
        public XSAttGroupDecl get() { return (XSAttGroupDecl)super._get(); }
    }

    public static class Attribute extends DelayedRef implements Ref.Attribute {
        public Attribute( PatcherManager manager, Locator loc, SchemaImpl schema, UName name ) {
            super(manager,loc,schema,name);
        }
        protected Object resolveReference( UName name ) {
            return super.schema.getAttributeDecl(
                name.getNamespaceURI(),
                name.getName());
        }
        
        protected String getErrorProperty() {
            return Messages.ERR_UNDEFINED_ATTRIBUTE;
        }
    
        public XSAttributeDecl getAttribute() { return (XSAttributeDecl)super._get(); }
    }

    public static class IdentityConstraint extends DelayedRef implements Ref.IdentityConstraint {
        public IdentityConstraint( PatcherManager manager, Locator loc, SchemaImpl schema, UName name ) {
            super(manager,loc,schema,name);
        }
        protected Object resolveReference( UName name ) {
            return super.schema.getIdentityConstraint(
                name.getNamespaceURI(),
                name.getName());
        }

        protected String getErrorProperty() {
            return Messages.ERR_UNDEFINED_IDENTITY_CONSTRAINT;
        }

        public XSIdentityConstraint get() { return (XSIdentityConstraint)super._get(); }
    }
}

