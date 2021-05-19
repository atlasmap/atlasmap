/*
 * Copyright (C) 2017 Red Hat, Inc.
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
package io.atlasmap.java.v2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class JavaEnumFields implements Serializable {

    private static final long serialVersionUID = 1L;

    protected List<JavaEnumField> javaEnumField;

    /**
     * Gets the value of the javaEnumField property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the javaEnumField property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getJavaEnumField().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JavaEnumField }
     * @return A list of {@link JavaEnumField}
     * 
     */
    public List<JavaEnumField> getJavaEnumField() {
        if (javaEnumField == null) {
            javaEnumField = new ArrayList<JavaEnumField>();
        }
        return this.javaEnumField;
    }

    public boolean equals(Object object) {
        if ((object == null)||(this.getClass()!= object.getClass())) {
            return false;
        }
        if (this == object) {
            return true;
        }
        final JavaEnumFields that = ((JavaEnumFields) object);
        {
            List<JavaEnumField> leftJavaEnumField;
            leftJavaEnumField = (((this.javaEnumField!= null)&&(!this.javaEnumField.isEmpty()))?this.getJavaEnumField():null);
            List<JavaEnumField> rightJavaEnumField;
            rightJavaEnumField = (((that.javaEnumField!= null)&&(!that.javaEnumField.isEmpty()))?that.getJavaEnumField():null);
            if ((this.javaEnumField!= null)&&(!this.javaEnumField.isEmpty())) {
                if ((that.javaEnumField!= null)&&(!that.javaEnumField.isEmpty())) {
                    if (!leftJavaEnumField.equals(rightJavaEnumField)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if ((that.javaEnumField!= null)&&(!that.javaEnumField.isEmpty())) {
                    return false;
                }
            }
        }
        return true;
    }

    public int hashCode() {
        int currentHashCode = 1;
        {
            currentHashCode = (currentHashCode* 31);
            List<JavaEnumField> theJavaEnumField;
            theJavaEnumField = (((this.javaEnumField!= null)&&(!this.javaEnumField.isEmpty()))?this.getJavaEnumField():null);
            if ((this.javaEnumField!= null)&&(!this.javaEnumField.isEmpty())) {
                currentHashCode += theJavaEnumField.hashCode();
            }
        }
        return currentHashCode;
    }

}
