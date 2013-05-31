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

package com.sun.xml.xsom;

import com.sun.xml.xsom.impl.scd.Iterators;
import com.sun.xml.xsom.impl.scd.ParseException;
import com.sun.xml.xsom.impl.scd.SCDImpl;
import com.sun.xml.xsom.impl.scd.SCDParser;
import com.sun.xml.xsom.impl.scd.Step;
import com.sun.xml.xsom.impl.scd.TokenMgrError;
import com.sun.xml.xsom.util.DeferedCollection;

import javax.xml.namespace.NamespaceContext;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 * Schema Component Designator (SCD).
 *
 * <p>
 * SCD for schema is what XPath is for XML. SCD allows you to select a schema component(s)
 * from a schema component(s).
 *
 * <p>
 * See <a href="http://www.w3.org/TR/2005/WD-xmlschema-ref-20050329/">XML Schema: Component Designators</a>.
 * This implementation is based on 03/29/2005 working draft.
 *
 * @author Kohsuke Kawaguchi
 */
public abstract class SCD {

    /**
     * Parses the string representation of SCD.
     *
     * <p>
     * This method involves parsing the path expression and preparing the in-memory
     * structure, so this is useful when you plan to use the same SCD against
     * different context node multiple times.
     *
     * <p>
     * If you want to evaluate SCD just once, use {@link XSComponent#select} methods.
     *
     * @param path
     *      the string representation of SCD, such as "/foo/bar".
     * @param nsContext
     *      Its {@link NamespaceContext#getNamespaceURI(String)} is used
     *      to resolve prefixes in the SCD to the namespace URI.
     */
    public static SCD create(String path, NamespaceContext nsContext) throws java.text.ParseException {
        try {
            SCDParser p = new SCDParser(path,nsContext);
            List<?> list = p.RelativeSchemaComponentPath();
            return new SCDImpl(path,list.toArray(new Step[list.size()]));
        } catch (TokenMgrError e) {
            throw setCause(new java.text.ParseException(e.getMessage(), -1 ),e);
        } catch (ParseException e) {
            throw setCause(new java.text.ParseException(e.getMessage(), e.currentToken.beginColumn ),e);
        }
    }

    private static java.text.ParseException setCause(java.text.ParseException e, Throwable x) {
        e.initCause(x);
        return e;
    }

    /**
     * Evaluates the SCD against the given context node and
     * returns the matched nodes.
     *
     * @return
     *      could be empty but never be null.
     */
    public final Collection<XSComponent> select(XSComponent contextNode) {
        return new DeferedCollection<XSComponent>(select(Iterators.singleton(contextNode)));
    }

    /**
     * Evaluates the SCD against the whole schema and
     * returns the matched nodes.
     *
     * <p>
     * This method is here because {@link XSSchemaSet}
     * doesn't implement {@link XSComponent}.
     *
     * @return
     *      could be empty but never be null.
     */
    public final Collection<XSComponent> select(XSSchemaSet contextNode) {
        return select(contextNode.getSchemas());
    }

    /**
     * Evaluates the SCD against the given context node and
     * returns the matched node.
     *
     * @return
     *      null if the SCD didn't match anything. If the SCD matched more than one node,
     *      the first one will be returned.
     */
    public final XSComponent selectSingle(XSComponent contextNode) {
        Iterator<XSComponent> r = select(Iterators.singleton(contextNode));
        if(r.hasNext())     return r.next();
        return null;
    }

    /**
     * Evaluates the SCD against the whole schema set and
     * returns the matched node.
     *
     * @return
     *      null if the SCD didn't match anything. If the SCD matched more than one node,
     *      the first one will be returned.
     */
    public final XSComponent selectSingle(XSSchemaSet contextNode) {
        Iterator<XSComponent> r = select(contextNode.iterateSchema());
        if(r.hasNext())     return r.next();
        return null;
    }

    /**
     * Evaluates the SCD against the given set of context nodes and
     * returns the matched nodes.
     *
     * @param contextNodes
     *      {@link XSComponent}s that represent the context node against
     *      which {@link SCD} is evaluated.
     *
     * @return
     *      could be empty but never be null.
     */
    public abstract Iterator<XSComponent> select(Iterator<? extends XSComponent> contextNodes);

    /**
     * Evaluates the SCD against the given set of context nodes and
     * returns the matched nodes.
     *
     * @param contextNodes
     *      {@link XSComponent}s that represent the context node against
     *      which {@link SCD} is evaluated.
     *
     * @return
     *      could be empty but never be null.
     */
    public final Collection<XSComponent> select(Collection<? extends XSComponent> contextNodes) {
        return new DeferedCollection<XSComponent>(select(contextNodes.iterator()));
    }

    /**
     * Returns the textual SCD representation as given to {@link SCD#create(String, NamespaceContext)}.
     */
    public abstract String toString();
}
