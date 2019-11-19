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

import com.sun.xml.xsom.SCD;
import com.sun.xml.xsom.XSComponent;

import java.util.Iterator;

/**
 * Schema component designator.
 *
 * @author Kohsuke Kawaguchi
 */
public final class SCDImpl extends SCD {
    /**
     * SCD is fundamentally a list of steps.
     */
    private final Step[] steps;

    /**
     * The original textual SCD representation.
     */
    private final String text;

    public SCDImpl(String text, Step[] steps) {
        this.text = text;
        this.steps = steps;
    }

    public Iterator<XSComponent> select(Iterator<? extends XSComponent> contextNode) {
        Iterator<XSComponent> nodeSet = (Iterator)contextNode;

        int len = steps.length;
        for( int i=0; i<len; i++ ) {
            if(i!=0 && i!=len-1 && !steps[i-1].axis.isModelGroup() && steps[i].axis.isModelGroup()) {
                // expand the current nodeset by adding abbreviatable complex type and model groups.
                // note that such expansion is not allowed to occure in in between model group axes.

                // TODO: this step is not needed if the next step is known not to react to
                // complex type nor model groups, such as, say Axis.FACET
                nodeSet = new Iterators.Unique<XSComponent>(
                    new Iterators.Map<XSComponent,XSComponent>(nodeSet) {
                        protected Iterator<XSComponent> apply(XSComponent u) {
                            return new Iterators.Union<XSComponent>(
                                Iterators.singleton(u),
                                Axis.INTERMEDIATE_SKIP.iterator(u) );
                        }
                    }
                );
            }
            nodeSet = steps[i].evaluate(nodeSet);
        }

        return nodeSet;
    }

    public String toString() {
        return text;
    }
}
