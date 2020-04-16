/**
 * Copyright (C) 2020 Red Hat, Inc.
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

public class ADMDigest {

    @JsonProperty("exportMappings")
    private ValueContainer exportMappings;
    @JsonProperty("exportMeta")
    private DataSourceMetadata[] exportMeta;
    @JsonProperty("exportBlockData")
    private ValueContainer[] exportBlockData;

    public ValueContainer getExportMappings() {
        return exportMappings;
    }

    public void setExportMappings(ValueContainer exportMappings) {
        this.exportMappings = exportMappings;
    }

    public DataSourceMetadata[] getExportMeta() {
        return exportMeta;
    }

    public void setExportMeta(DataSourceMetadata[] exportMeta) {
        this.exportMeta = exportMeta;
    }

    public ValueContainer[] getExportBlockData() {
        return exportBlockData;
    }

    public void setExportBlockData(ValueContainer[] exportBlockData) {
        this.exportBlockData = exportBlockData;
    }

}