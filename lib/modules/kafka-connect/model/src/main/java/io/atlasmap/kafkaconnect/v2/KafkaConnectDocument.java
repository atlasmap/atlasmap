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
package io.atlasmap.kafkaconnect.v2;

import org.apache.kafka.connect.data.Schema.Type;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import io.atlasmap.v2.Document;

/**
 * The Document for Kafka Connect module.
 */
@JsonRootName("KafkaConnectDocument")
@JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.CLASS, property = "jsonType")
public class KafkaConnectDocument extends Document {

    private static final long serialVersionUID = 1L;
    /** Root schema type. */
    private Type rootSchemaType;
    /** True if it's an enum. */
    private boolean enumeration;
    /** Enum fields. */
    private KafkaConnectEnumFields enumFields;

    /**
     * Gets the root schema type.
     * @return root schema type
     */
    public Type getRootSchemaType() {
        return rootSchemaType;
    }

    /**
     * Sets the root schema type.
     * @param rootSchemaType root schema type
     */
    public void setRootSchemaType(Type rootSchemaType) {
        this.rootSchemaType = rootSchemaType;
    }

    /**
     * Gets the value of the enumeration property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public Boolean isEnumeration() {
        return enumeration;
    }

    /**
     * Sets the value of the enumeration property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setEnumeration(Boolean value) {
        this.enumeration = value;
    }

    /**
     * Gets the value of the enumFields property.
     *
     * @return
     *     possible object is
     *     {@link KafkaConnectEnumFields }
     *
     */
    public KafkaConnectEnumFields getEnumFields() {
        return enumFields;
    }

    /**
     * Sets the value of the enumFields property.
     *
     * @param value
     *     allowed object is
     *     {@link KafkaConnectEnumFields }
     *
     */
    public void setEnumFields(KafkaConnectEnumFields value) {
        this.enumFields = value;
    }

    @Override
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
        return true;
    }

    @Override
    public int hashCode() {
        int currentHashCode = 1;
        currentHashCode = ((currentHashCode* 31)+ super.hashCode());
        return currentHashCode;
    }

}
