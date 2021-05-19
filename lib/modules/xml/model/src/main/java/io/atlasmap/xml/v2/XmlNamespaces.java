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

public class XmlNamespaces implements Serializable {

    private static final long serialVersionUID = 1L;

    protected List<XmlNamespace> xmlNamespace;

    /**
     * Gets the value of the xmlNamespace property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the xmlNamespace property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getXmlNamespace().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link XmlNamespace }
     * @return A list of {@link XmlNamespace}
     * 
     */
    public List<XmlNamespace> getXmlNamespace() {
        if (xmlNamespace == null) {
            xmlNamespace = new ArrayList<XmlNamespace>();
        }
        return this.xmlNamespace;
    }

    public boolean equals(Object object) {
        if ((object == null)||(this.getClass()!= object.getClass())) {
            return false;
        }
        if (this == object) {
            return true;
        }
        final XmlNamespaces that = ((XmlNamespaces) object);
        {
            List<XmlNamespace> leftXmlNamespace;
            leftXmlNamespace = (((this.xmlNamespace!= null)&&(!this.xmlNamespace.isEmpty()))?this.getXmlNamespace():null);
            List<XmlNamespace> rightXmlNamespace;
            rightXmlNamespace = (((that.xmlNamespace!= null)&&(!that.xmlNamespace.isEmpty()))?that.getXmlNamespace():null);
            if ((this.xmlNamespace!= null)&&(!this.xmlNamespace.isEmpty())) {
                if ((that.xmlNamespace!= null)&&(!that.xmlNamespace.isEmpty())) {
                    if (!leftXmlNamespace.equals(rightXmlNamespace)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if ((that.xmlNamespace!= null)&&(!that.xmlNamespace.isEmpty())) {
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
            List<XmlNamespace> theXmlNamespace;
            theXmlNamespace = (((this.xmlNamespace!= null)&&(!this.xmlNamespace.isEmpty()))?this.getXmlNamespace():null);
            if ((this.xmlNamespace!= null)&&(!this.xmlNamespace.isEmpty())) {
                currentHashCode += theXmlNamespace.hashCode();
            }
        }
        return currentHashCode;
    }

}
