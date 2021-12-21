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

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * The top container object of JSON Document inspection response that AtlasMap design time backend
 * service sends back to the UI in return for {@link JsonInspectionRequest}.
 */
@JsonRootName("JsonInspectionResponse")
@JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.CLASS, property = "jsonType")
public class JsonInspectionResponse implements Serializable {

    private static final long serialVersionUID = 1L;
    /** JSON Document object created as a result of the inspection. */
    protected JsonDocument jsonDocument;
    /** Error message. */
    protected String errorMessage;
    /** Execution time. */
    protected Long executionTime;

    /**
     * Gets the value of the jsonDocument property.
     * 
     * @return
     *     possible object is
     *     {@link JsonDocument }
     *     
     */
    public JsonDocument getJsonDocument() {
        return jsonDocument;
    }

    /**
     * Sets the value of the jsonDocument property.
     * 
     * @param value
     *     allowed object is
     *     {@link JsonDocument }
     *     
     */
    public void setJsonDocument(JsonDocument value) {
        this.jsonDocument = value;
    }

    /**
     * Gets the value of the errorMessage property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    /**
     * Sets the value of the errorMessage property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setErrorMessage(String value) {
        this.errorMessage = value;
    }

    /**
     * Gets the value of the executionTime property.
     * 
     * @return
     *     possible object is
     *     {@link Long }
     *     
     */
    public Long getExecutionTime() {
        return executionTime;
    }

    /**
     * Sets the value of the executionTime property.
     * 
     * @param value
     *     allowed object is
     *     {@link Long }
     *     
     */
    public void setExecutionTime(Long value) {
        this.executionTime = value;
    }

    @Override
    public boolean equals(Object object) {
        if ((object == null)||(this.getClass()!= object.getClass())) {
            return false;
        }
        if (this == object) {
            return true;
        }
        final JsonInspectionResponse that = ((JsonInspectionResponse) object);
        {
            JsonDocument leftJsonDocument;
            leftJsonDocument = this.getJsonDocument();
            JsonDocument rightJsonDocument;
            rightJsonDocument = that.getJsonDocument();
            if (this.jsonDocument!= null) {
                if (that.jsonDocument!= null) {
                    if (!leftJsonDocument.equals(rightJsonDocument)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.jsonDocument!= null) {
                    return false;
                }
            }
        }
        {
            String leftErrorMessage;
            leftErrorMessage = this.getErrorMessage();
            String rightErrorMessage;
            rightErrorMessage = that.getErrorMessage();
            if (this.errorMessage!= null) {
                if (that.errorMessage!= null) {
                    if (!leftErrorMessage.equals(rightErrorMessage)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.errorMessage!= null) {
                    return false;
                }
            }
        }
        {
            Long leftExecutionTime;
            leftExecutionTime = this.getExecutionTime();
            Long rightExecutionTime;
            rightExecutionTime = that.getExecutionTime();
            if (this.executionTime!= null) {
                if (that.executionTime!= null) {
                    if (!leftExecutionTime.equals(rightExecutionTime)) {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                if (that.executionTime!= null) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public int hashCode() {
        int currentHashCode = 1;
        {
            currentHashCode = (currentHashCode* 31);
            JsonDocument theJsonDocument;
            theJsonDocument = this.getJsonDocument();
            if (this.jsonDocument!= null) {
                currentHashCode += theJsonDocument.hashCode();
            }
        }
        {
            currentHashCode = (currentHashCode* 31);
            String theErrorMessage;
            theErrorMessage = this.getErrorMessage();
            if (this.errorMessage!= null) {
                currentHashCode += theErrorMessage.hashCode();
            }
        }
        {
            currentHashCode = (currentHashCode* 31);
            Long theExecutionTime;
            theExecutionTime = this.getExecutionTime();
            if (this.executionTime!= null) {
                currentHashCode += theExecutionTime.hashCode();
            }
        }
        return currentHashCode;
    }

}
