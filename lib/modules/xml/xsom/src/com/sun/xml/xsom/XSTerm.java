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

import com.sun.xml.xsom.visitor.XSTermFunction;
import com.sun.xml.xsom.visitor.XSTermFunctionWithParam;
import com.sun.xml.xsom.visitor.XSTermVisitor;

/**
 * A component that can be referenced from {@link XSParticle}
 * 
 * This interface provides a set of type check functions (<code>isXXX</code>),
 * which are essentially:
 * 
 * <pre>
 * boolean isXXX() {
 *     return this instanceof XXX;
 * }
 * </pre>
 * 
 * and a set of cast functions (<code>asXXX</code>), which are
 * essentially:
 * 
 * <pre>
 * XXX asXXX() {
 *     if(isXXX())  return (XXX)this;
 *     else          return null;
 * }
 * </pre>
 */
public interface XSTerm extends XSComponent
{
    void visit( XSTermVisitor visitor );
    <T> T apply( XSTermFunction<T> function );
    <T,P> T apply( XSTermFunctionWithParam<T,P> function, P param );

    // cast functions
    boolean isWildcard();
    boolean isModelGroupDecl();
    boolean isModelGroup();
    boolean isElementDecl();

    XSWildcard asWildcard();
    XSModelGroupDecl asModelGroupDecl();
    XSModelGroup asModelGroup();
    XSElementDecl asElementDecl();
}
