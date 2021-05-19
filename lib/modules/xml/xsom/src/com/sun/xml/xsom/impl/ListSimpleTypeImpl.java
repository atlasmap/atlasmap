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
import java.util.Set;

import org.xml.sax.Locator;

import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSListSimpleType;
import com.sun.xml.xsom.XSSimpleType;
import com.sun.xml.xsom.XSVariety;
import com.sun.xml.xsom.impl.parser.SchemaDocumentImpl;
import com.sun.xml.xsom.visitor.XSSimpleTypeFunction;
import com.sun.xml.xsom.visitor.XSSimpleTypeVisitor;

public class ListSimpleTypeImpl extends SimpleTypeImpl implements XSListSimpleType
{
    public ListSimpleTypeImpl( SchemaDocumentImpl _parent,
                               AnnotationImpl _annon, Locator _loc, ForeignAttributesImpl _fa,
                               String _name, boolean _anonymous, Set<XSVariety> finalSet,
                               Ref.SimpleType _itemType ) {

        super(_parent,_annon,_loc,_fa,_name,_anonymous, finalSet,
            _parent.getSchema().parent.anySimpleType);

        this.itemType = _itemType;
    }

    private final Ref.SimpleType itemType;
    public XSSimpleType getItemType() { return itemType.getType(); }

    public void visit( XSSimpleTypeVisitor visitor ) {
        visitor.listSimpleType(this);
    }
    public Object apply( XSSimpleTypeFunction function ) {
        return function.listSimpleType(this);
    }

    // list type by itself doesn't have any facet. */
    public XSFacet getFacet( String name ) { return null; }
    public List<XSFacet> getFacets( String name ) { return Collections.EMPTY_LIST; }

    public XSVariety getVariety() { return XSVariety.LIST; }

    public XSSimpleType getPrimitiveType() { return null; }

    public XSListSimpleType getBaseListType() {return this;}

    public boolean isList() { return true; }
    public XSListSimpleType asList() { return this; }
}
