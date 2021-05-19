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

import org.relaxng.datatype.ValidationContext;

/**
 * String with in-scope namespace binding information.
 *
 * <p>
 * In a general case, text (PCDATA/attributes) that appear in XML schema
 * cannot be correctly interpreted unless you also have in-scope namespace
 * binding (a case in point is QName.) Therefore, it's convenient to
 * handle the lexical representation and the in-scope namespace binding
 * in a pair.
 *
 * @author Kohsuke Kawaguchi
 */
public final class XmlString {
    /**
     * Textual value. AKA lexical representation.
     */
    public final String value;

    /**
     * Used to resole in-scope namespace bindings.
     */
    public final ValidationContext context;

    /**
     * Creates a new {@link XmlString} from a lexical representation and in-scope namespaces.
     */
    public XmlString(String value, ValidationContext context) {
        this.value = value;
        this.context = context;
        if(context==null)
            throw new IllegalArgumentException();
    }

    /**
     * Creates a new {@link XmlString} with empty in-scope namespace bindings.
     */
    public XmlString(String value) {
        this(value,NULL_CONTEXT);
    }

    /**
     * Resolves a namespace prefix to the corresponding namespace URI.
     *
     * This method is used for resolving prefixes in the {@link #value}
     * (such as when {@link #value} represents a QName type.)
     *
     * <p>
     * If the prefix is "" (empty string), the method
     * returns the default namespace URI.
     *
     * <p>
     * If the prefix is "xml", then the method returns
     * "http://www.w3.org/XML/1998/namespace",
     * as defined in the XML Namespaces Recommendation.
     *
     * @return
     *		namespace URI of this prefix.
     *		If the specified prefix is not declared,
     *		the implementation returns null.
     */
    public final String resolvePrefix(String prefix) {
        return context.resolveNamespacePrefix(prefix);
    }

    public String toString() {
        return value;
    }

    private static final ValidationContext NULL_CONTEXT = new ValidationContext() {
        public String resolveNamespacePrefix(String s) {
            if(s.length()==0)   return "";
            if(s.equals("xml")) return "http://www.w3.org/XML/1998/namespace";
            return null;
        }

        public String getBaseUri() {
            return null;
        }

        public boolean isUnparsedEntity(String s) {
            return false;
        }

        public boolean isNotation(String s) {
            return false;
        }
    };
}
