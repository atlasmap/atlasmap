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
package com.sun.xml.xsom.impl.parser;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Formats error messages.
 */
public class Messages
{
    /** Loads a string resource and formats it with specified arguments. */
    public static String format( String property, Object... args ) {
        String text = ResourceBundle.getBundle(
            Messages.class.getName()).getString(property);
        return MessageFormat.format(text,args);
    }
    
//
//
// Message resources
//
//
    public static final String ERR_UNDEFINED_SIMPLETYPE =
        "UndefinedSimpleType"; // arg:1
    public static final String ERR_UNDEFINED_COMPLEXTYPE =
        "UndefinedCompplexType"; // arg:1
    public static final String ERR_UNDEFINED_TYPE =
        "UndefinedType"; // arg:1
    public static final String ERR_UNDEFINED_ELEMENT =
        "UndefinedElement"; // arg:1
    public static final String ERR_UNDEFINED_MODELGROUP =
        "UndefinedModelGroup"; // arg:1
    public static final String ERR_UNDEFINED_ATTRIBUTE =
        "UndefinedAttribute"; // arg:1
    public static final String ERR_UNDEFINED_ATTRIBUTEGROUP =
        "UndefinedAttributeGroup"; // arg:1
    public static final String ERR_UNDEFINED_IDENTITY_CONSTRAINT =
        "UndefinedIdentityConstraint"; // arg:1
    public static final String ERR_UNDEFINED_PREFIX =
        "UndefinedPrefix"; // arg:1

    public static final String ERR_DOUBLE_DEFINITION =
        "DoubleDefinition"; // arg:1
    public static final String ERR_DOUBLE_DEFINITION_ORIGINAL =
        "DoubleDefinition.Original"; // arg:0
    
    public static final String ERR_MISSING_SCHEMALOCATION =
        "MissingSchemaLocation"; // arg:0
        
    public static final String ERR_ENTITY_RESOLUTION_FAILURE =
        "EntityResolutionFailure"; // arg:2

    public static final String ERR_SIMPLE_CONTENT_EXPECTED =
        "SimpleContentExpected"; // arg:2

    public static final String JAXP_UNSUPPORTED_PROPERTY =
        "JAXPUnsupportedProperty"; // arg:1

    public static final String JAXP_SUPPORTED_PROPERTY =
        "JAXPSupportedProperty"; // arg:1
}
