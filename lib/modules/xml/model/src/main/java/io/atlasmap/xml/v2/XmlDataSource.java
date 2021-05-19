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

import com.fasterxml.jackson.annotation.JsonTypeInfo;

import io.atlasmap.v2.DataSource;

@JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.CLASS, property = "jsonType")
public class XmlDataSource extends DataSource implements Serializable {

    private static final long serialVersionUID = 1L;

    protected XmlNamespaces xmlNamespaces;

    protected String template;

    /**
     * Gets the value of the xmlNamespaces property.
     * 
     * @return
     *     possible object is
     *     {@link XmlNamespaces }
     *     
     */
    public XmlNamespaces getXmlNamespaces() {
        return xmlNamespaces;
    }

    /**
     * Sets the value of the xmlNamespaces property.
     * 
     * @param value
     *     allowed object is
     *     {@link XmlNamespaces }
     *     
     */
    public void setXmlNamespaces(XmlNamespaces value) {
        this.xmlNamespaces = value;
    }

    /**
     * Gets the value of the template property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getTemplate() {
        return template;
    }

    /**
     * Sets the value of the template property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTemplate(String value) {
        this.template = value;
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
        final XmlDataSource that = ((XmlDataSource) object);
        {
            XmlNamespaces leftXmlNamespaces;
            leftXmlNamespaces = this.getXmlNamespaces();
            XmlNamespaces rightXmlNamespaces;
            rightXmlNamespaces = that.getXmlNamespaces();
            if (this.xmlNamespaces!= null) {
                if (that.xmlNamespaces!= null) {
                    if (!leftXmlNamespaces.equals(rightXmlNamespaces)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.xmlNamespaces!= null) {
                    return false;
                }
            }
        }
        {
            String leftTemplate;
            leftTemplate = this.getTemplate();
            String rightTemplate;
            rightTemplate = that.getTemplate();
            if (this.template!= null) {
                if (that.template!= null) {
                    if (!leftTemplate.equals(rightTemplate)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.template!= null) {
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
            XmlNamespaces theXmlNamespaces;
            theXmlNamespaces = this.getXmlNamespaces();
            if (this.xmlNamespaces!= null) {
                currentHashCode += theXmlNamespaces.hashCode();
            }
        }
        {
            currentHashCode = (currentHashCode* 31);
            String theTemplate;
            theTemplate = this.getTemplate();
            if (this.template!= null) {
                currentHashCode += theTemplate.hashCode();
            }
        }
        return currentHashCode;
    }

}
