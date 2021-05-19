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
package com.sun.xml.xsom;

/**
 * Model group.
 * 
 * @author
 *  Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface XSModelGroup extends XSComponent, XSTerm, Iterable<XSParticle>
{
    /**
     * Type-safe enumeration for kind of model groups.
     * Constants are defined in the {@link XSModelGroup} interface.
     */
    public static enum Compositor {
        ALL("all"),CHOICE("choice"),SEQUENCE("sequence");

        private Compositor(String _value) {
            this.value = _value;
        }

        private final String value;
        /**
         * Returns the human-readable compositor name.
         * 
         * @return
         *      Either "all", "sequence", or "choice".
         */
        public String toString() {
            return value;
        }
    }
    /**
     * A constant that represents "all" compositor.
     */
    static final Compositor ALL = Compositor.ALL;
    /**
     * A constant that represents "sequence" compositor.
     */
    static final Compositor SEQUENCE = Compositor.SEQUENCE;
    /**
     * A constant that represents "choice" compositor.
     */
    static final Compositor CHOICE = Compositor.CHOICE;

    Compositor getCompositor();

    /**
     * Gets <i>i</i>-ith child.
     */
    XSParticle getChild(int idx);
    /**
     * Gets the number of children.
     */
    int getSize();

    /**
     * Gets all the children in one array.
     */
    XSParticle[] getChildren();
}
