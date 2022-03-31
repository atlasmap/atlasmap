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

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The ADM digest model class.
 * @deprecated Document metadata, specification and mapping definition are stored separately.
 * This is kept only for backward compatibility.
 */
@Deprecated
public class ADMDigest {

    @JsonProperty("exportMappings")
    private ValueContainer exportMappings;
    @JsonProperty("exportMeta")
    private DataSourceMetadata[] exportMeta;
    @JsonProperty("exportBlockData")
    private ValueContainer[] exportBlockData;

    /**
     * Gets exportMappings property.
     * @return exportMappings property
     */
    public ValueContainer getExportMappings() {
        return exportMappings;
    }

    /**
     * Sets exportMappings property.
     * @param exportMappings exportMappings property
     */
    public void setExportMappings(ValueContainer exportMappings) {
        this.exportMappings = exportMappings;
    }

    /**
     * Get an array of {@link DataSourceMetadata} property.
     * @return an array of {@link DataSourceMetadata}
     */
    public DataSourceMetadata[] getExportMeta() {
        return exportMeta;
    }

    /**
     * Sets an array of {@link DataSourceMetadata} property.
     * @param exportMeta an array of {@link DataSourceMetadata}
     */
    public void setExportMeta(DataSourceMetadata[] exportMeta) {
        this.exportMeta = exportMeta;
    }

    /**
     * Gets an array of exportBlockData property.
     * @return exportBlockData property
     */
    public ValueContainer[] getExportBlockData() {
        return exportBlockData;
    }

    /**
     * Sets an array of exportBlockData property.
     * @param exportBlockData exportBlockData property
     */
    public void setExportBlockData(ValueContainer[] exportBlockData) {
        this.exportBlockData = exportBlockData;
    }

}
