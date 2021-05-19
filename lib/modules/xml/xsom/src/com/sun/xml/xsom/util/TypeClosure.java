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
package com.sun.xml.xsom.util;

import com.sun.xml.xsom.XSType;

/**
 * Perform a transitive closure operation on a type to determine if it
 * belongs to this set. 
 * 
 * The contains method returns true if the TypeSet contains an instance
 * of the specified XSType or any of the base types of the XSType.
 * 
 * @author <a href="mailto:Ryan.Shoemaker@Sun.COM">Ryan Shoemaker</a>, Sun Microsystems, Inc.
 */
public class TypeClosure extends TypeSet {

    private final TypeSet typeSet;
    
    public TypeClosure(TypeSet typeSet) {
        this.typeSet = typeSet;
    }
    
    /* (non-Javadoc)
     * @see com.sun.xml.xsom.util.TypeSet#contains(com.sun.xml.xsom.XSDeclaration)
     * 
     * transitive closure variation on the contains method.
     */
    public boolean contains(XSType type) {
        if( typeSet.contains(type) ) {
            return true;
        } else {
            XSType baseType = type.getBaseType();
            if( baseType == null ) {
                return false;
            } else {
                // climb the super type hierarchy
                return contains(baseType);
            }
        }
    }

}
