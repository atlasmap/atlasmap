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
package io.atlasmap.xml.v2;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class XmlEnumFields implements Serializable {

    private static final long serialVersionUID = 1L;

    protected List<XmlEnumField> xmlEnumField;

    /**
     * Gets the value of the xmlEnumField property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the xmlEnumField property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getXmlEnumField().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link XmlEnumField }
     * @return A list of {@link XmlEnumField}
     * 
     */
    public List<XmlEnumField> getXmlEnumField() {
        if (xmlEnumField == null) {
            xmlEnumField = new ArrayList<XmlEnumField>();
        }
        return this.xmlEnumField;
    }

    public boolean equals(Object object) {
        if ((object == null)||(this.getClass()!= object.getClass())) {
            return false;
        }
        if (this == object) {
            return true;
        }
        final XmlEnumFields that = ((XmlEnumFields) object);
        {
            List<XmlEnumField> leftXmlEnumField;
            leftXmlEnumField = (((this.xmlEnumField!= null)&&(!this.xmlEnumField.isEmpty()))?this.getXmlEnumField():null);
            List<XmlEnumField> rightXmlEnumField;
            rightXmlEnumField = (((that.xmlEnumField!= null)&&(!that.xmlEnumField.isEmpty()))?that.getXmlEnumField():null);
            if ((this.xmlEnumField!= null)&&(!this.xmlEnumField.isEmpty())) {
                if ((that.xmlEnumField!= null)&&(!that.xmlEnumField.isEmpty())) {
                    if (!leftXmlEnumField.equals(rightXmlEnumField)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if ((that.xmlEnumField!= null)&&(!that.xmlEnumField.isEmpty())) {
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
            List<XmlEnumField> theXmlEnumField;
            theXmlEnumField = (((this.xmlEnumField!= null)&&(!this.xmlEnumField.isEmpty()))?this.getXmlEnumField():null);
            if ((this.xmlEnumField!= null)&&(!this.xmlEnumField.isEmpty())) {
                currentHashCode += theXmlEnumField.hashCode();
            }
        }
        return currentHashCode;
    }

}
