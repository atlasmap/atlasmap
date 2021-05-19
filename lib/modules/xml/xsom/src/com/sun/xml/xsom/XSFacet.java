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
 * Facet for a simple type.
 * 
 * @author
 *  Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface XSFacet extends XSComponent
{
    /** Gets the name of the facet, such as "length". */
    String getName();
    
    /** Gets the value of the facet. */
    XmlString getValue();
    
    /** Returns true if this facet is "fixed". */
    boolean isFixed();
    
    
    // well-known facet name constants
    final static String FACET_LENGTH            = "length";
    final static String FACET_MINLENGTH         = "minLength";
    final static String FACET_MAXLENGTH         = "maxLength";
    final static String FACET_PATTERN           = "pattern";
    final static String FACET_ENUMERATION       = "enumeration";
    final static String FACET_TOTALDIGITS       = "totalDigits";
    final static String FACET_FRACTIONDIGITS    = "fractionDigits";
    final static String FACET_MININCLUSIVE      = "minInclusive";
    final static String FACET_MAXINCLUSIVE      = "maxInclusive";
    final static String FACET_MINEXCLUSIVE      = "minExclusive";
    final static String FACET_MAXEXCLUSIVE      = "maxExclusive";
    final static String FACET_WHITESPACE        = "whiteSpace";
}
