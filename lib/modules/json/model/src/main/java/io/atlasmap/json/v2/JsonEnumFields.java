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
package io.atlasmap.json.v2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class JsonEnumFields implements Serializable {

    private static final long serialVersionUID = 1L;

    protected List<JsonEnumField> jsonEnumField;

    /**
     * Gets the value of the jsonEnumField property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the jsonEnumField property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getJsonEnumField().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JsonEnumField }
     * @return A list of {@link JsonEnumField}
     * 
     */
    public List<JsonEnumField> getJsonEnumField() {
        if (jsonEnumField == null) {
            jsonEnumField = new ArrayList<JsonEnumField>();
        }
        return this.jsonEnumField;
    }

    public boolean equals(Object object) {
        if ((object == null)||(this.getClass()!= object.getClass())) {
            return false;
        }
        if (this == object) {
            return true;
        }
        final JsonEnumFields that = ((JsonEnumFields) object);
        {
            List<JsonEnumField> leftJsonEnumField;
            leftJsonEnumField = (((this.jsonEnumField!= null)&&(!this.jsonEnumField.isEmpty()))?this.getJsonEnumField():null);
            List<JsonEnumField> rightJsonEnumField;
            rightJsonEnumField = (((that.jsonEnumField!= null)&&(!that.jsonEnumField.isEmpty()))?that.getJsonEnumField():null);
            if ((this.jsonEnumField!= null)&&(!this.jsonEnumField.isEmpty())) {
                if ((that.jsonEnumField!= null)&&(!that.jsonEnumField.isEmpty())) {
                    if (!leftJsonEnumField.equals(rightJsonEnumField)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if ((that.jsonEnumField!= null)&&(!that.jsonEnumField.isEmpty())) {
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
            List<JsonEnumField> theJsonEnumField;
            theJsonEnumField = (((this.jsonEnumField!= null)&&(!this.jsonEnumField.isEmpty()))?this.getJsonEnumField():null);
            if ((this.jsonEnumField!= null)&&(!this.jsonEnumField.isEmpty())) {
                currentHashCode += theJsonEnumField.hashCode();
            }
        }
        return currentHashCode;
    }

}
