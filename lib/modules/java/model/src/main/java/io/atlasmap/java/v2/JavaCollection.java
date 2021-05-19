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

import io.atlasmap.v2.Collection;

public class JavaCollection extends Collection implements Serializable {

    private static final long serialVersionUID = 1L;

    protected String collectionClassName;

    /**
     * Gets the value of the collectionClassName property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getCollectionClassName() {
        return collectionClassName;
    }

    /**
     * Sets the value of the collectionClassName property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCollectionClassName(String value) {
        this.collectionClassName = value;
    }

    public boolean equals(Object object) {
        if ((object == null)||(this.getClass()!= object.getClass())) {
            return false;
        }
        if (this == object) {
            return true;
        }
        if (!super.equals(object)) {
            return false;
        }
        final JavaCollection that = ((JavaCollection) object);
        {
            String leftCollectionClassName;
            leftCollectionClassName = this.getCollectionClassName();
            String rightCollectionClassName;
            rightCollectionClassName = that.getCollectionClassName();
            if (this.collectionClassName!= null) {
                if (that.collectionClassName!= null) {
                    if (!leftCollectionClassName.equals(rightCollectionClassName)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.collectionClassName!= null) {
                    return false;
                }
            }
        }
        return true;
    }

    public int hashCode() {
        int currentHashCode = 1;
        currentHashCode = ((currentHashCode* 31)+ super.hashCode());
        {
            currentHashCode = (currentHashCode* 31);
            String theCollectionClassName;
            theCollectionClassName = this.getCollectionClassName();
            if (this.collectionClassName!= null) {
                currentHashCode += theCollectionClassName.hashCode();
            }
        }
        return currentHashCode;
    }

}
