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

package com.sun.xml.xsom.impl.scd;

import com.sun.xml.xsom.XSComponent;
import com.sun.xml.xsom.XSDeclaration;
import com.sun.xml.xsom.XSFacet;
import com.sun.xml.xsom.XSType;
import com.sun.xml.xsom.SCD;
import com.sun.xml.xsom.XSSchema;
import com.sun.xml.xsom.impl.UName;

import java.util.Iterator;

/**
 * Building block of {@link SCD}.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class Step<T extends XSComponent> {
    public final Axis<? extends T> axis;

    /**
     * 'Predicate' in SCD designates the index of the item. -1 if there's no predicate.
     * Predicate starts from 1.
     *
     * <p>
     * Because of the parsing order this parameter cannot be marked
     * final, even though it's immutable once it's parsed.
     */
    int predicate = -1;

    protected Step(Axis<? extends T> axis) {
        this.axis = axis;
    }

    /**
     * Perform filtering (which is different depending on the kind of step.)
     */
    protected abstract Iterator<? extends T> filter( Iterator<? extends T> base );

    /**
     * Evaluate this step against the current node set
     * and returns matched nodes.
     */
    public final Iterator<T> evaluate(Iterator<XSComponent> nodeSet) {
        // list up the whole thing
        Iterator<T> r = new Iterators.Map<T,XSComponent>(nodeSet) {
            protected Iterator<? extends T> apply(XSComponent contextNode) {
                return filter(axis.iterator(contextNode));
            }
        };

        // avoid duplicates
        r = new Iterators.Unique<T>(r);

        if(predicate>=0) {
            T item=null;
            for( int i=predicate; i>0; i-- ) {
                if(!r.hasNext())
                    return Iterators.empty();
                item = r.next();
            }
            return new Iterators.Singleton<T>(item);
        }

        return r;
    }

    /**
     * Matches any name.
     */
    static final class Any extends Step<XSComponent> {
        public Any(Axis<? extends XSComponent> axis) {
            super(axis);
        }

        // no filtering.
        protected Iterator<? extends XSComponent> filter(Iterator<? extends XSComponent> base) {
            return base;
        }
    }

    private static abstract class Filtered<T extends XSComponent> extends Step<T> {
        protected Filtered(Axis<? extends T> axis) {
            super(axis);
        }

        protected Iterator<T> filter(Iterator<? extends T> base) {
            return new Iterators.Filter<T>(base) {
                protected boolean matches(T d) {
                    return match(d);
                }
            };
        }

        protected abstract boolean match(T d);
    }

    /**
     * Matches a particular name.
     */
    static final class Named extends Filtered<XSDeclaration> {
        private final String nsUri;
        private final String localName;

        public Named(Axis<? extends XSDeclaration> axis, UName n) {
            this(axis,n.getNamespaceURI(),n.getName());
        }

        public Named(Axis<? extends XSDeclaration> axis, String nsUri, String localName) {
            super(axis);
            this.nsUri = nsUri;
            this.localName = localName;
        }

        protected boolean match(XSDeclaration d) {
            return d.getName().equals(localName) && d.getTargetNamespace().equals(nsUri);
        }
    }

    /**
     * Matches anonymous types.
     */
    static final class AnonymousType extends Filtered<XSType> {
        public AnonymousType(Axis<? extends XSType> axis) {
            super(axis);
        }

        protected boolean match(XSType node) {
            return node.isLocal();
        }
    }

    /**
     * Matches a particular kind of facets.
     */
    static final class Facet extends Filtered<XSFacet> {
        private final String name;
        public Facet(Axis<XSFacet> axis, String facetName) {
            super(axis);
            this.name = facetName;
        }

        protected boolean match(XSFacet f) {
            return f.getName().equals(name);
        }
    }

    /**
     * Matches a schema in a particular namespace.
     */
    static final class Schema extends Filtered<XSSchema> {
        private final String uri;
        public Schema(Axis<XSSchema> axis, String uri) {
            super(axis);
            this.uri = uri;
        }

        protected boolean match(XSSchema d) {
            return d.getTargetNamespace().equals(uri);
        }
    }
}
