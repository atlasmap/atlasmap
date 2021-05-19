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
package com.sun.xml.xsom.impl.scd;

import java.util.Iterator;

import com.sun.xml.xsom.SCD;
import com.sun.xml.xsom.XSComponent;

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
