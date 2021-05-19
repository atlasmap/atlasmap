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

import java.util.Collections;
import java.util.List;

import org.xml.sax.Locator;

import com.sun.xml.xsom.XSElementDecl;
import com.sun.xml.xsom.XSIdentityConstraint;
import com.sun.xml.xsom.XSXPath;
import com.sun.xml.xsom.impl.parser.SchemaDocumentImpl;
import com.sun.xml.xsom.visitor.XSFunction;
import com.sun.xml.xsom.visitor.XSVisitor;

/**
 * {@link XSIdentityConstraint} implementation.
 *
 * @author Kohsuke Kawaguchi
 */
public class IdentityConstraintImpl extends ComponentImpl implements XSIdentityConstraint, Ref.IdentityConstraint {

    private XSElementDecl parent;
    private final short category;
    private final String name;
    private final XSXPath selector;
    private final List<XSXPath> fields;
    private final Ref.IdentityConstraint refer;

    public IdentityConstraintImpl(SchemaDocumentImpl _owner, AnnotationImpl _annon, Locator _loc,
        ForeignAttributesImpl fa, short category, String name, XPathImpl selector,
        List<XPathImpl> fields, Ref.IdentityConstraint refer) {

        super(_owner, _annon, _loc, fa);
        this.category = category;
        this.name = name;
        this.selector = selector;
        selector.setParent(this);
        this.fields = Collections.unmodifiableList((List<? extends XSXPath>)fields);
        for( XPathImpl xp : fields )
            xp.setParent(this);
        this.refer = refer;
    }


    public void visit(XSVisitor visitor) {
        visitor.identityConstraint(this);
    }

    public <T> T apply(XSFunction<T> function) {
        return function.identityConstraint(this);
    }

    public void setParent(ElementDecl parent) {
        this.parent = parent;
        parent.getOwnerSchema().addIdentityConstraint(this);
    }

    public XSElementDecl getParent() {
        return parent;
    }

    public String getName() {
        return name;
    }

    public String getTargetNamespace() {
        return getParent().getTargetNamespace();
    }

    public short getCategory() {
        return category;
    }

    public XSXPath getSelector() {
        return selector;
    }

    public List<XSXPath> getFields() {
        return fields;
    }

    public XSIdentityConstraint getReferencedKey() {
        if(category==KEYREF)
            return refer.get();
        else
            throw new IllegalStateException("not a keyref");
    }

    public XSIdentityConstraint get() {
        return this;
    }
}
