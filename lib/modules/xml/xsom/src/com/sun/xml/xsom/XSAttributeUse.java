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
 * Attribute use.
 * 
 * @author
 *  Kohsuke Kawaguchi (kohsuke.kawaguchi@sun.com)
 */
public interface XSAttributeUse extends XSComponent
{
    boolean isRequired();
    XSAttributeDecl getDecl();

    /**
     * Gets the default value of this attribute use, if one is specified.
     * 
     * Note that if a default value is specified in the attribute
     * declaration, this method returns that value.
     */
    XmlString getDefaultValue();

    /**
     * Gets the fixed value of this attribute use, if one is specified.
     * 
     * Note that if a fixed value is specified in the attribute
     * declaration, this method returns that value.
     */
    XmlString getFixedValue();
}
