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
package io.atlasmap.v2;

import java.io.Serializable;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The Data Source metadata.
 */
public class DataSourceMetadata implements Serializable {

    private static final long serialVersionUID = 1L;

    /** ID */
    private String id;
    /** name */
    private String name;
    /** Document type */
    private String documentType;
    /** inspection type */
    private InspectionType inspectionType;
    /** true if it's source Document, or false */
    @JsonProperty("isSource")
    private boolean isSource;
    /** specification */
    @JsonIgnore
    private byte[] specification;
    /** DataSource type */
    private String dataSourceType;
    /** inspection parameters */
    private Map<String,String> inspectionParameters;

    /**
     * Gets the serial version UUID.
     * @return serial version UUID
     */
    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    /**
     * Gets the ID.
     * @return ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the ID.
     * @param id ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the name.
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     * @param name name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the Document type.
     * @return Document type
     */
    public String getDocumentType() {
        return documentType;
    }

    /**
     * Sets the Document type.
     * @param documentType Document type
     */
    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    /**
     * Gets the inspection type.
     * @return inspection type
     */
    public InspectionType getInspectionType() {
        return inspectionType;
    }

    /**
     * Sets the inspection type.
     * @param inspectionType inspection type
     */
    public void setInspectionType(InspectionType inspectionType) {
        this.inspectionType = inspectionType;
    }

    /**
     * Gets if it's a source Document.
     * @return true if it's a source Document, or false
     */
    public boolean getIsSource() {
        return isSource;
    }

    /**
     * Sets if it's a source Document.
     * @param isSource true if it's a source Document, or false
     */
    public void setIsSource(boolean isSource) {
        this.isSource = isSource;
    }

    /**
     * Gets the specification.
     * @return specification
     */
    public byte[] getSpecification() {
        return specification;
    }

    /**
     * Sets the specification.
     * @param specification specification
     */
    public void setSpecification(byte[] specification) {
        this.specification = specification;
    }

    /**
     * Gets the DataSource type.
     * @return DataSource type
     */
    public String getDataSourceType() {
        return dataSourceType;
    }

    /**
     * Sets the DataSource type.
     * @param dataSourceType DataSource type.
     */
    public void setDataSourceType(String dataSourceType) {
        this.dataSourceType = dataSourceType;
    }

    /**
     * Gets the inspection parameters.
     * @return inspection parameters
     */
    public Map<String,String> getInspectionParameters() {
        return inspectionParameters;
    }

    /**
     * Sets the inspection parameters.
     * @param inspectionParameters inspection parameters
     */
    public void setInspectionParameters(Map<String,String> inspectionParameters) {
        this.inspectionParameters = inspectionParameters;
    }

}
