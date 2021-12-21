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

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonRootName;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * The top container object of Kafka Connect Document inspection response that AtlasMap design time backend
 * service sends back to the UI in return for {@link KafkaConnectInspectionRequest}.
 */
@JsonRootName("KafkaConnectInspectionResponse")
@JsonTypeInfo(include = JsonTypeInfo.As.PROPERTY, use = JsonTypeInfo.Id.CLASS, property = "jsonType")
public class KafkaConnectInspectionResponse implements Serializable {

    private static final long serialVersionUID = 1L;
    /** The Kafka Connect Document created as a result of the inspection. */
    private KafkaConnectDocument kafkaConnectDocument;
    /** Error message. */
    private String errorMessage;
    /** Execution time. */
    private Long executionTime;

    /**
     * Gets the value of the kafkaCollectDocument property.
     * 
     * @return
     *     possible object is
     *     {@link KafkaConnectDocument }
     *     
     */
    public KafkaConnectDocument getKafkaConnectDocument() {
        return this.kafkaConnectDocument;
    }

    /**
     * Sets the value of the kafkaConnectDocument property.
     * 
     * @param value
     *     allowed object is
     *     {@link KafkaConnectDocument }
     *     
     */
    public void setKafkaConnectDocument(KafkaConnectDocument value) {
        this.kafkaConnectDocument = value;
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
}
