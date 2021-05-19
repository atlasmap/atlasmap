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

public class DataSourceMetadata implements Serializable {

    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String documentType;
    private InspectionType inspectionType;
    @JsonProperty("isSource")
    private boolean isSource;
    @JsonIgnore
    private byte[] specification;
    private String dataSourceType;
    private Map<String,String> inspectionParameters;

    public static long getSerialversionuid() {
        return serialVersionUID;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public InspectionType getInspectionType() {
        return inspectionType;
    }

    public void setInspectionType(InspectionType inspectionType) {
        this.inspectionType = inspectionType;
    }

    public boolean getIsSource() {
        return isSource;
    }

    public void setIsSource(boolean isSource) {
        this.isSource = isSource;
    }

    public byte[] getSpecification() {
        return specification;
    }

    public void setSpecification(byte[] specification) {
        this.specification = specification;
    }

    public String getDataSourceType() {
        return dataSourceType;
    }

    public void setDataSourceType(String dataSourceType) {
        this.dataSourceType = dataSourceType;
    }

    public Map<String,String> getInspectionParameters() {
        return inspectionParameters;
    }

    public void setInspectionParameters(Map<String,String> inspectionParameters) {
        this.inspectionParameters = inspectionParameters;
    }

}
