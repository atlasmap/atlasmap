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

public class ModifierList implements Serializable {

    private static final long serialVersionUID = 1L;

    protected List<Modifier> modifier;

    /**
     * Gets the value of the modifier property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the modifier property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getModifier().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link Modifier }
     * @return A list of {@link Modifier}
     * 
     */
    public List<Modifier> getModifier() {
        if (modifier == null) {
            modifier = new ArrayList<Modifier>();
        }
        return this.modifier;
    }

    public boolean equals(Object object) {
        if ((object == null)||(this.getClass()!= object.getClass())) {
            return false;
        }
        if (this == object) {
            return true;
        }
        final ModifierList that = ((ModifierList) object);
        {
            List<Modifier> leftModifier;
            leftModifier = (((this.modifier!= null)&&(!this.modifier.isEmpty()))?this.getModifier():null);
            List<Modifier> rightModifier;
            rightModifier = (((that.modifier!= null)&&(!that.modifier.isEmpty()))?that.getModifier():null);
            if ((this.modifier!= null)&&(!this.modifier.isEmpty())) {
                if ((that.modifier!= null)&&(!that.modifier.isEmpty())) {
                    if (!leftModifier.equals(rightModifier)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if ((that.modifier!= null)&&(!that.modifier.isEmpty())) {
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
            List<Modifier> theModifier;
            theModifier = (((this.modifier!= null)&&(!this.modifier.isEmpty()))?this.getModifier():null);
            if ((this.modifier!= null)&&(!this.modifier.isEmpty())) {
                currentHashCode += theModifier.hashCode();
            }
        }
        return currentHashCode;
    }

}
